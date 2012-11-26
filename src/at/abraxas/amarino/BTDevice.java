/*
  Amarino - A prototyping software toolkit for Android and Arduino
  Copyright (c) 2010 Bonifaz Kaufmann.  All right reserved.
  
  This application and its library is free software; you can redistribute
  it and/or modify it under the terms of the GNU Lesser General Public
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
package at.abraxas.amarino;

import it.gerdavax.easybluetooth.RemoteDevice;


/**
 * $Id: BTDevice.java 444 2010-06-10 13:11:59Z abraxas $
 */
public class BTDevice extends Device{
		
//	long id = -1;
//	String address;
//	String name;
//	int state = AmarinoIntent.DISCONNECTED;
//	 <pluginID, event>
//	HashMap<Integer, Event> events;
	
	private static final long serialVersionUID = 2760317150789338947L;

	public BTDevice(String address){
		super(address);
	}
	
	public BTDevice(long id, String address, String name){
		super(id, address, name);
	}
	
	public BTDevice(RemoteDevice rd){
		super(rd.getAddress(), rd.getFriendlyName());
	}
		
	public boolean equals(Object o){
		if (this == o)
			return true;
		
		if (o == null || (o.getClass() != this.getClass()))
			return false;
		
		BTDevice other = (BTDevice)o;
		if (this.id == other.id && this.id != -1) {
			return true;
		}
		if (this.address.equals(other.address)){
			return true;
		}
		return false;
	}
	
	@Override
	public BTDevice clone(){
		BTDevice device = new BTDevice(this.id, this.address, this.name);
		device.state = this.state;
		return device;
	}

	@Override
	public String toString() {
		return "BT " + address + " - " + name;
	}
	
	@Override
	public int getType(){
		return Device.BTDEVICE;
	}

}
