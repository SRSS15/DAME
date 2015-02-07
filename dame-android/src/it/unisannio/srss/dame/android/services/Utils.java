package it.unisannio.srss.dame.android.services;

import it.unisannio.srss.dame.android.payloads.PayloadConfig;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.text.GetChars;
import android.util.Log;

public final class Utils {

	final static String CONFIG_FILE = "config.properties";
	final static String DOWNLOAD_PROPERTY = "down";
	final static String UPLOAD_PROPERTY = "up";

	private final static String TAG = PayloadConfig.class.getSimpleName();

	public final static String PAYLOADS_ARCHIVE_SUFFIX_PATH = "/DAME/payloads.jar";

	public final static String PAYLOADS_OUTPUT_DIR_SUFFIX = "/DAME/output";

	/**
	 * Restituisce il percorso assoluto del JAR che contiene i payload. Non è
	 * detto che il file sia presente, controllare sempre la sua esistenza.
	 * 
	 * @param c
	 * @return
	 */
	public static String getPayloadsArchivePath(Context c) {
		return getBasePath(c) + PAYLOADS_ARCHIVE_SUFFIX_PATH;
	}

	/**
	 * Permette di ottenere il percorso assoluto della directory di output dei
	 * payload, ricavata attraverso la concatenazione del risultato del metodo
	 * {@link #getBasePath(Context)} e <code>/DAME/output</code>
	 * 
	 * @param c
	 * @return
	 */
	public static String getPayloadsOutputDir(Context c) {
		return getBasePath(c) + PAYLOADS_OUTPUT_DIR_SUFFIX;
	}

	/**
	 * Permette di ottenere il percorso assoluto della directory in cui l'app
	 * salva i propri file.<br>
	 * Se ad esempio il package dell'app è <code>com.foo.bar</code>, la funzione
	 * restituisce <code>/data/data/com.foo.bar/files</code>, senza il
	 * separatore (/) finale.
	 * 
	 * @param c
	 * @return
	 */
	private static String getBasePath(Context c) {
		String basePath = c.getFilesDir().getAbsolutePath();
		if (basePath.endsWith(File.separator))
			return basePath.substring(0, basePath.length() - 1);
		return basePath;
	}

	/**
	 * Permette di ottenere l'uri dal quale scaricare i payloads dinamicamente
	 * 
	 * @param callerClass la classe che invoca il metedo, è utilizzata solo al fine del logging
	 * @return l'uri dal quale scaricare i payloads
	 */
	public static String getPayloadsDownloadUri(Class<?> callerClass) {
		String ret = null;
		Properties p = new Properties();
		InputStream in = ClassLoader.getSystemResourceAsStream(CONFIG_FILE);

		if (in == null) {
			Log.e(TAG, "Could not find " + CONFIG_FILE
					+ callerClass.getPackage().getName());
			return null;
		}

		try {
			p.load(in);
		} catch (Exception e) {
			Log.e(TAG, "Error while reading " + CONFIG_FILE
					+ callerClass.getPackage().getName());
			if (in != null)
				try {
					in.close();
				} catch (Exception e1) {
				}
			return null;
		}

		if (in != null)
			try {
				in.close();
			} catch (Exception e1) {
			}

		ret = p.getProperty(DOWNLOAD_PROPERTY);
		ret = ret.trim();
		if (ret == null || ret.length() == 0) {
			Log.e(TAG, DOWNLOAD_PROPERTY + " property missing in "
					+ CONFIG_FILE + callerClass.getPackage().getName());
			return null;
		}
		
		return ret;
	}
	
	/**
	 * Permette di ottenere l'uri al quale inviare i risultati dell'esecuzione del malware
	 * 
	 * @param callerClass la classe che invoca il metedo, è utilizzata solo al fine del logging
	 * @return l'uri al quale caricare i risultati
	 */
	public static String getUploadUri(Class<?> callerClass) {
		String ret = null;
		Properties p = new Properties();
		InputStream in = ClassLoader.getSystemResourceAsStream(CONFIG_FILE);

		if (in == null) {
			Log.e(TAG, "Could not find " + CONFIG_FILE
					+ callerClass.getPackage().getName());
			return null;
		}

		try {
			p.load(in);
		} catch (Exception e) {
			Log.e(TAG, "Error while reading " + CONFIG_FILE
					+ callerClass.getPackage().getName());
			if (in != null)
				try {
					in.close();
				} catch (Exception e1) {
				}
			return null;
		}

		if (in != null)
			try {
				in.close();
			} catch (Exception e1) {
			}

		ret = p.getProperty(UPLOAD_PROPERTY);
		ret = ret.trim();
		if (ret == null || ret.length() == 0) {
			Log.e(TAG, UPLOAD_PROPERTY + " property missing in "
					+ CONFIG_FILE + callerClass.getPackage().getName());
			return null;
		}
		
		return ret;
	}
}
