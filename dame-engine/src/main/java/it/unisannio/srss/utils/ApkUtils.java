package it.unisannio.srss.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApkUtils {

	private final static Logger log = LoggerFactory.getLogger(ApkUtils.class);

	/**
	 * This method decompiles the specified apk file.
	 * 
	 * @return a file object which contains the decompiled apk directory
	 * 
	 * @throws IOException
	 *             maybe it can be a FileNotFoundException if it isn't able to
	 *             find the apk file
	 */
	public static File decompile(String apkInPath, String apktoolPath)
			throws IOException {
		File apkFile = FileUtils.checkFile(apkInPath);
		FileUtils.checkFileForExecution(apktoolPath);

		File tmpDir = Files.createTempDirectory("srss").toFile();
		// TODO ripristinare questa istruzione quando le cose funzionano
		// tmpDir.deleteOnExit();
		log.debug("Decompiling " + apkInPath + " in "
				+ tmpDir.getAbsolutePath());

		StringBuffer output = new StringBuffer();
		int exitCode = ExecUtils.exec(output, apktoolPath, "d", "-f", "-o",
				tmpDir.getAbsolutePath(), apkFile.getAbsolutePath());

		if (exitCode != 0) {
			String err = "Error while decompiling the APK (exit code "
					+ exitCode + "): " + output.toString();
			log.error(err);
			throw new IOException(err);
		}
		log.debug("APK successfully decompiled.");
		return tmpDir;
	}

	final static String JARSIGNER = "jarsigner";
	final static String ZIPALIGN = "zipalign";

	final static String SIG_ALG = "SHA1withRSA";
	final static String DIGEST_ALG = "SHA1";

	public static File compile(String decompiledPath,
			String androidBuildToolsPath, String apktoolPath, String apkOutPath)
			throws IOException {
		FileUtils.checkDir(androidBuildToolsPath);
		FileUtils.checkDir(decompiledPath);
		FileUtils.checkFileForExecution(apktoolPath);
		File apkOut = new File(apkOutPath);
		FileUtils.checkDirForWriting(apkOut.getParentFile(), true);

		if (!androidBuildToolsPath.endsWith(File.separator))
			androidBuildToolsPath += File.separator;
		String zipAlign = FileUtils.checkFileForExecution(
				androidBuildToolsPath + ZIPALIGN).getAbsolutePath();
		File keyStore = KeyUtils.autoGenerateKeyStore();

		File apkOutTmp = FileUtils.checkFile(compileApk(decompiledPath,
				apktoolPath));

		signApk(keyStore, apkOutTmp);

		return alignApk(zipAlign, apkOutTmp, apkOutPath);
	}

	private static File compileApk(String decompiledPath, String apktookPath)
			throws IOException {
		StringBuffer output = new StringBuffer();
		int exitCode = ExecUtils.exec(output, apktookPath, "b", decompiledPath);
		if (exitCode != 0) {
			String err = "Error while compiling the APK (exit code " + exitCode
					+ "): " + output.toString();
			log.error(err);
			throw new IOException(err);
		}
		if (!decompiledPath.endsWith(File.separator))
			decompiledPath += File.separator;
		// TODO restituisci l'apk in decompilePath + dist/
		// concateno dist
		File distDir = new File(decompiledPath, "dist");

		if (!distDir.isDirectory()) {
			String err = "The path " + distDir.getAbsolutePath()
					+ " isn't a directory or not exist";
			log.error(err);
			throw new FileNotFoundException(err);
		}
		
		APKFilter filter = new APKFilter();
		File[] apks = distDir.listFiles(filter);

		return apks[0];
	}
	
	private static class APKFilter implements FilenameFilter{

		@Override
		public boolean accept(File dir, String name) {
			if(name.toLowerCase().endsWith(".apk"))
				return true;
			return false;
		}
	}

	private static void signApk(File keystore, File apk) throws IOException {
		StringBuffer output = new StringBuffer();
		int exitCode = ExecUtils.exec(output, JARSIGNER, "-sigalg", SIG_ALG,
				"-digestalg", DIGEST_ALG, "-keystore",
				keystore.getAbsolutePath(), apk.getAbsolutePath(),
				KeyUtils.ALIAS);
		if (exitCode != 0) {
			String err = "Error while signing the APK " + apk.getAbsolutePath();
			log.error(err);
			throw new IOException(err);
		}
	}

	private static File alignApk(String zipAlign, File apk, String outputPath)
			throws IOException {
		StringBuffer output = new StringBuffer();
		int exitCode = ExecUtils.exec(output, zipAlign, "4",
				apk.getAbsolutePath(), outputPath);
		if (exitCode != 0) {
			String err = "Error while aligning the APK "
					+ apk.getAbsolutePath();
			log.error(err);
			throw new IOException(err);
		}
		return FileUtils.checkFile(outputPath);
	}
}
