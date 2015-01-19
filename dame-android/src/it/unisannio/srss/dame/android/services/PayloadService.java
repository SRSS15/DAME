package it.unisannio.srss.dame.android.services;

import it.unisannio.srss.dame.android.payloads.Payload;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class PayloadService extends Service {

	public final static String PAYLOAD_CLASS = "PAYLOAD_CLASS";

	private final static String TAG = PayloadService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String payloadStringClass = intent.getExtras().getString(PAYLOAD_CLASS);
		if (payloadStringClass == null) {
			Log.d(TAG, "Loading payload " + payloadStringClass);
			try {
				@SuppressWarnings("unchecked")
				final Class<Payload> payloadClass = (Class<Payload>) getLoader()
						.loadClass(payloadStringClass);
				final Payload payload = payloadClass.getConstructor(
						Context.class).newInstance(getApplicationContext());
				// TODO trovare un modo per evitare l'esecuzione multipla
				new Thread(payload, "PayloadRunner [" + payloadStringClass
						+ "]").start();
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
	 * @param c Context del chiamante
	 * @param payloadClass la classe del payload
	 */
	public static void runPayload(Context c, String payloadClass) {
		Intent i = new Intent(c, PayloadService.class);
		i.putExtra(PAYLOAD_CLASS, payloadClass);
		c.startService(i);
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
