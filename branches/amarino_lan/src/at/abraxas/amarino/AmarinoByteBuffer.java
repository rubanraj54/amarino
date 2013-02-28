package at.abraxas.amarino;

public class AmarinoByteBuffer {
	
	private byte[] buffer;
	private int pointer = 0;
	
	public AmarinoByteBuffer(int capacity){
		buffer = new byte[capacity];
		this.clear();
	}
	
	public static void main(String[] args) {

		AmarinoByteBuffer bb = new AmarinoByteBuffer(16);

		
		bb.put((byte) 2);
		bb.put((byte) 0);
		bb.put((byte) 0);
		bb.put((byte) 0);
		bb.put((byte) 5);
		
		System.out.println(bb.getIntArray(2)[1]);
		
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
	
	public int getInt() {
		String s = "";
		for(int i = 0; i < 4; i++)
			s+=buffer[i];
	    
	    return new Integer(s);
	}
	
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
	
	public long getLong(){
		
	}
	
	public long[] getLongArray(int numValues){
		
	}
	
	public boolean getBoolean(){
		if(buffer[0] == 1) return true;
		else return false;
	}
	
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
	
	public byte getByte(){
		return buffer[0];
	}
	
	public byte[] getByteArray(int numValues){
		byte[] bytes = new byte[numValues];
			
		for(int i = 0; i < numValues; i++){
			if(buffer[i] != -1)
				bytes[i] = buffer[i];
		}
		
		return bytes;
	}
	
	public char getChar(){
		
	}
	
	public char[] getCharArray(int numValues){
		
	}
	
	public float getFloat(){
		
	}

	public float[] getFloatArray(int numValues){
		
	}
	
	public short getShort(){
		
	}
	
	public short[] getShortArray(int numValues){
		
	}
	
	public String getString(){
		//TODO: for Strings we need to transmit an EOS (End of String) indicator to know when the String ends
		//		for Example ";"
	}
	
	public String[] getStringArray(int numValues){
		//TODO: for String Arrays we need a Delimiter between Strings (take EOS indicator)
		
	}
}


