package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class Activity_Login extends Activity {
	private static final String TAG = "Activity_Login";
	
	public static final String URI_USERNAME = "username";
	private EditText login_username;
	private Button login_button;
	public String username;

	@Override
 	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		login_username = (EditText)findViewById(R.id.login_username);
		login_button = (Button)findViewById(R.id.login_btn_enter);
		
		initClickHandler();
		//start network services
		initNetworkServices();
	}
	
	private void initClickHandler() {
		login_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// check if username has been entered
				username = login_username.getText().toString();
				//Log.d(TAG, username);
				//Log.d(TAG, Integer.toString(username.length()));
				if (username.length() > 0 ) {
					//Log.d(TAG, "Entering chat");
					Intent intent = new Intent(getApplicationContext(), Activity_TabHandler.class);
					intent.putExtra(URI_USERNAME, username);
					startActivity(intent);
				}
				else {
					//TODO ask user to enter a valid username
				}
			}
		});
	}
	
	private void initNetworkServices() {
	}
}
