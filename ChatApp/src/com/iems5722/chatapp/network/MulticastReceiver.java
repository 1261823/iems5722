package com.iems5722.chatapp.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MulticastReceiver extends Handler{
	public final static String TAG = "MulticastReceiver";
	public final static int MULTI_PORT = 26669;
	private MulticastSocket multiSocket = null;
	private boolean socketOK = true;
	
	public final static int INITIAL_MUTLICAST = 1;
	public final static int MULTICAST_LISTEN = 2;
	
	private Context context;
	private Looper looper;

	public MulticastReceiver(Looper looper, Context currentContext) {
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
		case MULTICAST_LISTEN :
			multicastListen();
			break;
			default: break;
		}		
	}
	
	
	public void initialMulticast(){
		Log.d(TAG, "initial multicast");
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
	
	public void	multicastListen() {
			Log.d(TAG, "multicast listening");
		
			try {
				byte[] requestData = new byte[1024];
		           
				while(true)
			       {
					 	DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length);
			            multiSocket.receive(requestPacket);

			            String requestString = new String(requestPacket.getData(), 0, requestPacket.getLength());
			            Toast.makeText(context, requestString, Toast.LENGTH_SHORT).show();
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
