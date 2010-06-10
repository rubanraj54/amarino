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
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import edu.mit.media.android.amarino.db.Event;

public class ShowCustomEvents extends ListActivity implements View.OnClickListener{
	
	public static final int MENU_ITEM_EDIT = 1;
	public static final int MENU_ITEM_DELETE = 2;

	EventListAdapter allCustomEventsListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.custom_event_management_title);
		setContentView(R.layout.custom_events_list);
		
		findViewById(R.id.add_custom_event_btn).setOnClickListener(this);
		
		allCustomEventsListAdapter = new EventListAdapter(this, Event.getEvents(getContentResolver(), true));
		setListAdapter(allCustomEventsListAdapter);
			
		getListView().setOnCreateContextMenuListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.add_custom_event_btn:
			onCreateCustomEventClick(v);
			break;
		}
	}
	
	
	public void onCreateCustomEventClick(View target){
		startActivityForResult(
				new Intent(ShowCustomEvents.this,EditCustomEvent.class)
					.setAction(Intent.ACTION_INSERT), 0);
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, EditCustomEvent.class);
    	i.setAction(Intent.ACTION_VIEW);
    	i.setData(Uri.parse(Event.CONTENT_URI+ "/" + allCustomEventsListAdapter.eventEntries.get(position).id));
    	startActivity(i);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		// Setup the menu header
		AdapterView.AdapterContextMenuInfo info;
        info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(allCustomEventsListAdapter.eventEntries.get(info.position).name);

        menu.add(0, MENU_ITEM_EDIT, 0, "Edit");
        menu.add(0, MENU_ITEM_DELETE, 0, "Delete");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
	        case MENU_ITEM_EDIT: {
	        	Intent i = new Intent(this, EditCustomEvent.class);
	        	i.setAction(Intent.ACTION_EDIT);
	        	i.setData(Uri.parse(Event.CONTENT_URI+ "/" + allCustomEventsListAdapter.eventEntries.get(info.position).id));
	        	startActivityForResult(i, 1);
	            return true;
	        }
	        case MENU_ITEM_DELETE:{
	        	Event.deleteEvent(getContentResolver(), allCustomEventsListAdapter.eventEntries.get(info.position));
	        	allCustomEventsListAdapter.eventEntries.remove(info.position);
	        	allCustomEventsListAdapter.notifyDataSetChanged();
	        	return true;
	        }

	    } 
		return false;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			allCustomEventsListAdapter.eventEntries = Event.getEvents(getContentResolver(), true);
			allCustomEventsListAdapter.notifyDataSetChanged();
		}
	}


	

	
}
