package com.iems5722.chatapp.network;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.gui.TcpAttachMsgVO;

public class PeerFileSender extends Handler {
	public final static  String TAG = "PeerFileSender";
	public final static int TCP_PORT = 6669;
	Socket tcpSocket = null;
	boolean socketOK = true;
	private final static int HEADER_SIZE=1024;
	private final static String INFO_SEPARATOR=";";
	private final static String ENDING_STRING = "@";
	
	public final static int SEND_FILE = 1;
	public final static int SEND_MSG = 2;
	
	private Context context;
	private MessageBuilder msgBuilder;
	
	public PeerFileSender(Looper looper, Context currentContext) {
		super(looper);
		Log.d(TAG, "Creating service");
		this.context = currentContext;
		msgBuilder = new MessageBuilder(context);
	}
	
	@Override
	public void handleMessage(Message msg) {
		Log.d(TAG, "handle msg :" + msg.what);
		switch(msg.what) {
		case SEND_FILE : TcpAttachMsgVO fileToSendVO = (TcpAttachMsgVO)msg.obj;
						 sendFile(fileToSendVO); 
						 break;
		case SEND_MSG : TcpAttachMsgVO msgToSendVO = (TcpAttachMsgVO)msg.obj;
						sendMsg(msgToSendVO);				 
						break; 
			default: break;
		}
		
	}
	
	public void sendMsg(TcpAttachMsgVO msgToSendVO) {
		try{
			String formattedChatMsg = msgBuilder.messageCreate(MessageBuilder.PRIVATE_MSG, msgToSendVO.getChatMsg());
			String fileInfoString = createFileInfoString(PeerFileService.MSG_TYPE_CHAT, formattedChatMsg);
			byte [] infoByteArray = fileInfoString.toString().getBytes("UTF-8");
			msgBuilder.savePrivateMessage(formattedChatMsg, msgToSendVO.getChatSessionId());
			tcpSend(msgToSendVO.getUserIp(), infoByteArray);
			
			Log.d(TAG, "TCP Msg sent : "+ msgToSendVO.getChatMsg());
		}catch(Exception ex){
			Log.d(TAG, ex.getMessage());
		}
	}
	
	
	
	
	public void sendFile(TcpAttachMsgVO fileToSendVO) {
		
			try {
				Log.d(TAG, "file to send URI : " + fileToSendVO.getAttachmentUri());
				File file = new File(getRealPathFromURI(fileToSendVO.getAttachmentUri()));
				long fileSize= file.length();
				
				byte [] fileByteArray  = new byte [(int)(fileSize)];
				byte [] outputByteArray = new byte [(int)(HEADER_SIZE+fileSize)];
				
				String fileInfoString = createFileInfoString(file.getName(), String.valueOf(fileSize));
				
				byte [] infoByteArray = fileInfoString.getBytes("UTF-8");
				
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
                
                tcpSend(fileToSendVO.getUserIp(), outputByteArray);
                
                Log.d(TAG, "file is sent");
                
                bis.close();
                fis.close();
				
			} catch (Exception e) {
				Log.e(TAG, "problem when sending files " + e.getMessage());
			} finally{
				closeSocket();
			}
		
	}
	
	private String createFileInfoString(String firstParam, String secondParam)throws Exception{
		StringBuilder fileInfoMsgSb = new StringBuilder();
		
		fileInfoMsgSb.append(firstParam);
		fileInfoMsgSb.append(INFO_SEPARATOR);
		fileInfoMsgSb.append(secondParam);
		fileInfoMsgSb.append(ENDING_STRING);
		return fileInfoMsgSb.toString();
	}
	
	private void tcpSend(String ipAddressToSend, byte[] byteArrayToSend) throws Exception {
		tcpSocket = new Socket(ipAddressToSend, TCP_PORT);
		OutputStream os = tcpSocket.getOutputStream();
        os.write(byteArrayToSend,0,byteArrayToSend.length);
        os.flush();
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
			if (!tcpSocket.isClosed()){
				tcpSocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Cannot stop TCP server " + e.getMessage());
		}
	}
	
	public boolean socketIsOK() {
		return socketOK;
	}
}
