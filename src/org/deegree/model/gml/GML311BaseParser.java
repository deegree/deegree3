package org.deegree.model.gml;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.types.Length;
import org.deegree.commons.types.Measure;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.crs.configuration.CRSConfiguration;
import org.deegree.model.crs.configuration.CRSProvider;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.CRSConfigurationException;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.primitive.Point;
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
class GML311BaseParser {

    private static final Logger LOG = LoggerFactory.getLogger( GML311BaseParser.class );

    private static final CRSProvider crsProvider = CRSConfiguration.getCRSConfiguration().getProvider();

    private static final QName GML_X = new QName( GMLNS, "X" );

    private static final QName GML_Y = new QName( GMLNS, "Y" );

    private static final QName GML_Z = new QName( GMLNS, "Z" );

    protected final GeometryFactory geomFac;

    protected final XMLStreamReaderWrapper xmlStream;

    protected GML311BaseParser( GeometryFactory geomFac, XMLStreamReaderWrapper xmlStream ) {
        this.geomFac = geomFac;
        this.xmlStream = xmlStream;
    }

    protected Point parseDirectPositionType( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        String srsName = determineCurrentSrsName( defaultSrsName );

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
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        return geomFac.createPoint( null, doubles, lookupCRS( srsName ) );
    }

    protected List<Point> parsePosList( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        CoordinateSystem crs = lookupCRS( determineCurrentSrsName( defaultSrsName ) );
        int coordDim = determineCoordDimensions( crs.getDimension() );

        String s = xmlStream.getElementText();
        // don't use String.split(regex) here (speed)
        StringTokenizer st = new StringTokenizer( s );
        List<String> tokens = new ArrayList<String>();
        while ( st.hasMoreTokens() ) {
            tokens.add( st.nextToken() );
        }
        int numCoords = tokens.size();
        if ( numCoords % coordDim != 0 ) {
            String msg = "Cannot parse 'gml:posList': contains " + tokens.size()
                         + " values, but coordinate dimension is " + coordDim + ". This does not match.";
            throw new XMLParsingException( xmlStream, msg );
        }

        int numPoints = numCoords / coordDim;
        List<Point> points = new ArrayList<Point>();

        int tokenPos = 0;
        for ( int i = 0; i < numPoints; i++ ) {
            double[] pointCoords = new double[coordDim];
            for ( int j = 0; j < coordDim; j++ ) {
                try {
                    pointCoords[j] = Double.parseDouble( tokens.get( tokenPos++ ) );
                } catch ( NumberFormatException e ) {
                    String msg = "Value '" + tokens.get( tokenPos - 1 ) + "' cannot be parsed as a double.";
                    throw new XMLParsingException( xmlStream, msg );
                }
            }
            points.add( geomFac.createPoint( null, pointCoords, crs ) );
        }
        return points;
    }

