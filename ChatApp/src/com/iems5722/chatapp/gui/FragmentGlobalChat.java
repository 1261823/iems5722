package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentGlobalChat extends Fragment {
	public final static String TAG = "FragmentGlobalChat";
	
	static FragmentGlobalChat init() {
		FragmentGlobalChat fragGM = new FragmentGlobalChat();
		return fragGM;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.frame_chatdetail, container, false);
		Fragment globalChatList = new GlobalChatList();
		Fragment globalChatMenu = new FragmentChatMenu();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.add(R.id.layout_details, globalChatList);
		transaction.add(R.id.layout_menu, globalChatMenu);
		transaction.commit();
		return rootView;
	}
}
