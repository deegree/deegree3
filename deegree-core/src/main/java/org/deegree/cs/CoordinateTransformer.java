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
package org.deegree.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for transforming coordinates to new a coordinate reference systems.
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information about the transformation of a list of ordinates.")
public class CoordinateTransformer extends Transformer {

    private static Logger LOG_TRANSFORM = LoggerFactory.getLogger( CoordinateTransformer.class.getCanonicalName()
                                                                   + ".TransformLogger" );

    private static Logger LOG = LoggerFactory.getLogger( CoordinateTransformer.class );

    /**
     * Creates a new CoordinateTransformer object.
     * 
     * @param targetCRS
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    public CoordinateTransformer( CoordinateSystem targetCRS ) throws IllegalArgumentException {
        super( targetCRS );
    }

    /**
     * Creates a new CoordinateTransformer object, with the given id as the target CRS.
     * 
     * @param targetCRS
     *            an identifier to which all other CRS's shall be transformed.
     * @throws UnknownCRSException
     *             if the given crs name could not be mapped to a valid (configured) crs.
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    public CoordinateTransformer( String targetCRS ) throws UnknownCRSException, IllegalArgumentException {
        super( targetCRS );
    }

    /**
     * Creates a new CoordinateTransformer object, with the given id as the target CRS.
     * 
     * @param transformation
     *            to be used.
     * @throws IllegalArgumentException
     *             if the given parameter is null.
     */
    public CoordinateTransformer( Transformation transformation ) throws IllegalArgumentException {
        super( transformation );
    }

    /**
     * Transforms all points to the CoordinateTransformer's coordinate system.
     * 
     * @param sourceCRS
     *            in which the given points are referenced.
     * @param points
     *            to transform.
     * @return a list of transformed point3d's or an empty list if something went wrong, never <code>null</code>
     * @throws TransformationException
     *             if no transformation could be created for the given source and target crs.
     * @throws IllegalArgumentException
     *             if the sourceCRS is <code>null</code>
     */
    public List<Point3d> transform( CoordinateSystem sourceCRS, List<Point3d> points )
                            throws TransformationException, IllegalArgumentException {

        List<Point3d> copy = new ArrayList<Point3d>( points.size() );
        for ( Point3d point : points ) {
            copy.add( (Point3d) point.clone() );
        }

        if ( copy == null || copy.size() == 0 ) {
            return new ArrayList<Point3d>();
        }
        Transformation trans = createCRSTransformation( sourceCRS );
        if ( TransformationFactory.isIdentity( trans ) ) {
            return copy;
        }

        List<Point3d> result = new ArrayList<Point3d>( copy.size() );
        TransformationException exception = null;
        try {
            result = trans.doTransform( copy );
        } catch ( TransformationException te ) {
            List<Point3d> tResult = te.getTransformedPoints();
            if ( tResult != null && tResult.size() > 0 ) {
                result = tResult;
            }
            exception = te;
        }

        if ( LOG_TRANSFORM.isDebugEnabled() || LOG.isDebugEnabled() ) {
            Map<Integer, String> errorMessages = null;
            if ( exception != null ) {
                errorMessages = exception.getTransformErrors();
            }
            for ( int i = 0; i < copy.size(); ++i ) {
                StringBuilder sb = new StringBuilder( 1000 );
                Point3d coord = copy.get( i );
                Point3d resultCoord = result.get( i );
                if ( resultCoord == null ) {
                    resultCoord = new Point3d( coord );
                }
                sb.append( trans.getSourceCRS().getCode() );
                sb.append( ";" );
                sb.append( coord.x );
                sb.append( ";" );
                sb.append( coord.y );
                sb.append( ";" );
                if ( trans.getSourceDimension() == 3 ) {
                    sb.append( coord.z );
                    sb.append( ";" );
                }
                sb.append( trans.getTargetCRS().getCode() );
                sb.append( ";" );
                sb.append( resultCoord.x );
                sb.append( ";" );
                sb.append( resultCoord.y );
                sb.append( ";" );
                if ( trans.getTargetDimension() == 3 ) {
                    sb.append( resultCoord.z );
                    sb.append( ";" );
                }
                String successString = "Success";
                if ( errorMessages != null ) {
                    String tmp = errorMessages.get( Integer.valueOf( i ) );
                    if ( tmp != null && !"".equals( tmp.trim() ) ) {
                        successString = tmp;
                    }
                }
                sb.append( successString );
                LOG_TRANSFORM.debug( sb.toString() );
                LOG.debug( sb.toString() );
            }
        }
        if ( result == null ) {
            result = new ArrayList<Point3d>();
        } else if ( sourceCRS.getDimension() == 2 && getTargetCRS().getDimension() == 2 ) {
            // pass the 3rd coordinate if dimension of source and target CRS is 2
            for ( int j = 0; j < result.size(); j++ ) {
                result.get( j ).z = points.get( j ).z;
            }
        }
        return result;
    }

    /**
     * Transforms a given coordinate into the CoordinateTransformer's coordinate system.
     * 
     * @param sourceCRS
     *            crs of the input coordinate, must not be <code>null</code>
     * @param input
     *            input coordinate, must not be <code>null</code> and array length must match the number of dimensions
     *            of <code>sourceCRS</code>
     * @param out
     *            output coordinate, used to store the transformed ordinates, must not be <code>null</code> and array
     *            length must match the number of dimensions of the target crs
     * @return transformed coordinate, this is the same instance as <code>out</code>
     * @throws TransformationException
     * @throws IllegalArgumentException
     */
    public double[] transform( CoordinateSystem sourceCRS, double[] input, double[] out )
                            throws IllegalArgumentException, TransformationException {
        Transformation trans = createCRSTransformation( sourceCRS );

        trans.doTransform( input, 0, out, 0, input.length );
        return out;
    }
}
