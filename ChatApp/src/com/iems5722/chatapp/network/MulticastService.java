package com.iems5722.chatapp.network;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MulticastService extends Service{
	private final static String TAG = "MulticastService";
	
	//common definitions for multicast
	public final static int MULTI_PORT = 26669;
	//group set to 230.0.0.1
	public static InetAddress group;
	public static MulticastSocket multiSocket = null;
	static final int Packet_Size = 1024;
	
	
	private Handler UIhandler;
	private final IBinder multicastServiceBinder = new MulticastServiceBinder();
	private static Looper multicastServiceLooper;
	public static MulticastServiceHandler multicastServiceHandler;
	public MulticastSender multicastSenderHandler;
	public MulticastReceiver multicastReceiverHandler;
	
	//commands recognised by service
	public final static int INIT_THREAD = 0;
	public final static int SEND_MSG = 1;
	
	//checks if part of multicast group
	public static boolean multicastGroup = false;
	public static boolean socketOK = false;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Creating service");
		 try {
			group = InetAddress.getByName("230.0.0.1");
		} catch (UnknownHostException e) {
			Log.e(TAG, "Error converting to inetaddress " + e.getMessage());
			e.printStackTrace();
		}
		//create service thread
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		multicastServiceLooper = thread.getLooper();
		multicastServiceHandler = new MulticastServiceHandler(multicastServiceLooper); 
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		Log.d(TAG, "onStartCommand");
		UIhandler = new Handler(Looper.getMainLooper());
		multicastServiceHandler.obtainMessage(INIT_THREAD).sendToTarget();
		return Service.START_STICKY;
	}
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return multicastServiceBinder;
	}
	
	public class MulticastServiceBinder extends Binder {
		public MulticastService getService() {
			return MulticastService.this;
		}
	}
	
	public MulticastServiceHandler getServiceHandler() {
		return multicastServiceHandler;
	}
	
	@Override
	public void onDestroy() {
		//TODO stop multicast listeners
		if (multicastGroup) {
			Log.i(TAG, "Setting socket to false");
			multicastReceiverHandler.leaveGroup();
			multicastReceiverHandler.closeSocket();
		}
		stopSelf();
	}
	
	 public final class MulticastServiceHandler extends Handler {
	    	public MulticastServiceHandler(Looper looper) {
	    		super(looper);
	    	}
	    	
	    	@Override
	    	public void handleMessage(Message msg) {
	    		Log.d(TAG, "handling message");
	        	switch (msg.what) {
	        		case MulticastService.INIT_THREAD:
	        				Log.d(TAG, "Initial Sender and Receiver Thread");
	        				//multicast sending thread
		        			HandlerThread multicastSenderThread = new HandlerThread(MulticastSender.TAG);
		        			multicastSenderThread.start();
		    	    		Looper multicastSenderLooper =  multicastSenderThread.getLooper();
		    	    		multicastSenderHandler = new MulticastSender(multicastSenderLooper, getApplicationContext());
		    	    		//multicast recv thread
		    	    		HandlerThread multicastReceiverThread = new HandlerThread(MulticastReceiver.TAG);
		        			multicastReceiverThread.start();
		    	    		Looper multicastReceiverLooper =  multicastReceiverThread.getLooper();
		    	    		multicastReceiverHandler = new MulticastReceiver(multicastReceiverLooper, getApplicationContext());
		    	    		//get threads to initialise
		    	    		Log.d(TAG, "Invoke Receiver Thread");
		    	    		multicastReceiverHandler.obtainMessage(MulticastReceiver.INITIAL_MUTLICAST).sendToTarget();
		    	    		multicastReceiverHandler.obtainMessage(MulticastReceiver.MULTICAST_LISTEN).sendToTarget();
	        			 	break;
	        		case 	MulticastService.SEND_MSG:
	        				//tell sending thread to do work
	        				Log.d(TAG, "call sender to send message " + (String)msg.obj);
	        				multicastSenderHandler.obtainMessage(MulticastSender.SEND_MSG, msg.obj).sendToTarget();
	        				break;
	        		default:break;
	        	}
	    	}
	 }
}
