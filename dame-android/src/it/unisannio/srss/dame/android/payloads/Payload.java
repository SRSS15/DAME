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

package it.unisannio.srss.dame.android.payloads;

import it.unisannio.srss.dame.android.services.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

public abstract class Payload implements Runnable {

	protected final Context context;
	protected final PayloadConfig config;
	
	protected final String tag = getClass().getSimpleName();

	public Payload(Context context) {
		this.context = context;
		this.config = PayloadConfig.loadFromProperties(getClass());
		if (this.config == null)
			throw new RuntimeException(
					"Unable to load configuration for payload "
							+ getClass().getCanonicalName()
							+ ". See logs for details.");
	}
	
	public Payload() {
		this(null);
	}

	public PayloadConfig getConfig() {
		return config;
	}
	

	/**
	 * Il file di output viene salvato nello spazio riservato all'app. <br>
	 * Ad esempio, se il package dell'app è <code>com.foo.bar</code>, allora la
	 * directory di output sarà: <blockquote>/data/data/com.foo
	 * .bar/files/DAME/output/</blockquote> Il nome del file è dato dal nome del
	 * payload, seguito dalla data di acquisizione dei dati, ad
	 * esempio:<blockquote>IMEI-20150107-193055242.txt</blockquote>indica che il
	 * payload IMEI ha salvato il file in data 7 gennaio 2015, alle ore
	 * 19:30:55.242.<br>
	 * <br>
	 * Il nome del file è pensato per essere univoco anche in seguito a chiamate
	 * successive del metodo. Per tale motivo, il payload può chiamare questo
	 * metodo tutte le volte che ne ha bisogno.
	 * 
	 * @param toSave
	 *            I dati che il payload intende salvare.
	 * @return <code>true</code> se il salvataggio è avvenuto con successo,
	 *         <code>false</code> altrimenti. In caso di fallimento, visionare i
	 *         log per maggiori dettagli.
	 */
	protected boolean save(String toSave) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmssSSS",
				Locale.US);
		String filename = Utils.getPayloadsOutputDir(context) + File.separator
				+ config.getName() + "-" + sdf.format(new Date()) + ".txt";
		PrintStream ps = null;
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			ps = new PrintStream(file);
		} catch (FileNotFoundException e) {
			Log.e(tag, "Unable to open the file " + filename + " for writing.");
			return false;
		}
		ps.print(toSave);
		ps.close();
		return true;
	}

	@Override
	public String toString() {
		return "Payload [config=" + config + "]";
	}
}
