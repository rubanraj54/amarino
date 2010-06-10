/*
  Amarino - A prototyping software toolkit for Android and Arduino
  Copyright (c) 2009 Bonifaz Kaufmann.  All right reserved.
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package edu.mit.media.android.amarino;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * The Settings screen. Preferences are defined in xml/preferences.xml config file.
 * This class provides also convenient static methods to get or set the values.
 * 
 * @author Bonifaz Kaufmann
 *
 */
public class BTPreferences extends PreferenceActivity {
	
	public static final String KEY_DEFAULT_PIN = "settings_default_pin";
	public static final String KEY_DEFAULT_ADDRESS = "settings_default_address";
	public static final String KEY_ALIVE_PERIOD = "settings_alive_period";
	public static final String KEY_SENSOR_READING_RATE = "settings_sensor_reading_rate";
	public static final String KEY_LAST_CONNECTED_ADDRESS = "settings_last_connected_address";
	public static final String KEY_STAY_CONNECTED = "settings_stay_connected";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.settings_title);
		// Load the preferences from an XML resource
	    addPreferencesFromResource(R.xml.preferences);
	}
	
	public static String getDefaultAddress(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString(BTPreferences.KEY_DEFAULT_ADDRESS, 
					context.getString(R.string.pref_last_device_address_default_value));
	}
	
	public static void setDefaultAddress(Context context, String lastAddress){
		PreferenceManager.getDefaultSharedPreferences(context)
			.edit().putString(BTPreferences.KEY_DEFAULT_ADDRESS, lastAddress)
			.commit();
	}
	
	public static String getLastConnectedAddress(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString(BTPreferences.KEY_LAST_CONNECTED_ADDRESS, "");
	}
	
	public static void setLastConnectedAddress(Context context, String lastAddress){
		PreferenceManager.getDefaultSharedPreferences(context)
			.edit().putString(BTPreferences.KEY_LAST_CONNECTED_ADDRESS, lastAddress)
			.commit();
	}
	
	public static long getAlivePeriod(Context context){
		return Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context)
			.getString(BTPreferences.KEY_ALIVE_PERIOD,
					context.getString(R.string.pref_alive_period_default_value)));
	}
	
	public static int getSensorReadingRate(Context context){
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
			.getString(BTPreferences.KEY_SENSOR_READING_RATE,
					context.getString(R.string.pref_sensor_reading_rate_default_value)));
	}
	
	public static boolean isStayConnectedSelected(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean(BTPreferences.KEY_STAY_CONNECTED,
					Boolean.getBoolean(context.getString(R.string.pref_stay_connected_default_value)));
	}
	
	public static void setStayConnected(Context context, boolean enabled){
		PreferenceManager.getDefaultSharedPreferences(context)
			.edit().putBoolean(BTPreferences.KEY_STAY_CONNECTED, enabled);
	}
	
}
