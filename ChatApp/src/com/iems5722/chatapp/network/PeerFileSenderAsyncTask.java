package com.iems5722.chatapp.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class PeerFileSenderAsyncTask  extends AsyncTask<String, Void, Void>{
	private static final String TAG = "PeerFileSenderAsyncTask";
	
	private Context context;
	
	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}

	
	@Override
	protected Void doInBackground(String... arg0) {
		PeerFileSender peerFileSenderThread = new PeerFileSender(context, mHandler);
	   	 if (!peerFileSenderThread.socketIsOK()) {
	       	   Log.e(TAG,"TCP file receiver port NOT STARTED");
	       	   Toast.makeText(context, "Cannot Start TCP file receiver port: ", Toast.LENGTH_LONG).show();
	       	   return null;        	
	         }
	   	 peerFileSenderThread.start();
		
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
