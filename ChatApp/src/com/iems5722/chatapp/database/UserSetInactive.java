package com.iems5722.chatapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

//Set all users to inactive
public class UserSetInactive extends AsyncTask<Void, Void, Void> {
	public final static String TAG = "UserSetInactive";
	private Context context;
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		Log.i(TAG, "Setting users to inactive");
		ContentValues values = new ContentValues();
		values.put(TblUser.STATUS, TblUser.STAT_OFF);
		int count = -1;
		count = context.getContentResolver().update(DbProvider.USER_URI, values, null, null);
		if (count == -1) {
			throw new IllegalStateException("Unable to update");
		}
		return null;
	}
}
