package de.simplenfc.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import de.simplenfc.Nfc;
import de.simplenfc.NfcConnectorStateReceiver;
import de.simplenfc.entity.NfcMessage;
import de.simplenfc.entity.exceptions.LowCapacityException;
import de.simplenfc.entity.exceptions.NDEFException;
import de.simplenfc.entity.exceptions.NfcDisabledException;
import de.simplenfc.entity.exceptions.ReadOnlyException;

public class NfcConnector extends Activity {
	public static final String EXTRA_MODE = "mode";
	public static final String MODE_WRITE = "write";
	public static final String MODE_READ = "read";
	
	
	/* **************************************** */
	/* ************ event handler ************* */
	/* **************************************** */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.handleIntent(this.getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		this.handleIntent(intent);
	}
	
	
	/* **************************************** */
	/* *********** private methods ************ */
	/* **************************************** */
	
	private void handleIntent(Intent intent){
		if (intent.hasExtra(EXTRA_MODE) && intent.getStringExtra(EXTRA_MODE).equals(MODE_WRITE)) {
			this.writeMessage(intent);
		} else {
			this.readMessage(intent);
		}
	}
	
	private void readMessage(Intent intent){
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if(rawMessages != null){
				NfcMessage[] parsedMessages = new NfcMessage[rawMessages.length];
				
				for (int i=0; i<rawMessages.length; i++) {
					NdefMessage msg = (NdefMessage) rawMessages[i];
					parsedMessages[i] = NfcMessage.PARSER.parseFromNdefMessage(msg);
				}
				
				new Nfc(this).handleMessages(parsedMessages);
			}
			
			this.finish();
		}
	}
	
	private void writeMessage(Intent intent) {
		Intent broadcastIntent = new Intent(NfcConnectorStateReceiver.ACTION_WRITER_STATECHANGED);
		Bundle b = new Bundle(1);
		
		if(!NfcAdapter.getDefaultAdapter(this).isEnabled()){
			b.putSerializable(NfcConnectorStateReceiver.EXTRA_EXCEPTION_NFCDISABLED, new NfcDisabledException());
			this.sendBroadcast(broadcastIntent.putExtras(b));
			this.finish();
		}
		
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			NfcMessage message = null;
			try {
				message = NfcMessage.PARSER.parseFromNdefMessage(new NdefMessage(intent.getByteArrayExtra(NfcMessage.KEY_MESSAGE)));
			} catch (FormatException e) {
				Log.e("NfcConnector", e.getMessage());
			}
			
			try {
				this.write(detectedTag, message);
			} catch (IOException e) {
				Log.e("NfcConnector", e.getMessage());
				b.putSerializable(NfcConnectorStateReceiver.EXTRA_EXCEPTION_IO, e);
			} catch (FormatException e) {
				Log.e("NfcConnector", e.getMessage());
				b.putSerializable(NfcConnectorStateReceiver.EXTRA_EXCEPTION_FORMAT, e);
			} catch (ReadOnlyException e) {
				Log.e("NfcConnector", e.getMessage());
				b.putSerializable(NfcConnectorStateReceiver.EXTRA_EXCEPTION_READONLY, e);
			} catch (LowCapacityException e) {
				Log.e("NfcConnector", e.getMessage());
				b.putSerializable(NfcConnectorStateReceiver.EXTRA_EXCEPTION_LOWCAPACITY, e);
			} catch (NDEFException e) {
				Log.e("NfcConnector", e.getMessage());
				b.putSerializable(NfcConnectorStateReceiver.EXTRA_EXCEPTION_NDEF, e);
			} catch (Exception e) {
				Log.e("NfcConnector", e.getMessage());
			}
			
			if(b.size() == 0){
				b.putBoolean(NfcConnectorStateReceiver.EXTRA_WRITTEN, true);
			}
			
			this.sendBroadcast(broadcastIntent.putExtras(b));
			this.finish();
		}
	}
	
	private void write(Tag tag, NfcMessage message) throws IOException, FormatException, ReadOnlyException, LowCapacityException, NDEFException{
		int size = message.getRAWMessage().toByteArray().length;

			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					throw new ReadOnlyException();
				}
				if (ndef.getMaxSize() < size) {
					throw new LowCapacityException(ndef.getMaxSize(), size);
				}
				
				ndef.writeNdefMessage(message.getRAWMessage());
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message.getRAWMessage());
					} catch (IOException e) {
						throw e;
					}
				} else {
					throw new NDEFException();
				}
			}
	}

	
	/* **************************************** */
	/* *********** public methods ************* */
	/* **************************************** */

	/* **************************************** */
	/* *********** setter & getter ************ */
	/* **************************************** */

	/* **************************************** */
	/* *********** static methods ************* */
	/* **************************************** */
	
	/* **************************************** */
	/* *********** internal class ************* */
	/* **************************************** */
}