    protected List<Point> parseCoordinates( String defaultSrsName )
                            throws XMLParsingException, XMLStreamException {

        CoordinateSystem crs = lookupCRS( defaultSrsName );

        String decimalSeparator = xmlStream.getAttributeValueWDefault( "decimal", "." );
        if ( !".".equals( decimalSeparator ) ) {
            String msg = "Currently, only '.' is supported as decimal separator.";
            throw new XMLParsingException( xmlStream, msg );
        }

        String coordinateSeparator = xmlStream.getAttributeValueWDefault( "cs", "," );
        String tupleSeparator = xmlStream.getAttributeValueWDefault( "ts", " " );

        String text = xmlStream.getElementText();

        List<String> tuples = new LinkedList<String>();
        StringTokenizer tupleTokenizer = new StringTokenizer( text, tupleSeparator );
        while ( tupleTokenizer.hasMoreTokens() ) {
            tuples.add( tupleTokenizer.nextToken() );
        }

        List<Point> points = new ArrayList<Point>( tuples.size() );
        for ( int i = 0; i < tuples.size(); i++ ) {
            StringTokenizer coordinateTokenizer = new StringTokenizer( tuples.get( i ), coordinateSeparator );
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
                    throw new XMLParsingException( xmlStream, msg );
                }
            }
            points.add( geomFac.createPoint( null, tuple, crs ) );
        }
        return points;
    }

    protected double[] parseCoordType()
                            throws XMLStreamException {

        int event = xmlStream.nextTag();

        // must be a 'gml:X' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_X.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Must contain an 'gml:X' element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        double x = xmlStream.getElementTextAsDouble();
        event = xmlStream.nextTag();
        if ( event == XMLStreamConstants.END_ELEMENT ) {
            return new double[] { x };
        }

        // must be a 'gml:Y' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_Y.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Second child element must be a 'gml:Y' element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        double y = xmlStream.getElementTextAsDouble();
        event = xmlStream.nextTag();
        if ( event == XMLStreamConstants.END_ELEMENT ) {
            return new double[] { x, y };
        }

        // must be a 'gml:Z' element
        if ( event != XMLStreamConstants.START_ELEMENT || !GML_Z.equals( xmlStream.getName() ) ) {
            String msg = "Invalid 'gml:coords' element. Third child element must be a 'gml:Z' element.";
            throw new XMLParsingException( xmlStream, msg );
        }
        double z = xmlStream.getElementTextAsDouble();

        event = xmlStream.nextTag();
        if ( event != XMLStreamConstants.END_ELEMENT ) {
            xmlStream.skipElement();
        }
        return new double[] { x, y, z };
    }

    protected Length parseLengthType()
                            throws XMLStreamException {
        String uom = xmlStream.getAttributeValue( null, "uom" );
        if ( uom == null ) {
            String msg = "Required attribute 'uom' missing in element '" + xmlStream.getName() + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        String s = xmlStream.getElementText();
        double value = 0.0;
        try {
            value = Double.parseDouble( s );
        } catch ( NumberFormatException e ) {
            String msg = "Error in element '" + xmlStream.getName() + "': expected a double value, but found '" + s
                         + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return new Length( value, uom );
    }

    protected Angle parseAngleType()
                            throws XMLStreamException {
        String uom = xmlStream.getAttributeValue( null, "uom" );
        if ( uom == null ) {
            String msg = "Required attribute 'uom' missing in element '" + xmlStream.getName() + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        String s = xmlStream.getElementText();
        double value = 0.0;
        try {
            value = Double.parseDouble( s );
        } catch ( NumberFormatException e ) {
            String msg = "Error in element '" + xmlStream.getName() + "': expected a double value, but found '" + s
                         + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return new Angle( value, uom );
    }

    protected Measure parseMeasureType()
                            throws XMLStreamException {
        String uom = xmlStream.getAttributeValue( null, "uom" );
        if ( uom == null ) {
            String msg = "Required attribute 'uom' missing in element '" + xmlStream.getName() + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        String s = xmlStream.getElementText();
        double value = 0.0;
        try {
            value = Double.parseDouble( s );
        } catch ( NumberFormatException e ) {
            String msg = "Error in element '" + xmlStream.getName() + "': expected a double value, but found '" + s
                         + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return new Measure( value, uom );
    }

    protected CoordinateSystem lookupCRS( String srsName ) {
        CoordinateSystem crs = null;
        try {
            crs = crsProvider.getCRSByID( srsName );
        } catch ( CRSConfigurationException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( crs == null ) {
            String msg = "Unknown coordinate reference system '" + srsName + "'.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return crs;
    }

    /**
     * Determines the <code>srsName</code> value for the current geometry element.
     * 
     * @param defaultSrsName
     *            default srs for the geometry, this is returned if the geometry element has no <code>srsName</code>
     *            attribute
     * @return the applicable <code>srsName</code> value, may be null
     */
    protected String determineCurrentSrsName( String defaultSrsName ) {
        String srsName = xmlStream.getAttributeValue( null, "srsName" );
        if ( srsName == null || srsName.length() == 0 ) {
            srsName = defaultSrsName;
        }
        return srsName;
    }

    protected double[] parseDoubleList()
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
                throw new XMLParsingException( xmlStream, msg );
            }
        }
        return doubles;
    }

    /**
     * Parses the <code>orientation</code> attribute from element that the associated <code>XMLStreamReader</code>
     * points to.
     * 
     * @return true, if the <code>orientation</attribute> is '+' or not present, false if the attribute is '-'
     */
    protected boolean parseOrientation() {

        String orientation = xmlStream.getAttributeValueWDefault( "orientation", "-" );
        if ( "-".equals( orientation ) ) {
            return false;
        } else if ( "+".equals( orientation ) ) {
            return true;
        }
        String msg = "Orientation value (='" + orientation + "') is not valid. Valid values are '-' and '+'.";
        throw new XMLParsingException( xmlStream, msg );
    }

    /**
     * Applies a simple heuristic to determine the number of coordinate dimensions.
     * 
     * @param defaultCoordDimensions
     *            default coordinate dimensionality, this is returned if the element has no <code>srsDimension</code>
     *            attribute
     * @return coordinate dimensionality
     */
    protected int determineCoordDimensions( int defaultCoordDimensions ) {

        String srsDimension = xmlStream.getAttributeValueWDefault( "srsDimension", "" + defaultCoordDimensions );
        int coordDimensions = 0;
        try {
            coordDimensions = Integer.parseInt( srsDimension );
        } catch ( NumberFormatException e ) {
            String msg = "Value of srsDimension attribute (='" + srsDimension + "') is not a valid integer.";
            throw new XMLParsingException( xmlStream, msg );
        }
        return coordDimensions;
    }
}
