package de.simplenfc.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;

import android.nfc.NdefRecord;
/**
 * Replacement for the NdefRecord class, including type of content and ID.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class NfcRecord {
	public static final String STRING_RECORD = "string";
	public static final String MAP_RECORD = "map";
	
	protected NdefRecord mRecord;
	
	
	/**
	 * Constructs new NfcRecord.
	 * @param record A typical NdefRecord.
	 */
	public NfcRecord(NdefRecord record){
		this.mRecord = record;
	}
	
	
	/**
	 * @return ID of record.
	 */
	public String getID(){
		return new String(this.mRecord.getId());
	}
	
	/**
	 * @return Type of record.
	 */
	public String getType(){
		return new String(this.mRecord.getType());
	}
	
	/**
	 * Returns the content of the record, based on its type.
	 * @return Returns either HashMap or String, based on type of record
	 */
	@SuppressWarnings("unchecked")
	public Object getContent(){
		if(this.getType().equals(MAP_RECORD)){
			ByteArrayInputStream byteIn = new ByteArrayInputStream(mRecord.getPayload());
			ObjectInputStream objectIn;
			HashMap<String, String> map = null;
			try {
				objectIn = new ObjectInputStream(byteIn);
				map = (HashMap<String, String>) objectIn.readObject();
				objectIn.close();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			return map;
		}else{
			return new String(this.mRecord.getPayload());
		}
	}
}