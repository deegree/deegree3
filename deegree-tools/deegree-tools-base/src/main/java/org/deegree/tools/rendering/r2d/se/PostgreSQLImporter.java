/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.tools.rendering.r2d.se;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderUtils;
import org.deegree.style.se.parser.PostgreSQLWriter;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.tools.i18n.Messages;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * <code>PostgreSQLImporter</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
@Tool(value = "This tool can be used to import SLD/SE files into a WMS styles database.")
public class PostgreSQLImporter {

	private static Options initOptions() {
		Options opts = new Options();

		Option opt = new Option("f", "sldfile", true, "input SE/SLD style file");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("d", "dburl", true, "database url, like jdbc:postgresql://localhost/dbname");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("u", "dbuser", true, "database user, like postgres");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("p", "dbpassword", true, "database password, if left off, will be set as empty");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("s", "schema", true, "table schema, if left off, public will be used");
		opt.setRequired(false);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);

		return opts;

	}

	/**
	 * @param args
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws IOException
	 */
	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError, IOException {
		Options options = initOptions();

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			CommandUtils.printHelp(options, PostgreSQLImporter.class.getSimpleName(), null, null);
		}

		try {
			new PosixParser().parse(options, args);

			String inputFile = options.getOption("sldfile").getValue();
			String url = options.getOption("dburl").getValue();
			String user = options.getOption("dbuser").getValue();
			String pass = options.getOption("dbpassword").getValue();
			if (pass == null) {
				pass = "";
			}
			String schema = options.getOption("schema").getValue();
			if (schema == null) {
				schema = "public";
			}

			XMLInputFactory fac = XMLInputFactory.newInstance();
			Style style = new SymbologyParser(true).parse(fac.createXMLStreamReader(new FileInputStream(inputFile)));

			Workspace workspace = new DefaultWorkspace(new File("nonexistant"));
			ResourceLocation<ConnectionProvider> loc = ConnectionProviderUtils.getSyntheticProvider("style", url, user,
					pass);
			workspace.startup();
			workspace.getLocationHandler().addExtraResource(loc);
			workspace.initAll();

			if (style.isSimple()) {
				new PostgreSQLWriter("style", schema, workspace).write(style, null);
			}
			else {
				new PostgreSQLWriter("style", schema, workspace).write(new FileInputStream(inputFile), style.getName());
			}
			workspace.destroy();
		}
		catch (ParseException exp) {
			System.err.println(Messages.getMessage("TOOL_COMMANDLINE_ERROR", exp.getMessage()));
			// printHelp( options );
		}

	}

}
