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
import it.gerdavax.android.bluetooth.BluetoothSocket;
import it.gerdavax.android.bluetooth.LocalBluetoothDevice;
import it.gerdavax.android.bluetooth.LocalBluetoothDeviceListener;
import it.gerdavax.android.bluetooth.RemoteBluetoothDevice;
import it.gerdavax.android.bluetooth.RemoteBluetoothDeviceListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.mit.media.android.amarino.log.AmarinoLogger;

public class BTHandler implements LocalBluetoothDeviceListener, 
								RemoteBluetoothDeviceListener {
	
	public static final String ACTION_RESPONSE = "amarino.RESPONSE";
	public static final int NOTIFY_ID = 151619;
	private final String TAG = "BTHandler";
	
	private boolean DEBUG = false;
	
	private Context context;
	private ArrayList<OnBTEventListener> onBTEventListeners;
	private ArrayList<OnReceivedDataListener> onReceivedDataListeners;
	
	private LocalBluetoothDevice localBluetoothDevice;
	private RemoteBluetoothDevice remoteBluetoothDevice;
	private BluetoothSocket bluetoothSocket;
	private BTSenderThread sender;
	private BTReceiverThread receiver;
	private InputStream input;
	private OutputStream output;
	private int port = 1; // always 1 with Arduino BT
	
	public BTHandler(Context context) throws Exception{
		this.context = context;
		onBTEventListeners = new ArrayList<OnBTEventListener>();
		onReceivedDataListeners = new ArrayList<OnReceivedDataListener>();
		init();
	}
	
	/*
	 * Initialization of local Bluetooth. 
	 * 
	 * This method is called within the constructor once, you only need to call it again
	 * if you want to reinitialize your local Bluetooth device
	 */
	public void init() throws Exception{
		if (DEBUG) Log.d(TAG, "Init Bluetooth");
		localBluetoothDevice = LocalBluetoothDevice.initLocalDevice(context);
		localBluetoothDevice.setListener(this);
	}
	
	/*
	 * Closes the local Bluetooth device.
	 * 
	 * This also cancels sender and receiver threads
	 * and unregisters all attached listeners. 
	 * 
	 */
	public void close(){
		if (DEBUG) Log.d(TAG, "Free all Bluetooth resources");

		closeThreads();
		localBluetoothDevice.close();
	}

	private void closeThreads() {
		if (sender != null)
			sender.finish();
		if (receiver != null)
			receiver.finish();
	}


	/*
	 * Returns true if Bluetooth is enabled.
	 */
	public boolean isBluetoothEnabled() throws Exception{
		return localBluetoothDevice.isEnabled();
	}
	
	/*
	 * Enables or disables Bluetooth
	 * 
	 * @return boolean - true if operation was successful, otherwise false
	 */
	public boolean setBluetoothEnabled(boolean enable){
		try {
			if (localBluetoothDevice != null) {
				localBluetoothDevice.setEnabled(enable);
				return true;
			}
		} catch (Exception e) {
			if (DEBUG) Log.e(TAG, "Bluetooth could not be enabled!");
			e.printStackTrace();
		}
		return false;
	}
	

	public void scanForDevices(){
		if (DEBUG) Log.d(TAG, "Start scanning for devices...");
		
		try {
			if (localBluetoothDevice != null && localBluetoothDevice.isEnabled()){
				localBluetoothDevice.scan();
			}
		} catch (Exception e) {
			Log.e(TAG, "Error while scanning for devices.");
			e.printStackTrace();
		}
	}
	
	/*
	 * Pairs your device with a remote device given by its address.
	 * If a pin is requested a notification message will appear in the
	 * notification bar. The notification message also provieds a dialog
	 * to enter the pin.
	 *  
	 * @param address - remote device address, i.e. "00:34:11:E5:2E:33"
	 */
	public void pairDevices(String address){
		if (DEBUG) Log.d(TAG, "pairing...");

		remoteBluetoothDevice = localBluetoothDevice.getRemoteBluetoothDevice(address);
		remoteBluetoothDevice.setListener(this);
		remoteBluetoothDevice.pair();
	}
	
	
	public void unpair(String address) {
		localBluetoothDevice.getRemoteBluetoothDevice(address).unpair();
	}
	
	/**
	 * Connects to the given address, but make sure device is paired by calling isPaired(address)
	 * before calling this method.#
	 * 
	 * @param address - address of a paired device
	 * @throws Exception
	 * @throws BluetoothException
	 */
	public void connectTo(String address) throws BluetoothException, Exception {
		remoteBluetoothDevice = localBluetoothDevice.getRemoteBluetoothDevice(address);
		remoteBluetoothDevice.setListener(this);

		bluetoothSocket = remoteBluetoothDevice.openSocket(port);
		output = bluetoothSocket.getOutputStream();
		input = bluetoothSocket.getInputStream();

		startThreads();
		
		// save last address of connected device
		BTPreferences.setLastConnectedAddress(context, address);
	}
	
	public void disconnect(){
		closeThreads();
		if (bluetoothSocket != null){
			bluetoothSocket.closeSocket();
		}
	}

	private void startThreads() {
		closeThreads();
		receiver = new BTReceiverThread();
		receiver.start();
		
		sender = new BTSenderThread();
		sender.start();
	}
	
	/*
	 * Returns if the remote Bluetooth device with the given address
	 * is paired with your local device
	 * 
	 * @param address - the address of the remote Bluetooth device
	 * @return - true if paired, otherwise false
	 */
	public boolean isPaired(String address){
		return localBluetoothDevice.getRemoteBluetoothDevice(address).isPaired();
	}
	
	public String getRemoteName(String address){
		try {
			return localBluetoothDevice.getRemoteBluetoothDevice(address).getName();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getRemoteClass(String address){
		try {
			return localBluetoothDevice.getRemoteBluetoothDevice(address).getDeviceClass();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public short getRSSI(){
		if (remoteBluetoothDevice != null)
			return remoteBluetoothDevice.getRSSI();
		else
			return -1;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
	public void sendData(byte[] data){
		if (sender != null){
			sender.sendData(data);
		}
	}
	
	public void setDebug(boolean value){
		DEBUG = value;
	}
	

	public void addOnBTEventListener(OnBTEventListener listener){
		onBTEventListeners.add(listener);
	}
	
	public boolean removeOnBTEventListener(OnBTEventListener listener){
		return onBTEventListeners.remove(listener);
	}
	
	public void addOnReceivedDataListener(OnReceivedDataListener listener){
		onReceivedDataListeners.add(listener);
	}
	
	public boolean removeOnReceivedDataListener(OnReceivedDataListener listener){
		return onReceivedDataListeners.remove(listener);
	}

	@Override
	public void bluetoothDisabled() {
		if (DEBUG) Log.d(TAG, "disabled");
		for (OnBTEventListener l : this.onBTEventListeners){ l.bluetoothDisabled(); }
	}

	@Override
	public void bluetoothEnabled() {
		if (DEBUG) Log.d(TAG, "enabled");
		for (OnBTEventListener l : this.onBTEventListeners){ l.bluetoothEnabled(); }
	}

	@Override
	public void scanCompleted(ArrayList<String> devs) {
		if (DEBUG) Log.d(TAG, "scanCompleted - found " + devs.size() + " devices");
		for (OnBTEventListener l : this.onBTEventListeners){ l.scanCompleted(devs); }
		
	}
	
	@Override
	public void deviceFound(String address) {
		if (DEBUG) Log.d(TAG, "deviceFound: " + address);
		for (OnBTEventListener l : this.onBTEventListeners){ l.deviceFound(address); }
	}	

	@Override
	public void scanStarted() {
		if (DEBUG) Log.d(TAG, "scanStarted");
		for (OnBTEventListener l : this.onBTEventListeners){ l.scanStarted(); }
	}

	@Override
	public void paired() {
		if (DEBUG) Log.d(TAG, "paired");
		// inform all others, we found a new friend
		for (OnBTEventListener l : this.onBTEventListeners){ l.paired(); }
	}
	
	@Override
	public void pinRequested() {
		if (DEBUG) Log.d(TAG, "pinRequested");
		for (OnBTEventListener l : this.onBTEventListeners){ l.pinRequested(); }
	}
	
	@Override
	public void gotServiceChannel(int serviceID, int channel) {
		if (DEBUG) Log.d(TAG, "gotServiceChannel - serviceID: " + serviceID+ " channel: " + channel);
		for (OnBTEventListener l : this.onBTEventListeners){ l.gotServiceChannel(serviceID, channel); }
	}

	@Override
	public void serviceChannelNotAvailable(int serviceID) {
		if (DEBUG) Log.d(TAG, "serviceChannelNotAvailable - serviceID: " + serviceID);
		for (OnBTEventListener l : this.onBTEventListeners){ l.serviceChannelNotAvailable(serviceID); }
	}

	
	class BTSenderThread extends Thread {
		
		public static final String TAG = "BTSenderThread";
		public boolean stopped = false;
		private byte[] data;
		private int i = 0;

		@Override
		public void run() {
			super.run();
			if (DEBUG) Log.d(TAG, "Sender started (" + this.getName()+ ")");

			while (!stopped){
				try {
					synchronized (this) {
						// wake me if new data to send is available
						this.wait();
					}

					if (data != null){
						// send the byte array in portions
						if (DEBUG) Log.d(TAG, "Sending data: " + new String(data));
						for (i=0;i<data.length;i++){
							output.write(data[i]);
						}
						data = null;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(TAG, "No connection to Bluetooth device");
				}
			}
			
			Log.d(TAG, "Sender stopped (" + this.getName()+ ")");
		}
		
		public synchronized void sendData(byte[] data){
			this.data = data;
			this.notify();
		}
		
		public synchronized void finish(){
			stopped = true;
			data = null;
			this.notify();
		}

	}
	
	class BTReceiverThread extends Thread {

		public static final String TAG = "BTReceiverThread";
		private boolean stopped = false;
		private byte[] buffer = new byte[1024];
		private int numBytes;
		String cleanedData = new String();
		int pos = 0;		
		private StringBuffer forwardBuffer = new StringBuffer();
		AmarinoLogger log = AmarinoLogger.getInstance();
		
		
		@Override
		public void run() {
			super.run();
			if (DEBUG) Log.d(TAG, "Receiver started (" + BTReceiverThread.this.getName()+ ")");
			try {

				while (!stopped && (numBytes = input.read(buffer)) != -1){
					
					String incomingData = new String(buffer, 0, numBytes);
					if (DEBUG) Log.d(TAG, "received: " + incomingData);

					notifiyListeners(incomingData, false);
					
					if (incomingData.equals(IntentEventMapper.ALIVE_FLAG)){
						// just do nothing, this was our alive request echo
					}
					else if (incomingData.contains(String.valueOf(IntentEventMapper.ALIVE_FLAG))){
						// delete the ALIVE_FLAG
						pos = incomingData.indexOf(IntentEventMapper.ALIVE_FLAG);
						cleanedData = incomingData.substring(0, pos);
						cleanedData += incomingData.substring(pos+1, incomingData.length());
						forwardData(cleanedData);
					}
					else {
						forwardData(incomingData);
					}
					
				}

			} catch (IOException e) {
				e.printStackTrace();
			} 
			if (DEBUG) Log.d(TAG, "Receiver stopped (" + BTReceiverThread.this.getName()+ ")");
		}
		
		public void finish(){
			stopped = true;
		}
		
		private synchronized void notifiyListeners(String data, boolean forward){
			for (OnReceivedDataListener l : onReceivedDataListeners) {
				if (forward)
					l.forwardData(data);
				else
				l.receivedData(data.getBytes());
			}
		}
		

		private void forwardData(String data){
			
			char c;
			for (int i=0;i<data.length();i++){
				c = data.charAt(i);
				if (c == IntentEventMapper.ACK_FLAG){
					sendIntentWithData(forwardBuffer.toString());
				}
				else if (c == IntentEventMapper.ARDUINO_MSG_FLAG){
					forwardBuffer = new StringBuffer();
				}
				else {
					forwardBuffer.append(c);
				}
			}
		}
		
		private void sendIntentWithData(String data){
			Intent i = new Intent(ACTION_RESPONSE);
			i.putExtra("data", data);
			if (DEBUG) Log.d(TAG, "forward data: " + data);
			context.sendBroadcast(i);
			notifiyListeners(data, true);
		}
		
	}
	
	
	
	protected static void showNotification(Context context, String title,
			String text, int icon, Intent intent, int flag, boolean isService) {
		
		NotificationManager notifyManager = 
			(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent launchIntent;
		if (isService)
			launchIntent = PendingIntent.getService(context, 0, intent, 0);
		else
			launchIntent = PendingIntent.getActivity(context, 0, intent, 0);
		Notification notification = new Notification(icon, title, System.currentTimeMillis());
		notification.flags |= flag;
		notification.setLatestEventInfo(context, title, text, launchIntent);
		notifyManager.notify(NOTIFY_ID, notification);
	}
	
	protected static void cancelNotification(Context context){
		NotificationManager notifyManager = 
			(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancel(NOTIFY_ID);
	}

}
