package com.iems5722.chatapp.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TblUser {
	//table   columns
	public static final String TABLE_USER 	  = "user";	
	public static final String USER_UFI 	  = "_id";
	public static final String USER_ID 	  	  = "user_id";
	public static final String USER_NAME	  = "user_name";
	public static final String IP_ADDR_INT    = "ipaddr_int";
	public static final String IP_ADDR_STR    = "ipaddr_str";
	public static final String STATUS 		  = "status";
	public static final String USER_DATETIME  = "user_dt";
	
	public static final String STAT_ON = "online";
	public static final String STAT_OFF = "offline";

	//create database
	private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " 
			+ TABLE_USER + " ("
			+ USER_UFI + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ USER_ID + " TEXT NOT NULL, "
			+ USER_NAME + " TEXT NOT NULL,  "
			+ IP_ADDR_INT + " INTEGER NOT NULL, "
			+ IP_ADDR_STR + " TEXT NOT NULL, "
			+ STATUS + " TEXT NOT NULL, "
			+ USER_DATETIME + " INTEGER NOT NULL);" ; 
				
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}	

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TblUser.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
		// TODO: Implement this to handle requests to update one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
