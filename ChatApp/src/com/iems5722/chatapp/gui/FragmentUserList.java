package com.iems5722.chatapp.gui;

import com.iems5722.chatapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentUserList extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_userlist, container, false);
		UserListActive userActive = new UserListActive();
		UserListInactive userInactive = new UserListInactive();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.add(R.id.usersActive, userActive);
		transaction.add(R.id.usersInactive, userInactive);
		transaction.commit();
		return rootView;
	}
}
