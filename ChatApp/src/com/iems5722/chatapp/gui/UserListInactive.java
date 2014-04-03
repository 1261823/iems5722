package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
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

public class UserListInactive extends ListFragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "UserListInactive";

	private long mRowId;	
	private UserListAdapter mAdapter;
	String sortOrder = TblUser.USER_NAME;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		Bundle arguments = getArguments();
		if (arguments != null) {
			mRowId = arguments.getLong(TblUser.USER_UFI);
		}
		String[] from = new String[] {TblUser.USER_NAME, TblUser.STATUS};
		int[] to = new int[] {R.id.user_name, R.id.user_status};
		mAdapter = new UserListAdapter(getActivity(), R.layout.userlist_detail, null, from, to, 0);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);	
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.user_offline_none));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Log.d(TAG, "onCreateLoader");		
		String[] column = {TblUser.USER_UFI, TblUser.USER_NAME, TblUser.STATUS, TblUser.USER_DATETIME};
		String selection = TblUser.STATUS + " = ?";
		String[] selectArgs = {"offline"};		
		return new CursorLoader(getActivity(), DbProvider.USER_URI, column, selection, selectArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished");
		mAdapter.swapCursor(cursor);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d(TAG, "onLoaderReset");
		mAdapter.swapCursor(null);		
	}
}
