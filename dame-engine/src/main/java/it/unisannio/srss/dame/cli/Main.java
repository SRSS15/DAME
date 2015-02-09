package it.unisannio.srss.dame.cli;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main {

	@Option(name = "-o", usage = "apk file dest path")
	private String apkDest = "(Default dest)";

	@Option(name = "-config", usage = "ftp server file config",required=true)
	private File config = new File(".");

	@Argument(required=true,usage="apk file source")
	File apk=new File(".");
	
	public static void main(String[] args) throws IOException {
		new Main().doMain(args);
	}

	public void doMain(String[] args) throws IOException {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			
			//TODO waiting for Danilo's code
			
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
