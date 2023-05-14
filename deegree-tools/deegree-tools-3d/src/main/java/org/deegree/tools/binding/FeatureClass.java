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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.uom.Measure;
import org.deegree.feature.Feature;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
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
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class FeatureClass extends ModelClass {

	private static final Logger LOG = getLogger(FeatureClass.class);

	private final static String D_PACKAGE = "org.deegree.tools.binding.citygml.";

	private final static String PACKAGE_DIR = "src/main/java/org/deegree/tools/binding/citygml/";

	protected final static String FIELD_NS = "NS";

	protected final static String QNAME = QName.class.getSimpleName();

	private final static String FIELD_FT_NAME = "FT_NAME";

	private final FeatureType ft;

	private FeatureClass baseType;

	private final List<PropertyType> propertyDeclarations;

	public FeatureClass(FeatureType ft, FeatureClass parent) {
		super(ft.getName(), D_PACKAGE, PACKAGE_DIR, null, false, ft.isAbstract());
		this.ft = ft;
		this.baseType = parent;
		AppSchema schema = ft.getSchema();
		propertyDeclarations = schema.getNewPropertyDecls(ft);
	}

	/**
	 * @return the ft
	 */
	public final FeatureType getFeatureType() {
		return ft;
	}

	/**
	 * @return the baseType
	 */
	public final FeatureClass getBaseType() {
		return baseType;
	}

	/**
	 * Find the common base type of this geometry type and another geometry type
	 * @param other to get the base type for.
	 * @return the common base
	 */
	public FeatureClass findCommonBaseType(FeatureClass other) {
		if (other == null || this.baseType == null) {
			return null;
		}
		if (this.equals(other)) {
			return this;
		}
		FeatureClass current = this;
		boolean found = false;
		while (!found && current != null) {
			FeatureClass check = other;
			while (!found && check != null) {
				found = check.equals(current);
				check = check.baseType;
			}
			if (!found) {
				current = null;
			}
		}
		return current;
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof FeatureClass) {
			final FeatureClass that = (FeatureClass) other;
			return super.equals(other) && this.baseType != null ? this.baseType.equals(that.baseType)
					: that.baseType == null;
		}
		return false;
	}

	@Override
	public int hashCode() {
		long result = 32452843;
		result = result * 37 + super.hashCode();
		if (this.baseType != null) {
			result = result * 37 + baseType.hashCode();
		}
		return (int) (result >>> 32) ^ (int) result;
	}

	@Override
	public String toString() {
		return getClassName() + (baseType == null ? "" : "(parent: " + this.baseType.getClassName() + ").");
	}

	/**
	 * @param featClasses
	 * @return a list of imports needed for this feature class.
	 */
	@Override
	public List<String> getImports(Map<QName, FeatureClass> featClasses) {
		Set<String> imports = new HashSet<String>();
		if (baseType != null) {
			if (!baseType.getPackageName().equals(this.getPackageName())) {
				imports.add(baseType.getClassQName());
			}
		}
		FeatureTypeInstanceWriter.addImports(imports);
		FeatureInstanceWriter.addImports(imports);
		// qname is always needed.
		imports.add(QName.class.getCanonicalName());
		if (!propertyDeclarations.isEmpty()) {
			// we'll need the lists
			imports.add(List.class.getCanonicalName());
			imports.add(ArrayList.class.getCanonicalName());
			for (PropertyType pd : propertyDeclarations) {
				if (pd != null) {
					Field field = createFieldFromProperty(pd, featClasses, imports);
					if (field != null) {
						if (!field.getCanonicalTypeName().startsWith("java.lang")) {
							imports.add(field.getCanonicalTypeName());
						}
						addField(field);
					}
				}
			}
		}
		return new ArrayList<String>(imports);
	}

	/**
	 * @param pd
	 * @param featClasses
	 * @param imports
	 * @return
	 */
	private Field createFieldFromProperty(PropertyType pd, Map<QName, FeatureClass> featClasses, Set<String> imports) {
		imports.add(pd.getClass().getCanonicalName());
		if (pd instanceof GeometryPropertyType) {
			// add the geometry type for the featuretype imports.
			imports.add(GeometryType.class.getCanonicalName());
			imports.add(CoordinateDimension.class.getCanonicalName());
			imports.add(ValueRepresentation.class.getCanonicalName());
			return getFromProperty((GeometryPropertyType) pd);
		}
		else if (pd instanceof FeaturePropertyType) {
			imports.add(ValueRepresentation.class.getCanonicalName());
			return getFromProperty((FeaturePropertyType) pd, featClasses);
		}
		else if (pd instanceof CodePropertyType) {
			return getFromProperty((CodePropertyType) pd);
		}
		else if (pd instanceof CustomPropertyType) {
			return getFromProperty((CustomPropertyType) pd, featClasses, imports);
		}
		else if (pd instanceof EnvelopePropertyType) {
			return getFromProperty((EnvelopePropertyType) pd);
		}
		else if (pd instanceof MeasurePropertyType) {
			return getFromProperty((MeasurePropertyType) pd);
		}
		else if (pd instanceof SimplePropertyType) {
			imports.add(BaseType.class.getCanonicalName());
			return getFromProperty((SimplePropertyType) pd);
		}
		else if (pd instanceof StringOrRefPropertyType) {
			return getFromProperty((StringOrRefPropertyType) pd);
		}
		else {
			LOG.warn("Ignore import for property type: " + pd.getName());
		}
		return null;
	}

	/**
	 * @param pd
	 * @param imports
	 * @return
	 */
	private Field getFromProperty(StringOrRefPropertyType pd) {
		String fieldName = createFieldName(createBetterMethodName(pd.getName().getLocalPart()));
		String fieldType = StringOrRef.class.getCanonicalName();
		return new Field(fieldName, fieldType, pd.isAbstract(), pd.getName());
	}

	/**
	 * @param pd
	 * @param imports
	 * @return
	 */
	private Field getFromProperty(SimplePropertyType pd) {
		String fieldName = createFieldName(createBetterMethodName(pd.getName().getLocalPart()));
		final PrimitiveType primitiveType = pd.getPrimitiveType();
		String fieldType = primitiveType.getBaseType().getValueClass().getCanonicalName();

		return new Field(fieldName, fieldType, pd.isAbstract(), pd.getName());
	}

	/**
	 * @param pd
	 * @param imports
	 * @return
	 */
	private Field getFromProperty(MeasurePropertyType pd) {
		String fieldName = createFieldName(createBetterMethodName(pd.getName().getLocalPart()));
		String fieldType = Measure.class.getCanonicalName();
		return new Field(fieldName, fieldType, pd.isAbstract(), pd.getName());
	}

	/**
	 * @param pd
	 * @param imports
	 * @return
	 */
	private Field getFromProperty(EnvelopePropertyType pd) {
		String fieldName = createFieldName(createBetterMethodName(pd.getName().getLocalPart()));
		String fieldType = Envelope.class.getCanonicalName();
		return new Field(fieldName, fieldType, pd.isAbstract(), pd.getName());
	}

	/**
	 * @param pd
	 * @param imports
	 * @param imports
	 * @return
	 */
	private Field getFromProperty(CustomPropertyType pd, Map<QName, FeatureClass> featClasses, Set<String> imports) {
		String fieldName = createFieldName(createBetterMethodName(pd.getName().getLocalPart()));
		String fieldType = "java.lang.Object";
		if (pd.isAbstract()) {
			Set<Field> possibleSubstitutes = new HashSet<Field>();
			PropertyType[] substitutions = pd.getSubstitutions();
			if (substitutions != null) {
				for (PropertyType p : substitutions) {
					if (!p.getName().equals(pd.getName())) {
						Field f = createFieldFromProperty(p, featClasses, imports);
						if (f != null) {
							possibleSubstitutes.add(f);
						}
						else {
							LOG.warn("Could not create a field type for the substitution property: " + p.getName());
						}
					}
				}
			}
			return new Field(fieldName, fieldType, pd.isAbstract(), possibleSubstitutes, pd.getName());
		}

		return new Field(fieldName, fieldType, pd.isAbstract(), pd.getName());
	}

	/**
	 * @param pd
	 * @param imports
	 * @return
	 */
	private Field getFromProperty(CodePropertyType pd) {
		String fieldName = createFieldName(createBetterMethodName(pd.getName().getLocalPart()));
		String fieldType = CodeType.class.getCanonicalName();
		return new Field(fieldName, fieldType, pd.isAbstract(), pd.getName());
	}

	/**
	 * @param pd
	 * @param imports
	 * @param featClasses
	 * @return
	 */
	private Field getFromProperty(FeaturePropertyType pd, Map<QName, FeatureClass> featClasses) {
		QName ft = pd.getFTName();
		if (ft != null) {
			FeatureClass fc = featClasses.get(ft);
			if (fc != null) {
				String fieldName = createFieldName(createBetterMethodName(fc.getClassName()));
				return new Field(fieldName, fc.getClassQName(), pd.isAbstract(), pd.getName());
			}
			LOG.error("Could not find a feature class for the feature type: " + ft);
		}
		else {
			if (new QName("http://www.opengis.net/gml", "featureMember").equals(pd.getName())
					|| new QName("http://www.opengis.net/gml", "featureMembers").equals(pd.getName())) {
				String fieldName = "members";
				if (!super.hasField(fieldName)) {
					String fieldType = Feature.class.getCanonicalName();
					return new Field(fieldName, fieldType, true, pd.getName());
				}
			}
			else {
				LOG.error("Could not get feature type name for feature property type: " + pd);
			}
		}
		return null;
	}

	/**
	 * @param pd
	 * @param imports
	 * @return
	 */
	private Field getFromProperty(GeometryPropertyType pd) {
		GeometryType type = (pd).getGeometryType();
		String fieldName = createFieldName(createBetterMethodName(pd.getName().getLocalPart()));
		String fieldType = type.getJavaType().getCanonicalName();
		return new Field(fieldName, fieldType, pd.isAbstract(), pd.getName());
	}

	/**
	 * @param out
	 */
	@Override
	public void writeClassDoc(Writer out) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("/**\n");
		sb.append(" * Accepts a CityGML element (").append(ft.getName()).append(")\n");
		sb.append(" * and creates a WPVS representation from it.\n");
		sb.append(" */\n");
		out.write(sb.toString());

	}

	@Override
	public void writeClassStart(Writer out) throws IOException {
		StringBuilder sb = new StringBuilder("public ");
		if (isAbstract()) {
			sb.append("abstract ");
		}
		if (isInterface()) {
			sb.append("interface");
		}
		else {
			sb.append("class ");
		}
		sb.append(getClassName());
		if (baseType != null) {
			if (baseType.isInterface() && !this.isInterface()) {
				sb.append(" implements ").append(baseType.getClassName());
			}
			else {
				sb.append(" extends ").append(baseType.getClassName());
			}
		}
		sb.append(" {\n");
		out.write(sb.toString());
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	@Override
	public void writeFields(Writer out) throws IOException {
		Field field = new Field(FIELD_NS, "String", "\"" + ft.getName().getNamespaceURI() + "\"", true, true);
		writeField(out, field);
		String fVal = "new QName( " + FIELD_NS + ", \"" + ft.getName().getLocalPart() + "\" )";
		field = new Field(FIELD_FT_NAME, QName.class.getCanonicalName(), fVal, true, true);
		writeField(out, field);

		FeatureTypeInstanceWriter.writeFields(out, ft);
		FeatureInstanceWriter.writeFields(out);

		super.writeFields(out);

	}

	/**
	 * @param out
	 */
	@Override
	public void writeMethods(Writer out, HashMap<QName, FeatureClass> featClasses) throws IOException {

		super.writeMethods(out, featClasses);
		openMethod(out, "public", QNAME, "getName", null, null, true);
		out.write(SP2 + "return " + FIELD_FT_NAME + ";\n");
		closeMethod(out);

	}

	/**
	 * @param out
	 */
	private void writeConstructor(Writer out) {

	}

}
