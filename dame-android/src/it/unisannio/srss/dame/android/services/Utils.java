package it.unisannio.srss.dame.android.services;

import java.io.File;

import android.content.Context;

public final class Utils {

	public final static String PAYLOADS_ARCHIVE_SUFFIX_PATH = "DAME/payloads.jar";
	
	public final static String PAYLOADS_OUTPUT_DIR_SUFFIX = "DAME/output";

	public static String getPayloadsArchivePath(Context c) {
		return getBasePath(c) + PAYLOADS_ARCHIVE_SUFFIX_PATH;
	}

	public static String getPayloadsOutputDir(Context c) {
		return getBasePath(c) + PAYLOADS_OUTPUT_DIR_SUFFIX;
	}

	private static String getBasePath(Context c) {
		String basePath = c.getFilesDir().getAbsolutePath();
		if (!basePath.endsWith(File.separator))
			basePath += File.separator;
		return basePath;
	}
}
