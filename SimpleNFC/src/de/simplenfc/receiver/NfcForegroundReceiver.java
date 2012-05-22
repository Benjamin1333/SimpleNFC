package de.simplenfc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.os.Bundle;
import de.simplenfc.activity.NfcConnector;
import de.simplenfc.entity.NfcMessage;
import de.simplenfc.listener.NfcForegroundListener;
import de.simplenfc.listener.NfcWriteListener;

/**
 * BroadcastReceiver to receive state changes from the {@link NfcConnector}. Receives Exception-messages and
 * success-messages.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class NfcForegroundReceiver extends BroadcastReceiver {
	public static final String ACTION_MESSAGE_RECEIVED = "simplenfc_writer_statechanged";
	public static final String EXTRA_NFCMESSAGE = "nfcmessage";
	public static final String EXTRA_NDEFMESSAGE = "ndefmessage";
	
	private NfcForegroundListener mListener;
	
	
	/**
	 * Construct a NfcConnectorStateReceiver.
	 * 
	 * @param listener {@link NfcWriteListener} to call on exceptions or success.
	 */
	public NfcForegroundReceiver(NfcForegroundListener listener){
		mListener = listener;
	}


	/**
	 * Called for every received Intent. See <a href="http://developer.android.com/reference/android/content/BroadcastReceiver.html#onReceive(android.content.Context, android.content.Intent)">Android-documentation</a>
	 * 
	 * @param context The context to use. Usually your Activity.
	 * @param intent Received Intent. Contains Exceptions or Success-message.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(ACTION_MESSAGE_RECEIVED)){
			Bundle b = intent.getExtras();
			
			if(b.containsKey(EXTRA_NFCMESSAGE)){
				mListener.onNfcMessageReceived((NfcMessage) b.getParcelable(EXTRA_NFCMESSAGE));
			}else if(b.containsKey(EXTRA_NDEFMESSAGE)){
				mListener.onNdefMessageReceived((NdefMessage)b.getParcelable(EXTRA_NDEFMESSAGE));
			}
		}
	}
}
