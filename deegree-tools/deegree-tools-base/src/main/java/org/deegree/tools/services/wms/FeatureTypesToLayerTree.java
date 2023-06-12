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
package org.deegree.tools.services.wms;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.tools.i18n.Messages;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultResourceLocation;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
@Tool(value = "generates a WMS layer tree/configuration file from a feature type hierarchy")
public class FeatureTypesToLayerTree {

	private static final Logger LOG = getLogger(FeatureTypesToLayerTree.class);

	private static Options initOptions() {
		Options opts = new Options();

		Option opt = new Option("f", "file", true, "path to a feature store configuration");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("o", "output", true, "path to the WMS configuration output file");
		opt.setRequired(true);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);

		return opts;

	}

	private static String ns = "http://www.deegree.org/services/wms";

	private static void writeLayer(HashSet<FeatureType> visited, XMLStreamWriter out, FeatureType ft, String storeId)
			throws XMLStreamException {
		if (visited.contains(ft) || ft == null) {
			return;
		}
		visited.add(ft);

		out.writeCharacters("\n");
		out.writeStartElement(ns, "RequestableLayer");

		XMLAdapter.writeElement(out, ns, "Name", ft.getName().getLocalPart());
		XMLAdapter.writeElement(out, ns, "Title", ft.getName().getLocalPart());

		for (FeatureType sub : ft.getSchema().getDirectSubtypes(ft)) {
			writeLayer(visited, out, sub, storeId);
		}

		XMLAdapter.writeElement(out, ns, "FeatureStoreId", storeId);

		out.writeEndElement();
		out.writeCharacters("\n");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = initOptions();

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			CommandUtils.printHelp(options, FeatureTypesToLayerTree.class.getSimpleName(), null, null);
		}

		XMLStreamWriter out = null;
		try {
			CommandLine line = new PosixParser().parse(options, args);

			String storeFile = line.getOptionValue("f");
			String nm = new File(storeFile).getName();
			String storeId = nm.substring(0, nm.length() - 4);

			FileOutputStream os = new FileOutputStream(line.getOptionValue("o"));
			XMLOutputFactory fac = XMLOutputFactory.newInstance();
			out = new IndentingXMLStreamWriter(fac.createXMLStreamWriter(os));
			out.setDefaultNamespace(ns);

			Workspace ws = new DefaultWorkspace(new File("nix"));
			ws.initAll();
			DefaultResourceIdentifier<FeatureStore> identifier = new DefaultResourceIdentifier<FeatureStore>(
					FeatureStoreProvider.class, "unknown");
			ws.add(new DefaultResourceLocation<FeatureStore>(new File(storeFile), identifier));
			ws.prepare(identifier);
			FeatureStore store = ws.init(identifier, null);

			AppSchema schema = store.getSchema();

			// prepare document
			out.writeStartDocument();
			out.writeStartElement(ns, "deegreeWMS");
			out.writeDefaultNamespace(ns);
			out.writeAttribute("configVersion", "0.5.0");
			out.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			out.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
					"http://www.deegree.org/services/wms https://schemas.deegree.org/core/3.5/wms/wms_configuration.xsd");
			out.writeStartElement(ns, "ServiceConfiguration");

			HashSet<FeatureType> visited = new HashSet<FeatureType>();

			if (schema.getRootFeatureTypes().length == 1) {
				writeLayer(visited, out, schema.getRootFeatureTypes()[0], storeId);
			}
			else {
				out.writeCharacters("\n");
				out.writeStartElement(ns, "UnrequestableLayer");
				XMLAdapter.writeElement(out, ns, "Title", "Root Layer");
				for (FeatureType ft : schema.getRootFeatureTypes()) {
					writeLayer(visited, out, ft, storeId);
				}
				out.writeEndElement();
				out.writeCharacters("\n");
			}

			out.writeEndElement();
			out.writeEndElement();
			out.writeEndDocument();
		}
		catch (ParseException exp) {
			System.err.println(Messages.getMessage("TOOL_COMMANDLINE_ERROR", exp.getMessage()));
			CommandUtils.printHelp(options, FeatureTypesToLayerTree.class.getSimpleName(), null, null);
		}
		catch (ResourceInitException e) {
			LOG.info("The feature store could not be loaded: '{}'", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		catch (FileNotFoundException e) {
			LOG.info("A file could not be found: '{}'", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		catch (XMLStreamException e) {
			LOG.info("The XML output could not be written: '{}'", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		catch (FactoryConfigurationError e) {
			LOG.info("The XML system could not be initialized: '{}'", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (XMLStreamException e) {
					LOG.trace("Stack trace:", e);
				}
			}
		}

	}

}
