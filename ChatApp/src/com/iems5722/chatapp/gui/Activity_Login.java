package com.iems5722.chatapp.gui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.UserSetInactive;
import com.iems5722.chatapp.network.ServiceNetwork;
import com.iems5722.chatapp.network.ThreadUDPSend;
import com.iems5722.chatapp.network.ServiceNetwork.NetworkBinder;
import com.iems5722.chatapp.preference.Settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
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
	final Context context = this;
	
	public static final String URI_USERNAME = "username";
	private EditText login_username;
	private Button login_button;
	public String username;
	public String prefUsername;
	public String userId;
	public String languageToLoad;
	private Menu menu;
	
	//Preferences
	private SharedPreferences prefs;
	
	@Override
 	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final UncaughtExceptionHandler subclass = Thread.currentThread().getUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		    @Override
		    public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
		    	Log.e(TAG, "uncaughtException", paramThrowable);

		    subclass.uncaughtException(paramThread, paramThrowable);
		    }
		});		
		
		setUpInterface();
	
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		loadPreference();
		prefs.registerOnSharedPreferenceChangeListener(this);
		postLocaleChange();
		
		//reset user status
		UserSetInactive setInactive = new UserSetInactive();
		setInactive.setContext(getApplicationContext());
		setInactive.execute();
		
		
	}
	
	private void setUpInterface() {
		setContentView(R.layout.activity_login);
		login_username = (EditText)findViewById(R.id.login_username);
		login_button = (Button)findViewById(R.id.login_btn_enter);		
		//respond to controls
		initClickHandler();	
		
		if (menu != null) {
			menu.clear();
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.setting_menu, menu);
		}
	}
	
	private void loadPreference() {
		Log.d(TAG, "loadPreference");
		
		String usernameKey = getString(R.string.pref_key_name);
		prefUsername = prefs.getString(usernameKey, "");
		if (!prefUsername.equals("")) {
			login_username.setText(prefUsername);
			
		}
		
		String userIdKey = getString(R.string.pref_key_userid);
		userId = prefs.getString(userIdKey, "");
		//if user id is null then generate a new one
		if (userId.equals("") || userId.equals(getString(R.string.pref_userid_default))) {
			userId = generateUserId();
			SharedPreferences.Editor prefEditor = prefs.edit();
			prefEditor.putString(userIdKey, userId).commit();
		}
		else {
			Log.d(TAG, "Existing User Id " + userId);
		}		
		
		
		String languageKey = getString(R.string.pref_key_lang);
		languageToLoad = prefs.getString(languageKey, "");
	}
	
	private void updatePreference() {
		//update preferences based on name input
		Log.d(TAG, "updatePreference");
		SharedPreferences.Editor prefEditor = prefs.edit();
		
		String usernameKey = getString(R.string.pref_key_name);
		username = login_username.getText().toString();
		if (!username.equals("")) {
			prefEditor.putString(usernameKey, username).commit();
		}
	}

	private String generateUserId() {
		String deviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		byte[] macAddrByte = deviceId.getBytes();
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
					if (username.length() > 0 ) {
						updatePreference();
						//Log.d(TAG, "Entering chat");
						Intent intent = new Intent(Activity_Login.this, Activity_TabHandler.class);
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
		this.menu = menu;
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
		//stop all services on app exit
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		loadPreference();
		if (key.equals(this.getString(R.string.pref_key_lang))) {
			languageToLoad = prefs.getString(key, "");
			postLocaleChange();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "New config");
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		setUpInterface();
		loadPreference();
	}
	
	public void postLocaleChange() {
    	Configuration newConfig = new Configuration();
        newConfig.locale = new Locale(languageToLoad);
        onConfigurationChanged(newConfig);		
	}
}
