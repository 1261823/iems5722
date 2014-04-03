package com.iems5722.chatapp.network;

public class UserDetail {
	int dbUserId;
	String md5UserId;
	public UserDetail(int dbUserId, String md5UserId) {
		this.dbUserId = dbUserId;
		this.md5UserId = md5UserId;
	}
}
