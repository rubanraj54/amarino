package edu.mit.media.hlt.workshop.workout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

public class Workout extends Activity {
	
	private static final String TAG = "Workout";
	
	private GraphView mGraph;
	private TextView mValueTV;
	private TextView mCounterTV;
	
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	
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
		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter("amarino.RESPONSE"));
		Intent setCollection = new Intent("amarino.SET_COLLECTION");
		setCollection.putExtra("COLLECTION_NAME", "Workout");
		sendBroadcast(setCollection);
		// tell Amarino to connect
		sendBroadcast(new Intent("amarino.CONNECT"));
	}


	@Override
	protected void onStop() {
		super.onStop();
		// tell Amarino to disconnect
		sendBroadcast(new Intent("amarino.DISCONNECT"));
		// do never forget to unregister a registered receiver
		unregisterReceiver(arduinoReceiver);
	}
	
    
    /**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino
	 * events.
	 * 
	 * It extracts data from the intent and updates the graph accordingly.
	 */
	public class ArduinoReceiver extends BroadcastReceiver {
		boolean counted = false;
		final int THRESHOLD = 400;

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				// get data out of the packet
				String data = intent.getStringExtra("data");
				if (data != null){
					mValueTV.setText(data);
					try {
						// convert data to a number
						final int sensorReading = Integer.parseInt(data);
						mGraph.addDataPoint(sensorReading);
						
						if (sensorReading < THRESHOLD && !counted) {
							counter++;
							mCounterTV.setText(String.valueOf(counter));
							counted = true;
						}
						else if (sensorReading >= THRESHOLD){
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