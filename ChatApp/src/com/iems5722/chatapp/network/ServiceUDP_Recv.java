package com.iems5722.chatapp.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class ServiceUDP_Recv extends Service {
	private final static String TAG = "ServiceUDP";
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private final IBinder mBinder = new LocalBinder();	
	
	//commands recognised by udp service
	public final static int UDP_PING_ALL = 1;

	
	//handler that recevies message from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler (Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "UDP Service Handler");
			switch(msg.what) {
				case UDP_PING_ALL:
					break;
			}
		}
	}
	
	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("UDPService");
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		return Service.START_STICKY;
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		public ServiceUDP_Recv getService() {
			return ServiceUDP_Recv.this;
		}
	}	

	@Override
	public void onDestroy() {
		stopSelf();
	}	
}
