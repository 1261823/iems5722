package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblGlobalChat;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;

public class GlobalChatList extends ListFragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "GlobalChatList";
	private GlobalChatAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(APP_TAG, "onCreate");

		String[] from = new String[] {TblGlobalChat.MESSAGE_ID};
		int[] to = new int[] {R.id.msg_id};
		mAdapter = new GlobalChatAdapter(getActivity(), R.layout.chat_message_sent, null, from, to, 0);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.chat_empty));
	}	
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), DbProvider.GCHAT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	

}
