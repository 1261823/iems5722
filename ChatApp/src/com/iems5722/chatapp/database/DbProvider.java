package com.iems5722.chatapp.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DbProvider extends ContentProvider{
	private static final String TAG = "DbProvider";
	
	public static DbHandler database;
	
	//content provider URI and authority
	public final static String AUTHORITY = "com.iems5722.chatapp.DbProvider";
	public final static String BASE_PATH_USER = TblUser.TABLE_USER;
	public final static String BASE_PATH_GCHAT = TblGlobalChat.TABLE_GLOBAL_CHAT;
	public final static String BASE_PATH_PCHAT = TblChat.TABLE_CHAT;
	public final static Uri USER_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_USER);
	public final static Uri GCHAT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_GCHAT);
	public final static Uri PCHAT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_PCHAT);
	
	//MIME types
	public static final String CONTENT_TYPE_USER = ContentResolver.CURSOR_DIR_BASE_TYPE + "/USERS";
	public static final String CONTENT_ITEM_USER = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/USER";
	public static final String CONTENT_TYPE_GCHAT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/GCHATS";
	public static final String CONTENT_ITEM_GCHAT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/GCHAT";	
	public static final String CONTENT_TYPE_PCHAT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/PCHATS";
	public static final String CONTENT_ITEM_PCHAT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/PCHAT";	
	
	//URI Matcher
	private static final int USER_LIST = 10;
	private static final int USER_ITEM = 11;	
	private static final int GCHAT_LIST = 20;
	private static final int GCHAT_ITEM = 21;	
	private static final int PCHAT_LIST = 30;
	private static final int PCHAT_ITEM = 31;
	private static final UriMatcher sURIMatcher = buildUriMatcher(); 

	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, BASE_PATH_USER, USER_LIST);
		matcher.addURI(AUTHORITY, BASE_PATH_USER + "/#", USER_ITEM);
		matcher.addURI(AUTHORITY, BASE_PATH_GCHAT, GCHAT_LIST);
		matcher.addURI(AUTHORITY, BASE_PATH_GCHAT + "/#", GCHAT_ITEM);		
		matcher.addURI(AUTHORITY, BASE_PATH_PCHAT, PCHAT_LIST);
		matcher.addURI(AUTHORITY, BASE_PATH_PCHAT + "/#", PCHAT_ITEM);
		return matcher;
	}		
	
	@Override
	public boolean onCreate() {
		database = new DbHandler(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		checkColumns(projection);
		int uriType = sURIMatcher.match(uri);
		switch(uriType) {
			case USER_LIST:
				queryBuilder.setTables(TblUser.TABLE_USER);
				break;
			case USER_ITEM:
				queryBuilder.setTables(TblUser.TABLE_USER);
				//queryBuilder.appendWhere(TblUser.USER_UFI + "=" + uri.getLastPathSegment());
				break;		
			case GCHAT_LIST:
				queryBuilder.setTables(TblGlobalChat.TABLE_GLOBAL_CHAT);
				break;
			case GCHAT_ITEM:
				queryBuilder.setTables(TblGlobalChat.TABLE_GLOBAL_CHAT);
				queryBuilder.appendWhere(TblGlobalChat.MESSAGE_ID + "=" + uri.getLastPathSegment());
				break;				
			case PCHAT_LIST:
				queryBuilder.setTables(TblChat.TABLE_CHAT);
				break;
			case PCHAT_ITEM:
				queryBuilder.setTables(TblChat.TABLE_CHAT);
				queryBuilder.appendWhere(TblChat.MESSAGE_ID + "=" + uri.getLastPathSegment());
				break;					
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(sqlDB, projection, selection, selectionArgs, null, null, sortOrder);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch(sURIMatcher.match(uri)) {
		case USER_LIST:
			return CONTENT_TYPE_USER;
		case USER_ITEM:
			return CONTENT_ITEM_USER;	
		case GCHAT_LIST:
			return CONTENT_TYPE_GCHAT;
		case GCHAT_ITEM:
			return CONTENT_ITEM_GCHAT;				
		case PCHAT_LIST:
			return CONTENT_TYPE_PCHAT;
		case PCHAT_ITEM:
			return CONTENT_ITEM_PCHAT;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		long id;
		switch(uriType) {
			case USER_LIST:
				values.remove(TblUser.USER_UFI);
				id = database.getWritableDatabase().insertOrThrow(TblUser.TABLE_USER, null, values);
				break;
			case GCHAT_LIST:
				values.remove(TblGlobalChat.MESSAGE_ID);
				id = database.getWritableDatabase().insertOrThrow(TblGlobalChat.TABLE_GLOBAL_CHAT, null, values);
				break;				
			case PCHAT_LIST:
				values.remove(TblChat.MESSAGE_ID);
				id = database.getWritableDatabase().insertOrThrow(TblChat.TABLE_CHAT, null, values);
				break;			
			default:
				id = -1;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		int count;
		switch(uriType) {
			case USER_ITEM:
				count = database.getWritableDatabase().delete(TblUser.TABLE_USER, TblUser.USER_UFI + "=?",
					    new String[] {Long.toString(ContentUris.parseId(uri))});
				break;
			case GCHAT_ITEM:
				count = database.getWritableDatabase().delete(TblGlobalChat.TABLE_GLOBAL_CHAT, TblGlobalChat.MESSAGE_ID + "=?",
					    new String[] {Long.toString(ContentUris.parseId(uri))});
				break;				
			case PCHAT_ITEM:
				count = database.getWritableDatabase().delete(TblChat.TABLE_CHAT, TblChat.MESSAGE_ID + "=?",
						new String[] {Long.toString(ContentUris.parseId(uri))});
				break;				
			default:
				count = 0;
		}		
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		int count;
		switch(uriType) {
			case USER_ITEM:
				count = database.getWritableDatabase().update(TblUser.TABLE_USER, values, TblUser.USER_UFI + "=?",
					    new String[] {Long.toString(ContentUris.parseId(uri))});
				break;
			case GCHAT_ITEM:
				count = database.getWritableDatabase().update(TblGlobalChat.TABLE_GLOBAL_CHAT, values, TblGlobalChat.MESSAGE_ID + "=?",
					    new String[] {Long.toString(ContentUris.parseId(uri))});
				break;				
			case PCHAT_ITEM:
				count = database.getWritableDatabase().update(TblChat.TABLE_CHAT, values, TblChat.MESSAGE_ID + "=?",
						new String[] {Long.toString(ContentUris.parseId(uri))});
				break;				
			default:
				count = 0;
		}		
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}		
		return count;
	}
	
	private void checkColumns(String[] projection) {
		String[] available = {
				TblChat.MESSAGE, TblChat.MESSAGE_ID, TblChat.MSG_DATETIME, TblChat.SESSION_ID, TblChat.TABLE_CHAT, TblChat.USER_ID,
				TblUser.IP_ADDR_INT, TblUser.IP_ADDR_STR, TblUser.USER_DATETIME, TblUser.USER_DATETIME, TblUser.STATUS, TblUser.TABLE_USER,
				TblUser.USER_ID, TblUser.USER_NAME, TblUser.USER_UFI,
				TblGlobalChat.MESSAGE, TblGlobalChat.MESSAGE_ID, TblGlobalChat.MSG_DATETIME, TblGlobalChat.TABLE_GLOBAL_CHAT, TblGlobalChat.USER_ID };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			if (!availableColumns.containsAll(requestedColumns)) {
				Log.e(TAG, "Requesting " + projection);
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}	

}
