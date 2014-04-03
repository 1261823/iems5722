package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

public class DialogWifiAvailable extends DialogFragment {
	public final static String TAG = "DialogWifiAvailable";
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder wifiDialog = new AlertDialog.Builder(getActivity());
		wifiDialog.setTitle(R.string.dialog_wifi_title)
		.setMessage(R.string.dialog_wifi_message)
		.setCancelable(true)
		.setPositiveButton(R.string.dialog_wifi_accept,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
					startActivity(intent);
					dialog.dismiss();
				}
			}
		)
		.setNegativeButton(R.string.dialog_wifi_decline,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}
		);
		return wifiDialog.create();
	}
}
