package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.database.TblUser;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PrivateChatAdapter extends SimpleCursorAdapter {
	public final static String TAG = "PrivateChatAdapter";
	private Context context;
	private int layout;
	
	private TextView vMsgId;
	private TextView vMsgAuthor;
	private TextView vMsgContent;
	private TextView vMsgTimestamp;
	
	public PrivateChatAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;	
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//log.d(APP_TAG, "newView");
		Cursor c = getCursor();
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v;
		int msgAuthor = c.getColumnIndex(TblUser.USER_NAME);
		String dbMsgAuthor = c.getString(msgAuthor);

		if (dbMsgAuthor.equals(Activity_TabHandler.msgUsername)) {
			//Log.i(TAG, "sent");
			v = inflater.inflate(R.layout.chat_message_sent, null);
		}
		else {
			//Log.i(TAG, "recv");
			v = inflater.inflate(layout, null);
		}
		loadView(v, context, c);
		return v;
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		//log.d(APP_TAG, "bindView");
		loadView(v, context, c);
	}
	
	private void loadView(View v, Context context, Cursor c) {
		int msgId = c.getColumnIndex(TblChat.MESSAGE_ID);
		int msgAuthor = c.getColumnIndex(TblUser.USER_NAME);
		int msgContent = c.getColumnIndex(TblChat.MESSAGE);
		int msgTimestamp = c.getColumnIndex(TblChat.MSG_DATETIME);
		
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
		vMsgTimestamp.setText(Long.toString(dbMsgTimestamp));
	}	
}
