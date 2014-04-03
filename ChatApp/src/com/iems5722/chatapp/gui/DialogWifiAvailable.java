package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.Window;

public class DialogWifiAvailable extends Activity {
	public final static String TAG = "DialogWifiAvailable";
	final Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d(APP_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		buildDialog();
	}
	
	public void buildDialog() {
		AlertDialog.Builder wifiDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_AlertDialog));
		
		wifiDialog.setTitle(R.string.dialog_wifi_title)
		.setMessage(R.string.dialog_wifi_message)
		.setCancelable(false)
		.setPositiveButton(R.string.dialog_wifi_accept,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
					startActivity(intent);
					dialog.dismiss();
					finish();
				}
			}
		)
		.setNegativeButton(R.string.dialog_wifi_decline,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					finish();
				}
			}
		);
		wifiDialog.show();
	}
}
