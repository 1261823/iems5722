package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblUser;

import android.app.DownloadManager.Query;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

//shows private chat sessions
public class SessionList extends ListFragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "SessionList";
	
	private long mRowId;		
	private SessionListAdapter mAdapter;
	private Cursor mCursor;
	
	static SessionList init() {
		SessionList fragSess = new SessionList();
		return fragSess;
	}		
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		String[] from = new String[] {TblChat.USER_ID};
		int[] to = new int[] {R.id.chat_peer};
		mAdapter = new SessionListAdapter(getActivity(), R.layout.chatlist_detail, null, from, to, 0);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);		
	}	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.priv_chat_empty));
	}	
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Log.d(TAG, "Creating loader");
		String selection = "SELECT " + TblChat.TABLE_CHAT + "." + TblChat.MESSAGE_ID + ", " + TblChat.MESSAGE + ", " + TblChat.SESSION_ID
				+ ", MAX(" + TblChat.MSG_DATETIME + ") AS " + TblChat.MSG_DATETIME + " , " + TblUser.USER_NAME 
				+ ", " + TblUser.TABLE_USER + "." + TblUser.USER_ID + ", " + TblChat.TABLE_CHAT + "." + TblChat.USER_ID
				+ " FROM " + TblChat.TABLE_CHAT + " INNER JOIN " + TblUser.TABLE_USER
				+ " ON "  + TblChat.TABLE_CHAT  + "." + TblChat.USER_ID + " = " + TblUser.TABLE_USER + "." + TblUser.USER_ID
				+ " GROUP BY " + TblChat.SESSION_ID;
		return new CursorLoader(getActivity(), DbProvider.PCTITLE_URI, null, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "Load finished");
		mAdapter.swapCursor(cursor);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(getActivity(), Activity_PrivateChat.class);
		i.putExtra(TblChat.KEY_MSG_ID, id);
		startActivity(i);
	}

}
