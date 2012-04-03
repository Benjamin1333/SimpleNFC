package de.simplenfc;

import android.app.Activity;
import de.simplenfc.entity.NfcMessage;
import de.simplenfc.listener.NfcPushListener;
import de.simplenfc.listener.NfcWriteListener;

public class NfcNew {
	private static final int MODE_SLEEP = 0;
	private static final int MODE_WRITE = 1;
	private static final int MODE_PUSH = 2;
	private static final int MODE_FOREGROUND = 3;
	
	private int mMode = MODE_SLEEP;

	
	public NfcNew(Activity a){
		
	}
	
	public void addMessageHandler(){}
	
	public boolean containsMessage(){return false;}
	public NfcMessage getMessage(){return null;}
	
	public void writeToTag(NfcMessage msg, NfcWriteListener listener){}
	public void writeToTag(NfcMessage msg){};
	public void pushToDevice(NfcMessage msg, NfcPushListener listener){};
	public void pushToDevice(NfcMessage msg){};
	
}
