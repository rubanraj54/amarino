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

import java.util.ArrayList;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public abstract class BTActivity extends ListActivity 
						implements OnBTEventListener, OnConnectionChangedListener {
	
	public static final String TAG = "BTActivity";
	public static boolean DEBUG = true;
	
	protected static final String BT_PREFS_NAME = "BT_Preferences_Paired_Devices";
	protected static final String KEY_PAIRED_DEVICES_NUM = "edu.mit.media.android.amarino.paired_devices_num";
	protected static final String KEY_PAIRED_DEVICE_NAME = "edu.mit.media.android.amarino.paired_device_name";
	protected static final String KEY_PAIRED_DEVICE_ADDRESS = "edu.mit.media.android.amarino.paired_device_address";

	protected static final int SHOW_DISCOVERED_DEVICES = 55;
	
	
	static Vector<BTDevice> discoveredDevices = new Vector<BTDevice>();
	
	protected BTService btService;
	protected boolean isBound = false;
	protected Handler handler = new Handler();

	private ProgressDialog dialog;
	int lastAccessedBTDevicePosition = -1;
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	if (DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }
    
    @Override
	protected void onStart() {
    	if (DEBUG) Log.d(TAG, "onStart");
		super.onStart();
		bindService(new Intent(BTActivity.this, BTService.class),
        		serviceConnection, Context.BIND_AUTO_CREATE);
		
	}
    
    
	@Override
	protected void onStop() {
		if (DEBUG) Log.d(TAG, "onStop");
		super.onStop();
		
		if (isBound) {
			btService.unregisterOnConnectionChangedListener(BTActivity.this);
			unbindService(serviceConnection);
			isBound = false;
		}
	}
    

	public void scanForDevices() {
		if (isBound) {
			try {
				if (btService.bluetoothHandler.isBluetoothEnabled()){
					btService.bluetoothHandler.scanForDevices();
				}
				else {
					showInfoDialog(R.string.bluetooth_disabled_dialog_title, 
							R.string.bluetooth_disabled_dialog_msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    
    public void sendData(byte[] data){
    	btService.bluetoothHandler.sendData(data);
    }
    
    public boolean isBluetoothEnabled() throws Exception{
		return btService.bluetoothHandler.isBluetoothEnabled();
    }

	public void toggleBluetooth() {
		if (isBound){
			try {
				if (btService.bluetoothHandler.isBluetoothEnabled()){
					showProgressDialog(getString(R.string.bluetooth_disabling_progess_dialog_msg));
					btService.bluetoothHandler.setBluetoothEnabled(false);
				}
				else {
					showProgressDialog(getString(R.string.bluetooth_enabling_progess_dialog_msg));
					btService.bluetoothHandler.setBluetoothEnabled(true);
				}
			} catch (Exception e) {
				Log.e(TAG, "Could not enable/disable Bluetooth");
			}
		}
	}
	
	public void pairDevices(String address){
		if (isBound) {
			try {
				if (btService.bluetoothHandler.isBluetoothEnabled()){
					//showProgressDialog(getString(R.string.bluetooth_pairing_dialog_msg));
					btService.bluetoothHandler.pairDevices(address);
				}
				else {
					showInfoDialog(R.string.bluetooth_disabled_dialog_title, 
							R.string.bluetooth_disabled_dialog_msg);
				}
			} catch (Exception e) {
				Log.e(TAG, "Could not enable/disable Bluetooth");
			}
		}
	}
	
	
	
    
    @Override
	public void bluetoothDisabled() {
    	hideProgressDialog();
	}

	@Override
	public void bluetoothEnabled() {
		hideProgressDialog();
	}
	
	@Override
	public void deviceFound(String address) {
		BTDevice d = new BTDevice();
		d.address = address;
		d.name = btService.bluetoothHandler.getRemoteName(address);
		d.state = btService.bluetoothHandler.isPaired(address) ? BTDevice.PAIRED : BTDevice.NOT_PAIRED;
		if (!discoveredDevices.contains(d)) {
			discoveredDevices.add(d);
		}
	}

	@Override
	public void scanCompleted(ArrayList<String> devices) {
		
		// delete previous list of paired devices
		SharedPreferences prefs = getSharedPreferences(BT_PREFS_NAME, MODE_PRIVATE);
		Editor edit = prefs.edit();

		int numPairedDevices = prefs.getInt(KEY_PAIRED_DEVICES_NUM, 0);
		// TODO instead of delete previous entries, we should check if new paired devices
		// are found and add them to the persistent list
		// delete should only happen on user intention
		for (int i=0; i<numPairedDevices; i++){
			edit.remove(KEY_PAIRED_DEVICE_NAME + i);
			edit.remove(KEY_PAIRED_DEVICE_ADDRESS + i);
		}
		edit.commit();
		
		// generate new list of devices
		numPairedDevices = 0;
		//discoveredDevices.clear();
		for (int i = 0; i<devices.size(); i++){
			// update and store paired devices
			//discoveredDevices.get(i).name = serviceBinder.bluetoothHandler.getRemoteName(discoveredDevices.get(i).address);
			//discoveredDevices.get(i).state = serviceBinder.bluetoothHandler.isPaired(discoveredDevices.get(i).address) ? BTDevice.PAIRED : BTDevice.NOT_PAIRED;
			BTDevice d = discoveredDevices.get(i);
			if (d.state==BTDevice.PAIRED || d.state == BTDevice.CONNECTING || d.state == BTDevice.CONNECTED){
				edit.putString(KEY_PAIRED_DEVICE_NAME + numPairedDevices, d.name);
				edit.putString(KEY_PAIRED_DEVICE_ADDRESS + numPairedDevices, d.address);
				numPairedDevices++;
			}
		}
		edit.putInt(KEY_PAIRED_DEVICES_NUM, numPairedDevices);
		edit.commit();
	}

	@Override
	public void scanStarted() {
	}
	
	@Override
	public void paired() {
		if (lastAccessedBTDevicePosition > -1)
			discoveredDevices.get(lastAccessedBTDevicePosition).state = BTDevice.PAIRED;
	}

	@Override
	public void pinRequested() {
		showInfoDialog(R.string.bluetooth_pin_requested_dialog_title,
				R.string.bluetooth_pin_requested_dialog_msg);
	}
	
	
	@Override
	public void gotServiceChannel(int serviceID, int channel) {
	}

	@Override
	public void serviceChannelNotAvailable(int serviceID) {
	}
	
	// used to inform super class
	protected abstract void hasBound();
		
	
	

	protected ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			btService = ((BTService.BTServiceBinder)service).getService();
			isBound = true;
			btService.bluetoothHandler.addOnBTEventListener(BTActivity.this);
			btService.registerOnConnectionChangedListener(BTActivity.this);
			btService.updateOnConnectionChangedListener(BTActivity.this);
			hasBound();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBound = false;
			btService = null;
		}
	};
	

	protected void showInfoDialog(final int title, final int msg) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				new AlertDialog.Builder(BTActivity.this)
					.setTitle(title)
					.setMessage(msg)
					.setCancelable(true)
					.setPositiveButton(R.string.ok, null)
					.create()
					.show();
			}
		});
	}

	/*
	 * Display a pop up dialog to indicate heavy processing. 
	 * 
	 * @param msg - the message to be shown inside the dialog.
	 */
	protected synchronized void showProgressDialog(final String msg) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				dialog = ProgressDialog.show(
					BTActivity.this, "", msg, true, true);
			}
		});
	}
	
	/*
	 * Hides a previously shown progress dialog.
	 */
	protected synchronized void hideProgressDialog() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});
	}

	


}


