package com.iems5722.chatapp;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener{
    private SettingsFragment prefFrag;
    public static final String KEY_PREF_NAME = "pref_name";
    private String username;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        
        if(extras != null) {
        	username = extras.getString(Activity_Login.URI_USERNAME);
        }
        
        //initialize name
        prefFrag = new SettingsFragment();
        prefFrag.setUsername(username);
        		
        		
       // Display the fragment as the main content.
        FragmentManager fragmananger = getFragmentManager();
        
        fragmananger.beginTransaction()
                .replace(android.R.id.content,prefFrag)
                .commit();
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.edit().putString(KEY_PREF_NAME, username);
        
    }
    
	 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	        if (key.equals(KEY_PREF_NAME)) {
	            Preference namePref = prefFrag.findPreference(key);
	            namePref.setSummary(sharedPreferences.getString(key, ""));
	        }
	}
    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
    
}