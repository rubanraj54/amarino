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
package edu.mit.media.android.amarino.db;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class Event {

	public static final int DEFAULT_EVENT = 1;
	public static final int CUSTOM_EVENT = 2;
	
	public static int flagCounter = 65; //'A'
	
	public long id;
	public String action;
	public String name;
	public String desc;
	public char flag;
	public int type;
	public ArrayList<EventData> data;
	
	public Event(){
	}
	
	public Event(int type){
		this.type = type;
	}
	
	@Override
	public boolean equals(Object o){
		if (this == o) 
			return true;
		
		if (o == null || (o.getClass() != this.getClass())) 
			return false;
		
		Event e = (Event) o;
		if (e.id==this.id)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return (int)id;
	}
	
	public static ArrayList<Event> getEvents(ContentResolver cr, boolean customEventsOnly){
		ArrayList<Event> events = new ArrayList<Event>();
		Cursor cursor = cr.query(Event.CONTENT_URI, null, null, null, null);
		if (cursor.moveToFirst()){
			int type;
			do {
				type = cursor.getInt(cursor.getColumnIndex(Event.TYPE));
				if (customEventsOnly) {
					if (type == Event.CUSTOM_EVENT)
						events.add(createEventFromCursor(cursor));
				}
				else 
					events.add(createEventFromCursor(cursor));
			} 
			while (cursor.moveToNext());
		}
		cursor.close();
		return events;
	}
	
	public static final ArrayList<EventData> getEventData(ContentResolver cr, Event e){
		ArrayList<EventData> eventData = null;
		
		// get all eventdata associated to an event
		Cursor cursor = cr.query(EventData.CONTENT_URI, null,
				EventData.EVENT_ID + "=?", new String[]{String.valueOf(e.id)}, null);
		
		if (cursor.moveToFirst()){
			eventData = new ArrayList<EventData>();
			do {
				EventData ed = new EventData();
				ed.id = cursor.getLong(cursor.getColumnIndex(EventData._ID));
				ed.key = cursor.getString(cursor.getColumnIndex(EventData.KEY));
				ed.type = cursor.getInt(cursor.getColumnIndex(EventData.TYPE));
				eventData.add(ed);
			} 
			while (cursor.moveToNext());
		}
		cursor.close();
		return eventData;
	}
	
	public static final Event getEvent(ContentResolver cr, Uri uri){
		Cursor cursor = cr.query(uri, null, null, null, null);
		Event event = null;
		if (cursor.moveToFirst()){
			event = createEventFromCursor(cursor);
		}
		cursor.close();
		return event;
	}
	
	public static final Event getEvent(ContentResolver cr, String action){
		Cursor cursor = cr.query(Event.CONTENT_URI, null,
				Event.ACTION + "=?", new String[]{action}, null);
		Event event = null;
		if (cursor.moveToFirst()){
			event = createEventFromCursor(cursor);
		}
		cursor.close();
		return event;
	}
	
	/**
	 * Returns all used event flags
	 * @param cr
	 * @return
	 */
	public static final char[] getEventFlags(ContentResolver cr){
		Cursor cursor = cr.query(
				Event.CONTENT_URI, 
				new String[]{Event.FLAG},
				Event.TYPE + "=?", new String[]{String.valueOf(Event.CUSTOM_EVENT)},
				null);
		
		int size = cursor.getCount();
		if (size > 0){ 
			char[] flags = new char[size];
			int i = 0;
			if (cursor.moveToFirst()){
				do {
					flags[i] = (char)cursor.getInt(cursor.getColumnIndex(Event.FLAG));
					i++;
				}
				while (cursor.moveToNext());
			}
			cursor.close();
			return flags;
		}
		else {
			cursor.close();
			return new char[0];
		}
	}
	
	public static final boolean isEventInCollection(ContentResolver cr, long collectionId, long eventId){
		boolean result = false;
		Cursor cursor = cr.query(
				Event.Collection_Event.CONTENT_URI, 
				new String[]{Event.Collection_Event._ID},
				Event.Collection_Event.COLLECTION_ID + "=" + collectionId + " AND " +
				Event.Collection_Event.EVENT_ID + "=" + eventId,
				null,
				null);
		if (cursor.getCount() > 0) 
			result = true;
		cursor.close();
		return result;
	} 
	
	/**
	 * Get the flag of a given action
	 * @param cr
	 * @param action
	 * @return
	 */
	public static final char getEventFlag(ContentResolver cr, String action){
		Cursor cursor = cr.query(Event.CONTENT_URI, new String[] {Event.FLAG},
				Event.ACTION + "=?", new String[]{action}, null);
		char flag = 0;
		if (cursor.moveToFirst()){
			flag = (char)cursor.getInt(cursor.getColumnIndex(Event.FLAG));
		}
		cursor.close();
		return flag;
	}
	
	
	/**
	 * Get all events of a given collection
	 */
	public static final ArrayList<Event> getEvents(ContentResolver cr, Collection c){
		ArrayList<Event> events = new ArrayList<Event>();
		Cursor cursor = cr.query(
				Event.Collection_Event.CONTENT_URI, null, 
				Event.Collection_Event.COLLECTION_ID  + "=?", new String[]{String.valueOf(c.id)}, null);

		if (cursor.moveToFirst()){
			do {
				Uri uri = Uri.withAppendedPath(Event.CONTENT_URI, 
						cursor.getString(cursor.getColumnIndex(Event.Collection_Event.EVENT_ID)));
				events.add(getEvent(cr, uri));
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		return events;
	}
	
	/**
	 * Add an event to a collection
	 */
	public static final Uri addEvent(ContentResolver cr, Event e, Collection c){
		ContentValues values = new ContentValues();
		values.put(Collection_Event.EVENT_ID, e.id);
		values.put(Collection_Event.COLLECTION_ID, c.id);
		return cr.insert(Collection_Event.CONTENT_URI, values);
	}
	
	/**
	 * Insert a new event into the event table
	 */
	public static final Uri insertEvent(ContentResolver cr, Event e){
		ContentValues values = new ContentValues();
		values.put(Event.NAME, e.name);
		values.put(Event.DESCRIPTION, e.desc);
		values.put(Event.ACTION, e.action);
		values.put(Event.FLAG, (int)e.flag);
		values.put(Event.TYPE, e.type);
		Uri uri = cr.insert(Event.CONTENT_URI, values);
		values.clear();
		
		if (e.data != null && e.data.size() > 0) {
			e.id = Event.getEvent(cr, uri).id;
			Event.insertEventData(cr, e);
		}

		return uri;
	}

	private static final void insertEventData(ContentResolver cr, Event e) {
		if (e.data != null){
			ContentValues values = new ContentValues();
			for (EventData ed : e.data){
				// if we have the EventData id already, means we deleted right before, thus we reuse the id
				if (ed.id > -1){
					values.put(EventData._ID, ed.id);
				}
				values.put(EventData.EVENT_ID, e.id);
				values.put(EventData.KEY, ed.key);
				values.put(EventData.TYPE, ed.type);
				cr.insert(EventData.CONTENT_URI, values);
				values.clear();
			}
		}
	}
	
	public static final void updateEvent(ContentResolver cr, Event e){
		ContentValues values = new ContentValues();
		values.put(Event.NAME, e.name);
		values.put(Event.DESCRIPTION, e.desc);
		values.put(Event.ACTION, e.action);
		values.put(Event.FLAG, (int)e.flag);
		cr.update(Uri.parse(Event.CONTENT_URI + "/" + e.id), values, null, null);
		// delete all event data of this event
		cr.delete(EventData.CONTENT_URI, EventData.EVENT_ID + "=?", new String[]{String.valueOf(e.id)});
		Event.insertEventData(cr, e);
		
	}
	
	public static final int deleteEvent(ContentResolver cr, Event e){
		int result = 0;
		result = cr.delete(Uri.parse(Event.CONTENT_URI + "/" + e.id), null, null);
		// delete associated eventData
		result += cr.delete(EventData.CONTENT_URI, 
				EventData.EVENT_ID + "=?", new String[]{String.valueOf(e.id)});
		
		// delete event entries in affected collections
		result += cr.delete(Event.Collection_Event.CONTENT_URI,
				Event.Collection_Event.EVENT_ID + "=?", new String[]{String.valueOf(e.id)});
		return result;
	}
	
	/**
	 * Delete all events associated to the given collection
	 */
	public static final int removeEvents(ContentResolver cr, Collection c){
		return cr.delete(Collection_Event.CONTENT_URI, 
				Collection_Event.COLLECTION_ID + "=?", new String[]{String.valueOf(c.id)});
	}
	
	public static final int removeEventFromCollection(ContentResolver cr, long collectionId, Event e){
		return cr.delete(Collection_Event.CONTENT_URI, 
				Collection_Event.COLLECTION_ID + "=? AND " + Collection_Event.EVENT_ID + "=?",
				new String[]{String.valueOf(collectionId), String.valueOf(e.id)});
	}
	
	
	private static final Event createEventFromCursor(Cursor cursor){
		Event event = new Event();
		event.id = cursor.getLong(cursor.getColumnIndex(Event._ID));
		event.name = cursor.getString(cursor.getColumnIndex(Event.NAME));
		event.action = cursor.getString(cursor.getColumnIndex(Event.ACTION));
		event.flag = (char)cursor.getInt(cursor.getColumnIndex(Event.FLAG));
		event.type = cursor.getInt(cursor.getColumnIndex(Event.TYPE));
		event.desc = cursor.getString(cursor.getColumnIndex(Event.DESCRIPTION));
		return event;
	}
	
	public static final int getNextFlag(){
		return flagCounter++;
	}
	
	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + AmarinoProvider.AUTHORITY + "/events");
	
	/**
     * The MIME type of {@link #CONTENT_URI} providing a directory of events.
     */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.media.mit.edu.android.event";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single event.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.media.mit.edu.android.event";
    
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "type DESC";
    
    public static final String _ID = "_id";
    /**
     * The name of the event
     * <P>Type: TEXT</P>
     */
	public static final String NAME = "name";
	
	/**
     * The action of the event
     * <P>Type: TEXT</P>
     */
	public static final String ACTION = "action";
	
	/**
     * The flag
     * <P>Type: TEXT</P>
     */
	public static final String FLAG = "flag";
	
	/**
     * The type of the event
     * <P>Type: INTEGER</P>
     */
	public static final String TYPE = "type";
	
	/**
     * The description of the event
     * <P>Type: TEXT</P>
     */
	public static final String DESCRIPTION = "desc";
	
	
	public interface Collection_Event {
		
		public static final Uri CONTENT_URI = 
			Uri.parse("content://" + AmarinoProvider.AUTHORITY + "/collections_events");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.media.mit.edu.android.collection_event";
	    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.media.mit.edu.android.collection_event";
	    
	    public static final String _ID = "_id";
		public static final String DEFAULT_SORT_ORDER = "_id DESC";
		public static final String COLLECTION_ID = "collection_id";
		public static final String EVENT_ID = "event_id";
	}
	

}
