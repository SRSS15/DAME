package it.unisannio.srss.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecUtils {

	private final static Logger log = LoggerFactory.getLogger(ExecUtils.class);

	/**
	 * Esegue un comando
	 * 
	 * @param outputBuffer
	 *            Una volta eseguito il comando, questo buffer conterrà
	 *            l'output.
	 * @param commandAndArgs
	 *            Comando da eseguire, seguito dai parametri.
	 * @return Il codice di uscita dell'esecuzione. Viene restituito -1 se
	 *         l'esecuzione viene interrotta.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int exec(StringBuffer outputBuffer, String... commandAndArgs)
			throws IOException {
		log.debug("Command execution: " + Arrays.toString(commandAndArgs));
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		Process p = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		int exitCode;
		try {
			exitCode = p.waitFor();
		} catch (InterruptedException e) {
			log.error("Command execution interrupted: "
					+ Arrays.toString(commandAndArgs));
			reader.close();
			return -1;
		}
		log.debug("Exit code: " + exitCode);
		if (outputBuffer != null) {
			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (i++ > 0)
					outputBuffer.append("\n");
				outputBuffer.append(line);
			}
			log.debug("Output: " + outputBuffer.toString());
		}
		reader.close();
		return exitCode;
	}
}
