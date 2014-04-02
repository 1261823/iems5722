package com.iems5722.chatapp.network;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class PeerFileSender extends Thread {
	final static private String TAG = "PeerFileSender";
	public final static int TCP_PORT = 6669;
	Socket tcpSocket = null;
	boolean socketOK = true;
	private final static int HEADER_SIZE=1024;
	private final static String INFO_SEPARATOR=";";
	private final static String ENDING_STRING = "@";
	
	
	Context context;
	Handler handler;
	
	public PeerFileSender(Context currentContext, Handler handler) {
		this.context = currentContext;
		this.handler = handler;
	}
	
	@Override
	public void run() {
		
			try {
				String filename = "c1.jpg";
				File file = new File("/sdcard/" + filename);
				long fileSize= file.length();
					
				
				tcpSocket = new Socket("10.0.2.2", TCP_PORT);
				
				//send file info first
			/*	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(tcpSocket.getOutputStream()));
				StringBuilder fileInfoMsgSb = new StringBuilder();
				
				fileInfoMsgSb.append(filename);
				fileInfoMsgSb.append(INFO_SEPARATOR);
				fileInfoMsgSb.append(String.valueOf(fileSize));
				
				bw.write(fileInfoMsgSb.toString());*/
				byte [] fileByteArray  = new byte [(int)(fileSize)];
				byte [] outputByteArray = new byte [(int)(HEADER_SIZE+fileSize)];
				
				
				StringBuilder fileInfoMsgSb = new StringBuilder();
				
				fileInfoMsgSb.append(filename);
				fileInfoMsgSb.append(INFO_SEPARATOR);
				fileInfoMsgSb.append(String.valueOf(fileSize));
				fileInfoMsgSb.append(ENDING_STRING);
				
				byte [] infoByteArray = fileInfoMsgSb.toString().getBytes("UTF-8");
				
				for (int j=0; j<infoByteArray.length; j++){				
						outputByteArray[j] = infoByteArray[j];					
				}
				
				//send file bytes  				
				
				
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(fileByteArray ,0,fileByteArray.length);
                
                for (int k=HEADER_SIZE; k<outputByteArray.length; k++){
                	outputByteArray[k] = fileByteArray[k-HEADER_SIZE];               	
                }
                                      
                OutputStream os = tcpSocket.getOutputStream();
                os.write(outputByteArray,0,outputByteArray.length);
                os.flush();
                
                
                Log.d(TAG, "file is sent");
                
                bis.close();
                fis.close();
				closeSocket();
				
				
			} catch (IOException e) {
				Log.e(TAG, "problem when sending files " + e.getMessage());
			}
				
		
	}
	
	public void closeSocket()
	{
		try {
			tcpSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Cannot stop TCP server " + e.getMessage());
		}
	}
	
	public boolean socketIsOK() {
		return socketOK;
	}
}
