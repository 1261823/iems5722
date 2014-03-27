package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;
import com.iems5722.chatapp.preference.Settings;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

public class Activity_TabHandler extends FragmentActivity implements
	FragmentChatMenu.OnButtonClickListener {
	
	private static final String TAG = "Activity_TabHandler";

	private ViewPager mViewPager;
	private SlidePagerAdapter mPagerAdapter;
	
	FragmentGlobalChat globalChat;
	UserList userList;
	SessionList privateChat;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		globalChat  = new FragmentGlobalChat();
		userList    = new UserList();
		privateChat = new SessionList();
		
		setContentView(R.layout.activity_main);

		mViewPager = (ViewPager)findViewById(R.id.project_pager);
		mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);		
		
		
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//final ActionBar actionBar = getActionBar();
		//actionBar.setDisplayShowTitleEnabled(false);
		//actionBar.setDisplayShowHomeEnabled(false);	
		//Note: this removes the action bar and preference menu
		//actionBar.hide();
	}

	private class SlidePagerAdapter extends FragmentPagerAdapter {
		SparseArray<Fragment> registeredFragment = new SparseArray<Fragment>();
		
		public SlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = new Fragment();
			switch (position) {
			case 0:
				return fragment = globalChat;
			case 1:
				return fragment = userList;
			case 2:
				return fragment = privateChat;
			}
			return fragment;
		}
		
		@Override
		public int getCount() {
			return 3;
		}
		
		@Override 
		public CharSequence getPageTitle(int position) {
			switch(position) {
			case 0:
				return getString(R.string.tab_globalchat);
			case 1:
				return getString(R.string.tab_users);
			case 2:
				return getString(R.string.tab_privatechat);
			}
			return null;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.d(TAG, "instantiateItem");
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			registeredFragment.put(position, fragment);
			return fragment;
		}
		
		public Fragment getRegisteredFragment(int position) {
			return registeredFragment.get(position);
		}
	}

	@Override
	public void buttonClick(int buttonId) {
		switch(buttonId) {
		case(R.id.menu_chat_send):
			//Send message to global chat
			Toast.makeText(getApplicationContext(), "Global message sent clicked", Toast.LENGTH_SHORT).show();
			break;
		default:
			throw new IllegalArgumentException("Unknown button clicked " + Integer.toString(buttonId));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting_menu, menu);
		return true;
	}	
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_pref:
			Intent iMenuPreference = new Intent(this, Settings.class);
			startActivity(iMenuPreference);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}		
	
}