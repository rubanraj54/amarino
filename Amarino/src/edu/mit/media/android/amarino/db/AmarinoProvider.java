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

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import edu.mit.media.android.amarino.IntentEventMapper;
import edu.mit.media.android.amarino.R;

public class AmarinoProvider extends ContentProvider{

	public static final String AUTHORITY = "edu.mit.media.android.provider.amarino";
	
	
	private static final String TAG = "AmarinoProvider";
	
	private static final String DATABASE_NAME = "amarino.db";
    private static final int DATABASE_VERSION = 4;
    
    private static final String COLLECTION_TABLE_NAME = "collection_tbl";
    private static final String EVENT_TABLE_NAME = "event_tbl";
    private static final String EVENTDATA_TABLE_NAME = "eventdata_tbl";
    private static final String COLLECTION_EVENT_TABLE_NAME = "collection_event_tbl";
    
    private static final int COLLECTIONS = 1;
    private static final int COLLECTION_ID = 2;
    private static final int EVENTS = 3;
    private static final int EVENT_ID = 4;
    private static final int EVENT_DATA = 5;
    private static final int EVENT_DATA_ID = 6;
    private static final int COLLECTIONS_EVENTS = 7;
    private static final int COLLECTIONS_EVENTS_ID = 8;

    
    private static final UriMatcher uriMatcher;
    private static HashMap<String, String> collectionProjectionMap;
    private static HashMap<String, String> eventProjectionMap;
    private static HashMap<String, String> eventDataProjectionMap;
    private static HashMap<String, String> collection_eventProjectionMap;
    private DatabaseHelper dbHelper;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		private final Context context;
		
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.d(TAG, "create database tables");
        	/* Create Collection Table */
        	db.execSQL("CREATE TABLE " + COLLECTION_TABLE_NAME + " ("
                    + Collection._ID + " INTEGER PRIMARY KEY,"
                    + Collection.NAME + " TEXT UNIQUE,"
                    + Collection.DEVICE_ADDRESS  + " TEXT"
                    + ");");
        	
        	/* Create Event Table */
            db.execSQL("CREATE TABLE " + EVENT_TABLE_NAME + " ("
                    + Event._ID + " INTEGER PRIMARY KEY,"
                    + Event.NAME + " TEXT UNIQUE,"
                    + Event.ACTION + " TEXT,"
                    + Event.FLAG + " INTEGER,"
                    + Event.TYPE + " INTEGER,"
                    + Event.DESCRIPTION + " TEXT"
                    + ");");
            
            /* Create Data Table */
            db.execSQL("CREATE TABLE " + EVENTDATA_TABLE_NAME + " ("
                    + EventData._ID + " INTEGER PRIMARY KEY,"
                    + EventData.EVENT_ID + " INTEGER REFERENCES " + EVENT_TABLE_NAME + "(_id), "
                    + EventData.KEY + " TEXT,"
                    + EventData.TYPE + " INTEGER"
                    + ");");
            
            /* Create Collection<->Event Table */
            db.execSQL("CREATE TABLE " + COLLECTION_EVENT_TABLE_NAME + " ("
                    + Event.Collection_Event._ID + " INTEGER PRIMARY KEY,"
                    + Event.Collection_Event.COLLECTION_ID + " INTEGER REFERENCES " + COLLECTION_TABLE_NAME + "(_id), "
                    + Event.Collection_Event.EVENT_ID + " INTEGER REFERENCES " + EVENT_TABLE_NAME + "(_id)"
                    + ");");

            Log.d(TAG, "database tables created");
            ContentValues values = new ContentValues();
            long eventId = 0;
            
