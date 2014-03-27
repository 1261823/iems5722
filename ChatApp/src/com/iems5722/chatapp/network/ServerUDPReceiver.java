package com.iems5722.chatapp.network;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.iems5722.chatapp.gui.Chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.net.DhcpInfo;
import android.util.Log;

public class ServerUDPReceiver extends Thread {

	final static private String TAG = "ServerUDPReceiver";
	final static boolean D = true;

	//Objects to communicate back to other threads
	Handler 		chatHandler;
	Context 		chatContext;
	Handler			udpSenderHandler;
	
	//WiFi Parameters
	static int 			intIPAddress;
	static InetAddress 	inetIPAddress;
	static int			intNetMask;
	static InetAddress	inetNetMask;
	static String 		MACAddress;
	static int 			intFirstAddress;
	static InetAddress 	inetFirstAddress;
	
	//P2P messaging components
	static int 				PORT = 6666;	
	static DatagramSocket 	serverSocket;  			 
	static boolean 			socketOK=true;	
	static InetAddress 		BroadcastAddress;
	static int				intBroadcastAddress;
	static Hashtable<String, UserRecord> UserHashtable;	

	class UserRecord {
		String peerName;
		InetAddress peerIPAddress;
		Boolean peerActive;
		
		public UserRecord(String inName, InetAddress inAddress) {
			peerName = inName;
			peerIPAddress = inAddress;
			peerActive = true;
		}
	}
	
	public void inactiveAllUsers() {
		//disables all users
		Log.i(TAG, "Inactivating all users");
		Iterator<Entry<String, UserRecord>> it;
		Map.Entry<String, UserRecord> entry;
		it = UserHashtable.entrySet().iterator();
		while(it.hasNext()) {
			entry = it.next();
			UserRecord peerRecord = entry.getValue();
			peerRecord.peerActive = false;
			Log.i(TAG, "IA " + peerRecord);	
			UserHashtable.put(entry.getKey().toString(), peerRecord);
		}
	}
	
	public boolean isUserKnown(String inMACAddress) {
		UserRecord peerRecord = UserHashtable.get(inMACAddress);
		if (peerRecord != null) {
			return true;
		}
		else {
			return false;
		}
	}
	public static String getAuthorName(String inMACAddress) {
		UserRecord peerRecord = UserHashtable.get(inMACAddress);
		if (peerRecord != null) {
			return peerRecord.peerName;
		}
		else {
			return "Unknown user";
		}		
	}
	
	public static String getMsgType(String inMessage) {
		Log.i(TAG, "gmT " + inMessage);		
		String[] msgParts = inMessage.split(ServerUDPSender.msgSep);
		String msgType = msgParts[0].toString();
		Log.i(TAG, "gmT response " + msgType);				
		return msgType;
	}
	
	public static String getMsgMAC(String inMessage) {
		Log.i(TAG, "gmM " + inMessage);		
		String[] msgParts = inMessage.split(ServerUDPSender.msgSep);
		String peerMAC = msgParts[1].toString();
		Log.i(TAG, "gmM response " + peerMAC);				
		return peerMAC;
	}
	
	public static String getMsgContent(String inMessage) {
		Log.i(TAG, "gmC " + inMessage);
		String[] msgParts = inMessage.split(ServerUDPSender.msgSep);
		String msgContent = msgParts[2].toString();
		Log.i(TAG, "gmC response " + msgContent);						
		return msgContent;
	}
	
	
	//Adds new user the hash table if user doesn't already exist
	public void AddTestNewUser(String peerMSg, InetAddress peerIPAddr) {
		String[] msgParts = peerMSg.split(ServerUDPSender.msgSep);
		String peerMAC  = msgParts[1].toString();
		String peerName = msgParts[2].toString();	
		
		UserRecord peerRecord = UserHashtable.get(peerMAC);
		if (peerRecord != null) {
			//update details if necessary 
			Log.d(TAG, "Found existing user");			
			peerRecord.peerName = peerName;
			peerRecord.peerIPAddress = peerIPAddr;
			peerRecord.peerActive = true;
		}
		else {
			if (UserHashtable.containsKey(peerMAC)) {
				//key exists but values are null
				Log.d(TAG, "Adding missing user");
				UserRecord userRec = new UserRecord(peerName, peerIPAddr);
				UserHashtable.put(peerMAC, userRec);				
			}
			else {
				//new entry
				Log.d(TAG, "Creating new user");
				UserRecord userRec = new UserRecord(peerName, peerIPAddr);
				UserHashtable.put(peerMAC, userRec);
			}
		}
		Log.d(TAG, "UR " + peerIPAddr + " " + peerMAC + " " + peerName);	
	}
	
	public ServerUDPReceiver(Context currentContext, Handler handler) {
		chatContext = currentContext;
		chatHandler = handler;
		try	{
			serverSocket = new DatagramSocket(PORT);
			Log.i(TAG,"trying to create the datagram...");
			serverSocket.setBroadcast(true);			
		} 
		catch(Exception e) {
			Log.e(TAG,"Cannot open socket"+e.getMessage());
			socketOK = false;
			return;
		}
		try	{
			getWiFiDetails();
		}
		catch(Exception e) {
			Log.e(TAG,"Cannot get my own Broadcast IP address");
		}
	}
	
