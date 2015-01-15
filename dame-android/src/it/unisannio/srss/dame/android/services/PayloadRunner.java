package it.unisannio.srss.dame.android.services;

import it.unisannio.srss.dame.android.payloads.Payload;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

// XXX questo probabilmente non serve più così com'è, va modificato
public class PayloadRunner extends Service {

	private final static String TAG = PayloadRunner.class.getSimpleName();

	private static PayloadRunner instance = null;

	private List<Class<Payload>> payloadClasses;

	@Override
	public void onCreate() {
		PayloadRunner.instance = this;
		this.payloadClasses = getPayloads();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Runner();
		return super.onStartCommand(intent, flags, startId);
	}

	private class Runner extends Thread {
		public Runner() {
			super("PayloadRunner");
			this.start();
		}

		@Override
		public void run() {
			ExecutorService es = Executors.newCachedThreadPool();
			for (Class<Payload> payload : payloadClasses) {
				try {
					Payload p = payload.getDeclaredConstructor(Context.class)
							.newInstance(getApplicationContext());
					es.execute(p);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			es.shutdown();
			try {
				if (es.awaitTermination(5, TimeUnit.MINUTES)) {
					Log.i(TAG, "All tasks completed!");
				} else {
					Log.e(TAG, "TIMEOUT!");
				}
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static PayloadRunner getInstance() {
		return instance;
	}

	private final static String PAYLOADS_CONFIG = "payloads.properties";

	@SuppressWarnings("unchecked")
	private static <T extends Payload> List<Class<T>> getPayloads() {
		List<Class<T>> classes = new ArrayList<Class<T>>();
		Properties p = new Properties();
		InputStream in = Payload.class.getResourceAsStream(PAYLOADS_CONFIG);
		if (in == null) {
			Log.e(TAG, "Could not find " + PAYLOADS_CONFIG + " in package "
					+ Payload.class.getPackage().getName());
			return classes;
		}
		try {
			p.load(in);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			if (in != null)
				try {
					in.close();
				} catch (Exception e1) {
				}
			return classes;
		}
		if (in != null)
			try {
				in.close();
			} catch (Exception e1) {
			}
		String classesString = p.getProperty("payloads");
		if (classesString == null || classesString.length() == 0) {
			Log.w(TAG, "No payloads found in the configuration");
			return classes;
		}
		StringTokenizer st = new StringTokenizer(classesString, ",");
		String token;
		while (st.hasMoreTokens()) {
			token = st.nextToken().trim();
			try {
				classes.add((Class<T>) Class.forName(token));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return classes;
	}

}
