package de.simplenfc.listener;

import android.nfc.NdefMessage;
import de.simplenfc.entity.NfcMessage;

/**
 * Interface for the NfcForegroundListener. Methods for handling foregrounddispatch.
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 */
public interface NfcForegroundListener {
	
	/**
	 * Called if a {@link NfcMessage} is read.
	 * @param message Message which is read.
	 */
	public void onNfcMessageReceived(NfcMessage message);
	
	/**
	 * Called if a {@link NdefMessage} is read.
	 * @param message Message which is read.
	 */
	public void onNdefMessageReceived(NdefMessage message);
}
