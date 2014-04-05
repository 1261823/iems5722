package com.iems5722.chatapp.gui;

import android.net.Uri;

public class TcpAttachMsgVO {
	private String userIp;
	private String chatMsg;
	private Uri attachmentUri;
	
	
	public String getUserIp() {
		return userIp;
	}
	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}
	public Uri getAttachmentUri() {
		return attachmentUri;
	}
	public void setAttachmentUri(Uri attachmentUri) {
		this.attachmentUri = attachmentUri;
	}
	
	public String getChatMsg() {
		return chatMsg;
	}
	public void setChatMsg(String chatMsg) {
		this.chatMsg = chatMsg;
	}
	
}
