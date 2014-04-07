package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.Window;

public class DialogWifiAvailable extends Activity {
	public final static String TAG = "DialogWifiAvailable";
	final Context context = this;
	public static boolean nowShowing = false;
	private static Handler uiHandler;
	
	public static void setHandler(Handler handler) {
		uiHandler = handler;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d(APP_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.registerReceiver(serviceBCR, filter);
		buildDialog();
	}
	
	//notify when connectivity changes
	private BroadcastReceiver serviceBCR = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.d(TAG, "Received " + intent.getAction());
			if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
				context.unregisterReceiver(serviceBCR);
				finish();
			}
		}
	};		
	
	
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
					nowShowing = false;
				}
			}
		)
		.setNeutralButton(R.string.dialog_wifi_dismiss, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			}
		)
		.setNegativeButton(R.string.dialog_wifi_decline,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					nowShowing = false;
					uiHandler.obtainMessage(Activity_Login.DEREGISTER_WIFI_BCR).sendToTarget();
					dialog.cancel();
					finish();
				}
			}
		);
		wifiDialog.show();
		nowShowing = true;
	}
}
