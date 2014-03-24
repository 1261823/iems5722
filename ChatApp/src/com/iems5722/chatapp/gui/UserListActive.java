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

public class UserListActive extends ListFragment implements LoaderCallbacks<Cursor> {
	private static final String TAG = "UserListActive";

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
		SQLiteDatabase db = DbProvider.database.getReadableDatabase();
		String[] column = {TblUser.USER_UFI, TblUser.USER_NAME, TblUser.STATUS};
		String selection = TblUser.STATUS + " = ?";
		String[] selectArgs = {"online"};
		Cursor cursor = db.query(TblUser.TABLE_USER, column, selection, selectArgs, null, null, sortOrder);
		
		mAdapter = new UserListAdapter(getActivity(), R.layout.userlist_detail, cursor, from, to, 0);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getResources().getString(R.string.user_online_none));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Log.d(TAG, "onCreateLoader");
		return new CursorLoader(getActivity(), DbProvider.USER_URI, null, null, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished");			
		mAdapter.swapCursor(cursor);		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset");
		mAdapter.swapCursor(null);		
	}
}
