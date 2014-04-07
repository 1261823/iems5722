package com.iems5722.chatapp.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.Window;

import com.iems5722.chatapp.R;


public class DialogWifiAvailable extends Activity {
	public final static String TAG = "DialogWifiAvailable";
	
	private Context context = this;
	public static boolean notShowing = true;
	private static Handler uiHandler;
	
	//network monitoring
	NetworkInfo networkInfo = null;	
	//dialog
	AlertDialog wifiDialog;
	
	public static void setHandler(Handler handler) {
		uiHandler = handler;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d(APP_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		buildDialog();
	};		
	
	public void buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_AlertDialog));

		builder.setTitle(R.string.dialog_wifi_title)
		.setMessage(R.string.dialog_wifi_message)
		.setCancelable(false)
		.setPositiveButton(R.string.dialog_wifi_accept,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					notShowing = true;
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
					notShowing = true;					
					uiHandler.obtainMessage(Activity_TabHandler.DEREGISTER_WIFI_BCR).sendToTarget();
					dialog.dismiss();
					finish();
				}
			}
		)
		.setNeutralButton(R.string.dialog_wifi_dismiss, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					notShowing = true;
					dialog.cancel();
					finish();
				}
			}
		);
		
		wifiDialog = builder.create();
		wifiDialog.show();
		notShowing = false;
	}
}
