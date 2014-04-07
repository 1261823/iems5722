package com.iems5722.chatapp.network;

import java.util.Calendar;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.preference.Settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

//Creates and interprets messages
public class MessageBuilder implements OnSharedPreferenceChangeListener {
	public final static String TAG = "MessageBuilder";
	private Context mContext;
	SharedPreferences prefs; 
	
	//Message type codes
	//ping message types
	public final static String PING_REQ_MSG = "PR";
	public final static String PING_ACK_MSG = "PA";	
	public final static String SIGN_OUT = "SO";
	public final static String GLOBAL_MSG = "GM";
	public final static String PRIVATE_MSG = "PM";
	
	//User id used to identify sender
	private static String msgUserId =  "";
	
	//Message parts
	public final static int MsgType = 0;
	public final static int MsgUser = 1;
	public final static int MsgContent = 2;
	private final static String msgSeparator = "-";
	
	public MessageBuilder(Context mContext) {
		this.mContext = mContext;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		readPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	public void readPreferences() {
		String userIdKey = mContext.getString(R.string.pref_key_userid);
		msgUserId = prefs.getString(userIdKey, "");
	}
	
	public String messageCreate(String MessageType, String MessageContent) {
		StringBuilder sb = new StringBuilder(ServiceNetwork.Packet_Size);
		sb.append(MessageType);
		sb.append(msgSeparator);
		sb.append(msgUserId);
		sb.append(msgSeparator);
		sb.append(MessageContent);
		Log.d(TAG, "Built message " + sb.toString());
		return sb.toString();
	}

	public static String getMessagePart(String inMessage, int msgPart) {
		String[] msgParts = inMessage.split(msgSeparator, 4);
		//Log.i(TAG, "Type " + msgParts[MsgType]);
		//Log.i(TAG, "User " + msgParts[MsgUser]);
		//Log.i(TAG, "Content " + msgParts[MsgContent]);
		return msgParts[msgPart];
	}
	
	public void saveGlobalMessage(String inMessage) {
    	String msgType = getMessagePart(inMessage, MsgType);
    	String msgUser = getMessagePart(inMessage, MsgUser);
    	String msgContent = getMessagePart(inMessage, MsgContent);
    	
    	Log.d(TAG, "Msg " + msgType + " UserId " + msgUser + " Username " + msgContent);

		if (msgType.equals(GLOBAL_MSG)) {
			Calendar c = Calendar.getInstance();
			long curDateTimeMS = c.getTimeInMillis();  
			
			ContentValues values = new ContentValues();
			
			values.put(TblGlobalChat.USER_ID, msgUser);
			values.put(TblGlobalChat.MESSAGE, msgContent);
			values.put(TblGlobalChat.MSG_DATETIME, curDateTimeMS);
			//add new global chat message
			Uri itemUri = mContext.getApplicationContext().getContentResolver().insert(DbProvider.GCHAT_URI, values);
			Log.d(TAG, "Added new global message " + itemUri.toString());  		
		}
	}
	
	public void savePrivateMessage(String inMessage, String session) {
    	String msgType = getMessagePart(inMessage, MsgType);
    	String msgUser = getMessagePart(inMessage, MsgUser);
    	String msgContent = getMessagePart(inMessage, MsgContent);
    	
    	Log.d(TAG, "Msg " + msgType + " UserId " + msgUser + " Username " + msgContent);
    	
		if (msgType.equals(PRIVATE_MSG)) {
			Calendar c = Calendar.getInstance();
			long curDateTimeMS = c.getTimeInMillis(); 
			
			ContentValues values = new ContentValues();
			
			values.put(TblChat.USER_ID, msgUser);
			values.put(TblChat.MESSAGE, msgContent);
			values.put(TblChat.MSG_DATETIME, curDateTimeMS);
			values.put(TblChat.SESSION_ID, session);
			//add new private chat message
			Uri itemUri = mContext.getApplicationContext().getContentResolver().insert(DbProvider.PCHAT_URI, values);
			Log.d(TAG, "Added new private message " + itemUri.toString());  	
		}
		

		
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(mContext.getString(R.string.pref_key_userid))) {
			readPreferences();
		}
	}	
	
}
