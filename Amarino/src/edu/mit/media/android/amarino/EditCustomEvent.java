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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.mit.media.android.amarino.db.Event;
import edu.mit.media.android.amarino.db.EventData;

public class EditCustomEvent extends Activity {
	
	private final static String TAG = "EditCustomEvent";
	private final static int STATE_EDIT = 1;
	private final static int STATE_INSERT = 2;
	private final static int STATE_VIEW = 3;
	
	private final static int INPUT_OK = 0;
	private final static int INPUT_NAME_ERROR = 2;
	private final static int INPUT_ACTION_ERROR = 4;
	private final static int INPUT_DESC_ERROR = 8;
	private final static int INPUT_FLAG_ERROR = 16;
	private final static int INPUT_DATA_ERROR = 32;
	
	int state;
	Event event;
	int selectedFlag = -1;
	String[] flags;
	ArrayList<DataEntryView> dataEntryViews = new ArrayList<DataEntryView>();
	boolean dirty = false;
	BTService btService;
	
	final int disabledTextColor = Color.LTGRAY;
	final int disabledBackgroundColor = Color.parseColor("#333333");

	
	EditText eventNameET;
	EditText eventDescET;
	EditText actionET;
	Button selectFlagBtn;
	ImageButton addDataBtn;
	ViewGroup dataContainer;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_custom_event);
		flags = getAvailableFlags();
		findViews();

		setOnClickListener();
		
		// Resolve the intent
        Intent intent = getIntent();
        String action = intent.getAction();
        
        if (action.equals(Intent.ACTION_VIEW)){
        	state = STATE_VIEW;
        	fetchEvent(intent);
        	adjustViews();
        	
        	setTitle("Event Details");
        }
        else if (action.equals(Intent.ACTION_EDIT)){
        	state = STATE_EDIT;
        	fetchEvent(intent);
        	setTitle(R.string.edit_event_title);
        }
        else if(action.equals(Intent.ACTION_INSERT)){
        	state = STATE_INSERT;
        	event = new Event();
        	setTitle(R.string.create_event_title);
        }
        else {
        	Log.e(TAG, "Cannot resolve intent: " + intent);
            finish();
            return;
        }
	}
	
	private void fetchEvent(Intent intent){
		Uri eventUri = intent.getData();
    	if (eventUri == null) {
    		Log.e(TAG, "no uri data found: " + intent);
            finish();
            return;
    	}
    	event = Event.getEvent(getContentResolver(), eventUri);
    	event.data = Event.getEventData(getContentResolver(), event);
    	eventNameET.setText(event.name);
    	eventDescET.setText(event.desc);
    	actionET.setText(event.action);
    	setFlagBtnText(event.flag);
    	
    	if (event.data != null && event.data.size() > 0) {
    		for (EventData ed : event.data) {
    			addDataEntryView(ed);
    		}
    	}

	}
	
	private void adjustViews(){
		if (state == STATE_VIEW){
			findViewById(R.id.button_panel).setVisibility(View.GONE);
			//hide edit text for desciption and show textview
			TextView desc = (TextView)findViewById(R.id.event_desc_data_text);
			desc.setTextColor(disabledTextColor);
			desc.setBackgroundColor(disabledBackgroundColor);
			desc.setText(event.desc);
			eventDescET.setVisibility(View.GONE);
			
			eventNameET.setEnabled(false);
			//eventDescET.setEnabled(false);
			actionET.setEnabled(false);
			selectFlagBtn.setEnabled(false);
			addDataBtn.setVisibility(View.GONE);
			
			// skip the data section if no data are attached
			if (event.data == null || event.data.size() < 1)
				findViewById(R.id.data_section).setVisibility(View.GONE);
			
			eventNameET.setTextColor(disabledTextColor);
			//eventDescET.setTextColor(disabledTextColor);
			actionET.setTextColor(disabledTextColor);
			selectFlagBtn.setTextColor(disabledTextColor);
			
			eventNameET.setBackgroundColor(disabledBackgroundColor);
			//eventDescET.setBackgroundColor(disabledBackgroundColor);
			actionET.setBackgroundColor(disabledBackgroundColor);
			selectFlagBtn.setBackgroundColor(disabledBackgroundColor);
		}
		else {
			findViewById(R.id.event_desc_data_text).setVisibility(View.GONE);
		}
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		dirty = false;
		bindService(new Intent(EditCustomEvent.this, BTService.class),
        		serviceConnection, Context.BIND_AUTO_CREATE);
	}


	@Override
	protected void onStop() {
		super.onStop();

		if (dirty) {
			if (btService != null) {
				btService.updateEventReceiver();
			}
		}
		if (btService != null)
			unbindService(serviceConnection);
	}


	private String[] getAvailableFlags(){
		final int allFlagsSize = 26;
		String[] allFlags = new String[allFlagsSize];
		for (int i=0; i<allFlagsSize; i++){
			// generate an array of all possible flags for custom events
			allFlags[i] = String.valueOf((char) (97+i));
		}
		return allFlags;
	}

