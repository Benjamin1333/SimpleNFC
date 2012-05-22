package de.simplenfc.listener.adapter;

import java.io.IOException;

/**
 * Adapter for the interface {@link NfcWriteListener}. All implemented methods
 * are stubs without any function.
 * 
 */
import android.nfc.FormatException;
import de.simplenfc.entity.NfcMessage;
import de.simplenfc.entity.exceptions.LowCapacityException;
import de.simplenfc.entity.exceptions.NDEFException;
import de.simplenfc.entity.exceptions.NfcDisabledException;
import de.simplenfc.entity.exceptions.ReadOnlyException;
import de.simplenfc.listener.NfcWriteListener;

public abstract class NfcWriteAdapter implements NfcWriteListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNfcException(NfcDisabledException e) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNfcException(IOException e) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNfcException(FormatException e) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNfcException(ReadOnlyException e) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNfcException(LowCapacityException e) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNfcException(NDEFException e) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNfcMessageWritten() {}

}
