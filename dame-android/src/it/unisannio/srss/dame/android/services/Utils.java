/*
 * Copyright 2015 
 * 	Danilo Cianciulli 			<cianciullidanilo@gmail.com>
 * 	Emranno Francesco Sannini 	<esannini@gmail.com>
 * 	Roberto Falzarano 			<robertofalzarano@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unisannio.srss.dame.android.services;

import java.io.File;

import android.content.Context;

public final class Utils {

	final static String CONFIG_FILE = "config.properties";
	final static String SERVER_PROPERTY = "server";
	final static String DOWNLOAD_PROPERTY = "payload_uri";
	final static String UPLOAD_PROPERTY = "result_uri";
	final static String USERNAME_PROPERTY = "username";
	final static String PASSWORD_PROPERTY = "password";


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
