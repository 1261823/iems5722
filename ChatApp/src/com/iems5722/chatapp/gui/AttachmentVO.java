package com.iems5722.chatapp.gui;

import android.net.Uri;

public class AttachmentVO {
	private String userIp;
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
	
	
}
