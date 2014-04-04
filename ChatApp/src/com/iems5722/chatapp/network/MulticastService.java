package com.iems5722.chatapp.network;

import com.iems5722.chatapp.network.PeerFileService.PeerFileServiceBinder;
import com.iems5722.chatapp.network.PeerFileService.PeerFileServiceHandler;

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
	
	private Handler UIhandler;
	private final IBinder multicastServiceBinder = new MulticastServiceBinder();
	private static Looper multicastServiceLooper;
	public static MulticastServiceHandler multicastServiceHandler;
	private MulticastSender multicastSenderHandler;
	private MulticastReceiver multicastReceiverHandler;
	
	
	
	public final static int INIT_THREAD = 0;
	public final static int SEND_MSG = 1;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Creating service");
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
	        				
		        			HandlerThread multicastSenderThread = new HandlerThread(MulticastSender.TAG);
		        			multicastSenderThread.start();
		    	    		Looper multicastSenderLooper =  multicastSenderThread.getLooper();
		    	    		multicastSenderHandler = new MulticastSender(multicastSenderLooper, getApplicationContext());
		    	    		
		    	    		HandlerThread multicastReceiverThread = new HandlerThread(MulticastReceiver.TAG);
		        			multicastReceiverThread.start();
		    	    		Looper multicastReceiverLooper =  multicastReceiverThread.getLooper();
		    	    		multicastReceiverHandler = new MulticastReceiver(multicastReceiverLooper, getApplicationContext());
		    	    		
		    	    		Log.d(TAG, "Invoke Receiver Thread");
		    	    		multicastReceiverHandler.obtainMessage(MulticastReceiver.INITIAL_MUTLICAST).sendToTarget();
		    	    		multicastReceiverHandler.obtainMessage(MulticastReceiver.MULTICAST_LISTEN).sendToTarget();
		    	    		
	        			 	break;
	        		case 	MulticastService.SEND_MSG:
	        				Log.d(TAG, "call sender to send message" + (String)msg.obj);
	        				multicastSenderHandler.obtainMessage(MulticastSender.INITIAL_MUTLICAST).sendToTarget();
	        				multicastSenderHandler.obtainMessage(MulticastSender.SEND_MSG, msg.obj).sendToTarget();
	        				break;
	        		default:break;
	        	}
	    	}
	 }
	

}
