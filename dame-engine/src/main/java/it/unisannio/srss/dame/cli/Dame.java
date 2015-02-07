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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Dame {

	private final static Logger log = LoggerFactory.getLogger(Dame.class);

	private final static String PAYLOADS_BASE_PACKAGE = "it.unisannio.srss.android.payloads";
	private final static String APKTOOL_DEFAULT_PATH = "tools/apktool";
	private final static String ANDROGUARD_DEFAULT_PATH = "androguard";
	private final static String PYTHON_DEFAULT_PATH = "python";

	private final static String PERMISSIONS_SCRIPT_PATH = "scripts/permission_extractor.py";

	private File apkTrusted = null;
	private File apkMalicious = null;

	private String downloadUrl = null;
	private String uploadUrl = null;

	private String apkToolPath = APKTOOL_DEFAULT_PATH;
	private String pythonPath = PYTHON_DEFAULT_PATH;

	private String androguardPath = ANDROGUARD_DEFAULT_PATH;

	public File getApkTrusted() {
		return apkTrusted;
	}

	public void setApkTrusted(String apkTrusted) {
		setApkTrusted(new File(apkTrusted));
	}

	public void setApkTrusted(File apkTrusted) {
		this.apkTrusted = apkTrusted;
	}

	public File getApkMalicious() {
		return apkMalicious;
	}

	public void setApkMalicious(String apkMalicious) {
		setApkMalicious(new File(apkMalicious));
	}

	public void setApkMalicious(File apkMalicious) {
		this.apkMalicious = apkMalicious;
	}

	public String getApkToolPath() {
		return apkToolPath;
	}

	public void setApkToolPath(String apkToolPath) {
		this.apkToolPath = apkToolPath;
	}

	public String getPythonPath() {
		return pythonPath;
	}

	public void setPythonPath(String pythonPath) {
		this.pythonPath = pythonPath;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	public String getAndroguardPath() {
		return androguardPath;
	}

	public void setAndroguardPath(String androguardPath) {
		this.androguardPath = androguardPath;
	}

	public List<Payload> getFilteredPayloadList() throws IOException,
			InterruptedException {
		System.out.println(getAPKPermissions().toString());
		return null;
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

	private Map<String, Set<UsagePoint>> getAPKPermissions()
			throws IOException, InterruptedException {
		File script = new File(PERMISSIONS_SCRIPT_PATH);
		if (!script.canRead() || !script.isFile()) {
			String err = "Could not locate or read the python script: "
					+ PERMISSIONS_SCRIPT_PATH;
			log.error(err);
			throw new FileNotFoundException(err);
		}
		if (!apkTrusted.canRead() || !apkTrusted.isFile()) {
			String err = "Could not locate or read the input APK: "
					+ apkTrusted.getAbsolutePath();
			log.error(err);
			throw new FileNotFoundException(err);
		}
		if (!checkDir(androguardPath)) {
			String err = "Could not locate or read the Androguard directory: "
					+ androguardPath;
			log.error(err);
			throw new FileNotFoundException(err);
		}
		ProcessBuilder pb = new ProcessBuilder(pythonPath, script.getAbsolutePath(),
				apkTrusted.getAbsolutePath(), androguardPath);
		Process p = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		int exitCode = p.waitFor();
		StringBuilder sb = new StringBuilder();
		String line = null;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			if (i++ > 0)
				sb.append("\n");
			sb.append(line);
		}
		reader.close();
		String output = sb.toString();
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
			res.put(permission.getType(), new HashSet<UsagePoint>(permission.getUsagePoints()));
		return res;
	}

	private static boolean checkDir(String dirPath) {
		File dir = new File(dirPath);
		return dir.isDirectory() && dir.canRead();
	}

	private static boolean checkOutputDir(String dirPath) {
		File dir = new File(dirPath);
		if (dir.isDirectory() && dir.canWrite())
			return true;
		if (dir.isDirectory() && !dir.canWrite())
			return false;
		return dir.mkdirs();
	}

}
