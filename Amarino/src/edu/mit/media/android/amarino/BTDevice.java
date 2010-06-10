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

public class BTDevice {

	public static final int PAIRED = 1;
	public static final int NOT_PAIRED = 2;
	public static final int CONNECTED = 3;
	public static final int CONNECTING = 4;
	
	String address = new String();
	String name = new String();
	int state = NOT_PAIRED;
	
	public boolean isPaired(){
		return state==PAIRED ? true : false;
	}
	
	public boolean equals(Object o){
		if (this == o)
			return true;
		
		if (o == null || (o.getClass() != this.getClass()))
			return false;
		
		if (address.equals( ((BTDevice)o).address)){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		for (int i=0;i<address.length();i++)
			hash += address.charAt(i);
		return hash;
	}

	@Override
	public String toString() {
		return address + " - " + name;
	}
	
	
	
}
