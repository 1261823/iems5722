package com.iems5722.chatapp.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.network.PeerFileReceiver;
import com.iems5722.chatapp.network.PeerFileSender;
//import com.iems5722.chatapp.network.ServerTCP;
//import com.iems5722.chatapp.network.ServerUDPReceiver;
//import com.iems5722.chatapp.network.ServerUDPSender;

public class Chat extends Activity {

	final static private String TAG = "Chat";
	final static private boolean D	= true;
	
	//Message types
	public final static int PACKET_CAME = 1;
	public final static int TOAST  = 2;
	public final static int INFO = 3;
	public final static int TCP_PACKET = 10;
	
	public final static int UDP_SENDER_UPDATE = 50;
	
	//GUI elements
	EditText		peerAddr;
	TextView		myAddr;
	EditText		msg;
	Button 			send;
	Button			ping;
	Button			pingIterator;
	ListView		msgList;
	ArrayAdapter	<String>receivedMessages;

	//Config options
	public static String username = "default";
	
	//UDP Server threads
	//ServerUDPReceiver udpReceiver;
	//ServerUDPSender   udpSender;
	//Handler udpHandler;
	
	//ServerTCP tcpThread;
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if(D) Log.d(TAG, "In the Handler");
        	switch (msg.what) {
            case PACKET_CAME:
            	Log.i(TAG, "PACKET_CAME");
                String incomingMessage = (String) msg.obj;
            	Log.i(TAG, incomingMessage);                
                //String msgActual = ServerUDPReceiver.getMsgContent(incomingMessage);
                //String msgAuthor = ServerUDPReceiver.getAuthorName(ServerUDPReceiver.getMsgMAC(incomingMessage));
                //receivedMessages.add(msgAuthor + ": " + msgActual);
                break;
            case TOAST:
            	String toastToMake= (String) msg.obj;
            	Toast.makeText(getApplicationContext(), toastToMake, Toast.LENGTH_SHORT).show();
                break;  
            case INFO:
            	Log.i(TAG, "INFO");
            	String infoMessage= (String) msg.obj;
            	myAddr.setText(infoMessage);
            	break;
            case TCP_PACKET:
            	String tcpMessage = (String) msg.obj;
            	receivedMessages.add("TCP: "+tcpMessage);
            	break;
            case UDP_SENDER_UPDATE:
            	//udpHandler = udpSender.getHandler();
            	Log.d(TAG, "UDP Handler updated");
            	break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras != null) {
        	username = extras.getString(Activity_Login.URI_USERNAME);
        }
        setUpGUI();
        //initUDP();
        initTCP();
    }
        
    private void setUpGUI() {    
        peerAddr = (EditText)findViewById(R.id.editPeerAddr);
        send = (Button)findViewById(R.id.send);
        send.setOnClickListener(send_listener);
        msgList = (ListView)findViewById(R.id.msgList);
        myAddr = (TextView)findViewById(R.id.textMyAddr);
        receivedMessages = new ArrayAdapter<String>(this, R.layout.message);
        msgList.setAdapter(receivedMessages);
        msg = (EditText)findViewById(R.id.msg);
        msg.setOnKeyListener(key_listener);
        
        ping = (Button)findViewById(R.id.ping);
        ping.setOnClickListener(ping_listener);   
        
        pingIterator = (Button)findViewById(R.id.pingiterate);
        pingIterator.setOnClickListener(pingiterator_listener);
    }
    /*    
    private void initUDP() {
    	udpReceiver = new ServerUDPReceiver(getApplicationContext(), mHandler);
        if (!udpReceiver.socketIsOK()) {
     	   Log.e(TAG,"Server NOT STARTED");
     	   Toast.makeText(getApplicationContext(), "Cannot Start UDP Server: ", Toast.LENGTH_LONG).show();
     	   return;
         }

        udpReceiver.start();
        Log.i(TAG,"UDP Receiver Started");
        
        udpSender = new ServerUDPSender(getApplicationContext(), mHandler);
        udpSender.start();
        Log.i(TAG,"UDP Sender Started");
    }
    */
    private void initTCP() {
    	/*
        tcpThread = new ServerTCP(getApplicationContext(), mHandler);
        if (!tcpThread.socketIsOK()) {
      	   Log.e(TAG,"Server NOT STARTED");
      	   Toast.makeText(getApplicationContext(), "Cannot Start TCP Server: ", Toast.LENGTH_LONG).show();
      	   return;        	
        }
        tcpThread.start();
        Log.i(TAG,"TCP Server Started");
        */
        
        PeerFileReceiver peerFileReceiverThread = new PeerFileReceiver(getApplicationContext(), mHandler);
        if (!peerFileReceiverThread.socketIsOK()) {
      	   Log.e(TAG,"TCP file receiver port NOT STARTED");
      	   Toast.makeText(getApplicationContext(), "Cannot Start TCP file receiver port: ", Toast.LENGTH_LONG).show();
      	   return;        	
        }
        peerFileReceiverThread.start();
        Log.i(TAG,"TCP file receiver Started");
    }
    
    private OnKeyListener key_listener = new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		    	String messageOut = msg.getText().toString();
		     	//SendPackage sendNewMessage = new SendPackage();
		    	//sendNewMessage.execute(theNewMessage);
		    	//udpHandler.obtainMessage(ServerUDPSender.MESSAGE_ALL, messageOut).sendToTarget();
		    	msg.setText("");
				return true;
			}
		return false;
		}
	};
    
    private OnClickListener send_listener = new OnClickListener() {
        public void onClick(View v) {
        	//postMessage();
//        	String messageOut = msg.getText().toString();
//        	String ipTarget = peerAddr.getText().toString();
//        	if (messageOut.length() > 0) {
//        		if (ipTarget.length() > 0) {
//        			String[] outgoing = {messageOut, ipTarget}; 
//        			udpHandler.obtainMessage(ServerUDPSender.MESSAGE_ONE, outgoing).sendToTarget();
//        		}
//        		else {
//		         	//SendPackage sendNewMessage = new SendPackage();
//		        	//sendNewMessage.execute(theNewMessage);
//		    		Log.i(TAG, "messageSent " + messageOut);
//		        	udpHandler.obtainMessage(ServerUDPSender.MESSAGE_ALL, messageOut).sendToTarget();
//			    	receivedMessages.add(username + ": "+ messageOut);
//        		}
//		    	msg.setText("");
//        	}
        	PeerFileSender peerFileSenderThread = new PeerFileSender(getApplicationContext(), mHandler);
        	 if (!peerFileSenderThread.socketIsOK()) {
            	   Log.e(TAG,"TCP file receiver port NOT STARTED");
            	   Toast.makeText(getApplicationContext(), "Cannot Start TCP file receiver port: ", Toast.LENGTH_LONG).show();
            	   return;        	
              }
        	 peerFileSenderThread.start();
        }
    };
    
    private OnClickListener ping_listener = new OnClickListener() {
        public void onClick(View v) {
        	Log.d(TAG, "ping clicked");
        	//udpHandler.obtainMessage(ServerUDPSender.PING_REQUEST_ALL).sendToTarget();      	
        }
    };   
    
    private OnClickListener pingiterator_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "ping clicked");	
        	//udpHandler.obtainMessage(ServerUDPSender.PING_ITERATE_ALL).sendToTarget();      	
		}
	};
    
    private class SendPackage extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... messageToSend) {
	    	try{
	    		Log.i(TAG, "Sending message in Async task");
	    		Log.i(TAG, "Message[0] is: " + messageToSend[0]);
	    		//udpSender.udpSendMessage(messageToSend[0], peerAddr.getText().toString());
	    	}catch(Exception e){
	    		Log.e(TAG,"Cannot send message "+ e.getMessage());
	    		Log.e(TAG, "Error message " + e);
	    		return null;
	    	}
	    	Log.d(TAG, "Async task done");
			return messageToSend[0];
		}
		
		@Override
		public void onPostExecute(String messageSent) {
	    	receivedMessages.add("Me: "+ messageSent);
	    	msg.setText("");			
		}
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	//udpReceiver.closeSocket();
    	//tcpThread.closeSocket();
    }  
}
