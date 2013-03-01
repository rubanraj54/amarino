package at.abraxas.amarino;

public class AmarinoByteBuffer {
	
	private byte[] buffer;
	private int pointer = 0;
	public final String EOS_MARKER = ";";
	
	public AmarinoByteBuffer(int capacity){
		buffer = new byte[capacity];
		this.clear();
	}
	
	public static void main(String[] args) {

		AmarinoByteBuffer bb = new AmarinoByteBuffer(16);

		
		bb.put((byte) 1);
		bb.put((byte) 0);
		bb.put((byte) 1);
		bb.put((byte) 0);
		bb.put((byte) 1);
		
		System.out.println(bb.getBooleanArray(5)[4]);
		
	}
	
	/**
	 * Adds a byte to the Buffer and increments the pointer by 1
	 * @param b
	 */
	public void put(byte b){
		buffer[pointer] = b;
		pointer++;
	}
	
	/**
	 * Clears the Buffer by overwriting every position with -1
	 */
	public void clear(){
		for(int i = 0; i < buffer.length; i++)
			buffer[i] = -1;
		
		pointer = 0;
	}
	
	/**
	 * Int in Arduino is 4 bytes
	 * @param pos
	 * @return
	 */
	public int getInt(int pos) {
		String s = "";
		for(int i = 0; i < 4; i++)
			s+=buffer[4*pos+i];
	    
	    return new Integer(s);
	}
	
	/**
	 * Int in Arduino is 4 bytes
	 * @param pos
	 * @return
	 */
	public int[] getIntArray(int numValues){
		int[] ints = new int[numValues];
		
		for(int i = 0; i < numValues; i++){
			String s = "";
			for(int j = 0; j < 4; j++){
				if(buffer[i+j] != -1)
					s+=buffer[i+j];
			}
			
			ints[i] = new Integer(s);
		}		
		return ints;
	}
	
//	public long getLong(int pos){
//		return -1;
//		
//	}
//	
//	public long[] getLongArray(int numValues){
//		return new long[numValues];
//	}
	
	/**
	 * Boolean in Arduino is 1 byte (0 or 1)
	 * @param pos
	 * @return
	 */
	public boolean getBoolean(int pos){
		if(buffer[pos] == 1) return true;
		else return false;
	}
	
	/**
	 * Boolean in Arduino is 1 byte (0 or 1)
	 * @param pos
	 * @return
	 */
	public boolean[] getBooleanArray(int numValues){
		boolean[] bools = new boolean[numValues];
		
		for(int i = 0; i < numValues; i++){
			//if position is -1 no value was written to this position
			if(buffer[i] != -1){ 
				if(buffer[i] == 1) bools[i] = true;
				else bools[i] = false;
			}
		}
		
		return bools;
	}
	
	public byte getByte(int pos){
		return buffer[pos];
	}
	
	public byte[] getByteArray(int numValues){
		byte[] bytes = new byte[numValues];
			
		for(int i = 0; i < numValues; i++){
			if(buffer[i] != -1)
				bytes[i] = buffer[i];
		}
		
		return bytes;
	}
	
	/**
	 * Chars in Arduino have 1 byte
	 * @param pos
	 * @return
	 */
	public char getChar(int pos){
		return (char) buffer[pos];
	}
	
	/**
	 * Chars in Arduino have 1 byte
	 * @param numValues
	 * @return
	 */
	public char[] getCharArray(int numValues){
		char[] chars = new char[numValues];
		
		for(int i = 0; i < numValues; i++){
			if(buffer[i] != -1)
				chars[i] = (char) buffer[i];
		}
		
		return chars;
	}
	
	/**
	 * Float in Arduino is float in Android (4 bytes)
	 * @param pos
	 * @return
	 */
	public float getFloat(int pos){
		String s = "";
		for(int i = 0; i < 4; i++)
			s+=buffer[4*pos+i];
	    
	    return new Float(s);
	}

	/**
	 * Float in Arduino is float in Android (4 bytes)
	 * @param pos
	 * @return
	 */
	public float[] getFloatArray(int numValues){
		float[] floats = new float[numValues];
		
		for(int i = 0; i < numValues; i++){
			String s = "";
			for(int j = 0; j < 4; j++){
				if(buffer[i+j] != -1)
					s+=buffer[i+j];
			}
			
			floats[i] = new Float(s);
		}		
		return floats;
	}
	
	/**
	 * Short in Arduino are 2 bytes
	 * @param pos
	 * @return
	 */
	public short getShort(int pos){
		String s = "";
		for(int i = 0; i < 2; i++)
			s+=buffer[2*pos+i];
	    
	    return new Short(s);
	}
	
	/**
	 * Short in Arduino are 2 bytes
	 * @param pos
	 * @return
	 */
	public short[] getShortArray(int numValues){
		short[] shorts = new short[numValues];
		
		for(int i = 0; i < numValues; i++){
			String s = "";
			for(int j = 0; j < 2; j++){
				if(buffer[i+j] != -1)
					s+=buffer[i+j];
			}
			
			shorts[i] = new Short(s);
		}		
		return shorts;
	}
	
	public String getString(int pos){
		//TODO: for Strings we need to transmit an EOS (End of String) indicator to know when the String ends
		//		for Example ";"
		
		String s = "";
		for(int i = 0; i < buffer.length; i++)
			s+= buffer[i]; //assuming chars from Arduino have only 1 byte
		
		String[] strings = s.split(EOS_MARKER);

		return strings[pos];
	}
	
	public String[] getStringArray(int numValues){
		//TODO: for String Arrays we need a Delimiter between Strings (take EOS indicator)
		
		String s = "";
		for(int i = 0; i < buffer.length; i++)
			s+= buffer[i]; //assuming chars from Arduino have only 1 byte
		
		String[] strings = s.split(EOS_MARKER);
		
		return strings;
		

	}
}


