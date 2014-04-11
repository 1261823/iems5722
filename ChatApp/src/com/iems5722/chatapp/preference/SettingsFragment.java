package com.iems5722.chatapp.preference;

import java.util.Locale;
import com.iems5722.chatapp.R;
import com.iems5722.chatapp.R.xml;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.Log;
import android.util.DisplayMetrics;
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	public static final String TAG = "SettingsFragment";
	
    SharedPreferences prefs;
    
	private String oldUsername;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        // Load default values once
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
        updateSummary();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        oldUsername = prefs.getString(getString(R.string.pref_key_name), "");
	}
        
    public void updateSummary() {    
    	Log.d(TAG, "Updating summary");
   
        String username = prefs.getString(getString(R.string.pref_key_name), "");
        Preference namePref = findPreference(getString(R.string.pref_key_name));
        namePref.setTitle(getString(R.string.pref_name_title));
        namePref.setSummary(getString(R.string.pref_name_sum) + " " + username);
        oldUsername = prefs.getString(getString(R.string.pref_key_name), "");
        
        boolean gmStatus = prefs.getBoolean(getString(R.string.pref_key_gm), true);
        Preference gmPref = findPreference(getString(R.string.pref_key_gm));
        gmPref.setTitle(getString(R.string.pref_gm_title));
        if (gmStatus) {
        	gmPref.setSummary(getString(R.string.pref_gm_sum_udp));
        }
        else {
        	gmPref.setSummary(getString(R.string.pref_gm_sum_mc));
        }
        
        Preference vibratePref = findPreference(getString(R.string.pref_key_vibrate));
        vibratePref.setTitle(getString(R.string.pref_vibrate_title));
        vibratePref.setSummary(getString(R.string.pref_vibrate_sum));
        
        String ringtonePath = prefs.getString(getString(R.string.pref_key_ringtone), "");
        Uri ringtoneUri = Uri.parse(ringtonePath);
        Ringtone ringtone = RingtoneManager.getRingtone(this.getActivity(), ringtoneUri);
        String ringtoneName = ringtone.getTitle(this.getActivity());
        Preference ringtonePref = findPreference(getString(R.string.pref_key_ringtone));
        ringtonePref.setTitle(getString(R.string.pref_ringtone_title));
        ringtonePref.setSummary(getString(R.string.pref_ringtone_sum) + " " + ringtoneName);
        
        String langCode = prefs.getString(getString(R.string.pref_key_lang), "");
        Preference langPref = findPreference(getString(R.string.pref_key_lang));
        ListPreference langPrefList = (ListPreference) langPref;
        langPref.setTitle(getString(R.string.pref_lang_title));
        langPref.setSummary(getString(R.string.pref_lang_sum) + " " + langPrefList.getEntry());
        
        String userId = prefs.getString(getString(R.string.pref_key_userid), getString(R.string.pref_userid_default));
        Preference useridPref = findPreference(getString(R.string.pref_key_userid));
        useridPref.setTitle(getString(R.string.pref_userid_title));
        useridPref.setSummary(getString(R.string.pref_userid_sum) + " " + userId);
    }
	
	public String getUsername() {
		return oldUsername;
	}
	public void setUsername(String username) {
		this.oldUsername = oldUsername;
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		//check that new name is valid
		if (key.equals(getString(R.string.pref_key_name))) {
			String newUsername = prefs.getString(getString(R.string.pref_key_name), "");
			if (newUsername.length() == 0 ) {
				Log.d(TAG, "Using old username " + oldUsername);
				SharedPreferences.Editor prefEditor = prefs.edit();
				prefEditor.putString(key, oldUsername).commit();
			}
		}
		/*
		else if (key.equals(getString(R.string.pref_key_lang))){
			String languageKey = getString(R.string.pref_key_lang);
        	String localString =  prefs.getString(languageKey, "");
        	Log.d(TAG, "LocalString " + localString);
        	languageToLoad = prefs.getString(languageKey, "");
        	postLocaleChange();
        */
        	/*
        	 Resources res = this.getActivity().getResources();
        	    // Change locale settings in the app.
        	    DisplayMetrics dm = res.getDisplayMetrics();
        	    android.content.res.Configuration conf = res.getConfiguration();
        	    conf.locale = new Locale(localString);
        	    res.updateConfiguration(conf, dm);
	    }
        	    */
		updateSummary();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}	
	
}