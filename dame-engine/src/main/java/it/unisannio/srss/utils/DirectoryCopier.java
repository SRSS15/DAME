/*
 * Copyright 2015 
 * 	Danilo Cianciulli 			<cianciullidanilo@gmail.com>
 * 	Emranno Francesco Sannini 	<esannini@gmail.com>
 * 	Roberto Falzarano 			<robertofalzarano@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unisannio.srss.utils;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.CopyOption;
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
				LOG.debug("Source Directory "+dir);
				Path newDirectory = target.resolve(source.relativize(dir));
				LOG.debug("Target Directory "+newDirectory);
				try{
					LOG.debug("creating directory tree "+Files.copy(dir, newDirectory,opt));
				}catch(IOException x){}

				return CONTINUE;
			}

			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				LOG.debug("Copying file:"+file);
				copyFile(file, target.resolve(source.relativize(file)));
				return CONTINUE;
			}

			public FileVisitResult visitFileFailed(Path file,
					IOException exc) throws IOException {
				return CONTINUE;
			}
		});
	}

	private static void copyFile(Path source,Path target) throws IOException{
		CopyOption[] options = new CopyOption[]{REPLACE_EXISTING,COPY_ATTRIBUTES};
		LOG.debug("Copied file "+Files.copy(source, target,options)); 
	}
}
