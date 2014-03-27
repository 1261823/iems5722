package com.iems5722.chatapp.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class PeerFileReceiver extends Thread {
	final static private String TAG = "PeerFileReceiver";
	public final static int TCP_PORT = 6669;
	ServerSocket serverSocket = null;
	boolean socketOK = true;
	
	private Context context;
	private Handler handler;
	
	
	
	public PeerFileReceiver(Context currentContext, Handler handler) {
		this.context = currentContext;
		this.handler = handler;
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
	
	@Override
	public void run() {
			
		while(socketOK) {
			try {
				
//				BufferedReader in;
//				Socket receiveSocket = serverSocket.accept();
//				in = new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
//				String lineStr = in.readLine();
//				while (lineStr!=null){
//					handler.obtainMessage(Chat.TOAST, lineStr).sendToTarget();
//					Log.i(TAG, "New client connected");
//					lineStr = in.readLine();					
//				}
				
				Socket receiveSocket = serverSocket.accept();
				int fileSize=690314;
				byte[] fileByteArray = new byte[fileSize];
				Log.d(TAG, "File comes");

				File newFile = new File("/sdcard/trial.jpg");
				FileOutputStream fos = new FileOutputStream(newFile);
				InputStream inputStream = receiveSocket.getInputStream();
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				int bytesRead = inputStream.read(fileByteArray, 0, fileSize);
				int currentBytesRead = 0;
				while (bytesRead > -1){
					 currentBytesRead += bytesRead;
					 if (currentBytesRead>=fileSize){ 
						 break;
					 }
					 bytesRead = inputStream.read(fileByteArray, currentBytesRead, fileSize-currentBytesRead);
				}
				if(currentBytesRead!=-1){
					bos.write(fileByteArray, 0, currentBytesRead);
					bos.flush();					
				}
				
				Log.d(TAG, "File receive finished");
				
				
				fos.close();
				bos.close();
				inputStream.close();
				receiveSocket.close();
				
			} catch (IOException e) {
				Log.e(TAG, "Problem when receiving files " + e.getMessage());
				socketOK = false;
				closeSocket();
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
