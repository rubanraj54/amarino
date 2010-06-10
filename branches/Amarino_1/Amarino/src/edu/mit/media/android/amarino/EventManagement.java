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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.mit.media.android.amarino.db.Collection;
import edu.mit.media.android.amarino.db.Event;


public class EventManagement extends ListActivity implements View.OnClickListener{

	public static final int MENU_ITEM_REMOVE_EVENT = 1;
	public static final int CREATE = 2;
	public static final int CHANGE = 3;
	
	private static final String TAG = "EventManagement";
	
	protected static final String PREF_CURRENT_COLLECTION = "edu.mit.media.android.amarino.current_collection_id";
	
	TextView collectionTV;
	TextView deviceAddressTV;
	
	EventListAdapter collectionEventsListAdapter;
	EventListAdapter allEventsListAdapter;
	
	ArrayList<Collection> collections;
	ArrayList<Event> events = new ArrayList<Event>();
	
	Collection currentCollection = null;
	boolean dirty = false;
	
	Handler handler = new Handler();
	BTService btService;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.event_management_title);
		setContentView(R.layout.event_management);
		
		findViews();
		
		collections = Collection.getCollections(getContentResolver());
		currentCollection = getCollectionById(getCurrentCollectionId(this));
		
		//Log.d(TAG, "id: " + currentCollection.id);
		
		initEventLists();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent(EventManagement.this, BTService.class),
        		serviceConnection, Context.BIND_AUTO_CREATE);
		dirty = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		allEventsListAdapter.eventEntries = Event.getEvents(getContentResolver(), false);
		allEventsListAdapter.notifyDataSetChanged();
		refreshCollection();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
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



	public static long getCurrentCollectionId(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREF_CURRENT_COLLECTION, -1);
	}


	private void findViews(){
		findViewById(R.id.create_collection_btn).setOnClickListener(this);
		findViewById(R.id.change_collection_btn).setOnClickListener(this);
		findViewById(R.id.delete_collection_btn).setOnClickListener(this);
		findViewById(R.id.add_event).setOnClickListener(this);
		findViewById(R.id.create_event).setOnClickListener(this);
		collectionTV = (TextView)findViewById(R.id.collection_tv);
		collectionTV.setOnClickListener(this);
		deviceAddressTV = (TextView)findViewById(R.id.device_address_tv);
		deviceAddressTV.setOnClickListener(this);
		
	}
	
 
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.create_collection_btn:
			onCreateCollectionClick(v);
			break;
		case R.id.change_collection_btn:
			onChangeCollectionClick(v);
			break;
		case R.id.delete_collection_btn:
			onDeleteCollectionClick(v);
			break;
		case R.id.add_event:
			onAddEventClick(v);
			break;
		case R.id.create_event:
			onCreateCustomEventClick(v);
			break;
		case R.id.collection_tv:
		case R.id.device_address_tv:
			onCollectionNameClick(v);
			break;
		}
		
	}

	
	private void initEventLists() {
		allEventsListAdapter = new EventListAdapter(this, Event.getEvents(getContentResolver(), false));
		collectionEventsListAdapter = new EventListAdapter(this, events);
		setListAdapter(collectionEventsListAdapter);
		
		// Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
	}
	
	public void onCollectionNameClick(View target){
		if (currentCollection != null)
			showCreateCollectionDialog(R.string.change, R.string.change, CHANGE);
	
	}

	public void onAddEventClick(View target){
		if (collections.size() == 0) {
			showCreateCollectionDialog(
					R.string.create_collection, R.string.create_collection_msg, CREATE);
		}
		else {
			showAddEventDialog();
		}
	}
	
	public void onCreateCustomEventClick(View target){
		startActivity(new Intent(EventManagement.this, ShowCustomEvents.class));
	}
	
	public void onCreateCollectionClick(View target){
		showCreateCollectionDialog(
				R.string.create_collection, R.string.create_collection_msg, CREATE);
	}
	
	public void onChangeCollectionClick(View target){
		if (collections.size() > 0){
			showSelectCollectionDialog();
		}
	}

	public void onDeleteCollectionClick(View target){
		showDeleteCollectionDialog();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// context menu of the list

		// Setup the menu header
		AdapterView.AdapterContextMenuInfo info;
        info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(events.get(info.position).name);

        // Add a menu item to remove
        menu.add(0, MENU_ITEM_REMOVE_EVENT, 0, "Remove");
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
	        case MENU_ITEM_REMOVE_EVENT: {
	        	AdapterView.AdapterContextMenuInfo info;
	            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	        	Event.removeEventFromCollection(getContentResolver(), 
	        			currentCollection.id, 
	        			events.get(info.position));
	        	events.remove(info.position);
				collectionEventsListAdapter.notifyDataSetChanged();
				dirty = true;
	            return true;
	        }

	    }
		return false;
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, EditCustomEvent.class);
    	i.setAction(Intent.ACTION_VIEW);
    	i.setData(Uri.parse(Event.CONTENT_URI+ "/" + events.get(position).id));
    	startActivity(i);
	}


	private void showSelectCollectionDialog() {
		String[] items = new String[collections.size()];
		for (int i=0; i<collections.size();i++){
			items[i] = collections.get(i).name;
		}
		new AlertDialog.Builder(EventManagement.this)
			.setTitle(R.string.select_collection_title)
			.setItems(items, new DialogInterface.OnClickListener()  {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					currentCollection = collections.get(which);
					udpateCurrentCollection();
					refreshCollection();
					dirty = true;
				}
			})
			.create()
			.show();
	}
	
	private void refreshCollection() {
		if (currentCollection == null) {
			collectionTV.setTypeface(Typeface.DEFAULT);
			collectionTV.setText(R.string.no_collections);
			deviceAddressTV.setText("");
			collectionEventsListAdapter.eventEntries.clear();
			collectionEventsListAdapter.notifyDataSetChanged();
		}
		else {
			collectionTV.setTypeface(Typeface.DEFAULT_BOLD);
			collectionTV.setText(currentCollection.name);
			deviceAddressTV.setText("[" + currentCollection.deviceAddress + "]");
			events = Event.getEvents(getContentResolver(), currentCollection);
			collectionEventsListAdapter.eventEntries = events;
			collectionEventsListAdapter.notifyDataSetChanged();
		}
	}
	
	private void showCreateCollectionDialog(final int title, final int msg, final int type){
		//Log.d(TAG, "showCreateCollectionDialog");
		final View v = getLayoutInflater().inflate(R.layout.create_collection_dialog, null, false);
		final EditText deviceAddressET = (EditText)v.findViewById(R.id.device_address);
		final EditText nameET = (EditText)v.findViewById(R.id.name);
		final Button createBtn = (Button)v.findViewById(R.id.create_btn);
		if (type == CREATE) {
			createBtn.setText(R.string.create);
			deviceAddressET.setText(BTPreferences.getDefaultAddress(this));
		}
		else if (type == CHANGE){
			createBtn.setText(R.string.change);
			nameET.setText(currentCollection.name);
			deviceAddressET.setText(currentCollection.deviceAddress);
		}
		
		final AlertDialog dialog = 
			new AlertDialog.Builder(EventManagement.this)
				.setTitle(title)
				.setView(v)
				.setMessage(msg)
				.create();

		createBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String name = nameET.getText().toString();
				String deviceAddress = deviceAddressET.getText().toString();
				
				if (type == CREATE){
					Uri newCollectionUri = Collection.addCollection(getContentResolver(), name, deviceAddress);
					collections = Collection.getCollections(getContentResolver());
					long collectionId = Long.parseLong(newCollectionUri.getLastPathSegment());
					currentCollection = getCollectionById(collectionId);
					udpateCurrentCollection();
					refreshCollection();
					dirty = true;
					showAddEventDialog();
				}
				else if (type == CHANGE) {
					if (!deviceAddress.equals(currentCollection.deviceAddress) ||
							name != currentCollection.name){
						Collection.updateCollection(
							getContentResolver(), currentCollection.id, name, deviceAddress);
	
						currentCollection.name = name;
						currentCollection.deviceAddress = deviceAddress;
						collectionTV.setText(name);
						deviceAddressTV.setText(deviceAddress);
						// TODO notify BTService when device address has changed!
						// or maybe maintain a dirty flag
					}
				}
				dialog.dismiss();
			}
		});
		
		
		
		deviceAddressET.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (validDeviceAddress(((EditText)v).getText().toString())) {
					createBtn.setEnabled(true);
				}
				else {
					createBtn.setEnabled(false);
				}
				return false;
			}
		});
		
		
		dialog.show();
	}
	
	private Collection getCollectionById(long id){
		for (int i=0; i<collections.size(); i++){
			if (collections.get(i).id == id)
				return collections.get(i);
		}
		return null;
	}

	
	private void showDeleteCollectionDialog() {
		new AlertDialog.Builder(EventManagement.this)
			.setTitle("Delete Collection")
			.setMessage("Do you want really delete this collection?")
			.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Event.removeEvents(getContentResolver(), currentCollection);
					Collection.deleteCollection(getContentResolver(), currentCollection.id);
					collections = Collection.getCollections(getContentResolver());
					if (collections.size() > 0)
						currentCollection = collections.get(0);
					else 
						currentCollection = null;
					udpateCurrentCollection();
					refreshCollection();
					dirty = true;
				}
			})
			.setNegativeButton("Cancel", null)
			.create()
			.show();
	}
	
	private void showAddEventDialog() {
		new AlertDialog.Builder(EventManagement.this)
			.setTitle("Add Event")
			.setAdapter(allEventsListAdapter, new DialogInterface.OnClickListener()  {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Event selectedEvent = (Event)allEventsListAdapter.getItem(which);
					if (!events.contains(selectedEvent))
						// if we don't want to have two events with same flags in one collection
						// here is the point where we should check for it,
						// but for now we are just fine with it
						// maybe we should at least warn the user about this
						addEventToCollection(selectedEvent);
					else
						Toast.makeText(EventManagement.this, "This event is already in your collection!", Toast.LENGTH_SHORT).show();
				}
			})
			.create()
			.show();
	}
	
	private void addEventToCollection(Event event) {
		if (currentCollection != null) {
			Event.addEvent(getContentResolver(), event, currentCollection);
			events.add(event);
			collectionEventsListAdapter.eventEntries = events;
			collectionEventsListAdapter.notifyDataSetChanged();
			dirty = true;
		}
	}
	
	private void udpateCurrentCollection(){
		long currentCollectionId = -1;
		if (currentCollection != null)
			currentCollectionId = currentCollection.id;
		setCurrentCollection(EventManagement.this, currentCollectionId);
	}
	
	public static void setCurrentCollection(Context context, long currentCollectionId){
		PreferenceManager.getDefaultSharedPreferences(context)
		.edit()
		.putLong(PREF_CURRENT_COLLECTION, currentCollectionId)
		.commit();
	}
	
	/*
	 * Checks if the given address is a correct Bluetooth device address.
	 * 
	 * @return - Needs to be in correct format (00:00:00:00:00:00) to return true,
	 * otherwise false
	 */
	private boolean validDeviceAddress(String address){
		// need to check if address is valid, implement a pattern matcher (regular expression)
		if (address.length() != 17){ // i.e. 00:00:00:00:00:00
			return false;
		}
		else {
			return true;
		}
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


	
	
	
	
	
}
