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
package org.deegree.gml.schema;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericAppSchema;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.gml.GMLVersion;
import org.w3c.dom.ls.LSInput;

/**
 * Provides access to the {@link AppSchema} defined in one or more GML schema documents.
 *
 * @see AppSchema
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @since 3.4
 */
public class GMLAppSchemaReader {

	private final GMLSchemaInfoSet gmlSchema;

	private final GmlObjectTypeFactory objectTypeFactory;

	// key: ft name, value: element declaration
	private Map<QName, XSElementDeclaration> ftNameToFtElement = new HashMap<QName, XSElementDeclaration>();

	// key: geometry name, value: element declaration
	private Map<QName, XSElementDeclaration> geometryNameToGeometryElement = new HashMap<QName, XSElementDeclaration>();

	// key: name of feature type, value: feature type
	private Map<QName, FeatureType> ftNameToFt = new HashMap<QName, FeatureType>();

	private Map<QName, GMLObjectType> typeNameToType = new HashMap<QName, GMLObjectType>();

	// key: name of ft A, value: name of ft B (A is in substitionGroup B)
	private Map<QName, QName> ftNameToSubstitutionGroupName = new HashMap<QName, QName>();

	private Map<QName, QName> geometryNameToSubstitutionGroupName = new HashMap<QName, QName>();

	private final Map<String, String> prefixToNs = new HashMap<String, String>();

	private final Map<String, String> nsToPrefix = new HashMap<String, String>();

	private int prefixIndex = 0;