	public void closeSocket()
	{
		serverSocket.close();
	}
	
	public boolean socketIsOK()
	{
	  return socketOK;
	}
	
	@Override
	public void run() {
		UserHashtable = new Hashtable<String, UserRecord>();
		//UDP socket
		byte[] receiveData = new byte[1024]; 
		while(socketOK) {
			Log.i(TAG,  "in listening loop waiting for packet");
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
			try {
				serverSocket.receive(receivePacket);
				udpSenderHandler = ServerUDPSender.getHandler();
		        InetAddress sourceIPAddress = receivePacket.getAddress();
		        Log.i(TAG,"Received a packet | Source IP Address: " + sourceIPAddress);
		        //only bother if it isn't from itself
		        if(!sourceIPAddress.equals(inetIPAddress)) {
		        	String message = new String(receivePacket.getData(),0,receivePacket.getLength());
		        	String msgType = getMsgType(message);
		        	Log.d(TAG, message + " type " + msgType);		        	
		        	//if it is a request, send ack reply
		        	if (msgType.equals(ServerUDPSender.PING_REQ_MSG)) {
		    			Log.i(TAG,  "Ping received");		        
		        		chatHandler.obtainMessage(Chat.TOAST, "New user joined: " + getMsgContent(message)).sendToTarget();		            			        		
		                udpSenderHandler.obtainMessage(ServerUDPSender.PING_ACKNOWLEDGE, sourceIPAddress).sendToTarget();
		        		//Add new user to UserHashtable
		                AddTestNewUser(message, sourceIPAddress);
		        	}
		        	//if it is a ack reply, add other user to hosts list
		        	else if (msgType.equals(ServerUDPSender.PING_ACK_MSG)) {
		    			Log.i(TAG,  "Ack received");			        		
		        		chatHandler.obtainMessage(Chat.TOAST, getMsgContent(message) + " added you").sendToTarget();		            			        				        		
		        		//Add responsive user to User
		        		AddTestNewUser(message, sourceIPAddress);
		        	}
		        	//else forward on to chat handler
		        	else if (msgType.equals(ServerUDPSender.MSG_ALL_MSG)) {
			        	Log.i(TAG,"Received sentence: " + message + " from " + sourceIPAddress);
			        	
			        	chatHandler.obtainMessage(Chat.PACKET_CAME, message).sendToTarget();    
			        	//if message comes from unknown host, send ping request to unknown host
			        	if(! isUserKnown(getMsgMAC(message))) {
				        	udpSenderHandler.obtainMessage(ServerUDPSender.PING_REQUEST_ONE, sourceIPAddress).sendToTarget();
			        	}
		        	}
		        	else {
		        		Log.e(TAG, "Uknown message type: " + msgType);
		        	}
		        }
			} 
			catch (Exception e)
			{
				Log.e(TAG,"Problems receiving packet: "+e.getMessage());
	    		Log.e(TAG, "Error message " + e);
				socketOK = false;
			} 
		} 
	}
	

	
    public static byte[] getIPAddress(int ip_int) {
        byte[] quads = new byte[4]; 
        for (int k = 0; k < 4; k++) 
           quads[k] = (byte) ((ip_int>> k * 8) & 0xFF);
		return quads;
    }	
    /*
	public static int ipToLong(InetAddress broadcastAddress) {
		int num = broadcastAddress.getAddress();
		int num = 0;
		for (int i =0; i <addrArray.length; i++) {
			int power = 3 - i;
			num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
		}
		return num;
	}    
    */
	
	public void updateBroadcastAddress() {
		try {
			intBroadcastAddress = (intIPAddress & intNetMask) | ~ intNetMask;
			BroadcastAddress = InetAddress.getByAddress(getIPAddress(intBroadcastAddress));
			intFirstAddress = (intIPAddress & intNetMask);
		} catch (UnknownHostException e) {
			Log.e(TAG, "WiFi details error");
		}
	}
    
	
    private void getWiFiDetails() throws UnknownHostException
    {
        WifiManager mWifi = (WifiManager) (chatContext.getSystemService(Context.WIFI_SERVICE));
        WifiInfo info = mWifi.getConnectionInfo();
        if(info==null) {
            if(D) Log.e(TAG,"Cannot Get WiFi Info");
            return;
        }
        else {
        	if(D) Log.d(TAG,"\n\nWiFi Status: " + info.toString());
        }
		  
        DhcpInfo dhcp = mWifi.getDhcpInfo(); 
        if (dhcp == null) { 
          Log.d(TAG, "Could not get dhcp info"); 
          return; 
        } 

        intIPAddress = dhcp.ipAddress;
        inetIPAddress = InetAddress.getByAddress(getIPAddress(intIPAddress));     
        //Log.d(TAG, Integer.toString(dhcp.netmask));
        intNetMask = dhcp.netmask;
        inetNetMask = InetAddress.getByAddress(getIPAddress(intNetMask));
        MACAddress = info.getMacAddress();

        String outMsg = "My IP: " + inetIPAddress + " " + inetNetMask + " MAC " + MACAddress;
        chatHandler.obtainMessage(Chat.INFO, outMsg).sendToTarget();
        Log.d(TAG, outMsg);

    	//update broadcast address
    	updateBroadcastAddress();
    	Log.d(TAG, "BCA " + BroadcastAddress);        
    }
}