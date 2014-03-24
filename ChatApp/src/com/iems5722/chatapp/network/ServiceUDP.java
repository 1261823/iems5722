package com.iems5722.chatapp.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ServiceUDP extends Service {
	private final IBinder mBinder = new LocalBinder();	

	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		return Service.START_STICKY;
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		public ServiceUDP getService() {
			return ServiceUDP.this;
		}
	}	

	@Override
	public void onDestroy() {
		stopSelf();
	}	
	
}
