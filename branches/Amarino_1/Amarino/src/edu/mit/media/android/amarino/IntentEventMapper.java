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

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import edu.mit.media.android.amarino.db.Event;
import edu.mit.media.android.amarino.db.EventData;

public class IntentEventMapper {
	
	/**
	 * For new events add
	 *  - an action string
	 *  - a start flag for this event
	 *  - add it to the action list in getActionList() 
	 *  - add event in convertIntent
	 *  - build method to analyze data in bundle
	 *  - register a method in Arduino sketch for that event
	 */
	public static final String TAG = "IntentEventMapper";
	
	public static final String ALIVE = "edu.mit.media.android.alive_request";
	public static final String CALL_STATE_RINGING = "edu.mit.media.android.ringing";
	public static final String CALL_STATE_IDLE = "edu.mit.media.android.idle";
	public static final String CALL_STATE_OFFHOOK = "edu.mit.media.android.offhook";
	public static final String BATTERY_LEVEL = "edu.mit.media.android.battery_changed";
	public static final String COMPASS = "edu.mit.media.android.compass";
	public static final String MAGNETIC_FIELD = "edu.mit.media.android.magnetic_field";
	public static final String ACCELEROMETER = "edu.mit.media.android.accelerometer";
	public static final String ORIENTATION = "edu.mit.media.android.orientation";
	public static final String TEMPRATURE = "edu.mit.media.android.temprature";
	public static final String EARTHQUAKE = "New_Earthquake_Found";
	public static final String TIME_TICK = Intent.ACTION_TIME_TICK;
	public static final String TEST_EVENT = "edu.mit.media.android.test";
	public static final String RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";
	
	public static final String KEY_TEMPREATURE = "temp";
	public static final String KEY_BATTERY_LEVEL = "level";
	public static final String KEY_COMPASS_HEADING = "heading";
	
	public static final String KEY_ORIENTATION_AZIMUTH = "azimuth";
	public static final String KEY_ORIENTATION_PITCH = "pitch";
	public static final String KEY_ORIENTATION_ROLL = "roll";
	
	public static final String KEY_ACCELEROMETER_X = "x";
	public static final String KEY_ACCELEROMETER_Y = "y";
	public static final String KEY_ACCELEROMETER_Z = "z";
	
	public static final String KEY_MAGNETIC_FIELD_X = "x";
	public static final String KEY_MAGNETIC_FIELD_Y = "y";
	public static final String KEY_MAGNETIC_FIELD_Z = "z";
	
	public static final String KEY_EARTHQUAKE_MAGNITUDE = "magnitude";
	public static final String KEY_EARTHQUAKE_LATITUDE = "latitude";
	public static final String KEY_EARTHQUAKE_LONGITUDE = "longitude";
	
	public static final char ACK_FLAG = 19;
	public static final char FLUSH_FLAG = 27;
	public static final char DELIMITER = ';'; // used to separate data strings
	public static final char ALIVE_FLAG = 17;
	public static final char ARDUINO_MSG_FLAG = 18;
	
	// alive msg is happens very often, we optimize it to be a constant
	// instead of constructing it always from ground
	public static final String ALIVE_MSG = ALIVE_FLAG + "" + ACK_FLAG;
	

	public static String getEventString(ContentResolver cr, Bundle b){
		String action = b.getString("action");

		// in case of alive message, send immediately our alive string
		if (action.equals(ALIVE)){
			return ALIVE_MSG;
		}
		else {
			Event e = Event.getEvent(cr, action);
			if (e != null) {
				return addProtocolFlags(e.flag, getDataAsString(cr, e, b));
			}
			else {
				return null;
			}
			
		}
	}
	
	private static String getDataAsString(ContentResolver cr, Event e, Bundle b){
		String result = "";
		e.data = Event.getEventData(cr, e);
		if (e.data != null && e.data.size() > 0){
			boolean first = true;
			for (EventData ed : e.data){
				// TODO this could be optimized, so that data are sent not as string
				// only but if data is a single byte it should be sent as a single byte
				// for int or other data types the arduino library needs to parse them correct
				if (!first)
					result += DELIMITER;
				result += EventData.extractData(ed, b);
				first = false;
			}
		}
		return result;
	}
	

	protected static String addProtocolFlags(char START_FLAG, String s){
		return START_FLAG + s + ACK_FLAG;
	}
	
}
