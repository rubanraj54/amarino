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

import edu.mit.media.android.amarino.db.Event;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventListAdapter extends BaseAdapter {

	ArrayList<Event> eventEntries;
	Context context;
	
	public EventListAdapter(Context context, ArrayList<Event> eventEntries){
		this.context = context;
		this.eventEntries = eventEntries;
	}
	
	public int getCount() {
		if (eventEntries != null) {
			return eventEntries.size();
		}
		return 0;
	}

	public Object getItem(int position) {
		return eventEntries.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout view = null;

		if (convertView == null) {
			view = new LinearLayout(context);
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vi.inflate(R.layout.event_entry, view, true);
		} else {
			view = (LinearLayout) convertView;
		}

		TextView name = (TextView) view.findViewById(R.id.event_name);
		TextView flag = (TextView) view.findViewById(R.id.event_flag);
		TextView detail = (TextView) view.findViewById(R.id.event_detail);
		
		Event event = eventEntries.get(position);
		name.setText(event.name);
		detail.setText(event.desc);
		flag.setText("'" + event.flag + "'");
		
		return view;
	}
}