            /* Test Event */
            values.put(Event.NAME, context.getString(R.string.event_name_test));
            values.put(Event.ACTION, IntentEventMapper.TEST_EVENT);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_test));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            
            /* TIME TICK EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_time_tick));
            values.put(Event.ACTION, Intent.ACTION_TIME_TICK);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_time_tick));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);

            /* PHONE STATE RINGING EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_phone_state_ringing));
            values.put(Event.ACTION, IntentEventMapper.CALL_STATE_RINGING);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_phone_state_ringing));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* incoming number */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, TelephonyManager.EXTRA_INCOMING_NUMBER);
            values.put(EventData.TYPE, EventData.STRING);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* PHONE STATE IDLE EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_phone_state_idle));
            values.put(Event.ACTION, IntentEventMapper.CALL_STATE_IDLE);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_phone_state_idle));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            
            /* PHONE STATE OFFHOOK EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_phone_state_offhook));
            values.put(Event.ACTION, IntentEventMapper.CALL_STATE_OFFHOOK);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_phone_state_offhook));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* incoming number */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, TelephonyManager.EXTRA_INCOMING_NUMBER);
            values.put(EventData.TYPE, EventData.STRING);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* COMPASS EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_compass));
            values.put(Event.ACTION, IntentEventMapper.COMPASS);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_compass));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* heading value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_COMPASS_HEADING);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* ORIENTATION EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_orientation));
            values.put(Event.ACTION, IntentEventMapper.ORIENTATION);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_orientation));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* azimuth value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_ORIENTATION_AZIMUTH);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* pitch value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_ORIENTATION_PITCH);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* roll value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_ORIENTATION_ROLL);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* ACCELEROMETER EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_accelerometer));
            values.put(Event.ACTION, IntentEventMapper.ACCELEROMETER);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_accelerometer));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* x value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_ACCELEROMETER_X);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* y value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_ACCELEROMETER_Y);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* z value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_ACCELEROMETER_Z);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* MAGNETIC_FIELD EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_magnetic_field));
            values.put(Event.ACTION, IntentEventMapper.MAGNETIC_FIELD);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_magnetic_field));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* x value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_MAGNETIC_FIELD_X);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* y value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_MAGNETIC_FIELD_Y);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* z value */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_MAGNETIC_FIELD_Z);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* TEMP SENSOR */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_temperature));
            values.put(Event.ACTION, IntentEventMapper.TEMPRATURE);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_temperature));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* temp */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_TEMPREATURE);
            values.put(EventData.TYPE, EventData.FLOAT);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* BATTERY LEVEL */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_battery_level));
            values.put(Event.ACTION, IntentEventMapper.BATTERY_LEVEL);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_battery_level));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* level */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_BATTERY_LEVEL);
            values.put(EventData.TYPE, EventData.INTEGER);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* EARTHQUATE EVENT */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_earthquake));
            values.put(Event.ACTION, IntentEventMapper.EARTHQUAKE);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_earthquake));
            eventId = db.insert(EVENT_TABLE_NAME, null, values);
            /* magnitude */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_EARTHQUAKE_MAGNITUDE);
            values.put(EventData.TYPE, EventData.DOUBLE);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* latitude */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_EARTHQUAKE_LATITUDE);
            values.put(EventData.TYPE, EventData.DOUBLE);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            /* longitude */
            values.clear();
            values.put(EventData.EVENT_ID, eventId);
            values.put(EventData.KEY, IntentEventMapper.KEY_EARTHQUAKE_LONGITUDE);
            values.put(EventData.TYPE, EventData.DOUBLE);
            db.insert(EVENTDATA_TABLE_NAME, null, values);
            
            /* Receive SMS Event */
            values.clear();
            values.put(Event.NAME, context.getString(R.string.event_name_receive_sms));
            values.put(Event.ACTION, IntentEventMapper.RECEIVE_SMS);
            values.put(Event.FLAG, Event.getNextFlag());
            values.put(Event.TYPE, Event.DEFAULT_EVENT);
            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_receive_sms));
            db.insert(EVENT_TABLE_NAME, null, values);
            
            /* add new events here */
            
            Log.d(TAG, "default events inserted!");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
