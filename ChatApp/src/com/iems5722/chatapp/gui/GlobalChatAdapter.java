package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.TblGlobalChat;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GlobalChatAdapter extends SimpleCursorAdapter {
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
		//log.d(APP_TAG, "newView");
		Cursor c = getCursor();
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);
		loadView(v, context, c);
		return v;
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		//log.d(APP_TAG, "bindView");
		loadView(v, context, c);
	}
	
	private void loadView(View v, Context context, Cursor c) {
		int msgId = c.getColumnIndex(TblGlobalChat.MESSAGE_ID);
		int msgAuthor = c.getColumnIndex(TblGlobalChat.USER_ID);
		int msgContent = c.getColumnIndex(TblGlobalChat.MESSAGE);
		int msgTimestamp = c.getColumnIndex(TblGlobalChat.MSG_DATETIME);
		
		long dbMsgId = c.getLong(msgId);	
		String dbMsgAuthor = c.getString(msgAuthor);
		String dbMsgContent = c.getString(msgContent);
		long dbMsgTimestamp = c.getLong(msgTimestamp);
		
		vMsgId = (TextView) v.findViewById(R.id.msg_author);
		vMsgAuthor = (TextView) v.findViewById(R.id.msg_timestamp);
		vMsgContent = (TextView) v.findViewById(R.id.msg_id);
		vMsgTimestamp = (TextView) v.findViewById(R.id.msg_recv);
		
		vMsgId.setText(Long.toString(dbMsgId));
		vMsgAuthor.setText(dbMsgAuthor);
		vMsgContent.setText(dbMsgContent);
		vMsgTimestamp.setText(Long.toString(dbMsgTimestamp));
	}	
}
