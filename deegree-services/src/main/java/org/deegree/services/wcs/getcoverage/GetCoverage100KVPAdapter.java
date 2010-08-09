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

import static org.deegree.commons.utils.kvp.KVPUtils.getDefault;
import static org.deegree.commons.utils.kvp.KVPUtils.getRequired;
import static org.deegree.protocol.wcs.WCSConstants.VERSION_100;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
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
import org.deegree.services.wcs.model.Grid;

/**
 * This is a kvp adapter for WCS 1.0.0 GetCoverage requests.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetCoverage100KVPAdapter {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( GetCoverage100KVPAdapter.class );

    /**
     * WCS accepts additional arbitrary named parameters that define range subsets, so we need to know which keys are
     * reserved
     */
    private static final Set<String> RESERVED_KEYS;

    private static final Map<String, InterpolationType> SUPPORTED_INTERPOLATIONS = new HashMap<String, InterpolationType>();

    static {
        RESERVED_KEYS = new HashSet<String>();
        String[] reservedKeys = new String[] { "SERVICE", "VERSION", "REQUEST", "COVERAGE", "CRS", "RESPONSE_CRS",
                                              "BBOX", "TIME", "WIDTH", "HEIGHT", "DEPTH", "RESX", "RESY", "RESZ",
                                              "INTERPOLATION", "FORMAT", "EXCEPTIONS" };
        RESERVED_KEYS.addAll( Arrays.asList( reservedKeys ) );

        SUPPORTED_INTERPOLATIONS.put( "NEAREST-NEIGHBOR", InterpolationType.NEAREST_NEIGHBOR );
        SUPPORTED_INTERPOLATIONS.put( "BILINEAR", InterpolationType.BILINEAR );
    }

    /**
     * Parse a 1.0.0 WCS KVP request
     * 
     * @param kvp
     * @return the parsed GetCoverage request
     * @throws OWSException
     */
    public static GetCoverage parse( Map<String, String> kvp )
                            throws OWSException {

        Version version = parseVersion( kvp );
        String coverage = getRequired( kvp, "COVERAGE" );
        if ( "".equals( coverage.trim() ) ) {
            throw new OWSException( "A coverage name must be suplied.", OWSException.MISSING_PARAMETER_VALUE,
                                    "coverage" );
        }
        String requestCRS = getRequired( kvp, "CRS" );
        String responseCRS = getDefault( kvp, "RESPONSE_CRS", requestCRS );
        String bbox = kvp.get( "BBOX" );
        if ( bbox == null ) {
            String time = kvp.get( "TIME" );
            if ( time != null ) {
                throw new OWSException( "The 'time' parameter is currently not supported in this WCS implementation.",
                                        OWSException.OPERATION_NOT_SUPPORTED );
            }
            throw new OWSException( "One of 'TIME' or 'BBOX' is mandatory ('TIME' is currently not supported).",
                                    OWSException.MISSING_PARAMETER_VALUE );
        }

        String interpolation = getDefault( kvp, "INTERPOLATION", InterpolationType.NEAREST_NEIGHBOR.name() );

        String format = getRequired( kvp, "FORMAT" );
        if ( format.startsWith( "image/" ) ) {
            format = format.substring( "image/".length() );
        }
        RangeSet rangeParameters = getRangeParameters( kvp );

        String exceptionFormat = getDefault( kvp, "EXCEPTIONS", WCSConstants.EXCEPTION_FORMAT_100 );

        CRS requestedCRS = new CRS( requestCRS );

        Envelope requestedEnvelope = parseEnvelope( bbox, requestedCRS );
        Envelope targetEnvelope;
        try {
            targetEnvelope = GeometryUtils.createConvertedEnvelope( requestedEnvelope, new CRS( responseCRS ) );
        } catch ( TransformationException e ) {
            throw new OWSException( "Specified envelope can not be converted into the response CRS: "
                                    + e.getLocalizedMessage(), OWSException.OPERATION_NOT_SUPPORTED, "responseCRS" );
        }

        Grid outputGrid = getGrid( kvp, targetEnvelope );

        return new GetCoverage( version, coverage, targetEnvelope, responseCRS, format, outputGrid, interpolation,
                                exceptionFormat, false, rangeParameters );
    }

    private static double[] parseEnvelopeCoords( String bbox )
                            throws OWSException {
        String[] parts = bbox.split( "," );
        double[] coords = new double[parts.length];
        try {
            for ( int i = 0; i < coords.length; i++ ) {
                coords[i] = Double.parseDouble( parts[i] );
            }
        } catch ( NumberFormatException e ) {
            throw new OWSException( "invalid bbox: " + bbox, OWSException.INVALID_PARAMETER_VALUE, "bbox" );
        }
        return coords;
    }

    private static Envelope parseEnvelope( String bbox, CRS crs )
                            throws OWSException {
        double[] coords = parseEnvelopeCoords( bbox );
        GeometryFactory geomFactory = new GeometryFactory();
        if ( coords.length == 4 ) {
            return geomFactory.createEnvelope( new double[] { coords[0], coords[1] }, new double[] { coords[2],
                                                                                                    coords[3] }, crs );
        }
        if ( coords.length == 6 ) {

            // rb: the minz,maxz values are at position 4,5 of the bbox (WTF).
            return geomFactory.createEnvelope( new double[] { coords[0], coords[1], coords[4] },
                                               new double[] { coords[2], coords[3], coords[5] }, crs );
        }
        throw new OWSException(
                                "Invalid bbox, the given bbox may only have 4 (minx,miny,maxx,maxy) or 6 (minx,miny,maxx,maxy,minz,maxz) parameters.",
                                OWSException.INVALID_PARAMETER_VALUE, "bbox" );
    }

    /**
     * Return a map with all but the reserved keys.
     */
    private static RangeSet getRangeParameters( Map<String, String> kvp ) {
        Set<String> paramKeys = new HashSet<String>( kvp.keySet() ); // copy, otherwise kvp will be altered
        paramKeys.removeAll( RESERVED_KEYS );

        List<AxisSubset> axis = new ArrayList<AxisSubset>( paramKeys.size() );
        for ( String axisName : paramKeys ) {
            if ( axisName != null ) {
                String values = kvp.get( axisName );
                List<SingleValue<?>> singleValues = new ArrayList<SingleValue<?>>();
                List<Interval<?, ?>> intervals = new ArrayList<Interval<?, ?>>();
                if ( values != null ) {
                    if ( values.contains( "/" ) ) {
                        List<Interval<?, ?>> parseIntervals = parseIntervals( values, ValueType.Void );
                        if ( !parseIntervals.isEmpty() ) {
                            intervals.addAll( parseIntervals );
                        }
                    } else {
                        String[] split = values.split( "," );
                        for ( String sv : split ) {
                            if ( sv != null ) {
                                singleValues.add( SingleValue.createFromString( ValueType.Void.name(), sv ) );
                            }
                        }

                    }
                }
                axis.add( new AxisSubset( axisName, null, intervals, singleValues ) );

            }
        }

        return new RangeSet( axis );
    }

    /**
     * Parses the String and creates intervals, following values are allowed.
     * <ul>
     * <li>min/max[/res]</li>
     * <li>min1/max1[/res1],min2/max2[/res2],...</li>
     * </ul>
     * 
     * @param intervals
     *            to create the interval(s) from.
     * @param type
     *            to use
     * @return a list of intervals or the empty list if the given string does not contain any wcs interval definitions.
     */
    public static List<Interval<?, ?>> parseIntervals( String intervals, ValueType type ) {
        List<Interval<?, ?>> result = new LinkedList<Interval<?, ?>>();
        if ( intervals.contains( "," ) ) {
            // something like WAVELENGTH=300/600,400/800,2000/77999
            String[] split = intervals.split( "," );
            for ( String inter : split ) {
                if ( inter != null ) {
                    Interval<?, ?> in = parseInterval( inter, type );
                    if ( in != null ) {
                        result.add( in );
                    }
                }
            }
        } else {
            Interval<?, ?> in = parseInterval( intervals, type );
            if ( in != null ) {
                result.add( in );
            }
        }
        return result;
    }

    /**
     * Get the interval defined by the given String.
     * 
     * @param interval
     * @return
     */
    private static Interval<?, ?> parseInterval( String interval, ValueType type ) {
        Interval<?, ?> result = null;
        String[] split = interval.split( "/" );
        if ( split.length == 2 || split.length == 3 ) {
            // should be min/max/res
            String min = split[0];
            String max = split[1];
            String res = null;
            if ( split.length == 3 ) {
                res = split[2];
            }
            SingleValue<?> spacing = res == null ? null : SingleValue.createFromString( ValueType.Void.name(), res );
            result = Interval.createFromStrings( type.toString(), min, max, Closure.closed, null, false, spacing );
        } else {
            LOG.warn( "Given intervall: " + interval + " has not enough values, maybe a default value is ment?" );
        }
        return result;
    }

    private static Grid getGrid( Map<String, String> kvp, Envelope targetEnvelope )
                            throws OWSException {
        try {
            if ( kvp.containsKey( "WIDTH" ) && kvp.containsKey( "HEIGHT" ) ) {
                int width = KVPUtils.getRequiredInt( kvp, "WIDTH" );
                int height = KVPUtils.getRequiredInt( kvp, "HEIGHT" );
                int depth = KVPUtils.getInt( kvp, "DEPTH", Integer.MIN_VALUE );
                if ( depth != Integer.MIN_VALUE && targetEnvelope.getCoordinateDimension() != 3 ) {
                    LOG.warn( "Depth is requested but the target envelope does not have 3 dimensions, only using width and height." );
                    depth = Integer.MIN_VALUE;
                }
                return Grid.fromSize( width, height, depth, targetEnvelope );
            }

            if ( kvp.containsKey( "RESX" ) && kvp.containsKey( "RESY" ) ) {
                double resx = KVPUtils.getRequiredDouble( kvp, "RESX" );
                double resy = KVPUtils.getRequiredDouble( kvp, "RESY" );
                double resz = KVPUtils.getDefaultDouble( kvp, "RESZ", Double.MIN_VALUE );
                if ( resz != Double.MIN_VALUE && targetEnvelope.getCoordinateDimension() != 3 ) {
                    LOG.warn( "RESZ is requested but the target envelope does not have 3 dimensions, only using resx and resy." );
                    resz = Double.MIN_VALUE;
                }
                return Grid.fromRes( resx, resy, resz, targetEnvelope );
            }
        } catch ( MissingParameterException e ) {
            throw new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE );
        } catch ( InvalidParameterValueException e ) {
            throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE );
        }

        throw new OWSException( "Missing required WIDTH/HEIGHT or RESX/RESY pair.",
                                OWSException.MISSING_PARAMETER_VALUE );

    }

    private static Version parseVersion( Map<String, String> kvp )
                            throws OWSException {
        String version = KVPUtils.getRequired( kvp, "VERSION" );
        if ( !Version.parseVersion( version ).equals( VERSION_100 ) ) {
            throw new OWSException( "Version must be: " + VERSION_100.toString(), OWSException.INVALID_PARAMETER_VALUE );
        }
        return VERSION_100;
    }
}
