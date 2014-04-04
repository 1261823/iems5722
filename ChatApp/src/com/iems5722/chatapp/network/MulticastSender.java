package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MulticastSender extends Handler{
	public final static  String TAG = "MulticastSender";
	public final static int MULTI_PORT = 26669;
	private MulticastSocket multiSocket = null;
	private boolean socketOK = true;
		
	private Context context;
	private InetAddress group;
	private Looper looper;
	
	public final static int INITIAL_MUTLICAST = 1;
	public final static int SEND_MSG = 2;
	
	public MulticastSender(Looper looper, Context currentContext) {
		super(looper);
		this.context = currentContext;
		this.looper = looper;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case INITIAL_MUTLICAST :
			 initialMulticast();		
		     break;
		case SEND_MSG :  
			sendMulticastMsg((String)msg.obj);
			break;
			default: break;
		}		
	}
	
	
	public void initialMulticast(){
		Log.d(TAG, "inital multicast");
		
		WifiManager wifi = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
	    MulticastLock mLock = wifi.createMulticastLock("mylock");
	    mLock.acquire();
	    
	    try{
		    group = InetAddress.getByName("230.0.0.1");       
		    multiSocket = new MulticastSocket(MULTI_PORT);
		    multiSocket.joinGroup(group);
		    
	    }catch(Exception e){
	    	socketOK = false;
	    	Log.e(TAG, "Multicast Sender initial problem : " + e.getMessage());
	    }		
	}
	
	public void sendMulticastMsg(String msg) {
		Log.d(TAG, "send multicast msg");
		
		try {
			String requestString = msg;
			byte[] requestData = new byte[requestString.length()];
			requestData = requestString.getBytes();
			
			DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, group, MULTI_PORT);
	        multiSocket.send(requestPacket); 
			
		} catch (Exception e) {
			Log.e(TAG, "problem when sending files " + e.getMessage());
		}finally{
			try{
		    multiSocket.leaveGroup(group);
			closeSocket();
			}catch (Exception e){
				Log.e(TAG, "error when leave group " + e.getMessage());
			}
				
		}
		
	}
	
	public void closeSocket()
	{
		try {
			 multiSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Cannot stop TCP server " + e.getMessage());
		}
	}
	
	public boolean socketIsOK() {
		return socketOK;
	}
}
