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

import edu.mit.media.android.amarino.db.Collection;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoteControl extends BroadcastReceiver {
	
	private static final String TAG = "RemoteControl";
	public static final String CONNECT = "amarino.CONNECT";
	public static final String DISCONNECT = "amarino.DISCONNECT";
	public static final String SET_COLLECTION = "amarino.SET_COLLECTION";
	public static final String EXTRA_COLLECTION_NAME = "COLLECTION_NAME";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			if (CONNECT.equals(action)){
				Log.d(TAG, "connect requested");
				Intent i = new Intent(context, BTService.class);
				i.setAction(CONNECT);
				context.startService(i);
			}
			else if (DISCONNECT.equals(action)){
				Log.d(TAG, "disconnect requested");
				Intent i = new Intent(context, BTService.class);
				i.setAction(DISCONNECT);
				context.startService(i);
			}
			else if (SET_COLLECTION.equals(action)){
				Log.d(TAG, "set collection requested");
				String name = intent.getStringExtra(EXTRA_COLLECTION_NAME);
				if (name != null){
					long id = Collection.getIdByCollectionName(context.getContentResolver(), name);
					if (id > -1){
						Log.d(TAG, "active collection set to: " + name);
						EventManagement.setCurrentCollection(context, id);
					}
					else {
						Log.d(TAG, "Collection with name [ " + name + " ] not found");
					}
				}
				else {
					Log.e(TAG, "String Extra (EXTRA_COLLECTION_NAME) not found");
				}
			}
		}
	}

}
