package com.iems5722.chatapp;

import java.io.IOException;
import java.net.ServerSocket;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ServerTCP extends Thread {
	final static private String TAG = "ServerTCP";
	public final static int TCP_PORT = 6667;
	ServerSocket serverSocket = null;
	boolean socketOK = true;
	
	Context context;
	Handler handler;
	
	public ServerTCP(Context currentContext, Handler handler) {
		this.context = currentContext;
		this.handler = handler;
		try {
			serverSocket = new ServerSocket(TCP_PORT);
		}
		catch (IOException e) {
			Log.e(TAG, "Cannot open socket " + e.getMessage());
			socketOK = false;
			return;
		}
	}
	
	@Override
	public void run() {
		Log.i(TAG, "In the running TCP thread");
		while(socketOK) {
			try {
				new ServerTCPThread(serverSocket.accept(), handler).start();
				Log.i(TAG, "New client connected");
			} catch (IOException e) {
				Log.e(TAG, "Cannot start thread for client " + e.getMessage());
			}
		}
	}
	
	public void closeSocket()
	{
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Cannot stop TCP server " + e.getMessage());
		}
	}
	
	public boolean socketIsOK() {
		return socketOK;
	}
	
	
}
