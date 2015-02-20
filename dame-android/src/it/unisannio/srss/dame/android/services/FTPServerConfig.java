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
	final static String PASSIVE_MODE_PROPERTY = "passive_mode";

	private final static String SCHEMA = "ftp://";

	private final String serverAddress;
	private final int serverPort;
	private final String payloadsUrl;
	private final String resultUrl;
	private final String username;
	private final String password;
	private final boolean passive;

	public FTPServerConfig(String serverAddress, int serverPort,
			String payloadsUrl, String resultUrl, String username,
			String password, boolean passive) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.payloadsUrl = payloadsUrl;
		this.resultUrl = resultUrl;
		this.username = username;
		this.password = password;
		this.passive = passive;
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

	public boolean isPassive() {
		return passive;
	}

	public static FTPServerConfig loadFromResource(Context c)
			throws IOException {
		Properties properties = new Properties();
		properties.load(c.getAssets().open(PROPERTIES_FILE));
		String serverProperty = properties.getProperty(SERVER_PROPERTY);
		boolean passive = Boolean.parseBoolean(properties.getProperty(
				PASSIVE_MODE_PROPERTY, "true"));
		// rimuovo lo schema
		serverProperty = serverProperty.substring(SCHEMA.length());
		String[] tokens = serverProperty.split(":");
		// se la porta non è indicata usa la 21
		return new FTPServerConfig(tokens[0],
				tokens.length == 2 ? Integer.parseInt(tokens[1]) : 21,
				properties.getProperty(DOWNLOAD_PROPERTY),
				properties.getProperty(UPLOAD_PROPERTY),
				properties.getProperty(USERNAME_PROPERTY),
				properties.getProperty(PASSWORD_PROPERTY), passive);
	}

	@Override
	public String toString() {
		return "FTPServerConfig [serverAddress=" + serverAddress
				+ ", serverPort=" + serverPort + ", payloadsUrl=" + payloadsUrl
				+ ", resultUrl=" + resultUrl + ", username=" + username
				+ ", password=" + password + ", passive=" + passive + "]";
	}

}
