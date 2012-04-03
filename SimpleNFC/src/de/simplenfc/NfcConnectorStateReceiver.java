package de.simplenfc;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;

import de.simplenfc.R;
import de.simplenfc.activity.NfcConnector;
import de.simplenfc.entity.exceptions.LowCapacityException;
import de.simplenfc.entity.exceptions.NDEFException;
import de.simplenfc.entity.exceptions.NfcDisabledException;
import de.simplenfc.entity.exceptions.ReadOnlyException;
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
public class NfcConnectorStateReceiver extends BroadcastReceiver {
	public static final String ACTION_WRITER_STATECHANGED = "simplenfc_writer_statechanged";
	public static final String EXTRA_EXCEPTION_NFCDISABLED = "nfcdisabledexception";
	public static final String EXTRA_EXCEPTION_IO = "ioexception";
	public static final String EXTRA_EXCEPTION_FORMAT = "formatexception";
	public static final String EXTRA_EXCEPTION_READONLY = "readonlyexception";
	public static final String EXTRA_EXCEPTION_LOWCAPACITY = "lowcapacityexception";
	public static final String EXTRA_EXCEPTION_NDEF = "ndefexception";
	public static final String EXTRA_WRITTEN = "tag_written";
	
	private NfcWriteListener listener;
	
	
	/**
	 * Construct a NfcConnectorStateReceiver.
	 * 
	 * @param listener {@link NfcWriteListener} to call on exceptions or success.
	 */
	public NfcConnectorStateReceiver(NfcWriteListener listener){
		this.listener = listener;
	}


	/**
	 * Called for every received Intent. See <a href="http://developer.android.com/reference/android/content/BroadcastReceiver.html#onReceive(android.content.Context, android.content.Intent)">Android-documentation</a>
	 * 
	 * @param context The context to use. Usually your Activity.
	 * @param intent Received Intent. Contains Exceptions or Success-message.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(ACTION_WRITER_STATECHANGED)){
			Bundle b = intent.getExtras();
			
			if(b.containsKey(EXTRA_EXCEPTION_NFCDISABLED)){
				this.listener.onNfcException((NfcDisabledException)b.getSerializable(EXTRA_EXCEPTION_NFCDISABLED));
			}else if(b.containsKey(EXTRA_EXCEPTION_IO)){
				this.listener.onNfcException((IOException)b.getSerializable(EXTRA_EXCEPTION_IO));
			}else if(b.containsKey(EXTRA_EXCEPTION_FORMAT)){
				this.listener.onNfcException((FormatException)b.getSerializable(EXTRA_EXCEPTION_FORMAT));
			}else if(b.containsKey(EXTRA_EXCEPTION_READONLY)){
				this.listener.onNfcException((ReadOnlyException)b.getSerializable(EXTRA_EXCEPTION_READONLY));
			}else if(b.containsKey(EXTRA_EXCEPTION_LOWCAPACITY)){
				this.listener.onNfcException((LowCapacityException)b.getSerializable(EXTRA_EXCEPTION_LOWCAPACITY));
			}else if(b.containsKey(EXTRA_EXCEPTION_NDEF)){
				this.listener.onNfcException((NDEFException)b.getSerializable(EXTRA_EXCEPTION_NDEF));
			}else if(b.containsKey(EXTRA_WRITTEN)){
				context.unregisterReceiver(this);
				this.listener.onNfcMessageWritten();
			}
		}
	}
}
