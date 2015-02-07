package it.unisannio.srss.dame.cli;

import java.io.IOException;

public class Test {

	public static void main(String[] args) throws IOException, InterruptedException {
		Dame dame = new Dame();
		dame.setApkTrusted("/home/danilo/Scrivania/com.evernote-2.apk");
		dame.setAndroguardPath("/opt/androguard/");
		dame.getFilteredPayloadList();
	}

}
