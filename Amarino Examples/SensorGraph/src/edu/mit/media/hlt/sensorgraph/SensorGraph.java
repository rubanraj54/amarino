/*
  SensorGraph - Example to use with Amarino
  Copyright (c) 2009 Bonifaz Kaufmann. 
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package edu.mit.media.hlt.sensorgraph;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

/**
 * <h3>Application that receives sensor readings from Arduino displaying it graphically.</h3>
 * 
 * This example demonstrates how to catch data sent from Arduino forwarded by Amarino.
 * SensorGraph registers a BroadcastReceiver to catch Intents with action string: <b>"amarino.RESPONSE"</b>
 * 
 * @author Bonifaz Kaufmann - December 2009
 *
 */
public class SensorGraph extends Activity {
	
	private static final String TAG = "SensorGraph";
	
	private GraphView mGraph;
	private TextView mValueTV;
	
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        setContentView(R.layout.main);
        
        // get handles to Views defined in our layout file
        mGraph = (GraphView)findViewById(R.id.graph);
        mValueTV = (TextView) findViewById(R.id.value);
        
        mGraph.setMaxValue(1024);
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter("amarino.RESPONSE"));
		
		Intent setCollection = new Intent("amarino.SET_COLLECTION");
		setCollection.putExtra("COLLECTION_NAME", "SensorGraph");
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

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String data = intent.getStringExtra("data");
				if (data != null){
					mValueTV.setText(data);
					try {
						final int sensorReading = Integer.parseInt(data);
						mGraph.addDataPoint(sensorReading);
					} catch (NumberFormatException e) {
						// data was not an integer
						//e.printStackTrace();
					}
				}
			}
		}
	}

}

