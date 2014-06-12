package utils;

import java.util.Calendar;

import java.util.Locale;

import android.content.Context;

import android.content.SharedPreferences;

public class Settings {

	private static String PREFERENCES_NAME = "WIKOUT_SETTINGS";

	public static void setTimeLastCheckVersion(Context context, int value) {

		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCES_NAME, 0);

		SharedPreferences.Editor editor = settings.edit();

		editor.putInt("timeLastCheckVersion", value);

		editor.commit();

	}

	public static int getTimeLastCheckVersion(Context context) {

		SharedPreferences settings = context.getSharedPreferences(
				PREFERENCES_NAME, 0);

		return settings.getInt("timeLastCheckVersion", 0);

	}

}
