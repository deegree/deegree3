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
import picocli.CommandLine.Parameters;

@Command(name = "updateschema", description = "Updates all found schema http(s)://schemas.deegree.org refernces")
public class UpdateSchema extends AbstractScanner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(UpdateSchema.class.getSimpleName());

	@Option(names = { "-w", "--write" }, description = "Make changes to files")
	private boolean write = false;

	@Parameters(index = "0", description = "Version to use in Schema references")
	private String version;

	public static void main(String... args) {
		new CommandLine(new UpdateSchema()).execute(args);
	}

	@Override
	public void run() {
		try {
			scanAndFix();
		} catch (Exception ex) {
			LOG.error("Error {}", ex.getMessage());
			LOG.warn("Execption", ex);
		}
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	public void scanAndFix() throws IOException {
		Path base = Paths.get(path);// .toAbsolutePath();
		LOG.info("Base Path: {}", base);
		List<Path> files = getFilesMatchingCompletePath(base, //
				"glob:**/*.{adoc,java,se,sld,xml,xsd}", // includes
				"glob:**/uncoupled/**", // excludes
				"glob:**/target/**");
		LOG.info("Found {} files", files.size());

		Pattern match = Pattern.compile("((?:http|https)://schemas\\.deegree\\.org)([^ \"]+\\.xsd)");

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

					output.append(mLoc.group(1));
					String stringOrg = mLoc.group(2);
					Path pathRel = Paths.get("/" + version).resolve(removeVersion(Paths.get(stringOrg)));
					String stringRep = StreamSupport.stream(pathRel.spliterator(), false) //
							.map(Path::toString) //
							.collect(joining("/", "/", ""));
					if (!stringOrg.equals(stringRep)) {
						LOG.info("[{}] loc {} <-> {}", rel, stringOrg, stringRep);
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

}
