package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.database.TblUser;
import com.iems5722.chatapp.preference.UnitConverter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class GlobalChatAdapter extends SimpleCursorAdapter {
	public final static String TAG = "GlobalChatAdapter";
	private Context context;
	private int layout;
	
	private TextView vMsgId;
	private TextView vMsgAuthor;
	private TextView vMsgContent;
	private TextView vMsgTimestamp;
	
	public GlobalChatAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;	
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//Log.d(TAG, "newView");
		Cursor c = getCursor();
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//for (int i = 0 ; i < cursor.getColumnCount(); i++) {
		//	Log.i(TAG, cursor.getColumnName(i) + " : " + cursor.getString(i));
		//}
		
		//change the message display if is own message
		View v;
		int msgAuthor = c.getColumnIndex(TblUser.USER_NAME);
		String dbMsgAuthor = c.getString(msgAuthor);

		//LayoutParams params;

		//Log.i(TAG, "Msg " + Long.toString(dbMsgId) + " from " + dbMsgAuthor + " vs " + Activity_TabHandler.userId);
		if (dbMsgAuthor.equals(Activity_TabHandler.msgUsername)) {
			//Log.i(TAG, "sent");
			v = inflater.inflate(R.layout.chat_message_sent, null);
			//params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			//params.setMargins(60, 10, 10, 0);
			//params.gravity = Gravity.RIGHT;
		}		
		else {
			//Log.i(TAG, "recv");
			v = inflater.inflate(layout, null);
			//params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			//params.setMargins(10, 10, 60, 0);
		}

		//v.setLayoutParams(params);
		loadView(v, context, c);
		return v;
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		//log.d(APP_TAG, "bindView");
		loadView(v, context, c);
	}
	
	private void loadView(View v, Context context, Cursor c) {
		LayoutParams params;
		params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(60, 10, 10, 0);
		v.setLayoutParams(params);
		//Log.d(TAG, "Applying params");
		
		int msgId = c.getColumnIndex(TblGlobalChat.MESSAGE_ID);
		int msgAuthor = c.getColumnIndex(TblUser.USER_NAME);
		int msgContent = c.getColumnIndex(TblGlobalChat.MESSAGE);
		int msgTimestamp = c.getColumnIndex(TblGlobalChat.MSG_DATETIME);
		
		long dbMsgId = c.getLong(msgId);	
		String dbMsgAuthor = c.getString(msgAuthor);
		String dbMsgContent = c.getString(msgContent);
		long dbMsgTimestamp = c.getLong(msgTimestamp);

		
		vMsgId = (TextView) v.findViewById(R.id.msg_id);
		vMsgAuthor = (TextView) v.findViewById(R.id.msg_author);
		vMsgContent = (TextView) v.findViewById(R.id.msg_recv);
		vMsgTimestamp = (TextView) v.findViewById(R.id.msg_timestamp);
		
		vMsgId.setText(Long.toString(dbMsgId));
		vMsgAuthor.setText(dbMsgAuthor);
		vMsgContent.setText(dbMsgContent);
		vMsgTimestamp.setText(UnitConverter.getDateTime(dbMsgTimestamp));
	}	
}
