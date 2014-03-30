package com.iems5722.chatapp.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MulticastReceiverAsyncTask extends AsyncTask<String, Void, Void> {
private static final String TAG = "MuticastReceiverAsyncTask";
	
	private Context context;
	
	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}


	@Override
	protected Void doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		MulticastReceiver multicastReceiver = new MulticastReceiver(context, mHandler);
		
        if (!multicastReceiver.socketIsOK()) {
      	   Log.e(TAG,"TCP file receiver port NOT STARTED");
      	   Toast.makeText(context, "Cannot Start TCP file receiver port: ", Toast.LENGTH_LONG).show();
      	   return null;        	
        }
        multicastReceiver.start();
        Log.i(TAG,"TCP file receiver Started");
		
		return null;
	}
	

	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	String toastToMake= (String) msg.obj;
            Toast.makeText(context, toastToMake, Toast.LENGTH_SHORT).show();
        }
    };
}
