/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.filter.xml;

import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.FES_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.OGC_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.filter.function.FunctionManager;
import org.deegree.filter.function.FunctionProvider;
import org.deegree.filter.function.ParameterType;
import org.deegree.gml.GMLVersion;

/**
 * Writes <code>Filter_Capabilities</code> documents that describe the capabilities of
 * deegree's filter implementation.
 *
 * TODO what about backend restrictions
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class FilterCapabilitiesExporter {

	/**
	 * Exports an <code>ogc:Filter_Capabilities</code> element (1.0.0) that describes the
	 * capabilities of deegree's filter implementation.
	 * @param writer used to write the XML
	 * @throws XMLStreamException if the exporting fails
	 */
	public static void export100(XMLStreamWriter writer) throws XMLStreamException {

		writer.setPrefix(OGC_PREFIX, OGCNS);
		writer.writeStartElement(OGCNS, "Filter_Capabilities");
		if (writer.getPrefix(OGCNS) == null) {
			writer.writeNamespace(OGC_PREFIX, OGCNS);
		}

		exportSpatialCapabilities100(writer);
		exportScalarCapabilities100(writer);

		writer.writeEndElement();
	}

	/**
	 * Exports an <code>ogc:Filter_Capabilities</code> element (1.1.0) that describes the
	 * capabilities of deegree's filter implementation.
	 * @param writer used to write the XML
	 * @throws XMLStreamException if the exporting fails
	 */
	public static void export110(XMLStreamWriter writer) throws XMLStreamException {

		writer.setPrefix(OGC_PREFIX, OGCNS);
		writer.writeStartElement(OGCNS, "Filter_Capabilities");
		if (writer.getPrefix(OGCNS) == null) {
			writer.writeNamespace(OGC_PREFIX, OGCNS);
		}

		exportSpatialCapabilities110(writer);
		exportScalarCapabilities110(writer);
		exportIdCapabilities110(writer);

		writer.writeEndElement();
	}

	/**
	 * Exports a <code>fes:Filter_Capabilities</code> element (2.0.0) that describes the
	 * capabilities of deegree's filter implementation.
	 * @param writer used to write the XML, must not be <code>null</code>
	 * @throws XMLStreamException if the exporting fails
	 */
	public static void export200(XMLStreamWriter writer) throws XMLStreamException {

		writer.setPrefix(FES_PREFIX, FES_20_NS);
		writer.writeStartElement(FES_20_NS, "Filter_Capabilities");
		if (writer.getPrefix(FES_20_NS) == null) {
			writer.writeNamespace(FES_PREFIX, FES_20_NS);
		}
		if (writer.getPrefix(OWS_11_NS) == null) {
			writer.writeNamespace("ows", OWS_11_NS);
		}

		exportConformance200(writer);
		exportIdCapabilities200(writer);
		exportScalarCapabilities200(writer);
		exportSpatialCapabilities200(writer);
		exportTemporalCapabilities200(writer);
		exportFunctions200(writer);
		exportExtendedCapabilities200(writer);

		writer.writeEndElement();
	}

	private static void exportConformance200(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "Conformance");
		exportConstraint200(writer, "ImplementsQuery", true);
		exportConstraint200(writer, "ImplementsAdHocQuery", true);
		exportConstraint200(writer, "ImplementsFunctions", true);
		exportConstraint200(writer, "ImplementsResourceId", true);
		exportConstraint200(writer, "ImplementsMinStandardFilter", true);
		exportConstraint200(writer, "ImplementsStandardFilter", true);
		exportConstraint200(writer, "ImplementsMinSpatialFilter", true);
		exportConstraint200(writer, "ImplementsSpatialFilter", true);
		exportConstraint200(writer, "ImplementsMinTemporalFilter", true);
		exportConstraint200(writer, "ImplementsTemporalFilter", true);
		exportConstraint200(writer, "ImplementsVersionNav", false);
		exportConstraint200(writer, "ImplementsSorting", true);
		exportConstraint200(writer, "ImplementsExtendedOperators", false);
		exportConstraint200(writer, "ImplementsMinimumXPath", true);
		exportConstraint200(writer, "ImplementsSchemaElementFunc", false);
		writer.writeEndElement();

	}

	private static void exportConstraint200(XMLStreamWriter writer, String conformanceClass, boolean implemented)
			throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "Constraint");
		writer.writeAttribute("name", conformanceClass);
		writer.writeEmptyElement(OWS_11_NS, "NoValues");
		writer.writeStartElement(OWS_11_NS, "DefaultValue");
		if (implemented) {
			writer.writeCharacters("TRUE");
		}
		else {
			writer.writeCharacters("FALSE");
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}

	private static void exportIdCapabilities200(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "Id_Capabilities");
		writer.writeEmptyElement(FES_20_NS, "ResourceIdentifier");
		writer.writeAttribute("name", "fes:ResourceId");
		writer.writeEndElement();
	}

	private static void exportScalarCapabilities200(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "Scalar_Capabilities");
		writer.writeEmptyElement(FES_20_NS, "LogicalOperators");
		writer.writeStartElement(FES_20_NS, "ComparisonOperators");
		exportComparisonOperator(writer, "PropertyIsEqualTo");
		exportComparisonOperator(writer, "PropertyIsNotEqualTo");
		exportComparisonOperator(writer, "PropertyIsLessThan");
		exportComparisonOperator(writer, "PropertyIsGreaterThan");
		exportComparisonOperator(writer, "PropertyIsLessThanOrEqualTo");
		exportComparisonOperator(writer, "PropertyIsGreaterThanOrEqualTo");
		exportComparisonOperator(writer, "PropertyIsLike");
		exportComparisonOperator(writer, "PropertyIsNull");
		exportComparisonOperator(writer, "PropertyIsNil");
		exportComparisonOperator(writer, "PropertyIsBetween");
		writer.writeEndElement();
		writer.writeEndElement();
	}

	private static void exportComparisonOperator(XMLStreamWriter writer, String name) throws XMLStreamException {
		writer.writeEmptyElement(FES_20_NS, "ComparisonOperator");
		writer.writeAttribute("name", name);
	}

	private static void exportSpatialCapabilities200(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "Spatial_Capabilities");
		writer.writeStartElement(FES_20_NS, "GeometryOperands");
		writer.writeNamespace("gml", CommonNamespaces.GMLNS);
		writer.writeNamespace("gml32", CommonNamespaces.GML3_2_NS);
		exportGeometryOperand(writer, "gml:Box");
		exportGeometryOperand(writer, "gml:Envelope");
		exportGeometryOperand(writer, "gml:Point");
		exportGeometryOperand(writer, "gml:LineString");
		exportGeometryOperand(writer, "gml:Curve");
		exportGeometryOperand(writer, "gml:Polygon");
		exportGeometryOperand(writer, "gml:Surface");
		exportGeometryOperand(writer, "gml:MultiPoint");
		exportGeometryOperand(writer, "gml:MultiLineString");
		exportGeometryOperand(writer, "gml:MultiCurve");
		exportGeometryOperand(writer, "gml:MultiPolygon");
		exportGeometryOperand(writer, "gml:MultiSurface");
		exportGeometryOperand(writer, "gml:CompositeCurve");
		exportGeometryOperand(writer, "gml:CompositeSurface");
		exportGeometryOperand(writer, "gml32:Envelope");
		exportGeometryOperand(writer, "gml32:Point");
		exportGeometryOperand(writer, "gml32:LineString");
		exportGeometryOperand(writer, "gml32:Curve");
		exportGeometryOperand(writer, "gml32:Polygon");
		exportGeometryOperand(writer, "gml32:Surface");
		exportGeometryOperand(writer, "gml32:MultiPoint");
		exportGeometryOperand(writer, "gml32:MultiLineString");
		exportGeometryOperand(writer, "gml32:MultiCurve");
		exportGeometryOperand(writer, "gml32:MultiPolygon");
		exportGeometryOperand(writer, "gml32:MultiSurface");
		exportGeometryOperand(writer, "gml32:CompositeCurve");
		exportGeometryOperand(writer, "gml32:CompositeSurface");
		// TODO what about Solids?
		writer.writeEndElement();
		writer.writeStartElement(FES_20_NS, "SpatialOperators");
		exportSpatialOperator(writer, "BBOX");
		exportSpatialOperator(writer, "Equals");
		exportSpatialOperator(writer, "Disjoint");
		exportSpatialOperator(writer, "Intersects");
		exportSpatialOperator(writer, "Touches");
		exportSpatialOperator(writer, "Crosses");
		exportSpatialOperator(writer, "Within");
		exportSpatialOperator(writer, "Contains");
		exportSpatialOperator(writer, "Overlaps");
		exportSpatialOperator(writer, "Beyond");
		exportSpatialOperator(writer, "DWithin");
		writer.writeEndElement();
		writer.writeEndElement();
	}

	private static void exportGeometryOperand(XMLStreamWriter writer, String name) throws XMLStreamException {
		writer.writeEmptyElement(FES_20_NS, "GeometryOperand");
		writer.writeAttribute("name", name);
	}

	private static void exportSpatialOperator(XMLStreamWriter writer, String name) throws XMLStreamException {
		writer.writeEmptyElement(FES_20_NS, "SpatialOperator");
		writer.writeAttribute("name", name);
	}

	private static void exportTemporalCapabilities200(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(FES_20_NS, "Temporal_Capabilities");
		writer.writeStartElement(FES_20_NS, "TemporalOperands");
		writer.writeNamespace("gml", CommonNamespaces.GMLNS);
		writer.writeNamespace("gml32", CommonNamespaces.GML3_2_NS);
		exportTemporalOperand(writer, "gml:TimeInstant");
		exportTemporalOperand(writer, "gml:TimePeriod");
		exportTemporalOperand(writer, "gml32:TimeInstant");
		exportTemporalOperand(writer, "gml32:TimePeriod");
		writer.writeEndElement();
		writer.writeStartElement(FES_20_NS, "TemporalOperators");
		exportTemporalOperator(writer, "After");
		exportTemporalOperator(writer, "Before");
		// exportTemporalOperator( writer, "Begins" );
		// exportTemporalOperator( writer, "BegunBy" );
		// exportTemporalOperator( writer, "TContains" );
		exportTemporalOperator(writer, "During");
		exportTemporalOperator(writer, "TEquals");
		// exportTemporalOperator( writer, "TOverlaps" );
		// exportTemporalOperator( writer, "Meets" );
		// exportTemporalOperator( writer, "OverlappedBy" );
		// exportTemporalOperator( writer, "MetBy" );
		// exportTemporalOperator( writer, "Ends" );
		// exportTemporalOperator( writer, "EndedBy" );
		writer.writeEndElement();
		writer.writeEndElement();
	}

	private static void exportTemporalOperand(XMLStreamWriter writer, String name) throws XMLStreamException {
		writer.writeEmptyElement(FES_20_NS, "TemporalOperand");
		writer.writeAttribute("name", name);
	}

	private static void exportTemporalOperator(XMLStreamWriter writer, String name) throws XMLStreamException {
		writer.writeEmptyElement(FES_20_NS, "TemporalOperator");
		writer.writeAttribute("name", name);
	}

	private static void exportFunctions200(XMLStreamWriter writer) throws XMLStreamException {

		Map<String, FunctionProvider> functions = FunctionManager.getFunctionProviders();
		if (!functions.isEmpty()) {
			writer.writeStartElement(FES_20_NS, "Functions");
			SortedSet<String> functionNames = new TreeSet<String>(functions.keySet());
			for (String functionName : functionNames) {
				FunctionProvider provider = functions.get(functionName);
				boolean containsGMLTypes = false;
				if (provider.getReturnType().getType(GML_31).getNamespaceURI().equals(GMLNS)) {
					containsGMLTypes = true;
				}
				else {
					for (ParameterType arg : provider.getArgs()) {
						if (arg.getType(GML_31).getNamespaceURI().equals(GMLNS)) {
							containsGMLTypes = true;
							break;
						}
					}
				}
				if (containsGMLTypes) {
					exportFunction200(writer, provider, GML_31);
				}
				exportFunction200(writer, provider, GML_32);
			}
			writer.writeEndElement();
		}
	}

	private static void exportFunction200(XMLStreamWriter writer, FunctionProvider function, GMLVersion version)
			throws XMLStreamException {

		writer.writeStartElement(FES_20_NS, "Function");
		writer.writeAttribute("name", function.getName());
		writer.writeStartElement(FES_20_NS, "Returns");
		QName typeName = function.getReturnType().getType(version);
		writer.writeNamespace(typeName.getPrefix(), typeName.getNamespaceURI());
		writer.writeCharacters(typeName.getPrefix() + ":" + typeName.getLocalPart());
		writer.writeEndElement();
		if (!function.getArgs().isEmpty()) {
			writer.writeStartElement(FES_20_NS, "Arguments");
			int i = 1;
			for (ParameterType inputType : function.getArgs()) {
				writer.writeStartElement(FES_20_NS, "Argument");
				writer.writeAttribute("name", "arg" + (i++));
				writer.writeStartElement(FES_20_NS, "Type");
				typeName = inputType.getType(version);
				writer.writeNamespace(typeName.getPrefix(), typeName.getNamespaceURI());
				writer.writeCharacters(typeName.getPrefix() + ":" + typeName.getLocalPart());
				writer.writeEndElement();
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}

	private static void exportExtendedCapabilities200(XMLStreamWriter writer) {
		// TODO Auto-generated method stub
	}

	private static void exportIdCapabilities110(XMLStreamWriter writer) throws XMLStreamException {

		writer.writeStartElement(OGCNS, "Id_Capabilities");

		// TODO: check what EID means (nothing found in spec/schema)
		writer.writeEmptyElement(OGCNS, "EID");
		writer.writeEmptyElement(OGCNS, "FID");

		writer.writeEndElement();
	}

	private static void exportScalarCapabilities100(XMLStreamWriter writer) throws XMLStreamException {

		writer.writeStartElement(OGCNS, "Scalar_Capabilities");

		writer.writeEmptyElement(OGCNS, "Logical_Operators");

		writer.writeStartElement(OGCNS, "Comparison_Operators");
		writer.writeEmptyElement(OGCNS, "Simple_Comparisons");
		writer.writeEmptyElement(OGCNS, "Like");
		writer.writeEmptyElement(OGCNS, "Between");
		writer.writeEmptyElement(OGCNS, "NullCheck");
		writer.writeEndElement();

		writer.writeStartElement(OGCNS, "Arithmetic_Operators");
		writer.writeEmptyElement(OGCNS, "Simple_Arithmetic");

		writer.writeStartElement(OGCNS, "Functions");
		writer.writeStartElement(OGCNS, "Function_Names");
		Map<String, FunctionProvider> functions = FunctionManager.getFunctionProviders();
		SortedSet<String> functionNames = new TreeSet<String>(functions.keySet());
		for (String functionName : functionNames) {
			FunctionProvider provider = functions.get(functionName);
			writer.writeStartElement(OGCNS, "Function_Name");
			writer.writeAttribute("nArgs", "" + provider.getArgs().size());
			writer.writeCharacters(provider.getName());
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private static void exportScalarCapabilities110(XMLStreamWriter writer) throws XMLStreamException {

		writer.writeStartElement(OGCNS, "Scalar_Capabilities");

		writer.writeEmptyElement(OGCNS, "LogicalOperators");

		writer.writeStartElement(OGCNS, "ComparisonOperators");
		writeElement(writer, OGCNS, "ComparisonOperator", "LessThan");
		writeElement(writer, OGCNS, "ComparisonOperator", "GreaterThan");
		writeElement(writer, OGCNS, "ComparisonOperator", "LessThanEqualTo");
		writeElement(writer, OGCNS, "ComparisonOperator", "GreaterThanEqualTo");
		writeElement(writer, OGCNS, "ComparisonOperator", "EqualTo");
		writeElement(writer, OGCNS, "ComparisonOperator", "NotEqualTo");
		writeElement(writer, OGCNS, "ComparisonOperator", "Like");
		writeElement(writer, OGCNS, "ComparisonOperator", "Between");
		writeElement(writer, OGCNS, "ComparisonOperator", "NullCheck");
		writer.writeEndElement();

		writer.writeStartElement(OGCNS, "ArithmeticOperators");
		writer.writeEmptyElement(OGCNS, "SimpleArithmetic");

		writer.writeStartElement(OGCNS, "Functions");
		writer.writeStartElement(OGCNS, "FunctionNames");
		Map<String, FunctionProvider> functions = FunctionManager.getFunctionProviders();
		SortedSet<String> functionNames = new TreeSet<String>(functions.keySet());
		for (String functionName : functionNames) {
			FunctionProvider provider = functions.get(functionName);
			writer.writeStartElement(OGCNS, "FunctionName");
			writer.writeAttribute("nArgs", "" + provider.getArgs().size());
			writer.writeCharacters(provider.getName());
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndElement();

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private static void exportSpatialCapabilities100(XMLStreamWriter writer) throws XMLStreamException {

		writer.writeStartElement(OGCNS, "Spatial_Capabilities");
		writer.writeStartElement(OGCNS, "Spatial_Operators");

		writer.writeEmptyElement(OGCNS, "BBOX");
		writer.writeEmptyElement(OGCNS, "Equals");
		writer.writeEmptyElement(OGCNS, "Disjoint");
		// yes, in 1.0.0, it's really 'Intersect', not 'Intersects'
		writer.writeEmptyElement(OGCNS, "Intersect");
		writer.writeEmptyElement(OGCNS, "Touches");
		writer.writeEmptyElement(OGCNS, "Crosses");
		writer.writeEmptyElement(OGCNS, "Within");
		writer.writeEmptyElement(OGCNS, "Contains");
		writer.writeEmptyElement(OGCNS, "Overlaps");
		writer.writeEmptyElement(OGCNS, "Beyond");
		writer.writeEmptyElement(OGCNS, "DWithin");

		writer.writeEndElement();
		writer.writeEndElement();
	}

	private static void exportSpatialCapabilities110(XMLStreamWriter writer) throws XMLStreamException {

		writer.writeStartElement(OGCNS, "Spatial_Capabilities");

		writer.writeStartElement(OGCNS, "GeometryOperands");
		writer.writeNamespace("gml", GMLNS);
		writeElement(writer, OGCNS, "GeometryOperand", "gml:Envelope");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:Point");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:LineString");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:Polygon");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:ArcByCenterPoint");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:CircleByCenterPoint");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:Arc");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:Circle");
		writeElement(writer, OGCNS, "GeometryOperand", "gml:ArcByBulge");
		// check and reactivate
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:Bezier" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:Clothoid" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:CubicSpline" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:Geodesic" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:OffsetCurve" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:Triangle" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:PolyhedralSurface" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:TriangulatedSurface" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:Tin" );
		// writeElement( writer, OGCNS, "GeometryOperand", "gml:Solid" );
		writer.writeEndElement();

		writer.writeStartElement(OGCNS, "SpatialOperators");
		exportSpatialOperator110(writer, "BBOX");
		exportSpatialOperator110(writer, "Equals");
		exportSpatialOperator110(writer, "Disjoint");
		exportSpatialOperator110(writer, "Intersects");
		exportSpatialOperator110(writer, "Touches");
		exportSpatialOperator110(writer, "Crosses");
		exportSpatialOperator110(writer, "Within");
		exportSpatialOperator110(writer, "Contains");
		exportSpatialOperator110(writer, "Overlaps");
		exportSpatialOperator110(writer, "Beyond");
		exportSpatialOperator110(writer, "DWithin");
		writer.writeEndElement();

		writer.writeEndElement();
	}

	private static void exportSpatialOperator110(XMLStreamWriter writer, String operatorName)
			throws XMLStreamException {
		writer.writeStartElement(OGCNS, "SpatialOperator");
		writer.writeAttribute("name", operatorName);
		writer.writeEndElement();
	}

}
