package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.TblUser;
import com.iems5722.chatapp.preference.UnitConverter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UserListAdapter extends SimpleCursorAdapter {
	private static final String TAG = "UserListAdapter";

	private Context context;
	private int layout;	

	public UserListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		//Log.d(TAG, "UserListAdapter");
		this.context = context;
		this.layout = layout;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//Log.d(TAG, "newView");
		cursor = getCursor();
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
		int colUsername   = c.getColumnIndex(TblUser.USER_NAME);
		int colStatus     = c.getColumnIndex(TblUser.STATUS);
		int colLastOnline = c.getColumnIndex(TblUser.USER_DATETIME);
		
		String username = c.getString(colUsername);	
		String status   = c.getString(colStatus);
		Long lastonline = c.getLong(colLastOnline);
		
		TextView disp_username = (TextView) v.findViewById(R.id.user_name);
		TextView disp_status   = (TextView) v.findViewById(R.id.user_status);

		disp_username.setText(username);
		if (status.equals("online")) {
			disp_status.setText(status);
		}
		else {
			disp_status.setText(context.getText(R.string.user_offline_lastseen) + " " + UnitConverter.getDateTime(lastonline));
		}
	}
}
