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
	
	Context context;
	Handler handler;
	
	public PeerFileSender(Context currentContext, Handler handler) {
		this.context = currentContext;
		this.handler = handler;
	}
	
	@Override
	public void run() {
		
			try {
				tcpSocket = new Socket("10.0.2.2", TCP_PORT);
				/*BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(tcpSocket.getOutputStream()));
				String testMsg = "Hi Hi Tcp Test";
				bw.write(testMsg);
				bw.close();*/
				
			    
				File file = new File("/sdcard/c1.jpg");
				long fileSize= file.length();
				byte [] fileByteArray  = new byte [(int)fileSize];
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(fileByteArray,0,fileByteArray.length);
                
        
                
                OutputStream os = tcpSocket.getOutputStream();
                os.write(fileByteArray,0,fileByteArray.length);
                os.flush();
                
                
                Log.d(TAG, "file sent");
                
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
