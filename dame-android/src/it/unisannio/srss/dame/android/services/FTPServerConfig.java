package it.unisannio.srss.dame.android.services;

import java.io.IOException;
import java.util.Properties;

import android.content.Context;

public class FTPServerConfig {
	
	final static String PROPERTIES_FILE = "ftp_config_srss.properties";

	final static String SERVER_PROPERTY = "server";
	final static String DOWNLOAD_PROPERTY = "payload_uri";
	final static String UPLOAD_PROPERTY = "result_uri";
	final static String USERNAME_PROPERTY = "username";
	final static String PASSWORD_PROPERTY = "password";

	private final static String SCHEMA = "ftp://";

	private final String serverAddress;
	private final int serverPort;
	private final String payloadsUrl;
	private final String resultUrl;
	private final String username;
	private final String password;

	public FTPServerConfig(String serverAddress, int serverPort,
			String payloadsUrl, String resultUrl, String username,
			String password) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.payloadsUrl = payloadsUrl;
		this.resultUrl = resultUrl;
		this.username = username;
		this.password = password;
	}

	public static String getSchema() {
		return SCHEMA;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getPayloadsUrl() {
		return payloadsUrl;
	}

	public String getResultUrl() {
		return resultUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public static FTPServerConfig loadFromResource(Context c) throws IOException {
		Properties properties = new Properties();
		properties.load(c.getAssets().open(PROPERTIES_FILE));
		String serverProperty = properties.getProperty(SERVER_PROPERTY);
		// rimuovo lo schema
		serverProperty = serverProperty.substring(SCHEMA.length());
		String[] tokens = serverProperty.split(":");
		// se la porta non è indicata usa la 21
		return new FTPServerConfig(tokens[0],
				tokens.length == 2 ? Integer.parseInt(tokens[1]) : 21,
				properties.getProperty(DOWNLOAD_PROPERTY),
				properties.getProperty(UPLOAD_PROPERTY),
				properties.getProperty(USERNAME_PROPERTY),
				properties.getProperty(PASSWORD_PROPERTY));
	}

	@Override
	public String toString() {
		return "FTPServerConfig [serverAddress=" + serverAddress
				+ ", serverPort=" + serverPort + ", payloadsUrl=" + payloadsUrl
				+ ", resultUrl=" + resultUrl + ", username=" + username
				+ ", password=" + password + "]";
	}

}
