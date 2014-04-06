package com.iems5722.chatapp.gui;


import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblUser;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class UserListActive extends ListFragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "UserListActive";

	private UserListAdapter mAdapter;
	String sortOrder = TblUser.USER_NAME;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "onCreate");
		String[] from = new String[] {TblUser.USER_NAME, TblUser.STATUS, TblUser.USER_DATETIME};
		int[] to = new int[] {R.id.user_name, R.id.user_status};
		mAdapter = new UserListAdapter(getActivity(), R.layout.userlist_detail, null, from, to, 0);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.user_online_none));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		//Log.d(TAG, "onCreateLoader");
		String[] column = {TblUser.USER_UFI, TblUser.USER_NAME, TblUser.STATUS, TblUser.USER_DATETIME};
		String selection = TblUser.STATUS + " = ? " + " AND " + TblUser.USER_ID + " != ? ";
		String[] selectArgs = {"online", Activity_TabHandler.userId};
		return new CursorLoader(getActivity(), DbProvider.USER_URI, column, selection, selectArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		//Log.d(TAG, "onLoadFinished");			
		mAdapter.swapCursor(cursor);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		//Log.d(TAG, "onLoaderReset");
		mAdapter.swapCursor(null);		
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(getActivity(), Activity_PrivateChat.class);
		i.putExtra(TblUser.USER_UFI, id);
		startActivity(i);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		MenuInflater mi = getActivity().getMenuInflater();
		mi.inflate(R.menu.user_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
			case R.id.user_ping:
				//TODO ping one contact
				//placeholder for other functions like iew contact details, block contact etc
				return true;
			case R.id.user_chat:
				//start a chat with user
				Intent i = new Intent(getActivity(), Activity_PrivateChat.class);
				i.putExtra(TblUser.USER_UFI, info.id);
				startActivity(i);
				return true;
		}
		return super.onContextItemSelected(item);
	}	
	
}
