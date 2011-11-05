package at.abraxas.amarino.plugin.gesture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
import at.abraxas.amarino.plugin.BackgroundService;

public class GestureService extends BackgroundService
								implements SensorEventListener{
	
	private static final String TAG = "GestureService";
	private static final boolean DEBUG = true;
	
	private final int STATE_NORMAL = 10;
	private final int STATE_TURNED_AROUND = 20;
	
	private final int MSG_FLIP_OVER = 1;
	private final int MSG_REVERT_BACK = 2;
	
	private final int SENSOR_INTERVAL_DEFAULT = 2; // should reduce battery drain slightly
	private final int SENSOR_INTERVAL_SLOW = 30;  // should reduce battery drain even more
	private final int INITAL_SENSOR_COUNT = 1;
	
	private int interval = SENSOR_INTERVAL_DEFAULT;
	private int sensorCount = INITAL_SENSOR_COUNT;
	
	private SensorManager sm;
	private Sensor orientationSensor;
	
	private int state = STATE_NORMAL;
	private int oldState = STATE_NORMAL;

	
	public GestureService() {
		super(TAG, DEBUG); 
		
	}
	
	
	@Override
	public boolean init() {
		pluginId = PreferenceManager.getDefaultSharedPreferences(this)
			.getInt(AmarinoIntent.EXTRA_PLUGIN_ID, -1);
		
		/* add your code here */
		initSensorManager();
		
		return true; // note: return true if init was successful
	}

	
	@Override
	public void cleanup() {
		/* add your code here */
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sm.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		if (sensorCount % interval == 0){
			sensorCount = INITAL_SENSOR_COUNT;
			
			switch (event.sensor.getType()){
				case Sensor.TYPE_ORIENTATION:
					//Log.d(GestureService.TAG, "sensor active");
					
					state = getState(event.values[SensorManager.DATA_Y],
							event.values[SensorManager.DATA_Z]);
					
					if (oldState == STATE_NORMAL && state == STATE_TURNED_AROUND){
						// flip over detected
						Amarino.sendDataFromPlugin(this, pluginId, MSG_FLIP_OVER);
					}
					else if (oldState == STATE_TURNED_AROUND && state == STATE_NORMAL){
						// revert back detected
						Amarino.sendDataFromPlugin(this, pluginId, MSG_REVERT_BACK);
					}
					
					//Log.d(TAG, "state: " + state);
					
					oldState = state;
					break;
			}
		}
		else {
			sensorCount++;
			//Log.d(GestureService.TAG, "sensorcount: " + sensorCount);
		}
		
	}
	
	private void initSensorManager() {
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		orientationSensor = sm.getSensorList(Sensor.TYPE_ORIENTATION).get(0);

		sm.registerListener(GestureService.this, 
					orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private int getState(float y, float z) {
		//Log.d(TAG, y + " , " + z);
		if ((y > 168 || y < -168) && (z > -13 && z < 13)) {
			return STATE_TURNED_AROUND;
		}
		else {
			return STATE_NORMAL;
		}
	}


	

	

}
