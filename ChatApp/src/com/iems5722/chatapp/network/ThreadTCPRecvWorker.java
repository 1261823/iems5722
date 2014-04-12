package com.iems5722.chatapp.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.iems5722.chatapp.preference.MsgNotifier;

public class ThreadTCPRecvWorker extends Thread{

	private Socket receiptSocket;
	private Context context;
	private MsgNotifier msgNotifier;
	private Handler mHandler;
	private MessageBuilder msgBuilder;
	private PeerFileService peerFileService;
	
	private static final String TAG="ThreadTCPRecvWorker";
	private final static int HEADER_SIZE=1024;
	private final static String INFO_SEPARATOR=";";
	private final static String FILE_INFO_SEPARATOR="#";
	private final static String PRESERVED_DIR="chatApp";
	private final static String ENDING_STRING = "@";
	private final static String URI_STRING="Uri_Here";
	
	public ThreadTCPRecvWorker(Socket receiptSocket, Context context, PeerFileService peerFileService){
		this.receiptSocket = receiptSocket;
		this.context = context;
		this.msgNotifier = new  MsgNotifier(context);
		this.peerFileService = peerFileService;
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
			}else if (firstParam.equals(PeerFileService.MSG_TYPE_FILE)){//assume other type must be file
				//decode file info detail msg
				Log.d(TAG, "second Param" + secondParam);
				String[] fileDetailArray = secondParam.split(FILE_INFO_SEPARATOR);
				String formattedChatMsg = fileDetailArray[0];
				String filenameString = fileDetailArray[1];
				String filesizeString = fileDetailArray[2];
				
				int filesize = Integer.parseInt(filesizeString);
				
				//receive file after info arrived
				byte[] fileByteArray = new byte[filesize];
				Log.d(TAG, "File comes");
				
				createFileWorkspace();
				
				File newFile = new File(Environment.getExternalStorageDirectory()+"/"+PRESERVED_DIR, filenameString);
				
				if(newFile==null){
					Log.d(TAG, "no space to create file in external storage");
				}
				FileOutputStream fos = new FileOutputStream(newFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				
				peerFileService.initFileDownloadProgress();
				
				int bytesRead = inputStream.read(fileByteArray, 0, filesize);
				int currentBytesRead = 0; 
				while (bytesRead > -1){
					 currentBytesRead += bytesRead;
					 
					 peerFileService.updateFileDownloadProgress(getFileProgressPercentage(currentBytesRead,filesize));
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
				
				Uri fileUri = Uri.fromFile(newFile);
				

				//things to do after received files 
				msgNotifier.messageReceive();
				
				msgBuilder.savePrivateMessage(formattedChatMsg.replace(URI_STRING, "content://" + fileUri.toString()), null);
				
				//only can see when still in private chat
				peerFileService.previewFileProcess(fileUri);
				
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
	
	public static void ensure_dir(File new_dir) { 
		 if (!new_dir.exists()) { 
			 if (!new_dir.mkdirs()) { 
				 Log.d(TAG, "Failed to create directory " + new_dir); 
			 } else { 
				 Log.d(TAG, "Directory created " + new_dir); 
			 } 
		} 
	}

	 private void createFileWorkspace() { 
		 Log.d(TAG, "createWorkspace"); 
		 if (isExternalStorageWritable()) { 
			 File newFile = new File(Environment.getExternalStorageDirectory(), PRESERVED_DIR); 
			 ensure_dir(newFile); 
		 }		 
	 } 

	 public boolean isExternalStorageWritable() { 
		 String state = Environment.getExternalStorageState(); 
		 if (Environment.MEDIA_MOUNTED.equals(state)) { 
			 return true; 
		 } 
		 	return false; 
	 } 
	 
	 private int getFileProgressPercentage(int receivedSize, int totalSize){
		 double receivedSizeDouble = new Double(receivedSize).doubleValue();
		 double totalSizeDouble = new Double(totalSize).doubleValue();
		 double resultPercent = (receivedSizeDouble/totalSizeDouble) * 100.00f;
		 return new Double(resultPercent).intValue();
	 }
	
}
