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

public class MulticastReceiver extends Thread{
	final static private String TAG = "MulticastReceiver";
	public final static int MULTI_PORT = 26669;
	MulticastSocket multiSocket = null;
	boolean socketOK = true;
	static final int TOAST  = 2;
	
	Context context;
	Handler handler;
	
	public MulticastReceiver(Context currentContext, Handler handler) {
		this.context = currentContext;
		this.handler = handler;
		
		
		
		WifiManager wifi = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
	    MulticastLock mLock = wifi.createMulticastLock("mylock");
	    mLock.acquire();
	    
	    try{
		    InetAddress group = InetAddress.getByName("230.0.0.1");       
		    multiSocket = new MulticastSocket(MULTI_PORT);
		    multiSocket.joinGroup(group);
	    }catch(Exception e){
	    	 socketOK = false;
	    	Log.e(TAG, "Multicast Receiver initial problem : " + e.getMessage());
	    }
	
	
	}
	
	@Override
	public void run() {
			try {
				Looper.prepare();
				byte[] requestData = new byte[1024];

				 while(true)
			       {
			            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length);
			            multiSocket.receive(requestPacket);

			            String requestString = new String(requestPacket.getData(), 0, requestPacket.getLength());
			            handler.obtainMessage(TOAST, requestString).sendToTarget();
			            Log.d(TAG,"Got Msg = "+ requestString); 
			       }
				
			} catch (Exception e) {
				Log.e(TAG, "problem when receiving msg " + e.getMessage());
			}finally{
				closeSocket();
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
