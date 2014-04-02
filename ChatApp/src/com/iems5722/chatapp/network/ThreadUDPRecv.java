package com.iems5722.chatapp.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.ContentValues;
import android.content.Context;
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
	
	//information for service to send ack back to
	private InetAddress sourceIPAddress;	
	
	//commands recognised by udp service
	public final static int UDP_INIT = 1;
	public final static int UDP_LISTEN = 2;
	
	public ThreadUDPRecv (Looper looper, Context serviceContext) {
		super(looper);
		Log.i(TAG, "Creating UDP Recv Thread");
		handlerReady = true;
		mContext = serviceContext;
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
			default:
				Log.e(TAG, "Unknown command: " + msg.what);
		}
	}
	
	//set up sockets for sending udp
	public void setupUDP() {
		try	{
			Log.i(TAG,"trying to create the datagram...");
			ServiceNetwork.serverSocket = new DatagramSocket(ServiceNetwork.UDP_PORT);
			ServiceNetwork.serverSocket.setBroadcast(true);	
			ServiceNetwork.SocketOK = true;
		} 
		catch(Exception e) {
			Log.e(TAG,"Cannot open socket"+e.getMessage());
			ServiceNetwork.SocketOK = false;
			return;
		}		
	}	
	
	//main part for handling incoming udp messages
	public void listenUDP() {
		//UDP socket
		byte[] receiveData = new byte[ServiceNetwork.Packet_Size]; 
		while(ServiceNetwork.SocketOK) {
			Log.i(TAG,  "in listening loop waiting for packet");
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
			try {
				ServiceNetwork.serverSocket.receive(receivePacket);
		        sourceIPAddress = receivePacket.getAddress();
		        Log.i(TAG,"Received a packet | Source IP Address: " + sourceIPAddress);
		        String message = new String(receivePacket.getData(),0,receivePacket.getLength());
	        	String msgType = MessageBuilder.getMessagePart(message, MessageBuilder.MsgType);
		        //check if it is own packet
		        if(sourceIPAddress.equals(ServiceNetwork.inetIPAddress)) {
		        	//network discovery success
		        	Log.d(TAG, message + " type " + msgType);		        	
		        	//if it is a request, send ack reply
		        	if (msgType.equals(MessageBuilder.PING_REQ_MSG)) {		        	
		        		Log.d(TAG,"Ping Method Success");
		        	}
		        }
		        else {
		        	Log.d(TAG, message + " type " + msgType);		        	
		        	//if it is a request, send ack reply
		        	if (msgType.equals(MessageBuilder.PING_REQ_MSG)) {
		    			Log.i(TAG,  "Ping received");
		    			//tell service to send ping ack back to user
		    			ServiceNetwork.udpPingReply(sourceIPAddress);		
		    			//TODO add user to database
		    			updateUser(message);
		        	}
		    		else if (msgType.equals(MessageBuilder.PING_ACK_MSG)) {
		    			Log.i(TAG,  "Ack received");			        		
		        		//TODO set user to active
		        		//TODO AddTestNewUser(message, sourceIPAddress);
		    		}
		        	else {
		        		Log.e(TAG, "Uknown message: " + message);
		        		
		        	}		    			
		        }
			}
			catch (Exception e) {
				Log.e(TAG,"Problems receiving packet: "+e.getMessage());
				Log.e(TAG, "Error message " + e);
				ServiceNetwork.SocketOK = false;
			} 	
		}
	}	
	
	//updates user database
	public void updateUser(String message) {
    	String msgType = MessageBuilder.getMessagePart(message, MessageBuilder.MsgType);
    	String msgUser = MessageBuilder.getMessagePart(message, MessageBuilder.MsgUser);
    	String msgContent = MessageBuilder.getMessagePart(message, MessageBuilder.MsgContent);
		ContentValues values = new ContentValues();
		
	}
}
