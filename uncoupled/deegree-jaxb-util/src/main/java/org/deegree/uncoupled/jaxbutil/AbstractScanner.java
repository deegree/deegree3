/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.uncoupled.jaxbutil;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Option;

public abstract class AbstractScanner {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractScanner.class);

	protected static final Pattern PAT_VERSION = Pattern.compile("^(VERSION|V|)[0-9]([._-][0-9]+)*(|-SNAPSHOT)$",
			Pattern.CASE_INSENSITIVE);

	@Option(names = { "-b", "--base" }, description = "Base path (where to start scanning)")
	protected String path = "../../";

	@Option(names = { "--schemas" }, //
			description = "Location where to find schemas, default to http://schemas.deegree.org")
	protected String schemasUrl = "http://schemas.deegree.org";

	public static List<Path> getFilesMatching(Path basePath, String glob) throws IOException {
		if (!Files.isDirectory(basePath)) {
			return emptyList();
		}

		final PathMatcher mext = FileSystems.getDefault().getPathMatcher(glob);
		final List<Path> res = new LinkedList<Path>();

		Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				if (!attrs.isDirectory() && mext.matches(file.getFileName())) {
					LOG.trace("Lookup Soruce file {} for test", file);
					res.add(file);
				}

				return FileVisitResult.CONTINUE;
			}
		});

		return res;
	}

	public static List<Path> getFilesMatchingCompletePath(Path basePath, String glob, String... excludes)
			throws IOException {
		if (!Files.isDirectory(basePath)) {
			return emptyList();
		}
		FileSystem fs = FileSystems.getDefault();
		final PathMatcher mext = fs.getPathMatcher(glob);
		List<PathMatcher> mexcludes = Arrays.stream(excludes) //
				.map(fs::getPathMatcher) //
				.collect(Collectors.toList());
		final List<Path> res = new LinkedList<Path>();

		Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				boolean exclude = false;
				for (PathMatcher mexclude : mexcludes) {
					if (mexclude.matches(file)) {
						exclude = true;
						break;
					}
				}

				if (!attrs.isDirectory() && !exclude && mext.matches(file)) {
					LOG.trace("Lookup Soruce file {} for test", file);
					res.add(file);
				}

				return FileVisitResult.CONTINUE;
			}
		});

		return res;
	}

	protected Path removeVersion(Path p) {
		Path res = null;
		for (int i = 0, len = p.getNameCount(); i < len; i++) {
			String seg = p.getName(i).toString();
			if (PAT_VERSION.matcher(seg).matches()) {
				continue;
			}
			if (res == null) {
				res = p.getName(i);
			} else {
				res = res.resolve(p.getName(i));
			}
		}
		return res;
	}
}
