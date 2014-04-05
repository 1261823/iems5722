package com.iems5722.chatapp.network;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

public class PeerFileSender extends Handler {
	public final static  String TAG = "PeerFileSender";
	public final static int TCP_PORT = 6669;
	Socket tcpSocket = null;
	boolean socketOK = true;
	private final static int HEADER_SIZE=1024;
	private final static String INFO_SEPARATOR=";";
	private final static String ENDING_STRING = "@";
	
	public final static int SEND_FILE = 1;
	
	private Context context;
	
	
	public PeerFileSender(Looper looper, Context currentContext) {
		super(looper);
		this.context = currentContext;
	
	}
	
	@Override
	public void handleMessage(Message msg) {
		Log.d(TAG, "handle msg :" + msg.what);
		switch(msg.what) {
		case SEND_FILE : Uri fileToSend = (Uri)msg.obj;
						 sendFile(fileToSend); 
						 break;
			default: break;
		}
		
	}
	
	
	public void sendFile(Uri fileToSend) {
		
			try {
				Log.d(TAG, "file to send URI : " + fileToSend);
//					String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//		            Cursor cursor = this.context.getContentResolver().query(
//		                               fileToSend, filePathColumn, null, null, null);
//		            cursor.moveToFirst();
//
//		            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//		            String filePath = cursor.getString(columnIndex);
//		            cursor.close();
//
//		            Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
				File file = new File(getRealPathFromURI(fileToSend));

				long fileSize= file.length();
					
				
				tcpSocket = new Socket("10.0.2.2", TCP_PORT);
				
				
				byte [] fileByteArray  = new byte [(int)(fileSize)];
				byte [] outputByteArray = new byte [(int)(HEADER_SIZE+fileSize)];
				
				
				StringBuilder fileInfoMsgSb = new StringBuilder();
				
				fileInfoMsgSb.append(file.getName());
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
				
				
			} catch (Exception e) {
				Log.e(TAG, "problem when sending files " + e.getMessage());
			}
		
	}
	
	private String getRealPathFromURI(Uri contentURI) {
	    String result;
	    Cursor cursor = this.context.getContentResolver().query(contentURI, null, null, null, null);
	    if (cursor == null) { 
	        result = contentURI.getPath();
	    } else { 
	        cursor.moveToFirst(); 
	        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	        result = cursor.getString(idx);
	        cursor.close();
	    }
	    return result;
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
