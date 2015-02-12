package it.unisannio.srss.helloworld;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

	private Button phoneStateButton;
	private Button smsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getByName("www.google.com");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			Log.e(MainActivity.class.getCanonicalName(), "e che cazzo non conosci google?");
		}
		
		try{
		if(ip.isReachable(5000))
			Toast.makeText(this, "ping ok", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "ping failed", Toast.LENGTH_LONG).show();
		}catch (IOException e){
			Log.e(MainActivity.class.getCanonicalName(), "ping failed");
		}
		
		phoneStateButton = (Button) findViewById(R.id.button);
		smsButton = (Button) findViewById(R.id.button2);

		phoneStateButton.setOnClickListener(this);
		smsButton.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button) {
			phoneState();
		} else if (v.getId() == R.id.button2) {
			sms();
		}
	}
	
	private void phoneState(){
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		Toast.makeText(MainActivity.this, imei, Toast.LENGTH_LONG).show();
	}
	
	private void sms(){
		Uri smsInbox = Uri.parse("content://sms/inbox");
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(smsInbox, null, null, null, null);
		cursor.moveToFirst();
		String msgData = "";
		while (cursor.moveToNext()) {
			msgData += "\n";
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				msgData += " " + cursor.getColumnName(i) + ":"
						+ cursor.getString(i) + "\n";
			}
		}
		Toast.makeText(MainActivity.this, msgData, Toast.LENGTH_LONG)
				.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
