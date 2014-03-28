package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ServiceUDP_Send extends Service {
	private final static String TAG = "SenderUDP_Send";
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	boolean handlerReady = false;	
	
	//Sending messaging parameters
	private int 			PORT = 6666;	
	private DatagramSocket 	serverSocket;  			 
	private int				intIPAddress;
	private InetAddress  	inetIPAddress = null;	
	private int				intNetMask;
	private InetAddress  	inetNetMask = null;		
	private String 			MACAddress;
	
	private InetAddress 	BroadcastAddress;
	private int				intBroadcastAddress;
	private int 			intFirstAddress;	
	private InetAddress 	inetFirstAddress;
	
	//commands recognised by udp service
	public final static int 	INIT_BCR = 0;	
	public final static int 	PING_REQUEST_ALL = 1;	
	public final static int 	PING_REQUEST_ONE = 2;	
	public final static int 	PING_ITERATE_ALL = 3;
	public final static int 	PING_ACKNOWLEDGE = 4;
	public final static String PING_REQ_MSG = "PING_REQ";
	public final static String PING_ACK_MSG = "PING_ACK";
		
	//P2P Messaging components
	final static String msgSep = "-";		
	
    public static byte[] getIPAddress(int ip_int) {
        byte[] quads = new byte[4]; 
        for (int k = 0; k < 4; k++) 
           quads[k] = (byte) ((ip_int>> k * 8) & 0xFF);
		return quads;
    }	
	
	//handler that recevies message from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler (Looper looper) {
			super(looper);
			Log.i(TAG, "Creating ServiceHandler");
			handlerReady = true;
		}
		
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "UDP Service Handler");
			String outMessage;
			InetAddress peerAddress;	
			//TODO send user id from preferences
			String username = "username";
			String userId = "macaddresshash";
			switch(msg.what) {
				case INIT_BCR:
					Log.d(TAG, "INIT_BCR");
					setupBroadcastReceiver();
					break;
				case PING_REQUEST_ALL:
					Log.d(TAG, "PING_REQUEST_ALL");
	        		outMessage = PING_REQ_MSG + msgSep + userId + msgSep + username;
	        		Log.i(TAG, "PIA " + BroadcastAddress + " " + outMessage);	        		
	        		udpSendMessage(outMessage, BroadcastAddress);
					break;
				case PING_REQUEST_ONE:
					Log.d(TAG, "PING_REQUEST_ONE");
	        		peerAddress = (InetAddress) msg.obj;	
	        		outMessage = PING_REQ_MSG + msgSep + userId + msgSep + username;
	        		udpSendMessage(outMessage, peerAddress);					
					break;
				case PING_ITERATE_ALL:
					Log.d(TAG, "PING_ITERATE_ALL");
					//send individual packet to all possible ip addresses based on subnet mask
	        		int outIP = intFirstAddress;
	        		outMessage = PING_REQ_MSG + msgSep + userId + msgSep + username;
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
					Log.d(TAG, "PING_ACKNOWLEDGE");
	        		//Send username and WiFi MAC address back	
	        		String ackReply = PING_ACK_MSG + msgSep + userId + msgSep + username;
	        		peerAddress = (InetAddress) msg.obj;
	        		Log.i(TAG, "ACK " + peerAddress + " " + ackReply);	        		
	            	udpSendMessage(ackReply, peerAddress);					
					break;
				default:
					Log.e(TAG, "Unknown command: " + msg.what);
			}
		}
	}
	
	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceUDP_Recv");
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		Log.d(TAG, "onStartCommand");
		mServiceHandler.obtainMessage(INIT_BCR).sendToTarget();		
		return Service.START_STICKY;
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceBCR);
		stopSelf();
	}	
	
	private void setupBroadcastReceiver() {
		LocalBroadcastManager.getInstance(this).registerReceiver(serviceBCR, new IntentFilter(ServiceNetwork.BC_NETWORK_UPDATE));
		
	}
	
	private void updateNetworkDetail(Intent intent) {
		//update network details
		intIPAddress  = intent.getIntExtra(ServiceNetwork.NT_IP_INT, -1);
		inetIPAddress = (InetAddress) intent.getSerializableExtra(ServiceNetwork.NT_IP_INET);
		intNetMask = intent.getIntExtra(ServiceNetwork.NT_NM_INT, -1);
		inetNetMask = (InetAddress) intent.getSerializableExtra(ServiceNetwork.NT_NM_INET);
		MACAddress = intent.getStringExtra(ServiceNetwork.NT_MAC_ADDR);
		//update broadcast address
		try {
			intBroadcastAddress = (intIPAddress & intNetMask) | ~ intNetMask;
			BroadcastAddress = InetAddress.getByAddress(getIPAddress(intBroadcastAddress));
			intFirstAddress = (intIPAddress & intNetMask);
			inetFirstAddress = InetAddress.getByAddress(getIPAddress(intFirstAddress));
		} catch (UnknownHostException e) {
			Log.e(TAG, "WiFi details error");
		}		
	}
	
	private BroadcastReceiver serviceBCR = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received " + intent.getAction());
			if (intent.getAction().equals(ServiceNetwork.BC_NETWORK_UPDATE)) {
				updateNetworkDetail(intent);
				mServiceHandler.obtainMessage(PING_REQUEST_ALL).sendToTarget();
			}
			if (intent.getAction().equals(ServiceUDP_Recv.BC_PING_ACK)) {
				//send ack reply
				InetAddress returnAddress = (InetAddress) intent.getSerializableExtra(ServiceUDP_Recv.ACK_IPADDR);
		        mServiceHandler.obtainMessage(PING_ACKNOWLEDGE, returnAddress).sendToTarget();
			}
		}
	};	
	
	public void udpSendMessage(String msg, InetAddress peerInetAddress) {
		byte[] sendData  = new byte[1024]; 
		sendData = msg.getBytes(); 
		InetAddress peerAddress = peerInetAddress;
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, peerAddress, PORT);
		try {
			ServiceUDP_Recv.serverSocket.send(sendPacket);
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
}
