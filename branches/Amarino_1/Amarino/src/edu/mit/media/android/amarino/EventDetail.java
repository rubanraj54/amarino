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
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.mit.media.android.amarino.db.Event;
import edu.mit.media.android.amarino.db.EventData;

public class EventDetail extends Activity {
	
	public static final String EVENT_ID_EXTRA = "event_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.long_app_name);
		setContentView(R.layout.event_detail);
		
		long eventId = getIntent().getLongExtra(EVENT_ID_EXTRA, -1);
		Event event = Event.getEvent(getContentResolver(), Uri.parse(Event.CONTENT_URI + "/" + eventId));
		event.data = Event.getEventData(getContentResolver(), event);

		String type = "default";
		if (event.type == Event.CUSTOM_EVENT)
			type = "custom";
		
		((TextView)findViewById(R.id.event_name)).setText("NAME\n" + event.name);
		((TextView)findViewById(R.id.event_action)).setText("ACTION\n" + event.action);
		((TextView)findViewById(R.id.event_flag)).setText("FLAG\n" + "'" + event.flag + "'");
		((TextView)findViewById(R.id.event_desc)).setText("DESC\n" + event.desc);
		((TextView)findViewById(R.id.event_type)).setText("TYPE\n" + type);
		
		if (event.data != null){
			LinearLayout vg = (LinearLayout)findViewById(R.id.event_data);
		
			for (EventData ed : event.data){
				TextView tv = new TextView(this);
				tv.setText("KEY: " + ed.key + "\nTYPE: " + EventData.getTypeAsString(ed.type));
				tv.setPadding(0, 0, 0, 10);
				vg.addView(tv);
			}
		}

	}
	
	

}
