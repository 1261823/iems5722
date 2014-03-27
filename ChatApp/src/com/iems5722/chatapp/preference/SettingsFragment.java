package com.iems5722.chatapp.preference;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.R.xml;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
	private String username;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        Preference namePref = findPreference(Settings.KEY_PREF_NAME);
        namePref.setDefaultValue(username);
        namePref.setSummary(username);
       
    }
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
    
    
   
    
   
    
}