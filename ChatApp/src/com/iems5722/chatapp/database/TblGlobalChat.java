package com.iems5722.chatapp.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TblGlobalChat {
	//table   columns
	public static final String TABLE_GLOBAL_CHAT = "globalchat";	
	public static final String MESSAGE_ID = "_id";
	public static final String USER_ID = "user_id";
	public static final String MESSAGE = "message";
	public static final String MSG_DATETIME = "datetime";

	//create database
	private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " 
			+ TABLE_GLOBAL_CHAT + " ("
			+ MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
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
