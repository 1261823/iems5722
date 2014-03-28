package com.iems5722.chatapp.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


//The point of this service is to listen for UDP messages
//UDP messages are used for user discovery
//Service saves users directly into database. No UI interaction occurs

public class ServiceUDP_Recv extends Service {
	private final static String TAG = "ServiceUDP_Recv";
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	boolean handlerReady = false;
	
	//commands recognised by udp service
	public final static int UDP_INIT = 1;
	public final static int UDP_LISTEN = 2;
	
	//Listener network parameters
	static int 				PORT = 6666;	
	static DatagramSocket 	serverSocket;  			 
	static boolean 			socketOK = true;	
	static int				intIPAddress;
	static InetAddress  	inetIPAddress = null;
	
	//intent to send ping to all
	private Intent sendPingIntent;
	public static final String BC_PING_ALL = "BroadcastPingAll";
	public static final String BC_PING_ITERATE = "BroadcastPingIterate";
	public static final String BC_PING_ONE = "BroadcastPingOne";
	
	//intent for sender service to send ack
	private Intent sendAckIntent;
	public static final String BC_PING_ACK = "BroadcastPingAck";
	public static final String ACK_IPADDR = "replyToAddress";	

	//handler that recevies message from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler (Looper looper) {
			super(looper);
			Log.i(TAG, "Creating ServiceHandler");
			handlerReady = true;
		}
		
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "UDP Service Handler");
			switch(msg.what) {
				case UDP_INIT:
					setupUDP();
					break;
				case UDP_LISTEN:
					listenUDP();
					break;
				default:
					Log.e(TAG, "Unknown command: " + msg.what);
			}
		}
	}
	
	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("ServiceUDP_Recv");
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int StartId) {
		Log.d(TAG, "onStartCommand");
		mServiceHandler.obtainMessage(UDP_INIT).sendToTarget();		
		return Service.START_STICKY;
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceBCR);
		stopSelf();
	}	
	
	public void setupUDP() {
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
		LocalBroadcastManager.getInstance(this).registerReceiver(serviceBCR, new IntentFilter(ServiceNetwork.BC_NETWORK_UPDATE));
	}
	
	private BroadcastReceiver serviceBCR = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received " + intent.getAction());
			if (intent.getAction().equals(ServiceNetwork.BC_NETWORK_UPDATE)) {
				//update network details
				intIPAddress  = intent.getIntExtra(ServiceNetwork.NT_IP_INT, -1);
				inetIPAddress = (InetAddress) intent.getSerializableExtra(ServiceNetwork.NT_IP_INET);
				if (intIPAddress != -1) {
					requestPingAll();
					Log.d(TAG, "WiFi connected, going to listen");
					socketOK = true;
					mServiceHandler.obtainMessage(UDP_LISTEN).sendToTarget();	
				}
				else {
					socketOK = false;
				}
			}
		}
	};	
	
	public void requestPingAll() {
		Log.d(TAG, "Checking for new hosts");
		sendPingIntent = new Intent(BC_PING_ALL);
		LocalBroadcastManager.getInstance(this).sendBroadcast(sendPingIntent);
	}
	
	public static String getMsgType(String inMessage) {
		Log.i(TAG, "gmT " + inMessage);		
		String[] msgParts = inMessage.split(ServerUDPSender.msgSep);
		String msgType = msgParts[0].toString();
		Log.i(TAG, "gmT response " + msgType);				
		return msgType;
	}	
	
	public void listenUDP() {
		//UDP socket
		byte[] receiveData = new byte[1024]; 
		while(socketOK) {
			Log.i(TAG,  "in listening loop waiting for packet");
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
			try {
				serverSocket.receive(receivePacket);
				//udpSenderHandler = ServerUDPSender.getHandler();
		        InetAddress sourceIPAddress = receivePacket.getAddress();
		        Log.i(TAG,"Received a packet | Source IP Address: " + sourceIPAddress);
		        //only bother if it isn't from itself
		        if(sourceIPAddress.equals(inetIPAddress)) {
		        	//network discovery success
		        	Log.d(TAG,"Ping method success");
		        }
		        else {
		        	String message = new String(receivePacket.getData(),0,receivePacket.getLength());
		        	String msgType = getMsgType(message);
		        	Log.d(TAG, message + " type " + msgType);		        	
		        	//if it is a request, send ack reply
		        	if (msgType.equals(ServiceUDP_Send.PING_REQ_MSG)) {
		    			Log.i(TAG,  "Ping received");
		    			//send ping ack back to user
		    			sendAckIntent = new Intent(BC_PING_ACK);
		    			sendAckIntent.putExtra(ACK_IPADDR, intIPAddress);		    			
						LocalBroadcastManager.getInstance(this).sendBroadcast(sendAckIntent); 			
		    			//add user to database
		        	}
		    		else if (msgType.equals(ServiceUDP_Send.PING_ACK_MSG)) {
		    			Log.i(TAG,  "Ack received");			        		
		        		//set user to active
		        		//AddTestNewUser(message, sourceIPAddress);
		    		}
		        	else {
		        		Log.e(TAG, "Uknown message type: " + msgType);
		        	}		    			
		        }
			}
			catch (Exception e) {
				Log.e(TAG,"Problems receiving packet: "+e.getMessage());
				Log.e(TAG, "Error message " + e);
				socketOK = false;
			} 	
		}
	}
}
