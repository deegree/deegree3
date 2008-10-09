//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.gml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.model.crs.configuration.CRSConfiguration;
import org.deegree.model.crs.configuration.CRSProvider;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.CRSConfigurationException;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.composite.CompositeSurface;
import org.deegree.model.geometry.multi.MultiCurve;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.multi.MultiPoint;
import org.deegree.model.geometry.multi.MultiSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryAdapter extends XMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( GML311GeometryAdapter.class );

    private static String FID = "gid";

    private static String GMLID = "id";

    private static String GML_NS = CommonNamespaces.GMLNS;

    // TODO in what way does this have to be configurable?
    private GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();

    // TODO in what way does this have to be configurable?
    private CRSProvider crsProvider = CRSConfiguration.getCRSConfiguration().getProvider();

    /**
     * Returns the object representation for the given <code>gml:_Geometry</code> element event that the cursor of the
     * given <code>XMLStreamReader</code> points at.
     * <p>
     * Currently, the following geometry elements are supported:
     * <table border="1">
     * <tr>
     * <th>Geometry element name<br>
     * (in gml namespace)</th>
     * <th>Return type<br>
     * (deegree {@link Geometry})</th>
     * </tr>
     * <tr>
     * <td align="center">Box/Envelope</td>
     * <td align="center">{@link Envelope}</td>
     * </tr>
     * <tr>
     * <td align="center">CompositeSurface</td>
     * <td align="center">{@link CompositeSurface}</td>
     * </tr>
     * <tr>
     * <td align="center">Curve</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">LinearRing</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">LineString</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiCurve</td>
     * <td align="center">{@link MultiCurve}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiGeometry</td>
     * <td align="center">{@link MultiGeometry}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiLineString</td>
     * <td align="center">{@link MultiCurve}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiPoint</td>
     * <td align="center">{@link MultiPoint}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiPolygon</td>
     * <td align="center">{@link MultiSurface}</td>
     * </tr>
     * <tr>
     * <td align="center">MultiSurface</td>
     * <td align="center">{@link MultiSurface}</td>
     * </tr>
     * <tr>
     * <td align="center">Point/Center</td>
     * <td align="center">{@link Point}</td>
     * </tr>
     * <tr>
     * <td align="center">Polygon</td>
     * <td align="center">{@link Surface}</td>
     * </tr>
     * <tr>
     * <td align="center">Ring</td>
     * <td align="center">{@link Curve}</td>
     * </tr>
     * <tr>
     * <td align="center">Surface</td>
     * <td align="center">{@link Surface}</td>
     * </tr>
     * </table>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event of the geometry, afterwards points at the
     *            the <code>END_ELEMENT</code> event of the geometry
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the geometry element has no own
     *            <code>srsName</code> attribute
     * @return corresponding {@link Geometry} object
     * @throws XMLParsingException
     *             if the element is not a valid "gml:_Geometry" element
     * @throws XMLStreamException
     */
    public Geometry parseGeometry( XMLStreamReader xmlStream, String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        LOG.debug( " - parsing gml:_Geometry (begin): " + getCurrentEventInfo( xmlStream ) );

        Geometry geometry = null;

        if ( !GML_NS.equals( xmlStream.getNamespaceURI() ) ) {
            String msg = "Invalid gml:_Geometry element: " + xmlStream.getName()
                         + "' is not a GML geometry element. Not in the gml namespace.";
            throw new XMLParsingException( this, xmlStream, msg );
        }

        String name = xmlStream.getLocalName();
        if ( name.equals( "Point" ) ) {
            geometry = parsePoint( xmlStream, defaultSrsName );
        } else {
            String msg = "Invalid GML geometry: '" + xmlStream.getName()
                         + "' is not a (supported) GML geometry element.";
            throw new XMLParsingException( this, xmlStream, msg );
        }

        LOG.debug( " - parsing gml:_Geometry (end): " + getCurrentEventInfo( xmlStream ) );
        return geometry;
    }

    /**
     * Returns the object representation of a (&lt;gml:Point&gt;) element. Consumes all corresponding events from the
     * given <code>XMLStream</code>.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;gml:Point&gt;), afterwards points at
     *            the next event after the <code>END_ELEMENT</code> (&lt;/gml:Point&gt;)
     * @param defaultSrsName
     *            default srs for the geometry, this is only used if the "gml:Point" has no <code>srsName</code>
     *            attribute
     * @return corresponding {@link Point} object
     * @throws XMLParsingException
     * @throws XMLStreamException
     */
    public Point parsePoint( XMLStreamReader xmlStream, String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        Point point = null;
        String gid = parseGeometryId( xmlStream );
        String srsName = determineCurrentSrsName( xmlStream, defaultSrsName );

        // must contain exactly one of the following child elements: "gml:pos", "gml:coordinates" or "gml:coord"
        if ( xmlStream.nextTag() != XMLStreamConstants.START_ELEMENT ) {
            String name = xmlStream.getLocalName();
            if ( "pos".equals( name ) ) {
                double[] coords = parseDoubleList( xmlStream );
                point = geomFac.createPoint( gid, coords, lookupCRS( srsName, xmlStream ) );
            } else if ( "coordinates".equals( name ) ) {
                double[][] coordTuples = parseCoordinatesType( xmlStream );
                if ( coordTuples.length != 1 ) {
                    String msg = "A gml:Point element must contain exactly one tuple of coordinates.";
                    throw new XMLParsingException( this, xmlStream, msg );
                }
                point = geomFac.createPoint( gid, coordTuples[0], lookupCRS( srsName, xmlStream ) );
            } else if ( "coord".equals( name ) ) {
                // deprecated since GML 3.0, only included for backward compatibility
                double[] coords = parseCoordType( xmlStream );
                point = geomFac.createPoint( gid, coords, lookupCRS( srsName, xmlStream ) );
            } else {
                String msg = "Error in 'gml:Point' element. Expected either a 'gml:pos', 'gml:coordinates'"
                             + " or a 'gml:coord' element, but found '" + name + "'.";
                throw new XMLParsingException( this, xmlStream, msg );
            }
        }
        return point;
    }

    private double[] parseDoubleList( XMLStreamReader xmlStream )
                            throws XMLParsingException, XMLStreamException {
        String s = xmlStream.getElementText();
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
                throw new XMLParsingException( this, xmlStream, msg );
            }
        }
        return doubles;
    }

    private double[][] parseCoordinatesType( XMLStreamReader xmlStream )
                            throws XMLParsingException, XMLStreamException {

        String decimalSeparator = getAttributeValue( xmlStream, "decimal", "." );
        if ( !".".equals( decimalSeparator ) ) {
            String msg = "Currently, only '.' is supported as decimal separator.";
            throw new XMLParsingException( this, xmlStream, msg );
        }

        String coordinateSeparator = getAttributeValue( xmlStream, "cs", "," );
        String tupleSeparator = getAttributeValue( xmlStream, "ts", " " );

        String text = xmlStream.getElementText();

        List<String> tuples = new LinkedList<String>();
        StringTokenizer tupleTokenizer = new StringTokenizer( text, tupleSeparator );
        while ( tupleTokenizer.hasMoreTokens() ) {
            tuples.add( tupleTokenizer.nextToken() );
        }

        double[][] coordinateTuples = new double[tuples.size()][];
        for ( int i = 0; i < coordinateTuples.length; i++ ) {
            StringTokenizer coordinateTokenizer = new StringTokenizer( coordinateSeparator );
            List<String> tokens = new ArrayList<String>();
            while ( coordinateTokenizer.hasMoreTokens() ) {
                tokens.add( coordinateTokenizer.nextToken() );
            }
            double[] tuple = new double[tokens.size()];
            for ( int j = 0; j < tuple.length; j++ ) {
                try {
                    tuple[j] = Double.parseDouble( tokens.get( j ) );
                } catch ( NumberFormatException e ) {
                    String msg = "Value '" + tokens.get( j ) + "' cannot be parsed as a double.";
                    throw new XMLParsingException( this, xmlStream, msg );
                }
            }
            coordinateTuples[i] = tuple;
        }
        return coordinateTuples;
    }

    private static QName GML_X = new QName( GML_NS, "X" );

    private static QName GML_Y = new QName( GML_NS, "Y" );

    private static QName GML_Z = new QName( GML_NS, "Z" );

    private double[] parseCoordType( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // must be a 'gml:X' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_X.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Must contain an 'gml:X' element.";
            throw new XMLParsingException( this, xmlStream, msg );
        }
        double x = parseDouble( xmlStream.getElementText() );        
        event = xmlStream.nextTag();
        if ( event == XMLStreamConstants.END_ELEMENT ) {
            return new double[] { x };
        }
        
        // must be a 'gml:Y' element        
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_Y.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Second child element must be a 'gml:Y' element.";
            throw new XMLParsingException( this, xmlStream, msg );
        }
        double y = parseDouble( xmlStream.getElementText() );
        event = xmlStream.nextTag();
        if ( event == XMLStreamConstants.END_ELEMENT ) {
            return new double[] { x, y };
        }

        // must be a 'gml:Z' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_Z.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Third child element must be a 'gml:Z' element.";
            throw new XMLParsingException( this, xmlStream, msg );
        }
        double z = parseDouble( xmlStream.getElementText() );

        event = xmlStream.nextTag();
        if ( event != XMLStreamConstants.END_ELEMENT ) {
            skipElement( xmlStream );
        }
        return new double[] { x, y, z };
    }

    /**
     * Parses the geometry id attribute from the geometry <code>START_ELEMENT</code> event that the given
     * <code>XMLStreamReader</code> points to.
     * <p>
     * Looks after 'gml:id' (GML 3) first, if no such attribute is present, the 'gid' (GML 2) attribute is used.
     * 
     * @param xmlReader
     *            must point to the <code>START_ELEMENT</code> event of the feature
     * @return the geometry id, or "" (empty string) if neither a 'gml:id' nor a 'gid' attribute is present
     */
    private String parseGeometryId( XMLStreamReader xmlReader ) {

        String gid = xmlReader.getAttributeValue( GML_NS, GMLID );
        if ( gid == null ) {
            gid = xmlReader.getAttributeValue( null, FID );
        }

        // Check that the geometry id has the correct form. "gid" and "gml:id" are both based
        // on the XML type "ID": http://www.w3.org/TR/xmlschema11-2/#NCName
        // Thus, they must match the NCName production rule. This means that they may not contain
        // a separating colon (only at the first position a colon is allowed) and must not
        // start with a digit.
        if ( gid != null && gid.length() > 0 && !gid.matches( "[^\\d][^:]+" ) ) {
            String msg = Messages.getMessage( "ERROR_INVALID_FEATUREID", gid );
            throw new IllegalArgumentException( msg );
        }
        return gid;
    }

    /**
     * Determines the <code>srsName</code> value for the current geometry element.
     * 
     * @param xmlStream
     *            must point to the <code>START_ELEMENT</code> event of the geometry (which may have an
     *            <code>srsName</code> attribute)
     * @param defaultSrsName
     *            default srs for the geometry, this is returned if the geometry element has no <code>srsName</code>
     *            attribute
     * @return the applicable <code>srsName</code> value, may be null
     */
    private String determineCurrentSrsName( XMLStreamReader xmlStream, String defaultSrsName ) {
        String srsName = xmlStream.getAttributeValue( null, FID );
        if ( srsName == null || srsName.length() == 0 ) {
            srsName = defaultSrsName;
        }
        return srsName;
    }

    private CoordinateSystem lookupCRS( String srsName, XMLStreamReader xmlStream ) {
        CoordinateSystem crs = null;
        try {
            crs = crsProvider.getCRSByID( srsName );
        } catch ( CRSConfigurationException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( crs == null ) {
            String msg = "Unknown coordinate reference system '" + srsName + "'.";
            throw new XMLParsingException( this, xmlStream, msg );
        }
        return crs;
    }
}
