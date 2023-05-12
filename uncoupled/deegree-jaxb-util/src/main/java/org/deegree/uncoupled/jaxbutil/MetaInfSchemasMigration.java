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

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "migrateschema", description = "Migrate Schemas to structure w/o version element")
public class MetaInfSchemasMigration extends AbstractScanner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MetaInfSchemasMigration.class.getSimpleName());

	@Option(names = { "-w", "--write" }, description = "Make changes to files")
	private boolean write = false;

	public static void main(String... args) {
		new CommandLine(new MetaInfSchemasMigration()).execute(args);
	}

	public void run() {
		try {
			moveSchemasAndExamples();
			convertMetaInfReferences();
			convertRelative();
			convertImport();
		} catch (Exception ex) {
			LOG.error("Error {}", ex.getMessage());
			LOG.warn("Execption", ex);
		}
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	/* move files into folders without version */

	public void moveSchemasAndExamples() throws IOException {
		Path base = Paths.get(path);
		LOG.info("Base Path: {}", base);
		List<Path> files = getFilesMatchingCompletePath(base, "glob:**/META-INF/schemas/**", // includes
				"glob:**/uncoupled/**", // excludes
				"glob:**/target/**");
		LOG.info("Found {} files", files.size());

		files.forEach(p -> {
			Path relA = base.relativize(p);
			try {
				Path pathMove = makeMovePath(p);
				if (!pathMove.equals(p)) {
					Path relB = base.relativize(pathMove);
					LOG.info("Moving {} -> {}", relA, relB);
					if (write) {
						if (!Files.isDirectory(pathMove.getParent())) {
							Files.createDirectories(pathMove.getParent());
						}
						Files.move(p, pathMove);
					}
				}
			} catch (Exception ex) {
				LOG.warn("[{}] failed to move, {}", relA, ex.getMessage());
				LOG.warn("Execption", ex);
			}

		});
	}

	/* convert internal meta-inf references */

	public void convertMetaInfReferences() throws IOException {
		Path base = Paths.get(path);
		LOG.info("Base Path: {}", base);
		List<Path> files = getFilesMatchingCompletePath(base, "glob:**/*.java", // includes
				"glob:**/uncoupled/**", // excludes
				"glob:**/target/**");
		LOG.info("Found {} files", files.size());

		Pattern match = Pattern.compile("\\/META-INF\\/schemas\\/[^ \"]+\\.xsd", Pattern.DOTALL);

		files.forEach(p -> {
			Path rel = base.relativize(p);
			try {
				String content = new String(Files.readAllBytes(p));
				int lastIndex = 0;
				StringBuilder output = new StringBuilder();

				LOG.debug("- {} [size {}]", rel, content.length());
				Matcher mLoc = match.matcher(content);
				while (mLoc.find()) {
					output.append(content, lastIndex, mLoc.start());

					String stringOrg = mLoc.group();
					Path pathRel = removeVersion(Paths.get(stringOrg));
					String stringRep = StreamSupport.stream(pathRel.spliterator(), false) //
							.map(Path::toString) //
							.collect(joining("/", "/", ""));
					if (!stringOrg.equals(stringRep)) {
						LOG.info("[{}] reference {} <-> {}", rel, stringOrg, stringRep);
						output.append(stringRep);
					} else {
						output.append(stringOrg);
					}

					lastIndex = mLoc.end();
				}
				if (lastIndex < content.length()) {
					output.append(content, lastIndex, content.length());
				}
				if (write) {
					Files.write(p, output.toString().getBytes());
				}
			} catch (Exception ex) {
				LOG.warn("{} failed to read with {}", rel, ex.getMessage());
				LOG.warn("Execption", ex);
			}
		});
	}

	// StringVisitor visitMeta = new
	// StringVisitor("\\/META-INF\\/schemas\\/.*\\.xsd", Pattern.CASE_INSENSITIVE);

	private Path makeMovePath(Path path) throws Exception {
		Path match = Paths.get("META-INF/schemas");
		for (int i = 0, len = path.getNameCount(); i < len - 1; i++) {
			if (path.subpath(i, len - 1).startsWith(match)) {
				Path sub = removeVersion(path.subpath(i + 2, len));
				return path.subpath(0, i + 2).resolve(sub);
			}
		}
		throw new Exception("Could not convert path");
	}

	/* import handling */
	
	public void convertRelative() throws IOException {
		Path base = Paths.get(path);// .toAbsolutePath();
		LOG.info("Base Path: {}", base);
		List<Path> files = getFilesMatchingCompletePath(base, //
				"glob:**/META-INF/schemas/**/*.xsd", // includes
				"glob:**/uncoupled/**", // excludes
				"glob:**/target/**");
		LOG.info("Found {} files", files.size());

		Pattern matchLocation = Pattern.compile("schemaLocation=\"([^\"]+)\"", Pattern.DOTALL);
		Pattern matchRelative = Pattern.compile("^\\.\\.\\/[^ ]+\\.xsd$");
		files.forEach(p -> {
			Path rel = base.relativize(p);
			try {
				Path pathFile = makeSubpathNoVersion(p);

				String content = new String(Files.readAllBytes(p));
				int lastIndex = 0;
				StringBuilder output = new StringBuilder();

				LOG.debug("- {} [size {}]", rel, content.length());
				Matcher mLoc = matchLocation.matcher(content);
				while (mLoc.find()) {
					output.append(content, lastIndex, mLoc.start(1));
					String contentSub = mLoc.group(1);
					if ( matchRelative.matcher(contentSub).matches() ) {
						Path a = pathFile.getParent();
						Path b = Paths.get(contentSub);
						while ( b.getNameCount() > 1 && a.getNameCount() > 1 && "..".equals(b.getName(0).toString())) {
							//LOG.info("Before {} / {}", a, b);
							a = a.subpath(0, a.getNameCount() - 1);
							b = b.subpath(1, b.getNameCount());
							//LOG.info("After {} / {}", a, b);
						}
						Path pathRef;
						if (a.getNameCount() == 1 &&  "..".equals(b.getName(0).toString())) {
							pathRef = b.subpath(1, b.getNameCount());
						} else {
							pathRef = a.resolve(b); 
						}
						String stringBase = StreamSupport.stream(pathFile.getParent().spliterator(), false) //
								.map(elem -> "..") //
								.collect(joining("/"));
						String stringRel = StreamSupport.stream(pathRef.spliterator(), false) //
								.map(Path::toString) //
								.collect(joining("/"));
						String newUrl = stringBase + "/" + stringRel;
						if ( !contentSub.equals(newUrl))
							LOG.info( "[{}] \n {} -> {} ", rel, contentSub, newUrl );
					} else {
						output.append(contentSub);
					}
					lastIndex = mLoc.end(1);
				}
				if (lastIndex < content.length()) {
					output.append(content, lastIndex, content.length());
				}

				if (write) {
					Files.write(p, output.toString().getBytes());
				}
			} catch (Exception ex) {
				LOG.warn("{} failed to read with {}", rel, ex.getMessage());
				LOG.warn("Execption", ex);
			}
		});
		
	}

	public void convertImport() throws IOException {
		Path base = Paths.get(path);// .toAbsolutePath();
		LOG.info("Base Path: {}", base);
		List<Path> files = getFilesMatchingCompletePath(base, //
				"glob:**/META-INF/schemas/**/*.xsd", // includes
				"glob:**/uncoupled/**", // excludes
				"glob:**/target/**");
		LOG.info("Found {} files", files.size());

		Pattern matchLocation = Pattern.compile("schemaLocation=\"([^\"]+)\"", Pattern.DOTALL);
		Pattern matchSchemaUrl = Pattern.compile("(?:http|https)://schemas\\.deegree\\.org[^ \"]+\\.xsd");

		files.forEach(p -> {
			Path rel = base.relativize(p);
			try {
				Path pathFile = makeSubpathNoVersion(p);

				String content = new String(Files.readAllBytes(p));
				int lastIndex = 0;
				StringBuilder output = new StringBuilder();

				LOG.debug("- {} [size {}]", rel, content.length());
				Matcher mLoc = matchLocation.matcher(content);
				while (mLoc.find()) {
					output.append(content, lastIndex, mLoc.start(1));

					String contentSub = mLoc.group(1);
					int lastIndexSub = 0;
					LOG.trace("[{}] loc {}", rel, contentSub);
					Matcher mUrl = matchSchemaUrl.matcher(contentSub);
					while (mUrl.find()) {
						output.append(contentSub, lastIndexSub, mUrl.start());

						Path pathRef = makeSubpathNoVersion(mUrl.group());

						String stringBase = StreamSupport.stream(pathFile.getParent().spliterator(), false) //
								.map(elem -> "..") //
								.collect(joining("/"));
						String stringRel = StreamSupport.stream(pathRef.spliterator(), false) //
								.map(Path::toString) //
								.collect(joining("/"));
						String newUrl = stringBase + "/" + stringRel;

						LOG.info("[{}] Change Import {} -> {}", rel, mUrl.group(), newUrl);

						output.append(newUrl);
						lastIndexSub = mUrl.end();
					}
					if (lastIndexSub < contentSub.length()) {
						output.append(contentSub, lastIndexSub, contentSub.length());
					}

					lastIndex = mLoc.end(1);
				}
				if (lastIndex < content.length()) {
					output.append(content, lastIndex, content.length());
				}

				if (write) {
					Files.write(p, output.toString().getBytes());
				}
			} catch (Exception ex) {
				LOG.warn("{} failed to read with {}", rel, ex.getMessage());
				LOG.warn("Execption", ex);
			}
		});
	}

	private Path makeSubpathNoVersion(String uri) throws Exception {
		if (uri.startsWith(schemasUrl)) {
			Path p = Paths.get(uri.substring(schemasUrl.length() + 1));
			return removeVersion(p);
		}
		throw new Exception("Could not convert uri");
	}

	private Path makeSubpathNoVersion(Path path) throws Exception {
		Path match = Paths.get("META-INF/schemas");
		for (int i = 0, len = path.getNameCount(); i < len - 1; i++) {
			if (path.subpath(i, len - 1).startsWith(match)) {
				String sp = path.subpath(i + 2, len).toString();
				// LOG.info("Subpath {}", sp);
				return removeVersion(Paths.get(sp));
			}
		}
		throw new Exception("Could not convert path");
	}
}
