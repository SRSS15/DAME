package roberto.falzarano.gmail.com.hellowordtest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickButton1(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        Toast.makeText(this,"button 1", Toast.LENGTH_LONG).show();
    }

    public void onClickButton2(){
        Uri smsInbox = Telephony.Sms.Inbox.CONTENT_URI;
        ContentResolver cr = getContentResolver();
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
        Toast.makeText(this,"button 2", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
