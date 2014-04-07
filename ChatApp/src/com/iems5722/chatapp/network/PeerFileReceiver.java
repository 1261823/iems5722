package com.iems5722.chatapp.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.gui.Activity_PrivateChat;
import com.iems5722.chatapp.network.ServiceNetwork.ServiceHandler;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

public class PeerFileReceiver extends Handler {
	public final static String TAG = "PeerFileReceiver";
	public final static int TCP_PORT = 6669;
	ServerSocket serverSocket = null;
	boolean socketOK = true;
	private final static int HEADER_SIZE=1024;
	private final static String INFO_SEPARATOR=";";
	private final static String SD_CARD_PATH="/sdcard/";
	private final static String ENDING_STRING = "@";
	
	private Context context;
	private Handler mHandler;

	
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
			Socket receiveSocket = null;
			try {
				
				/*//assume file info is sent first
				BufferedReader in;
				Socket receiveSocket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
				String lineStr = in.readLine();*/
				Log.d(TAG, "Peer file receiver listening");
		
				byte[] headerByteArray = new byte[HEADER_SIZE];
				
				receiveSocket = serverSocket.accept();
				
				Log.d(TAG, "Incoming TCP accepted");
				
				InputStream inputStream = receiveSocket.getInputStream();
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
					updatePrivateMsg(chatMsg);
					this.mHandler.obtainMessage(Activity_PrivateChat.TOAST, chatMsg);					
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
					this.mHandler.obtainMessage(Activity_PrivateChat.TOAST, "File received!");
					
					fos.close();
					bos.close();
				}
				
				inputStream.close();
				
				
			} catch (Exception e) {
				Log.e(TAG, "Problem when receiving files " + e.getMessage());
				socketOK = false;
				closeSocket();
			}finally{
				try{					
					if (receiveSocket!=null && !receiveSocket.isClosed()){
						receiveSocket.close();
					}
				}catch(IOException ioe){
					Log.d(TAG, ioe.getMessage());
				}
			}
		}
	}
	
	public void updatePrivateMsg(String message) throws Exception{
    	String msgType = MessageBuilder.getMessagePart(message, MessageBuilder.MsgType);
    	String msgUser = MessageBuilder.getMessagePart(message, MessageBuilder.MsgUser);
    	String msgContent = MessageBuilder.getMessagePart(message, MessageBuilder.MsgContent);
    	
    	String userIdKey = context.getString(R.string.pref_key_userid);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String receiverUserId = prefs.getString(userIdKey, "");
    	
    	Log.d(TAG, "Msg " + msgType + " UserId " + msgUser + " Username " + msgContent);
    	
    	Calendar c = Calendar.getInstance();
		long curDateTimeMS = c.getTimeInMillis();     	
    	    	
		ContentValues values = new ContentValues();
		values.put(TblChat.USER_ID, msgUser);
		values.put(TblChat.MESSAGE, msgContent);
		values.put(TblChat.MSG_DATETIME, curDateTimeMS);
		values.put(TblChat.SESSION_ID, receiverUserId);
		//add new user
		Uri itemUri = context.getApplicationContext().getContentResolver().insert(DbProvider.PCHAT_URI, values);
		Log.d(TAG, "Added new private message " + itemUri.toString());    	
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
