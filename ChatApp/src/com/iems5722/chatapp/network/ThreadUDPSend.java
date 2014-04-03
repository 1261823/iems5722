package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


//Thread that handles sending UDP packets
public class ThreadUDPSend extends Handler {
	public final static String TAG = "ThreadUDPSend";
	
	//references back to service
	boolean handlerReady = false;
	private Context mContext;	

	//message builder
	MessageBuilder msgBuilder;
	String username = "";

	//commands recognised by udp service
	public final static int     PING_REQUEST_ALL = 1;	
	public final static int     PING_REQUEST_ONE = 2;	
	public final static int     PING_ITERATE_ALL = 3;
	public final static int     PING_ACKNOWLEDGE = 4;
	public final static int		PREF_UPDATE_USER = 10;
	
	public ThreadUDPSend(Looper looper, Context serviceContext) {
		super(looper);
		//Log.i(TAG, "Creating UDP Sender Thread");
		mContext = serviceContext;
		handlerReady = true;
		msgBuilder = new MessageBuilder(mContext);
	}
	
	@Override
	public void handleMessage(Message msg) {
		//Log.i(TAG, "UDP Send Handler");
		String outMessage;
		InetAddress peerAddress;	
		switch(msg.what) {	
			case PING_REQUEST_ALL:
				Log.i(TAG, "PING_REQUEST_ALL");
				outMessage = msgBuilder.messageCreate(MessageBuilder.PING_REQ_MSG, username);
	    		Log.i(TAG, "PIA " + ServiceNetwork.BroadcastAddress + " " + outMessage);	        		
	    		udpSendMessage(outMessage, ServiceNetwork.BroadcastAddress);
				break;
			case PING_REQUEST_ONE:
				Log.i(TAG, "PING_REQUEST_ONE");
	    		peerAddress = (InetAddress) msg.obj;
	    		outMessage = msgBuilder.messageCreate(MessageBuilder.PING_REQ_MSG, username);
	    		udpSendMessage(outMessage, peerAddress);					
				break;
			case PING_ITERATE_ALL:
				Log.i(TAG, "PING_ITERATE_ALL");
				//send individual packet to all possible ip addresses based on subnet mask
				//use as last resort
	    		int outIP = ServiceNetwork.intFirstAddress;
	    		outMessage = msgBuilder.messageCreate(MessageBuilder.PING_REQ_MSG, username);
	    		for(long i = 0; outIP < ServiceNetwork.intBroadcastAddress; i++) {
	    			InetAddress outAddress;
					try {
						outAddress = InetAddress.getByAddress(ServiceNetwork.getIPAddress(outIP));
		        		udpSendMessage(outMessage, outAddress);	        			
					} catch (UnknownHostException e) {
			    		Log.e(TAG,"Invalid IP " + outIP + " conversion. Error Message "+ e.getMessage());
			    		Log.e(TAG, "Error message " + e);							
						e.printStackTrace();
					}		
	    		}
				break;
			case PING_ACKNOWLEDGE:
				Log.d(TAG, "PING_ACKNOWLEDGE");
	    		//Send username and WiFi MAC address back	
				outMessage = msgBuilder.messageCreate(MessageBuilder.PING_ACK_MSG, username);
	    		peerAddress = (InetAddress) msg.obj;
	    		Log.i(TAG, "ACK " + peerAddress + " " + outMessage);	        		
	        	udpSendMessage(outMessage, peerAddress);					
				break;
			case PREF_UPDATE_USER:
				username = (String) msg.obj;
				Log.d(TAG, "New username set: " + username);
				break;
			default:
				Log.e(TAG, "Unknown command: " + msg.what);		
		}
	}	

	public void udpSendMessage(String msg, InetAddress peerInetAddress) {
		byte[] sendData  = new byte[1024]; 
		sendData = msg.getBytes(); 
		InetAddress peerAddress = peerInetAddress;
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, peerAddress, ServiceNetwork.UDP_PORT);
		try {
			ServiceNetwork.serverSocket.send(sendPacket);
		} catch (IOException e) {
    		Log.e(TAG,"Cannot send message "+ e.getMessage());
    		Log.e(TAG, "Error message " + e);
    		e.printStackTrace();
		}
        Log.i(TAG,"Sent packet: " + msg);		
	}
	
	public void udpSendMessage(String msg, String peerStrAddress) {
		try {
			udpSendMessage(msg, InetAddress.getByName(peerStrAddress));
		} catch (UnknownHostException e) {
    		Log.e(TAG,"Cannot resolve host "+ e.getMessage());
    		Log.e(TAG, "Error message " + e);
			e.printStackTrace();
		}
	}	
}
