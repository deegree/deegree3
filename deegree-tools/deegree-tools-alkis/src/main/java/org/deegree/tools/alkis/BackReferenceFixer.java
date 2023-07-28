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
package org.deegree.tools.alkis;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.Feature;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.FeatureReference;
import org.deegree.gml.reference.GmlDocumentIdContext;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
@Tool(value = "adds inversDientZurDarstellungVon properties for GeoInfoDok 6.0.1 files")
public class BackReferenceFixer {

	private static final Logger LOG = getLogger(BackReferenceFixer.class);

	private static final String ns601 = "http://www.adv-online.de/namespaces/adv/gid/6.0";

	private static Options initOptions() {
		Options opts = new Options();

		Option opt = new Option("i", "input", true, "input file");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("o", "output", true, "output file");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("s", "schema", true, "schema file");
		opt.setRequired(true);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);

		return opts;
	}

	public static void main(String[] args) {
		Options opts = initOptions();
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			CommandLine line = new PosixParser().parse(opts, args);
			String input = line.getOptionValue('i');
			String output = line.getOptionValue('o');
			String schema = line.getOptionValue('s');
			fis = new FileInputStream(input);
			fos = new FileOutputStream(output);
			XMLInputFactory xifac = XMLInputFactory.newInstance();
			XMLOutputFactory xofac = XMLOutputFactory.newInstance();
			XMLStreamReader xreader = xifac.createXMLStreamReader(input, fis);
			IndentingXMLStreamWriter xwriter = new IndentingXMLStreamWriter(xofac.createXMLStreamWriter(fos));
			GMLStreamReader reader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_32, xreader);

			AppSchema appSchema = new GMLAppSchemaReader(null, null, schema).extractAppSchema();
			reader.setApplicationSchema(appSchema);

			GMLStreamWriter writer = GMLOutputFactory.createGMLStreamWriter(GMLVersion.GML_32, xwriter);
			XlinkedObjectsHandler handler = new XlinkedObjectsHandler(true, null, new GmlXlinkOptions());
			writer.setReferenceResolveStrategy(handler);

			QName prop = new QName(ns601, "dientZurDarstellungVon");

			Map<String, List<String>> refs = new HashMap<String, List<String>>();
			Map<String, List<String>> types = new HashMap<String, List<String>>();
			Map<String, String> bindings = null;

			for (Feature f : reader.readFeatureCollectionStream()) {
				if (bindings == null) {
					bindings = f.getType().getSchema().getNamespaceBindings();
				}
				for (Property p : f.getProperties(prop)) {
					FeatureReference ref = (FeatureReference) p.getValue();
					List<String> list = refs.get(ref.getId());
					if (list == null) {
						list = new ArrayList<String>();
						refs.put(ref.getId(), list);
					}
					list.add(f.getId());
					list = types.get(ref.getId());
					if (list == null) {
						list = new ArrayList<String>();
						types.put(ref.getId(), list);
					}
					list.add("inversZu_dientZurDarstellungVon_" + f.getType().getName().getLocalPart());
				}
			}

			QName[] inversePropNames = new QName[] { new QName(ns601, "inversZu_dientZurDarstellungVon_AP_Darstellung"),
					new QName(ns601, "inversZu_dientZurDarstellungVon_AP_LTO"),
					new QName(ns601, "inversZu_dientZurDarstellungVon_AP_PTO"),
					new QName(ns601, "inversZu_dientZurDarstellungVon_AP_FPO"),
					new QName(ns601, "inversZu_dientZurDarstellungVon_AP_KPO_3D"),
					new QName(ns601, "inversZu_dientZurDarstellungVon_AP_LPO"),
					new QName(ns601, "inversZu_dientZurDarstellungVon_AP_PPO") };

			reader.close();
			fis.close();
			writer.setNamespaceBindings(bindings);

			fis = new FileInputStream(input);
			xreader = xifac.createXMLStreamReader(input, fis);
			reader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_32, xreader);
			reader.setApplicationSchema(appSchema);

			if (bindings != null) {
				for (Map.Entry<String, String> e : bindings.entrySet()) {
					if (!e.getKey().isEmpty()) {
						xwriter.setPrefix(e.getValue(), e.getKey());
					}
				}
			}
			xwriter.writeStartDocument();
			xwriter.setPrefix("gml", "http://www.opengis.net/gml/3.2");
			xwriter.writeStartElement("http://www.opengis.net/gml/3.2", "FeatureCollection");
			xwriter.writeNamespace("gml", "http://www.opengis.net/gml/3.2");

			GmlDocumentIdContext ctx = new GmlDocumentIdContext(GMLVersion.GML_32);

			for (Feature f : reader.readFeatureCollectionStream()) {
				if (refs.containsKey(f.getId())) {
					List<Property> props = new ArrayList<Property>(f.getProperties());
					ListIterator<Property> iter = props.listIterator();
					String name = iter.next().getName().getLocalPart();
					while (name.equals("lebenszeitintervall") || name.equals("modellart") || name.equals("anlass")
							|| name.equals("zeigtAufExternes") || name.equals("istTeilVon")
							|| name.equals("identifier")) {
						if (iter.hasNext()) {
							name = iter.next().getName().getLocalPart();
						}
						else {
							break;
						}
					}
					if (iter.hasPrevious()) {
						iter.previous();
					}
					for (QName propName : inversePropNames) {
						Iterator<String> idIter = refs.get(f.getId()).iterator();
						Iterator<String> typeIter = types.get(f.getId()).iterator();
						while (idIter.hasNext()) {
							String id = idIter.next();
							if (typeIter.next().equals(propName.getLocalPart())) {
								PropertyType pt = f.getType().getPropertyDeclaration(propName);
								Property p = new GenericProperty(pt, new FeatureReference(ctx, "#" + id, null));
								iter.add(p);
							}
						}
					}
					f.setProperties(props);

				}
				xwriter.writeStartElement("http://www.opengis.net/gml/3.2", "featureMember");
				writer.write(f);
				xwriter.writeEndElement();
			}

			xwriter.writeEndElement();
			xwriter.close();
		}
		catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(fos);
		}
	}

}
