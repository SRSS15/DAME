package it.unisannio.srss.dame.android.test;

import it.unisannio.srss.dame.android.services.NetworkService;
import it.unisannio.srss.dame.android.services.PayloadService;
import android.app.Activity;

public class TestActivity extends Activity {

	@Override
	protected void onStart() {
		String a = "ciao";
		
		PayloadService.runPayload(this, "payloadClass");
		
		NetworkService.doNetworkStuff(this);
	}
}
