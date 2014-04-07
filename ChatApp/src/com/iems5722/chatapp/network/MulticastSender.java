package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.DatagramPacket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MulticastSender extends Handler{
	public final static  String TAG = "MulticastSender";

	private Context context;
	private Looper looper;
	MessageBuilder msgBuilder;
	
	
	public final static int SEND_MSG = 0;
	
	public MulticastSender(Looper looper, Context currentContext) {
		super(looper);
		Log.d(TAG, "Creating service");
		this.context = currentContext;
		this.looper = looper;
		msgBuilder = new MessageBuilder(context);
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
		if (MulticastService.multicastGroup && MulticastService.socketOK) {
			try {
				String outMessage = msgBuilder.messageCreate(MessageBuilder.GLOBAL_MSG, msg);
				byte[] requestData = new byte[1024];
				requestData = outMessage.getBytes();
				Log.d(TAG, Integer.toString(requestData.length));
				
				DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, MulticastService.group, MulticastService.MULTI_PORT);
		        MulticastService.multiSocket.send(requestPacket); 
				Log.d(TAG, "Sent message " + outMessage);
				Log.d(TAG, "Group status " + MulticastService.multicastGroup);
				Log.d(TAG, "MC service status " + MulticastService.socketOK);
			} catch (IOException e) {
				Log.e(TAG, "problem when sending files " + e);
			}
		}
		else {
			Log.d(TAG, "Did not send");
			Log.d(TAG, "Group status " + MulticastService.multicastGroup);
			Log.d(TAG, "MC service status " + MulticastService.socketOK);
		}
	}
}
