package de.simplenfc;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.FormatException;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.util.Log;
import de.simplenfc.activity.NfcConnector;
import de.simplenfc.entity.NfcMessage;
import de.simplenfc.entity.exceptions.LowCapacityException;
import de.simplenfc.entity.exceptions.NDEFException;
import de.simplenfc.entity.exceptions.NfcDisabledException;
import de.simplenfc.entity.exceptions.ReadOnlyException;
import de.simplenfc.listener.NfcBeamListener;
import de.simplenfc.listener.NfcForegroundListener;
import de.simplenfc.listener.NfcWriteListener;
import de.simplenfc.listener.adapter.NfcWriteAdapter;
import de.simplenfc.receiver.NfcConnectorStateReceiver;
import de.simplenfc.receiver.NfcForegroundReceiver;

/**
 * Main class to write/read/beam NFC-tags and register NfcMessageHandler.
 * Nfc needs a reference for an activity which lifecycle has
 * to be tracked by {@link Nfc#onCreate()}, {@link Nfc#onResume()} and {@link Nfc#onPause()}.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class Nfc {
	public static boolean DEBUG = false;
	public static String PACKAGENAME;
	
	private static final String TAG = "Nfc";
	private static final int VERSION_ICECREAMSANDWICH = 14;
	private static final int VERSION_GINGERBREAD = 10;
	
	private enum Mode { SLEEP, WRITE, BEAM, PUSH, FOREGROUND }
	private enum State { CREATE, RESUME, PAUSE }
	
	private Mode mMode = Mode.SLEEP;
	private State mState = null;
	private Activity mActivity;
	private NfcAdapter mAdapter;
	private NfcMessage mMessage;
	private NfcConnectorStateReceiver mNfcReceiver;
	private NfcWriteListener mWriteListener;
	private NfcForegroundReceiver mForegroundReceiver;
	
	
	/**
	 * Construct and Nfc-object.
	 * 
	 * @param activity The activity to use.
	 */
	public Nfc(Activity activity){
		mActivity = activity;
		mAdapter = NfcAdapter.getDefaultAdapter(mActivity);
		
		if(Nfc.DEBUG)Log.v(TAG, "\tinitialize nfc, native support? "+this.isAvailable());
		
		if (Build.VERSION.SDK_INT >= VERSION_GINGERBREAD 
				&& PackageManager.PERMISSION_GRANTED != mActivity.checkCallingOrSelfPermission("android.permission.NFC")) {

			throw new SecurityException("android.permission.NFC must be included in AndroidManifest.xml.");
		}
	}
	

	/**
	 * Checks if native nfc-funcationality is available.
	 * @return True if is available, otherwise false;
	 */
	public boolean isAvailable(){
		if(mAdapter == null) return false;
		else return true;
	}
	
	
	/**
	 * Creates a {@link NfcMessage} which is bounded to the entire application.
	 * Once this tag will be read in the future the application will be launched automatically. 
	 * @param messageId An id for the registration of an handler through {@link Nfc#addMessageHandler(String, Class)}.
	 * @return Bounded {@link NfcMessage}
	 */
	public NfcMessage obtainMessageBoundToThisApp(String messageId){
		NfcMessage message = new NfcMessage();
		message.addRecord(messageId, NdefRecord.TNF_MIME_MEDIA, ("application/"+mActivity.getPackageName()).getBytes(), new byte[] {});
		return message;
	}
	
	/**
	 * Registers an activity as handler for received {@link NfcMessage}. The mapping between
	 * activity and {@link NfcMessage} is placed on nfcMessageId.
	 * 
	 * @param nfcMessageId Identification-string for the expected {@link NfcMessage}.
	 * @param activity Activity which handles the received {@link NfcMessage}.
	 */
	public void addMessageHandler(String nfcMessageId, Class activity){
		NfcMessageHandler.getInstance(mActivity).registerMessageId(nfcMessageId, activity);
	}
	
	
	/**
	 * Scans the extras-bundle of the given Intent to find a {@link NfcMessage}.
	 * 
	 * @param intent Intent whose extras will be scanned to find a {@link NfcMessage}.
	 * @return true if a {@link NfcMessage} exists, otherwise false.
	 */
	public boolean containsMessage(Intent intent){
		if(intent.hasExtra(NfcMessage.KEY_MESSAGE)) return true;
		else return false;
	}
	
	
	/**
	 * Returns the containing {@link NfcMessage}.
	 * 
	 * @param intent Intent whose extras will be scanned to find a {@link NfcMessage}.
	 * @return {@link NfcMessage} if existing or null if not.
	 */
	public NfcMessage getMessage(Intent intent){
		return (NfcMessage) intent.getParcelableExtra(NfcMessage.KEY_MESSAGE);
	}
	
	
	/**
	 * Handles a bunch of NfcMessages. Each {@link NfcMessage} will be checked by {@link NfcMessageHandler} 
	 * to find a registered activity.
	 * 
	 * @param messages Array of {@link NfcMessage}.
	 * @return boolean If messagehandler exists true otherwise false.
	 */
	public boolean handleMessages(NfcMessage[] messages){
		NfcMessageHandler handler = NfcMessageHandler.getInstance(mActivity);
		return handler.handleMessages(messages);
	}
	
	
	/**
	 * Starts writemode to listen for existing NFC-tags. If a NFC-tag is located it will be written with 
	 * the given {@link NfcMessage}. 
	 * 
	 * @param message {@link NfcMessage} which will be written to an available NFC-tag.
	 * @param listener {@link NfcWriteListener} to handle Exceptions or success.
	 */
	public void writeToTag(NfcMessage message, NfcWriteListener listener){
		mWriteListener = listener;
		mMessage = message;
		mMode = Mode.WRITE;
		
		if(State.RESUME.equals(mState)){
			this.enableWriteMode();
		}
	}
	
	/**
	 * @see Nfc#writeToTag(NfcMessage, NfcWriteListener)
	 * @param message {@link NfcMessage} which will be written to an available NFC-tag.
	 */
	public void writeToTag(NfcMessage message){
		writeToTag(message, null);
	}
		
	
	/**
	 * Checks if current android-version supports nfcbeam(since API 14).
	 * @return True if beam is supported otherwise false.
	 */
	public boolean supportsBeam(){
		if(Build.VERSION.SDK_INT >= VERSION_ICECREAMSANDWICH) return true;
		else return false;
	}
	
	
	/**
	 * Start beaming NfcMesage to another android-device.
	 * @param message Message which will be beamed.
	 * @param listener Listener as callback for a successfull beam.
	 */
	public void beamToDevice(NfcMessage message, final NfcBeamListener listener){
		mMode = Mode.BEAM;
		
		mAdapter.setOnNdefPushCompleteCallback(new OnNdefPushCompleteCallback() {
			
			@Override
			public void onNdefPushComplete(NfcEvent event) {
				mMode = Mode.SLEEP;
				mMessage = null;
				
				if(listener != null){
					listener.onNfcMessagePushed();
				}
			}
		}, mActivity);
		
		mAdapter.setNdefPushMessage(message.getRAWMessage(), mActivity);
	}
	
	
	/**
	 * @see Nfc#beamToDevice(NfcMessage, NfcBeamListener)
	 * @param message
	 */
	public void beamToDevice(NfcMessage message){
		this.beamToDevice(message, null);
	}
	
	
	/**
	 * Used to push a NfcMessage to another device.
	 * @deprecated since API 14. Use {@link Nfc#beamToDevice(NfcMessage, NfcBeamListener)} instead.
	 * @param message Message which will be pushed.
	 */
	public void pushToDevice(NfcMessage message){
		mMode = Mode.PUSH;
		mMessage = message;
		
		if(State.RESUME.equals(mState)){
			this.enablePushMode();
		}
	}
	
	
	/**
	 * Enabled foregrounddispatch to read all available messages through nfc.
	 * @param listener Listener for handling different kind of messages.
	 */
	public void dispatchAllMessages(NfcForegroundListener listener){
		if(Nfc.DEBUG)Log.v(TAG, "\tprepare to read all messages");
		mMode = Mode.FOREGROUND;
		mForegroundReceiver = new NfcForegroundReceiver(listener);
		
		this.enableForegroundDispatch();
	}
	

	/**
	 * Resets {@link Nfc} by aborting beaming/pushing/reading/writing messages.
	 */
	public void reset(){
		if(Nfc.DEBUG)Log.v(TAG, "\treset from mode "+mMode+" with message "+mMessage);
		
		mMode = Mode.SLEEP;
		mMessage = null;
		
		if(Mode.BEAM.equals(mMode)){
			mAdapter.setNdefPushMessage(null, mActivity);
		}else if(Mode.PUSH.equals(mMode)){
			//it's quite enough to null the mMessage
		}else if(Mode.WRITE.equals(mMode)){
			mAdapter.disableForegroundDispatch(mActivity);
		}
	}
	
	
	private void enableWriteMode(){
		if(Nfc.DEBUG)Log.v(TAG, "\tprepare to write message "+mMessage);
		
		mNfcReceiver = new NfcConnectorStateReceiver(mWriteListener){

			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				
				mMode = Mode.SLEEP;
				mMessage = null;
				mWriteListener = null;
			}
			
		};
		
		IntentFilter filter = new IntentFilter(NfcConnectorStateReceiver.ACTION_STATECHANGED);
		mActivity.registerReceiver(mNfcReceiver, filter);
		
		Intent intent = new Intent(mActivity, NfcConnector.class);
		intent.setExtrasClassLoader(NfcMessage.class.getClassLoader());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(NfcConnector.EXTRA_MODE, NfcConnector.MODE_WRITE);
		intent.putExtra(NfcMessage.KEY_MESSAGE, mMessage.getRAWMessage().toByteArray());
		
		PendingIntent pendingIntent= PendingIntent.getActivity(mActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NfcAdapter.getDefaultAdapter(mActivity).enableForegroundDispatch((Activity) mActivity, pendingIntent,
				new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) }
		, null);
	}
	
	private void enablePushMode(){
		if(Nfc.DEBUG)Log.v(TAG, "\tprepare to push message "+mMessage);
		
		mAdapter.enableForegroundNdefPush(mActivity, mMessage.getRAWMessage());
		
		Intent intent = new Intent(mActivity, NfcConnector.class);
		intent.setExtrasClassLoader(NfcMessage.class.getClassLoader());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent pendingIntent= PendingIntent.getActivity(mActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NfcAdapter.getDefaultAdapter(mActivity).enableForegroundDispatch((Activity) mActivity, pendingIntent,
				new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) }
		, null);
	}
	
	private void enableForegroundDispatch(){
		if(Nfc.DEBUG)Log.v(TAG, "\tprepare to read all messages ");
		
		IntentFilter filter = new IntentFilter(NfcForegroundReceiver.ACTION_MESSAGE_RECEIVED);
		mActivity.registerReceiver(mForegroundReceiver, filter);
		
		Intent intent = new Intent(mActivity, NfcConnector.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(NfcConnector.EXTRA_MODE, NfcConnector.MODE_FOREGROUND);
		
		PendingIntent pendingIntent= PendingIntent.getActivity(mActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mAdapter.enableForegroundDispatch(mActivity, pendingIntent,
				new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) }
		, null);
	}

	
	/**
	 * Called when activity is created.
	 */
	public void onCreate(){
		if(Nfc.DEBUG)Log.v(TAG, "\tonCreate mode: "+mMode+", message: "+mMessage);
		
		mState = State.CREATE;
	}
	
	
	/**
	 * Called when activity is resumed.
	 */
	public void onResume(){
		if(Nfc.DEBUG)Log.v(TAG, "\tonResume mode: "+mMode+", message: "+mMessage);
		
		mState = State.RESUME;
		
		if(mMessage == null) return;
		
		if(Mode.WRITE.equals(mMode)){
			this.enableWriteMode();
		}else if(Mode.PUSH.equals(mMode)){
			this.enablePushMode();
		}else if(Mode.FOREGROUND.equals(mMode)){
			this.enableForegroundDispatch();
		}
	}
	
	
	/**
	 * Called when activity is paused.
	 */
	public void onPause(){
		if(Nfc.DEBUG)Log.v(TAG, "\tonPause mode: "+mMode+", message: "+mMessage);
		
		mState = State.PAUSE;
		
		if(mNfcReceiver != null){
			mActivity.unregisterReceiver(mNfcReceiver);
		}
		
		if(mForegroundReceiver != null){
			mActivity.unregisterReceiver(mForegroundReceiver);
		}
		
		mAdapter.disableForegroundNdefPush(mActivity);
		mAdapter.disableForegroundDispatch(mActivity);
	}
}
