package com.iems5722.chatapp.preference;

import java.util.Locale;

import com.iems5722.chatapp.R;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	private final static String TAG = "Settings";
    private SettingsFragment prefFrag;
	public String languageToLoad;
	
	//Preferences
	private SharedPreferences prefs;	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //initialize name
        prefFrag = new SettingsFragment();        		
		//get name from preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);	
		loadPreference();
		setUpInterface();
    }
	
	private void loadPreference() {
		Log.d(TAG, "loadPreference");
		String languageKey = getString(R.string.pref_key_lang);
		languageToLoad = prefs.getString(languageKey, "");
	}
	
	private void setUpInterface() {
	       // Display the fragment as the main content.
        FragmentManager fragmananger = getFragmentManager();
        fragmananger.beginTransaction()
                .replace(android.R.id.content,prefFrag)
                .commit();		
	}
    
	 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	        if (key.equals(getString(R.string.pref_key_name))) {
	            Preference namePref = prefFrag.findPreference(key);
	            namePref.setSummary(sharedPreferences.getString(key, ""));
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
    
    @Override
    protected void onDestroy(){
    	prefs.unregisterOnSharedPreferenceChangeListener(this);
    	super.onDestroy();
    }
    
}