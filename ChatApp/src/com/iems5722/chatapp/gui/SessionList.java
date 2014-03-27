package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblUser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

public class SessionList extends ListFragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "SessionList";
	
	private long mRowId;		
	private SessionListAdapter mAdapter;
	private Cursor cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		Bundle arguments = getArguments();
		if (arguments != null) {
			mRowId = arguments.getLong(TblChat.MESSAGE_ID);
		}
		String[] from = new String[] {TblChat.USER_ID};
		int[] to = new int[] {R.id.chat_peer};
		SQLiteDatabase db = DbProvider.database.getReadableDatabase();
		String sql = "SELECT " + TblChat.TABLE_CHAT + "." + TblChat.MESSAGE_ID + ", " + TblChat.MESSAGE + ", " + TblChat.SESSION_ID
				+ ", MAX(" + TblChat.MSG_DATETIME + "), " + TblUser.USER_NAME 
				+ ", " + TblUser.TABLE_USER + "." + TblUser.USER_ID + ", " + TblChat.TABLE_CHAT + "." + TblChat.USER_ID
				+ " FROM " + TblChat.TABLE_CHAT + " INNER JOIN " + TblUser.TABLE_USER
				+ " ON "  + TblChat.TABLE_CHAT  + "." + TblChat.USER_ID + " = " + TblUser.TABLE_USER + "." + TblUser.USER_ID
				+ " GROUP BY " + TblChat.SESSION_ID;
		Log.d(SessionList.class.getName(), sql);
		Cursor cursor = db.rawQuery(sql, null);
		mAdapter = new SessionListAdapter(getActivity(), R.layout.chatlist_detail, cursor, from, to, 0);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);		
	}	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//Log.d(APP_TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.priv_chat_empty));
	}	
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		String selection = TblChat.SESSION_ID + " != ?";
		String[] selectionArgs = {"0"};
		String sortOrder = TblChat.MSG_DATETIME;
		return new CursorLoader(getActivity(), DbProvider.PCHAT_URI, null, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);		
	}

}
