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

import static org.deegree.tools.binding.FeatureClass.QNAME;
import static org.deegree.tools.binding.ModelClass.SP;
import static org.deegree.tools.binding.ModelClass.SP2;
import static org.deegree.tools.binding.ModelClass.SP3;
import static org.deegree.tools.binding.ModelClass.SP4;
import static org.deegree.tools.binding.ModelClass.closeMethod;
import static org.deegree.tools.binding.ModelClass.hashMap;
import static org.deegree.tools.binding.ModelClass.list;
import static org.deegree.tools.binding.ModelClass.map;
import static org.deegree.tools.binding.ModelClass.oif;
import static org.deegree.tools.binding.ModelClass.openMethod;
import static org.deegree.tools.binding.ModelClass.writeField;
import static org.deegree.tools.binding.RootFeature.FTYPE;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.utils.StringPair;
import org.deegree.feature.AbstractFeature;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLVersion;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class FeatureInstanceWriter {

	final static String PROP = Property.class.getSimpleName();

	private final static String GML = GMLVersion.class.getSimpleName();

	private final static String ENV = Envelope.class.getSimpleName();

	private final static String TON = TypedObjectNode.class.getSimpleName();

	private final static String ID = "fid";

	private final static String PROP_MAP = "properties";

	protected static void addImports(Set<String> imports) {
		// feature implementation
		imports.add(AbstractFeature.class.getCanonicalName());
		imports.add(Property.class.getCanonicalName());
		imports.add(QName.class.getCanonicalName());
		imports.add(TypedObjectNode.class.getCanonicalName());
		imports.add(Map.class.getCanonicalName());
		imports.add(HashMap.class.getCanonicalName());
	}

	protected static void writeFields(Writer out) throws IOException {
		// id
		Field f = new Field(ID, "String");
		writeField(out, f);

		f = new Field(PROP_MAP, map(QNAME, list(TON)), hashMap(QNAME, list(TON), true), false, true);
		writeField(out, f);
	}

	/**
	 * @param out
	 * @param featClasses
	 * @throws IOException
	 */
	protected static void writeFeatureMethods(Writer out, HashMap<QName, FeatureClass> featClasses) throws IOException {
		List<StringPair> params = new ArrayList<StringPair>();

		out.write(SP + "//---------------------------------------\n");
		out.write(SP + "// Methods not implemented by " + AbstractFeature.class.getCanonicalName() + ".\n");
		out.write(SP + "//---------------------------------------\n");

		handleIds(out);

		// public QName getName();
		// implemented by all feature classes

		// public FeatureType getType();
		openMethod(out, "public", FTYPE, "getType", params, null, true);
		out.write(SP2 + "return this;\n");
		closeMethod(out);

		// public Property[] getProperties();
		openMethod(out, "public", PROP + "[]", "getProperties", params, null, true);
		out.write(SP2 + "return null;\n");
		closeMethod(out);

		// implemented by abstract feature.
		// public Property[] getProperties( GMLVersion version );
		// public Property[] getProperties( QName propName, GMLVersion version );
		// public Property getProperty( QName propName, GMLVersion version );
		// public Envelope getEnvelope();

		// public Property[] getProperties( QName propName );
		params.add(new StringPair(QNAME, "propName"));
		openMethod(out, "public", PROP + "[]", "getProperties", params, null, true);
		out.write(SP2 + "return null;\n");
		closeMethod(out);

		// public Property getProperty( QName propName );
		openMethod(out, "public", PROP, "getProperty", params, null, true);
		out.write(SP2 + "return null;\n");
		closeMethod(out);
		params.clear();

		// public Property[] getGeometryProperties();
		openMethod(out, "public", PROP + "[]", "getGeometryProperties", params, null, true);
		out.write(SP2 + "return null;\n");
		closeMethod(out);

		// done by abstract feature.

		handleSetPropertyValues(out);
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	private static void handleSetPropertyValues(Writer out) throws IOException {
		setPropVal(out);
		// done in abstract feature.
		// public void setPropertyValue( QName propName, int occurrence, TypedObjectNode
		// value, GMLVersion version );

		// public void setProperties( List<Property> props ) throws
		// IllegalArgumentException;
		setProps(out);

		setPropsVers(out);
		// ;

	}

	/**
	 * Creates the method
	 * <code>public void setProperties( List<Property> props, GMLVersion version ) throws IllegalArgumentException</code>
	 * @param out
	 * @throws IOException
	 */
	private static void setPropsVers(Writer out) throws IOException {
		List<StringPair> params = new ArrayList<StringPair>();
		params.add(new StringPair("List<" + PROP + ">", "props"));
		params.add(new StringPair(GML, "version"));

		List<String> throwables = new ArrayList<String>(1);
		throwables.add(IllegalArgumentException.class.getSimpleName());
		openMethod(out, "public", "void", "setProperties", params, throwables, true);
		out.write(SP2 + oif(true, " props != null", "!props.isEmpty()"));
		out.write(SP3 + "for ( " + PROP + " prop : props ) {\n");
		out.write(SP4 + "setPropertyValue( prop.getName(), -1, prop.getValue(), version);\n");
		out.write(SP3 + "}\n");
		out.write(SP2 + "}\n");
		closeMethod(out);

	}

	/**
	 * Creates the method
	 * <code>public void setPropertyValue( QName propName, int occurrence, TypedObjectNode value );</code>
	 * @param out
	 * @throws IOException
	 */
	private static void setPropVal(Writer out) throws IOException {
		List<StringPair> params = new ArrayList<StringPair>();
		params.add(new StringPair(QNAME, "propName"));
		params.add(new StringPair("int", "occurrence"));
		params.add(new StringPair(TON, "value"));
		openMethod(out, "public", "void", "setPropertyValue", params, null, true);
		out.write(SP2 + oif(true, "value != null", "propName != null"));
		String vals = list(TON) + " values";
		out.write(SP3 + vals + " = " + PROP_MAP + ".get( propName );\n");
		out.write(SP3 + oif(true, "values == null"));
		out.write(SP4 + "values = " + ModelClass.arlist(TON, true, -1) + ";\n");
		out.write(SP4 + "properties.put( propName, values );\n");
		out.write(SP3 + "}\n");
		out.write(SP3 + oif(true, "occurrence != -1"));
		out.write(SP4 + "values.set( occurrence, value );\n");
		out.write(SP3 + "} else {\n");
		out.write(SP4 + "values.add( value );\n");
		out.write(SP3 + "}\n");
		out.write(SP2 + "}\n");
		closeMethod(out);
	}

	/**
	 * Creates the method
	 * <code>public void setProperties( List<Property> props ) throws IllegalArgumentException</code>
	 * @param out
	 * @throws IOException
	 */
	private static void setProps(Writer out) throws IOException {

		List<StringPair> params = new ArrayList<StringPair>();
		params.add(new StringPair("List<" + PROP + ">", "props"));
		List<String> throwables = new ArrayList<String>(1);
		throwables.add(IllegalArgumentException.class.getSimpleName());
		openMethod(out, "public", "void", "setProperties", params, throwables, true);
		out.write(SP2 + oif(true, " props != null", "!props.isEmpty()"));
		out.write(SP3 + "for ( " + PROP + " prop : props ) {\n");
		out.write(SP4 + "setPropertyValue( prop.getName(), -1, prop.getValue() );\n");
		out.write(SP3 + "}\n");
		out.write(SP2 + "}\n");
		closeMethod(out);
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	private static void handleIds(Writer out) throws IOException {
		// public String getId();
		openMethod(out, "public", "String", "getId", null, null, true);
		out.write(SP2 + "return " + ID + ";\n");
		closeMethod(out);

		// public void setId( String id );
		List<StringPair> params = new ArrayList<StringPair>();
		params.add(new StringPair("String", "id"));
		openMethod(out, "public", "void", "setId", params, null, true);
		out.write(SP2 + ID + " = id;\n");
		closeMethod(out);
	}

	private static void writeGetId(Writer out) {

	}

}
