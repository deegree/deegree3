//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/controller/ows/OWSException.java $
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
package org.deegree.protocol.ows;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.configuration.BoundingBoxType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;

/**
 * Supplies some basic exports methods for deegree/commons to ows mappings.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 *
 */
public class OWSCommonXMLAdapter extends XMLAdapter {

    /**
     * ows namespace without version
     */
    public static final String OWS_NS = "http://www.opengis.net/ows";

    /**
     * ows 1.1 version
     */
    public static final String OWS110_NS = "http://www.opengis.net/ows/1.1";

    /**
     * normal xml namespace
     */
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    /**
     * the ows prefix
     */
    public static final String OWS_PREFIX = "ows";

    /**
     * the ows 1.1.0 prefix
     */
    public static final String OWS110_PREFIX = "ows110";

    /**
     * the xml prefix
     */
    public static final String XML_PREFIX = "xml";

    private static final GeometryFactory geomFac = new GeometryFactory();

    static {
        // add to common namespaces from xml adapter
        nsContext.addNamespace( OWS_PREFIX, OWS_NS );
        nsContext.addNamespace( OWS110_PREFIX, OWS110_NS );
        nsContext.addNamespace( XML_PREFIX, XML_NS );
    }

    /**
     * Parses the given element of type <code>ows:BoundingBoxType</code>.
     *
     * @param boundingBoxDataElement
     *            element of type <code>ows:BoundingBoxType</code>
     * @param defaultCRS
     *            default CRS to use if no crs attribute is specified
     * @return corresponding <code>Envelope</code> object
     * @throws UnknownCRSException
     * @throws XMLParsingException
     *             if a syntactical or semantical error has been encountered in the element's contents
     */
    public Envelope parseBoundingBoxType( OMElement boundingBoxDataElement, CRS defaultCRS )
                            throws UnknownCRSException {

        // "ows:LowerCorner" element (minOccurs="1", maxOccurs="1")
        double[] lowerCorner = parseDoubleList( getRequiredElement( boundingBoxDataElement,
                                                                    new XPath( "ows110:LowerCorner", nsContext ) ) );

        // "ows:UpperCorner" element (minOccurs="1", maxOccurs="1")
        double[] upperCorner = parseDoubleList( getRequiredElement( boundingBoxDataElement,
                                                                    new XPath( "ows110:UpperCorner", nsContext ) ) );

        // "crs" attribute (optional)
        CRS crs = defaultCRS;
        String crsName = boundingBoxDataElement.getAttributeValue( new QName( "crs" ) );
        if ( crsName == null ) {
            crs = new CRS( crsName );
        }

        // "dimensions" attribute (optional)
        // int dimensions = getNodeAsInt( boundingBoxDataElement, new XPath( "@dimensions", nsContext ), -1 );

        return geomFac.createEnvelope( lowerCorner, upperCorner, crs );
    }

    private double[] parseDoubleList( OMElement positionElement )
                            throws XMLParsingException {
        String s = positionElement.getText();
        // don't use String.split(regex) here (speed)
        StringTokenizer st = new StringTokenizer( s );
        List<String> tokens = new ArrayList<String>();
        while ( st.hasMoreTokens() ) {
            tokens.add( st.nextToken() );
        }
        double[] doubles = new double[tokens.size()];
        for ( int i = 0; i < doubles.length; i++ ) {
            try {
                doubles[i] = Double.parseDouble( tokens.get( i ) );
            } catch ( NumberFormatException e ) {
                String msg = "Value '" + tokens.get( i ) + "' cannot be parsed as a double.";
                throw new XMLParsingException( this, positionElement, msg );
            }
        }
        return doubles;
    }

    /**
     * Exports an {@link Envelope} as a <code>ows:BoundingBoxType</code>.
     *
     * @param writer
     * @param bbox
     *            envelope to be exported
     * @throws XMLStreamException
     */
    public static void exportBoundingBoxType( XMLStreamWriter writer, Envelope bbox )
                            throws XMLStreamException {

        // "crs" attribute (optional)
        if ( bbox.getCoordinateSystem() != null ) {
            writer.writeAttribute( "crs", bbox.getCoordinateSystem().getName());
        }

        // "dimensions" attribute (optional)
        writer.writeAttribute( "dimensions", "" + bbox.getCoordinateDimension() );

        // "ows:LowerCorner" element (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS110_NS, "LowerCorner" );
        exportPositionType( writer, bbox.getMin() );
        writer.writeEndElement();

        // "ows:UpperCorner" element (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS110_NS, "UpperCorner" );
        exportPositionType( writer, bbox.getMax() );
        writer.writeEndElement();
    }

    /**
     * Exports an {@link BoundingBoxType} as an <code>ows:BoundingBoxType</code>. Coordinates will be separated with a
     * space.
     *
     * @param writer
     * @param bbox
     *            to be exported
     * @throws XMLStreamException
     */
    public static void exportBoundingBoxType110( XMLStreamWriter writer, BoundingBoxType bbox )
                            throws XMLStreamException {

        if ( bbox != null ) {
            exportBoundingBoxType( writer, bbox, OWS110_NS );
        }
    }

    /**
     * @param writer
     * @param bbox
     * @param owsNS
     * @throws XMLStreamException
     */
    private static void exportBoundingBoxType( XMLStreamWriter writer, BoundingBoxType bbox, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "BoundingBox" );
        // "crs" attribute (optional)
        if ( bbox.getCrs() != null ) {
            writer.writeAttribute( "crs", bbox.getCrs() );
        }

        // "dimensions" attribute (optional)
        writer.writeAttribute( "dimensions", "" + bbox.getDimensions() );

        // "ows:LowerCorner" element (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( owsNS, "LowerCorner" );
        exportCoordinateList( writer, bbox.getLowerCorner(), " " );
        writer.writeEndElement();

        // "ows:UpperCorner" element (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( owsNS, "UpperCorner" );
        exportCoordinateList( writer, bbox.getUpperCorner(), " " );
        writer.writeEndElement();
        writer.writeEndElement();// OWS110_NS, "BoundingBox"

    }

    /**
     * @param writer
     *            to export to
     * @param coordinates
     *            to be exported
     * @param separator
     *            to use between coordinates, if <code>null</code> a space character will be used.
     * @throws XMLStreamException
     *             if the writing fails.
     */
    public static void exportCoordinateList( XMLStreamWriter writer, List<? extends Number> coordinates,
                                             String separator )
                            throws XMLStreamException {
        if ( coordinates != null && !coordinates.isEmpty() ) {
            String sep = separator;
            if ( separator == null || "".equals( separator ) ) {
                sep = " ";
            }
            int size = coordinates.size();
            StringBuilder sb = new StringBuilder( size );
            for ( int i = 0; i < size; ++i ) {
                sb.append( coordinates.get( i ) );
                if ( ( i + 1 ) < size ) {
                    sb.append( sep );
                }
            }
            writer.writeCharacters( sb.toString() );
        }

    }

    /**
     * Exports a {@link Point} as a <code>ows:PositionType</code>.
     *
     * @param writer
     * @param pos
     *            point to be exported
     * @throws XMLStreamException
     */
    public static void exportPositionType( XMLStreamWriter writer, Point pos )
                            throws XMLStreamException {
        boolean needsDelim = false;
        for ( double coord : pos.getAsArray() ) {
            if ( needsDelim ) {
                writer.writeCharacters( " " );
            }
            writer.writeCharacters( "" + coord );
            needsDelim = true;
        }
    }
}
