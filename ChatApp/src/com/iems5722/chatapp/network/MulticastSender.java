package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class MulticastSender extends Thread{
	final static private String TAG = "MulticastSender";
	public final static int MULTI_PORT = 26669;
	MulticastSocket multiSocket = null;
	boolean socketOK = true;
	private String msg;
	
	Context context;
	Handler handler;
	InetAddress group;
	
	public MulticastSender(Context currentContext, String msg, Handler handler) {
		this.context = currentContext;
		this.handler = handler;
		this.msg = msg;
		
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
	    }finally{
	    	
	    	
	    }
	
	
	}
	
	@Override
	public void run() {
		Looper.prepare();
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
