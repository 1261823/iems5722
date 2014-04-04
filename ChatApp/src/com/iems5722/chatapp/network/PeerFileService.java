package com.iems5722.chatapp.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PeerFileService extends Service{
	private final static String TAG = "PeerFileService";
	private static Looper peerFileServiceLooper;
	private Handler UIhandler;
	private final IBinder peerFileServiceBinder = new PeerFileServiceBinder();
	
	public static PeerFileServiceHandler peerFileServiceHandler;
	private PeerFileSender peerFileSenderHandler;
	private PeerFileReceiver peerFileReceiverHandler;
	
	public final static int INIT_THREAD = 0;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Creating service");
		//create service thread
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		peerFileServiceLooper = thread.getLooper();
		peerFileServiceHandler = new PeerFileServiceHandler(peerFileServiceLooper); 
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		Log.d(TAG, "onStartCommand");
		UIhandler = new Handler(Looper.getMainLooper());
		peerFileServiceHandler.obtainMessage(INIT_THREAD).sendToTarget();
		return Service.START_STICKY;
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return peerFileServiceBinder;
	}
	
	public class PeerFileServiceBinder extends Binder {
		public PeerFileService getService() {
			//Log.d(TAG, "passing back service");
			return PeerFileService.this;
		}
	}
	
	
	
	public PeerFileServiceHandler getServiceHandler() {
		return peerFileServiceHandler;
	}
	
	@Override
	public void onDestroy() {
		//Log.d(TAG, "onDestroy");
		stopSelf();
	}
	
	 public final class PeerFileServiceHandler extends Handler {
	    	public PeerFileServiceHandler(Looper looper) {
	    		super(looper);
	    	}
	    	
	    	@Override
	    	public void handleMessage(Message msg) {
	    		Log.d(TAG, "handling message");
	        	switch (msg.what) {
	        		case PeerFileService.INIT_THREAD: 
	        			
	        			Log.d(TAG, "Initial Sender and Receiver Thread");
	        			
	        			HandlerThread peerFileSenderThread = new HandlerThread(PeerFileSender.TAG);
	        			peerFileSenderThread.start();
	    	    		Looper peerFileSenderLooper = peerFileSenderThread.getLooper();
	    	    		peerFileSenderHandler = new PeerFileSender(peerFileSenderLooper, getApplicationContext());
	    	    		
	        			HandlerThread peerFileReceiverThread = new HandlerThread(PeerFileReceiver.TAG);
	        			peerFileReceiverThread.start();
	    	    		Looper peerFileReceiverLooper = peerFileReceiverThread.getLooper();
	    	    		peerFileReceiverHandler = new PeerFileReceiver(peerFileReceiverLooper, getApplicationContext());
	    	    		
	    	    		Log.d(TAG, "Invoke Receiver Thread");
	    	    		peerFileReceiverHandler.obtainMessage(PeerFileReceiver.INITIAL_TCP_PORT).sendToTarget();
	    	    		peerFileReceiverHandler.obtainMessage(PeerFileReceiver.TCP_LISTEN).sendToTarget();
	    	    		break;
	        		default: break;	
	        	}
	    	}
	    	
	 }

}
