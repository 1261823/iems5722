package com.iems5722.chatapp.network;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.iems5722.chatapp.gui.Activity_Login;
import com.iems5722.chatapp.gui.Activity_TabHandler;
import com.iems5722.chatapp.gui.DialogWifiAvailable;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class ServiceNetwork extends Service {
	private final static String TAG = "ServiceNetwork";
	
	//ServiceNetwork thread
	private static Looper mServiceLooper;
	public static ServiceHandler mServiceHandler;
	public final static int INIT_THREAD = 0;
	public final static int SEND_PING_ACK = 5;
	public final static int SEND_PING_ALL = 6;
	public final static int PREF_NAME = 10;
	public final static int WIFI_INACTIVE = 20;
	public final static int DEREGISTER_WIFI_BCR = 21;
	public final static int WIFI_CONN = 22;
	public final static int WIFI_DC = 23;
	public final static int MC_SEND = 30;
	public final static int UDP_SEND = 31;
	
	//references to threads started by service
	private static Looper looperNetwork;
	public ThreadNetwork networkHandler;
	private static Looper looperUDPSend;
	public ThreadUDPSend udpSendHandler;
	private static Looper looperUDPRecv;
	public ThreadUDPRecv udpRecvHandler;	
	
	private static Looper looperMCSend;
	public ThreadMCSend mcSendHandler;
	private static Looper looperMCRecv;
	public ThreadMCRecv mcRecvHandler;
	
	//Network parameters used by all threads
	//own username
	static String		username;
	static String		user_id;
	//WiFi Parameters
	static int 			intIPAddress;
	static InetAddress 	inetIPAddress;
	static int			intNetMask;
	static InetAddress	inetNetMask;
	static String 		MACAddress;
	static InetAddress 	BroadcastAddress;
	static int			intBroadcastAddress;
	static int 			intFirstAddress;	
	static InetAddress 	inetFirstAddress;	
	//Existing WiFi parameters
	static int			currentIPAddress = -1;
	static int			currentNetMask = -1;	
	static boolean 		WiFiConnected = false;

	//UDP details
	static final int UDP_PORT = 6666;
	static boolean UDP_SOCKET_OK = false;
	static DatagramSocket serverSocket;  
	static final int Packet_Size = 1024;
	
	//common definitions for multicast
	public final static int MULTI_PORT = 26669;
	//group set to 230.0.0.1
	public static InetAddress group;
	public static MulticastSocket multiSocket = null;
	//checks if part of multicast group
	public static boolean multicastGroup = false;
	public static boolean MC_SOCKET_OK = false;
	
	
	//binder to this service
	private final IBinder mBinder = new NetworkBinder();	
	boolean handlerReady = false;
	
	private Handler UIhandler;
	
	public void setUIHandler(Handler mHandler) {
		this.UIhandler = mHandler;
	}
	
	public ServiceHandler getServiceHandler() {
		return mServiceHandler;
	}

	//method for converting int to ip address
    public static byte[] getIPAddress(int ip_int) {
        byte[] quads = new byte[4]; 
        for (int k = 0; k < 4; k++) 
           quads[k] = (byte) ((ip_int>> k * 8) & 0xFF);
		return quads;
    }
    
    public final class ServiceHandler extends Handler {
    	public ServiceHandler(Looper looper) {
    		super(looper);
    		Log.i(TAG, "Creating ServiceHandler Thread");
    	}
    	
    	@Override
    	public void handleMessage(Message msg) {
    		//Log.i(TAG, "handling message");
        	switch (msg.what) {
        	case(INIT_THREAD):
        		Log.i(TAG, "Initialising threads");
	    		//create network monitoring thread
	    		HandlerThread networkThread = new HandlerThread(ThreadNetwork.TAG);
	    		networkThread.start();
	    		looperNetwork = networkThread.getLooper();
	    		networkHandler = new ThreadNetwork(looperNetwork, getApplicationContext(), mServiceHandler);
	    		//create UDP sender thread
	    		HandlerThread udpSendThread = new HandlerThread(ThreadUDPSend.TAG);
	    		udpSendThread.start();
	    		looperUDPSend = udpSendThread.getLooper();
	    		udpSendHandler = new ThreadUDPSend(looperUDPSend, getApplicationContext());
	    		//create UDP receiver thread
	    		HandlerThread udpRecvThread = new HandlerThread(ThreadUDPRecv.TAG);
	    		udpRecvThread.start();
	    		looperUDPRecv = udpRecvThread.getLooper();
	    		udpRecvHandler = new ThreadUDPRecv(looperUDPRecv, getApplicationContext(), mServiceHandler);
	    		
				Log.d(TAG, "Initial Sender and Receiver Thread");
				//multicast sending thread
    			HandlerThread multicastSenderThread = new HandlerThread(ThreadMCSend.TAG);
    			multicastSenderThread.start();
    			looperMCSend =  multicastSenderThread.getLooper();
	    		mcSendHandler = new ThreadMCSend(looperMCSend, getApplicationContext());
	    		//multicast recv thread
	    		HandlerThread multicastReceiverThread = new HandlerThread(ThreadMCRecv.TAG);
    			multicastReceiverThread.start();
	    		looperMCRecv =  multicastReceiverThread.getLooper();
	    		mcRecvHandler = new ThreadMCRecv(looperMCRecv, getApplicationContext());	    		
	    		
	    		//get threads to do work
	    		networkHandler.obtainMessage(ThreadNetwork.NTWK_START_MONITOR).sendToTarget();
	    		udpRecvHandler.obtainMessage(ThreadUDPRecv.UDP_INIT).sendToTarget();
	    		udpRecvHandler.obtainMessage(ThreadUDPRecv.UDP_LISTEN).sendToTarget();
	    		
	    		mcRecvHandler.obtainMessage(ThreadMCRecv.INITIAL_MUTLICAST).sendToTarget();
	    		mcRecvHandler.obtainMessage(ThreadMCRecv.MULTICAST_LISTEN).sendToTarget();
	    		break;
        	case(WIFI_INACTIVE):
        		Log.i(TAG, "Recv WiFi inactive notice");
        		//don't show again if now showing
        		if (DialogWifiAvailable.notShowing) {
	        		//inform UI thread that wifi is disconnected
	        		UIhandler.obtainMessage(Activity_TabHandler.WIFI_INACTIVE).sendToTarget();
        		}
        		break;
        	case(DEREGISTER_WIFI_BCR):
        		networkHandler.obtainMessage(ThreadNetwork.NTWK_STOP_MONITOR).sendToTarget();
        		break;
        	case(SEND_PING_ACK):
        		//get udp thread to send ack to host
        		InetAddress pingTo = (InetAddress) msg.obj;
        		udpSendHandler.obtainMessage(ThreadUDPSend.PING_ACKNOWLEDGE, pingTo).sendToTarget();
        		break;
        	case(SEND_PING_ALL):
        		udpPingAll();
        		break;
        	case(MC_SEND):
        		//get multicast send thread to send message
				Log.d(TAG, "call sender to send message " + (String)msg.obj);
        		mcSendHandler.obtainMessage(ThreadMCSend.SEND_MSG, msg.obj).sendToTarget();
        		break;
        	case(UDP_SEND):
        		Log.d(TAG, "Calling UDP sender to send " + (String)msg.obj);
        		udpSendHandler.obtainMessage(ThreadUDPSend.SEND_MSG, msg.obj).sendToTarget();
        		break;
        	case(WIFI_CONN):
        		WiFiConnected = true;
        		break;
        	case(WIFI_DC):
        		WiFiConnected = false;
			    currentIPAddress = -1;
			    currentNetMask = -1;
			    break;
        	}
    	}
    }

	@Override
	public void onCreate() {
		//Log.d(TAG, "Creating service");
		//create service thread
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper); 
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		//Log.d(TAG, "onStartCommand");
		UIhandler = new Handler(Looper.getMainLooper());
		mServiceHandler.obtainMessage(INIT_THREAD).sendToTarget();
		return Service.START_STICKY;
	}	
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy - attempting to close udp ports");
		UDP_SOCKET_OK = false;
		networkHandler.obtainMessage(ThreadNetwork.NTWK_STOP_MONITOR).sendToTarget();
		//udpRecvHandler.obtainMessage(ThreadUDPRecv.UDP_CLOSE).sendToTarget();
		udpRecvHandler.stopUDP();
		Log.d(TAG, "stopping multicast");
		if (multicastGroup) {
			Log.i(TAG, "Setting socket to false");
			mcRecvHandler.leaveGroup();
			mcRecvHandler.closeSocket();
		}
		
		stopSelf();
	}		
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class NetworkBinder extends Binder {
		public ServiceNetwork getService() {
			//Log.d(TAG, "passing back service");
			return ServiceNetwork.this;
		}
	}	

	public void udpPingAll() {
		Log.d(TAG, "Recv Ping Request All");
		if(WiFiConnected && !username.isEmpty()) {
			Log.d(TAG, "sending request to handler");
			udpSendHandler.obtainMessage(ThreadUDPSend.PING_REQUEST_ALL).sendToTarget();
		}
		else {
			Log.d(TAG, "not ready to ping " + Boolean.toString(WiFiConnected) + " " + username);
			
		}
	}
	
	public void udpPingReply(InetAddress replyAddr) {
		if (WiFiConnected) {
			udpSendHandler.obtainMessage(ThreadUDPSend.PING_ACKNOWLEDGE, replyAddr).sendToTarget();
		}
	}
	
	public String getMACAddress() {
		if (WiFiConnected) {
			return MACAddress;
		}
		return null;
	}
	
	public void setUsername(String newname) {
		username = newname;
	}
	
	public void setUserid(String newid) {
		user_id = newid;
	}
}
