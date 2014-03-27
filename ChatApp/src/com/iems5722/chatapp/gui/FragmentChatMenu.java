package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentChatMenu extends Fragment {
	private final static String TAG = "FragmentChatMenu";
	private OnButtonClickListener clickListener;
	private Button btnSendMessage; 
	
	public interface OnButtonClickListener {
		public void buttonClick(int buttonId);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.chat_menu, container, false);
		btnSendMessage = (Button) view.findViewById(R.id.menu_chat_send);
		setClickListener();
		return view;
	}
        
	private void setClickListener() {
		btnSendMessage.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	clickListener.buttonClick(R.id.menu_chat_send);
	        }
		});
	}
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		try {
			clickListener = (OnButtonClickListener) activity;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnButtonClickListener");
		}
	}
}
