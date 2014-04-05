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

public class DialogAttachmentPicker extends Activity { 
	public final static String TAG = "DialogAttachmentPicker";
	final Context context = this;
	
	private static final int SELECT_PHOTO=100;	
	private static final int SELECT_AUDIO=200;
	private static final int SELECT_VIDEO=300;
	private static final int SELECT_FILE=400;
	
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
						Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(photoPickerIntent, SELECT_PHOTO);   
						break;
					case(1):
						//video
						Intent videoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(videoPickerIntent, SELECT_VIDEO);
						break;
					case(2):
						//audio
						Intent audioPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(audioPickerIntent, SELECT_AUDIO);
						break;
						
					case(3):
						//file
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
	        	}catch(Exception ex){
	        		Log.d(TAG, ex.getMessage());	        		
	        	}
	        }
	  
	}	
}
