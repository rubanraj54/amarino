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

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AmarinoPhoneStateListener extends PhoneStateListener {

	public static final String TAG = "AmarinoPhoneStateListener";
	Context context;
	
	public AmarinoPhoneStateListener(Context context) {
		this.context = context;
	}
	
	@Override
	public void onCallStateChanged(int state, String incomingNumber){
		super.onCallStateChanged(state, incomingNumber);
		
		switch (state){
			case TelephonyManager.CALL_STATE_RINGING:
				Log.d(TAG, "ringing" + " no:" + incomingNumber);
				context.sendBroadcast(new Intent(IntentEventMapper.CALL_STATE_RINGING));
				break;
			
			case TelephonyManager.CALL_STATE_IDLE:
				Log.d(TAG, "idle");
				context.sendBroadcast(new Intent(IntentEventMapper.CALL_STATE_IDLE));
				break;
			
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.d(TAG, "offhook" + " no:" + incomingNumber);
				context.sendBroadcast(new Intent(IntentEventMapper.CALL_STATE_OFFHOOK));
				break;
		}
	}
	
	public void register(){
		((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
			.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	public void unregister(){
		((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
			.listen(this, PhoneStateListener.LISTEN_NONE);
	}
}
