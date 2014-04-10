package com.iems5722.chatapp.network;

import java.io.IOException;
import java.net.ServerSocket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PeerFileReceiver extends Handler {
	public final static String TAG = "PeerFileReceiver";
	public final static int TCP_PORT = 6669;
	ServerSocket serverSocket = null;
	boolean socketOK = true;
	
	
	private Context context;
	private Handler mHandler;
	private PeerFileService peerFileService;

	
	
	public final static int INITIAL_TCP_PORT  = 1;
	public final static int TCP_LISTEN = 2;
	
	
	public PeerFileReceiver(Looper looper, Context currentContext) {
		this.context = currentContext;
		Log.d(TAG, "Creating service");		
	}
	
	public Handler getmHandler() {
		return mHandler;
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}
	
	public PeerFileService getPeerFileService() {
		return peerFileService;
	}

	public void setPeerFileService(PeerFileService peerFileService) {
		this.peerFileService = peerFileService;
	}


	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
			case INITIAL_TCP_PORT :  
							 initialTcpPort();
							 break;
			case TCP_LISTEN :
							 tcpListen(); 
							 break;
			default: break;
		}
		
	}
	
	public void initialTcpPort(){
		try {
			serverSocket = new ServerSocket(TCP_PORT);			
			socketOK = true;
		}
		catch (IOException e) {
			Log.e(TAG, "Cannot open socket " + e.getMessage());
			socketOK = false;
			return;
		}		
	}
	
	public void tcpListen() {
			
		while(socketOK) {
			try{
				ThreadTCPRecvWorker threadTCPRecvWorker = new ThreadTCPRecvWorker(serverSocket.accept(), this.context, this.peerFileService);
				threadTCPRecvWorker.start();
			}catch(Exception exception){
				Log.d(TAG, "Error while waiting incoming TCP connection:"+ exception.getMessage());
				socketOK =false;
				closeSocket();
			}
		}
	}
	
	
	public void closeSocket()
	{
		try {
			if(!serverSocket.isClosed()){
				serverSocket.close();
			}
		} catch (IOException e) {
			Log.e(TAG, "Cannot stop TCP receive server socket " + e.getMessage());
		}
	}
	
	public boolean socketIsOK() {
		return socketOK;
	}
	
	public void setSocketOK(boolean socketOk) {
		this.socketOK=socketOK;
	}
}
