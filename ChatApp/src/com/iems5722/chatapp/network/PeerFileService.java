package com.iems5722.chatapp.network;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.iems5722.chatapp.gui.Activity_PrivateChat;

public class PeerFileService extends Service{
	private final static String TAG = "PeerFileService";
	private static Looper peerFileServiceLooper;
	private Handler uiHandler;
	private final IBinder peerFileServiceBinder = new PeerFileServiceBinder();
	
	public static PeerFileServiceHandler peerFileServiceHandler;
	private PeerFileSender peerFileSenderHandler;
	private PeerFileReceiver peerFileReceiverHandler;
	
	public final static int INIT_THREAD = 0;
	public final static int SEND_FILE = 1;
	public final static String MSG_TYPE_CHAT = "chatMsg";
	public final static String MSG_TYPE_FILE = "chatFile";
	
	
	
	public Handler getUiHandler() {
		return uiHandler;
	}


	public void setUiHandler(Handler uiHandler) {
		this.uiHandler = uiHandler;
	}


	@Override
	public void onCreate() {
		Log.d(TAG, "Creating service");
		//create service thread
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		peerFileServiceLooper = thread.getLooper();
		peerFileServiceHandler = new PeerFileServiceHandler(peerFileServiceLooper, this);
		
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		Log.d(TAG, "onStartCommand");
		uiHandler = new Handler(Looper.getMainLooper());
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
	
	public PeerFileSender getPeerFileSender(){
		return this.peerFileSenderHandler;		
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		peerFileReceiverHandler.setSocketOK(false);
		peerFileReceiverHandler.closeSocket();
		stopSelf();
	}
	
	public void previewFileProcess(Uri fileUri){
		uiHandler.obtainMessage(Activity_PrivateChat.DONE_AND_PREVIEW, fileUri).sendToTarget();
	}
	
	public void updateFileDownloadProgress(int percentage){
		uiHandler.obtainMessage(Activity_PrivateChat.UPDATE_PROGRESS_BAR, percentage).sendToTarget();		
	}
	
	public void initFileDownloadProgress(){
		uiHandler.obtainMessage(Activity_PrivateChat.INIT_PROGRESS_BAR).sendToTarget();	
	}
	
	
	 public final class PeerFileServiceHandler extends Handler {
		 	private PeerFileService peerFileService; 
		 
	    	public PeerFileServiceHandler(Looper looper, PeerFileService peerFileService) {
	    		super(looper);
	    		this.peerFileService  = peerFileService;
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
	    	    		peerFileReceiverHandler.setmHandler(uiHandler);
	    	    		peerFileReceiverHandler.setPeerFileService(this.peerFileService); 
	    	    		
	    	    		peerFileReceiverHandler.obtainMessage(PeerFileReceiver.INITIAL_TCP_PORT).sendToTarget();
	    	    		peerFileReceiverHandler.obtainMessage(PeerFileReceiver.TCP_LISTEN).sendToTarget();
	    	    		break;
	        		case PeerFileService.SEND_FILE:
	        			Log.d(TAG, "send file : "+ msg.obj);
	        			peerFileSenderHandler.obtainMessage(PeerFileSender.SEND_FILE, msg.obj).sendToTarget();
	        			break;
	        		default:
	        			Log.d(TAG, "Wrong request msg: "+ msg.what);
	        			break;	
	        	}
	        	
	        	
	    	}
	    	
	 }

}
