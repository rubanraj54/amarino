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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import edu.mit.media.android.amarino.log.AmarinoLogger;
import edu.mit.media.android.amarino.log.LogListener;

public class Monitoring extends Activity implements LogListener {
	
	private Button monitoringBtn;
	private EditText dataToSendET;
	private Button sendBtn;
	private ScrollView logScrollView;
	private TextView logTV;
	
	private BTService btService;
	
	private Handler handler = new Handler();
	private AmarinoLogger logger;
	private boolean monitoring;
	private boolean userTouch = false;

	ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			btService = ((BTService.BTServiceBinder)service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			btService = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.monitoring_title);
		setContentView(R.layout.monitoring);
		
		bindService(new Intent(Monitoring.this, BTService.class),
        		serviceConnection, Context.BIND_AUTO_CREATE);
		
		monitoringBtn = (Button)findViewById(R.id.monitoring_btn);
		dataToSendET = (EditText)findViewById(R.id.data_to_send);
		sendBtn = (Button)findViewById(R.id.send_btn);
		logScrollView = (ScrollView)findViewById(R.id.log_scroll);
		logTV = (TextView)findViewById(R.id.log);
		logTV.setText("========== Logging Window ==========\n");
		
		monitoring = PreferenceManager.getDefaultSharedPreferences(Monitoring.this)
			.getBoolean(AmarinoLogger.KEY_IS_LOG_ENABLED, false);
		
		logger = AmarinoLogger.getInstance();
		updateMonitoringState();
		
		logScrollView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!userTouch && event.getAction() == MotionEvent.ACTION_DOWN){
					userTouch = true;
				}
				else if (userTouch && event.getAction() == MotionEvent.ACTION_UP){
					userTouch = false;
				}
				return false;
			}
		});
		
		monitoringBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				monitoring = !monitoring;
				PreferenceManager.getDefaultSharedPreferences(Monitoring.this)
					.edit()
					.putBoolean(AmarinoLogger.KEY_IS_LOG_ENABLED, monitoring)
					.commit();
				updateMonitoringState();
			}
		});
		
		sendBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (btService != null) {
					if (btService.state == BTService.CONNECTED){
						btService.sendData(dataToSendET.getEditableText().toString() + IntentEventMapper.ACK_FLAG);
					}
					else {
						Toast.makeText(Monitoring.this, "not connected", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		findViewById(R.id.clear_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clearLogClickHandler(v);
			}
		});

	}
	
	
	
	private void updateMonitoringState(){
		if (monitoring) {
			monitoringBtn.setText("Disable Monitoring");
			
			synchronized (logger) {
				logTV.append("Monitoring enabled!\n");
				logTV.append(logger.getLog());
				logScrollView.smoothScrollBy(0, logTV.getHeight());
				logger.registerLogListener(this);
			}
		}
		else {
			monitoringBtn.setText("Enable Monitoring");
			logger.unregisterLogListener(this);
			logTV.append("Monitoring disabled!\n");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		logScrollView.fullScroll(View.FOCUS_DOWN);
	}

	@Override
	protected void onStop() {
		super.onStop();
		logger.unregisterLogListener(this);
		if (btService!=null)
			unbindService(serviceConnection);
	}
	
	public void clearLogClickHandler(View target){
		logTV.setText("========== Logging Window ==========\n");
		logger.clear();
		updateMonitoringState();
	}
	
	

	@Override
	public void logChanged(final String lastAddedMsg) {

		handler.post(new Runnable() {
			
			@Override
			public void run() {
				logTV.append(lastAddedMsg + "\n");
				if (!userTouch){
					logScrollView.post(new Runnable() {
						
						@Override
						public void run() {
							logScrollView.smoothScrollBy(0, 60);
						}
					});
				}
				
			}
		});
	}

	
}
