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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class EventReceiver extends BroadcastReceiver {
	
	public static final String TAG = "EventReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d(TAG, "Event received: " + intent.getAction());
		Bundle b = intent.getExtras();
		if (b == null)
			b = new Bundle();
		b.putString("action", intent.getAction());
		
		String data = IntentEventMapper.getEventString(context.getContentResolver(), b);
		if (data != null) {
			Log.d(TAG, "send data: " + data);
			Intent i = new Intent(context, BTService.class);
			i.setAction(BTService.SEND_DATA);
			i.putExtra("data", data);
			context.startService(i);
		}
		else {
			Log.d(TAG, "action unknown - " + intent.getAction());
		}

	}


}