	/**
	 * Creates a new {@link GMLAppSchemaReader} from the given schema file (which may be a
	 * directory).
	 * @param gmlVersion gml version of the schema files, can be null (auto-detect GML
	 * version)
	 * @param namespaceHints optional hints (key: prefix, value: namespaces) for
	 * generating 'nice' qualified feature type and property type names, may be null
	 * @param schemaFile
	 * @throws ClassCastException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public GMLAppSchemaReader(GMLVersion gmlVersion, Map<String, String> namespaceHints, File schemaFile)
			throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			MalformedURLException, UnsupportedEncodingException {
		this(gmlVersion, namespaceHints, getSchemaURLs(schemaFile));
	}

	/**
	 * Creates a new {@link GMLAppSchemaReader} from the given schema URL(s).
	 * @param gmlVersion gml version of the schema files, can be null (auto-detect GML
	 * version)
	 * @param namespaceHints optional hints (key: prefix, value: namespaces) for
	 * generating 'nice' qualified feature type and property type names, may be null
	 * @param schemaUrls
	 * @throws ClassCastException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public GMLAppSchemaReader(GMLVersion gmlVersion, Map<String, String> namespaceHints, String... schemaUrls)
			throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this(new GMLSchemaInfoSet(gmlVersion, schemaUrls), namespaceHints);
	}

	/**
	 * Creates a new {@link GMLAppSchemaReader} from the given <code>LSInput</code>s.
	 * @param gmlVersion gml version of the schema files, can be null (auto-detect GML
	 * version)
	 * @param namespaceHints optional hints (key: prefix, value: namespaces) for
	 * generating 'nice' qualified feature type and property type names, may be null
	 * @param inputs
	 * @throws ClassCastException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public GMLAppSchemaReader(GMLVersion gmlVersion, Map<String, String> namespaceHints, LSInput... inputs)
			throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this(new GMLSchemaInfoSet(gmlVersion, inputs), namespaceHints);
	}

	private GMLAppSchemaReader(final GMLSchemaInfoSet gmlSchema, final Map<String, String> namespaceHints)
			throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.gmlSchema = gmlSchema;
		for (Entry<String, String> nsToPrefix : gmlSchema.getNamespacePrefixes().entrySet()) {
			this.nsToPrefix.put(nsToPrefix.getKey(), nsToPrefix.getValue());
			this.prefixToNs.put(nsToPrefix.getValue(), nsToPrefix.getKey());
		}
		if (namespaceHints != null) {
			for (Entry<String, String> prefixToNs : namespaceHints.entrySet()) {
				nsToPrefix.put(prefixToNs.getValue(), prefixToNs.getKey());
				this.prefixToNs.put(prefixToNs.getKey(), prefixToNs.getValue());
			}
		}
		List<XSElementDeclaration> featureElementDecls = gmlSchema.getFeatureElementDeclarations(null, false);
		// feature element declarations
		for (XSElementDeclaration elementDecl : featureElementDecls) {
			QName ftName = createQName(elementDecl.getNamespace(), elementDecl.getName());
			ftNameToFtElement.put(ftName, elementDecl);
			XSElementDeclaration substitutionElement = elementDecl.getSubstitutionGroupAffiliation();
			if (substitutionElement != null) {
				QName substitutionElementName = createQName(substitutionElement.getNamespace(),
						substitutionElement.getName());
				ftNameToSubstitutionGroupName.put(ftName, substitutionElementName);
			}
		}
		// geometry element declarations
		List<XSElementDeclaration> geometryElementDecls = gmlSchema.getGeometryElementDeclarations(null, false);
		for (XSElementDeclaration elementDecl : geometryElementDecls) {
			QName elName = createQName(elementDecl.getNamespace(), elementDecl.getName());
			geometryNameToGeometryElement.put(elName, elementDecl);
			XSElementDeclaration substitutionElement = elementDecl.getSubstitutionGroupAffiliation();
			if (substitutionElement != null) {
				QName substitutionElementName = createQName(substitutionElement.getNamespace(),
						substitutionElement.getName());
				geometryNameToSubstitutionGroupName.put(elName, substitutionElementName);
			}
		}
		objectTypeFactory = new GmlObjectTypeFactory(gmlSchema, nsToPrefix);
	}

	private static String[] getSchemaURLs(File schemaFile) throws MalformedURLException, UnsupportedEncodingException {
		List<String> schemaUrls = new ArrayList<String>();
		if (!schemaFile.exists()) {
			throw new IllegalArgumentException("File/directory '" + schemaFile + "' does not exist.");
		}
		if (schemaFile.isDirectory()) {
			String[] inputFiles = schemaFile.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xsd");
				}
			});
			for (String file : inputFiles) {
				schemaUrls.add(new URL(schemaFile.toURI().toURL(), URLEncoder.encode(file, "UTF-8")).toExternalForm());
			}
		}
		else if (schemaFile.isFile()) {
			schemaUrls.add(schemaFile.toURI().toURL().toExternalForm());
		}
		else {
			throw new IllegalArgumentException("'" + schemaFile + "' is neither a file nor a directory.");
		}
		return schemaUrls.toArray(new String[schemaUrls.size()]);
	}

	public AppSchema extractAppSchema() {
		for (QName ftName : ftNameToFtElement.keySet()) {
			FeatureType ft = buildFeatureType(ftNameToFtElement.get(ftName));
			ftNameToFt.put(ftName, ft);
		}
		// resolveFtReferences();
		FeatureType[] fts = ftNameToFt.values().toArray(new FeatureType[ftNameToFt.size()]);

		Map<FeatureType, FeatureType> ftSubstitution = new HashMap<FeatureType, FeatureType>();
		for (QName ftName : ftNameToSubstitutionGroupName.keySet()) {
			QName substitutionFtName = ftNameToSubstitutionGroupName.get(ftName);
			if (substitutionFtName != null) {
				ftSubstitution.put(ftNameToFt.get(ftName), ftNameToFt.get(substitutionFtName));
			}
		}

		final List<GMLObjectType> genericGmlObjectTypes = new ArrayList<GMLObjectType>();
		for (final XSElementDeclaration elDecl : gmlSchema.getGeometryElementDeclarations(null, false)) {
			final GMLObjectType type = buildGenericObjectType(elDecl);
			genericGmlObjectTypes.add(type);
			typeNameToType.put(type.getName(), type);
		}

		Map<GMLObjectType, GMLObjectType> typeToSuperType = new HashMap<GMLObjectType, GMLObjectType>();
		for (QName ftName : geometryNameToSubstitutionGroupName.keySet()) {
			QName substitutionFtName = geometryNameToSubstitutionGroupName.get(ftName);
			if (substitutionFtName != null) {
				typeToSuperType.put(typeNameToType.get(ftName), typeNameToType.get(substitutionFtName));
			}
		}
		for (final XSElementDeclaration elDecl : gmlSchema.getTimeObjectElementDeclarations(null, false)) {
			final GMLObjectType type = buildGenericObjectType(elDecl);
			genericGmlObjectTypes.add(type);
			typeNameToType.put(type.getName(), type);
		}
		for (final XSElementDeclaration elDecl : gmlSchema.getTimeSliceElementDeclarations(null, false)) {
			final GMLObjectType type = buildGenericObjectType(elDecl);
			genericGmlObjectTypes.add(type);
			typeNameToType.put(type.getName(), type);
		}
		return new GenericAppSchema(fts, ftSubstitution, prefixToNs, gmlSchema, genericGmlObjectTypes, typeToSuperType);
	}

	private FeatureType buildFeatureType(XSElementDeclaration elDecl) {
		final GMLObjectType type = objectTypeFactory.build(elDecl);
		final List<XSElementDeclaration> fcDecls = gmlSchema.getFeatureCollectionElementDeclarations(null, false);
		if (fcDecls.contains(elDecl)) {
			return new GenericFeatureCollectionType(type.getName(), type.getPropertyDeclarations(),
					elDecl.getAbstract());
		}
		return new GenericFeatureType(type.getName(), type.getPropertyDeclarations(), elDecl.getAbstract());
	}

	private GMLObjectType buildGenericObjectType(final XSElementDeclaration elDecl) {
		return objectTypeFactory.build(elDecl);
	}

	private QName createQName(String namespace, String localPart) {
		String prefix = nsToPrefix.get(namespace);
		if (prefix == null) {
			prefix = generatePrefix();
			nsToPrefix.put(namespace, prefix);
		}
		return new QName(namespace, localPart, prefix);
	}

	private String generatePrefix() {
		return "app" + prefixIndex++;
	}

}
