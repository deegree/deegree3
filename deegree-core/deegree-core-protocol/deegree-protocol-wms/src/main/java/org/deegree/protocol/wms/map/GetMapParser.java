//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.protocol.wms.map;

import static java.awt.Color.WHITE;
import static java.util.Collections.emptyMap;
import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;
import static org.deegree.commons.xml.CommonNamespaces.SLDNS;
import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsDouble;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredElementTextAsDouble;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredElementTextAsInteger;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getRequiredText;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getText;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipToRequiredElement;

import java.awt.Color;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.layer.LayerRef;
import org.deegree.layer.dims.DimensionInterval;
import org.deegree.protocol.wms.AbstractWmsParser;
import org.deegree.protocol.wms.WMSConstants;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.protocol.wms.ops.SLDParser;
import org.deegree.protocol.wms.sld.StyleContainer;
import org.deegree.protocol.wms.sld.StylesContainer;
import org.deegree.style.StyleRef;

/**
 * Adapter between XML <code>GetMap</code> requests and {@link GetMap} objects.
 * <p>
 * Supported WMS versions:
 * <ul>
 * <li>1.3.0</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetMapParser extends AbstractWmsParser {

    private static final SimpleGeometryFactory GEOMETRY_FACTORY = new SimpleGeometryFactory();

    private final Map<String, String> requestParameters;
    
    public GetMapParser() {
        requestParameters = emptyMap();
    }
    
    public GetMapParser( Map<String, String> requestParameters) {
        this.requestParameters = requestParameters;
    }
    
    /**
     * Parses a WMS <code>GetMap</code> document into a {@link GetMap} object.
     * 
     * <p>
     * Supported WMS versions:
     * <ul>
     * <li>1.3.0</li>
     * </ul>
     * </p>
     * 
     * @return parsed {@link GetMap} request, never <code>null</code>
     * @throws XMLStreamException
     *             if an error occurs during parsing the xml
     * @throws InvalidParameterException
     *             if the request version is not supported
     * @throws OWSException
     *             if the CRS is not supported or an error occurred during parsing a value
     */
    public GetMap parse( XMLStreamReader getMap )
                            throws OWSException, XMLStreamException {
        Version version = forwardToStartAndDetermineVersion( getMap );
        if ( !WMSConstants.VERSION_130.equals( version ) )
            throw new InvalidParameterException( "Version " + version + " is not supported (yet)." );
        try {
            return parse130( getMap );
        } catch ( UnknownCRSException e ) {
            throw new OWSException( e.getMessage(), OWSException.NO_APPLICABLE_CODE );
        } catch ( ParseException e ) {
            throw new OWSException( e.getMessage(), OWSException.NO_APPLICABLE_CODE );
        }
    }

    private GetMap parse130( XMLStreamReader in )
                            throws UnknownCRSException, XMLStreamException, OWSException, ParseException {
        skipToRequiredElement( in, new QName( SLDNS, "StyledLayerDescriptor" ) );
        StylesContainer parsedStyles = SLDParser.parse( in );
        List<LayerRef> layers = new ArrayList<LayerRef>();
        List<StyleRef> styles = new ArrayList<StyleRef>();
        for ( StyleContainer style : parsedStyles.getStyles() ) {
            layers.add( style.getLayerRef() );
            styles.add( style.getStyleRef() );
        }

        skipToRequiredElement( in, new QName( SLDNS, "CRS" ) );
        ICRS crs = parseCRS( in );

        skipToRequiredElement( in, new QName( SLDNS, "BoundingBox" ) );
        Envelope envelope = parseBoundingBox( in );

        skipToRequiredElement( in, new QName( SLDNS, "Output" ) );
        Output output = parseOutput( in );

        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put( "EXCEPTIONS", parseExceptions( in ) );

        Map<String, List<?>> dimensions = parseDimensions( in );

        return createGetMap( layers, styles, crs, envelope, output, parameterMap, dimensions );
    }

    private GetMap createGetMap( List<LayerRef> layers, List<StyleRef> styles, ICRS crs, Envelope envelope,
                                 Output output, Map<String, String> parameterMap, Map<String, List<?>> dimensions ) {
        int width = output.width;
        int height = output.height;
        String format = output.format;
        boolean transparent = output.transparent;
        Color color = output.bgcolor;
        return new GetMap( layers, styles, width, height, envelope, crs, format, transparent, color, parameterMap,
                           dimensions, requestParameters );
    }

    private Output parseOutput( XMLStreamReader in )
                            throws XMLStreamException, OWSException {
        skipToRequiredElement( in, new QName( SLDNS, "Size" ) );
        skipToRequiredElement( in, new QName( SLDNS, "Width" ) );
        int width = getRequiredElementTextAsInteger( in, new QName( SLDNS, "Width" ), true );
        skipToRequiredElement( in, new QName( SLDNS, "Height" ) );
        int height = getRequiredElementTextAsInteger( in, new QName( SLDNS, "Height" ), true );
        skipToRequiredElement( in, new QName( WMSNS, "Format" ) );
        String format = getRequiredText( in, new QName( WMSNS, "Format" ), true );

        boolean transparent = false;
        if ( in.getName().equals( new QName( SLDNS, "Transparent" ) ) )
            transparent = XMLStreamUtils.getElementTextAsBoolean( in, new QName( SLDNS, "Transparent" ), false, true );

        Color bgcolor = Color.WHITE;
        if ( in.getName().equals( new QName( SLDNS, "BGcolor" ) ) )
            bgcolor = parseColor( in );

        skipElement( in );
        nextElement( in );
        return new Output( width, height, format, transparent, bgcolor );
    }

    private Color parseColor( XMLStreamReader in )
                            throws XMLStreamException, OWSException {
        String bgcolor = XMLStreamUtils.getText( in, new QName( SLDNS, "BGcolor" ), null, true );
        if ( bgcolor == null )
            return WHITE;
        try {
            return Color.decode( bgcolor );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "Could not parse BGcolor '" + bgcolor + "' as color.",
                                    OWSException.NO_APPLICABLE_CODE );
        }
    }

    private String parseExceptions( XMLStreamReader in )
                            throws XMLStreamException {
        QName exceptions = new QName( SLDNS, "Exceptions" );
        if ( in.getName().equals( exceptions ) ) {
            return XMLStreamUtils.getText( in, exceptions, "XML", true );
        }
        return "XML";
    }

    private Map<String, List<?>> parseDimensions( XMLStreamReader in )
                            throws XMLStreamException {
        Map<String, List<?>> dimensions = new HashMap<String, List<?>>();
        parseAndAddTime( in, dimensions );

        QName elevation = new QName( SLDNS, "Elevation" );
        if ( in.getName().equals( elevation ) ) {
            nextElement( in );
            parseAndAddValues( in, dimensions );
            parseAndAddInterval( in, dimensions );
        }
        return dimensions;
    }

    private void parseAndAddValues( XMLStreamReader in, Map<String, List<?>> dimensions )
                            throws XMLStreamException {
        QName value = new QName( SLDNS, "Value" );
        List<Double> values = new ArrayList<Double>();
        while ( in.isStartElement() && in.getName().equals( value ) ) {
            double valueValue = getElementTextAsDouble( in, value, Double.NaN, true );
            if ( !Double.isNaN( valueValue ) )
                values.add( valueValue );
        }
        if ( !values.isEmpty() ) {
            dimensions.put( "elevation", values );
        }
    }

    @SuppressWarnings("unchecked")
    private void parseAndAddInterval( XMLStreamReader in, Map<String, List<?>> dimensions )
                            throws XMLStreamException {
        QName interval = new QName( SLDNS, "Interval" );
        if ( in.getName().equals( interval ) ) {
            QName min = new QName( SLDNS, "Min" );
            skipToRequiredElement( in, min );
            double minValue = getRequiredElementTextAsDouble( in, min, true );

            QName max = new QName( SLDNS, "Max" );
            skipToRequiredElement( in, max );
            double maxValue = getRequiredElementTextAsDouble( in, max, true );
            DimensionInterval<Double, Double, Double> dimensionInterval = new DimensionInterval<Double, Double, Double>(
                                                                                                                         minValue,
                                                                                                                         maxValue,
                                                                                                                         0d );
            dimensions.put( "elevation", Arrays.asList( dimensionInterval ) );
        }
    }

    private void parseAndAddTime( XMLStreamReader in, Map<String, List<?>> dimensions )
                            throws XMLStreamException {
        QName time = new QName( SLDNS, "Time" );
        if ( in.getName().equals( time ) ) {
            String timeValue = getText( in, time, null, true );
            if ( timeValue != null ) {
                DateTime parsedDateTime = ISO8601Converter.parseDateTime( timeValue );
                dimensions.put( "time", Arrays.asList( parsedDateTime ) );
            }
        }
    }

    private ICRS parseCRS( XMLStreamReader in )
                            throws UnknownCRSException, XMLStreamException {
        String crs = XMLStreamUtils.getRequiredText( in, new QName( SLDNS, "CRS" ), true );
        return CRSManager.lookup( crs );
    }

    private Envelope parseBoundingBox( XMLStreamReader in )
                            throws UnknownCRSException, OWSException, XMLStreamException {
        String crsValue = XMLStreamUtils.getAttributeValue( in, "crs" );
        ICRS crs = CRSManager.lookup( crsValue );

        QName lowerCorner = new QName( OWS_NS, "LowerCorner" );
        skipToRequiredElement( in, lowerCorner );
        double[] min = parseCorner( in, lowerCorner );

        QName upperCorner = new QName( OWS_NS, "UpperCorner" );
        skipToRequiredElement( in, upperCorner );
        double[] max = parseCorner( in, upperCorner );

        return GEOMETRY_FACTORY.createEnvelope( min, max, crs );
    }

    private double[] parseCorner( XMLStreamReader in, QName expectedElement )
                            throws OWSException, XMLStreamException {
        String corner = getRequiredText( in, expectedElement, true );
        String[] coords = corner.split( " " );
        if ( coords.length != 2 )
            throw new OWSException( "Could not parse element " + expectedElement, OWSException.NO_APPLICABLE_CODE );
        try {
            double coord1 = Double.parseDouble( coords[0] );
            double coord2 = Double.parseDouble( coords[1] );
            return new double[] { coord1, coord2 };
        } catch ( NumberFormatException e ) {
            String msg = "Invalid element " + expectedElement
                         + ". Expected are two double values, seperated by an white space.";
            throw new OWSException( msg, OWSException.NO_APPLICABLE_CODE );
        }
    }

    private class Output {
        int width;

        int height;

        String format;

        boolean transparent;

        Color bgcolor;

        public Output( int width, int height, String format, boolean transparent, Color bgcolor ) {
            this.width = width;
            this.height = height;
            this.format = format;
            this.transparent = transparent;
            this.bgcolor = bgcolor;
        }
    }

}