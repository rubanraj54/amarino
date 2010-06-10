/*
  Amarino - A prototyping software toolkit for Android and Arduino
  Copyright (c) 2010 Bonifaz Kaufmann.  All right reserved.
  
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
package edu.mit.media.android.amarino.log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class AmarinoLogger {

	public static final String KEY_IS_LOG_ENABLED = "is_log_enabled";
	private static final int MAX_LOG_ENTRIES = 80;
	private static AmarinoLogger instance;
	private ArrayList<LogListener> listeners;
	private LinkedList<String> log;
	private int logSize = 0;
	
	// I am a singleton
	private AmarinoLogger(){ 
		log = new LinkedList<String>();
		listeners = new ArrayList<LogListener>();
	}
	
	public static synchronized AmarinoLogger getInstance(){
		if (instance == null)
			instance = new AmarinoLogger();
		return instance;
	}
	
	public void add(String msg){
		 synchronized (log){
			if (logSize < MAX_LOG_ENTRIES){
				logSize++;
			}
			else {
				// we don't check if elements are present, since we trust logSize
				log.removeFirst(); 
			}
			log.add(msg);
		}
		notifyListeners(msg.toString());
	}
	
	public synchronized void clear(){
		logSize = 0;
		log.clear();
	}
	
	public synchronized String getLog(){
		StringBuilder sb = new StringBuilder();
		ListIterator<String> iter = log.listIterator();
		while (iter.hasNext()){
			sb.append(iter.next());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
	public synchronized void registerLogListener(LogListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public synchronized void unregisterLogListener(LogListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(final String lastAddedMsg){
		if (listeners != null){
			for (LogListener ll : listeners)
				ll.logChanged(lastAddedMsg);
		}
	}
}
