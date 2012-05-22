package de.simplenfc.listener;


/**
 * Interface for the NfcBeamListener. Methods for handling a push attempt.
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 */
public interface NfcBeamListener {
	/**
	 * Pushing NFCMessage to an NFC-enabled device succeeded..
	 */
	public void onNfcMessagePushed();
}
