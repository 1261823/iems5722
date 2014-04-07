package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblGlobalChat;
import com.iems5722.chatapp.database.TblUser;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class PrivateChatList extends ListFragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "PrivateChatList";
	private PrivateChatAdapter mAdapter;
	
	private String sessionId = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "onCreate");
		Bundle arguments = getArguments();
		//only load if session id available
		if (arguments != null) {
			if (arguments.containsKey(TblChat.SESSION_ID)) {
				sessionId = arguments.getString(TblChat.SESSION_ID);
				if (! sessionId.isEmpty()) {
					getLoaderManager().initLoader(0, null, this);
				}
			}
		}
		
		String[] from = new String[] {TblChat.MESSAGE_ID};
		int[] to = new int[] {R.id.msg_id};
		mAdapter = new PrivateChatAdapter(getActivity(), R.layout.chat_message_recv, null, from, to, 0);
		setListAdapter(mAdapter);
	}	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.chat_empty));
		getListView().setDivider(getResources().getDrawable(android.R.color.transparent));
		getListView().setDividerHeight(10);
		getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		getListView().setStackFromBottom(true);		
	}	
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Log.d(TAG, "creating loader");
		String selection = TblChat.SESSION_ID + " = ?";
		String[] selectionArgs = {sessionId};
		String sortOrder = TblChat.MSG_DATETIME;
		return new CursorLoader(getActivity(), DbProvider.PCHAT_URI, null, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "load finished");
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "load reset");
		mAdapter.swapCursor(null);
	}
	
	public void setSessionId(String sessionId) {
		Log.i(TAG, "Setting session id to " + sessionId);
		this.sessionId = sessionId;
		getLoaderManager().restartLoader(0, null, this);
	}

}
