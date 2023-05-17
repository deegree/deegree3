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
package org.deegree.tools.binding;

import static org.deegree.tools.binding.FeatureClass.FIELD_NS;
import static org.deegree.tools.binding.FeatureClass.QNAME;
import static org.deegree.tools.binding.FeatureInstanceWriter.PROP;
import static org.deegree.tools.binding.ModelClass.SP;
import static org.deegree.tools.binding.ModelClass.SP2;
import static org.deegree.tools.binding.ModelClass.SP3;
import static org.deegree.tools.binding.ModelClass.arlist;
import static org.deegree.tools.binding.ModelClass.closeMethod;
import static org.deegree.tools.binding.ModelClass.linkedHashMap;
import static org.deegree.tools.binding.ModelClass.list;
import static org.deegree.tools.binding.ModelClass.map;
import static org.deegree.tools.binding.ModelClass.oif;
import static org.deegree.tools.binding.ModelClass.openMethod;
import static org.deegree.tools.binding.ModelClass.writeField;
import static org.deegree.tools.binding.RootFeature.FEAT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.lang.model.type.PrimitiveType;
import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.StringPair;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericAppSchema;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.StringOrRefPropertyType;
import org.deegree.feature.types.property.ValueRepresentation;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class FeatureTypeInstanceWriter {

	private static final Logger LOG = getLogger(FeatureTypeInstanceWriter.class);

	private final static String PTYPE = PropertyType.class.getSimpleName();

	private final static String GEOM_PTYPE = GeometryPropertyType.class.getSimpleName();

	private final static String GML = GMLVersion.class.getSimpleName();

	private final static String ASCHEMA = AppSchema.class.getSimpleName();

	private final static String FIELD_PROPS = "ALLOWED_PROPERTIES";

	private final static String FIELD_N2PROPS = "NAME_TO_PROPTYPE";

	private final static String FIELD_DEF_GEOM_PROP = "DEFAULT_GEOMETRY_PROPERTY";

	/**
	 * @param imports
	 */
	public static void addImports(Set<String> imports) {
		// featuretype implementation
		imports.add(FeatureType.class.getCanonicalName());
		imports.add(PropertyType.class.getCanonicalName());
		imports.add(GenericAppSchema.class.getCanonicalName());
		imports.add(GeometryPropertyType.class.getCanonicalName());
		imports.add(List.class.getCanonicalName());
		imports.add(GMLVersion.class.getCanonicalName());
		imports.add(LinkedHashMap.class.getCanonicalName());
		imports.add(Collection.class.getCanonicalName());

	}

	protected static void writeFields(Writer out, FeatureType ft) throws IOException {
		AppSchema schema = ft.getSchema();
		List<PropertyType> propertyDeclarations = schema.getNewPropertyDecls(ft);
		// write the feature type name as a field;

		if (!propertyDeclarations.isEmpty()) {
			Field field = new Field(FIELD_N2PROPS, map(QNAME, PTYPE), linkedHashMap(QNAME, PTYPE, true), true, true);
			writeField(out, field);
			field = new Field(FIELD_PROPS, list(QNAME), arlist(QNAME, true, propertyDeclarations.size()), true, true);
			writeField(out, field);

			field = new Field(FIELD_DEF_GEOM_PROP, GeometryPropertyType.class.getSimpleName(), null, true, true);
			writeField(out, field);
			// write static block of property types.
			out.write(SP + "static {\n");
			Iterator<PropertyType> it = propertyDeclarations.iterator();
			Set<String> currentVars = new HashSet<String>();
			while (it.hasNext()) {
				PropertyType prop = it.next();
				if (prop != null) {
					QName name = prop.getName();
					out.write(SP2);
					if (!currentVars.contains("np")) {
						out.write(QNAME + " ");
						currentVars.add("np");
					}
					out.write("np = new " + QNAME + "( " + FIELD_NS + ", \"" + name.getLocalPart() + "\" );\n");
					out.write(SP2 + FIELD_PROPS + ".add( np );\n");

					String propTypeDecl = createPropertyTypeInstance(out, prop, currentVars, 0);
					if (prop instanceof GeometryPropertyType && !currentVars.contains(FIELD_DEF_GEOM_PROP)) {
						out.write(SP2 + FIELD_DEF_GEOM_PROP + " = " + propTypeDecl + ";\n");
						out.write(SP2 + FIELD_N2PROPS + ".put( np, " + FIELD_DEF_GEOM_PROP + " );\n");
						currentVars.add(FIELD_DEF_GEOM_PROP);
					}
					else {
						// np is the qname of the property
						out.write(SP2 + FIELD_N2PROPS + ".put( np, " + propTypeDecl + " );\n");
					}

				}
			}
			if (!currentVars.contains(FIELD_DEF_GEOM_PROP)) {
				out.write(SP2 + FIELD_DEF_GEOM_PROP + " = null;\n");
			}
			out.write(SP + "}\n");
		}
	}

	private static String createPropertyTypeInstance(Writer out, PropertyType prop, Set<String> currentVars, int level)
			throws IOException {
		int min = prop.getMinOccurs();
		int max = prop.getMaxOccurs();
		List<PropertyType> subs = Arrays.asList(prop.getSubstitutions());
		StringBuilder propTypeInstance = new StringBuilder("new ");
		propTypeInstance.append(prop.getClass().getSimpleName()).append("( ");
		String name = "np";
		if (level > 0) {
			QName pN = prop.getName();
			name = "new " + QNAME + "( \"" + pN.getNamespaceURI() + "\", \"" + pN.getLocalPart() + "\" )";
		}
		propTypeInstance.append(name).append(", ");
		propTypeInstance.append(min).append(", ");
		propTypeInstance.append(max).append(", ");
		String subVar = createSubstitutionList(out, prop, subs, currentVars, level);
		if (prop instanceof GeometryPropertyType) {
			createNewPropertyType((GeometryPropertyType) prop, subVar, currentVars, propTypeInstance, out);
		}
		else if (prop instanceof FeaturePropertyType) {
			createNewPropertyType((FeaturePropertyType) prop, subVar, currentVars, propTypeInstance, out);
		}
		else if (prop instanceof CustomPropertyType) {
			createNewPropertyType((CustomPropertyType) prop, subVar, currentVars, propTypeInstance, out);
		}
		else if (prop instanceof SimplePropertyType) {
			createNewPropertyType((SimplePropertyType) prop, subVar, currentVars, propTypeInstance, out);
		}
		else if ((prop instanceof StringOrRefPropertyType) || (prop instanceof MeasurePropertyType)
				|| (prop instanceof CodePropertyType) || (prop instanceof EnvelopePropertyType)) {
			// new StringOrRefPropertyType( name, minOccurs, maxOccurs, isAbstract,
			// substitutions );
			// new MeasurePropertyType( name, minOccurs, maxOccurs, isAbstract,
			// substitutions )
			// new EnvelopePropertyType( name, minOccurs, maxOccurs, isAbstract,
			// substitutions )
			// new CodePropertyType( name, minOccurs, maxOccurs, isAbstract, substitutions
			// );
			propTypeInstance.append(prop.isAbstract()).append(", ");
			// add substitution;
			propTypeInstance.append(subVar);
		}
		else {
			LOG.warn("Ignore import for property type: " + prop.getName());
		}
		propTypeInstance.append(")");
		return propTypeInstance.toString();
	}

	/**
	 * @param subs
	 * @param currentVars
	 * @throws IOException
	 */
	private static String createSubstitutionList(Writer out, PropertyType parent, List<PropertyType> subs,
			Set<String> currentVars, int level) throws IOException {
		String varName = "subs" + level;
		if (subs.size() == 1) {
			return "null";
		}
		out.write(SP2);
		if (!currentVars.contains(varName)) {
			out.write(list(PTYPE));
			currentVars.add(varName);
		}
		out.write(varName + " = ");
		if (subs.size() == 1) {
			out.write("null;\n");
		}
		else {
			out.write(arlist(PTYPE, true, subs.size()) + ";\n");
			List<String> subInstances = new ArrayList<String>();
			for (PropertyType pt : subs) {
				if (!parent.getName().equals(pt.getName())) {
					subInstances.add(createPropertyTypeInstance(out, pt, currentVars, level + 1));
				}
			}
			for (String s : subInstances) {
				out.write(SP2 + varName + ".add( " + s + " );\n");
			}

		}
		return varName;
	}

	/**
	 * new SimplePropertyType( name, minOccurs, maxOccurs, type, isAbstract, substitutions
	 * )
	 * @param prop
	 * @param subVar
	 * @param currentVars
	 * @param result
	 * @param out
	 */
	private static void createNewPropertyType(SimplePropertyType prop, String subVar, Set<String> currentVars,
			StringBuilder result, Writer out) {
		// TODO Auto-generated method stub

		String pt = PrimitiveType.class.getSimpleName() + "." + prop.getPrimitiveType().getBaseType().name();
		result.append(pt).append(", ");
		result.append(prop.isAbstract()).append(", ");
		result.append(subVar);

	}

	/**
	 * new CustomPropertyType( name, minOccurs, maxOccurs, xsdType, isAbstract,
	 * substitutions )
	 * @param prop
	 * @param subVar
	 * @param currentVars
	 * @param result
	 * @param out
	 */
	private static void createNewPropertyType(CustomPropertyType prop, String subVar, Set<String> currentVars,
			StringBuilder result, Writer out) {
		// TODO Auto-generated method stub
		result.append("null").append("/*todo create the xsd type here*/, ");
		result.append(prop.isAbstract()).append(", ");
		result.append(subVar);
	}

	/**
	 * new FeaturePropertyType( name, minOccurs, maxOccurs, valueFtName, isAbstract,
	 * substitutions, representation )
	 * @param prop
	 * @param subVar
	 * @param abst
	 * @param currentVars
	 * @param result
	 * @param out
	 */
	private static void createNewPropertyType(FeaturePropertyType prop, String subVar, Set<String> currentVars,
			StringBuilder result, Writer out) {

		QName ftName = prop.getFTName();
		if (ftName == null) {
			ftName = prop.getName();
		}
		result.append("new ").append(QNAME).append("(\"").append(ftName.getNamespaceURI());
		result.append("\", \"").append(ftName.getLocalPart()).append("\"), ");
		result.append(prop.isAbstract()).append(", ");
		result.append(subVar).append(", ");
		result.append(getValueRep(prop.getAllowedRepresentation()));

	}

	/**
	 * return new GeometryPropertyType( name, min, max, allowedGeometryTypes,
	 * coordinateDimension, abs, subsVar, allowedRepresentation );
	 * @param prop
	 * @param subsVar
	 * @param abs
	 * @param result
	 * @param out
	 * @throws IOException
	 */
	private static void createNewPropertyType(GeometryPropertyType prop, String subsVar, Set<String> currentVars,
			StringBuilder result, Writer out) throws IOException {
		Set<GeometryType> allowedGeometryTypes = prop.getAllowedGeometryTypes();
		String geomType = null;
		if (allowedGeometryTypes.size() > 1) {
			// create tmp set and add it to the geometry property

			String vName = "geomSet";
			out.write(SP2);
			if (!currentVars.contains(vName)) {
				out.write(ModelClass.newSet(GeometryType.class.getSimpleName(), vName));
				currentVars.add(vName);
			}
			else {
				out.write(vName + " = " + ModelClass.hashSet(GeometryType.class.getSimpleName(), true) + ";\n");
			}
			for (GeometryType gt : allowedGeometryTypes) {
				out.write(SP2 + vName + ".add( " + GeometryType.class.getSimpleName() + "." + gt.name() + ");\n");
			}
			geomType = vName;
		}
		else {
			geomType = GeometryType.class.getSimpleName() + "." + prop.getGeometryType().name();
		}
		String cDim = CoordinateDimension.class.getSimpleName() + "." + prop.getCoordinateDimension().name();
		String vRep = getValueRep(prop.getAllowedRepresentation());
		result.append(geomType).append(", ");
		result.append(cDim).append(", ");
		result.append(prop.isAbstract()).append(", ");
		// add substitution;
		result.append(subsVar).append(", ");
		result.append(vRep);
		// return "new GeometryPropertyType( np,"+ min+","+ max+",
	}

	private static String getValueRep(ValueRepresentation vr) {
		return ValueRepresentation.class.getSimpleName() + "." + vr.name();
	}

	/**
	 * @param out
	 * @param featClasses
	 * @throws IOException
	 */
	protected static void writeFeatureTypeMethods(Writer out, HashMap<QName, FeatureClass> featClasses)
			throws IOException {

		List<StringPair> params = new ArrayList<StringPair>();

		out.write(SP + "//---------------------------------------\n");
		out.write(SP + "// Methods defined by " + FeatureType.class.getCanonicalName() + " interface.\n");
		out.write(SP + "//---------------------------------------\n");
		// openMethod( out, "public", QNAME, "getName", params, null, true );
		// out.write( SP2 + "return " + getFeatureType().getName() + ";\n" );
		// closeMethod( out );

		// public PropertyType getPropertyDeclaration( QName propName );
		params.add(new StringPair(QNAME, "propName"));
		openMethod(out, "public", PTYPE, "getPropertyDeclaration", params, null, true);
		out.write(SP2 + "return " + FIELD_N2PROPS + ".get( propName );\n");
		closeMethod(out);
		params.clear();

		// public PropertyType getPropertyDeclaration( QName propName, GMLVersion version
		// );
		params.add(new StringPair(QNAME, "propName"));
		params.add(new StringPair(GML, "version"));
		openMethod(out, "public", PTYPE, "getPropertyDeclaration", params, null, true);
		out.write(SP2 + oif(true, "pt == null"));
		out.write(SP3 + "pt = " + FIELD_N2PROPS + ".get( propName );\n");
		out.write(SP2 + "}\n");
		out.write(SP2 + "return pt;\n");
		closeMethod(out);
		params.clear();

		// public List<PropertyType> getPropertyDeclarations();
		openMethod(out, "public", "List<" + PTYPE + ">", "getPropertyDeclarations", params, null, true);
		out.write(SP2 + "return new " + arlist(PTYPE) + "( " + FIELD_N2PROPS + ".values() );\n");
		closeMethod(out);

		// public List<PropertyType> getPropertyDeclarations( GMLVersion version );
		params.add(new StringPair(GML, "version"));
		openMethod(out, "public", "List<" + PTYPE + ">", "getPropertyDeclarations", params, null, true);
		out.write(SP2 + ModelClass.newList(PTYPE, "result"));
		out.write(SP2 + oif(true, "stdProps.size() > 0"));
		out.write(SP3 + "result.addAll( stdProps );\n");
		out.write(SP2 + "}\n");
		out.write(SP2 + oif(true, FIELD_N2PROPS + ".size() > 0"));
		out.write(SP3 + "result.addAll( " + FIELD_N2PROPS + ".values() );\n");
		out.write(SP2 + "}\n");
		out.write(SP2 + "return result;\n");
		closeMethod(out);
		params.clear();

		// public GeometryPropertyType getDefaultGeometryPropertyDeclaration();
		openMethod(out, "public", GEOM_PTYPE, "getDefaultGeometryPropertyDeclaration", params, null, true);
		out.write(SP2 + "return " + FIELD_DEF_GEOM_PROP + ";\n");
		closeMethod(out);

		// public boolean isAbstract();
		openMethod(out, "public", "boolean", "isAbstract", params, null, true);
		out.write(SP2 + "return false;\n");
		closeMethod(out);

		// public Feature newFeature( String fid, List<Property> props, GMLVersion version
		// );
		params.add(new StringPair("String", "fid"));
		params.add(new StringPair("List<" + PROP + ">", "props"));
		params.add(new StringPair(GML, "version"));
		openMethod(out, "public", FEAT, "newFeature", params, null, true);
		out.write(SP2 + "return null;\n");
		closeMethod(out);
		params.clear();

		// public ApplicationSchema getSchema();
		openMethod(out, "public", ASCHEMA, "getSchema", params, null, true);
		out.write(SP2 + "return null;\n");
		closeMethod(out);

		// public void setSchema( ApplicationSchema applicationSchema );
		params.add(new StringPair(ASCHEMA, "applicationSchema"));
		openMethod(out, "public", "void", "setSchema", params, null, true);
		out.write(SP2 + "//assign value;\n");
		closeMethod(out);
		params.clear();

	}

}
