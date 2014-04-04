package com.iems5722.chatapp.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.iems5722.chatapp.gui.Activity_Login;
import com.iems5722.chatapp.gui.DialogWifiAvailable;
import com.iems5722.chatapp.network.ServiceNetwork.ServiceHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

//the point of this thread in the network service is to monitor the network connectivity
//1. confirm that wifi is active and connected
//2. ask to connect to wifi if available
//3. check IP address and subnet
//4. broadcast if IP address or subnet changes

public class ThreadNetwork extends Handler {
	public final static String TAG = "ThreadNetwork";

	//references back to service
	boolean handlerReady = false;
	private Context mContext;	
	private ServiceHandler mServiceHandler;
	
	//WiFi Parameters
	NetworkInfo networkInfo = null;		
	//monitoring network state
	boolean monitoringNetwork = false;
	IntentFilter filter;
	
	//commands recognised by udp service
	public final static int NTWK_START_MONITOR = 0;
	public final static int NTWK_STOP_MONITOR = 1;
	public final static int NTWK_UPDATE = 2;
	
	public ThreadNetwork (Looper looper, Context serviceContext, ServiceHandler serviceHandler) {
		super(looper);
		Log.i(TAG, "Creating Network Monitoring Thread");
		mContext = serviceContext;
		mServiceHandler = serviceHandler;
		filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		handlerReady = true;
	}
	
	@Override
	public void handleMessage(Message msg) {
		//Log.i(TAG, "handling message");
    	switch (msg.what) {
        case NTWK_START_MONITOR:
        	Log.i(TAG, "NTWK_START_MONITOR");
        	if (!monitoringNetwork) {
        		mContext.registerReceiver(serviceBCR, filter);
        		monitoringNetwork = true;
        	}
        	break;
        case NTWK_STOP_MONITOR:
        	Log.i(TAG, "NTWK_STOP_MONITOR");
    		if (monitoringNetwork) {
    			mContext.unregisterReceiver(serviceBCR);
    			monitoringNetwork = false;
    		}
        	break;
    	case NTWK_UPDATE:
    		Log.i(TAG, "NTWK_UPDATE");
    		checkWifiActive();
    		getWiFiDetails();
    		break;
    	default:
    		Log.e(TAG, "Unknown command: " + msg.what);
    	}	
	}	
	
	//notify when connectivity changes
	private BroadcastReceiver serviceBCR = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.d(TAG, "Received " + intent.getAction());
			if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
				if (checkWifiActive()) {
					getWiFiDetails();
				}
			}
		}
	};		
	
	//alerts user that they are not online
	//asks user to connect to wifi if possible 
    private boolean checkWifiActive() {
    	Boolean wifiActive = false;
    	Log.i(TAG, "Checking WiFi connectivity");
    	 ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(mContext.getApplicationContext().CONNECTIVITY_SERVICE);
    	 if (connectivityManager == null) {
             Log.e(TAG,"Cannot Get Connection Info");
    	 }
    	 else {
    		 networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    		 if (networkInfo.isConnected()) {
    			 Log.i(TAG, "Wifi connected");
    			 wifiActive = true;
    			 ServiceNetwork.WiFiConnected = true;
    		 }
    		 else {
    			Log.d(TAG, "WiFi not connected");
    			ServiceNetwork.currentIPAddress = -1;
    			ServiceNetwork.currentNetMask = -1;
    			ServiceNetwork.WiFiConnected = false;
    		 }
    	 }
    	 if (!wifiActive) {
    		 //inform service that wifi not functioning
    		 mServiceHandler.obtainMessage(ServiceNetwork.WIFI_INACTIVE).sendToTarget();
    	 }
    	 return wifiActive; 
    }	
    
	//update wifi information in service
    //todo make thread safe
    private void getWiFiDetails() {
    	if (networkInfo.isConnected()) {
	        WifiManager mWifi = (WifiManager) mContext.getSystemService(mContext.getApplicationContext().WIFI_SERVICE);
	        WifiInfo info = mWifi.getConnectionInfo();
	        if(info == null) {
	            Log.e(TAG,"Cannot Get WiFi Info");
	            return;
	        }
	        else {
	        	//Log.d(TAG,"\n\nWiFi Status: " + info.toString());
	        }
			  
	        DhcpInfo dhcp = mWifi.getDhcpInfo(); 
	        if (dhcp == null) { 
	            Log.d(TAG, "Could not get dhcp info"); 
	            return; 
	        } 
	        try {
				//if network details have changed, send broadcast and update records
	        	//update network details
				if (ServiceNetwork.intIPAddress != ServiceNetwork.currentIPAddress || 
					ServiceNetwork.intNetMask != ServiceNetwork.currentNetMask) {
					Log.i(TAG, "New network detected");
					//TODO need to ping all 
		        	ServiceNetwork.intIPAddress = dhcp.ipAddress;
		        	ServiceNetwork.inetIPAddress = InetAddress.getByAddress(ServiceNetwork.getIPAddress(ServiceNetwork.intIPAddress));     
			        //Log.d(TAG, Integer.toString(dhcp.netmask));
		        	ServiceNetwork. intNetMask = dhcp.netmask;
		        	ServiceNetwork.inetNetMask = InetAddress.getByAddress(ServiceNetwork.getIPAddress(ServiceNetwork.intNetMask));
		        	ServiceNetwork.MACAddress = info.getMacAddress();
		    		//update broadcast address
		    		try {
		    			ServiceNetwork.intBroadcastAddress = (ServiceNetwork.intIPAddress & ServiceNetwork.intNetMask) | ~ ServiceNetwork.intNetMask;
		    			ServiceNetwork.BroadcastAddress = InetAddress.getByAddress(ServiceNetwork.getIPAddress(ServiceNetwork.intBroadcastAddress));
		    			ServiceNetwork.intFirstAddress = (ServiceNetwork.intIPAddress & ServiceNetwork.intNetMask);
		    			ServiceNetwork.inetFirstAddress = InetAddress.getByAddress(ServiceNetwork.getIPAddress(ServiceNetwork.intFirstAddress));
		    		} catch (UnknownHostException e) {
		    			Log.e(TAG, "WiFi details error");
		    		}		        	
				}	        	
			} catch (UnknownHostException e) {
				Log.e(TAG,"Cannot get WiFI details");
				e.printStackTrace();
			}
	
	        String outMsg = "My IP: " + ServiceNetwork.inetIPAddress + " " + ServiceNetwork.inetNetMask + " MAC " + ServiceNetwork.MACAddress;
	        Log.d(TAG, outMsg);
    	}
    }	    
}
