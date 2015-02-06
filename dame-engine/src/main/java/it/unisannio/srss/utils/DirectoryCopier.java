/**
 *	@author Roberto Falzarano <robertofalzarano@gmail.com>
 */

package it.unisannio.srss.utils;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryCopier {
	
	private final static Logger LOG = LoggerFactory
			.getLogger(DirectoryCopier.class);

	public static void copy(final Path source, final Path target) throws IOException {

		EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);// follow the link when walking in a directory

		Files.walkFileTree(source, options, Integer.MAX_VALUE,
				new FileVisitor<Path>() {
			 
			public FileVisitResult postVisitDirectory(Path dir,
					IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs)  {
				CopyOption[] opt = new CopyOption[]{COPY_ATTRIBUTES,REPLACE_EXISTING};
				LOG.info("Source Directory "+dir);
				Path newDirectory = target.resolve(source.relativize(dir));
				LOG.info("Target Directory "+newDirectory);
				try{
					LOG.info("creating directory tree "+Files.copy(dir, newDirectory,opt));
				}
				catch(FileAlreadyExistsException x){
				}
				catch(IOException x){
					return FileVisitResult.SKIP_SUBTREE;
				}

				return CONTINUE;
			}

			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				// TODO Auto-generated method stub
				//LOG.info("results");
				LOG.info("Copying file:"+file);
				copyFile(file, target.resolve(source.relativize(file)));
				return CONTINUE;
			}

			public FileVisitResult visitFileFailed(Path file,
					IOException exc) throws IOException {
				// TODO -generated method stub
				return CONTINUE;
			}
		});
	}

	private static void copyFile(Path source,Path target) throws IOException{
		CopyOption[] options = new CopyOption[]{REPLACE_EXISTING,COPY_ATTRIBUTES};
		LOG.info("Copied file "+Files.copy(source, target,options)); 
	}
}
