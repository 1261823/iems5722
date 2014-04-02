package com.iems5722.chatapp.preference;

import com.iems5722.chatapp.R;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener{
    private SettingsFragment prefFrag;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //initialize name
        prefFrag = new SettingsFragment();        		
        		
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