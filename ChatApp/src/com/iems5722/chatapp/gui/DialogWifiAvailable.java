package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;

public class DialogWifiAvailable extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		AlertDialog.Builder wifiDialog = new AlertDialog.Builder(this);
		wifiDialog.setTitle(R.string.dialog_wifi_title)
		.setMessage(R.string.dialog_wifi_message)
		.setCancelable(true)
		.setPositiveButton(R.string.dialog_wifi_accept,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
					startActivity(intent);
					finish();
				}
			}
		);
//		AlertDialog alertDialog = locationDialog.create();
		wifiDialog.show();
	}
}
