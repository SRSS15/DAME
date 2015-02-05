package it.unisannio.srss.dame.android.services;

import java.io.File;

import android.content.Context;

public final class Utils {

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
}
