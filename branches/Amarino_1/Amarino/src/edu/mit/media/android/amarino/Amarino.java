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

  Acknowledgements:
  Amarino uses the Bluetooth API of Stefano Sanna which can be found at
  http://code.google.com/p/android-bluetooth/
*/
package edu.mit.media.android.amarino;

import it.gerdavax.android.bluetooth.BluetoothException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.mit.media.android.amarino.db.Collection;

public class Amarino extends Activity implements OnConnectionChangedListener{
	
	public static final String TAG = "Amarino";
	
	private static final int MENU_ABOUT = 23;
	
	ImageView bluetoothIV;
	ImageView eventIV;
	ImageView monitoringIV;
	ImageView settingsIV;
	TextView statusTV;
	ImageButton connectBtn;
	
	BTService btService;
	Handler handler = new Handler();
	boolean isBound = false;
	//int state = BTService.DISCONNECTED;
	boolean connected = false;
	boolean reconnecting = false;
	
	// Service connection will get us a handle to our btService
	ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			btService = ((BTService.BTServiceBinder)service).getService();
			isBound = true;
//			if (btService.state == BTService.CONNECTED){
//				state = BTService.CONNECTED;
//				//connectBtn.setImageResource(R.drawable.connect_button_on_128);
//				//statusTV.setText(R.string.connected);
//			}
//			else {
//				state = BTService.DISCONNECTED;
//				//connectBtn.setImageResource(R.drawable.connect_button_off_128);
//				//statusTV.setText(R.string.disconnected);
//			}
			btService.registerOnConnectionChangedListener(Amarino.this);
			btService.updateOnConnectionChangedListener(Amarino.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBound = false;
			
		}
	};
	
	/**
	 * ConnectorTask is used to connect to a BT device, since this can
	 * take a while it has become an AsyncTask which runs in the background.
	 * This is a very convenient mechanism, because we can decide what
	 * to do up front and what to do when the task is finished
	 */
	class ConnectorTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... addresses) {
			try {
				btService.connect(addresses[0]);
			} catch (BluetoothException e) {
				// TODO show error message
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				// TODO show error message
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			btService.state = BTService.CONNECTING;
			connectBtn.setImageResource(R.drawable.connect_button_connecting_128);
			statusTV.setText(R.string.connecting);
		}

		@Override
		protected void onPostExecute(Boolean isConnected) {
			super.onPostExecute(isConnected);
			if (isConnected) {
				statusTV.setText(R.string.connected);
				connectBtn.setImageResource(R.drawable.connect_button_on_128);
			}
			else {
				Toast.makeText(Amarino.this, "Connecting failed.", Toast.LENGTH_SHORT).show();
				statusTV.setText(R.string.disconnected);
				btService.state = BTService.DISCONNECTED;
				connectBtn.setImageResource(R.drawable.connect_button_off_128);
			}
		}
		
		
		
	}
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        findViews();
        setClickListener();

        startService(new Intent(Amarino.this, BTService.class));
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    	bindService(new Intent(Amarino.this, BTService.class),
        		serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	if (isBound) {
    		btService.unregisterOnConnectionChangedListener(Amarino.this);
			unbindService(serviceConnection);
			isBound = false;
		}
    }
    
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (btService.state == BTService.DISCONNECTED)
    		stopService(new Intent(this, BTService.class));
	}

    
	private void findViews(){
    	bluetoothIV = (ImageView)findViewById(R.id.bluetooth);
    	eventIV = (ImageView)findViewById(R.id.event);
    	monitoringIV = (ImageView)findViewById(R.id.monitoring);
    	settingsIV = (ImageView)findViewById(R.id.settings);
    	statusTV = (TextView)findViewById(R.id.status);
    	connectBtn = (ImageButton)findViewById(R.id.connect);
    }
	

    
    private void setClickListener(){
    	
    	connectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        if (isBound) {
		        	try {
						if (btService.bluetoothHandler.isBluetoothEnabled()){
							switch(btService.state){
							case BTService.CONNECTED:
							case BTService.RECONNECTING:
								btService.disconnect(true);
								connectBtn.setImageResource(R.drawable.connect_button_off_128);
								statusTV.setText(R.string.disconnected);
								break;
							case BTService.DISCONNECTED:
								new ConnectorTask().execute(Collection.getCurrentDeviceAddress(Amarino.this));
								break;
							default:
								Toast.makeText(Amarino.this, R.string.wait_operation_going_on, Toast.LENGTH_SHORT).show();
							}
						}
						else {
							Toast.makeText(Amarino.this, R.string.bluetooth_disabled_dialog_msg, Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						Toast.makeText(Amarino.this, "Coulnd not connect. Bluetooth unaccessible. Error 27!", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
		        	
		        }
		        else {
		        	// TODO proper error message
					Toast.makeText(Amarino.this, "Coulnd not connect. Background service not bound. Error 13!", Toast.LENGTH_SHORT).show();
				
		        }
			}
		});
    	
    	bluetoothIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Amarino.this, BluetoothManagement.class));
			}
		});
    	
		eventIV.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Amarino.this, EventManagement.class));
			}
		});
		
		monitoringIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Amarino.this, Monitoring.class));
			}
		});
		
		settingsIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Amarino.this, BTPreferences.class));
			}
		});
    }

	@Override
	public void deviceConnected() {
		Log.d(TAG, "got connected msg");
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				statusTV.setText(R.string.connected);
				connectBtn.setImageResource(R.drawable.connect_button_on_128);
			}
		});
		
		
	}

	@Override
	public void deviceDisconnected() {
		Log.d(TAG, "got disconnected msg");
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				statusTV.setText(R.string.disconnected);
				connectBtn.setImageResource(R.drawable.connect_button_off_128);
			}
		});
		
	}
    
	@Override
	public void deviceReconnecting() {
		Log.d(TAG, "got reconnecting msg");
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				statusTV.setText(R.string.try_reconnect);
				connectBtn.setImageResource(R.drawable.connect_button_reconnect_128);
			}
		});
		
	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ABOUT, Menu.FIRST, R.string.menu_about)
			.setIcon(android.R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case MENU_ABOUT:
			String title = getString(R.string.app_name) + " " + getVersion();
			new AlertDialog.Builder(Amarino.this)
				.setTitle(title)
				.setView(View.inflate(this, R.layout.about, null))
				.setIcon(R.drawable.icon_small)
				.setPositiveButton("OK", null)
				.show();
			break;
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	
	private String getVersion() {
		String version = "1.0"; 
		try { 
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0); 
		    version = pi.versionName; 
		} catch (PackageManager.NameNotFoundException e) { 
		    Log.e(TAG, "Package name not found", e); 
		} 
		return version;
	}


}
