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

		try {
			FileUtils.checkFileForExecution(apktoolPath);
		} catch (FileNotFoundException e) {
			log.warn("Please set the apktool path properly.");
			throw e;
		} catch (IOException e) {
			log.warn("Please make " + apktoolPath + " executable.");
			throw e;
		}

		File tmpDir = Files.createTempDirectory("srss").toFile();
		tmpDir.deleteOnExit();
		log.info("Decompiling " + apkInPath + " in " + tmpDir.getAbsolutePath());

		StringBuffer output = new StringBuffer();
		StringBuffer error = new StringBuffer();
		int exitCode = ExecUtils
				.exec(output, error, null, apktoolPath, "d", "-f", "-o",
						tmpDir.getAbsolutePath(), apkFile.getAbsolutePath());

		if (exitCode != 0) {
			String err = "Error while decompiling the APK (exit code "
					+ exitCode + "): " + output.toString() + "\n"
					+ error.toString();
			log.error(err);
			throw new IOException(err);
		}
		log.info("APK successfully decompiled.");
		return tmpDir;
	}

	final static String JARSIGNER = "jarsigner";
	final static String ZIPALIGN = "zipalign";

	final static String SIG_ALG = "SHA1withRSA";
	final static String DIGEST_ALG = "SHA1";

	public static File compile(String decompiledPath,
			String androidBuildToolsPath, String apktoolPath, String apkOutPath)
			throws IOException {
		try {
			FileUtils.checkDir(androidBuildToolsPath);
		} catch (FileNotFoundException e) {
			log.warn("Please set the Android Build Tools path properly.");
		}
		FileUtils.checkDir(decompiledPath); // non dovrebbe mai dare problemi
											// poiché si usa una directory
											// temporanea
		try {
			FileUtils.checkFileForExecution(apktoolPath);
		} catch (FileNotFoundException e) {
			log.warn("Please set the apktool path properly.");
			throw e;
		} catch (IOException e) {
			log.warn("Please make " + apktoolPath + " executable.");
			throw e;
		}
		File apkOut = new File(apkOutPath);
		try {
			FileUtils.checkDirForWriting(apkOut.getParentFile(), true);
		} catch (IOException e) {
			log.warn("Please set the output path properly.");
			throw e;
		}

		if (!androidBuildToolsPath.endsWith(File.separator))
			androidBuildToolsPath += File.separator;
		String zipAlign;
		try {
			zipAlign = FileUtils.checkFileForExecution(
					androidBuildToolsPath + ZIPALIGN).getAbsolutePath();
		} catch (FileNotFoundException e) {
			log.warn("Please set the Android Build Tools path properly.");
			throw e;
		} catch (IOException e) {
			log.warn("Please make " + androidBuildToolsPath + ZIPALIGN
					+ " executable.");
			throw e;
		}

		log.info("Compiling malicious APK in " + apkOutPath);
		File keyStore = KeyUtils.autoGenerateKeyStore();

		File apkOutTmp = FileUtils.checkFile(compileApk(decompiledPath,
				apktoolPath));

		signApk(keyStore, apkOutTmp);

		File res = alignApk(zipAlign, apkOutTmp, apkOutPath);

		log.info("Malicious APK successfully compiled.");
		return res;
	}

	private static File compileApk(String decompiledPath, String apktoolPath)
			throws IOException {
		StringBuffer output = new StringBuffer();
		StringBuffer error = new StringBuffer();
		log.debug("Compiling unsigned malicious APK");
		int exitCode = ExecUtils.exec(output, error, null, apktoolPath, "b",
				decompiledPath);
		if (exitCode != 0) {
			String err = "Error while compiling the APK (exit code " + exitCode
					+ "): " + output.toString() + "\n" + error.toString();
			log.error(err);
			throw new IOException(err);
		}
		// concateno dist
		File distDir = FileUtils.checkDir(new File(decompiledPath, "dist"));

		APKFilter filter = new APKFilter();
		File[] apks = distDir.listFiles(filter);
		log.debug("Unsigned malicious APK compiled in "
				+ apks[0].getAbsolutePath());

		return apks[0];
	}

	private static class APKFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (name.toLowerCase().endsWith(".apk"))
				return true;
			return false;
		}
	}

	private static void signApk(File keystore, File apk) throws IOException {
		StringBuffer output = new StringBuffer();
		StringBuffer error = new StringBuffer();
		log.debug("Signing malicious APK");
		int exitCode = ExecUtils.exec(output, error, null, JARSIGNER,
				"-sigalg", SIG_ALG, "-digestalg", DIGEST_ALG, "-keystore",
				keystore.getAbsolutePath(), apk.getAbsolutePath(),
				KeyUtils.ALIAS, "-storepass", KeyUtils.PASSWORD);
		if (exitCode != 0) {
			String err = "Error while signing the APK " + apk.getAbsolutePath()
					+ " (exit code " + exitCode + "): " + output.toString()
					+ "\n" + error.toString();
			log.error(err);
			throw new IOException(err);
		}
		log.debug("Malicious APK successfully signed");
	}

	private static File alignApk(String zipAlign, File apk, String outputPath)
			throws IOException {
		StringBuffer output = new StringBuffer();
		StringBuffer error = new StringBuffer();
		new File(outputPath).delete(); // cancello il file se già esiste
		log.debug("Aligning malicious APK");
		int exitCode = ExecUtils.exec(output, error, null, zipAlign, "4",
				apk.getAbsolutePath(), outputPath);
		if (exitCode != 0) {
			String err = "Error while aligning the APK "
					+ apk.getAbsolutePath() + " (exit code " + exitCode + "): "
					+ output.toString() + "\n" + error.toString();
			log.error(err);
			throw new IOException(err);
		}
		File res = FileUtils.checkFile(outputPath);
		log.debug("Malicious APK successfully aligned");
		return res;
	}
}
