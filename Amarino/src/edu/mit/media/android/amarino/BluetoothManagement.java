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

import it.gerdavax.android.bluetooth.BluetoothException;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class BluetoothManagement extends BTActivity implements View.OnClickListener{

	public static final String TAG = "BluetoothManagement";
	ImageView bluetoothOnOffIV;
	TextView enableDisableBTTV;
	ImageView scanDevicesIV;
	DeviceListAdapter devicesListAdapter;
	ProgressBar scanningPB;
	

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.bt_management_title);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.bluetooth_management);
		
		findViews();
		loadPreviousPairedDevices();
		initScannedDevicesList();
        setClickListener();
	}
	
	
	private void findViews(){
		bluetoothOnOffIV = (ImageView)findViewById(R.id.bluetooth_on_off);
		bluetoothOnOffIV.setOnClickListener(this);
		scanDevicesIV = (ImageView)findViewById(R.id.scan_devices);
		scanDevicesIV.setOnClickListener(this);
		enableDisableBTTV = (TextView)findViewById(R.id.enable_disable_bt_tv);
		enableDisableBTTV.setOnClickListener(this);
		scanningPB = (ProgressBar)findViewById(R.id.progress_scan);
		scanningPB.setVisibility(View.INVISIBLE); 
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.scan_devices:
			onScanDevicesClick(v);
			break;
		case R.id.bluetooth_on_off:
			onEnableDisableButtonClick(v);
			break;
		case R.id.enable_disable_bt_tv:
			onEnableDisableButtonClick(v);
			break;
		}
	}
	
	
	private void loadPreviousPairedDevices() {
		SharedPreferences prefs = getSharedPreferences(BT_PREFS_NAME, MODE_PRIVATE);
		int numPairedDevices = prefs.getInt(KEY_PAIRED_DEVICES_NUM, 0);
		
		for (int i=0; i<numPairedDevices; i++){
			BTDevice d = new BTDevice();
			d.name = prefs.getString(KEY_PAIRED_DEVICE_NAME + i, "");
			d.address = prefs.getString(KEY_PAIRED_DEVICE_ADDRESS + i, "");
			d.state = BTDevice.PAIRED;
			Log.d(TAG, "paired device stored: " + d.name);
			if (!discoveredDevices.contains(d))
				discoveredDevices.add(d);
		}
	}
	
	

	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}


	private void initScannedDevicesList() {
		devicesListAdapter = new DeviceListAdapter();
		setListAdapter(devicesListAdapter);
		
	}

	public void onEnableDisableButtonClick(View target){
		if (isBound){
			bluetoothOnOffIV.setColorFilter(Color.DKGRAY, Mode.MULTIPLY);
			toggleBluetooth();
		}
	}
	
	public void onScanDevicesClick(View target){
		if (isBound) {
			scanForDevices();
			// TODO clear list before scanning (!concurrent exception)
		}
	}
	
	private void setClickListener(){
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final String address = ((BTDevice)devicesListAdapter.getItem(position)).address;
				new AlertDialog.Builder(BluetoothManagement.this)
					.setTitle(address)
					.setItems(new CharSequence[]{"set as default device"}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							BTPreferences.setDefaultAddress(BluetoothManagement.this, address);
							Toast.makeText(BluetoothManagement.this,
									"Device [" + address + "] has been set as default device.", Toast.LENGTH_SHORT).show();
						}
					})
					.create()
					.show();
				return true;
			}
		});
	}
	
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (!scanDevicesIV.isEnabled()) {
			Toast.makeText(this, "Please wait until scanning has been finished!", Toast.LENGTH_SHORT).show();
		}
		else {
			lastAccessedBTDevicePosition = position;
			final String address = discoveredDevices.get(position).address;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothManagement.this)
				.setCancelable(false)
				.setNegativeButton(R.string.cancel, 
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}
				);
			
			if (discoveredDevices.get(position).state == BTDevice.PAIRED){
				builder.setMessage(R.string.bluetooth_connect_to_device_dialog_msg)
					.setNeutralButton(R.string.bluetooth_unpair, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (isBound && btService.bluetoothHandler.isPaired(address)) {
								btService.bluetoothHandler.unpair(address);
							}
							discoveredDevices.get(lastAccessedBTDevicePosition).state = BTDevice.NOT_PAIRED;
							devicesListAdapter.notifyDataSetChanged();
						}
					})
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
	
							if (isBound && btService.bluetoothHandler.isPaired(address)) {
								new ConnectorTask().execute(address);
					        	
							}
							else {
								discoveredDevices.get(lastAccessedBTDevicePosition).state = BTDevice.NOT_PAIRED;
								devicesListAdapter.notifyDataSetChanged();
							}
	
						}
					});
			}
			else if (discoveredDevices.get(position).state == BTDevice.NOT_PAIRED){
				builder.setMessage(R.string.bluetooth_pair_to_device_dialog_msg)
	    			.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								pairDevices(address);
							}
						}
	    			);
			}
			else {
				builder.setMessage("This device is already connected!");
				builder.setPositiveButton(R.string.disconnect,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								disconnect();
							}
						}
	    			);
			}
			builder.create().show();
		}
	}
	
	
	private void updateBluetoothStatus(){
		try {
			//bluetoothOnOffIV.setImageResource(R.drawable.bluetooth_icon);
			if (isBluetoothEnabled()){
				// TODO create 2 different drawables to indicate state
				bluetoothOnOffIV.setColorFilter(Color.GREEN, Mode.MULTIPLY);
				enableDisableBTTV.setText(R.string.disable_bluetooth);
			}
			else {
				bluetoothOnOffIV.setColorFilter(Color.RED, Mode.MULTIPLY);
				enableDisableBTTV.setText(R.string.enable_bluetooth);
			}
			bluetoothOnOffIV.invalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void connectTo(String address) throws Exception, BluetoothException {
		if (isBound) {
			btService.connect(address);
			discoveredDevices.get(lastAccessedBTDevicePosition).state = BTDevice.CONNECTED;
			handler.post(new Runnable() {
				@Override
				public void run() {
					devicesListAdapter.notifyDataSetChanged();
				}
			});
		}
	}
	
	public void disconnect(){
		if (isBound) {
			btService.disconnect(true);
			discoveredDevices.get(lastAccessedBTDevicePosition).state = BTDevice.PAIRED;
			handler.post(new Runnable() {
				@Override
				public void run() {
					devicesListAdapter.notifyDataSetChanged();
				}
			});
		}
		
	}
	
	private void udpateLastConnectedDeviceState(int state){
		String address = BTPreferences.getLastConnectedAddress(this);
		for (BTDevice b : discoveredDevices)
			if (b.address.equals(address))
				b.state = state;
		handler.post(new Runnable() {
			@Override
			public void run() {
				devicesListAdapter.notifyDataSetChanged();
			}
		});
	}
	
	
	@Override
	public void bluetoothDisabled() {
    	super.bluetoothDisabled();
    	updateBluetoothStatus();
	}

	@Override
	public void bluetoothEnabled() {
		super.bluetoothEnabled();
		updateBluetoothStatus();
	}
	
	@Override
	public void paired() {
		super.paired();
		devicesListAdapter.notifyDataSetChanged();
		
	}
	
	
	@Override
	public void deviceFound(String address) {
		super.deviceFound(address);
		devicesListAdapter.notifyDataSetChanged();
	}

	@Override 
	public void scanCompleted(ArrayList<String> devices) {
		super.scanCompleted(devices);
		setProgressBarIndeterminateVisibility(false);
		scanningPB.setVisibility(View.INVISIBLE);
		((AnimationDrawable)scanDevicesIV.getDrawable()).stop();
		scanDevicesIV.setImageResource(R.drawable.scan_devices);
		scanDevicesIV.setEnabled(true);
		//devicesListAdapter.notifyDataSetChanged();
	}

	@Override
	public void scanStarted() {
		super.scanStarted();
		setProgressBarIndeterminateVisibility(true);
		scanningPB.setVisibility(View.VISIBLE);
		scanDevicesIV.setEnabled(false);
		scanDevicesIV.setImageResource(R.anim.scan_devices_anim);
		((AnimationDrawable)scanDevicesIV.getDrawable()).start();
	}
	
	
	@Override
	public void deviceConnected() {
		udpateLastConnectedDeviceState(BTDevice.CONNECTED);
	}


	@Override
	public void deviceDisconnected() {
		udpateLastConnectedDeviceState(BTDevice.PAIRED);
	}
	
	@Override
	public void deviceReconnecting() {
		udpateLastConnectedDeviceState(BTDevice.CONNECTING);
	}

	
	@Override
	protected void hasBound(){
		updateBluetoothStatus();
	}

	protected class DeviceListAdapter extends BaseAdapter {

		public int getCount() {

			if (discoveredDevices != null) {
				return discoveredDevices.size();
			}
			return 0;
		}

		public Object getItem(int position) {
			return discoveredDevices.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout view = null;

			if (convertView == null) {
				view = new LinearLayout(BluetoothManagement.this);
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				vi.inflate(R.layout.discovered_device, view, true);
			} else {
				view = (LinearLayout) convertView;
			}

			TextView addressTV = (TextView) view.findViewById(R.id.device_address);
			TextView nameTV = (TextView) view.findViewById(R.id.device_name);
			TextView statusTV = (TextView) view.findViewById(R.id.device_status);
			
			String status;
			
			int state = discoveredDevices.get(position).state;
			if (state ==  BTDevice.PAIRED){
				status = getString(R.string.paired);
				statusTV.setTextColor(Color.rgb(180, 220, 80));
			}
			else if (state == BTDevice.CONNECTED){
				status = getString(R.string.connected);
				statusTV.setTextColor(Color.GREEN);
			}
			else if (state == BTDevice.CONNECTING) {
				status = getString(R.string.connecting);
				statusTV.setTextColor(Color.YELLOW);
			}
			else{
				status = getString(R.string.not_paired);
				statusTV.setTextColor(Color.LTGRAY);
			}

			addressTV.setText(discoveredDevices.get(position).address);
			String name = discoveredDevices.get(position).name;
			if (name == null)
				name = getString(R.string.no_name_found);
			nameTV.setText(name);
			statusTV.setText(status);

			return view;
		}
	}
	
	class ConnectorTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... addresses) {
			try {
				btService.connect(addresses[0]);
			} catch (BluetoothException e) {
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			discoveredDevices.get(lastAccessedBTDevicePosition).state = BTDevice.CONNECTING;
			devicesListAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Boolean isConnected) {
			super.onPostExecute(isConnected);
			if (isConnected) {
				deviceConnected();
			}
			else {
				Toast.makeText(BluetoothManagement.this, "Connection failed!", Toast.LENGTH_SHORT).show();
				deviceDisconnected();
			}
		}
	}

	

	


}

