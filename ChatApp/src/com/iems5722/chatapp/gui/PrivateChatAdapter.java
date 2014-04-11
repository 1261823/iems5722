package com.iems5722.chatapp.gui;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.database.TblChat;
import com.iems5722.chatapp.database.TblUser;
import com.iems5722.chatapp.preference.UnitConverter;

public class PrivateChatAdapter extends SimpleCursorAdapter {
	public final static String TAG = "PrivateChatAdapter";
	private Context context;
	private int layout;
	private LayoutInflater mInflater;
	
	private static final int TYPE_SENT = 0;
	private static final int TYPE_RECV = 1;	
	
	private TextView vMsgId;
	private TextView vMsgAuthor;
	private TextView vMsgContent;
	private TextView vMsgTimestamp;
	
	public PrivateChatAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;	
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	private int getItemViewType(Cursor cursor) {
		//String[] colname = cursor.getColumnNames();
		//int i = 0;
		//int counter = cursor.getCount();
		//Log.d(TAG, Integer.toString(counter));
		//for (String s:colname) {
		//	Log.d(TAG, "col " + s + " val " + cursor.getString(i));
		//	i++;
		//}
		int colAuthor = cursor.getColumnIndex(TblUser.USER_ID);
		String msgAuthor = cursor.getString(colAuthor);
		if (msgAuthor.equals(Activity_TabHandler.userId)) {
			return TYPE_SENT;
		}
		else {
			return TYPE_RECV;
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		return getItemViewType(cursor);
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	public static class ViewHolder {
		public View viewholder;
	}	
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//Log.d(TAG, "newView");
		Cursor c = getCursor();
		ViewHolder holder = new ViewHolder();
		View v = null;
		
		//change the message display if is own message
		int colAuthor = c.getColumnIndex(TblUser.USER_ID);
		String msgAuthor = c.getString(colAuthor);

		//LayoutParams params;
		//Log.i(TAG, "Msg " + msg + " from " + msgAuthor + " vs " + Activity_TabHandler.userId);
		if (msgAuthor.equals(Activity_TabHandler.userId)) {
			//Log.i(TAG, "Msg from self");
			v = mInflater.inflate(R.layout.chat_message_sent, parent, false);
			holder.viewholder = (View) v.findViewById(R.id.chat_message_sent);
		}		
		else {
			//Log.i(TAG, "Msg from other");
			v = mInflater.inflate(R.layout.chat_message_recv, parent, false);
			holder.viewholder = (View) v.findViewById(R.id.chat_message_recv);
		}
		v.setTag(holder);
		//v.setLayoutParams(params);
		//loadView(v, context, c);
		return v;
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		//log.d(APP_TAG, "bindView");
		ViewHolder holder = (ViewHolder) v.getTag();
		loadView(v, context, c);
	}
	
	private void loadView(View v, Context context, Cursor c) {
		int msgId = c.getColumnIndex(TblChat.MESSAGE_ID);
		int msgAuthor = c.getColumnIndex(TblUser.USER_NAME);
		int msgContent = c.getColumnIndex(TblChat.MESSAGE);
		int msgTimestamp = c.getColumnIndex(TblChat.MSG_DATETIME);
		
		long dbMsgId = c.getLong(msgId);	
		String dbMsgAuthor = c.getString(msgAuthor);
		String dbMsgContent = c.getString(msgContent);
		long dbMsgTimestamp = c.getLong(msgTimestamp);
		
		vMsgId = (TextView) v.findViewById(R.id.msg_id);
		vMsgAuthor = (TextView) v.findViewById(R.id.msg_author);
		vMsgContent = (TextView) v.findViewById(R.id.msg_recv);
		vMsgTimestamp = (TextView) v.findViewById(R.id.msg_timestamp);
		
		
		vMsgId.setText(Long.toString(dbMsgId));
		vMsgAuthor.setText(dbMsgAuthor);
		vMsgTimestamp.setText(UnitConverter.getDateTime(dbMsgTimestamp));

		//special handling for stored content URI
		if (dbMsgContent.startsWith("content://")){
			String contentUriString = dbMsgContent.substring(10);
			String filename = dbMsgContent.substring(dbMsgContent.lastIndexOf("/")+1);
			String defaultText =  "Content Received : ";
			String displayText = defaultText + filename;
			
			SpannableStringBuilder stringBuilder = new SpannableStringBuilder(displayText);
			ContentSpannable contentSpannable = new ContentSpannable(contentUriString ,context){

			            @Override
			            public void onClick(View widget) {
			                Intent intent = new Intent(Intent.ACTION_VIEW);
			                intent.setDataAndType(getContentUri(), getMimeType(getContentUri().getPath())); 
			                getContext().startActivity(intent);
			            }
			        };
			        
		    stringBuilder.setSpan(contentSpannable, defaultText.length()-1 , displayText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		    vMsgContent.setText( stringBuilder, BufferType.SPANNABLE);
		    vMsgContent.setMovementMethod(LinkMovementMethod.getInstance());
		}else{
			vMsgContent.setText(dbMsgContent);			
		}
	}
	
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
