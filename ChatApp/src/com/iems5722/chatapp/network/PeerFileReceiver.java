package com.iems5722.chatapp.network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	private final static int HEADER_SIZE=1024;
	private final static String INFO_SEPARATOR=";";
	private final static String SD_CARD_PATH="/sdcard/";
	private final static String ENDING_STRING = "@";
	
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
				
				/*//assume file info is sent first
				BufferedReader in;
				Socket receiveSocket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
				String lineStr = in.readLine();*/
				
				byte[] headerByteArray = new byte[HEADER_SIZE];
				Socket receiveSocket = serverSocket.accept();
				InputStream inputStream = receiveSocket.getInputStream();
				inputStream.read(headerByteArray, 0, HEADER_SIZE);
				
				String fileInfoStr = new String(headerByteArray, "UTF-8");
				String fileInfoUsefulPart = fileInfoStr.substring(0, fileInfoStr.indexOf(ENDING_STRING));
				Log.i(TAG, "File info arrived "+fileInfoUsefulPart);
				
				String [] fileInfoStrArray = fileInfoUsefulPart.split(INFO_SEPARATOR); 
				String filename = fileInfoStrArray[0];
				String filesizeStr = fileInfoStrArray[1];
				int filesize = Integer.parseInt(filesizeStr);

				
				//receive file after info arrived
				byte[] fileByteArray = new byte[filesize];
				Log.d(TAG, "File comes");

				File newFile = new File(SD_CARD_PATH+filename);
				FileOutputStream fos = new FileOutputStream(newFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				
				int bytesRead = inputStream.read(fileByteArray, 0, filesize);
				int currentBytesRead = 0; 
				while (bytesRead > -1){
					 currentBytesRead += bytesRead;
					 if (currentBytesRead>=filesize){ 
						 break;
					 }
					 bytesRead = inputStream.read(fileByteArray, currentBytesRead, filesize-currentBytesRead);
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
