package com.iems5722.chatapp.gui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

class ContentSpannable extends ClickableSpan
{
    private Uri contentUri;
    private Context context;

	
	public ContentSpannable(String uri, Context context) {
        this.contentUri = Uri.parse(uri);
        this.context = context;
    }
    
    @Override
    public void updateDrawState(TextPaint ds) {
            // Customize your Text Look if required
        ds.setColor(Color.WHITE);
        ds.setUnderlineText(true);
        ds.setFakeBoldText(true);
        ds.setShadowLayer(10, 1, 1, Color.RED);
        //ds.setTextSize(15);
    }
    
    @Override
    public void onClick(View widget) {
    
    }
    
    public Uri getContentUri() {
		return contentUri;
	}

	public void setContentUri(Uri contentUri) {
		this.contentUri = contentUri;
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
