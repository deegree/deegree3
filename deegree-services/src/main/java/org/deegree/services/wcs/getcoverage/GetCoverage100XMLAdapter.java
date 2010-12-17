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
package org.deegree.services.wcs.getcoverage;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XPath;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.rangeset.ValueType;
import org.deegree.coverage.rangeset.Interval.Closure;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.utils.GeometryUtils;
import org.deegree.protocol.wcs.WCSConstants;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.wcs.WCSRequest100XMLAdapter;
import org.deegree.services.wcs.model.Grid;

/**
 * This is an xml adapter for GetCoverage requests after the WCS 1.0.0 spec.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetCoverage100XMLAdapter extends WCSRequest100XMLAdapter {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( GetCoverage100XMLAdapter.class );

    /**
     * @param rootElement
     */
    public GetCoverage100XMLAdapter( OMElement rootElement ) {
        this.setRootElement( rootElement );
    }

    /**
     * @return the parsed request
     * @throws OWSException
     */
    public GetCoverage parse()
                            throws OWSException {

        Version version = checkVersion( rootElement );

        String coverage = parseCoverageName();

        checkDomainSubset();

        Envelope requestEnvelope = parseEnvelope();

        String outputCRS = parseOutputCRS( requestEnvelope.getCoordinateSystem().getName() );

        Grid grid = parseGrid( requestEnvelope, outputCRS );

        RangeSet rangeSet = parseRangeSubset();

        String interpolation = getInterpolation();

        String format = parseOutputFormat();

        // remove the image as in KVP
        if ( format.startsWith( "image/" ) ) {
            format = format.substring( "image/".length() );
        }

        return new GetCoverage( version, coverage, requestEnvelope, outputCRS, format, grid, interpolation,
                                WCSConstants.EXCEPTION_FORMAT_100, false, rangeSet );
    }

    /**
     * @return
     * @throws OWSException
     * 
     */
    private RangeSet parseRangeSubset()
                            throws OWSException {
        OMElement range = getElement( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                              + ":rangeSubset", wcsNSContext ) );
        RangeSet parsedRange = null;

        if ( range != null ) {
            List<OMElement> axisSubset = getRequiredElements( rootElement, new XPath( "/" + WCS_PREFIX
                                                                                      + ":GetCoverage/" + WCS_PREFIX
                                                                                      + ":rangeSubset/" + WCS_PREFIX
                                                                                      + ":axisSubset", wcsNSContext ) );
            List<AxisSubset> axisSubsets = new ArrayList<AxisSubset>( axisSubset.size() );
            for ( OMElement axis : axisSubset ) {
                if ( axis != null ) {
                    String name = axis.getAttributeValue( new QName( "name" ) );
                    if ( name == null || "".equals( name ) ) {
                        throw new OWSException( "missing axisSubset/@name in request",
                                                OWSException.MISSING_PARAMETER_VALUE, "axisSubset/@name" );
                    }
                    List<OMElement> ranges = getRequiredElements( axis, new XPath( "*", nsContext ) );
                    List<Interval<?, ?>> intervals = null;
                    List<SingleValue<?>> singleValues = null;
                    for ( OMElement rangeElem : ranges ) {
                        String localName = rangeElem.getLocalName();

                        if ( "interval".equals( localName ) ) {

                            SingleValue<?> min = parseTypedLiteral( getElement( rangeElem, new XPath( WCS_PREFIX
                                                                                                      + ":min",
                                                                                                      nsContext ) ) );
                            SingleValue<?> max = parseTypedLiteral( getElement( rangeElem, new XPath( WCS_PREFIX
                                                                                                      + ":max",
                                                                                                      nsContext ) ) );
                            if ( min.type != max.type ) {
                                throw new OWSException(
                                                        "The type declarations of the interval are different, this may not be.",
                                                        OWSException.INVALID_PARAMETER_VALUE );
                            }

                            String type = rangeElem.getAttributeValue( new QName( "type" ) );
                            type = ( type == null || "".equals( type ) ) ? "String" : type;
                            ValueType t = ValueType.fromString( type );
                            if ( t != min.type ) {
                                throw new OWSException(
                                                        "The type declarations of the interval are different, this may not be.",
                                                        OWSException.INVALID_PARAMETER_VALUE );
                            }

                            String semantic = rangeElem.getAttributeValue( new QName( "semantic" ) );

                            boolean atomic = getNodeAsBoolean( rangeElem, new XPath( "@atomic", nsContext ), false );

                            SingleValue<?> spacing = parseTypedLiteral( getElement( rangeElem, new XPath( WCS_PREFIX
                                                                                                          + ":res",
                                                                                                          nsContext ) ) );

                            Closure closure = parseClosure( rangeElem );

                            if ( intervals == null ) {
                                intervals = new ArrayList<Interval<?, ?>>();
                            }
                            // rb: warning is ok, min and max were checked to be equal.
                            intervals.add( new Interval( min, max, closure, semantic, atomic, spacing ) );
                        } else if ( "singleValue".equals( localName ) ) {
                            SingleValue<?> single = parseTypedLiteral( rangeElem );
                            boolean isInterval = false;
                            if ( single.type == ValueType.Void || single.type == ValueType.String ) {
                                // and now for the check if the value of the single value contains a '/', than it is
                                // actually an interval.
                                String val = (String) (Comparable<?>) single.value;
                                if ( val != null ) {
                                    if ( val.contains( "/" ) ) {
                                        // aha, an interval
                                        LOG.warn( "A single value uses interval semantic, parsing the single value as an interval." );
                                        if ( intervals == null ) {
                                            intervals = new ArrayList<Interval<?, ?>>();
                                        }
                                        List<Interval<?, ?>> parseIntervals = GetCoverage100KVPAdapter.parseIntervals(
                                                                                                                       val,
                                                                                                                       single.type );
                                        if ( !parseIntervals.isEmpty() ) {
                                            isInterval = true;
                                            intervals.addAll( parseIntervals );
                                        }
                                    }
                                }
                            }
                            if ( !isInterval ) {
                                if ( singleValues == null ) {
                                    singleValues = new ArrayList<SingleValue<?>>();
                                }
                                singleValues.add( parseTypedLiteral( rangeElem ) );
                            }

                        }
                    }
                    axisSubsets.add( new AxisSubset( name, null, intervals, singleValues ) );
                }

            }
            parsedRange = new RangeSet( axisSubsets );

        }
        return parsedRange;

    }

    /**
     * @param rangeElem
     * @return
     */
    private Closure parseClosure( OMElement rootElement ) {
        String closureValue = rootElement.getAttributeValue( new QName( "closure" ) );
        return Closure.fromString( closureValue );

    }

    private SingleValue<?> parseTypedLiteral( OMElement typedLiteralType ) {
        if ( typedLiteralType == null ) {
            return null;
        }
        String type = typedLiteralType.getAttributeValue( new QName( "type" ) );
        String value = typedLiteralType.getText();
        return SingleValue.createFromString( type, value );
    }

    /**
     * throws OWSException if the only a temporal subset is requested.
     * 
     * @throws OWSException
     */
    private void checkDomainSubset()
                            throws OWSException {
        OMElement domainSubset = getElement( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                                     + ":domainSubset", wcsNSContext ) );

        checkRequiredElement( "domainSubset", domainSubset );

        domainSubset = getElement( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                           + ":domainSubset/" + WCS_PREFIX + ":spatialSubset",
                                                           wcsNSContext ) );
        OMElement temporalSubset = getElement( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                                       + ":domainSubset/" + WCS_PREFIX
                                                                       + ":temporalSubset", wcsNSContext ) );
        if ( domainSubset == null ) {
            if ( temporalSubset != null ) {
                throw new OWSException( "The temporal subset is currently not supported by this WCS implementation.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }
            checkRequiredElement( "spatialSubset or temporalSubset", null );
        }

        if ( temporalSubset != null ) {
            LOG.warn( "The temporal subset is currently not supported by this WCS implementation, it will be ignored." );
        }

    }

    private String getInterpolation() {
        return getNodeAsString( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                        + ":interpolationMethod", wcsNSContext ),
                                InterpolationType.NEAREST_NEIGHBOR.name() );
    }

    private String parseOutputFormat()
                            throws OWSException {
        String format = getNodeAsString( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                                 + ":output/" + WCS_PREFIX + ":format", wcsNSContext ),
                                         null );
        checkRequiredString( "format", format );
        return format;
    }

    private String parseOutputCRS( String defaultCRS ) {
        return getNodeAsString( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX + ":output/"
                                                        + WCS_PREFIX + ":crs", wcsNSContext ), defaultCRS );
    }

    private OMElement parseEnvelopeElement()
                            throws OWSException {
        OMElement envElem = getElement( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                                + ":domainSubset/" + WCS_PREFIX
                                                                + ":spatialSubset/gml:Envelope", wcsNSContext ) );
        checkRequiredElement( "Envelope", envElem );
        return envElem;
    }

    private String parseSRSName( OMElement envelope, String defaultSRS ) {
        return getNodeAsString( envelope, new XPath( "@srsName", wcsNSContext ), defaultSRS );
    }

    private String parseCoverageName()
                            throws OWSException {
        String coverage = getNodeAsString( rootElement, new XPath( "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX
                                                                   + ":sourceCoverage", wcsNSContext ), null );
        checkRequiredString( "sourceCoverage", coverage );
        return coverage;
    }

    private Grid parseGrid( Envelope requestEnvelope, String outputCRS )
                            throws OWSException {
        Envelope targetEnvelope = requestEnvelope;
        try {
            targetEnvelope = GeometryUtils.createConvertedEnvelope( requestEnvelope, new CRS( outputCRS ) );
        } catch ( TransformationException e ) {
            // request = target;
        }
        String path = "/" + WCS_PREFIX + ":GetCoverage/" + WCS_PREFIX + ":domainSubset/" + WCS_PREFIX
                      + ":spatialSubset/";
        OMElement gridElem = getElement( rootElement, new XPath( path + "gml:Grid | " + path + "gml:RectifiedGrid",
                                                                 wcsNSContext ) );
        checkRequiredElement( "Grid", gridElem );

        int dimension = getRequiredNodeAsInteger( gridElem, new XPath( "@dimension", wcsNSContext ) );

        OMElement gridEnvElem = getElement( gridElem, new XPath( "gml:limits/gml:GridEnvelope", wcsNSContext ) );
        double[] min = parseNums( "gml:low", getElement( gridEnvElem, new XPath( "gml:low", wcsNSContext ) ) );
        double[] max = parseNums( "gml:high", getElement( gridEnvElem, new XPath( "gml:high", wcsNSContext ) ) );

        // TODO check for axis-order, how to handle the rectified grid?
        int width = (int) Math.round( max[0] - min[0] );
        int height = (int) Math.round( max[1] - min[1] );
        int depth = Integer.MIN_VALUE;
        if ( min.length >= 3 ) {
            depth = (int) Math.round( max[2] - min[2] );
        }

        String[] axisNames = getNodesAsStrings( gridElem, new XPath( "gml:axisName", wcsNSContext ) );
        if ( axisNames.length == 0 ) {
            checkRequiredString( "axisName", null );
        }
        if ( "RectifiedGrid".equals( gridElem.getLocalName() ) ) {
            // origin
            OMElement origin = getElement( gridElem, new XPath( "gml:origin", wcsNSContext ) );
            checkRequiredElement( "origin", origin );
            double[] origPoint = parseNums( "gml:pos", getElement( origin, new XPath( "gml:pos", wcsNSContext ) ) );

            // offsetVector
            String[] offsets = getNodesAsStrings( gridElem, new XPath( "gml:offsetVector", wcsNSContext ) );
            if ( offsets == null || offsets.length == 0 ) {
                checkRequiredElement( "one or more offsetVector", null );
            }
            List<double[]> offsetVectors = new ArrayList<double[]>( offsets.length );
            for ( String ov : offsets ) {
                offsetVectors.add( parseNumsFromString( "gml:offsetVector", ov ) );
            }
        }

        return Grid.fromSize( width, height, depth, targetEnvelope );

    }

    private Envelope parseEnvelope()
                            throws OWSException {

        // get the values from a spatial subset
        OMElement envelope = parseEnvelopeElement();
        String srsName = parseSRSName( envelope, "EPSG:4326" );

        List<OMElement> posElems = getElements( envelope, new XPath( "gml:pos", wcsNSContext ) );
        if ( posElems.size() != 2 ) {
            throw new OWSException( "invalid envelope, need two gml:pos", OWSException.INVALID_PARAMETER_VALUE,
                                    "spatialSubset" );
        }
        double[] min = parseNums( "gml:pos", posElems.get( 0 ) );
        double[] max = parseNums( "gml:pos", posElems.get( 1 ) );

        CRS crs = new CRS( srsName );
        GeometryFactory geomFactory = new GeometryFactory();
        return geomFactory.createEnvelope( min, max, crs );
    }

    private double[] parseNums( String elemName, OMElement element )
                            throws OWSException {
        if ( element == null ) {
            throw new OWSException( "missing " + elemName, OWSException.MISSING_PARAMETER_VALUE, elemName );
        }
        return parseNumsFromString( elemName, element.getText() );
    }

    private double[] parseNumsFromString( String elemName, String numbers )
                            throws OWSException {
        try {
            String[] parts = numbers.split( " " );
            double[] values = new double[parts.length];
            for ( int i = 0; i < values.length; i++ ) {
                values[i] = Double.parseDouble( parts[i] );
            }
            return values;
        } catch ( NumberFormatException ex ) {
            throw new OWSException( "invalid " + elemName + " " + numbers, OWSException.INVALID_PARAMETER_VALUE,
                                    "spatialSubset" );
        } catch ( IndexOutOfBoundsException ex ) {
            throw new OWSException( "invalid " + elemName + " " + numbers, OWSException.INVALID_PARAMETER_VALUE,
                                    "spatialSubset" );
        }

    }

}
