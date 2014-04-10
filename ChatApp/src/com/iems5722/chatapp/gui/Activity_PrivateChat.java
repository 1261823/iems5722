package com.iems5722.chatapp.gui;


import java.util.Set;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.DbProvider;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblUser;
import com.iems5722.chatapp.network.PeerFileSender;
import com.iems5722.chatapp.network.PeerFileService;
import com.iems5722.chatapp.network.PeerFileService.PeerFileServiceBinder;

public class Activity_PrivateChat extends FragmentActivity implements 
	FragmentChatMenu.OnButtonClickListener,
	LoaderCallbacks<Cursor> {
	public final static String TAG = "Activity_PrivateChat";
	private final static int ACTIVIITY_ATTACHMENT_PICKER=100;
	
	public final static int INIT_PROGRESS_BAR = 1;
	public final static int UPDATE_PROGRESS_BAR = 2;
	public final static int DONE_AND_PREVIEW = 3;
	
	private ActionBar actionBar;

	//Peer File Transfer service
	PeerFileService peerFileService;
	private Intent peerFileServiceIntent;
	private Handler peerFileServiceHandler;
		
	//loader types
	final static int LOAD_USER = 0;
	final static int LOAD_SESSION = 1;
	Loader<Cursor> LOADER_USER;
	Loader<Cursor> LOADER_SESSION;
	
	// peer details
	private String peerIdAddress;
	//user identifer
	private long dbPUFI = -1;
	private String dbPID = "";
	
	//session id
	private long dbMsgId = -1;
	private String dbSessionId = "";
	
	private Context privateChatContext = this;
	private ProgressDialog progressBar;
	private int progressBarStatus = 0;
	

	private Handler progressBarHandler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frame_chatdetail);

		Bundle arguments = getIntent().getExtras();
		// Need to give session id into private chat list to load history
		// two entry points to this activity
		// 1. by tapping on user's name. Has USER UFI, need to find USER ID
		// 2. by clicking on an existing chat session. Has MSG ID, need to find SESSION ID
		// Note: USER_ID = SESSION_ID
		if (arguments != null) {
			Set<String> strSet = arguments.keySet();
			Log.i(TAG, "KeySize " + Integer.toString(strSet.size()));
			for (String s:strSet) {
				Log.d(TAG, "Key " + s + " : " + arguments.getLong(s));
			}
			if (getIntent().hasExtra(TblUser.USER_UFI)) {
				// handle scenario 1 where only user ufi is known
				// Get the User ID and User IP Address
				dbPUFI = arguments.getLong(TblUser.USER_UFI);
				getSupportLoaderManager().initLoader(LOAD_USER, arguments, this);
			}
			if (getIntent().hasExtra(TblChat.KEY_MSG_ID)) {
				// handle scenario 2 where only messge id is known
				// Get the Session ID, then get User ID and USER IP Address
				dbMsgId = arguments.getLong(TblChat.KEY_MSG_ID);
				getSupportLoaderManager().initLoader(LOAD_SESSION, arguments, this);
			}
			// test at least one id value is available
			if (dbPUFI == -1 && dbMsgId == -1) {
				Log.e(TAG, "Entered private chat with no id");
			}
		}
		
		if (findViewById(R.id.layout_details) != null) {
			if (savedInstanceState != null) {
                return;
            }
			//fragments in activity
			PrivateChatList chatSession = new PrivateChatList(); 
			FragmentChatMenu chatMenu = new FragmentChatMenu();
			getSupportFragmentManager().beginTransaction().add(R.id.layout_details, chatSession).commit();
			getSupportFragmentManager().beginTransaction().add(R.id.layout_menu, chatMenu).commit();
		}	
		
		//create Peer File Transfer service
		peerFileServiceIntent = new Intent(this, PeerFileService.class);
		bindService(peerFileServiceIntent, peerFileServiceConnection, Context.BIND_AUTO_CREATE);
		
		//initial progress bar
		progressBar = new ProgressDialog(this);
		progressBar.setCancelable(false);
		progressBar.setMessage("File downloading... :D");
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(100);
	}
	
	
	
	
	
	@Override
	public void buttonClick(int buttonId) {
		switch(buttonId) {
		case(R.id.menu_chat_send):
			//TODO Send message to recipient
			EditText chatText = (EditText)this.findViewById(R.id.menu_chat_input);
			
			String msgToSend = chatText.getText().toString().trim();
			if (msgToSend.length() > 0) {
				String ipAddress = peerIdAddress.replace("/", "");
				TcpAttachMsgVO chatMsgVO = new TcpAttachMsgVO();
				chatMsgVO.setUserIp(ipAddress);
				chatMsgVO.setChatMsg(msgToSend);
				chatMsgVO.setChatSessionId(dbSessionId);
				
			
				peerFileService.getPeerFileSender().obtainMessage(PeerFileSender.SEND_MSG, chatMsgVO).sendToTarget();
				//Toast.makeText(this, "Sent private message to " + peerIdAddress, Toast.LENGTH_SHORT).show();
				chatText.setText("");
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown button clicked " + Integer.toString(buttonId));
		}			
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Log.d(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.private_chat, menu);
		return true;
	}	

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.chat_attachment:
			//show attachment picker
			Intent iAttachmentPicker = new Intent(this, DialogAttachmentPicker.class);
			iAttachmentPicker.addCategory(Intent.CATEGORY_LAUNCHER);
			iAttachmentPicker.addFlags(Intent.FLAG_FROM_BACKGROUND);
			//not sure if session information needs to be passed to intent
			startActivityForResult(iAttachmentPicker, ACTIVIITY_ATTACHMENT_PICKER);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		switch(loaderId) {
		case(LOAD_USER):
			if (bundle.containsKey(TblUser.USER_UFI)) {
				// load chat session
				dbPUFI = bundle.getLong(TblUser.USER_UFI);	
				Log.d(TAG, "Creating cursor looking for user ufi " + Long.toString(dbPUFI));
				String[] userCol = {TblUser.USER_UFI, TblUser.USER_ID, TblUser.USER_NAME, TblUser.IP_ADDR_STR};;
				String userSel = TblUser.USER_UFI + " = ?";
				String[] userArgs = {Long.toString(dbPUFI)};
				LOADER_USER = new CursorLoader(this, DbProvider.USER_URI, userCol, userSel, userArgs, null);
			}
			else if (bundle.containsKey(TblUser.USER_ID)) {
				// look for user details after getting session id from message id
				dbPID = bundle.getString(TblUser.USER_ID);	
				Log.d(TAG, "Creating cursor looking for user id " + dbPID);
				String[] userCol = {TblUser.USER_UFI, TblUser.USER_ID, TblUser.USER_NAME, TblUser.IP_ADDR_STR};;
				String userSel = TblUser.USER_ID + " = ?";
				String[] userArgs = {dbPID};
				LOADER_USER = new CursorLoader(this, DbProvider.USER_URI, userCol, userSel, userArgs, null);
			}
			return LOADER_USER;
		case(LOAD_SESSION):
			// get session id from message id
			dbMsgId = bundle.getLong(TblChat.KEY_MSG_ID);	
			Log.d(TAG, "Creating cursor looking for message id " + Long.toString(dbMsgId));
			String[] sessCol = {TblChat.TABLE_CHAT + "." +TblChat.MESSAGE_ID, TblChat.MESSAGE, TblChat.MSG_DATETIME, TblChat.SESSION_ID};;
			String sessSel = TblChat.TABLE_CHAT + "." + TblChat.MESSAGE_ID + " = ?";
			String[] sessArgs = {Long.toString(dbMsgId)};
			LOADER_SESSION = new CursorLoader(this, DbProvider.PCHAT_URI, null, sessSel, sessArgs, null);
			return LOADER_SESSION;		
		default:
			Log.e(TAG, "Unkown loader requested " + Integer.toString(loaderId));
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.equals(LOADER_USER)) {
			Log.d(TAG, "User details loaded");
			if (cursor.moveToFirst()) {
				int colUsername   = cursor.getColumnIndex(TblUser.USER_NAME);
				String username = cursor.getString(colUsername);
				actionBar = getActionBar();
				actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.android_blue));
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setDisplayShowHomeEnabled(false);	
				actionBar.setTitle(getString(R.string.priv_chat_title) + " " + username);
				//Note: this removes the action bar and preference menu
				//actionBar.hide();
				
				int colIPAddress = cursor.getColumnIndex(TblUser.IP_ADDR_STR);
				peerIdAddress = cursor.getString(colIPAddress);
				Log.d(TAG, "Communicating with " + peerIdAddress);
				//Toast.makeText(this, "communicating with "+ peerIdAddress, Toast.LENGTH_SHORT).show();
				
				int colUserId = cursor.getColumnIndex(TblUser.USER_ID);
				dbPID = cursor.getString(colUserId);
				dbSessionId = dbPID;
				Log.d(TAG, "Session ID " + dbSessionId);
				//Toast.makeText(this, "Session Id "+ dbSessionId, Toast.LENGTH_SHORT).show();
				
				//update fragment
				PrivateChatList chatSession = (PrivateChatList) getSupportFragmentManager().findFragmentById(R.id.layout_details);
				chatSession.setSessionId(dbSessionId);
			}
		}
		else if (loader.equals(LOADER_SESSION)) {
			Log.d(TAG, "Session details loaded");
			if (cursor.moveToFirst()) {
				int colSessionId = cursor.getColumnIndex(TblChat.SESSION_ID);
				dbSessionId = cursor.getString(colSessionId);
				Toast.makeText(this, "Session Id " + dbSessionId, Toast.LENGTH_SHORT).show();
				Bundle args = new Bundle();
				args.putString(TblUser.USER_ID, dbSessionId);
				getSupportLoaderManager().initLoader(LOAD_USER, args, this);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		//not used
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, returnedIntent); 
	    	if (requestCode == ACTIVIITY_ATTACHMENT_PICKER){
		    	if(resultCode == RESULT_OK){
		    		Uri selectedFileUri = returnedIntent.getData();
		    		String ipAddress = peerIdAddress.replace("/", "");
		    		Log.d(TAG, "prepare to send file ip: " + ipAddress + " file URI: " + selectedFileUri);
		    		
		    		TcpAttachMsgVO attachVO = new TcpAttachMsgVO();
		    		attachVO.setUserIp(ipAddress);
		    		attachVO.setAttachmentUri(selectedFileUri);
		    		
		    		peerFileService.getPeerFileSender().obtainMessage(PeerFileSender.SEND_FILE, attachVO).sendToTarget();
		    	}	    	
	    	}
	}
	
	private ServiceConnection peerFileServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "onPeerFileServiceConnected");	
			PeerFileServiceBinder peerFileServiceBinder = (PeerFileServiceBinder) binder;
			peerFileService = peerFileServiceBinder.getService();
			peerFileServiceHandler = peerFileService.getServiceHandler();
			peerFileService.setUiHandler(mHandler);
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onPeerFileServiceDisconnected");				
		}
	};
	
	@Override
	public void onDestroy() {
		unbindService(peerFileServiceConnection);		
		super.onDestroy();
	}
	
	public int getProgressBarStatus() {
		return progressBarStatus;
	}

	public void setProgressBarStatus(int progressBarStatus) {
		this.progressBarStatus = progressBarStatus;
	}
	
	public Thread createProgressBarThread(){
		
		return new Thread(new Runnable() {
			  public void run() {
				while (progressBarStatus < 100) {

				  // process some tasks
				  progressBarStatus = getProgressBarStatus();

				  // your computer is too fast, sleep 1 second
				  try {
					Thread.sleep(200);
				  } catch (InterruptedException e) {
					e.printStackTrace();
				  }

				  // Update the progress bar
				  progressBarHandler.post(new Runnable() {
					public void run() {
					  progressBar.setProgress(progressBarStatus);
					}
				  });
				}

				if (progressBarStatus >= 100) {

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				
					progressBar.dismiss();
				}
			  }
		       });
		
	}
	
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Log.d(TAG, "received message");
        	switch (msg.what) {
        	case INIT_PROGRESS_BAR:
        		progressBar.setProgress(0);
        		progressBarStatus = 0;
        		progressBar.show();
        		Thread  thread = createProgressBarThread();
        		thread.start();
        		//Log.d(TAG, "File percentage"+ msg.. + "%");
    			break;
        	case UPDATE_PROGRESS_BAR:
        		setProgressBarStatus(Integer.parseInt(msg.obj.toString()));
    			break;
        	case DONE_AND_PREVIEW:
        		//progressBar.dismiss();
        		//Toast.makeText(privateChatContext.getApplicationContext(),  msg.obj.toString(), Toast.LENGTH_SHORT).show();
        		Uri receivedFileUri = (Uri)msg.obj;
        		Intent intent = new Intent();  
        		intent.setAction(android.content.Intent.ACTION_VIEW);  
        		intent.setDataAndType(receivedFileUri, getMimeType(receivedFileUri.getPath()));  
        		startActivity(intent);  
    			break;	
    		}
        }
	};
	
	public  String getMimeType(String url)
	{
		 String type = null;
		 	String extensionLastPart=url.substring(url.lastIndexOf(".")); 
		    String extension = MimeTypeMap.getFileExtensionFromUrl(extensionLastPart);
		    if (extension != null) {
		        MimeTypeMap mime = MimeTypeMap.getSingleton();
		        type = mime.getMimeTypeFromExtension(extension.toLowerCase());
		    }
		    return type;

	}
	
}
