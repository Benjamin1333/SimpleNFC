package de.simplenfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.util.Log;
import de.simplenfc.activity.NfcConnector;
import de.simplenfc.entity.NfcMessage;
import de.simplenfc.listener.NfcWriteListener;

/**
 * Main class to write NFC-tags and register NfcMessageHandler.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class Nfc {
	public static String PACKAGE_NAME;
	
	private Context context;
	private NfcMessageHandler messageHandler;
	
	
	/**
	 * Construct and Nfc-object.
	 * 
	 * @param context The context to use. Usually your Activity.
	 */
	public Nfc(Context context){
		this.context = context;
		if(PACKAGE_NAME == null){
			PACKAGE_NAME = context.getPackageName();
		}
	}
	
	/**
	 * Construct and Nfc-object.
	 * 
	 * @param context The context to use. Usually your Activity.
	 * @param 
	 * @deprecated Use only if minSdkVersion is lower than 14.
	 */
	public Nfc(Context context, String hans){
		this.context = context;
		if(PACKAGE_NAME == null){
			PACKAGE_NAME = context.getPackageName();
		}
	}
	
	/**
	 * Registers an activity as handler for received {@link NfcMessage}. The mapping between
	 * activity and {@link NfcMessage} is placed on messageId.
	 * 
	 * @param nfcMessageId Identification-string for the expected {@link NfcMessage}.
	 * @param activity Activity which handles the received {@link NfcMessage}.
	 */
	public void addNfcMessageHandler(String nfcMessageId, @SuppressWarnings("rawtypes") Class activity){
		if(messageHandler == null){
			messageHandler = NfcMessageHandler.getInstance(this.context);
		}
		messageHandler.registerMessageId(nfcMessageId, activity);
	}
	
	
	/**
	 * Handles a bunch of NfcMessages. Each {@link NfcMessage} will be checked by {@link NfcMessageHandler} 
	 * to find a registered activity.
	 * 
	 * @param messages Array of {@link NfcMessage}.
	 */
	public void handleMessages(NfcMessage[] messages){
		if(messageHandler == null){
			messageHandler = NfcMessageHandler.getInstance(this.context);
		}
		messageHandler.handleMessages(messages);
	}
	
	
	/**
	 * Scans the extras-bundle of the given Intent to find a {@link NfcMessage}.
	 * 
	 * @param intent Intent whose extras will be scanned to find a {@link NfcMessage}.
	 * @return true if a {@link NfcMessage} exists, otherwise false.
	 */
	public boolean containsNfcMessage(Intent intent){
		if(intent.hasExtra(NfcMessage.KEY_MESSAGE)) return true;
		else return false;
	}
	
	
	/**
	 * Returns the containing {@link NfcMessage}.
	 * 
	 * @param intent Intent whose extras will be scanned to find a {@link NfcMessage}.
	 * @return {@link NfcMessage} if existing or null if not.
	 */
	public NfcMessage getNfcMessage(Intent intent){
		return (NfcMessage) intent.getParcelableExtra(NfcMessage.KEY_MESSAGE);
	}
	
	
	/**
	 * Starts writemode to listen for existing NFC-tags. If a NFC-tag is located it will be written with 
	 * the given {@link NfcMessage}. 
	 * 
	 * @param message {@link NfcMessage} which will be written to an available NFC-tag.
	 * @param listener {@link NfcWriteListener} to handle Exceptions or success.
	 */
	public void startWriteMode(NfcMessage message, NfcWriteListener listener){
		if(listener != null){
			this.context.registerReceiver(new NfcConnectorStateReceiver(listener), new IntentFilter(NfcConnectorStateReceiver.ACTION_WRITER_STATECHANGED));
		}
		
		this.pendingToWrite(message);
	}
	
	
	/**
	 * Starts writemode to listen for existing NFC-tags. If a NFC-tag is located it will be written with 
	 * the given {@link NfcMessage}. 
	 * 
	 * @param message NfcMessage {@link NfcMessage} which will be written to an available NFC-tag.
	 */
	public void startWriteMode(NfcMessage message) {
		this.pendingToWrite(message);
	}
	
	
	private void pendingToWrite(NfcMessage message){
		Intent intent = new Intent(this.context, NfcConnector.class);
		intent.setExtrasClassLoader(NfcMessage.class.getClassLoader());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(NfcConnector.EXTRA_MODE, NfcConnector.MODE_WRITE);
		intent.putExtra(NfcMessage.KEY_MESSAGE, message.getRAWMessage().toByteArray());
		
		PendingIntent pendingIntent= PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NfcAdapter.getDefaultAdapter(this.context).enableForegroundDispatch((Activity) this.context, pendingIntent,
				new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) }
		, null);
	}
	
	public void startPushMode(Activity activity, NfcMessage message, NfcAdapter.CreateNdefMessageCallback callback){
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this.context);
		if(callback != null){

			
		}else{
			adapter.setNdefPushMessage(message.getRAWMessage(), activity, activity);
			adapter.setOnNdefPushCompleteCallback(new OnNdefPushCompleteCallback() {
				@TargetApi(14)
				@Override
				public void onNdefPushComplete(NfcEvent event) {
					Log.v("nfclabs", "onNdefPushComplete");
					
				}
			}, activity, activity);
		}
	}
}
