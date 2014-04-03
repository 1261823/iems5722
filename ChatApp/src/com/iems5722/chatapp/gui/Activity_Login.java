package com.iems5722.chatapp.gui;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.network.ServiceNetwork;
import com.iems5722.chatapp.network.ServiceNetwork.NetworkBinder;
import com.iems5722.chatapp.preference.Settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class Activity_Login extends FragmentActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = "Activity_Login";
	
	public static final String URI_USERNAME = "username";
	private EditText login_username;
	private Button login_button;
	public String username;
	public String prefUsername;
	public String userId;
	
	//Preferences
	private SharedPreferences prefs;

	//Networking service
	ServiceNetwork NetworkService;
	private Intent NetServiceIntent;	
	Handler ServiceHandler;
	
	//events recognised by handler
	public static final int SERV_READY = 0;
	public static final int WIFI_INACTIVE = 1;
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Log.d(TAG, "received message");
        	switch (msg.what) {
        	case (WIFI_INACTIVE):
        		//inform user that wifi inactive
        		DialogFragment wifidialog = new DialogWifiAvailable();
        		wifidialog.show(getSupportFragmentManager(), DialogWifiAvailable.TAG);
        		break;
    		}
        }
	};

	@Override
 	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		login_username = (EditText)findViewById(R.id.login_username);
		login_button = (Button)findViewById(R.id.login_btn_enter);
		//start network services
		NetServiceIntent = new Intent(this, ServiceNetwork.class);
		initNetworkServices();
		//respond to controls
		initClickHandler();		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		loadPreference();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	private void loadPreference() {
		Log.d(TAG, "loadPreference");
		String usernameKey = getString(R.string.pref_key_name);
		prefUsername = prefs.getString(usernameKey, "");
		if (!prefUsername.equals("")) {
			login_username.setText(prefUsername);
		}	
	}
	
	private void updatePreference() {
		Log.d(TAG, "updatePreference");
		SharedPreferences.Editor prefEditor = prefs.edit();
		
		String usernameKey = getString(R.string.pref_key_name);
		username = login_username.getText().toString();
		if (!username.equals("")) {
			prefEditor.putString(usernameKey, username).commit();
		}
		
		String userIdKey = getString(R.string.pref_key_userid);
		userId = prefs.getString(userIdKey, "");
		//if user id is null then generate a new one
		if (userId.equals("") || userId.equals(getString(R.string.pref_userid_default))) {
			userId = generateUserId();
			prefEditor.putString(userIdKey, userId).commit();
		}
		else {
			Log.d(TAG, "Existing User Id " + userId);
		}
	}

	private String generateUserId() {
		String MACAddress = NetworkService.getMACAddress();
		byte[] macAddrByte = MACAddress.getBytes();
		//Log.i(TAG, "MACByte " + macAddrByte.toString());
		
		SecureRandom sr = new SecureRandom();
		byte[] randomByte = new byte[1024];
		sr.nextBytes(randomByte);
		//Log.i(TAG, "PRN " + randomByte.toString());
				
		byte[] predigest = new byte[macAddrByte.length + randomByte.length];
		System.arraycopy(randomByte, 0, predigest, 0, randomByte.length);
		System.arraycopy(macAddrByte, 0, predigest, randomByte.length, macAddrByte.length);
		//Log.i(TAG, "Prepend " + predigest.toString());
		
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			digester.update(predigest);
			byte messageDigest[] = digester.digest();
			
			StringBuilder hexString = new StringBuilder();
	        for (byte aMessageDigest : messageDigest) {
	            String h = Integer.toHexString(0xFF & aMessageDigest);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        
	        Log.i(TAG, "MD5 " + hexString.toString());
	        return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	private void initClickHandler() {
		login_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// check if username has been entered
				username = login_username.getText().toString();
				//Log.d(TAG, username);
				//Log.d(TAG, Integer.toString(username.length()));
				try {
					//test if network has been initialised
					String MACAddress = NetworkService.getMACAddress();
					if (username.length() > 0 ) {
						//Log.d(TAG, "Entering chat");
						updatePreference();
						Intent intent = new Intent(getApplicationContext(), Activity_TabHandler.class);
						intent.putExtra(URI_USERNAME, username);
						startActivity(intent);
					}
					else {
						//TODO ask user to enter a valid username
					}
				}
				catch (Exception e) {
					//network not yet initialised
					//ask user to wait and retry a bit later
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
		stopService(NetServiceIntent);
		unbindService(NetServiceConnection);
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void initNetworkServices() {
		startService(NetServiceIntent);
		bindService(NetServiceIntent, NetServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection NetServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "onServiceConnected");	
			NetworkBinder networkBinder = (NetworkBinder) binder;
			NetworkService = networkBinder.getService();
			NetworkService.setUIHandler(mHandler);
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected");				
		}
	};

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		loadPreference();
	}
}
