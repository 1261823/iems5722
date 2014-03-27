package com.iems5722.chatapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHandler extends SQLiteOpenHelper{
	// Database Version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CHAT_APP";
	
    public DbHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		TblUser.onCreate(db);
		TblGlobalChat.onCreate(db);
		TblChat.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		TblUser.onUpgrade(db, oldVersion, newVersion);
		TblGlobalChat.onUpgrade(db, oldVersion, newVersion);
		TblChat.onUpgrade(db, oldVersion, newVersion);
	}

}
