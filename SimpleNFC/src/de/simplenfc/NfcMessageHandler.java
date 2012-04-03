package de.simplenfc;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
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
	private static NfcMessageHandler INSTANCE;
	
	private Context context;
	private HashMap<String, Intent> messageId2Intent;
	
	
	/**
	 * Constructs a new NfcMessageHandler in the given context
	 * @param context The context to use. Usually your Activity.
	 */
	private NfcMessageHandler(Context context){
		this.context = context.getApplicationContext();
		this.messageId2Intent = new HashMap<String, Intent>();
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
			if(this.messageId2Intent.containsKey(msg.getID())){
				Intent intent = this.messageId2Intent.get(msg.getID());
				intent.putExtra(NfcMessage.KEY_MESSAGE, msg);
				
				this.context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
		this.messageId2Intent.put(messageId, new Intent(this.context, activity));
	}
}
