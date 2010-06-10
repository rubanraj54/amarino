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
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import edu.mit.media.android.amarino.BTPreferences;
import edu.mit.media.android.amarino.EventManagement;

public final class Collection {

	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + AmarinoProvider.AUTHORITY + "/collections");
	
	public Collection(){
	}
	
	public long id;
	public String name;
	public String deviceAddress;
	
	public static final Uri addCollection(ContentResolver cr, String name, String deviceAddress){
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(DEVICE_ADDRESS, deviceAddress);
		return cr.insert(CONTENT_URI, values);
	}
	

	public static final int deleteCollection(ContentResolver cr, long id){
		return cr.delete(Uri.parse(CONTENT_URI + "/" + id), null, null);
	}
	
	public static final int updateCollection(ContentResolver cr, long id, String name, String deviceAddress){
		ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(DEVICE_ADDRESS, deviceAddress);
		return cr.update(CONTENT_URI, values, _ID  + "=?", new String[] {String.valueOf(id)});
	}
	
	public static final Collection getCollection(ContentResolver cr, long id){
		Collection collection = null;
		Cursor cursor = cr.query(Uri.parse(CONTENT_URI + "/" + id), null, null, null, null);
		if (cursor != null){
			if (cursor.moveToFirst()){
				collection = new Collection();
				collection.id = id;
				collection.name = cursor.getString(cursor.getColumnIndex(NAME));
				collection.deviceAddress = cursor.getString(cursor.getColumnIndex(DEVICE_ADDRESS));
			}
			cursor.close();
		}
		return collection;
	}
	
	public static final long getIdByCollectionName(ContentResolver cr, String name){
		long id = -1;
		Cursor cursor = cr.query(CONTENT_URI, new String[]{Collection._ID},
				Collection.NAME + " like ?", new String[]{name}, null);
		if (cursor != null){
			if (cursor.moveToFirst()){
				id = cursor.getLong(0);
			}
			cursor.close();
		}
		return id;
	}
	
	public static final Collection getCurrentCollection(Context context){
		long currentCollectionId = EventManagement.getCurrentCollectionId(context);
		if (currentCollectionId < 0) 
			return null;
		else
			return getCollection(context.getContentResolver(), currentCollectionId);
	}
	
	public static final String getCurrentDeviceAddress(Context context){
		Collection c = getCurrentCollection(context);
		if (c==null){
			return BTPreferences.getDefaultAddress(context);
		}
		else {
			return c.deviceAddress;
		}
	}
	
	public static final ArrayList<Collection> getCollections(ContentResolver cr){
		ArrayList<Collection> collections = new ArrayList<Collection>();
		Cursor cursor = cr.query(CONTENT_URI, null, null, null, DEFAULT_SORT_ORDER);
		if (cursor != null){
			if (cursor.moveToFirst()){
				do {
					Collection c = new Collection();
					c.id = cursor.getLong(cursor.getColumnIndex(_ID));
					c.name = cursor.getString(cursor.getColumnIndex(NAME));
					c.deviceAddress = cursor.getString(cursor.getColumnIndex(DEVICE_ADDRESS));
					collections.add(c);
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
		return collections;
	}

	/**
     * The MIME type of {@link #CONTENT_URI} providing a directory of collections.
     */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.media.mit.edu.android.collection";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single collection.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.media.mit.edu.android.collection";
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "name DESC";
    
    
    public static final String _ID = "_id";
    /**
     * The name of the collection
     * <P>Type: TEXT</P>
     */
	public static final String NAME = "name";
	
	public static final String DEVICE_ADDRESS = "device_address";
	

}
