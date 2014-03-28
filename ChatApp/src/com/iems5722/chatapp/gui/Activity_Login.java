package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.network.ServiceNetwork;
import com.iems5722.chatapp.network.ServiceNetwork.NetworkBinder;
import com.iems5722.chatapp.network.ServiceUDP_Recv;
import com.iems5722.chatapp.network.ServiceUDP_Send;
import com.iems5722.chatapp.preference.Settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class Activity_Login extends Activity {
	private static final String TAG = "Activity_Login";
	
	public static final String URI_USERNAME = "username";
	private EditText login_username;
	private Button login_button;
	public String username;
	
	//network service
	ServiceNetwork networkService;
	private Intent networkIntent;
	Handler networkHandler;
	
	//udp service
	ServiceUDP_Recv UDPRecv;
	private Intent UDPListenerIntent;
	ServiceUDP_Send UDPSend;
	private Intent UDPSenderIntent;
	
	//events recognised by handler
	public static final int NTWK_WIFI_AVAIL = 1;

	@Override
 	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		login_username = (EditText)findViewById(R.id.login_username);
		login_button = (Button)findViewById(R.id.login_btn_enter);
		
		initClickHandler();
		//start network services
		networkIntent = new Intent(this, ServiceNetwork.class);
		UDPListenerIntent = new Intent(this, ServiceUDP_Recv.class);
		UDPSenderIntent = new Intent(this, ServiceUDP_Send.class);
		initNetworkServices();
	}
	
	private void initClickHandler() {
		login_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// check if username has been entered
				username = login_username.getText().toString();
				//Log.d(TAG, username);
				//Log.d(TAG, Integer.toString(username.length()));
				if (username.length() > 0 ) {
					//Log.d(TAG, "Entering chat");
					Intent intent = new Intent(getApplicationContext(), Activity_TabHandler.class);
					intent.putExtra(URI_USERNAME, username);
					startActivity(intent);
				}
				else {
					//TODO ask user to enter a valid username
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting_menu, menu);
		return true;
	}	
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_pref:
			Intent iMenuPreference = new Intent(this, Settings.class);
			iMenuPreference.putExtra(URI_USERNAME, username);
			startActivity(iMenuPreference);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//stop all services on app exit
		stopService(networkIntent);
		unbindService(mConnection);
		stopService(UDPListenerIntent);
		try {
			stopService(UDPSenderIntent);
		}
		catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
	
	private void initNetworkServices() {
		startService(networkIntent);
		bindService(networkIntent, mConnection, Context.BIND_AUTO_CREATE);
		startService(UDPListenerIntent);
		startService(UDPSenderIntent);		
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "onServiceConnected");	
			NetworkBinder networkBinder = (NetworkBinder) binder;
			networkService = networkBinder.getService();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected");				
		}
	};	
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {		
        	Log.d(TAG, "UIThread recv handler msg");
        	switch (msg.what) {
        		case NTWK_WIFI_AVAIL:
        			//note this does not work
        			Log.d(TAG, "NTWK_WIFI_AVAIL");
    				Intent wifiIntent = new Intent (getBaseContext(), DialogWifiAvailable.class);
    				wifiIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    				wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				wifiIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
    				getApplicationContext().startActivity(wifiIntent);        			
        		break;
        	}	
        }
	};
}
