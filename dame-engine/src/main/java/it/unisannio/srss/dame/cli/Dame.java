package it.unisannio.srss.dame.cli;

import it.unisannio.srss.dame.android.payloads.Payload;
import it.unisannio.srss.dame.injection.Permission;
import it.unisannio.srss.dame.injection.UsagePoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Danilo Cianciulli
 */
public class Dame {

	private final static Logger log = LoggerFactory.getLogger(Dame.class);

	private final static String PAYLOADS_BASE_PACKAGE = "it.unisannio.srss.android.payloads";
	private final static String APKTOOL_DEFAULT_PATH = "tools/apktool";
	private final static String ANDROGUARD_DEFAULT_PATH = "androguard";
	private final static String PYTHON_DEFAULT_PATH = "python";

	private final static String PERMISSIONS_SCRIPT_PATH = "scripts/permission_extractor.py";
	private final static String SMALI_TO_APK_SCRIPT_PATH = "scripts/smaliToSignedApk.py";

	private String apkIn = null;
	private String apkOut = null;

	private String serverConfigPath = null;

	private String apktoolPath = APKTOOL_DEFAULT_PATH;
	private String pythonPath = PYTHON_DEFAULT_PATH;

	private String androguardPath = ANDROGUARD_DEFAULT_PATH;

	public String getApkIn() {
		return apkIn;
	}

	public void setApkIn(String apkIn) {
		this.apkIn = apkIn;
	}

	public String getApkOut() {
		return apkOut;
	}

	public void setApkOut(String apkOut) {
		this.apkOut = apkOut;
	}

	public String getApktoolPath() {
		return apktoolPath;
	}

	public void setApktoolPath(String apktoolPath) {
		this.apktoolPath = apktoolPath;
	}

	public String getPythonPath() {
		return pythonPath;
	}

	public void setPythonPath(String pythonPath) {
		this.pythonPath = pythonPath;
	}

	public String getServerConfigPath() {
		return serverConfigPath;
	}

	public void setServerConfigPath(String serverConfigPath) {
		this.serverConfigPath = serverConfigPath;
	}

	public String getAndroguardPath() {
		return androguardPath;
	}

	public void setAndroguardPath(String androguardPath) {
		this.androguardPath = androguardPath;
	}

	public List<Payload> getFilteredPayloadList() throws IOException {
		List<Payload> payloads = getAllPayloads();
		Map<String, Set<UsagePoint>> apkPermissions = getAPKPermissions();
		Iterator<Payload> i = payloads.iterator();
		Payload p;
		while(i.hasNext()){
			p = i.next();
			boolean applicable = true;
			for(String permission : p.getConfig().getPermissions())
				if(!apkPermissions.containsKey(permission))
					applicable=false;
			if(!applicable)
				i.remove();
		}
		return payloads;
	}

	public List<Payload> getAllPayloads() {
		Reflections reflections = new Reflections(PAYLOADS_BASE_PACKAGE);
		Set<Class<? extends Payload>> payloadClasses = reflections
				.getSubTypesOf(Payload.class);
		List<Payload> payloads = new ArrayList<Payload>();
		for (Class<? extends Payload> payloadClass : payloadClasses) {
			try {
				payloads.add(payloadClass.newInstance());
			} catch (Exception e) {
				log.error(
						"Could not load payload: "
								+ payloadClass.getCanonicalName(), e);
			}
		}
		return payloads;
	}

	public Map<String, Set<UsagePoint>> getAPKPermissions()
			throws IOException {
		File script = checkFile(PERMISSIONS_SCRIPT_PATH);
		File apkIn = checkFile(this.apkIn);
		checkDir(androguardPath);

		StringBuffer outputBuffer = new StringBuffer();
		int exitCode = exec(outputBuffer, pythonPath,
				script.getAbsolutePath(), apkIn.getAbsolutePath(),
				androguardPath);
		String output = outputBuffer.toString();
		if (exitCode != 0) {
			log.error(output);
			throw new IOException(output);
		}
		ObjectMapper mapper = new ObjectMapper();
		List<Permission> permissions = mapper.readValue(output,
				new TypeReference<List<Permission>>() {
				});
		Map<String, Set<UsagePoint>> res = new HashMap<String, Set<UsagePoint>>();
		for (Permission permission : permissions)
			res.put(permission.getType(),
					new HashSet<UsagePoint>(permission.getUsagePoints()));
		return res;
	}
	
	public void inject(List<Payload> payloads){
		// TODO load server config (default config.properties nella stessa cartella di apkIn)
		// TODO apk to smali
		// TODO common injection
		// TODO call injection
		// TODO smali to apk (default out.apk nella stessa cartella di apkIn)
		// TODO rimozione dati temporanei
	}

	private static File checkFile(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (!file.canRead() || !file.isFile()) {
			String err = "Could not find or read the file: " + filePath;
			log.error(err);
			throw new FileNotFoundException(err);
		}
		return file;
	}

	private static void checkDir(String dirPath) throws FileNotFoundException {
		File dir = new File(dirPath);
		if (!dir.isDirectory() || !dir.canRead()) {
			String err = "Could not find or read the directory: " + dirPath;
			log.error(err);
			throw new FileNotFoundException(err);
		}
	}

	private static void checkOutputDir(String dirPath) throws IOException {
		File dir = new File(dirPath);
		if (dir.isDirectory() && dir.canWrite())
			return;
		if (dir.isDirectory() && !dir.canWrite()) {
			String err = "Could not write to the directory: " + dirPath;
			log.error(err);
			throw new IOException(err);
		}
		if (!dir.mkdirs()) {
			String err = "Could not create the directory: " + dirPath;
			log.error(err);
			throw new IOException(err);
		}
	}

	/**
	 * Esegue un comando
	 * 
	 * @param outputBuffer
	 *            Una volta eseguito il comando, questo buffer conterrà
	 *            l'output.
	 * @param commandAndArgs
	 *            Comando da eseguire, seguito dai parametri.
	 * @return Il codice di uscita dell'esecuzione. Viene restituito -1 se l'esecuzione viene interrotta.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static int exec(StringBuffer outputBuffer, String... commandAndArgs)
			throws IOException {
		if (outputBuffer == null)
			throw new IllegalArgumentException(
					"The output buffer must be not null!");
		log.debug("Command execution: " + Arrays.toString(commandAndArgs));
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		Process p = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		int exitCode;
		try {
			exitCode = p.waitFor();
		} catch (InterruptedException e) {
			log.error("Command execution interrupted: " + Arrays.toString(commandAndArgs));
			reader.close();
			return -1;
		}
		log.debug("Exit code: " + exitCode);
		String line = null;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			if (i++ > 0)
				outputBuffer.append("\n");
			outputBuffer.append(line);
		}
		log.debug("Output: " + outputBuffer.toString());
		reader.close();
		return exitCode;
	}

}
