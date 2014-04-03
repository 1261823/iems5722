package com.iems5722.chatapp.gui;



import com.iems5722.chatapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.widget.Toast;

public class DialogAttachmentPicker extends Activity { 
	public final static String TAG = "DialogAttachmentPicker";
	final Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d(APP_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		buildDialog();
	}
	
	public void buildDialog() {
		AlertDialog.Builder attachDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_AlertDialog));
		
		attachDialog.setTitle(R.string.att_title);
		attachDialog.setItems(R.array.attachment, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
					case(0):
						//photos
						Toast.makeText(getParent(), "Photo", Toast.LENGTH_SHORT).show();
					case(1):
						//audio
						Toast.makeText(getParent(), "Audio", Toast.LENGTH_SHORT).show();
					case(2):
						//video
						Toast.makeText(getParent(), "Video", Toast.LENGTH_SHORT).show();
					case(3):
						//file
						Toast.makeText(getParent(), "File", Toast.LENGTH_SHORT).show();
					default:
						Log.e(TAG, "Unknown command: " + which);
				}
			}
		});
		attachDialog.setCancelable(true);
		attachDialog.show();
	}
}
