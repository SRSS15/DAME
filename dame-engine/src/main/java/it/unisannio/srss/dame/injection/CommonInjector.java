/**
 *	@author Roberto Falzarano <robertofalzarano@gmail.com>
 */

package it.unisannio.srss.dame.injection;

import it.unisannio.srss.utils.DirectoryCopier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonInjector {

	private final static Logger LOG = LoggerFactory
			.getLogger(CommonInjector.class);

	public static void inject(Path commonSmaliSource, Path appSmaliPath) throws FileNotFoundException{
		
		if(Files.notExists(commonSmaliSource) || Files.notExists(appSmaliPath)){
			String msg = "Common smali source or app smali source directory doesn't exist";
			LOG.error(msg);
			throw new FileNotFoundException(msg);
		}
		
		LOG.info("Start coping common smali from "+commonSmaliSource.toString()+" to "+appSmaliPath.toString());
		try {
			DirectoryCopier.copy(commonSmaliSource, appSmaliPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("End coping common smali code");
		
	}
}
