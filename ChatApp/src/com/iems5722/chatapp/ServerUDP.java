package com.iems5722.chatapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.net.DhcpInfo;
import android.util.Log;

public class ServerUDP extends Thread {

	final static private String TAG = "ServerUDP";
	final static boolean D = true;
	final static int PORT = 6666;

	Handler 		mHandler;      			
	Context 		mContext;	   			
	DatagramSocket 	serverSocket;  			 
	boolean 		socketOK=true; 			
	
	InetAddress 	myIPAddress; 			
	InetAddress 	myPeerAddress;			
	
	public ServerUDP(Context currentContext, Handler handler)
	{
		mContext = currentContext;
		mHandler = handler;
		try
		{
			serverSocket = new DatagramSocket(PORT);
			Log.i(TAG,"trying to create the datagram...");
			serverSocket.setBroadcast(true);
		} 
		catch(Exception e)
		{
			Log.e(TAG,"Cannot open socket"+e.getMessage());
			socketOK = false;
			return;
		}
		
		try
		{
			getMyWiFiIPAddress();
			Log.i(TAG,"My IP address:"+myIPAddress);
		}
		catch(Exception e)
		{
			Log.e(TAG,"Cannot get my own Broadcast IP address");
		}
	}
	
	public void closeSocket()
	{
		serverSocket.close();
	}
	
	boolean socketIsOK()
	{
	  return socketOK;
	}
	
	@Override
	public void run()
	{
		byte[] receiveData = new byte[1024]; 
		while(socketOK) 
		{ 
			DatagramPacket receivePacket = 
			    new DatagramPacket(receiveData, receiveData.length); 
			try
			{
				serverSocket.receive(receivePacket);
		        Log.i(TAG,"Received a packet");
		        InetAddress sourceIPAddress = receivePacket.getAddress();
		        Log.d(TAG,"Received a packet | Source IP Address: "+sourceIPAddress);
		        if(!sourceIPAddress.equals(myIPAddress))
		        {
		        	String sentence = new String(receivePacket.getData(),0,receivePacket.getLength()); 
		        	mHandler.obtainMessage(Chat.PACKET_CAME,sentence).sendToTarget();    
		        	Log.i(TAG,"Received sentence: "+sentence);
		        }
		        
			} 
			catch (Exception e)
			{
				Log.e(TAG,"Problems receiving packet: "+e.getMessage());
				socketOK = false;
			} 
		} 
	}

	public void sendMessage(String msg, String serverAddrStr) throws IOException 
	{
		byte[] sendData  = new byte[1024]; 
		sendData = msg.getBytes(); 

		myPeerAddress=InetAddress.getByName(serverAddrStr);
		Log.i(TAG,"myPeerAddress "+ myPeerAddress);
		DatagramPacket sendPacket =
			new DatagramPacket(sendData, sendData.length, myPeerAddress, PORT);
		
		serverSocket.send(sendPacket);
        Log.i(TAG,"Sent packet: "+msg);
	}
	
    private void getMyWiFiIPAddress() throws UnknownHostException
    {
        WifiManager mWifi = (WifiManager) (mContext.getSystemService(Context.WIFI_SERVICE));
        WifiInfo info = mWifi.getConnectionInfo();
        if(info==null)
        {
            if(D) Log.e(TAG,"Cannot Get WiFi Info");
            return;
        }
        else
        {
        	if(D) Log.d(TAG,"\n\nWiFi Status: " + info.toString());
        }
		  
        DhcpInfo dhcp = mWifi.getDhcpInfo(); 
        if (dhcp == null) 
        { 
          Log.d(TAG, "Could not get dhcp info"); 
          return; 
        } 

        int myIntegerIPAddress = dhcp.ipAddress;

        byte[] quads = new byte[4]; 
        for (int k = 0; k < 4; k++) 
           quads[k] = (byte) ((myIntegerIPAddress>> k * 8) & 0xFF);

        myIPAddress = InetAddress.getByAddress(quads);
        mHandler.obtainMessage(Chat.INFO,"MyIP:"+myIPAddress).sendToTarget();    
    }
}