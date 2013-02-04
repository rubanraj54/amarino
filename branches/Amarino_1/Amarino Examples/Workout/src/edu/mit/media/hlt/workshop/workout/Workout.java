package edu.mit.media.hlt.workshop.workout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class Workout extends Activity {
	
	private static final String TAG = "Workout";
	private static final String DEVICE_ADDRESS =  "00:06:66:03:17:17"; //"00:06:66:03:73:7B";
	
	private GraphView mGraph;
	private TextView mValueTV;
	private TextView mCounterTV;
	
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	
	int threshold = 400;
	
	private int counter = 0;
	long timestamp = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.main);
        
        // get handles to Views defined in our layout file
        mGraph = (GraphView)findViewById(R.id.graph);
        mValueTV = (TextView) findViewById(R.id.value);
        mCounterTV = (TextView) findViewById(R.id.counter);
        
        mGraph.setMaxValue(1200);
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		Preferences.getThreshold(this);
		
		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		Amarino.connect(this, DEVICE_ADDRESS);
	}


	@Override
	protected void onStop() {
		super.onStop();
		// tell Amarino to disconnect
		Amarino.disconnect(this, DEVICE_ADDRESS);
		// do never forget to unregister a registered receiver
		unregisterReceiver(arduinoReceiver);
	}
	

    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_prefs:
			Intent intent = new Intent(this, Preferences.class);
			this.startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}




	/**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino
	 * events.
	 * 
	 * It extracts data from the intent and updates the graph accordingly.
	 */
	public class ArduinoReceiver extends BroadcastReceiver {
		boolean counted = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String data = null;

				final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
				
				if (dataType == AmarinoIntent.STRING_EXTRA){
					data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
					
					if (data != null){
						try {
							// convert data to a number
							final int sensorReading = Integer.parseInt(data);
							mGraph.addDataPoint(sensorReading);
							
							if (sensorReading < threshold && !counted) {
								counter++;
								mCounterTV.setText(String.valueOf(counter));
								counted = true;
							}
							else if (sensorReading >= threshold){
								counted = false;
							}
							timestamp = System.currentTimeMillis();
							
							
						} catch (NumberFormatException e) {
							// data was not an integer
							//e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
