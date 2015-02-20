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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

public class FTPService {

	private final FTPClient ftp;
	private final String url, user, pwd;
	private final int port;
	private final boolean passive;

	private final static String TAG = FTPService.class.getSimpleName();

	public FTPService(String host, int port, String user, String pwd,
			boolean passive) {
		this.ftp = new FTPClient();
		this.url = host;
		this.user = user;
		this.pwd = pwd;
		this.port = port;
		this.passive = passive;
		// for debugging use
		ftp.addProtocolCommandListener(new PrintCommandListener(
				new PrintWriter(System.out)));
	}

	public void connect() throws SocketException, IOException {
		int reply;
		ftp.connect(url, port);
		reply = ftp.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			Log.e(TAG, "Exception in connecting to FTP Server: code " + reply);
		}
		ftp.login(user, pwd);
		ftp.setFileType(FTP.BINARY_FILE_TYPE);
		if (passive)
			ftp.enterLocalPassiveMode();
		else
			ftp.enterLocalActiveMode();
	}

	public void downloadFile(String remoteFilePath, String localFilePath) {
		File file = new File(localFilePath);
		File parentFile = file.getParentFile();
		if (!parentFile.isDirectory() && !file.getParentFile().mkdirs()) {
			Log.e(TAG,
					"Unable to create the directory "
							+ parentFile.getAbsolutePath());
			return;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			this.ftp.retrieveFile(remoteFilePath, fos);
			fos.flush();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Unable to open " + file.getAbsolutePath()
					+ " for writing.", e);
		} catch (IOException e) {
			Log.e(TAG, "Error while downloading the payloads.", e);
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (Exception e) {
				}
		}
	}

	public void uploadFile(String localFilePath, String remoteFilePath) {
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(localFilePath);
			this.ftp.storeFile(remoteFilePath, fin);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Error while sending: file " + localFilePath
					+ " does not exist", e);
		} catch (IOException e) {
			Log.e(TAG, "Error while sending.", e);
		} finally {
			if (fin != null)
				try {
					fin.close();
				} catch (Exception e) {
				}
		}
	}

	public void disconnect() {
		if (this.ftp.isConnected()) {
			try {
				this.ftp.logout();
				this.ftp.disconnect();
			} catch (IOException e) {
				Log.e(TAG, "Error while disconnecting.", e);
			}
		}
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPwd() {
		return pwd;
	}

	public int getPort() {
		return port;
	}

	public boolean isPassive() {
		return passive;
	}
}
