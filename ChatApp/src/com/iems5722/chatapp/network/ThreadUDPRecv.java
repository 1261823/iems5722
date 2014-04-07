package com.iems5722.chatapp.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblUser;
import com.iems5722.chatapp.network.ServiceNetwork.ServiceHandler;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

//Thread that handles receiving UDP packets and opening UDP port
public class ThreadUDPRecv extends Handler {
	public final static String TAG = "ThreadUDPRecv";
	
	//references back to service
	boolean handlerReady = false;
	private Context mContext;	
	private ServiceHandler mServiceHandler;
	
	//information for service to send ack back to
	private InetAddress sourceIPAddress;	
	
	//commands recognised by udp service
	public final static int UDP_INIT = 1;
	public final static int UDP_LISTEN = 2;
	public final static int UDP_CLOSE = 3;
	
	//List of users to recognise messages from
	List<UserDetail> UserList = new ArrayList<UserDetail>();
	List<String> UserIdList = new ArrayList<String>();

	
	public ThreadUDPRecv (Looper looper, Context serviceContext, ServiceHandler serviceHandler) {
		super(looper);
		Log.i(TAG, "Creating UDP Recv Thread");
		handlerReady = true;
		mContext = serviceContext;
		mServiceHandler = serviceHandler;
	}
	@Override
	public void handleMessage(Message msg) {
		Log.i(TAG, "UDP Recv Handler");
		switch(msg.what) {
			case UDP_INIT:
				setupUDP();
				break;
			case UDP_LISTEN:
				listenUDP();
				break;
			case UDP_CLOSE:
				stopUDP();
				break;
			default:
				Log.e(TAG, "Unknown command: " + msg.what);
		}
	}
	
	//set up sockets for sending udp
	private void setupUDP() {
		try	{
			//Log.i(TAG,"trying to create the datagram...");
			ServiceNetwork.serverSocket = new DatagramSocket(ServiceNetwork.UDP_PORT);
			ServiceNetwork.serverSocket.setBroadcast(true);	
			ServiceNetwork.UDP_SOCKET_OK = true;
		} 
		catch(Exception e) {
			Log.e(TAG,"Cannot open socket"+e.getMessage());
			ServiceNetwork.UDP_SOCKET_OK = false;
			return;
		}		
	}	
	
	//main part for handling incoming udp messages
	private void listenUDP() {
		try {
			//UDP socket
			byte[] receiveData = new byte[ServiceNetwork.Packet_Size]; 
			while(ServiceNetwork.UDP_SOCKET_OK) {
				Log.i(TAG,  "in listening loop waiting for packet");
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 

				ServiceNetwork.serverSocket.receive(receivePacket);
		        sourceIPAddress = receivePacket.getAddress();
		        Log.i(TAG,"Received a packet | Source IP Address: " + sourceIPAddress);
		        String message = new String(receivePacket.getData(),0,receivePacket.getLength());
	        	String msgType = MessageBuilder.getMessagePart(message, MessageBuilder.MsgType);
		        //check if it is own packet
		        if(sourceIPAddress.equals(ServiceNetwork.inetIPAddress)) {
		        	//network discovery success
		        	//Log.d(TAG, message + " type " + msgType);		        	
		        	//if it is a request, send ack reply
		        	if (msgType.equals(MessageBuilder.PING_REQ_MSG)) {		        	
		        		Log.d(TAG,"Ping from self received successfully");
		        	}
		        }
		        else {
		        	Log.d(TAG, message + " type " + msgType);		
		        	getAllUserId();
		        	//if it is a request, send ack reply
		        	if (msgType.equals(MessageBuilder.PING_REQ_MSG)) {
		    			Log.i(TAG,  "Ping received");
		    			//tell service to send ping ack back to user
		    			mServiceHandler.obtainMessage(ServiceNetwork.SEND_PING_ACK, sourceIPAddress).sendToTarget();	
		        	}
	    			updateUser(message);
		        }
			}
		}
		catch (Exception e) {
			Log.e(TAG,"Problems receiving packet: "+e.getMessage());
			ServiceNetwork.UDP_SOCKET_OK = false;
		} 	
		Log.d(TAG, "Socket not ok");
	}	
	
	//clean up
	public void stopUDP() {
		try	{
			Log.i(TAG,"Stopping UDP service");
			ServiceNetwork.serverSocket.setBroadcast(false);
			ServiceNetwork.serverSocket.close();
			ServiceNetwork.UDP_SOCKET_OK = false;
		} 
		catch(Exception e) {
			Log.e(TAG,"Cannot close socket"+e.getMessage());
			return;
		}			
	}
	
	//updates user database
	public void updateUser(String message) {
    	String msgType = MessageBuilder.getMessagePart(message, MessageBuilder.MsgType);
    	String msgUser = MessageBuilder.getMessagePart(message, MessageBuilder.MsgUser);
    	String msgContent = MessageBuilder.getMessagePart(message, MessageBuilder.MsgContent);
    	Log.d(TAG, "Msg " + msgType + " UserId " + msgUser + " Username " + msgContent);
    	
		Calendar c = Calendar.getInstance();
		long curDateTimeMS = c.getTimeInMillis();     	
    	    	
		ContentValues values = new ContentValues();
		int mRowId = 0;
		values.put(TblUser.USER_ID, msgUser);
		values.put(TblUser.USER_NAME, msgContent);
		values.put(TblUser.IP_ADDR_INT, 0);
		values.put(TblUser.IP_ADDR_STR, sourceIPAddress.toString());
		if (msgType.equals(MessageBuilder.SIGN_OUT)) {
			values.put(TblUser.STATUS, TblUser.STAT_OFF);
		}
		else {
			values.put(TblUser.STATUS, TblUser.STAT_ON);
		}
		values.put(TblUser.USER_DATETIME, curDateTimeMS);
		
		
		if (UserIdList.contains(msgUser)) {
			//update user with new information
			int index = UserIdList.indexOf(msgUser);
			mRowId = UserList.get(index).dbUserId;
			
			Log.d(TAG, "Updating user " + mRowId);
			Log.d(TAG, "User MD5 is " + UserList.get(index).md5UserId);
			int count = mContext.getApplicationContext().getContentResolver().update(
					ContentUris.withAppendedId(DbProvider.USER_URI, mRowId), values, null, null);
			if (count != 1) {
				throw new IllegalStateException("Unable to update " + mRowId);
			}
		}
		else {
			//add new user
			Uri itemUri = mContext.getApplicationContext().getContentResolver().insert(DbProvider.USER_URI, values);
			Log.d(TAG, "Creating new user " + itemUri.toString());
		}
	}

	private void getAllUserId() {
		SQLiteDatabase db = DbProvider.database.getReadableDatabase();
		String[] projection = {TblUser.USER_UFI, TblUser.USER_ID};
		Cursor cursor = db.query(TblUser.TABLE_USER, projection, null, null, null, null, null);
		UserList.clear();
		UserIdList.clear();
		if (cursor.moveToFirst()) {
			do {
				UserDetail newUser = new UserDetail(cursor.getInt(0), cursor.getString(1));
				UserList.add(newUser);
				UserIdList.add(cursor.getString(1));
			}
			while (cursor.moveToNext());
		}
		Log.i(TAG, "Found existing users : " + UserList.size());
		for (int i = 0; i < UserList.size(); i ++) {
			Log.i(TAG, UserList.get(i).dbUserId + " : " + UserList.get(i).md5UserId);
		}
	}
}
