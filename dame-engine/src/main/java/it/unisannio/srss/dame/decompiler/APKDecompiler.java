package it.unisannio.srss.dame.decompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;

public class APKDecompiler {

	public final static String APKTOOL = "tools/apktool";

	private String apkFilePath;
	private String tmpDirName;

	public APKDecompiler(String apkFilePath, String tmpDirName) {
		this.apkFilePath = apkFilePath;
		this.tmpDirName = tmpDirName;
	}

	public APKDecompiler() {
		apkFilePath = null;
		tmpDirName = null;
	}

	public String getApkFilePath() {
		return apkFilePath;
	}

	public void setApkFilePath(String apk) {
		this.apkFilePath = apk;
	}

	public String getTmpDirName() {
		return tmpDirName;
	}

	public void setTmpDirName(String tmpDirName) {
		this.tmpDirName = tmpDirName;
	}

	/**
	 * This method decompile e previously specified apk file, you must specify
	 * an apk file path using setApkFilePath() method before use this one.
	 * 
	 * @return a file object which contains the decompiled apk
	 * 
	 * @throws IOException
	 *             maybe it can be a FileNotFoundException if it isn't able to
	 *             find the apk file
	 * @throws InterruptedException
	 */
	public File decompile() throws IOException, InterruptedException {

		File apkFile = null;

		// check if the apk exist and it is readable
		if (apkFilePath == null || !(apkFile = new File(apkFilePath)).canRead()) {
			System.err.println("APK not found!");
			throw new FileNotFoundException("APK not found!");
		}

		File tmpDir;
		
		if (this.tmpDirName == null)
			tmpDir = Files.createTempDirectory("srss").toFile();
		else
			tmpDir = Files.createTempDirectory(this.tmpDirName).toFile();
		
		// tmpDir.deleteOnExit();
		ProcessBuilder pb = new ProcessBuilder(APKTOOL, "d", "-f", "-o",
				tmpDir.getAbsolutePath(), apkFile.getAbsolutePath());
		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		Process p = pb.start();
		p.waitFor();
		
		return tmpDir;
	}

}
