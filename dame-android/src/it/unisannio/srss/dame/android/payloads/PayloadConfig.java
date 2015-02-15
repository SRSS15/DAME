package it.unisannio.srss.dame.android.payloads;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import android.util.Log;

public class PayloadConfig {

	public final static int ONCE = 0;
	public final static int ALWAYS = 1;

	final static String CONFIG_FILE = "payload.properties";
	private final static String TAG = PayloadConfig.class.getSimpleName();

	final static String NAME_PROPERTY = "name";
	private String name;

	final static String DESCRIPTION_PROPERTY = "description";
	private String description;

	final static String PERMISSIONS_PROPERTY = "permissions";
	private Set<String> permissions;

	final static String EXECUTION = "execution";
	private int execution;

	public PayloadConfig() {
		name = null;
		description = null;
		execution = ONCE;
		permissions = new HashSet<String>();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public int getExecution() {
		return execution;
	}

	public static PayloadConfig loadFromProperties(Class<?> payloadClass) {
		Properties p = new Properties();
		InputStream in = payloadClass.getResourceAsStream(CONFIG_FILE);
		if (in == null) {
			Log.e(TAG, "Could not find " + CONFIG_FILE + " in package "
					+ payloadClass.getPackage().getName());
			return null;
		}
		try {
			p.load(in);
		} catch (Exception e) {
			Log.e(TAG, "Error while reading " + CONFIG_FILE + " in package "
					+ payloadClass.getPackage().getName());
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
		PayloadConfig c = new PayloadConfig();
		c.name = p.getProperty(NAME_PROPERTY);
		if (c.name == null || c.name.length() == 0) {
			Log.e(TAG, NAME_PROPERTY + " property missing in " + CONFIG_FILE
					+ " in package " + payloadClass.getPackage().getName());
			return null;
		}
		c.description = p.getProperty(DESCRIPTION_PROPERTY);
		String permissions = p.getProperty(PERMISSIONS_PROPERTY);
		if (permissions != null && permissions.length() > 0) {
			StringTokenizer st = new StringTokenizer(permissions, ",");
			while (st.hasMoreTokens())
				c.permissions.add(st.nextToken().trim());
		}
		String exec = p.getProperty(EXECUTION);
		if (exec == null || exec.trim().equalsIgnoreCase("once"))
			c.execution = ONCE;
		else
			c.execution = ALWAYS;
		return c;
	}

	@Override
	public String toString() {
		return "PayloadConfig [name=" + name + ", description=" + description
				+ ", permissions=" + permissions + ", execution="
				+ (execution == ONCE ? "ONCE" : "ALWAYS") + "]";
	}
}
