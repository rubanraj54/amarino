package at.abraxas.amarino;

/**
 * 
 * @author matthias vavti
 */
public class LANDevice extends Device{

	private static final long serialVersionUID = 5459640043775601480L;

	public LANDevice(long id, String address, String name) {
		super(id, address, name);
	}
	
	public LANDevice(String address){
		super(address);
	}

	@Override
	public int getType() {
		return Device.LANDEVICE;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (o == null || (o.getClass() != this.getClass()))
			return false;
		
		LANDevice other = (LANDevice)o;
		if (this.id == other.id && this.id != -1) {
			return true;
		}
		if (this.address.equals(other.address)){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "LAN " + address + " - " + name;
	}

	@Override
	public Device clone() {
		LANDevice device = new LANDevice(this.id, this.address, this.name);
		device.state = this.state;
		return device;
	}

}
