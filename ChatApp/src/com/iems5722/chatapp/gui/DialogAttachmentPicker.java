package com.iems5722.chatapp.gui;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Window;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.network.ThreadUDPSend;

public class DialogAttachmentPicker extends Activity { 
	public final static String TAG = "DialogAttachmentPicker";
	final Context context = this;
	
	private static final int SELECT_PHOTO=100;	
	private static final int SELECT_AUDIO=200;
	private static final int SELECT_VIDEO=300;
	private static final int SELECT_FILE=400;
	private int currentExtIntent=0;
	private AlertDialog.Builder attachDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d(APP_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		buildDialog();
	}
	
	public void buildDialog() {
		attachDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_AlertDialog));
		
		attachDialog.setTitle(R.string.att_title);
		attachDialog.setItems(R.array.attachment, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
					case(0):
						//photos
						currentExtIntent = SELECT_PHOTO;
						Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(photoPickerIntent, SELECT_PHOTO);   
						break;
					case(1):
						//video
						currentExtIntent =  SELECT_VIDEO;
						Intent videoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(videoPickerIntent, SELECT_VIDEO);
						break;
					case(2):
						//audio
						currentExtIntent = SELECT_AUDIO;
						Intent audioPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(audioPickerIntent, SELECT_AUDIO);
						break;
						
					case(3):
						//file
						currentExtIntent = SELECT_FILE;
						Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
						filePickerIntent.setType("file/*");
						startActivityForResult(filePickerIntent, SELECT_FILE);
						break;
					default:
						Log.e(TAG, "Unknown command: " + which);
						break;
				}
			}
		});
		
		attachDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				stopAllRunningActivity();
			}
		});
		
		attachDialog.setCancelable(true);
		attachDialog.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, returnedIntent); 
	    	
	    	if(resultCode == RESULT_OK){  
	        	try{
		            Uri selectedFileUri = returnedIntent.getData();
		            Log.d(TAG,"selected file : " + selectedFileUri.getPath());
		            Intent resultIntent = new Intent();
		            resultIntent.setData(selectedFileUri);
		            setResult(RESULT_OK, resultIntent);
		            this.finishActivity(requestCode);
		            this.finish();
		            currentExtIntent=0;
	        	}catch(Exception ex){
	        		Log.d(TAG, ex.getMessage());	        		
	        	}
	        	
	        }else{
	        	stopAllRunningActivity();
	        }
	  
	}	
	
	@Override
	public void onDestroy() {
		//inform other users 
		super.onDestroy();
		stopAllRunningActivity();
	}	
	
	private void stopAllRunningActivity(){
		
		if (currentExtIntent!=0){
			this.finishActivity(currentExtIntent);
		}
		this.finish();
	}
	

	
	
}
