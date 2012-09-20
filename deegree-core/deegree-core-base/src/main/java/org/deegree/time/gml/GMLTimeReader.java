//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.time.gml;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.gml.reference.GmlDocumentIdContext;
import org.deegree.time.TimeObject;
import org.deegree.time.complex.TimeComplex;
import org.deegree.time.complex.TimeTopologyComplex;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.deegree.time.primitive.TimePrimitive;

/**
 * Parser for temporal and temporal-related constructs from the GML 3 specification series (3.0/3.1/3.2).
 * <p>
 * Supports the following temporal elements:
 * <p>
 * <ul>
 * <li><code>TimeInstant (not implemented yet)</code></li>
 * <li><code>TimePeriod (not implemented yet)</code></li>
 * <li><code>TimeEdge (not implemented yet)</code></li>
 * <li><code>TimeNode (not implemented yet)</code></li>
 * <li><code>TimeTopologyComplex (not implemented yet)</code></li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLTimeReader {

    // local names of all concrete elements substitutable for "gml:AbstractTimeObject"
    private static final Set<String> timeElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:AbstractTimePrimitive"
    private static final Set<String> primitiveElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:AbstractTimeGeometricPrimitive"
    private static final Set<String> geometricPrimitiveElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:AbstractTimeTopologyPrimitive"
    private static final Set<String> geometricTopologyElements = new HashSet<String>();

    // local names of all concrete elements substitutable for "gml:AbstractTimeComplex"
    private static final Set<String> complexElements = new HashSet<String>();

    static {
        // concrete substitutions for "gml:AbstractTimeGeometricPrimitive"
        geometricPrimitiveElements.add( "TimeInstant" );
        geometricPrimitiveElements.add( "TimePeriod" );

        // concrete substitutions for "gml:AbstractTimeTopologyPrimitive"
        geometricTopologyElements.add( "TimeEdge" );
        geometricTopologyElements.add( "TimeNode" );

        // concrete substitutions for "gml:AbstractTimePrimitive"
        primitiveElements.addAll( geometricPrimitiveElements );
        primitiveElements.addAll( geometricTopologyElements );

        // concrete substitutions for "gml:AbstractTimeComplex"
        complexElements.add( "TimeTopologyComplex" );

        // concrete substitutions for "gml:AbstractTimeObject"
        timeElements.addAll( primitiveElements );
        timeElements.addAll( complexElements );
    }

    private final GmlDocumentIdContext idContext;

    public GMLTimeReader( GmlDocumentIdContext idContext ) {
        this.idContext = idContext;
    }

    public boolean isTimeObject( QName name ) {
        return timeElements.contains( name.getLocalPart() );
    }

    /**
     * Returns the object representation for the given <code>gml:AbstractTimeObject</code> element event that the cursor
     * of the given <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:AbstractTimeObject&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/gml:AbstractTimeObject&gt;)</li>
     * </ul>
     * <p>
     * GML 3.2.1 specifies the following elements to be <b>directly</b> substitutable for
     * <code>gml:AbstractTimeObject</code>:
     * <ul>
     * <li><code>gml:AbstractTimePrimitive</code></li>
     * <li><code>gml:AbstractTimeComplex</code></li>
     * </ul>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:AbstractTimeObject&gt;), points at
     *            the corresponding <code>END_ELEMENT</code> event (&lt;/gml:AbstractTimeObject&gt;) afterwards
     * @return corresponding {@link TimeObject} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public TimeObject readTimeObject( XMLStreamReader xmlStream ) {
        String localName = xmlStream.getLocalName();
        if ( primitiveElements.contains( localName ) ) {
            return readTimePrimitive( xmlStream );
        } else if ( complexElements.contains( localName ) ) {
            return readTimeComplex( xmlStream );
        }
        String msg = "Invalid GML time object: '" + xmlStream.getName()
                     + "' does not denote a GML time object element.";
        throw new XMLParsingException( xmlStream, msg );
    }

    public TimePrimitive readTimePrimitive( XMLStreamReader xmlStream ) {
        String localName = xmlStream.getLocalName();
        if ( geometricPrimitiveElements.contains( localName ) ) {
            return readTimeGeometricPrimitive( xmlStream );
        } else if ( geometricTopologyElements.contains( localName ) ) {
            return readTimeTopologyPrimitive( xmlStream );
        }
        String msg = "Invalid GML time object: '" + xmlStream.getName()
                     + "' does not denote a GML time primitive element.";
        throw new XMLParsingException( xmlStream, msg );
    }

    public TimeGeometricPrimitive readTimeGeometricPrimitive( XMLStreamReader xmlStream ) {
        String localName = xmlStream.getLocalName();
        if ( "TimeInstant".equals( localName ) ) {
            return readTimeInstant( xmlStream );
        } else if ( "TimePeriod".equals( localName ) ) {
            return readTimePeriod( xmlStream );
        }
        String msg = "Invalid GML time object: '" + xmlStream.getName()
                     + "' does not denote a GML time geometric primitive element.";
        throw new XMLParsingException( xmlStream, msg );
    }

    public TimeInstant readTimeInstant( XMLStreamReader xmlStream ) {
        // TODO Auto-generated method stub
        return null;
    }

    public TimePeriod readTimePeriod( XMLStreamReader xmlStream ) {
        // TODO Auto-generated method stub
        return null;
    }

    public TimePrimitive readTimeTopologyPrimitive( XMLStreamReader xmlStream ) {
        String localName = xmlStream.getLocalName();
        if ( "TimeEdge".equals( localName ) ) {
            return readTimeEdge( xmlStream );
        } else if ( "TimeNode".equals( localName ) ) {
            return readTimeNode( xmlStream );
        }
        String msg = "Invalid GML time object: '" + xmlStream.getName()
                     + "' does not denote a GML time geometric primitive element.";
        throw new XMLParsingException( xmlStream, msg );
    }

    public TimePrimitive readTimeEdge( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException( "Parsing of gml:TimeEdge is not implemented yet." );
    }

    public TimePrimitive readTimeNode( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException( "Parsing of gml:TimeNode is not implemented yet." );
    }

    public TimeComplex readTimeComplex( XMLStreamReader xmlStream ) {
        String localName = xmlStream.getLocalName();
        if ( "TimeTopologyComplex".equals( localName ) ) {
            return readTimeTopologyComplex( xmlStream );
        }
        String msg = "Invalid GML time object: '" + xmlStream.getName()
                     + "' does not denote a GML time complex element.";
        throw new XMLParsingException( xmlStream, msg );
    }

    public TimeTopologyComplex readTimeTopologyComplex( XMLStreamReader xmlStream ) {
        throw new UnsupportedOperationException( "Parsing of gml:TimeTopologyComplex is not implemented yet." );
    }
}
