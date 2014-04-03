package com.iems5722.chatapp.gui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.database.TblUser;

//gets private chat sessions
public class SessionListAdapter extends SimpleCursorAdapter {
	private static final String TAG = "SessionListAdapter";

	private Context context;
	private int layout;	
	private Cursor cursor;

	public SessionListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
		this.cursor = c;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//Log.d(TAG, "newView");
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);
		loadView(v, context, cursor);
		return v;
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		//Log.d(TAG, "bindView");
		loadView(v, context, c);
	}	
	
	private void loadView(View v, Context context, Cursor c) {
		//Log.d(TAG, "loadView");		
		int colUsername  = c.getColumnIndex(TblUser.USER_NAME);
		int colTimestamp = c.getColumnIndex(TblChat.MSG_DATETIME);
		int colLastMsg   = c.getColumnIndex(TblChat.MESSAGE);
		
		String username   = c.getString(colUsername);	
		int timestamp     = c.getInt(colTimestamp);
		String lastonline = c.getString(colLastMsg);
		
		TextView disp_peername  = (TextView) v.findViewById(R.id.chat_peer);
		TextView disp_timestamp = (TextView) v.findViewById(R.id.chat_timestamp);
		TextView disp_lastmsg   = (TextView) v.findViewById(R.id.chat_lastmessage);

		disp_peername.setText(username);
		disp_timestamp.setText(Integer.toString(timestamp));
		disp_lastmsg.setText(lastonline);
	}
}