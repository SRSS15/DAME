package it.unisannio.srss.dame.model;

import it.unisannio.srss.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

public class FTPServerConfig {

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

	public void writeToFile(String filePath) throws FileNotFoundException,
			IOException {
		writeToFile(new File(filePath));
	}

	public void writeToFile(File file) throws FileNotFoundException,
			IOException {
		FileUtils.checkDirForWriting(file.getParentFile(), true);
		file.delete();
		PrintStream ps = new PrintStream(file);
		ps.println(SERVER_PROPERTY + "=" + SCHEMA + serverAddress + ":"
				+ serverPort);
		ps.println(DOWNLOAD_PROPERTY + "=" + payloadsUrl);
		ps.println(UPLOAD_PROPERTY + "=" + resultUrl);
		ps.println(USERNAME_PROPERTY + "=" + username);
		ps.println(PASSWORD_PROPERTY + "=" + password);
		ps.close();
	}

	public static FTPServerConfig loadFromFile(String filePath)
			throws IOException {
		return loadFromFile(new File(filePath));
	}

	public static FTPServerConfig loadFromFile(File file) throws IOException {
		FileUtils.checkFile(file);
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
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
}
