package at.abraxas.amarino;;


public class AmarinoByteBuffer {
	
	private byte[] buffer;
	private int pointer = 0;
	public final String EOS_MARKER = ";";
	
	public AmarinoByteBuffer(int capacity){
		buffer = new byte[capacity];
		this.clear();
	}
	
	public static void main(String[] args) {

		AmarinoByteBuffer bb = new AmarinoByteBuffer(4);

		int a = 2000;
	    byte[] ret = new byte[4];
	    ret[3] = (byte) (a & 0xFF);   
	    ret[2] = (byte) ((a >> 8) & 0xFF);   
	    ret[1] = (byte) ((a >> 16) & 0xFF);   
	    ret[0] = (byte) ((a >> 24) & 0xFF);
			
		bb.put(ret);
		        
        System.out.println(bb.getInt(0));
        
        bb.clear();
        
        bb.put((byte) 'c');
        System.out.println(bb.getChar(0));
        
        bb.clear();
        
        bb.put((byte) 0x01);
        System.out.println(bb.getBoolean(0));
        
	}
	
	/**
	 * Adds a byte to the Buffer and increments the pointer by 1
	 * @param b
	 */
	public void put(byte b){
		buffer[pointer] =  b;
		pointer++;
	}
	
	/**
	 * Adds an array of bytes to the Buffer and increments the pointer by the size of the array
	 * @param b
	 */
	public void put(byte[] b){
		for(int i = 0; i < b.length; i++){
			buffer[pointer] = b[i];
			pointer++;
		}
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
	 * Int in Arduino (Uno/AtMega) is 2 bytes
	 * Int in Arduino Due is 4 bytes
	 * 
	 * @param pos
	 * @return
	 */
	public int getInt(int pos) {
		int value=0;

	    for (int i = 0; i < 2; i++) {
	        int shift = (2 - 1 - i) * 8;
	        value += (buffer[i] & 0xFF) << shift;
	    }
	    return value;

	}
	
	/**
	 * Int in Arduino (Uno/AtMega) is 2 bytes
	 * Int in Arduino Due is 4 bytes
	 * 
	 * @param pos
	 * @return
	 */
	public int[] getIntArray(int numValues){
		int[] ints = new int[numValues];
		
		for(int i = 0; i < numValues; i++){
			ints[i] = getInt(i);
		}		
		
		return ints;
	}
	
	/**
	 * Long in Arduino is 4 bytes
	 * 
	 * @param pos
	 * @return
	 */
	public int getLong(int pos) {
		int value=0;

	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (buffer[i] & 0xFF) << shift;
	    }
	    return value;

	}
	
	/**
	 * Long in Arduino is 4 bytes
	 * 
	 * @param pos
	 * @return
	 */
	public int[] getLongArray(int numValues){
		int[] ints = new int[numValues];
		
		for(int i = 0; i < numValues; i++){
			ints[i] = getInt(i);
		}		
		
		return ints;
	}
	
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
		return Float.intBitsToFloat(getInt(pos));
	}

	/**
	 * Float in Arduino is float in Android (4 bytes)
	 * @param pos
	 * @return
	 */
	public float[] getFloatArray(int numValues){
		float[] floats = new float[numValues];
		
		for(int i = 0; i < numValues; i++){		
			floats[i] = getFloat(i);	
		}
			
		return floats;
	}
	
	/**
	 * Short in Arduino are 2 bytes
	 * @param pos
	 * @return
	 */
	public short getShort(int pos){
		short value=0;
		
		for(int i = 0; i < 2; i++){
			if(buffer[pos*2+i] == -1) throw new IllegalArgumentException("No Value written to this position of ByteBuffer");
			value = (short) (value | buffer[pos*2+i] << 1-i*8);
		}
		
		return value;
	}
	
	/**
	 * Short in Arduino are 2 bytes
	 * @param pos
	 * @return
	 */
	public short[] getShortArray(int numValues){
		short[] shorts = new short[numValues];
		
		for(int i = 0; i < numValues; i++){
			shorts[i] = getShort(i);
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


