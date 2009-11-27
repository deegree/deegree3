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
package org.deegree.geometry.gml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;

/**
 * Exports a Geometry bean (that belongs to GML 2.1.*) to a {@link XMLStreamWriter}.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GML21GeometryEncoder {

    private static final String GML21NS = "http://www.opengis.net/gml";

    private XMLStreamWriter writer;

    /**
     * @param writer
     */
    public GML21GeometryEncoder( XMLStreamWriter writer ) {
        this.writer = writer;
    }

    /**
     * @param geometry
     * @throws XMLStreamException
     */
    public void export( Geometry geometry )
                            throws XMLStreamException {

        if ( geometry instanceof Point ) {
            exportPoint( (Point) geometry );
        } else if ( geometry instanceof Polygon ) {
            exportPolygon( (Polygon) geometry );
        } else if ( geometry instanceof LinearRing ) {
            exportLinearRing( (LinearRing) geometry );
        } else if ( geometry instanceof LineString ) {
            exportLineString( (LineString) geometry );
        } else if ( geometry instanceof Envelope ) {
            exportEnvelope( (Envelope) geometry );
        } else if ( geometry instanceof MultiGeometry ) {
            exportMultiGeometry( (MultiGeometry) geometry );
        } else if ( geometry instanceof MultiPoint ) {
            exportMultiPoint( (MultiPoint) geometry );
        } else if ( geometry instanceof MultiLineString ) {
            exportMultiLineString( (MultiLineString) geometry );
        } else if ( geometry instanceof MultiPolygon ) {
            exportMultiPolygon( (MultiPolygon) geometry );
        }
    }

    /**
     * @param point
     * @throws XMLStreamException
     */
    public void exportPoint( Point point )
                            throws XMLStreamException {
        writer.writeStartElement( "gml", "Point", GML21NS );

        writer.writeAttribute( null, "gid", point.getId() );
        writer.writeAttribute( null, "srsName", point.getCoordinateSystem().getName() );

        writer.writeStartElement( "gml", "coord", GML21NS );
        switch ( point.getCoordinateDimension() ) {
        case 1:
            writer.writeStartElement( "gml", "X", GML21NS );
            writer.writeCharacters( String.valueOf( point.get0() ) );
            writer.writeEndElement();
            if ( point.getCoordinateDimension() == 1 ) {
                break;
            }
            //$FALL-THROUGH$
        case 2:
            writer.writeStartElement( "gml", "Y", GML21NS );
            writer.writeCharacters( String.valueOf( point.get1() ) );
            writer.writeEndElement();
            if ( point.getCoordinateDimension() == 2 ) {
                break;
            }
            //$FALL-THROUGH$
        case 3:
            writer.writeStartElement( "gml", "Z", GML21NS );
            writer.writeCharacters( String.valueOf( point.get2() ) );
            writer.writeEndElement();
        }
        writer.writeEndElement(); // </gml:coord>

        writer.writeEndElement(); // </gml:Point>
    }

    public void exportPolygon( Polygon polygon ) {

    }

    public void exportLinearRing( LinearRing linearRing ) {

    }

    public void exportLineString( LineString linearString ) {

    }

    public void exportEnvelope( Envelope envelope ) {

    }

    public void exportMultiGeometry( MultiGeometry multiGeometry ) {

    }

    public void exportMultiPoint( MultiPoint multiPoint ) {

    }

    public void exportMultiLineString( MultiLineString multiLineString ) {

    }

    public void exportMultiPolygon( MultiPolygon multiPolygon ) {

    }

}
