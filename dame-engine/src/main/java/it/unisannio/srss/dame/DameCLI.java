package it.unisannio.srss.dame;

import it.unisannio.srss.dame.android.payloads.Payload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class DameCLI {

	@Option(name = "-o", aliases = "--output", usage = "apk file dest path ("
			+ Dame.DEFAULT_OUT_APK_FILE + " in the source apk path by default)")
	private String apkOut = null;

	@Option(name = "-c", aliases = "--ftp-server-config", usage = "ftp server file config ("
			+ Dame.DEFAULT_SERVER_CONFIG_FILE
			+ " in the source apk path by default)")
	private File config = null;

	@Option(name = "-at", aliases = "--apktool", usage = "apktool path ("
			+ Dame.APKTOOL_DEFAULT_PATH + " by default)")
	private String apktoolPath = null;

	@Option(name = "-py", usage = "python path (system path by default)")
	private String pythonPath = null;

	@Option(name = "-ag", aliases = "--androguard", usage = "androguard path ("
			+ Dame.ANDROGUARD_DEFAULT_PATH_CONST + " by default)")
	private String androguardPath = null;

	@Option(name = "-bt", aliases = "--android-build-tools", usage = "Android build tools path ("
			+ Dame.BUILD_TOOLS_DEFAULT_PATH_CONST + " by default)")
	private String buildToolsPath = null;

	@Option(name = "-v", aliases = "--version", usage = "Prints the engine version and build time.")
	private boolean version;

	@Argument(required = true, usage = "apk source file")
	private File apk = null;

	public static void main(String[] args) {
		new DameCLI().doMain(args);
	}

	public void doMain(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			if (version) {
				printVersion();
				return;
			}
			// run engine
			Dame dame = new Dame();
			String apkIn = apk.getAbsolutePath();
			dame.setApkIn(apkIn);
			if (apkOut != null)
				dame.setApkOut(apkOut);
			if (config != null) {
				String serverConfigPath = config.getAbsolutePath();
				dame.setServerConfigPath(serverConfigPath);
			}
			if (apktoolPath != null)
				dame.setApktoolPath(apktoolPath);
			if (pythonPath != null)
				dame.setPythonPath(pythonPath);
			if (androguardPath != null)
				dame.setAndroguardPath(androguardPath);
			if (buildToolsPath != null)
				dame.setAndroidBuildToolsPath(buildToolsPath);
			List<Payload> payloads = dame.getFilteredPayloadList();
			if (!payloads.isEmpty()) {
				System.out
						.println("Available payloads list. Insert payload's index for injection.\nYou can also specify more than one payload, splited by comma.");
				int i = 0;
				for (Payload payload : payloads) {
					System.out.println(i + " - "
							+ payload.getConfig().getName() + " ("
							+ payload.getConfig().getDescription() + ")");
					i++;
				}
				System.out.print("Insert comma-separated payload indexes: ");
				Scanner scanner = new Scanner(System.in);
				String fromExploiter = scanner.nextLine();
				scanner.close();
				StringTokenizer st = new StringTokenizer(fromExploiter, ",");

				List<Payload> toInject = new ArrayList<Payload>();
				while (st.hasMoreTokens()) {
					toInject.add(payloads.get(Integer.parseInt(st.nextToken()
							.trim())));
				}
				dame.inject(toInject);
			} else {
				System.out.println("No available payloads for your apk!");
			}
		} catch (CmdLineException e) {
			if (version) {
				printVersion();
				return;
			}
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java -jar dame.jar [options...] FILE.apk");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			// print option sample. This is useful some time
			// System.err.println(" Example: java SampleMain"+parser.printExample());
			return;
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static void printVersion() {
		Properties p = new Properties();
		try {
			p.load(ClassLoader.getSystemResourceAsStream("version.properties"));
			System.out.println("Version: " + p.getProperty("version"));
			System.out.println("Build date: " + p.getProperty("build.date"));
		} catch (IOException e) {
			return;
		}
	}
}
