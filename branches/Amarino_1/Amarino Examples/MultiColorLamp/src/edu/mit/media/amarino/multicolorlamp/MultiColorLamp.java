/*
  MultiColorLamp - Example to use with Amarino
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
package edu.mit.media.amarino.multicolorlamp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MultiColorLamp extends Activity implements OnSeekBarChangeListener{
	
	private static final String TAG = "MultiColorLamp";
	
	final int DELAY = 150;
	SeekBar redSB;
	SeekBar greenSB;
	SeekBar blueSB;
	View colorIndicator;
	
	int red, green, blue;
	long lastChange;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // this will start Amarino in the background trying to connect to the
        // address specified in collection Multicolor Lamp
        Intent setCollection = new Intent("amarino.SET_COLLECTION");
		setCollection.putExtra("COLLECTION_NAME", "Multicolor Lamp");
		sendBroadcast(setCollection);
		
        sendBroadcast(new Intent("amarino.CONNECT"));
        
        // get references to views defined in our main.xml layout file
        redSB = (SeekBar) findViewById(R.id.SeekBarRed);
        greenSB = (SeekBar) findViewById(R.id.SeekBarGreen);
        blueSB = (SeekBar) findViewById(R.id.SeekBarBlue);
        colorIndicator = findViewById(R.id.ColorIndicator);

        // register listeners
        redSB.setOnSeekBarChangeListener(this);
        greenSB.setOnSeekBarChangeListener(this);
        blueSB.setOnSeekBarChangeListener(this);
    }
    
	@Override
	protected void onStart() {
		super.onStart();

		// load last state
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        red = prefs.getInt("red", 0);
        green = prefs.getInt("green", 0);
        blue = prefs.getInt("blue", 0);
        
        // set seekbars and feedback color according to last state
        redSB.setProgress(red);
        greenSB.setProgress(green);
        blueSB.setProgress(blue);
        colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));
        new Thread(){
        	public void run(){
        		try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {}
				Log.d(TAG, "update colors");
        		updateAllColors();
        	}
        }.start();
        
	}

	@Override
	protected void onStop() {
		super.onStop();
		// save state
		PreferenceManager.getDefaultSharedPreferences(this)
			.edit()
				.putInt("red", red)
				.putInt("green", green)
				.putInt("blue", blue)
			.commit();
		
		// stop Amarino's background service, we don't need it any more 
		sendBroadcast(new Intent("amarino.DISCONNECT"));
	}



	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// do not send to many updates, Arduino can't handle so much
		if (System.currentTimeMillis() - lastChange > DELAY ){
			updateState(seekBar);
			lastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		lastChange = System.currentTimeMillis();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {
		
		switch (seekBar.getId()){
			case R.id.SeekBarRed:
				red = seekBar.getProgress();
				updateRed();
				break;
			case R.id.SeekBarGreen:
				green = seekBar.getProgress();
				updateGreen();
				break;
			case R.id.SeekBarBlue:
				blue = seekBar.getProgress();
				updateBlue();
				break;
		}
		// provide user feedback
		colorIndicator.setBackgroundColor(Color.rgb(red, green, blue));
	}
	
	private void updateAllColors() {
		// send state to Arduino
        updateRed();
        updateGreen();
        updateBlue();
	}
	
	private void updateRed(){
		// we use for each color a separate action string and put only the changing
		// value in it, hence we have to create three custom events in Amarino
		// we could have used one action string for all colors and put always
		// all three colors in it, but this would cause unnecessary high traffic 
		Intent intent = new Intent();
        intent.setAction("amarino.multicolorlamp.red");
		intent.putExtra("red", red);
		
		// broadcast this intent which contains the color
		// and make sure Amarino is connected and
		// has a matching custom event for it
		sendBroadcast(intent);
	}
	
	private void updateGreen(){
		Intent intent = new Intent();
		intent.setAction("amarino.multicolorlamp.green");
		intent.putExtra("green", green);
		sendBroadcast(intent);
	}
	
	private void updateBlue(){
		Intent intent = new Intent();
		intent.setAction("amarino.multicolorlamp.blue");
		intent.putExtra("blue", blue);
		sendBroadcast(intent);
	}
	
}