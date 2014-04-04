package com.iems5722.chatapp.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TblChat {
	//table   columns
	public static final String TABLE_CHAT = "chat";	
	public static final String MESSAGE_ID = "_id";
	public static final String SESSION_ID = "session_id";
	public static final String USER_ID = "user_id";
	public static final String MESSAGE = "msg";
	public static final String MSG_DATETIME = "msg_dt";

	//create database
	private static final String DATABASE_CREATE = "CREATE TABLE  IF NOT EXISTS " 
			+ TABLE_CHAT + " ("
			+ MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ SESSION_ID + " TEXT NOT NULL, " // for simplicity the other user's id is the session id
			+ USER_ID + " TEXT NOT NULL,  "
			+ MESSAGE + " BLOB NOT NULL, "
			+ MSG_DATETIME + " INTEGER NOT NULL);" ; 
				
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}	

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TblChat.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
		// TODO: Implement this to handle requests to update one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
