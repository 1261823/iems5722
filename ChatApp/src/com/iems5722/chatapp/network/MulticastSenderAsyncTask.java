package com.iems5722.chatapp.network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MulticastSenderAsyncTask extends AsyncTask<String, Void, Void> {
private static final String TAG = "MuticastSenderAsyncTask";
	
	private Context context;
	private String msg;
	
	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}


	@Override
	protected Void doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		MulticastSender multicastSender = new MulticastSender(context,msg, mHandler);
        if (!multicastSender.socketIsOK()) {
      	   Log.e(TAG,"TCP file receiver port NOT STARTED");
      	   Toast.makeText(context, "Cannot Start TCP file receiver port: ", Toast.LENGTH_LONG).show();
      	   return null;        	
        }
        multicastSender.start();
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
