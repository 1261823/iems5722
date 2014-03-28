package com.iems5722.chatapp.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.iems5722.chatapp.gui.Activity_Login;
import com.iems5722.chatapp.gui.DialogWifiAvailable;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;



//the point of this service is to monitor the network connectivity
// 1. confirm that wifi is active and connected
// 2. ask to connect to wifi if available
// 3. check IP address and subnet
// 4. broadcast if IP address or subnet changes

public class ServiceNetwork extends Service {
	private final static String TAG = "ServiceNetwork";
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private final IBinder mBinder = new NetworkBinder();	
	boolean handlerReady = false;
	
	private Handler UIhandler;
	
	//commands recognised by udp service
	public final static int NTWK_UPDATE = 1;
	public final static int NTWK_GET_NETMASK = 2;
	public final static int NTWK_INIT = 3;
	
	//broadcast issued when network parameters change
	private Intent updateIntent;
	public static final String BC_NETWORK_UPDATE = "BroadcastNetworkUpdate";
	public static final String NT_IP_INT = "int_ipAddress";
	public static final String NT_IP_INET = "inet_ipAddress";
	public static final String NT_NM_INT = "int_Netmask";
	public static final String NT_NM_INET = "inet_Netmask";
	public static final String NT_MAC_ADDR = "macAddress";

	//WiFi Parameters
	NetworkInfo networkInfo = null;	
	static int 			intIPAddress;
	static InetAddress 	inetIPAddress;
	static int			intNetMask;
	static InetAddress	inetNetMask;
	static String 		MACAddress;
	//Existing WiFi parameters
	static int			currentIPAddress = -1;
	static int			currentNetMask = -1;

	public Handler getHandler() {
		Log.i(TAG, "returning handler");
		return mServiceHandler;
	}		
	
	//handler that receives message from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler (Looper looper) {
			super(looper);
			Log.i(TAG, "Creating ServiceHandler");
			handlerReady = true;
		}
		
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "handling message");
        	switch (msg.what) {
            case NTWK_INIT:
				//register to listen for network change event
				IntentFilter filter = new IntentFilter();
				filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
				registerReceiver(serviceBCR, filter);
				checkWifiActive();
				break;
        	case NTWK_UPDATE:
        		checkWifiActive();
        		getWiFiDetails();
        		break;
        	default:
        		Log.e(TAG, "Unknown command: " + msg.what);
        	}	
		}
	}
	
    public static byte[] getIPAddress(int ip_int) {
        byte[] quads = new byte[4]; 
        for (int k = 0; k < 4; k++) 
           quads[k] = (byte) ((ip_int>> k * 8) & 0xFF);
		return quads;
    }
    
    private boolean checkWifiActive() {
    	Log.d(TAG, "Checking WiFi connectivity");
    	 ConnectivityManager connectivityManager = (ConnectivityManager) getApplication().getSystemService(CONNECTIVITY_SERVICE);
    	 if (connectivityManager == null) {
             Log.e(TAG,"Cannot Get Connection Info");
             return false;
    	 }
    	 else {
    		 networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    		 if (networkInfo.isConnected()) {
    			 Log.i(TAG, "Wifi connected");
    			 return true;
    		 }
    		 else {
    			Log.d(TAG, "WiFi not connected");
    			currentIPAddress = -1;
    			currentNetMask = -1;
    			
				Intent wifiIntent = new Intent (getBaseContext(), DialogWifiAvailable.class);
				wifiIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				wifiIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
				getApplicationContext().startActivity(wifiIntent);  
    			return false;
    		 }
    	 }
    }
	
    private void getWiFiDetails() {
    	if (networkInfo.isConnected()) {
	        WifiManager mWifi = (WifiManager) getApplication().getSystemService(WIFI_SERVICE);
	        WifiInfo info = mWifi.getConnectionInfo();
	        if(info == null) {
	            Log.e(TAG,"Cannot Get WiFi Info");
	            return;
	        }
	        else {
	        	Log.d(TAG,"\n\nWiFi Status: " + info.toString());
	        }
			  
	        DhcpInfo dhcp = mWifi.getDhcpInfo(); 
	        if (dhcp == null) { 
	            Log.d(TAG, "Could not get dhcp info"); 
	            return; 
	        } 
	        try {
		        intIPAddress = dhcp.ipAddress;
		        inetIPAddress = InetAddress.getByAddress(getIPAddress(intIPAddress));     
		        //Log.d(TAG, Integer.toString(dhcp.netmask));
		        intNetMask = dhcp.netmask;
				inetNetMask = InetAddress.getByAddress(getIPAddress(intNetMask));
				MACAddress = info.getMacAddress();
				//if network details have changed, send broadcast and update records
				if (intIPAddress != currentIPAddress || intNetMask != currentNetMask) {
					Log.i(TAG, "Broadcasting updated network details");
					currentIPAddress = intIPAddress;
					currentNetMask = intNetMask;
					//broadcast updated details
					updateIntent = new Intent(BC_NETWORK_UPDATE);
					updateIntent.putExtra(NT_IP_INT, intIPAddress);
					updateIntent.putExtra(NT_IP_INET, inetIPAddress);
					updateIntent.putExtra(NT_NM_INT, intNetMask);
					updateIntent.putExtra(NT_NM_INET, inetNetMask);
					updateIntent.putExtra(NT_MAC_ADDR, MACAddress);	
					LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
				}
				
				
			} catch (UnknownHostException e) {
				Log.e(TAG,"Cannot get WiFI details");
				e.printStackTrace();
			}
	        
	
	        String outMsg = "My IP: " + inetIPAddress + " " + inetNetMask + " MAC " + MACAddress;
	        Log.d(TAG, outMsg);
    	}
    }	
	
	private BroadcastReceiver serviceBCR = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received " + intent.getAction());
			if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
				if (checkWifiActive()) {
					getWiFiDetails();
				}
			}
		}
	};		
    
    
	@Override
	public void onCreate() {
		Log.d(TAG, "Creating service");
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		//init handlers and threads
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		Log.d(TAG, "onStartCommand");
		UIhandler = new Handler(Looper.getMainLooper());
		mServiceHandler.obtainMessage(NTWK_INIT).sendToTarget();
		return Service.START_STICKY;
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class NetworkBinder extends Binder {
		public ServiceNetwork getService() {
			Log.d(TAG, "passing back service");
			return ServiceNetwork.this;
		}
	}	

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		unregisterReceiver(serviceBCR);
		stopSelf();
	}	
	
	public boolean handlerReady() {
		Log.d(TAG, "passing back status");
		return handlerReady;
	}
}
