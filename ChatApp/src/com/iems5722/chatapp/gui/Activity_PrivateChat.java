package com.iems5722.chatapp.gui;


import java.net.InetAddress;
import java.net.UnknownHostException;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblUser;
import com.iems5722.chatapp.preference.Settings;

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
import android.view.ViewGroup;
import android.widget.Toast;

public class Activity_PrivateChat extends FragmentActivity implements 
	FragmentChatMenu.OnButtonClickListener,
	LoaderCallbacks<Cursor> {
	public final static String TAG = "Activity_PrivateChat";
	private long dbPUFI;
	private ActionBar actionBar;
	// peer details
	private InetAddress peerIdAddress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getIntent().getExtras();
		if (arguments != null) {
			dbPUFI = arguments.getLong(TblUser.USER_UFI);				
			setContentView(R.layout.frame_chatdetail);
			if (findViewById(R.id.layout_details) != null) {
				if (savedInstanceState != null) {
	                return;
	            }
				//!!!TODO!!! private chat session not done yet!!! using session list as placeholder
				SessionList chatSession = new SessionList();
				FragmentChatMenu chatMenu = new FragmentChatMenu();
				
				chatSession.setArguments(getIntent().getExtras());
				chatMenu.setArguments(getIntent().getExtras());
				
				getSupportFragmentManager().beginTransaction().add(R.id.layout_details, chatSession).commit();
				getSupportFragmentManager().beginTransaction().add(R.id.layout_menu, chatMenu).commit();
				
				getSupportLoaderManager().initLoader(0, arguments, this);
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
		Log.d(TAG, "Creating cursor looking for " + Long.toString(dbPUFI));
		String[] column = {TblUser.USER_UFI, TblUser.USER_ID, TblUser.USER_NAME, TblUser.IP_ADDR_STR};
		String selection = TblUser.USER_UFI + " = ?";
		String[] selectArgs = {Long.toString(dbPUFI)};
		return new CursorLoader(this, DbProvider.USER_URI, column, selection, selectArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d(TAG, "User details loaded");
		if (cursor.moveToFirst()) {
			int colUsername   = cursor.getColumnIndex(TblUser.USER_NAME);
			String username = cursor.getString(colUsername);
			actionBar = getActionBar();
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);	
			actionBar.setTitle(getString(R.string.priv_chat_title) + username);
			//Note: this removes the action bar and preference menu
			//actionBar.hide();
			
			int colPeerIP = cursor.getColumnIndex(TblUser.IP_ADDR_STR);
			Toast.makeText(this, "communicating with "+ cursor.getString(colPeerIP), Toast.LENGTH_SHORT).show();
			/* TODO put this as async task
			try {
				peerIdAddress = InetAddress.getByName(cursor.getString(colPeerIP));
			} catch (UnknownHostException e) {
				Log.e(TAG, "Error getting IP address of peer");
				e.printStackTrace();
			}
			*/
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		//not used
	}
}
