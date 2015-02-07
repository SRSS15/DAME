package it.unisannio.srss.dame.android.services;

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

public class FTPServer {

	 private FTPClient ftp = null;
	 private String url, user, pwd;
	 
	 private final static String TAG = FTPServer.class.getSimpleName();
	 
	    public FTPServer(String host, String user, String pwd){
	        ftp = new FTPClient();
	        url = host;
	        this.user = user;
	        this.pwd = pwd;
	    }
	 
	    public void connect() throws SocketException, IOException{
	    	// for debugging use
	        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
	        int reply;
	        ftp.connect(url);
	        reply = ftp.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(reply)) {
	            ftp.disconnect();
	            Log.e(TAG,"Exception in connecting to FTP Server");
	        }
	        ftp.login(user, pwd);
	        ftp.setFileType(FTP.BINARY_FILE_TYPE);
	        ftp.enterLocalPassiveMode();
	    }
	 
	    public void downloadFile(String remoteFilePath, String localFilePath) {
	        try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
	            this.ftp.retrieveFile(remoteFilePath, fos);
	            fos.flush();
	            fos.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	 
	    public void uploadFile(String localFilePath, String remoteFilePath){
	    	try(FileInputStream fin = new FileInputStream(localFilePath)){
	    		this.ftp.storeFile(remoteFilePath, fin);
	    		fin.close();
	    	} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "file " + localFilePath+" does not exist");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	 
	    public void disconnect() {
	        if (this.ftp.isConnected()) {
	            try {
	                this.ftp.logout();
	                this.ftp.disconnect();
	            } catch (IOException f) {
	                // do nothing as file is already downloaded from FTP server
	            }
	        }
	    }

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPwd() {
			return pwd;
		}

		public void setPwd(String pwd) {
			this.pwd = pwd;
		}	
}
