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

import edu.mit.media.android.amarino.IntentEventMapper;
import android.net.Uri;
import android.os.Bundle;

public class EventData {

	public static final int UNKNOWN = -1;
	public static final int FLOAT = 1;
	public static final int DOUBLE = 2;
	public static final int BYTE = 3;
	public static final int SHORT = 4;
	public static final int INTEGER = 5;
	public static final int LONG = 6;
	public static final int CHAR = 7;
	public static final int STRING = 8;
	public static final int BOOLEAN = 9;
	public static final int FLOAT_ARRAY = 10;
	public static final int DOUBLE_ARRAY = 11;
	public static final int BYTE_ARRAY = 12;
	public static final int SHORT_ARRAY =  13;
	public static final int INTEGER_ARRAY = 14;
	public static final int LONG_ARRAY = 15;
	public static final int CHAR_ARRAY = 16;
	public static final int STRING_ARRAY = 17;
	public static final int BOOLEAN_ARRAY = 18;
	public static final int CHARSEQUENCE = 19;
	
	public static final String[] types = {
		"Float", "Double", "Byte", "Short",
		"Integer", "Long", "Char", "String", "Boolean",
		"FloatArray", "DoubleArray", "ByteArray", "ShortArray",
		"IntegerArray", "LongArray", "CharArray", "StringArray", "BooleanArray",
		"CharSequence"
	};
	
	public long id = -1;
	public int type;
	public String key;
	
	
	public static String getTypeAsString(int type){
		if (type > 0 && type < 20)
			return types[type-1];
		else {
			return "unknown type";
		}
	}
	
	public static int getTypeAsInteger(String type){
		for (int i=0; i<types.length;i++){
			if (types[i].equals(type)) return i+1;
		}
		return UNKNOWN;
	}
	
	public static String extractData(EventData ed, Bundle b){
		switch (ed.type){
		// TODO think about return data more concise in terms of data types
		case FLOAT: 		return String.valueOf(b.getFloat(ed.key));
		case DOUBLE: 		return String.valueOf(b.getDouble(ed.key));
		case BYTE: 			return String.valueOf(b.getByte(ed.key));
		case SHORT: 		return String.valueOf(b.getShort(ed.key));
		case INTEGER: 		return String.valueOf(b.getInt(ed.key));
		case LONG: 			return String.valueOf(b.getLong(ed.key));		
		case CHAR: 			return String.valueOf(b.getChar(ed.key));
		case STRING: 		return String.valueOf(b.getString(ed.key));
		case BOOLEAN: 		return String.valueOf(b.getBoolean(ed.key));
		
		case FLOAT_ARRAY: 	
			float[] floats = b.getFloatArray(ed.key);
			String resultFloats = "";
			for (int i=0; i<floats.length; i++){
				resultFloats += floats[i];
				if (i+1<floats.length)
					resultFloats += IntentEventMapper.DELIMITER;
			}
			return resultFloats;
			
		case DOUBLE_ARRAY: 
			double[] doubles = b.getDoubleArray(ed.key);
			String resultDoubles = "";
			for (int i=0; i<doubles.length; i++){
				resultDoubles += doubles[i];
				if (i+1<doubles.length)
					resultDoubles += IntentEventMapper.DELIMITER;
			}
			return resultDoubles;
			
		case BYTE_ARRAY: 
			byte[] bytes = b.getByteArray(ed.key);
			String resultBytes = "";
			for (int i=0; i<bytes.length; i++){
				resultBytes += bytes[i];
				if (i+1<bytes.length)
					resultBytes += IntentEventMapper.DELIMITER;
			}
			return resultBytes;
			
		case SHORT_ARRAY:
			short[] shorts = b.getShortArray(ed.key);
			String resultShorts = "";
			for (int i=0; i<shorts.length; i++){
				resultShorts += shorts[i];
				if (i+1<shorts.length)
					resultShorts += IntentEventMapper.DELIMITER;
			}
			return resultShorts;
			
		case INTEGER_ARRAY:
			int[] ints = b.getIntArray(ed.key);
			String resultInts = "";
			for (int i=0; i<ints.length; i++){
				resultInts += ints[i];
				if (i+1<ints.length)
					resultInts += IntentEventMapper.DELIMITER;
			}
			return resultInts;
			
		case LONG_ARRAY:
			long[] longs = b.getLongArray(ed.key);
			String resultLongs = "";
			for (int i=0; i<longs.length; i++){
				resultLongs += longs[i];
				if (i+1<longs.length)
					resultLongs += IntentEventMapper.DELIMITER;
			}
			return resultLongs;
			
		case CHAR_ARRAY:
			char[] chars = b.getCharArray(ed.key);
			String resultChars = "";
			for (int i=0; i<chars.length; i++){
				resultChars += chars[i];
				if (i+1<chars.length)
					resultChars += IntentEventMapper.DELIMITER;
			}
			return resultChars;
			
		case STRING_ARRAY:
			String[] strings = b.getStringArray(ed.key);
			String resultStrings = "";
			for (int i=0; i<strings.length; i++){
				resultStrings += strings[i];
				if (i+1<strings.length)
					resultStrings += IntentEventMapper.DELIMITER;
			}
			return resultStrings;
			
		case BOOLEAN_ARRAY:
			boolean[] booleans = b.getBooleanArray(ed.key);
			String resultBooleans = "";
			for (int i=0; i<booleans.length; i++){
				resultBooleans += booleans[i];
				if (i+1<booleans.length)
					resultBooleans += IntentEventMapper.DELIMITER;
			}
			return resultBooleans;
		
		case CHARSEQUENCE:  return b.getCharSequence(ed.key).toString();
		
		default: return "";
		}
	}
	
	
	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + AmarinoProvider.AUTHORITY + "/eventdata");
	
	/**
     * The MIME type of {@link #CONTENT_URI} providing a directory of collections.
     */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.media.mit.edu.android.eventdata";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single collection.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.media.mit.edu.android.eventdata";
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "key DESC";
    
    
    public static final String _ID = "_id";
    
    /**
     * The type of the event
     * <P>Type: INTEGER</P>
     */
    public static final String EVENT_ID = "event_id";
    /**
     * The type of the data
     * <P>Type: INTEGER</P>
     */
	public static final String TYPE = "type";
	
	/**
     * The key to access the data
     * <P>Type: TEXT</P>
     */
	public static final String KEY = "key";
}
