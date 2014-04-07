package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.DatagramPacket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ThreadMCSend extends Handler{
	public final static  String TAG = "MulticastSender";

	private Context context;
	private Looper looper;
	
	public final static int SEND_MSG = 0;
	
	public ThreadMCSend(Looper looper, Context currentContext) {
		super(looper);
		Log.d(TAG, "Creating service");
		this.context = currentContext;
		this.looper = looper;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case SEND_MSG :  
			sendMulticastMsg((String)msg.obj);
			break;
		default: break;
		}		
	}
	
	public void sendMulticastMsg(String msg) {
		Log.d(TAG, "send multicast msg " + msg);
		//only send if currently part of group and socket is ok
		if (ServiceNetwork.multicastGroup && ServiceNetwork.MC_SOCKET_OK) {
			try {
				byte[] requestData = new byte[1024];
				requestData = msg.getBytes();
				Log.d(TAG, Integer.toString(requestData.length));
				
				DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, ServiceNetwork.group, ServiceNetwork.MULTI_PORT);
				ServiceNetwork.multiSocket.send(requestPacket); 
				Log.d(TAG, "Sent message " + msg);
				Log.d(TAG, "Group status " + ServiceNetwork.multicastGroup);
				Log.d(TAG, "MC service status " + ServiceNetwork.MC_SOCKET_OK);
			} catch (IOException e) {
				Log.e(TAG, "problem when sending files " + e);
			}
		}
		else {
			Log.d(TAG, "Did not send");
			Log.d(TAG, "Group status " + ServiceNetwork.multicastGroup);
			Log.d(TAG, "MC service status " + ServiceNetwork.MC_SOCKET_OK);
		}
	}
}
