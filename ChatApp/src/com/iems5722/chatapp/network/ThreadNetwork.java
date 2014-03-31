package com.iems5722.chatapp.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.iems5722.chatapp.gui.Activity_Login;
import com.iems5722.chatapp.gui.DialogWifiAvailable;

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
	
	//WiFi Parameters
	NetworkInfo networkInfo = null;		
	
	//commands recognised by udp service
	public final static int NTWK_INIT = 0;	
	public final static int NTWK_UPDATE = 1;
	public final static int NTWK_SHUTDOWN = 2;
	
	public ThreadNetwork (Looper looper, Context serviceContext) {
		super(looper);
		Log.i(TAG, "Creating Network Monitoring Thread");
		mContext = serviceContext;
		handlerReady = true;
	}
	
	@Override
	public void handleMessage(Message msg) {
		Log.i(TAG, "handling message");
    	switch (msg.what) {
        case NTWK_INIT:
        	Log.i(TAG, "Network Init");
			//register to listen for network change event
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			mContext.registerReceiver(serviceBCR, filter);
			break;
    	case NTWK_UPDATE:
    		Log.i(TAG, "Network Update");
    		checkWifiActive();
    		getWiFiDetails();
    		break;
    	case NTWK_SHUTDOWN:
    		mContext.unregisterReceiver(serviceBCR);
    		break;  		
    	default:
    		Log.e(TAG, "Unknown command: " + msg.what);
    	}	
	}	
	
	//notify when connectivity changes
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
	
	//alerts user that they are not online
	//asks user to connect to wifi if possible 
    private boolean checkWifiActive() {
    	Log.d(TAG, "Checking WiFi connectivity");
    	 ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(mContext.getApplicationContext().CONNECTIVITY_SERVICE);
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
    			ServiceNetwork.currentIPAddress = -1;
    			ServiceNetwork.currentNetMask = -1;
    			
				Intent wifiIntent = new Intent (mContext.getApplicationContext(), DialogWifiAvailable.class);
				wifiIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				wifiIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
				mContext.startActivity(wifiIntent);  
    			return false;
    		 }
    	 }
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
	        	Log.d(TAG,"\n\nWiFi Status: " + info.toString());
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
