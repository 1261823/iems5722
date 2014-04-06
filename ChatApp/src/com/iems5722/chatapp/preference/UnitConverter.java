package com.iems5722.chatapp.preference;

import java.text.SimpleDateFormat;

public class UnitConverter {

	public static String getDateTime(Long timeMS) {
		SimpleDateFormat timeString;
		timeString = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
		return timeString.format(timeMS);
	}
}
