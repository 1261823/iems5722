package com.iems5722.chatapp.preference;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.R.xml;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	public static final String TAG = "SettingsFragment";
	private String username;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        // Load default values once
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
        updateSummary();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
        
    public void updateSummary() {    
    	Log.d(TAG, "Updating summary");
    
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        String username = prefs.getString(getString(R.string.pref_key_name), "");
        Preference namePref = findPreference(getString(R.string.pref_key_name));
        namePref.setSummary(getString(R.string.pref_name_sum) + " " + username);
       
        String ringtonePath = prefs.getString(getString(R.string.pref_key_ringtone), "");
        Uri ringtoneUri = Uri.parse(ringtonePath);
        Ringtone ringtone = RingtoneManager.getRingtone(this.getActivity(), ringtoneUri);
        String ringtoneName = ringtone.getTitle(this.getActivity());
        Preference ringtonePref = findPreference(getString(R.string.pref_key_ringtone));
        ringtonePref.setSummary(getString(R.string.pref_ringtone_sum) + " " + ringtoneName);
        
        String language = prefs.getString(getString(R.string.pref_key_lang), "");
        Preference langPref = findPreference(getString(R.string.pref_key_lang));
        langPref.setSummary(getString(R.string.pref_lang_sum) + " " + language);
        
        String userId = prefs.getString(getString(R.string.pref_key_userid), getString(R.string.pref_userid_default));
        Preference useridPref = findPreference(getString(R.string.pref_key_userid));
        useridPref.setSummary(getString(R.string.pref_userid_sum) + " " + userId);
    }
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSummary();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}	
	
}