package at.abraxas.amarino;


import java.io.Serializable;
import java.util.HashMap;

public abstract class Device implements Serializable{

private static final long serialVersionUID = -6041931825295548358L;
	
	public static final int BTDEVICE = 1;
	public static final int LANDEVICE = 2;
	
	public static final int _16BIT = 1;
	public static final int _32BIT = 2;

	long id = -1;
	String address;
	String name;
	int state = AmarinoIntent.DISCONNECTED;
	// <pluginID, event>
	HashMap<Integer, Event> events;
	int architecture = -1;
	
	public Device(String address){
		this.address = address;
	}
	
	public Device(long id, String address, String name){
		this.id = id;
		this.address = address;
		this.name = name;
	}
	
	public Device(String address, String name){
		this.address = address;
		this.name = name;
	}
		
	public String getAddress(){
		return this.address;
	}
	
	public String getName(){
		return this.name;
	}
	
	public abstract int getType();
	
	public abstract boolean equals(Object o);

	public abstract Device clone();
	
	@Override
	public int hashCode() {
		int hash = 7;
		for (int i=0;i<address.length();i++)
			hash += address.charAt(i);
		return hash;
	}

	@Override
	public abstract String toString();

	public int getArchitecture() {
		return architecture;
	}

	public void setArchitecture(int architecture) {
		this.architecture = architecture;
	}
	
	
}
