package com.iems5722.chatapp.network;

import java.net.DatagramSocket;
import java.net.InetAddress;

import com.iems5722.chatapp.gui.Activity_Login;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ServiceNetwork extends Service {
	private final static String TAG = "ServiceNetwork";
	
	//ServiceNetwork thread
	private static Looper mServiceLooper;
	public static ServiceHandler mServiceHandler;
	public final static int INIT_THREAD = 0;
	
	//references to threads started by service
	private static Looper looperNetwork;
	public static ThreadNetwork networkHandler;
	private static Looper looperUDPSend;
	public static ThreadUDPSend udpSendHandler;
	private static Looper looperUDPRecv;
	public static ThreadUDPRecv udpRecvHandler;	
	
	//Network parameters used by all threads
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

	//UDP details
	static final int UDP_PORT = 6666;
	static boolean SocketOK = false;
	static DatagramSocket serverSocket;  
	static final int Packet_Size = 1024;
	
	//binder to this service
	private final IBinder mBinder = new NetworkBinder();	
	boolean handlerReady = false;
	
	private Handler UIhandler;

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
    	}
    	
    	@Override
    	public void handleMessage(Message msg) {
    		Log.i(TAG, "handling message");
        	switch (msg.what) {
        	case(INIT_THREAD):
	    		//create network monitoring thread
	    		HandlerThread networkThread = new HandlerThread(ThreadNetwork.TAG);
	    		networkThread.start();
	    		looperNetwork = networkThread.getLooper();
	    		networkHandler = new ThreadNetwork(looperNetwork, getApplicationContext());		
	    		//create UDP sender thread
	    		HandlerThread udpSendThread = new HandlerThread(ThreadUDPSend.TAG);
	    		udpSendThread.start();
	    		looperUDPSend = udpSendThread.getLooper();
	    		udpSendHandler = new ThreadUDPSend(looperUDPSend, getApplicationContext());
	    		//create UDP receiver thread
	    		HandlerThread udpRecvThread = new HandlerThread(ThreadUDPRecv.TAG);
	    		udpRecvThread.start();
	    		looperUDPRecv = udpRecvThread.getLooper();
	    		udpRecvHandler = new ThreadUDPRecv(looperUDPRecv, getApplicationContext());
	    		//get threads to do work
	    		networkHandler.obtainMessage(ThreadNetwork.NTWK_INIT).sendToTarget();
	    		udpRecvHandler.obtainMessage(ThreadUDPRecv.UDP_INIT).sendToTarget();
	    		udpRecvHandler.obtainMessage(ThreadUDPRecv.UDP_LISTEN).sendToTarget();
        	}
    	}
    }

	@Override
	public void onCreate() {
		Log.d(TAG, "Creating service");
		//create service thread
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper); 
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		Log.d(TAG, "onStartCommand");
		UIhandler = new Handler(Looper.getMainLooper());
		mServiceHandler.obtainMessage(INIT_THREAD).sendToTarget();
		return Service.START_STICKY;
	}	
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		networkHandler.obtainMessage(ThreadNetwork.NTWK_SHUTDOWN).sendToTarget();
		stopSelf();
	}		
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class NetworkBinder extends Binder {
		public ServiceNetwork getService() {
			Log.d(TAG, "passing back service");
			return ServiceNetwork.this;
		}
	}	

	public static void udpPigAll() {
		Log.d(TAG, "Recv Ping Request All");
		udpSendHandler.obtainMessage(ThreadUDPSend.PING_REQUEST_ALL).sendToTarget();
	}
	
	public static void udpPingReply(InetAddress replyAddr) {
		udpSendHandler.obtainMessage(ThreadUDPSend.PING_ACKNOWLEDGE, replyAddr).sendToTarget();
	}
	
	public String getMACAddress() {
		return MACAddress;
	}
}
