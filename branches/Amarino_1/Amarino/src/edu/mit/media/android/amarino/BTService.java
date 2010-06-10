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
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import edu.mit.media.android.amarino.db.Collection;
import edu.mit.media.android.amarino.db.Event;
import edu.mit.media.android.amarino.log.AmarinoLogger;

/**
 * BTService is a central class in this application. When a BT device
 * is connected the service runs in the background even if the application
 * is not visible any more. BTService manages the AliveChecker to maintain
 * the connection to a connected BT device.
 * 
 * BTService can be used to connect or disconnect to BT devices.
 * You can use the BTHandler thru this service, don't instantiate a
 * BTHandler on your own. BTService is the only one with a reference to BTHandler.
 * 
 * Since BTService is involved in almost any important operation it
 * generates all Logger messages.
 * 
 * BTService instantiates a BroadcastReceiver dynamically based on the active
 * collection and its events. Furthermore this service will dynamically activate a
 * PhoneStateListener and a SensorListerer if events request them.
 * 
 * If you want to get informed about connection state changes you need to register
 * a OnConnectionChangedListener to this BTService. BTService calls connected() and 
 * disconnected() so that you can update your class state.
 *
 */
public class BTService extends Service 
				implements OnBTEventListener, OnReceivedDataListener,
							OnSharedPreferenceChangeListener{

	public static final String TAG = "BTService";
	public static final int CONNECTED = 1;
	public static final int DISCONNECTED = 2;
	public static final int RECONNECTING= 3;
	public static final int CONNECTING = 4;
	
	public static final String SEND_DATA = "edu.mit.media.android.amarino.SEND_DATA";
	
	private static final boolean DEBUG = true;
	
	private static final long ALIVE_TIME_OUT = 4000;	 // waiting time for response
	private static final long ALIVE_START_DELAY = 5000;  // start checking delayed
	
	protected BTHandler bluetoothHandler;
	protected int state = DISCONNECTED;
	
	private final IBinder binder = new BTServiceBinder();
	private Timer aliveCheckTimer = new Timer();
	private AliveChecker aliveChecker = new AliveChecker();
	boolean connectionAlive = false;

	private String response = new String();
	private AmarinoPhoneStateListener phoneStateListener;
	private boolean isPhoneStateListenerRegistered = false;
	private AmarinoSensorListener sensorListener;
	private BroadcastReceiver eventReceiver;
	private TestEventSender testEventSender;
	
	private ArrayList<OnConnectionChangedListener> connectionChangedListeners 
		= new ArrayList<OnConnectionChangedListener>(1);
	
	private AmarinoLogger logger;
	private boolean monitoring = false; // is monitoring activated or not
	boolean disconnectRequested = false; // if the user requested a disconnect
	boolean startedRemotely = false;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		if (DEBUG) Log.d(TAG, "onCreate");
		super.onCreate();
		
		// instantiate so that later listeners can be activated when needed
		phoneStateListener = new AmarinoPhoneStateListener(BTService.this);
		sensorListener = new AmarinoSensorListener(BTService.this);
		
		initBluetoothHandler();
		
		PreferenceManager.getDefaultSharedPreferences(BTService.this)
			.registerOnSharedPreferenceChangeListener(BTService.this);
		
		logger = AmarinoLogger.getInstance();  // singleton
		monitoring = PreferenceManager.getDefaultSharedPreferences(BTService.this)
						.getBoolean(AmarinoLogger.KEY_IS_LOG_ENABLED, false);
		
		BTHandler.showNotification(this, 
				getString(R.string.bluetooth_service_started_title), 
				getString(R.string.bluetooth_service_started_text),
				R.drawable.icon,
				new Intent(this, Amarino.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
				Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR,
				false);
		
		log("background service created");
	}


	private void initBluetoothHandler() {
		try {
			bluetoothHandler = new BTHandler(this);
			bluetoothHandler.addOnBTEventListener(this);
			bluetoothHandler.addOnReceivedDataListener(this);
		} catch (Exception e) {
			Toast.makeText(this,"Error while initialization of local Bluetooth device.",
					Toast.LENGTH_SHORT).show();
			log("Error while initialization of local Bluetooth device.");
			e.printStackTrace();
		}
		log("local Bluetooth device initialized");
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		//if (DEBUG) Log.d(TAG, "onStart");
		super.onStart(intent, startId);
		if (intent != null) {
			String action = intent.getAction();
			if (SEND_DATA.equals(action) && state == CONNECTED) {
				sendData(intent.getStringExtra("data"));
			}
			else if (RemoteControl.CONNECT.equals(action)){
				startedRemotely = true;
				if (state != DISCONNECTED)
					disconnect(true);
				try {
					connect(Collection.getCurrentDeviceAddress(BTService.this));
				} catch (BluetoothException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (RemoteControl.DISCONNECT.equals(action)){
				if (state != DISCONNECTED)
					disconnect(true);
				if (startedRemotely)
					this.stopSelf();
			}
			
		}
		// TODO when service has been crashed this is the point where we want to restore the state
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		stopAliveChecker();
		
		unregisterSensorListener();
		
		if (eventReceiver != null) {
			unregisterReceiver(eventReceiver);
		}
		
		if (isPhoneStateListenerRegistered)
			phoneStateListener.unregister();
		
		PreferenceManager.getDefaultSharedPreferences(BTService.this)
			.unregisterOnSharedPreferenceChangeListener(BTService.this);
		
		bluetoothHandler.close();
		BTHandler.cancelNotification(this);
		logger.clear();
		log("service stopped");
	}
	
	/**
	 * This method dynamically registers a broadcast receiver. The intent filter
	 * for it depends on which events are selected with the current collection.
	 * PhoneStateListeners and SensorListeners are registered when required.
	 */
	private void registerEventReceiver() {
		long currentCollectionId = EventManagement.getCurrentCollectionId(this);

		// only get events if there is a valid collection active
		if (currentCollectionId > -1) {
			Collection currentCollection = 
				Collection.getCollection(getContentResolver(), currentCollectionId);
			if (currentCollection != null) {
				ArrayList<Event> currentEvents = 
					Event.getEvents(getContentResolver(), currentCollection);
				
				boolean hasPhoneEvent = false;

				if (currentEvents.size() > 0) {
					IntentFilter intentFilter = new IntentFilter();
					
					for (Event e : currentEvents) {
						//if (DEBUG) Log.d(TAG, "action register request: " + e.action);
						// check if we need a PhoneStateListener
						if (!hasPhoneEvent)
							hasPhoneEvent = isPhoneStateEvent(e);
						
						// check if we need a SensorEventListener
						// sensorListeners bypass the receiver for performance reasons,
						// thus we don't need to register actions for sensor events
						if (!startSensorListenerIfNeeded(e)){
							if (DEBUG) Log.d(TAG, "action registered: " + e.action);
							log("action registered: " + e.action);
							intentFilter.addAction(e.action);
						}
						
						if (e.action.equals(IntentEventMapper.TEST_EVENT)){
							if (testEventSender == null)
								testEventSender = new TestEventSender(this);
							testEventSender.start();
						}
					}
					
					eventReceiver = new EventReceiver();
					registerReceiver(eventReceiver, intentFilter);
					
					// only when there were phone events registered, we start
					// the phoneStateListener
					if (hasPhoneEvent) {
						phoneStateListener.register();
						isPhoneStateListenerRegistered = true;
						log("phone state listener activated");
					}
					
				} // end if (currentEvents.size...
			} // end if (currentCollection...
		}
	}
	
	private boolean isPhoneStateEvent(Event e){
		if (e.action.equals(IntentEventMapper.CALL_STATE_IDLE) ||
			e.action.equals(IntentEventMapper.CALL_STATE_OFFHOOK) ||
			e.action.equals(IntentEventMapper.CALL_STATE_RINGING)) 	{
			return true;
		}
		return false;
	}
	
	private boolean startSensorListenerIfNeeded(Event e){
		if (e.action.equals(IntentEventMapper.COMPASS)){
			sensorListener.register(AmarinoSensorListener.TYPE_COMPASS);
			if (DEBUG) Log.d(TAG, "compass sensor listener activated");
			log("compass sensor listener activated");
			return true;
		}
		else if (e.action.equals(IntentEventMapper.ACCELEROMETER)){
			sensorListener.register(Sensor.TYPE_ACCELEROMETER);
			if (DEBUG) Log.d(TAG, "accelerometer sensor listener activated");
			log("accelerometer sensor listener activated");
			return true;
		}
		else if (e.action.equals(IntentEventMapper.MAGNETIC_FIELD)){
			sensorListener.register(Sensor.TYPE_MAGNETIC_FIELD);
			if (DEBUG) Log.d(TAG, "magnetic field sensor listener activated");
			log("magnetic field sensor listener activated");
			return true;
		}
		else if (e.action.equals(IntentEventMapper.ORIENTATION)){
			sensorListener.register(Sensor.TYPE_ORIENTATION);
			if (DEBUG) Log.d(TAG, "orientation sensor listener activated");
			log("orientation sensor listener activated");
			return true;
		}
		else if (e.action.equals(IntentEventMapper.TEMPRATURE)){
			sensorListener.register(Sensor.TYPE_TEMPERATURE);
			if (DEBUG) Log.d(TAG, "temp sensor listener activated");
			log("temprature sensor listener activated");
			return true;
		}
		return false;
	}
	
	/**
	 * Connect to a certain BT device, make sure this device is already paired
	 * before trying to connect to it.
	 * @param address - address of the BT device (i.e. 12:33:11:00:A5:B7)
	 * @throws BluetoothException
	 * @throws Exception
	 */
	public void connect(String address) throws BluetoothException, Exception {
		disconnectRequested = false;
		
		log("connecting to: " + address);
		bluetoothHandler.connectTo(address);
		log("connection to " + address + "established");
		
		if (state != RECONNECTING)
			registerEventReceiver();
		
		state = CONNECTED;
		notifyConnectionChangedListeners();
		reStartAliveChecker();
		
		BTHandler.showNotification(BTService.this,
				getString(R.string.bluetooth_connection_established_title), 
				getString(R.string.bluetooth_connection_established_text, address),
				R.drawable.icon,
				new Intent(this, Amarino.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
				Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR, 
				false);
	}
	
	/**
	 * Disconnect from a connected BT device.
	 * @param stopAliveChecker - if the aliveChecker should be stopped when disconnecting
	 *  set false if you just want to reconnect.
	 */
	public void disconnect(boolean stopAliveChecker){
		
		if (stopAliveChecker) {
			disconnectRequested = true;
			state = DISCONNECTED;
			stopAliveChecker();
			stopAllEventListeners();
		}
		else {
			state = RECONNECTING;
		}
		

		bluetoothHandler.disconnect();

		notifyConnectionChangedListeners();
		log("disconnected");
	}
	
	private void unregisterSensorListener(){
		sensorListener.unregister(Sensor.TYPE_ALL);
	}

	private void unregisterPhoneStateListener() {
		if (isPhoneStateListenerRegistered) {
			phoneStateListener.unregister();
			isPhoneStateListenerRegistered = false;
		}
	}
	private void stopAllEventListeners(){
		// stop test event sender if it is running
		if (testEventSender != null && testEventSender.started)
			testEventSender.stop();
		unregisterSensorListener();
		unregisterPhoneStateListener();
		
		if (eventReceiver != null) {
			unregisterReceiver(eventReceiver);
			eventReceiver = null;
		}
		log("unregistered all actions");
	}
	
	/**
	 * Use this method to send data. You could bypass this by
	 * directly sending to BTHandler, but then you don't get logging messages
	 * @param data - data you want to send (don't send more than the buffer of
	 *  the Arduino library can get (currently 64 byte raw data)
	 */
	public void sendData(String data){
		log("send data: " + data.substring(0, data.length()-1));
		bluetoothHandler.sendData(data.getBytes());
	}
	
	/**
	 * Call this method if you want to update the EventReceiver. For example
	 * if the collection has changed or events have been added or removed.
	 */
	public void updateEventReceiver(){
		if (state == CONNECTED){
			if (eventReceiver != null) {
				if (DEBUG) Log.d(TAG, "unregister event receiver");
				stopAllEventListeners();
				eventReceiver = null;
			}
			registerEventReceiver();
		}
	}
	
	/**
	 * This stops the AliveChecker
	 */
	public void stopAliveChecker(){
		aliveChecker.cancel();
		aliveCheckTimer.cancel();
		log("alive checker stopped");
	}

	/**
	 * Stop and restart the AliveChecker
	 */
	public void reStartAliveChecker() {
		long period = BTPreferences.getAlivePeriod(this);
		if (period > 0){
			aliveCheckTimer.cancel();
			aliveChecker.cancel();
			aliveCheckTimer = new Timer();
			aliveChecker = new AliveChecker();
			aliveCheckTimer.schedule(aliveChecker, ALIVE_START_DELAY, period);
			log("alive checker restarted");
		}
	}
	
	private void log(String msg){
		if (monitoring) {
			logger.add(msg);
		}
	}
	
	public void registerOnConnectionChangedListener(OnConnectionChangedListener l){
		connectionChangedListeners.add(l);
	}
	
	public void unregisterOnConnectionChangedListener(OnConnectionChangedListener l){
		connectionChangedListeners.remove(l);
	}
	
	public void updateOnConnectionChangedListener(OnConnectionChangedListener l){
		switch (state){
			case CONNECTED: l.deviceConnected(); break;
			case DISCONNECTED: l.deviceDisconnected(); break;
			case RECONNECTING: l.deviceReconnecting(); break;
		}
	}
	
	private void notifyConnectionChangedListeners(){
		for(OnConnectionChangedListener l : connectionChangedListeners){
			updateOnConnectionChangedListener(l);
		}
	}


	public class BTServiceBinder extends Binder {
		BTService getService() {
			return BTService.this;
		}
	}
	
	
	
	/* ------------- Interface implementations -----------------*/
	
	@Override
	public void bluetoothDisabled() {
		if (DEBUG) Log.d(TAG, "bluetoothDisabled");
		log("bluetooth disabled");
	}

	@Override
	public void bluetoothEnabled() {
		if (DEBUG) Log.d(TAG, "bluetothDisabled");
		log("bluetooth enabled");
	}

	@Override
	public void scanCompleted(ArrayList<String> devs) {
		if (DEBUG) Log.d(TAG, "scanCompleted");
		log("scan for devices completed");
	}
	
	@Override
	public void deviceFound(String address) {
		if (DEBUG) Log.d(TAG, "deviceFound: " + address);
		log("device found: " + address);
	}

	@Override
	public void scanStarted() {
		if (DEBUG) Log.d(TAG, "scanStarted");
		log("scan for devices started");
	}

	@Override
	public void paired() {
		if (DEBUG) Log.d(TAG, "paired");
		log("paired");
	}

	@Override
	public void pinRequested() {
		if (DEBUG) Log.d(TAG, "pinRequested");
		log("pin requested");
	}
	
	@Override
	public void gotServiceChannel(int serviceID, int channel) {
		if (DEBUG) Log.d(TAG, "gotServiceChannel");
	}

	@Override
	public void serviceChannelNotAvailable(int serviceID) {
		if (DEBUG) Log.d(TAG, "serviceChannelNotAvailable");
	}
	

	/**
	 * Callback method used by BTHandler to inform the Service when data has been received.
	 */
	@Override
	public void receivedData(byte[] b) {
		connectionAlive = true;
		String data = new String(b);

		// we collect all received data, so put it into a buffer and check afterwards if
		// our expected message is somewhere in the buffer
		response += data;

		//Log.d(TAG, "new response is: " + response);
	}
	
	@Override
	public void forwardData(String data) {
		log("forward received data: " + data);
	}

	/**
	 * Callback used by BTPreferences to inform service about settings changes.
	 * Service needs to know when settings have been changed to change alive 
	 * checker period or monitoring state during runtime.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals(BTPreferences.KEY_ALIVE_PERIOD)){
			if (DEBUG) Log.d(TAG, "restart alive checker");
			log("period for alive checking changed");
			reStartAliveChecker();
		}
		else if (key.equals(AmarinoLogger.KEY_IS_LOG_ENABLED)){
			monitoring = prefs.getBoolean(key, false);
		}
		else if (key.equals(BTPreferences.KEY_STAY_CONNECTED) && BTPreferences.isStayConnectedSelected(this)){
			Toast.makeText(BTService.this, "this feature is not implemented yet", Toast.LENGTH_SHORT).show();
			BTPreferences.setStayConnected(this, true);
		}
	}
	
	
	/**
	 * The AliveChecker keeps track if the connection is still alive or not.
	 * If the connection is broken it tries to reconnect.
	 * 
	 * To check for connection AliveChecker sends periodically a character
	 * '@' to Arduino and awaits an echo back. Arduino has some seconds time
	 * to respond before AliveChecker calls the connection as lost.
	 * AliveChecker search in the respond buffer for an '@' Symbol. If there is
	 * one the connection is considered alive.
	 */
	private class AliveChecker extends TimerTask {

		boolean lostConnection = false;
		
		@Override
		public void run() {
			response = ""; // clear response buffer before sending alive request
			
			// to increase performance we send the alive message directly by ourselves instead of
			// broadcasting an intent. Nevertheless this breaks the overall design and should
			// be considered carefully, since we add complexity to the application!
			
			//sendBroadcast(new Intent(IntentEventConverter.ALIVE));  // commented for performance reasons
			
			if (connectionAlive){
				connectionAlive = false; // set it dirty
			}
			else {
				log("check connection by sending an alive request " + IntentEventMapper.ALIVE_FLAG);
				sendData(IntentEventMapper.ALIVE_MSG); //optimized version
				
				try {
					// allow some time to respond
					Thread.sleep(ALIVE_TIME_OUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// since this thread sleeps awhile, we need to check if the user hasn't
				// disconnected in between, if we wouldn't do that thread would try to reconnect
				if (!disconnectRequested){
					// check if buffer contains our alive message
					if (response.contains(String.valueOf(IntentEventMapper.ALIVE_FLAG))){
						log("connection is alive");
						// show notification only the first time after reconnect
						if (lostConnection) {
							BTHandler.showNotification(
								BTService.this, 
								getString(R.string.bluetooth_connection_established_title),
								getString(R.string.bluetooth_connection_established_text, BTPreferences.getLastConnectedAddress(BTService.this)),
								R.drawable.icon,
								new Intent(BTService.this, Amarino.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
								Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR,
								false);
							lostConnection = false;
						}
					}
					else {
						// show that we lost connection
						if (DEBUG) Log.d(TAG, "We lost connection, try to reconnect!");
						log("alive check failed, got no proper response from bluetooth device");
						
						// show notification only the first time we lost connection
						if (!lostConnection) {
							BTHandler.showNotification(
									BTService.this, 
									getString(R.string.bluetooth_lost_connection),
									getString(R.string.bluetooth_reconnect, BTPreferences.getLastConnectedAddress(BTService.this)),
									R.drawable.icon,
									new Intent(BTService.this, Amarino.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
									Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR,
									false);
							lostConnection = true;
							disconnect(false);
						}
						
						// try to reconnect
						reconnect();
					}
				}
			}
			
			// since the response buffer has been checked we can clear it now
			response = "";
		}
		
		private void reconnect(){
			state = RECONNECTING;
			notifyConnectionChangedListeners();
			bluetoothHandler.close();
			try {
				log("try to reconnect...");
				bluetoothHandler.init();
				connect(BTPreferences.getLastConnectedAddress(BTService.this));
			} catch (Exception e) {
				if (DEBUG) Log.d(TAG, "no connection");
				log("reconnect was not successful");
			}
		}
	}

	
}
