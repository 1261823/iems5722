package com.iems5722.chatapp.gui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import com.iems5722.chatapp.database.UserSetInactive;
import com.iems5722.chatapp.network.MulticastReceiver;
import com.iems5722.chatapp.network.MulticastService;
import com.iems5722.chatapp.network.MulticastService.MulticastServiceBinder;
import com.iems5722.chatapp.network.PeerFileService;
import com.iems5722.chatapp.network.PeerFileService.PeerFileServiceBinder;
import com.iems5722.chatapp.network.ServiceNetwork;
import com.iems5722.chatapp.network.ServiceNetwork.NetworkBinder;
import com.iems5722.chatapp.network.ThreadUDPSend;
import com.iems5722.chatapp.preference.Settings;


public class Activity_TabHandler extends FragmentActivity implements
	FragmentChatMenu.OnButtonClickListener,
	OnSharedPreferenceChangeListener {
	private static final String TAG = "Activity_TabHandler";

	//Preferences
	private SharedPreferences prefs;	
	private String msgUsername;
	
	private ViewPager mViewPager;
	private SlidePagerAdapter mPagerAdapter;
	
	FragmentGlobalChat globalChat;
	UserList userList;
	SessionList privateChat;
	
	//Networking service
	ServiceNetwork networkService;
	private Intent netServiceIntent;
	Handler serviceHandler;
	
	//Peer File Transfer service
	PeerFileService peerFileService;
	private Intent peerFileServiceIntent;
	private Handler peerFileServiceHandler;
	
	//Peer File Transfer service
	MulticastService multicastService;
	private Intent multicastServiceIntent;
	private Handler multicastServiceHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "onCreate");
		globalChat  = new FragmentGlobalChat();
		userList    = new UserList();
		privateChat = new SessionList();
		
		//create connections to service
		netServiceIntent = new Intent(this, ServiceNetwork.class);
		bindService(netServiceIntent, netServiceConnection, Context.BIND_AUTO_CREATE);
		
		//create Peer File Transfer service
		peerFileServiceIntent = new Intent(this, PeerFileService.class);
		startService(peerFileServiceIntent);
		bindService(peerFileServiceIntent, peerFileServiceConnection, Context.BIND_AUTO_CREATE);
		
		//create Multicast Message service
		multicastServiceIntent = new Intent(this, MulticastService.class);
		startService(multicastServiceIntent);
		bindService(multicastServiceIntent, multicastServiceConnection, Context.BIND_AUTO_CREATE);
				
		
		setContentView(R.layout.activity_main);

		mViewPager = (ViewPager)findViewById(R.id.project_pager);
		mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);		
		
		//get name from preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);		
		
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//final ActionBar actionBar = getActionBar();
		//actionBar.setDisplayShowTitleEnabled(false);
		//actionBar.setDisplayShowHomeEnabled(false);	
		//Note: this removes the action bar and preference menu
		//actionBar.hide();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//ping for users if possible
		pingAllUsers();
	}
	
	private class SlidePagerAdapter extends FragmentPagerAdapter {
		SparseArray<Fragment> registeredFragment = new SparseArray<Fragment>();
		public SlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = new Fragment();
			switch (position) {
			case 0:
				return fragment = globalChat;
			case 1:
				return fragment = userList;
			case 2:
				return fragment = privateChat;
			}
			return fragment;
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
		public Object instantiateItem(ViewGroup container, int position) {
			//Log.d(TAG, "instantiateItem");
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			registeredFragment.put(position, fragment);
			return fragment;
		}
		
		public Fragment getRegisteredFragment(int position) {
			return registeredFragment.get(position);
		}
	}

	@Override
	public void buttonClick(int buttonId) {
		switch(buttonId) {
		case(R.id.menu_chat_send):
			
			//Send message to global chat
			EditText chatText = (EditText)this.findViewById(R.id.menu_chat_input);
		
			multicastServiceHandler.obtainMessage(MulticastService.SEND_MSG, chatText.getText().toString()).sendToTarget();
			 
			Toast.makeText(getApplicationContext(), "Global message sent clicked", Toast.LENGTH_SHORT).show();
			//networkService.networkHandler.obtainMessage(ThreadNetwork.NTWK_UPDATE).sendToTarget();
			//networkService.udpSendHandler.obtainMessage(ThreadUDPSend.PING_REQUEST_ALL).sendToTarget();
			
			chatText.setText("");
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
			initPreference();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected");				
		}
	};
	
	private ServiceConnection peerFileServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "onPeerFileServiceConnected");	
			PeerFileServiceBinder peerFileServiceBinder = (PeerFileServiceBinder) binder;
			peerFileService = peerFileServiceBinder.getService();
			peerFileServiceHandler = peerFileService.getServiceHandler();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onPeerFileServiceDisconnected");				
		}
	};
	
	private ServiceConnection multicastServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "onMulticastConnected");	
			MulticastServiceBinder multicastServiceBinder = (MulticastServiceBinder) binder;
			multicastService = multicastServiceBinder.getService();
			multicastServiceHandler = multicastService.getServiceHandler();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onMulticastDisconnected");				
		}
	};

	private void initPreference() {
		//Log.d(TAG, "initPreference");
		String usernameKey = this.getString(R.string.pref_key_name);	
		readUsername(usernameKey);
	}	
	
	@Override
	public void onDestroy() {
		//inform other users 
		networkService.udpSendHandler.obtainMessage(ThreadUDPSend.SIGN_OUT).sendToTarget();
		
		unbindService(netServiceConnection);
		unbindService(peerFileServiceConnection);
		unbindService(multicastServiceConnection);
		
		stopService(peerFileServiceIntent);
		stopService(multicastServiceIntent);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(this.getString(R.string.pref_key_name))) {
			readUsername(key);
		}
	}
	
	private void readUsername(String key) {
		msgUsername = prefs.getString(key, "");
		networkService.setUsername(msgUsername);
		pingAllUsers();
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
}