//	private String[] getAvailableFlags(){
//		final int allFlagsSize = 26;
//		char[] allFlags = new char[allFlagsSize];
//		char[] usedFlags = Event.getEventFlags(getContentResolver());
//		
//		// flags from 'a' to 'z' (97-122) are reserved for custom events
//		for (int i=0; i<allFlagsSize; i++){
//			// generate an array of all possible flags for custom events
//			allFlags[i] = (char) (97+i);
//		}
//		
//		// subtract usedFlags from all flags and return the result
//		final int resultSize = allFlagsSize - usedFlags.length;
//		String[] result = new String[resultSize];
//		
//		int j = 0;
//		for (int i=0;i<allFlagsSize;i++){
//			if (!containsFlag(usedFlags, allFlags[i])){
//				result[j++] = String.valueOf(allFlags[i]); 
//			}
//		}
//		return result;
//	}
	
	private boolean containsFlag(char[] set, char c){
		for (int i=0;i<set.length;i++){
			if (set[i] == c) return true;
		}
		return false;
	}

	private void findViews(){
        eventNameET = (EditText)findViewById(R.id.event_name_data);
    	eventDescET = (EditText)findViewById(R.id.event_desc_data);
    	actionET = (EditText)findViewById(R.id.action_data);
    	selectFlagBtn = (Button)findViewById(R.id.select_flag_btn);
        addDataBtn = (ImageButton)findViewById(R.id.add_data);
        dataContainer = (ViewGroup)findViewById(R.id.data_container);
	}
	
	private void setOnClickListener(){
		selectFlagBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(EditCustomEvent.this)
                .setItems(flags, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setFlagBtnText(flags[which].charAt(0));
					}
				})
                .setTitle(R.string.select_flag)
                .show();
			}
		});
		
		
		addDataBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addDataEntryView(null);
			}
		});
		
		findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doSaveAction();
			}
		});
		
		findViewById(R.id.discard_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
	}
	
	
	private void setFlagBtnText(char c){
		selectedFlag = Integer.valueOf(c);
		selectFlagBtn.setText(
				getString(R.string.ascii_label, "'" + c + "'\n") +
				getString(R.string.byte_label, selectedFlag));
	}
	
	private DataEntryView addDataEntryView(EventData ed) {
		final DataEntryView dataEntryView = new DataEntryView(EditCustomEvent.this, ed);
		dataEntryView.minusIB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dataContainer.removeView(dataEntryView);
				dataContainer.postInvalidate();
			}
		});
		if (state == STATE_VIEW){
			dataEntryView.minusIB.setVisibility(View.GONE);
			dataEntryView.keyET.setEnabled(false);
			dataEntryView.typeBtn.setEnabled(false);
			
			dataEntryView.keyET.setTextColor(disabledTextColor);
			dataEntryView.typeBtn.setTextColor(disabledTextColor);
			
			dataEntryView.keyET.setBackgroundColor(disabledBackgroundColor);
			dataEntryView.typeBtn.setBackgroundColor(disabledBackgroundColor);
		}
		dataContainer.addView(dataEntryView);
		return dataEntryView;
	}
	
	private void doSaveAction() {
		int errorID = isValidInput();
		if (errorID == INPUT_OK) {
			event.name = eventNameET.getText().toString();
			event.action = actionET.getText().toString();
			event.desc = eventDescET.getText().toString();
			event.type = Event.CUSTOM_EVENT;
			event.flag = (char)selectedFlag;
			if (dataContainer.getChildCount() > 0){
				event.data = new ArrayList<EventData>(dataContainer.getChildCount());
				for (int i=0; i<dataContainer.getChildCount(); i++){
					EventData ed = ((DataEntryView)dataContainer.getChildAt(i)).getEventData();
					event.data.add(ed);
				}
			}
	        // Save or create the event
			
	        switch (state) {
	            case STATE_EDIT:
	            	Event.updateEvent(getContentResolver(), event);
	            	if (Event.isEventInCollection(getContentResolver(),
	            			EventManagement.getCurrentCollectionId(this),
	            			event.id)) {
	            		dirty = true;
	            	}
	            	break;
	            case STATE_INSERT: 
	            	Event.insertEvent(getContentResolver(), event);
	            	break;
	        }
	        
	        setResult(Activity.RESULT_OK);
	        finish();
		}
		else {
			// display a dialog message to indicate mandatory input
			String msg = "Input missing for following data fields: \n\n";
			if ((errorID & INPUT_NAME_ERROR) == INPUT_NAME_ERROR)
				msg += " - " + getString(R.string.name_label) + "\n";
			if ((errorID & INPUT_DESC_ERROR) == INPUT_DESC_ERROR)
				msg += " - " + getString(R.string.desc_label) + "\n";
			if ((errorID & INPUT_ACTION_ERROR) == INPUT_ACTION_ERROR)
				msg += " - " + getString(R.string.action_label) + "\n";
			if ((errorID & INPUT_FLAG_ERROR) == INPUT_FLAG_ERROR)
				msg += " - " + getString(R.string.flag_label) + "\n";
			if ((errorID & INPUT_DATA_ERROR) == INPUT_DATA_ERROR)
				msg += " - " + getString(R.string.data_label) + "\n";
			
			new AlertDialog.Builder(this)
				.setPositiveButton(R.string.ok, null)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Need more data")
				.setMessage(msg)
				.create()
				.show();
			
		}
    }
	
	private int isValidInput(){
		int errorCode = INPUT_OK;
		if (eventNameET.getText().length() < 1) errorCode |= INPUT_NAME_ERROR;
		if (actionET.getText().length() < 1) errorCode |= INPUT_ACTION_ERROR;
		if (eventDescET.getText().length() < 1) errorCode |= INPUT_DESC_ERROR;
		if (selectedFlag < 0) errorCode |= INPUT_FLAG_ERROR;

		// if data entries are selected
		if (dataContainer.getChildCount() > 0){
			for (int i=0; i<dataContainer.getChildCount(); i++){
				DataEntryView dev = (DataEntryView)dataContainer.getChildAt(i);
				if (!dev.isValid()) {
					errorCode |= INPUT_DATA_ERROR;
					break;
				}
			}
		}
		return errorCode;
	}
	
	protected ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			btService = ((BTService.BTServiceBinder)service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			btService = null;
		}
	};
	
	private static class DataEntryView extends LinearLayout {

		Button typeBtn;
		EditText keyET;
		ImageButton minusIB;
		Context context;
		
		EventData eventData;
		
		public DataEntryView(Context context, EventData eventData) {
			super(context);
			this.eventData = eventData;
			init(context);
		}
		public DataEntryView(Context context, AttributeSet attrs, EventData eventData) {
			super(context, attrs);
			this.eventData = eventData;
			init(context);
		}

		private void init(Context context){
			this.context = context;
			
			View v = inflate(context, R.layout.data_entry, this);
			
			keyET = (EditText)v.findViewById(R.id.key_data);
			typeBtn = (Button)v.findViewById(R.id.select_type_btn);
			minusIB = (ImageButton)v.findViewById(R.id.minus_data);
			
			if (eventData != null){
				typeBtn.setText(EventData.getTypeAsString(eventData.type));
				keyET.setText(eventData.key);
			}
			
			typeBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(DataEntryView.this.context)
	                .setItems(EventData.types, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							typeBtn.setText(EventData.types[which]);
						}
					})
	                .setTitle(R.string.type)
	                .show();
				}
			});
			
		}
		
		public EventData getEventData(){
			if (eventData == null)
				eventData = new EventData();
			eventData.key = keyET.getText().toString();
			eventData.type = EventData.getTypeAsInteger(typeBtn.getText().toString());
			return eventData;
		}
		
		public boolean isValid(){
			if (keyET.getText().length() > 0 && 
				!typeBtn.getText().toString().equals(context.getString(R.string.type))){
				return true;
			}
			return false;
		}
		
	}
	

	
}
