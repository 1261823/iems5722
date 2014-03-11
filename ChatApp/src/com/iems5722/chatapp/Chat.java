package com.iems5722.chatapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
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

public class Chat extends Activity 
{

	final static private String TAG = "Chat";
	final static private boolean D	= true;
	final static int PACKET_CAME = 1;
	final static int TOAST  = 2;
	final static int INFO = 3;
	final static int TCP_PACKET = 10;
	
	EditText		peerAddr;
	TextView		myAddr;
	EditText		msg;
	Button 			send;
	ListView		msgList;
	ArrayAdapter	<String>receivedMessages;

	ServerUDP udpThread;
	ServerTCP tcpThread;
	
	private final Handler mHandler = new Handler() 
	{
        @Override
        public void handleMessage(Message msg) {

        	if(D) Log.d(TAG, "In the Handler");
        	switch (msg.what) 
        	{
	            case PACKET_CAME:
	                String incomingMessage = (String) msg.obj;
	                receivedMessages.add("You: "+incomingMessage);
	                break;
	            case TOAST:
	            	String toastToMake= (String) msg.obj;
	            	Toast.makeText(getApplicationContext(), toastToMake, Toast.LENGTH_SHORT).show();
	                break;  
	            case INFO:
	            	String infoMessage= (String) msg.obj;
	            	myAddr.setText(infoMessage);
	            	break;
	            case TCP_PACKET:
	            	String tcpMessage = (String) msg.obj;
	            	receivedMessages.add("TCP: "+tcpMessage);
	            	break;
            }
        }
    };    
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        peerAddr = (EditText)findViewById(R.id.editPeerAddr);
        send = (Button)findViewById(R.id.send);
        send.setOnClickListener(send_listener);
        msgList = (ListView)findViewById(R.id.msgList);
        myAddr = (TextView)findViewById(R.id.textMyAddr);
        receivedMessages = new ArrayAdapter<String>(this, R.layout.message);
        msgList.setAdapter(receivedMessages);
        msg = (EditText)findViewById(R.id.msg);
        msg.setOnKeyListener(new OnKeyListener() {

        public boolean onKey(View v, int keyCode, KeyEvent event) 
        {
        		if ((event.getAction() == KeyEvent.ACTION_UP) &&
        				(keyCode == KeyEvent.KEYCODE_ENTER)) {
        			postMessage();
        			return true;
        		}
        		return false;
        	}
        });
        
        udpThread = new ServerUDP(getApplicationContext(), mHandler);

        if (!udpThread.socketIsOK())
        {
     	   Log.e(TAG,"Server NOT STARTED");
     	   Toast.makeText(getApplicationContext(), "Cannot Start UDP Server: ", Toast.LENGTH_LONG).show();
     	   return;
         }

        udpThread.start();
        Log.i(TAG,"UDP Server Started");
        
        tcpThread = new ServerTCP(getApplicationContext(), mHandler);
        if (!tcpThread.socketIsOK()) {
      	   Log.e(TAG,"Server NOT STARTED");
      	   Toast.makeText(getApplicationContext(), "Cannot Start TCP Server: ", Toast.LENGTH_LONG).show();
      	   return;        	
        }
        tcpThread.start();
        Log.i(TAG,"TCP Server Started");
        
    }
    
    private OnClickListener send_listener = new OnClickListener() 
    {
        public void onClick(View v) {
        	//postMessage();
        	String theNewMessage = msg.getText().toString();
         	SendPackage sendNewMessage = new SendPackage();
        	sendNewMessage.execute(theNewMessage);
        }
    };
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	udpThread.closeSocket();
    	tcpThread.closeSocket();
    }
    
    private void postMessage()
    {
    	String theNewMessage = msg.getText().toString();

    	try{
    		udpThread.sendMessage(theNewMessage, peerAddr.getText().toString());
    	}catch(Exception e){
    		Log.e(TAG,"Cannot send message "+ e.getMessage());
    		Log.e(TAG, "Error message " + e);
    	}
    	receivedMessages.add("Me: "+theNewMessage);
    	msg.setText("");
    }
    
    private class SendPackage extends AsyncTask<String, Void, String> {
   
		@Override
		protected String doInBackground(String... messageToSend) {
	    	try{
	    		Log.i(TAG, "Sending message in Async task");
	    		udpThread.sendMessage(messageToSend[0], peerAddr.getText().toString());
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
    
}
