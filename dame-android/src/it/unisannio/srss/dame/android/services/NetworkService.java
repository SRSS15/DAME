package it.unisannio.srss.dame.android.services;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class NetworkService extends Service {

	private final Object downloadLock = new Object();
	private final Object uploadLock = new Object();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// controlla se ha già scaricato i payload
		if ((new File(Utils.getPayloadsArchivePath(this))).exists()) {
			// l'archivio dei payloads è stato già scaricato
			// in questo caso si fa l'upload di eventuali file di output
			uploadOutputs();
		} else {
			// l'archivio non è stato ancora scaricato
			downloadPayloads();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void downloadPayloads() {
		new Thread() {
			@Override
			public void run() {
				synchronized (downloadLock) {
					// TODO download dell'archivio
					// salvarlo nel path restituito dal metodo
					// Utils.getPayloadsArchivePath(this)
				}
			};
		}.start();
	}

	private void uploadOutputs() {
		new Thread() {
			@Override
			public void run() {
				synchronized (uploadLock) {
					// TODO enumerazione dei file di output e upload. Usare
					// tutti i file che sono presenti nella cartella restituita
					// dal metodo Utis.getPayloadsOutputDir(this)
				}
			};
		}.start();
	}

	/**
	 * Questo metodo viene usato per utilità per semplificare la chiamata di
	 * avvio del servizio nel codice smali.
	 * 
	 * @param c
	 */
	public static void doNetworkStuff(Context c) {
		c.startService(new Intent(c, NetworkService.class));
	}

}
