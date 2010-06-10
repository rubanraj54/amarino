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

import java.util.Timer;
import java.util.TimerTask;

import edu.mit.media.android.amarino.db.Event;

public class TestEventSender {
	
	BTService btService;
	Timer tt;
	char eventFlag;
	public boolean started = false;
	
	public TestEventSender(BTService service){
		btService = service;
		eventFlag = Event.getEventFlag(btService.getContentResolver(), IntentEventMapper.TEST_EVENT);
	}
	
	public void start(){
		tt = new Timer();
		tt.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				btService.sendData(IntentEventMapper.addProtocolFlags(eventFlag, ""));
			}
		}, 2000, 5000); // start after 2 sec, repeat execution every 5 seconds
		started = true;
	}
	
	public void stop(){
		tt.cancel();
		started = false;
	}
	
	
	

}
