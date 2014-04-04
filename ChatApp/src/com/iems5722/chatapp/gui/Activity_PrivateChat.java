package com.iems5722.chatapp.gui;


import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblUser;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class Activity_PrivateChat extends FragmentActivity implements 
	FragmentChatMenu.OnButtonClickListener,
	LoaderCallbacks<Cursor> {
	public final static String TAG = "Activity_PrivateChat";
	private ActionBar actionBar;

	
	//loader types
	final static int LOAD_USER = 0;
	final static int LOAD_SESSION = 1;
	Loader<Cursor> LOADER_USER;
	Loader<Cursor> LOADER_SERSSION;
	
	// peer details
	private String peerIdAddress;
	//user identifer
	private long dbPUFI = -1;
	private String dbPID = "";
	//session id
	private String dbSessionId = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getIntent().getExtras();
		if (arguments != null) {
			if (getIntent().hasExtra(TblUser.USER_UFI)) {
				dbPUFI = arguments.getLong(TblUser.USER_UFI);
			}
			if (getIntent().hasExtra(TblChat.SESSION_ID)) {
				dbSessionId = arguments.getString(TblChat.SESSION_ID);
			}
			// test at least one id value is available
			if (dbPUFI == -1 && dbSessionId.isEmpty()) {
				Log.e(TAG, "Entered private chat with no id");
			}
			setContentView(R.layout.frame_chatdetail);
			if (findViewById(R.id.layout_details) != null) {
				if (savedInstanceState != null) {
	                return;
	            }
				// Need to give session id into private chat list to load history
				// two entry points to this activity
				// 1. by tapping on user's name. Has USER UFI, need to find USER ID
				// 2. by clicking on an existing chat session. Has SESSION ID --> USER ID
				// 
				
				if (dbPUFI != -1) {
					// handle scenario 1 where only user ufi is known
					Log.d(TAG, "No session id");
					getSupportLoaderManager().initLoader(LOAD_USER, arguments, this);
				} else {
					// handle scenario 2 where session id and user id is known
					dbPID = dbSessionId;
					arguments.putString(TblChat.SESSION_ID, dbSessionId);
				}
				

				//!!!TODO!!! private chat session not done yet!!! using session list as placeholder
				//fragments in activity
				PrivateChatList chatSession = new PrivateChatList(); 
				FragmentChatMenu chatMenu = new FragmentChatMenu();
				chatSession.setArguments(arguments);
				
				getSupportFragmentManager().beginTransaction().add(R.id.layout_details, chatSession).commit();
				getSupportFragmentManager().beginTransaction().add(R.id.layout_menu, chatMenu).commit();
								
				
			}
		}
		else {
			Log.e(TAG, "Entered private chat without peer to chat with");
		}
	}
	
	@Override
	public void buttonClick(int buttonId) {
		switch(buttonId) {
		case(R.id.menu_chat_send):
			//TODO Send message to recipient
			Toast.makeText(this, "Sent private message to " + peerIdAddress, Toast.LENGTH_SHORT).show();
			break;
		default:
			throw new IllegalArgumentException("Unknown button clicked " + Integer.toString(buttonId));
		}			
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Log.d(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.private_chat, menu);
		return true;
	}	

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.chat_attachment:
			//show attachment picker
			Intent iAttachmentPicker = new Intent(this, DialogAttachmentPicker.class);
			iAttachmentPicker.addCategory(Intent.CATEGORY_LAUNCHER);
			iAttachmentPicker.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			iAttachmentPicker.addFlags(Intent.FLAG_FROM_BACKGROUND);
			//not sure if session information needs to be passed to intent
			startActivity(iAttachmentPicker);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		dbPUFI = bundle.getLong(TblUser.USER_UFI);	
		Log.d(TAG, "Creating cursor looking for user " + Long.toString(dbPUFI));
		String[] column = {TblUser.USER_UFI, TblUser.USER_ID, TblUser.USER_NAME, TblUser.IP_ADDR_STR};;
		String selection = TblUser.USER_UFI + " = ?";
		String[] selectArgs = {Long.toString(dbPUFI)};
		LOADER_USER = new CursorLoader(this, DbProvider.USER_URI, column, selection, selectArgs, null);
		return LOADER_USER;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "User details loaded");
		if (cursor.moveToFirst()) {
			int colUsername   = cursor.getColumnIndex(TblUser.USER_NAME);
			String username = cursor.getString(colUsername);
			actionBar = getActionBar();
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.android_blue));
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);	
			actionBar.setTitle(getString(R.string.priv_chat_title) + " " + username);
			//Note: this removes the action bar and preference menu
			//actionBar.hide();
			
			int colIPAddress = cursor.getColumnIndex(TblUser.IP_ADDR_STR);
			peerIdAddress = cursor.getString(colIPAddress);
			Toast.makeText(this, "communicating with "+ peerIdAddress, Toast.LENGTH_SHORT).show();
			
			int colUserId = cursor.getColumnIndex(TblUser.USER_ID);
			dbPID = cursor.getString(colUserId);
			dbSessionId = dbPID;
			Toast.makeText(this, "Session Id "+ dbSessionId, Toast.LENGTH_SHORT).show();
			
			//update fragment
			PrivateChatList chatSession = (PrivateChatList) getSupportFragmentManager().findFragmentById(R.id.layout_details);
			chatSession.setSessionId(dbSessionId);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		//not used
	}

	
}
