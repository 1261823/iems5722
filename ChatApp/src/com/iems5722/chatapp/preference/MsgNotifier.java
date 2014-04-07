package com.iems5722.chatapp.preference;

import com.iems5722.chatapp.R;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class MsgNotifier implements OnSharedPreferenceChangeListener{
	public final static String TAG = "MsgNotifier";
	private Context mContext;
	
	private SharedPreferences prefs;	
	String vibrateKey;
	String ringtoneKey; 
	
	private Vibrator vibrator;
	private Long vDuration = (long) 300;
	private Boolean bVibrate;
	private Boolean hasVibrate = false;

	private String ringtoneStr;
	private Uri ringtoneURI;
	private Ringtone ringtone;
	
	private Notification notification;
	
	public MsgNotifier(Context context) {
		mContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.registerOnSharedPreferenceChangeListener(this);	
		
		initPreference();
		
		vibrator = (Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE);
		if (vibrator.hasVibrator()) {
			hasVibrate = true;
		}
	}
	
	public void messageReceive() {
		if (bVibrate && hasVibrate) {
			vibrator.vibrate(vDuration);
		}
		if (ringtone != null) {
			ringtone.play();
		}
		
	}
	
	public void initPreference() {
		vibrateKey = mContext.getString(R.string.pref_key_vibrate);
		ringtoneKey = mContext.getString(R.string.pref_key_ringtone);

		bVibrate = prefs.getBoolean(vibrateKey, true);

		ringtoneStr = prefs.getString(ringtoneKey, "DEFAULT_SOUND"); 
		ringtoneURI = Uri.parse(ringtoneStr);
		ringtone = RingtoneManager.getRingtone(mContext, ringtoneURI);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(vibrateKey)) {
			bVibrate = prefs.getBoolean(key, true);
		}
		if (key.equals(ringtoneKey)) {
			ringtoneStr = prefs.getString(ringtoneKey, "DEFAULT_SOUND"); 
			ringtoneURI = Uri.parse(ringtoneStr);
			ringtone = RingtoneManager.getRingtone(mContext, ringtoneURI);
		}
	}
	
	
}
