package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Calendar;

import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.database.TblUser;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ThreadMCRecv extends Handler{
	public final static String TAG = "MulticastReceiver";
	
	public final static int INITIAL_MUTLICAST = 1;
	public final static int MULTICAST_LISTEN = 2;
	public final static int MULTICAST_STOP = 3;
	
	private Context context;
	private Looper looper;
	private MessageBuilder msgBuilder;
	
	MulticastLock mLock;

	public ThreadMCRecv(Looper looper, Context currentContext) {
		super(looper);
		Log.d(TAG, "Creating service");
		this.context = currentContext;
		this.looper = looper;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case INITIAL_MUTLICAST :  
			initialMulticast();
			break;
		case MULTICAST_LISTEN :
			multicastListen();
			break;
		case MULTICAST_STOP:
			Log.d(TAG, "Stopping multicast");
			leaveGroup();
			closeSocket();
			break;
		default: break;
		}		
	}
	
	
	public void initialMulticast(){
		Log.d(TAG, "initial multicast");
		WifiManager wifi = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
	    mLock = wifi.createMulticastLock("mylock");
	    mLock.acquire();
	    try{
	    	try{
	    		ServiceNetwork.group = InetAddress.getByName("230.0.0.1");
			}
	    	catch (UnknownHostException e) {
	    		Log.e(TAG, "Error converting to inetaddress " + e.getMessage());
				e.printStackTrace();
			}
	    	ServiceNetwork.multiSocket = new MulticastSocket(ServiceNetwork.MULTI_PORT);
	    	ServiceNetwork.multiSocket.joinGroup(ServiceNetwork.group);
	    	ServiceNetwork.multicastGroup = true;
	    	ServiceNetwork.MC_SOCKET_OK = true;
	    	msgBuilder = new MessageBuilder(context);
	    }catch(Exception e){
	    	ServiceNetwork.MC_SOCKET_OK = false;
	    	Log.e(TAG, "Multicast Receiver initial problem : " + e.getMessage());
	    }
	}
	
	public void	multicastListen() {
			Log.d(TAG, "multicast listening");
			try {
				byte[] requestData = new byte[ServiceNetwork.Packet_Size];
		           
				while(ServiceNetwork.MC_SOCKET_OK)
					{
						Log.i(TAG, "in listening loop waiting for multicast");
						Log.i(TAG, mLock.toString());
						Log.i(TAG, Boolean.toString(mLock.isHeld()));
					 	DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length);
					 	ServiceNetwork.multiSocket.receive(requestPacket);

			            String requestString = new String(requestPacket.getData(), 0, requestPacket.getLength());
			            //Toast.makeText(context, requestString, Toast.LENGTH_SHORT).show();
			            Log.d(TAG,"Got Msg = "+ requestString); 
			            String msgType = MessageBuilder.getMessagePart(requestString, MessageBuilder.MsgType);
			            String msgSender = MessageBuilder.getMessagePart(requestString, MessageBuilder.MsgUser);
			            if(msgType.equals(MessageBuilder.GLOBAL_MSG) && !msgSender.equals(ServiceNetwork.user_id)) {
			            	//don't add string if is from self
			            	msgBuilder.saveGlobalMessage(requestString);
			            }
					}
				Log.d(TAG, "Stopping listen");
			} catch (Exception e) {
				Log.e(TAG, "problem when receiving msg " + e.getMessage());
			}
			/*this couldn't be invoked within the try loop
			 * finally{
				//clean up
				leaveGroup();
				closeSocket();
			}
			*/ 
	}
	
	public void updateGlobalMsg(String message) {
    	String msgType = MessageBuilder.getMessagePart(message, MessageBuilder.MsgType);
    	String msgUser = MessageBuilder.getMessagePart(message, MessageBuilder.MsgUser);
    	String msgContent = MessageBuilder.getMessagePart(message, MessageBuilder.MsgContent);
    	
    	Log.d(TAG, "Msg " + msgType + " UserId " + msgUser + " Username " + msgContent);
    	
    	Calendar c = Calendar.getInstance();
		long curDateTimeMS = c.getTimeInMillis();     	
    	    	
		ContentValues values = new ContentValues();
		values.put(TblGlobalChat.USER_ID, msgUser);
		values.put(TblGlobalChat.MESSAGE, msgContent);
		values.put(TblGlobalChat.MSG_DATETIME, curDateTimeMS);
		//add new user
		Uri itemUri = context.getApplicationContext().getContentResolver().insert(DbProvider.GCHAT_URI, values);
		Log.d(TAG, "Added new global message " + itemUri.toString());    	
	}
	
	public void leaveGroup() {
		try {
			Log.d(TAG, "Leaving group");
			ServiceNetwork.multiSocket.leaveGroup(ServiceNetwork.group);
			ServiceNetwork.multicastGroup = false;
		} catch (IOException e) {
			Log.e(TAG, "Could not leave group " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public void closeSocket()
	{
		try {
			Log.d(TAG, "Socket closed");
			ServiceNetwork.multiSocket.close();
			ServiceNetwork.MC_SOCKET_OK = false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Cannot stop TCP server " + e.getMessage());
		}
	}
}
