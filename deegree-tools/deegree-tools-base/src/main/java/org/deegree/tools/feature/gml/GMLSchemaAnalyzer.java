/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.tools.feature.gml;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.gml.GMLVersion;
import org.deegree.tools.i18n.Messages;

/**
 * Prints an analysis of the feature type hierarchy defined in a GML application schema as
 * well as information on the geometry element hierarchy.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
@Tool("Prints an analysis of the feature type hierarchy defined in a GML application schema as well as information on the geometry element hierarchy.")
public class GMLSchemaAnalyzer {

	// command line parameters
	private static final String OPT_INPUT_FILE = "inputfile";

	private static final String OPT_NAMESPACE = "namespace";

	private static void printFeatureTypeHierarchy(org.deegree.gml.schema.GMLSchemaInfoSet analyzer, String ns) {
		System.out.println("Feature types in namespace {" + ns + "}:\n");
		XSElementDeclaration abstractFeatureDecl = analyzer.getAbstractFeatureElementDeclaration();
		for (XSElementDeclaration featureDecl : analyzer.getSubstitutions(abstractFeatureDecl, ns, false, false)) {
			if (!featureDecl.equals(abstractFeatureDecl)) {
				printElementHierarchy(analyzer, featureDecl, ns, "");
			}
		}
	}

	private static void printGeometryTypeInformation(org.deegree.gml.schema.GMLSchemaInfoSet analyzer) {
		System.out.println("\nGeometry types:\n");
		XSElementDeclaration geometryDecl = analyzer.getAbstractGeometryElementDeclaration();
		for (XSElementDeclaration decl : analyzer.getSubstitutions(geometryDecl, null, false, false)) {
			if (!decl.equals(geometryDecl)) {
				printElementHierarchy(analyzer, decl, null, "");
			}
		}

		System.out.println("\nCurve segments:\n");
		XSElementDeclaration curveSegmentDecl = analyzer.getAbstractCurveSegmentElementDeclaration();
		for (XSElementDeclaration decl : analyzer.getSubstitutions(curveSegmentDecl, null, false, false)) {
			if (!decl.equals(curveSegmentDecl)) {
				printElementHierarchy(analyzer, decl, null, "");
			}
		}

		System.out.println("\nSurface patches:\n");
		XSElementDeclaration surfacePatchDecl = analyzer.getAbstractSurfacePatchElementDeclaration();
		for (XSElementDeclaration decl : analyzer.getSubstitutions(surfacePatchDecl, null, false, false)) {
			if (!decl.equals(surfacePatchDecl)) {
				printElementHierarchy(analyzer, decl, null, "");
			}
		}
	}

	private static void printElementHierarchy(org.deegree.gml.schema.GMLSchemaInfoSet analyzer,
			XSElementDeclaration decl, String ns, String indent) {
		if (ns == null) {
			System.out.println(indent + "- {" + decl.getNamespace() + "}" + decl.getName()
					+ (decl.getAbstract() ? " (abstract)" : ""));
		}
		else {
			System.out.println(indent + "- " + decl.getName() + (decl.getAbstract() ? " (abstract)" : ""));
		}

		for (XSElementDeclaration featureDecl : analyzer.getSubstitutions(decl, ns, false, false)) {
			printElementHierarchy(analyzer, featureDecl, ns, indent + "  ");
		}
	}

	/**
	 * @param args
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws ClassCastException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, MalformedURLException {

		Options options = initOptions();

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(options);
		}

		try {
			new PosixParser().parse(options, args);

			String inputFileName = options.getOption(OPT_INPUT_FILE).getValue();
			String namespace = options.getOption(OPT_NAMESPACE).getValue();

			File file = new File(inputFileName);
			org.deegree.gml.schema.GMLSchemaInfoSet analyzer = new org.deegree.gml.schema.GMLSchemaInfoSet(
					GMLVersion.GML_31, file.toURI().toURL().toString());

			printFeatureTypeHierarchy(analyzer, namespace);
			printGeometryTypeInformation(analyzer);

		}
		catch (ParseException exp) {
			System.err.println(Messages.getMessage("TOOL_COMMANDLINE_ERROR", exp.getMessage()));
			// printHelp( options );
		}
	}

	private static Options initOptions() {

		Options opts = new Options();

		Option opt = new Option(OPT_INPUT_FILE, true, "input GML application schema file");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_NAMESPACE, true, "namespace of the element declarations to be analyzed");
		opt.setRequired(true);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);

		return opts;

	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, GMLSchemaAnalyzer.class.getSimpleName(), null, null);
	}

}
