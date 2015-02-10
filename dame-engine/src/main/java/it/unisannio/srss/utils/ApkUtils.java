package it.unisannio.srss.utils;

import java.io.File;
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
	public static File decompile(String apkInPath, String apktoolPath) throws IOException {
		File apkFile = FileUtils.checkFile(apkInPath);

		File tmpDir = Files.createTempDirectory("srss").toFile();
		// TODO ripristinare questa istruzione quando le cose funzionano
		// tmpDir.deleteOnExit();
		
		StringBuffer output = new StringBuffer();
		int exitCode = ExecUtils.exec(output, apktoolPath, "d", "-f", "-o",
				tmpDir.getAbsolutePath(), apkFile.getAbsolutePath());
		
		if (exitCode != 0) {
			String err = "Error while generating the keystore (exit code "
					+ exitCode + "): " + output.toString();
			log.error(err);
			throw new IOException(err);
		}
		return tmpDir;
	}

	public static File compile(String decompiledPath, String androidBuildToolsPath) throws IOException {
		FileUtils.checkDir(androidBuildToolsPath);
		FileUtils.checkDir(decompiledPath);
		File keyStore = KeyUtils.autoGenerateKeyStore();
		// TODO in corso
		return null;
	}
}
