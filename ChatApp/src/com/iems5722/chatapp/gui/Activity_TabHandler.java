package com.iems5722.chatapp.gui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
import java.util.Locale;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblUser;
import com.iems5722.chatapp.database.UserSetInactive;

import com.iems5722.chatapp.network.MessageBuilder;
import com.iems5722.chatapp.network.ServiceNetwork;
import com.iems5722.chatapp.network.ServiceNetwork.NetworkBinder;
import com.iems5722.chatapp.network.ThreadUDPSend;
import com.iems5722.chatapp.preference.Settings;


public class Activity_TabHandler extends FragmentActivity implements
	FragmentChatMenu.OnButtonClickListener,
	OnSharedPreferenceChangeListener {
	private static final String TAG = "Activity_TabHandler";
	
	MessageBuilder msgBuilder;
	
	//Preferences
	private SharedPreferences prefs;	
	static String msgUsername;
	static String userId;
	static boolean sendGlobalMsg_UDP;
	
	//view objects
	private ViewPager mViewPager;
	private SlidePagerAdapter mPagerAdapter;
	static final int ITEMS = 3;
	private Menu menu;
	
	FragmentGlobalChat globalChat;
	UserList userList;
	SessionList privateChat;
	String languageToLoad;
	
	//Networking service
	ServiceNetwork networkService;
	private Intent netServiceIntent;
	Handler serviceHandler;

	//events recognised by handler
	public static final int SERV_READY = 0;
	public static final int WIFI_INACTIVE = 1;
	public static final int DEREGISTER_WIFI_BCR = 2;	
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Log.d(TAG, "received message");
        	switch (msg.what) {
        	case (WIFI_INACTIVE):
        		createWifiDialog();
    			break;
        	case (DEREGISTER_WIFI_BCR):
        		serviceHandler.obtainMessage(DEREGISTER_WIFI_BCR).sendToTarget();;
        		break;
        	}	
        }
	};
	
	public void createWifiDialog() {
		//inform user that wifi inactive
		Intent iWifiDialog = new Intent(this, DialogWifiAvailable.class);
    	iWifiDialog.addCategory(Intent.CATEGORY_LAUNCHER);
    	iWifiDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	iWifiDialog.addFlags(Intent.FLAG_FROM_BACKGROUND);
		//not sure if session information needs to be passed to intent
		startActivity(iWifiDialog);
		DialogWifiAvailable.setHandler(mHandler);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Calendar c = Calendar.getInstance();
		long diff;
		long start = c.getTimeInMillis();     
		
		//get name from preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);		
		initPreference();
		msgBuilder = new MessageBuilder(this);
		
		long prefdone = c.getTimeInMillis();
		diff = prefdone - start;
		Log.i(TAG, "prefloadtime " + Long.toString(diff));
		
		
		final UncaughtExceptionHandler subclass = Thread.currentThread().getUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		    @Override
		    public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
		    	Log.getStackTraceString(paramThrowable);
		    	Log.e(TAG, "uncaughtException", paramThrowable);
		    subclass.uncaughtException(paramThread, paramThrowable);
		    }
		});
		
		//Log.d(TAG, "onCreate");
		globalChat  = new FragmentGlobalChat();
		userList    = new UserList();
		privateChat = new SessionList();
		
		long fragdone = c.getTimeInMillis();
		diff = fragdone - prefdone;
		Log.i(TAG, "fragloadtime " + Long.toString(diff));		
		
		//create connections to service
		netServiceIntent = new Intent(this, ServiceNetwork.class);
		startService(netServiceIntent);
		bindService(netServiceIntent, netServiceConnection, Context.BIND_AUTO_CREATE);
		
		long servdone = c.getTimeInMillis();
		diff = servdone - fragdone;
		Log.i(TAG, "servloadtime " + Long.toString(diff));			
		
		long servdone2 = c.getTimeInMillis();
		diff = servdone2 - servdone;
		Log.i(TAG, "servloadtime2 " + Long.toString(diff));			

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//final ActionBar actionBar = getActionBar();
		//actionBar.setDisplayShowTitleEnabled(false);
		//actionBar.setDisplayShowHomeEnabled(false);	
		//Note: this removes the action bar and preference menu
		//actionBar.hide();
		setUpInterface();
		
		long intdone = c.getTimeInMillis();
		diff = intdone - servdone2;
		Log.i(TAG, "intdone " + Long.toString(diff));	
	}
	
	public void setUpInterface() {
		Log.i(TAG, "Setting up interface");
		setContentView(R.layout.activity_main);
		mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager)findViewById(R.id.project_pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);

		if (menu != null) {
			menu.clear();
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.tab_menu, menu);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//ping for users if possible
		pingAllUsers();
	}
	
	private class SlidePagerAdapter extends FragmentStatePagerAdapter  {
		SparseArray<Fragment> registeredFragment = new SparseArray<Fragment>();
		public SlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
		public Fragment getItem(int position) {
			//Fragment fragment = new Fragment();
			switch (position) {
			case 0:
				return FragmentGlobalChat.init();
			case 1:
				return UserList.init();
			case 2:
				return SessionList.init();
			default:
				return FragmentGlobalChat.init();
			}
			//return fragment;
		}
		
		@Override
		public int getItemPosition(Object object) {
			Log.d(TAG, "getItemPosition");
		    return PagerAdapter.POSITION_NONE;
		}
		
		@Override
		public int getCount() {
			return 3;
		}
		
		@Override 
		public CharSequence getPageTitle(int position) {
			switch(position) {
			case 0:
				return getString(R.string.tab_globalchat);
			case 1:
				return getString(R.string.tab_users);
			case 2:
				return getString(R.string.tab_privatechat);
			}
			return null;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d(TAG, "DestroyItem on " + Integer.toString(position));
			super.destroyItem(container, position, object);
		}
	}

	@Override
	public void buttonClick(int buttonId) {
		switch(buttonId) {
		case(R.id.menu_chat_send):			
			//Send message to global chat
			EditText chatText = (EditText)this.findViewById(R.id.menu_chat_input);
			String outputString = chatText.getText().toString().trim();
			if (outputString.length() > 0) {
				String outMessage = msgBuilder.messageCreate(MessageBuilder.GLOBAL_MSG, outputString);
				if (sendGlobalMsg_UDP)	{
					Log.d(TAG, "Sending messages via UDP");
					serviceHandler.obtainMessage(ServiceNetwork.UDP_SEND, outMessage).sendToTarget();
				}
				else {
					Log.d(TAG, "Sending messages via multicast");
					serviceHandler.obtainMessage(ServiceNetwork.MC_SEND, outMessage).sendToTarget();
				}
				
				msgBuilder.saveGlobalMessage(outMessage);
				//Toast.makeText(getApplicationContext(), "Global message sent clicked", Toast.LENGTH_SHORT).show();
				//networkService.networkHandler.obtainMessage(ThreadNetwork.NTWK_UPDATE).sendToTarget();
				//networkService.udpSendHandler.obtainMessage(ThreadUDPSend.PING_REQUEST_ALL).sendToTarget();
				chatText.setText("");
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown button clicked " + Integer.toString(buttonId));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Log.d(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tab_menu, menu);
		return true;
	}	
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_ping:
			//check for new users
			pingAllUsers();
			Toast.makeText(this, R.string.menu_ping_toast, Toast.LENGTH_SHORT).show();
			return true;		
		case R.id.menu_pref:
			//go to preferences menu
			Intent iMenuPreference = new Intent(this, Settings.class);
			startActivity(iMenuPreference);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}	

	private ServiceConnection netServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "onServiceConnected");	
			NetworkBinder networkBinder = (NetworkBinder) binder;
			networkService = networkBinder.getService();
			serviceHandler = networkService.getServiceHandler();
			postLocaleChange();
			networkService.setUsername(msgUsername);
			networkService.setUserid(userId);
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected");				
		}
	};

	private void initPreference() {
		//Log.d(TAG, "initPreference");
		String usernameKey = this.getString(R.string.pref_key_name);	
		msgUsername = prefs.getString(usernameKey, "");
		String useridKey = this.getString(R.string.pref_key_userid);
		userId = prefs.getString(useridKey, "");
		String languageKey = this.getString(R.string.pref_key_lang);
		languageToLoad = prefs.getString(languageKey, "");
		//true = udp
		String sendGlobalKey = this.getString(R.string.pref_key_gm);
		sendGlobalMsg_UDP = prefs.getBoolean(sendGlobalKey, true);
		updateDatabase();
	}	
	
	@Override
	public void onDestroy() {
		//inform other users 
		networkService.udpSendHandler.obtainMessage(ThreadUDPSend.SIGN_OUT).sendToTarget();
		unbindService(netServiceConnection);
		stopService(netServiceIntent);
		
		//unbindService(multicastServiceConnection);
		//stopService(multicastServiceIntent);

		super.onDestroy();
	}
	
	private void pingAllUsers() {
		//set all users to offline then wait for response to confirm user still online
		UserSetInactive setInactive = new UserSetInactive();
		setInactive.setContext(getApplicationContext());
		setInactive.execute();
		//check for new users
		if (networkService != null && !msgUsername.isEmpty()) {
			Log.d(TAG, "Pinging with username: " + msgUsername);
			networkService.udpPingAll();
		}
	}
	
	private void updateDatabase() {
		Log.i(TAG, "Updating user details");
		if (userId != null) {
			Calendar c = Calendar.getInstance();
			long curDateTimeMS = c.getTimeInMillis();     	
	    	    	
			ContentValues values = new ContentValues();
			int mRowId = 0;
			values.put(TblUser.USER_ID, userId);
			values.put(TblUser.USER_NAME, msgUsername);
			values.put(TblUser.IP_ADDR_INT, 0);
			values.put(TblUser.IP_ADDR_STR, "0");
			values.put(TblUser.STATUS, TblUser.STAT_ON);
			values.put(TblUser.USER_DATETIME, curDateTimeMS);
			
			//update user with new information
			
			Log.d(TAG, "User MD5 is " + userId);
			String selection = TblUser.USER_ID + " =? ";
			String[] selArgs = {userId};
			int count = this.getApplicationContext().getContentResolver().update(
						DbProvider.USER_URI, values, selection, selArgs);
			Log.d(TAG, "User update result " + count);
			if (count != 1) {
				Log.d(TAG, "Inserting user " + msgUsername);
				Uri itemUri = this.getApplicationContext().getContentResolver().insert(DbProvider.USER_URI, values);
			}
			else {
				Log.d(TAG, "Updating user " + msgUsername);
			}
		}
		else {
			Log.d(TAG, "User details not saved");
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(this.getString(R.string.pref_key_name))) {
			msgUsername = prefs.getString(key, "");
			updateDatabase();
			networkService.setUsername(msgUsername);
		}
		else if (key.equals(this.getString(R.string.pref_key_lang))) {
			languageToLoad = prefs.getString(key, "");
			postLocaleChange();
		}
		else if (key.equals(this.getString(R.string.pref_key_gm))) {
			sendGlobalMsg_UDP = prefs.getBoolean(key, true);
		}
	}	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "New config");
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		setUpInterface();
	}
	
	public void postLocaleChange() {
		Configuration newConfig = new Configuration();
	    newConfig.locale = new Locale(languageToLoad);
	    onConfigurationChanged(newConfig);		
	}	
}