//            ContentValues values = new ContentValues();
//            values.put(Event.NAME, context.getString(R.string.event_name_receive_sms));
//            values.put(Event.ACTION, IntentEventMapper.RECEIVE_SMS);
//            values.put(Event.FLAG, 77); //'M'
//            values.put(Event.TYPE, Event.DEFAULT_EVENT);
//            values.put(Event.DESCRIPTION, context.getString(R.string.event_desc_receive_sms));
//            db.insert(EVENT_TABLE_NAME, null, values);
            
            db.execSQL("DROP TABLE IF EXISTS " + COLLECTION_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + EVENTDATA_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + COLLECTION_EVENT_TABLE_NAME);
            onCreate(db);
            Log.d(TAG, "upgrade db");
        }
    }

    

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }
	
    @Override
	public String getType(Uri uri) {
    	switch (uriMatcher.match(uri)) {
        case COLLECTIONS:
            return Collection.CONTENT_TYPE;
        case COLLECTION_ID:
            return Collection.CONTENT_ITEM_TYPE;
        case EVENTS:
            return Event.CONTENT_TYPE;
        case EVENT_ID:
            return Event.CONTENT_ITEM_TYPE;
        case EVENT_DATA:
            return EventData.CONTENT_TYPE;
        case EVENT_DATA_ID:
            return EventData.CONTENT_ITEM_TYPE;
        /* relationship tables */
        case COLLECTIONS_EVENTS:
        	return Event.Collection_Event.CONTENT_TYPE;
        case COLLECTIONS_EVENTS_ID:
        	return Event.Collection_Event.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}
    

	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = null;
		
		switch (uriMatcher.match(uri)) {
        case COLLECTIONS:
            qb.setTables(COLLECTION_TABLE_NAME);
            qb.setProjectionMap(collectionProjectionMap);
            orderBy = getSortOrder(sortOrder, Collection.DEFAULT_SORT_ORDER);
            break;

        case COLLECTION_ID:
            qb.setTables(COLLECTION_TABLE_NAME);
            qb.setProjectionMap(collectionProjectionMap);
            qb.appendWhere(Collection._ID + "=" + uri.getPathSegments().get(1));
            orderBy = getSortOrder(sortOrder, Collection.DEFAULT_SORT_ORDER);
            break;
            
        case EVENTS:
        	qb.setTables(EVENT_TABLE_NAME);
        	qb.setProjectionMap(eventProjectionMap);
        	orderBy = getSortOrder(sortOrder, Event.DEFAULT_SORT_ORDER);
        	break;
        	
        case EVENT_ID:
        	qb.setTables(EVENT_TABLE_NAME);
        	qb.setProjectionMap(eventProjectionMap);
        	qb.appendWhere(Event._ID + "=" + uri.getPathSegments().get(1));
        	orderBy = getSortOrder(sortOrder, Event.DEFAULT_SORT_ORDER);
        	break;
        	
        case EVENT_DATA:
        	qb.setTables(EVENTDATA_TABLE_NAME);
        	qb.setProjectionMap(eventDataProjectionMap);
        	orderBy = getSortOrder(sortOrder, EventData.DEFAULT_SORT_ORDER);
        	break;
        	
        case EVENT_DATA_ID:
        	qb.setTables(EVENTDATA_TABLE_NAME);
        	qb.setProjectionMap(eventDataProjectionMap);
        	qb.appendWhere(Event._ID + "=" + uri.getPathSegments().get(1));
        	orderBy = getSortOrder(sortOrder, EventData.DEFAULT_SORT_ORDER);
        	break;
        	
        case COLLECTIONS_EVENTS:
        	qb.setTables(COLLECTION_EVENT_TABLE_NAME);
        	qb.setProjectionMap(collection_eventProjectionMap);
        	orderBy = getSortOrder(sortOrder, Event.Collection_Event.DEFAULT_SORT_ORDER);
        	break;
        	
        case COLLECTIONS_EVENTS_ID:
        	qb.setTables(COLLECTION_EVENT_TABLE_NAME);
        	qb.setProjectionMap(collection_eventProjectionMap);
        	qb.appendWhere(Event.Collection_Event._ID + "=" + uri.getPathSegments().get(1));
        	orderBy = getSortOrder(sortOrder, Event.Collection_Event.DEFAULT_SORT_ORDER);
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
		
        // Get the database and run the query
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	/*
	 * If no sort order is specified use the default
	 */
	private String getSortOrder(String sortOrder, String defaultSortOrder) {
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
            orderBy = defaultSortOrder;
        } else {
            orderBy = sortOrder;
        }
		return orderBy;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		
		ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = -1;
        
		switch (uriMatcher.match(uri)) {
        case COLLECTIONS:
        	if (values.containsKey(Collection.NAME) == false){
        		values.put(Collection.NAME, "NO NAME");
        	}
        	if (values.containsKey(Collection.DEVICE_ADDRESS) == false){
        		values.put(Collection.NAME, "00:00:00:00:00:00");
        	}
        	rowId = db.insert(COLLECTION_TABLE_NAME, Collection.NAME, values);
            if (rowId > 0) {
                Uri collectionUri = ContentUris.withAppendedId(Collection.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(collectionUri, null);
                return collectionUri;
            }
            break;
        
        case EVENTS:
        	if (values.containsKey(Event.NAME) == false){
        		values.put(Collection.NAME, "NO NAME");
        	}
        	if (values.containsKey(Event.TYPE) == false){
        		values.put(Event.TYPE, Event.CUSTOM_EVENT);
        	}
        	if (values.containsKey(Event.ACTION) == false){
        		values.put(Event.ACTION, "");
        	}
        	if (values.containsKey(Event.FLAG) == false){
        		values.put(Event.FLAG, "122"); // 'z'
        	}
        	if (values.containsKey(Event.DESCRIPTION) == false){
        		values.put(Event.DESCRIPTION, "");
        	}
        	rowId = db.insert(EVENT_TABLE_NAME, Event.NAME, values);
            if (rowId > 0) {
                Uri eventUri = ContentUris.withAppendedId(Event.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(eventUri, null);
                return eventUri;
            }
        	break;
        	
        case EVENT_DATA:
        	if (values.containsKey(EventData.KEY) == false){
        		values.put(EventData.KEY, "NO KEY");
        	}
        	if (values.containsKey(EventData.TYPE) == false){
        		values.put(EventData.TYPE, EventData.STRING);
        	}
        	rowId = db.insert(EVENTDATA_TABLE_NAME, EventData.KEY, values);
            if (rowId > 0) {
                Uri eventDataUri = ContentUris.withAppendedId(EventData.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(eventDataUri, null);
                return eventDataUri;
            }
        	break;
        	
        case COLLECTIONS_EVENTS:
        	rowId = db.insert(COLLECTION_EVENT_TABLE_NAME, null, values);
            if (rowId > 0) {
                Uri collection_event_uri = ContentUris.withAppendedId(Event.Collection_Event.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(collection_event_uri, null);
                return collection_event_uri;
            }
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
		
        throw new SQLException("Failed to insert row into " + uri);
	}

	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        
        switch (uriMatcher.match(uri)) {
        case COLLECTIONS:
            count = db.update(COLLECTION_TABLE_NAME, values, where, whereArgs);
            break;

        case COLLECTION_ID:
            String collectionId = uri.getPathSegments().get(1);
            count = db.update(COLLECTION_TABLE_NAME, values, Collection._ID + "=" + collectionId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        case EVENTS:
        	count = db.update(EVENT_TABLE_NAME, values, where, whereArgs);
            break;
        case EVENT_ID:
        	String eventId = uri.getPathSegments().get(1);
            count = db.update(EVENT_TABLE_NAME, values, Event._ID + "=" + eventId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
        case EVENT_DATA:
        	count = db.update(EVENTDATA_TABLE_NAME, values, where, whereArgs);
        	break;
        case EVENT_DATA_ID:
        	String eventDataId = uri.getPathSegments().get(1);
            count = db.update(EVENTDATA_TABLE_NAME, values, EventData._ID + "=" + eventDataId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
            
        case COLLECTIONS_EVENTS:
        	count = db.update(COLLECTION_EVENT_TABLE_NAME, values, where, whereArgs);
        	break;
        case COLLECTIONS_EVENTS_ID:
        	String collection_events_id = uri.getPathSegments().get(1);
            count = db.update(COLLECTION_EVENT_TABLE_NAME, values, EventData._ID + "=" + collection_events_id
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;   
            


        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        
        switch (uriMatcher.match(uri)) {
        case COLLECTIONS:
            count = db.delete(COLLECTION_TABLE_NAME, where, whereArgs);
            break;
        case COLLECTION_ID:
            String collectionId = uri.getPathSegments().get(1);
            count = db.delete(COLLECTION_TABLE_NAME, Collection._ID + "=" + collectionId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
            
        case EVENTS:
        	count = db.delete(EVENT_TABLE_NAME, where, whereArgs);
            break;
        case EVENT_ID:
        	String eventId = uri.getPathSegments().get(1);
            count = db.delete(EVENT_TABLE_NAME, Event._ID + "=" + eventId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
            
        case EVENT_DATA:
        	count = db.delete(EVENTDATA_TABLE_NAME, where, whereArgs);
        	break;
        case EVENT_DATA_ID:
        	String eventDataId = uri.getPathSegments().get(1);
            count = db.delete(EVENTDATA_TABLE_NAME, EventData._ID + "=" + eventDataId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
            
        case COLLECTIONS_EVENTS:
        	count = db.delete(COLLECTION_EVENT_TABLE_NAME, where, whereArgs);
        	break;
        case COLLECTIONS_EVENTS_ID:
        	String collection_events_id = uri.getPathSegments().get(1);
            count = db.delete(COLLECTION_EVENT_TABLE_NAME, Event.Collection_Event._ID + "=" + collection_events_id
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;  
            
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        
        return count;
	}
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "collections", COLLECTIONS);
		uriMatcher.addURI(AUTHORITY, "collections/#", COLLECTION_ID);
		uriMatcher.addURI(AUTHORITY, "events", EVENTS);
		uriMatcher.addURI(AUTHORITY, "events/#", EVENT_ID);
		uriMatcher.addURI(AUTHORITY, "eventdata", EVENT_DATA);
		uriMatcher.addURI(AUTHORITY, "eventdata/#", EVENT_DATA_ID);
		uriMatcher.addURI(AUTHORITY, "collections_events", COLLECTIONS_EVENTS);
		uriMatcher.addURI(AUTHORITY, "collections_events/#", COLLECTIONS_EVENTS_ID);

		collectionProjectionMap = new HashMap<String, String>();
		collectionProjectionMap.put(Collection._ID, Collection._ID);
		collectionProjectionMap.put(Collection.NAME, Collection.NAME);
		collectionProjectionMap.put(Collection.DEVICE_ADDRESS, Collection.DEVICE_ADDRESS);
		
		eventProjectionMap = new HashMap<String, String>();
		eventProjectionMap.put(Event._ID, Event._ID);
		eventProjectionMap.put(Event.NAME, Event.NAME);
		eventProjectionMap.put(Event.ACTION, Event.ACTION);
		eventProjectionMap.put(Event.FLAG, Event.FLAG);
		eventProjectionMap.put(Event.TYPE, Event.TYPE);
		eventProjectionMap.put(Event.DESCRIPTION, Event.DESCRIPTION);
		
		eventDataProjectionMap = new HashMap<String, String>();
		eventDataProjectionMap.put(EventData._ID, EventData._ID);
		eventDataProjectionMap.put(EventData.KEY, EventData.KEY);
		eventDataProjectionMap.put(EventData.TYPE, EventData.TYPE);
		
		collection_eventProjectionMap = new HashMap<String, String>();
		collection_eventProjectionMap.put(Event.Collection_Event._ID, Event.Collection_Event._ID);
		collection_eventProjectionMap.put(Event.Collection_Event.COLLECTION_ID, Event.Collection_Event.COLLECTION_ID);
		collection_eventProjectionMap.put(Event.Collection_Event.EVENT_ID, Event.Collection_Event.EVENT_ID);

    }
	
}
