package at.abraxas.amarino.example.plugin.compass;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.plugin.BackgroundService;

public class MyBackgroundService extends BackgroundService 
			implements SensorEventListener, OnSharedPreferenceChangeListener {
	
	
	private static final String TAG = "Example Compass Plugin";
	private static final boolean DEBUG = true;
	
	private SensorManager sm;
	private Sensor orientationSensor;
	private int frequency;
	private int ignoreThreshold = 0;
	private int ignoreCounter = 0;
	
	
	public MyBackgroundService(){
		super(TAG, DEBUG); 
	}
		

	@Override
	public boolean init() {
		/* here should be your specific initialization code */
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		frequency = prefs.getInt(MyEditActivity.PREF_FREQUENCY, 50);
		ignoreThreshold = MyEditActivity.getRate(frequency);
		
		prefs.registerOnSharedPreferenceChangeListener(this);
	
		// make sure not to call it twice
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		orientationSensor = sm.getSensorList(Sensor.TYPE_ORIENTATION).get(0);
		sm.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI);
		
		return true;
	}
	
	@Override
	public void cleanup() {
		sm.unregisterListener(this);
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}



	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
			
			if (ignoreCounter >= ignoreThreshold) {
				ignoreCounter = 0;
				int heading = (int)event.values[0];
				
				if (DEBUG) Log.d(TAG, "send: " + heading);
				Amarino.sendDataFromPlugin(this, pluginId, heading);
			}
			else {
				ignoreCounter++;
			}
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// we don't need this
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs,
			String key) {
		if (MyEditActivity.PREF_FREQUENCY.equals(key)){
			ignoreThreshold = MyEditActivity.getRate(prefs.getInt(key, 50));
		}
	}

	
	
}
