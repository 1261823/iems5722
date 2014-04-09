package com.iems5722.chatapp.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.gui.Activity_PrivateChat;
import com.iems5722.chatapp.preference.MsgNotifier;

public class ThreadTCPRecvWorker extends Thread{

	private Socket receiptSocket;
	private Context context;
	private MsgNotifier msgNotifier;
	private Handler mHandler;
	private MessageBuilder msgBuilder;
	
	private static final String TAG="ThreadTCPRecvWorker";
	private final static int HEADER_SIZE=1024;
	private final static String INFO_SEPARATOR=";";
	private final static String SD_CARD_PATH="/sdcard/";
	private final static String ENDING_STRING = "@";
	
	public ThreadTCPRecvWorker(Socket receiptSocket, Context context, Handler handler){
		this.receiptSocket = receiptSocket;
		this.context = context;
		this.msgNotifier = new  MsgNotifier(context);
		this.mHandler = handler;
		msgBuilder = new MessageBuilder(context);
	}
	
	@Override
	public void run(){
		
		InputStream inputStream = null;		
		try {
			/*//assume file info is sent first
			BufferedReader in;
			Socket receiveSocket = serverSocket.accept();
			in = new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
			String lineStr = in.readLine();*/
			Log.d(TAG, "Peer file receiver listening");
	
			byte[] headerByteArray = new byte[HEADER_SIZE];
			
			Log.d(TAG, "Incoming TCP accepted");
			
			inputStream = this.receiptSocket.getInputStream();
			inputStream.read(headerByteArray, 0, HEADER_SIZE);
			
			String fileInfoStr = new String(headerByteArray, "UTF-8");
			String fileInfoUsefulPart = fileInfoStr.substring(0, fileInfoStr.indexOf(ENDING_STRING));
			Log.d(TAG, "File info arrived "+fileInfoUsefulPart);
			
			
			String [] fileInfoStrArray = fileInfoUsefulPart.split(INFO_SEPARATOR); 
			String firstParam = fileInfoStrArray[0];
			String secondParam = fileInfoStrArray[1];
			
			
			if (firstParam.equals(PeerFileService.MSG_TYPE_CHAT)){
				String chatMsg = secondParam;
				Log.d(TAG, "Received Chat Msg: " + chatMsg);
				msgBuilder.savePrivateMessage(chatMsg, null);
				msgNotifier.messageReceive();					
			}else{//assume other type must be file
				int filesize = Integer.parseInt(secondParam);
				
				//receive file after info arrived
				byte[] fileByteArray = new byte[filesize];
				Log.d(TAG, "File comes");
				this.mHandler.obtainMessage(Activity_PrivateChat.TOAST, "File comes");

				File newFile = new File(Environment.getExternalStorageDirectory(),firstParam);
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
				
				msgNotifier.messageReceive();
				
				fos.close();
				bos.close();
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Problem when receiving files " + e.getMessage());
		}finally{
			try{					
				if (inputStream!=null){
					inputStream.close();
				}
				if (this.receiptSocket!=null && !this.receiptSocket.isClosed()){
					this.receiptSocket.close();
				}
			}catch(IOException ioe){
				Log.d(TAG, ioe.getMessage());
			}
		}

	}
	
	
	
}
