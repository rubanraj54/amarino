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

import java.util.HashMap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import edu.mit.media.android.amarino.db.Event;

public class AmarinoSensorListener implements SensorEventListener{

	public static final int TYPE_COMPASS = 1024;
	// slow down sending of events, only send every 3th sensor event (about 1 sec)
	private int skip_reading_threshold;
	private BTService btService;
	private HashMap<Integer, Sensor> sensors;
	private boolean compassSensorRegistered = false;
	private boolean orientationSensorRegistered = false;
	Sensor sensor;
	float values[];
	StringBuilder sb = new StringBuilder();
	String data;
	
	// keep flags as member variables to reduce db accesses
	char compassFlag;
	char orientationFlag;
	char accelerometerFlag;
	char magneticFlag;
	char tempFlag;

	int sensorType;
	int skippedEventsCompass = 0;
	int skippedEventsOrientation = 0;
	int skippedEventsAcc = 0;
	int skippedEventsMagnetic = 0;
	int skippedEventsTemp = 0;
	
	
	public AmarinoSensorListener(BTService service){
		this.btService = service;
		skip_reading_threshold = BTPreferences.getSensorReadingRate(service);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nothing to do here
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		sensorType = event.sensor.getType();
		if (sensors.containsKey(sensorType)){
			values = event.values;
			
			switch (sensorType) {
			
			case Sensor.TYPE_ORIENTATION:
				if (compassSensorRegistered){
					// send only heading
					if (skippedEventsCompass++ >= skip_reading_threshold){
						skippedEventsCompass = 0;
						sendData(compassFlag);
					}
				}
				else {
					if (skippedEventsOrientation++ >= skip_reading_threshold){
						skippedEventsOrientation = 0;
						// send all values
						sendData(orientationFlag);
					}
				}
				break;
				
			case Sensor.TYPE_ACCELEROMETER:
				if (skippedEventsAcc++ >= skip_reading_threshold){
					skippedEventsAcc = 0;
					sendData(accelerometerFlag);
				}
				break;
				
			case Sensor.TYPE_MAGNETIC_FIELD:
				if (skippedEventsMagnetic++ >= skip_reading_threshold){
					skippedEventsMagnetic = 0;
					sendData(magneticFlag);
				}
				break;

			case Sensor.TYPE_TEMPERATURE:
				if (skippedEventsTemp++ >= skip_reading_threshold){
					skippedEventsTemp = 0;
					sendData(tempFlag);
				}
				break;
			}
			
		}
	}
	
	private void sendData(char flag){
		// TODO message construction should be done in IntentEventMapper
		// 
		sb = new StringBuilder();
		sb.append(flag);
		if (flag == compassFlag){
			sb.append((int)values[0]);
		}
		else if (flag == orientationFlag){
			sb.append((int)values[0]).append(IntentEventMapper.DELIMITER)
			  .append((int)values[1]).append(IntentEventMapper.DELIMITER)
			  .append((int)values[2]);
		}
		else if (flag == tempFlag){
			sb.append(values[0]);
		}
		else {
			sb.append(values[0]).append(IntentEventMapper.DELIMITER)
			  .append(values[1]).append(IntentEventMapper.DELIMITER)
			  .append(values[2]);
		}
		sb.append(IntentEventMapper.ACK_FLAG);
		//Log.d("TAG", sb.toString());
		btService.sendData(sb.toString());
	}
	
	public void register(int sensorType){
		
		if (sensors == null) {
			sensors = new HashMap<Integer, Sensor>(1);
			getFlags();
		}
		
		if (sensorType == TYPE_COMPASS) {
			// don't double register orientation sensor
			if (!compassSensorRegistered && !orientationSensorRegistered)
				registerSensor(Sensor.TYPE_ORIENTATION);
			compassSensorRegistered = true;
		}
		else if (sensorType == Sensor.TYPE_ORIENTATION){
			if (!compassSensorRegistered && !orientationSensorRegistered)
				registerSensor(sensorType);
			orientationSensorRegistered = true;
		}
		else {
			registerSensor(sensorType);
		}

	}
	
	private void registerSensor(int sensorType){
		SensorManager sm = (SensorManager) btService.getSystemService(Context.SENSOR_SERVICE);
		sensor = sm.getSensorList(sensorType).get(0);
		sensors.put(sensorType, sensor);
		sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void unregister(int sensorType){
		
		if (sensorType == Sensor.TYPE_ALL){
			compassSensorRegistered = false;
			orientationSensorRegistered = false;
			unregsiterAllSensors();
		}
		else if (sensorType == TYPE_COMPASS){
			if (compassSensorRegistered && !orientationSensorRegistered)
				unregisterSensor(Sensor.TYPE_ORIENTATION);
		}
		else if (sensorType == Sensor.TYPE_ORIENTATION){
			if (!compassSensorRegistered && orientationSensorRegistered)
				unregisterSensor(sensorType);
		}
		else {
			unregisterSensor(sensorType);
		}
		
	}
	
	private void unregsiterAllSensors(){
		SensorManager sm = (SensorManager) btService.getSystemService(Context.SENSOR_SERVICE);
		sm.unregisterListener(this);
		if (sensors != null)
			sensors.clear();
	}
	
	private void unregisterSensor(int sensorType){
		SensorManager sm = (SensorManager) btService.getSystemService(Context.SENSOR_SERVICE);
		if (sensors != null){
			sensor = sensors.get(sensorType);
			if (sensor != null) {
				sm.unregisterListener(this, sensor);
				sensors.remove(sensorType);
			}
		}
	}
	
	private void getFlags(){
		compassFlag = Event.getEventFlag(btService.getContentResolver(), IntentEventMapper.COMPASS);
		orientationFlag = Event.getEventFlag(btService.getContentResolver(), IntentEventMapper.ORIENTATION);
		accelerometerFlag = Event.getEventFlag(btService.getContentResolver(), IntentEventMapper.ACCELEROMETER);
		magneticFlag = Event.getEventFlag(btService.getContentResolver(), IntentEventMapper.MAGNETIC_FIELD);
		tempFlag = Event.getEventFlag(btService.getContentResolver(), IntentEventMapper.TEMPRATURE);
	}
	

}
