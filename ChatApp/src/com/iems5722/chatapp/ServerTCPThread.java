package com.iems5722.chatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class ServerTCPThread extends Thread {
	final static private String TAG = "ServerTCPThread";
	private Socket socket = null;
	private Handler handler = null;
	
	public ServerTCPThread(Socket socket, Handler tcpHandler) {
		Log.i(TAG, "Creating new TCP Recv Thread");
		this.socket = socket;
		this.handler = tcpHandler;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "TCP Receiving Thread");
		try {
			BufferedReader input = new BufferedReader(
								   new InputStreamReader(socket.getInputStream()));
			String inputLine;
			while((inputLine = input.readLine()) != null) {
	        	handler.obtainMessage(Chat.TCP_PACKET, inputLine).sendToTarget();    
	        	Log.i(TAG,"Received sentence: "+ inputLine);
			}
			input.close();
			socket.close();
		}
		catch (IOException e) {
			Log.e("TAG", "Cannot open socket " + e.getMessage());
		}
	}

}
