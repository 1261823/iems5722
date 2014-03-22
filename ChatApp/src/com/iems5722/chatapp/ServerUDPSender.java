package com.iems5722.chatapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.iems5722.chatapp.ServerUDPReceiver.UserRecord;

public class ServerUDPSender extends Thread {

	final static private String TAG = "ServerUDPSender";
	
	//Objects from Main thread
	Context chatContext;
	Handler chatHandler;
	
	//Commands recognised by this threads handler
	public static Handler mHandler;	
	private Looper udpLooper;
	final static int 	PING_REQUEST_ALL = 1;	
	final static int 	PING_REQUEST_ONE = 2;	
	final static int 	PING_ITERATE_ALL = 3;
	final static int 	PING_ACKNOWLEDGE = 4;
	final static String PING_REQ_MSG = "PING_REQ";
    final static String PING_ACK_MSG = "PING_ACK";
	final static int 	MESSAGE_ALL = 10;
	final static int 	MESSAGE_ONE = 20;
    final static String MSG_ALL_MSG = "MSG_ALL";
	

	//Connection details from UDP Receiver
	private int 			serverPort;
	private String 			MACAddress;
	private InetAddress 	BroadcastAddress;
	private int				intBroadcastAddress;
	private DatagramSocket 	serverSocket;
	private int 			intFirstAddress;	
	private InetAddress 	inetFirstAddress;
	
	//P2P Messaging components
	final static String msgSep = "-";
	InetAddress 		peerAddress;		
	static Hashtable<String, UserRecord> UserHashtable;	
	
	class MessageObject {
		InetAddress peerIPAddress;		
		String message;
		
		public MessageObject(InetAddress peerIPAddress, String message) {
			this.peerIPAddress = peerIPAddress;
			this.message = message;
		}
	}
	
	public ServerUDPSender(Context mContext, Handler mHandler) {
		chatContext = mContext;
		chatHandler = mHandler;
		updateConnectionDetails();
	}

	public void updateConnectionDetails() {
		MACAddress = ServerUDPReceiver.MACAddress;
		intFirstAddress = ServerUDPReceiver.intFirstAddress;
		inetFirstAddress = ServerUDPReceiver.inetFirstAddress;
		BroadcastAddress = ServerUDPReceiver.BroadcastAddress;
		intBroadcastAddress = ServerUDPReceiver.intBroadcastAddress;
		serverSocket = ServerUDPReceiver.serverSocket;
		serverPort = ServerUDPReceiver.PORT;
		UserHashtable = ServerUDPReceiver.UserHashtable;
	}

	public void udpSendMessage(String msg, InetAddress peerInetAddress) {
		byte[] sendData  = new byte[1024]; 
		sendData = msg.getBytes(); 
		peerAddress = peerInetAddress;
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, peerAddress, serverPort);
		try {
			serverSocket.send(sendPacket);
		} catch (IOException e) {
    		Log.e(TAG,"Cannot send message "+ e.getMessage());
    		Log.e(TAG, "Error message " + e);
    		e.printStackTrace();
		}
        Log.i(TAG,"Sent packet: "+msg);		
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
	
	public static Handler getHandler() {
		return mHandler;
	}	
	
	public void createHandler() {
		//Handler to receive messages
		mHandler = new Handler() {
			String outMessage;
			InetAddress peerAddress;	
	        @Override
	        public void handleMessage(Message msg) {
            	Log.i(TAG, "UDP Send Message Handler");
	        	switch (msg.what) {
	        	case PING_REQUEST_ALL:
	        		outMessage = PING_REQ_MSG + msgSep + MACAddress + msgSep + Chat.username;
	                chatHandler.obtainMessage(Chat.TOAST, "Checking for new users").sendToTarget();
	        		Log.i(TAG, "PIA " + BroadcastAddress + " " + outMessage);	        		
	        		udpSendMessage(outMessage, BroadcastAddress);
	        		break;
	        	case PING_REQUEST_ONE:
	        		peerAddress = (InetAddress) msg.obj;	
	        		outMessage = PING_REQ_MSG + msgSep + MACAddress + msgSep + Chat.username;
	                chatHandler.obtainMessage(Chat.TOAST, "Pinging user " + BroadcastAddress + " " + outMessage).sendToTarget();
	        		Log.i(TAG, "PIO " + peerAddress + " " + outMessage);	        		
	        		udpSendMessage(outMessage, peerAddress);
	        		break;	
	        	case PING_ITERATE_ALL:
	        		//send individual packet to all possible ip addresses based on subnet mask
	        		int outIP = intFirstAddress;
	        		outMessage = PING_REQ_MSG + msgSep + MACAddress + msgSep + Chat.username;
	                chatHandler.obtainMessage(Chat.TOAST, "Iterating through all IP addresses").sendToTarget();
	        		for(long i = 0; outIP < intBroadcastAddress; i++) {
	        			InetAddress outAddress;
						try {
							outAddress = InetAddress.getByAddress(ServerUDPReceiver.getIPAddress(outIP));
			        		udpSendMessage(outMessage, outAddress);	        			
						} catch (UnknownHostException e) {
				    		Log.e(TAG,"Invalid IP " + outIP + " conversion. Error Message "+ e.getMessage());
				    		Log.e(TAG, "Error message " + e);							
							e.printStackTrace();
						}
	        		}	        		
	        		break;
	            case PING_ACKNOWLEDGE:
	            	InetAddress peerAddress = (InetAddress) msg.obj;	
	        		//Send username and WiFi MAC address back	
	        		String ackReply = PING_ACK_MSG + msgSep + MACAddress + msgSep + Chat.username;
	        		Log.i(TAG, "ACK " + peerAddress + " " + ackReply);	        		
	            	udpSendMessage(ackReply, peerAddress);
	                break;
	            case MESSAGE_ONE:
	            	String[] messageDetail = (String[]) msg.obj;
	            	String outMessageOne = MSG_ALL_MSG + msgSep + MACAddress + msgSep + messageDetail[0];
	            	udpSendMessage(outMessageOne, messageDetail[1]);
	            	break;
	            case MESSAGE_ALL:
	            	String typedMsg = (String) msg.obj;	
	            	String outMessage = MSG_ALL_MSG + msgSep + MACAddress + msgSep + typedMsg;
	            	
            		Iterator<Entry<String, UserRecord>> it;
            		Map.Entry<String, UserRecord> entry;
            		it = UserHashtable.entrySet().iterator();
            		while(it.hasNext()) {
            			entry = it.next();
            			String key = entry.getKey();
            			InetAddress outAddress = UserHashtable.get(key).peerIPAddress;
						Log.d(TAG, "MA " + outAddress + " " + outMessage);
						udpSendMessage(outMessage, outAddress);
	            	}
	            	break;
	            }
	        }
	    };		
	}
	
	public void updateHandlers() {
        chatHandler.obtainMessage(Chat.UDP_SENDER_UPDATE).sendToTarget();
	}

	
	@Override
	public void run() {
		Looper.prepare();
		createHandler();
		updateHandlers();
		Looper.loop();	
	}
	
	public void closeSender()
	{
		udpLooper.quit();
	}	
	
}
