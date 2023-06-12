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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

/**
 * Prints an analysis of the global element declarations in an XML schema.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
@Tool("Prints an analysis of the global element declarations in an XML schema and their content models.")
public class SchemaAnalyzer {

	// command line parameters
	private static final String OPT_INPUT_FILE = "inputfile";

	private static final String OPT_NAMESPACE = "namespace";

	protected final XSModel schema;

	protected SchemaAnalyzer(XSModel schema) {
		this.schema = schema;
	}

	public Set<XSElementDeclaration> getSubstitutions(XSElementDeclaration substitutee) {
		Set<XSElementDeclaration> substitutions = new HashSet<XSElementDeclaration>();
		XSNamedMap elementDeclMap = schema.getComponents(XSConstants.ELEMENT_DECLARATION);
		for (int i = 0; i < elementDeclMap.getLength(); i++) {
			XSElementDecl elementDecl = (XSElementDecl) elementDeclMap.item(i);
			if (isSubstitutable(elementDecl.getSubstitutionGroupAffiliation(), substitutee)) {
				substitutions.add(elementDecl);
			}
		}
		return substitutions;
	}

	private boolean isSubstitutable(XSElementDeclaration elementDecl, XSElementDeclaration substitutee) {
		if (elementDecl == null) {
			return false;
		}
		if (elementDecl == substitutee) {
			return true;
		}
		return isSubstitutable(elementDecl.getSubstitutionGroupAffiliation(), substitutee);
	}

	public String toString(QName elementName) {
		XSElementDeclaration elementDecl = schema.getElementDeclaration(elementName.getNamespaceURI(),
				elementName.getLocalPart());
		return toString(elementDecl);
	}

	public String toString(XSElementDeclaration elementDecl) {
		String s = "Element name: '" + elementDecl.getName() + "', (" + elementDecl.getNamespace() + ")";
		s += "\n - abstract: " + (elementDecl.getAbstract() ? "true" : "false");

		if (elementDecl.getSubstitutionGroupAffiliation() != null) {
			s += "\n - substitutionGroup hierarchy: ";
			s += generateSubstitutionHierarchy(elementDecl.getSubstitutionGroupAffiliation(), "  ");
		}

		XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
		switch (typeDef.getTypeCategory()) {
			case XSTypeDefinition.SIMPLE_TYPE: {
				s += toString((XSSimpleTypeDefinition) typeDef);
				break;
			}
			case XSTypeDefinition.COMPLEX_TYPE: {
				s += toString((XSComplexTypeDefinition) typeDef);
				break;
			}
			default: {
				// cannot happen
			}
		}
		return s;
	}

	String toString(XSSimpleTypeDefinition simpleType) {
		String s = "\n - simple type: '" + simpleType.getName() + "' (" + simpleType.getNamespace() + ")";
		XSTypeDefinition baseType = simpleType.getBaseType();
		if (baseType != null) {
			s += "\n - type hierarchy:";
			s += generateTypeHierarchy(baseType, "  ");
		}
		return s;
	}

	String toString(XSComplexTypeDefinition complexType) {
		String s = "\n - complex type: '" + complexType.getName() + "' (" + complexType.getNamespace() + ")";
		XSObjectList attributeUses = complexType.getAttributeUses();
		for (int i = 0; i < attributeUses.getLength(); i++) {
			XSAttributeUse attributeUse = (XSAttributeUse) attributeUses.item(i);
			s += "\n - attribute: " + attributeUse.getAttrDeclaration().getName();
		}
		XSTypeDefinition baseType = complexType.getBaseType();
		if (baseType != null) {
			s += "\n - type hierarchy:";
			s += generateTypeHierarchy(baseType, "  ");
		}

		s += "\n - content model: ";
		switch (complexType.getContentType()) {
			case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
				s += "element only";
				XSParticle particle = complexType.getParticle();
				s += generateParticleHierarchy(particle, "  ");
				break;
			}
			case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
				s += "empty";
				break;
			}
			case XSComplexTypeDefinition.CONTENTTYPE_MIXED: {
				s += "mixed";
				XSParticle particle = complexType.getParticle();
				s += generateParticleHierarchy(particle, "  ");
				break;
			}
			case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE: {
				s += "simple";
				break;
			}
			default: {
				// cannot happen
			}
		}

		return s;
	}

	String generateTypeHierarchy(XSTypeDefinition type, String indent) {

		String s = "\n" + indent + "-> '" + type.getName() + "' (" + type.getNamespace() + "'): ";
		switch (type.getTypeCategory()) {
			case XSTypeDefinition.SIMPLE_TYPE: {
				s += "simple";
				break;
			}
			case XSTypeDefinition.COMPLEX_TYPE: {
				s += "complex";
				break;
			}
			default: {
				// cannot happen
			}
		}
		if (type.getBaseType() != null && type.getBaseType() != type) {
			s += generateTypeHierarchy(type.getBaseType(), " " + indent);
		}
		return s;
	}

	String generateParticleHierarchy(XSParticle particle, String indent) {

		String s = "";

		switch (particle.getTerm().getType()) {
			case XSConstants.MODEL_GROUP: {
				XSModelGroup modelGroup = (XSModelGroup) particle.getTerm();
				switch (modelGroup.getCompositor()) {
					case XSModelGroup.COMPOSITOR_ALL: {
						s = "\n" + indent + "- all " + generateOccurenceInfo(particle);
						XSObjectList subParticles = modelGroup.getParticles();
						for (int i = 0; i < subParticles.getLength(); i++) {
							XSParticle subParticle = (XSParticle) subParticles.item(i);
							s += generateParticleHierarchy(subParticle, " " + indent);
						}
						break;
					}
					case XSModelGroup.COMPOSITOR_CHOICE: {
						s = "\n" + indent + "- choice " + generateOccurenceInfo(particle);
						XSObjectList subParticles = modelGroup.getParticles();
						for (int i = 0; i < subParticles.getLength(); i++) {
							XSParticle subParticle = (XSParticle) subParticles.item(i);
							s += generateParticleHierarchy(subParticle, " " + indent);
						}
						break;
					}
					case XSModelGroup.COMPOSITOR_SEQUENCE: {
						if (!isParticleRedundant(particle)) {
							s = "\n" + indent + "- sequence " + generateOccurenceInfo(particle);
							XSObjectList subParticles = modelGroup.getParticles();
							for (int i = 0; i < subParticles.getLength(); i++) {
								XSParticle subParticle = (XSParticle) subParticles.item(i);
								s += generateParticleHierarchy(subParticle, " " + indent);
							}
						}
						else {
							XSObjectList subParticles = modelGroup.getParticles();
							for (int i = 0; i < subParticles.getLength(); i++) {
								XSParticle subParticle = (XSParticle) subParticles.item(i);
								s += generateParticleHierarchy(subParticle, indent);
							}
						}
						break;
					}
					default: {
						// cannot happen
					}
				}
				break;
			}
			case XSConstants.ELEMENT_DECLARATION: {
				XSElementDeclaration elementDecl = (XSElementDeclaration) particle.getTerm();
				s = "\n" + indent + "- element: '" + elementDecl.getName() + "' (" + elementDecl.getNamespace() + ") "
						+ generateOccurenceInfo(particle);
				s += ", type: '" + elementDecl.getTypeDefinition().getName() + "' ("
						+ elementDecl.getTypeDefinition().getNamespace() + ")";
				break;
			}
			case XSConstants.WILDCARD: {
				s = "\n" + indent + "- wildcard " + generateOccurenceInfo(particle);
				break;
			}
			default: {
				// cannot happen
			}
		}
		return s;
	}

	boolean isParticleRedundant(XSParticle particle) {
		if (particle.getMaxOccursUnbounded()) {
			return false;
		}
		return particle.getMinOccurs() == 1 && particle.getMaxOccurs() == 1;
	}

	String generateOccurenceInfo(XSParticle particle) {
		return "(minOccurs=" + particle.getMinOccurs() + ", maxOccurs="
				+ (particle.getMaxOccursUnbounded() ? "unbounded" : particle.getMaxOccurs()) + ")";
	}

	String generateSubstitutionHierarchy(XSElementDeclaration elementDecl, String indent) {
		if (elementDecl == null) {
			return "";
		}
		String s = "\n" + indent + "-> '" + elementDecl.getName() + "' (" + elementDecl.getNamespace() + ")";
		s += generateSubstitutionHierarchy(elementDecl.getSubstitutionGroupAffiliation(), indent + " ");
		return s;
	}

	List<PropertyDeclaration> getProperties(XSElementDeclaration elementDecl) {

		List<PropertyDeclaration> properties = new ArrayList<PropertyDeclaration>();
		XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) elementDecl.getTypeDefinition();

		switch (complexType.getContentType()) {
			case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
			case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
				XSParticle particle = complexType.getParticle();
				collectProperties(particle, properties);
				break;
			}
			case XSComplexTypeDefinition.CONTENTTYPE_EMPTY:
			case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE: {
				break;
			}
			default: {
				// cannot happen
			}
		}
		return properties;
	}

	private void collectProperties(XSParticle particle, List<PropertyDeclaration> properties) {
		switch (particle.getTerm().getType()) {
			case XSConstants.MODEL_GROUP: {
				XSModelGroup modelGroup = (XSModelGroup) particle.getTerm();
				XSObjectList subParticles = modelGroup.getParticles();
				for (int i = 0; i < subParticles.getLength(); i++) {
					XSParticle subParticle = (XSParticle) subParticles.item(i);
					collectProperties(subParticle, properties);
				}
				break;
			}
			case XSConstants.ELEMENT_DECLARATION: {
				XSElementDeclaration elementDecl = (XSElementDeclaration) particle.getTerm();
				QName propertyName = new QName(elementDecl.getNamespace(), elementDecl.getName());
				String minOccurs = "" + particle.getMinOccurs();
				String maxOccurs = particle.getMaxOccursUnbounded() ? "unbounded" : "" + particle.getMaxOccurs();
				QName propertyType = new QName(elementDecl.getTypeDefinition().getNamespace(),
						elementDecl.getTypeDefinition().getName());
				PropertyDeclaration property = new PropertyDeclaration(propertyName, minOccurs, maxOccurs,
						propertyType);
				properties.add(property);
				break;
			}
			case XSConstants.WILDCARD: {
				break;
			}
			default: {
				// cannot happen
			}
		}
	}

	public String getElementDeclarationSummary(String namespace) {
		StringBuffer sb = new StringBuffer();
		XSNamedMap elementMap = schema.getComponentsByNamespace(XSConstants.ELEMENT_DECLARATION, namespace);
		SortedSet<String> elementNames = new TreeSet<String>();
		for (int i = 0; i < elementMap.getLength(); i++) {
			XSElementDeclaration elementDecl = (XSElementDeclaration) elementMap.item(i);
			sb.append(toString(elementDecl) + "\n");
			elementNames.add(elementDecl.getName());
		}
		sb.append(elementNames.size() + " element declarations in namespace: '" + namespace + "':\n");
		for (String elementName : elementNames) {
			sb.append(elementName + "\n");
		}
		return sb.toString();
	}

	public void printSimpleTypesSummary(String namespace) {
		XSNamedMap elementMap = schema.getComponentsByNamespace(XSConstants.TYPE_DEFINITION, namespace);
		SortedSet<String> simpleTypeNames = new TreeSet<String>();
		for (int i = 0; i < elementMap.getLength(); i++) {
			XSTypeDefinition typeDef = (XSTypeDefinition) elementMap.item(i);
			if (typeDef.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
				System.out.println(toString(((XSSimpleTypeDefinition) typeDef)));
				simpleTypeNames.add(typeDef.getName());
			}
		}
		System.out.println(simpleTypeNames.size() + " simple types in namespace: '" + namespace + "':\n");
		for (String typeName : simpleTypeNames) {
			System.out.println(typeName);
		}
	}

	public void printComplexTypesSummary(String namespace) {
		XSNamedMap elementMap = schema.getComponentsByNamespace(XSConstants.TYPE_DEFINITION, namespace);
		SortedSet<String> complexTypeNames = new TreeSet<String>();
		for (int i = 0; i < elementMap.getLength(); i++) {
			XSTypeDefinition typeDef = (XSTypeDefinition) elementMap.item(i);
			if (typeDef.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
				System.out.println(toString(((XSComplexTypeDefinition) typeDef)));
				complexTypeNames.add(typeDef.getName());
			}
		}
		System.out.println(complexTypeNames.size() + " complex types in namespace: '" + namespace + "':\n");
		for (String typeName : complexTypeNames) {
			System.out.println(typeName);
		}
	}

	/**
	 * @param args
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws ClassCastException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {

		Options options = initOptions();

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(options);
		}

		try {
			new PosixParser().parse(options, args);

			String inputFileName = options.getOption(OPT_INPUT_FILE).getValue();
			String namespace = options.getOption(OPT_NAMESPACE).getValue();

			System.setProperty(DOMImplementationRegistry.PROPERTY,
					"org.apache.xerces.dom.DOMXSImplementationSourceImpl");
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
			XSLoader schemaLoader = impl.createXSLoader(null);

			File inputFile = new File(inputFileName);
			System.out.println("Loading input schema: '" + inputFileName + "'");
			XSModel schema = schemaLoader.loadURI(inputFile.toURI().toURL().toString());
			SchemaAnalyzer analyzer = new SchemaAnalyzer(schema);

			// analyzer.printElementDeclarationSummary( "http://www.opengis.net/gml" );
			String s = analyzer.getElementDeclarationSummary(namespace);
			System.out.println(s);
		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
		}
	}

	private static Options initOptions() {

		Options opts = new Options();

		Option opt = new Option(OPT_INPUT_FILE, true, "input XML schema file");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_NAMESPACE, true, "namespace of the element declarations to be analyzed");
		opt.setRequired(true);
		opts.addOption(opt);

		opts.addOption("?", "help", false, "print (this) usage information");

		return opts;
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, SchemaAnalyzer.class.getSimpleName(), null, null);
	}

}
