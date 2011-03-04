//$HeadURL$
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
package org.deegree.filter.xml;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.filter.function.FunctionManager;
import org.deegree.filter.function.FunctionProvider;

/**
 * Writes (currently static) XML <code>ogc:Filter_Capabilities</code> documents that describes the capabilities of
 * deegree's filter implementation.
 * 
 * TODO what about functions TODO what about restrictions of backends
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FilterCapabilitiesExporter {

    /**
     * Exports an <code>ogc:Filter_Capabilities</code> element (1.0.0) that describes the capabilities of deegree's
     * filter implementation.
     * 
     * @param writer
     *            used to write the XML
     * @throws XMLStreamException
     *             if the exporting fails
     */
    public static void export100( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.setPrefix( "ogc", OGCNS );

        writer.writeStartElement( OGCNS, "Filter_Capabilities" );

        exportSpatialCapabilities100( writer );
        exportScalarCapabilities100( writer );

        writer.writeEndElement();
    }

    /**
     * Exports an <code>ogc:Filter_Capabilities</code> element (1.1.0) that describes the capabilities of deegree's
     * filter implementation.
     * 
     * @param writer
     *            used to write the XML
     * @throws XMLStreamException
     *             if the exporting fails
     */
    public static void export110( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.setPrefix( "ogc", OGCNS );

        writer.writeStartElement( OGCNS, "Filter_Capabilities" );

        exportSpatialCapabilities110( writer );
        exportScalarCapabilities110( writer );
        exportIdCapabilities110( writer );

        writer.writeEndElement();
    }

    private static void exportIdCapabilities110( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.writeStartElement( OGCNS, "Id_Capabilities" );

        // TODO: check what EID means (nothing found in spec/schema)
        writer.writeEmptyElement( OGCNS, "EID" );
        writer.writeEmptyElement( OGCNS, "FID" );

        writer.writeEndElement();
    }

    private static void exportScalarCapabilities100( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.writeStartElement( OGCNS, "Scalar_Capabilities" );

        writer.writeEmptyElement( OGCNS, "Logical_Operators" );

        writer.writeStartElement( OGCNS, "Comparison_Operators" );
        writer.writeEmptyElement( OGCNS, "Simple_Comparisons" );
        writer.writeEmptyElement( OGCNS, "Like" );
        writer.writeEmptyElement( OGCNS, "Between" );
        writer.writeEmptyElement( OGCNS, "NullCheck" );
        writer.writeEndElement();

        writer.writeStartElement( OGCNS, "Arithmetic_Operators" );
        writer.writeEmptyElement( OGCNS, "Simple_Arithmetic" );

        writer.writeStartElement( OGCNS, "Functions" );
        writer.writeStartElement( OGCNS, "Function_Names" );
        Map<String, FunctionProvider> functions = FunctionManager.getFunctionProviders();
        SortedSet<String> functionNames = new TreeSet<String>( functions.keySet() );
        for ( String functionName : functionNames ) {
            FunctionProvider provider = functions.get( functionName );
            writer.writeStartElement( OGCNS, "Function_Name" );
            writer.writeAttribute( "nArgs", "" + provider.getArgCount() );
            writer.writeCharacters( provider.getName() );
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeEndElement();

        writer.writeEndElement();
    }

    private static void exportScalarCapabilities110( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.writeStartElement( OGCNS, "Scalar_Capabilities" );

        writer.writeEmptyElement( OGCNS, "LogicalOperators" );

        writer.writeStartElement( OGCNS, "ComparisonOperators" );
        writeElement( writer, OGCNS, "ComparisonOperator", "LessThan" );
        writeElement( writer, OGCNS, "ComparisonOperator", "GreaterThan" );
        writeElement( writer, OGCNS, "ComparisonOperator", "LessThanEqualTo" );
        writeElement( writer, OGCNS, "ComparisonOperator", "GreaterThanEqualTo" );
        writeElement( writer, OGCNS, "ComparisonOperator", "EqualTo" );
        writeElement( writer, OGCNS, "ComparisonOperator", "NotEqualTo" );
        writeElement( writer, OGCNS, "ComparisonOperator", "Like" );
        writeElement( writer, OGCNS, "ComparisonOperator", "Between" );
        writeElement( writer, OGCNS, "ComparisonOperator", "NullCheck" );
        writer.writeEndElement();

        writer.writeStartElement( OGCNS, "ArithmeticOperators" );
        writer.writeEmptyElement( OGCNS, "SimpleArithmetic" );

        writer.writeStartElement( OGCNS, "Functions" );
        writer.writeStartElement( OGCNS, "FunctionNames" );
        Map<String, FunctionProvider> functions = FunctionManager.getFunctionProviders();
        SortedSet<String> functionNames = new TreeSet<String>( functions.keySet() );
        for ( String functionName : functionNames ) {
            FunctionProvider provider = functions.get( functionName );
            writer.writeStartElement( OGCNS, "FunctionName" );
            writer.writeAttribute( "nArgs", "" + provider.getArgCount() );
            writer.writeCharacters( provider.getName() );
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndElement();
        
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private static void exportSpatialCapabilities100( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.writeStartElement( OGCNS, "Spatial_Capabilities" );
        writer.writeStartElement( OGCNS, "Spatial_Operators" );

        writer.writeEmptyElement( OGCNS, "BBOX" );
        writer.writeEmptyElement( OGCNS, "Equals" );
        writer.writeEmptyElement( OGCNS, "Disjoint" );
        // yes, in 1.0.0, it's really 'Intersect', not 'Intersects'
        writer.writeEmptyElement( OGCNS, "Intersect" );
        writer.writeEmptyElement( OGCNS, "Touches" );
        writer.writeEmptyElement( OGCNS, "Crosses" );
        writer.writeEmptyElement( OGCNS, "Within" );
        writer.writeEmptyElement( OGCNS, "Contains" );
        writer.writeEmptyElement( OGCNS, "Overlaps" );
        writer.writeEmptyElement( OGCNS, "Beyond" );
        writer.writeEmptyElement( OGCNS, "DWithin" );

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportSpatialCapabilities110( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.writeStartElement( OGCNS, "Spatial_Capabilities" );

        writer.writeStartElement( OGCNS, "GeometryOperands" );
        writer.setPrefix( "gml", GMLNS );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Envelope" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Point" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:LineString" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Polygon" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:ArcByCenterPoint" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:CircleByCenterPoint" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Arc" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Circle" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:ArcByBulge" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Bezier" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Clothoid" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:CubicSpline" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Geodesic" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:OffsetCurve" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Triangle" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:PolyhedralSurface" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:TriangulatedSurface" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Tin" );
        writeElement( writer, OGCNS, "GeometryOperand", "gml:Solid" );
        writer.writeEndElement();

        writer.writeStartElement( OGCNS, "SpatialOperators" );
        exportSpatialOperator110( writer, "BBOX" );
        exportSpatialOperator110( writer, "Equals" );
        exportSpatialOperator110( writer, "Disjoint" );
        exportSpatialOperator110( writer, "Intersects" );
        exportSpatialOperator110( writer, "Touches" );
        exportSpatialOperator110( writer, "Crosses" );
        exportSpatialOperator110( writer, "Within" );
        exportSpatialOperator110( writer, "Contains" );
        exportSpatialOperator110( writer, "Overlaps" );
        exportSpatialOperator110( writer, "Beyond" );
        exportSpatialOperator110( writer, "DWithin" );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private static void exportSpatialOperator110( XMLStreamWriter writer, String operatorName )
                            throws XMLStreamException {
        writer.writeStartElement( OGCNS, "SpatialOperator" );
        writer.writeAttribute( "name", operatorName );
        writer.writeEndElement();
    }
}