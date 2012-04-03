package de.simplenfc.listener;

import java.io.IOException;

import android.nfc.FormatException;

import de.simplenfc.R;
import de.simplenfc.entity.exceptions.LowCapacityException;
import de.simplenfc.entity.exceptions.NDEFException;
import de.simplenfc.entity.exceptions.NfcDisabledException;
import de.simplenfc.entity.exceptions.ReadOnlyException;

/**
 * Interface for the NfcWriteListener. Methods for handling a writing attempt.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public interface NfcWriteListener {
	
	
	/**
	 * Called if Nfc is disabled and NfcDisabledException is thrown.
	 * @param e Thrown {@link NfcDisabledException}.
	 */
	public void onNfcException(NfcDisabledException e);
	
	
	/**
	 * Called if IOException is thrown.
	 * @param e Thrown {@link IOException}.
	 */
	public void onNfcException(IOException e);
	
	
	/**
	 * Called if something goes wrong during formatting the NFC-tag.
	 * @param e Thrown {@link FormatException}.
	 */
	public void onNfcException(FormatException e);
	
	
	/**
	 * If NFC-tag is readonly ReadOnlyException is thrown.
	 * @param e Thrown {@link ReadOnlyException}.
	 */
	public void onNfcException(ReadOnlyException e);
	
	
	/**
	 * Called if NFC-tags capacity is too small.
	 * @param e Thrown {@link LowCapacityException}.
	 */
	public void onNfcException(LowCapacityException e);
	
	
	/**
	 * Called if NFC-tag doesn't support NdefMessages.
	 * @param e Thrown {@link NDEFException}.
	 */
	public void onNfcException(NDEFException e);
	
	
	/**
	 * Wrtiting NFCMessage on NFC-tag succeeded..
	 */
	public void onNfcMessageWritten();
}
