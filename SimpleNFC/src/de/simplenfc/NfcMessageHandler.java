package de.simplenfc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.simplenfc.entity.NfcMessage;
/**
 * Singleton class to add handler for messages and call those handlers, based on a given message.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class NfcMessageHandler {
	private static final String TAG = "NfcMessageHandler";
	private static NfcMessageHandler INSTANCE;
	private static final String FILENAME = "handler";
	
	private Context mContext;
	private HashMap<String, String> mMessageId2Class;
	
	
	/**
	 * Constructs a new NfcMessageHandler in the given context
	 * @param context The context to use. Usually your Activity.
	 */
	private NfcMessageHandler(Context context){
		this.mContext = context.getApplicationContext();
		this.mMessageId2Class = new HashMap<String, String>();
		this.deserializeFromFile();
	}
	
	
	/**
	 * Singleton-Pattern - Access to Instance.
	 * @param c The context to use. Usually your Activity.
	 * @return The message handler.
	 */
	public static NfcMessageHandler getInstance(Context c){
		if(INSTANCE == null){
			return INSTANCE = new NfcMessageHandler(c);
		}else{
			return INSTANCE;
		}
	}
	
	
	/**
	 * Open Activities based on the messages passed.
	 * @param messages Array of messages, which handlers should be opened.
	 * @return If there is no associated handler, returns false otherwise true.
	 */
	public boolean handleMessages(NfcMessage[] messages){
		if(messages == null || messages.length == 0) return false;

		for (NfcMessage msg : messages) {
			if(this.mMessageId2Class.containsKey(msg.getID())){
				String className = this.mMessageId2Class.get(msg.getID());
				Intent intent = null;
				try {
					intent = new Intent(this.mContext, Class.forName(className));
				} catch (ClassNotFoundException e) {
					Log.e(TAG, e.getMessage());
				}
				intent.putExtra(NfcMessage.KEY_MESSAGE, msg);
				
				this.mContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Register Message ID an pass appropriate handler class that is opened later on.
	 * @param messageId The message ID as a String.
	 * @param activity A class that is called, once the message is passed to the instance.
	 */
	public void registerMessageId(String messageId, @SuppressWarnings("rawtypes") Class activity){
		this.mMessageId2Class.put(messageId, activity.getName());
		
		this.serializeToFile();
	}
	
	private void serializeToFile(){
		try {
			JSONObject json = new JSONObject(mMessageId2Class);
			
			FileOutputStream fos = mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(json.toString().getBytes());
			bos.close();
			fos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void deserializeFromFile(){
		if(!Arrays.asList(mContext.fileList()).contains(FILENAME)){
			return;
		}
		
		try {
			FileInputStream fis = mContext.openFileInput(FILENAME);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
			    content.append(line);
			}
			fis.close();
			
			mMessageId2Class = new HashMap<String, String>();
			JSONObject json = new JSONObject(content.toString());
			Iterator<String> keys = json.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				mMessageId2Class.put(key, json.getString(key));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
