package de.simplenfc.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import de.simplenfc.Nfc;

/**
 * The {@link NfcMessage} is the container for all {@link NfcRecord}s and simplifies the process
 * of creating the correct arguments. Simply pass an ID to the constructor, add {@link Nfcrecord}s
 * and it is ready to be written onto a tag. 
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class NfcMessage implements Parcelable{
	public static final String KEY_MESSAGE = "nfc_message";
	
	private NdefMessage message;
	private String ID;
	
	
	/**
	 * Constructs a new {@link NfcMessage} with all parameters set to standard values.
	 * @param messageID An ID for the new message.
	 */
	public NfcMessage(String messageID){
		this.ID = messageID;
		NdefRecord mimeTypRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, ("application/"+Nfc.PACKAGE_NAME).getBytes(), messageID.getBytes(), new byte[] {});
		this.message = new NdefMessage(new NdefRecord[]{mimeTypRecord});
		Log.v("nfclavs", "packagename: "+Nfc.PACKAGE_NAME);
	}
	
	protected NfcMessage(Parcel in){
		byte bytes[] = in.createByteArray();
		try {
			this.message = new NdefMessage(bytes);
		} catch (FormatException e) {
			e.printStackTrace();
		}
		NdefRecord[] records = this.message.getRecords();
		this.ID = new String(records[records.length-1].getId());
	}
	
	protected NfcMessage(NdefMessage msg){
		this.message = msg;
		this.ID = new String(this.message.getRecords()[0].getId());
	}
	
	
	/**
	 * Adds a String-record to the message.
	 * @param id An ID as String.
	 * @param content The content of the new record as a String.
	 */
	public void addRecord(String id, String content){
		 ArrayList<NdefRecord> records = new ArrayList<NdefRecord>(Arrays.asList(this.message.getRecords()));
		 //new NdefRecord(tnf, type, id, payload)
		 records.add(new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NfcRecord.STRING_RECORD.getBytes(), id.getBytes(), content.getBytes()));
		 this.message = new NdefMessage(records.toArray(new NdefRecord[]{}));
	}
	
	
	/**
	 * Adds a {@link HashMap}-record to the message.
	 * @param id An ID as String.
	 * @param map The content of the new record as a {@link HashMap}.
	 * @throws IOException May be thrown while converting the HashMap into ByteStream.
	 * @throws ClassNotFoundException May be thrown while converting the HashMap into ByteStream.
	 */
	public void addRecord(String id, HashMap<String, String> map) throws IOException, ClassNotFoundException{
		 ArrayList<NdefRecord> records = new ArrayList<NdefRecord>(Arrays.asList(this.message.getRecords()));
		 //new NdefRecord(tnf, type, id, payload)
		 
		 ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		 ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
		 objectOut.writeObject(map);
		 objectOut.close();
		 
		 records.add(new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NfcRecord.MAP_RECORD.getBytes(), id.getBytes(), byteOut.toByteArray()));
		 this.message = new NdefMessage(records.toArray(new NdefRecord[]{}));
	}
	
	
	/**
	 * Compatibility to NdefRecords.
	 * @param id An ID as String.
	 * @param TNF a 3-bit TNF constant.
	 * @param type byte array, containing zero to 255 bytes, must not be null.
	 * @param payload byte array, containing zero to (2 ** 32 - 1) bytes, must not be null.
	 */
	public void addRecord(String id, short TNF, byte[] type, byte[] payload){
		 ArrayList<NdefRecord> records = new ArrayList<NdefRecord>(Arrays.asList(this.message.getRecords()));
		 //new NdefRecord(tnf, type, id, payload)
		 records.add(new NdefRecord(TNF, type, id.getBytes(), payload));
		 this.message = new NdefMessage(records.toArray(new NdefRecord[]{}));
	}
	
	
	/**
	 * Returns all {@link NfcRecord}s of the message.
	 * @return All Records as {@link NfcRecord}s.
	 */
	public HashMap<String, NfcRecord> getRecords(){
		NdefRecord[] rawRecords = this.message.getRecords();
		int length = rawRecords.length;
		HashMap<String, NfcRecord> simpleRecords = new HashMap<String, NfcRecord>(length);
		NfcRecord simpleRecord = null;
		
		for(int i=1; i<length; i++){
			simpleRecord = new NfcRecord(rawRecords[i]);
			simpleRecords.put(simpleRecord.getID(), simpleRecord);
		}
		
		return simpleRecords;
	}
	
	
	/**
	 * Returns a single {@NdefRecord} with given ID, if not found, return null.
	 * @param id The ID of the {@link NfcRecord} to return as a String.
	 * @return {@link NdefRecord}.
	 * @deprecated Hier müssen wir noch mal reinschauen, sollte eigentlich ein {@link NfcRecord} sein.
	 */
	public NdefRecord getRecord(String id){
		NdefRecord[] rawRecords = this.message.getRecords();
		int length = rawRecords.length;
		for(int i=1; i<length; i++){
			if(new String(rawRecords[i].getId()).equals(id)) return rawRecords[i];
		}
		
		return null;
	}
	
	
	/**
	 * German description of the message.
	 * @return German description of the message (ID and number of records and IDs).
	 */
	@Override
	public String toString() {
		NdefRecord[] records = this.message.getRecords();
		int length = records.length;
		String IDs = "";
		for(int i=0; i<length; i++){
			IDs += new String(records[i].getId());
			if(i != length-1) IDs += ", ";
		}
		
		return "SimpleNFCMessage '" + this.ID + "' mit " + records.length + " records (" + IDs+")";
	}
	

	/**
	 * Return a simple {@link NdefMessage} without the comfort of a {@link NfcMessage}
	 * @return Return a simple {@link NdefMessage} without the comfort of a {@link NfcMessage}
	 */
	public NdefMessage getRAWMessage(){
		return this.message;
	}
	
	
	/**
	 * @return The ID of the message.
	 */
	public String getID(){
		return this.ID;
	}

	
	/**
	 * Interface to declare objects which could parse {@link NfcMessages} from {@link NdefMessage}s.
	 * 
	 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
	 * @author Dennis Becker (simplenfc@denbec.de)
	 * @version 1.0
	 *
	 */
	public interface Parser{
		public NfcMessage parseFromNdefMessage(NdefMessage msg);
	}
	
	
	/**
	 * Implementation of the {@link Parser}-Interface.
	 */
	public static final NfcMessage.Parser PARSER = new NfcMessage.Parser() {
		@Override
		public NfcMessage parseFromNdefMessage(NdefMessage msg) {
			return new NfcMessage(msg);
		}
	};
	

	
	/**
	 * Implementation of the Parcelable.Creator<T>-Interface.
	 */
	public static final Parcelable.Creator<NfcMessage> CREATOR = new Parcelable.Creator<NfcMessage>() {
		@Override
		public NfcMessage createFromParcel(Parcel in) {
			return new NfcMessage(in);
		}

		@Override
		public NfcMessage[] newArray(int size) {
			return new NfcMessage[size];
		}
	};
	
	
	/**
	 * see <a href="http://developer.android.com/reference/android/os/Parcelable.html#describeContents()">Android-documentation</a> 
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	
	/**
	 * see <a href="http://developer.android.com/reference/android/os/Parcelable.html#writeToParcel(android.os.Parcel, int)">Android-documentation</a>
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByteArray(this.message.toByteArray());
	}
}
