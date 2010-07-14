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

package org.deegree.cs.transformations.coordinate;

import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>ProjectionTransform</code> class wraps the access to a projection, by calling it's doProjection.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about axis of the projection as well as the used projection and the incoming ordinates.")
public class ProjectionTransform extends Transformation {

    private static Logger LOG = LoggerFactory.getLogger( ProjectionTransform.class );

    private Projection projection;

    private boolean swapAxis = false;

    /**
     * @param projectedCRS
     *            The crs containing a projection.
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public ProjectionTransform( ProjectedCRS projectedCRS, CRSIdentifiable id ) {
        super( projectedCRS.getGeographicCRS(), projectedCRS, id );
        this.projection = projectedCRS.getProjection();
        swapAxis = checkAxisOrientation( projectedCRS.getAxis() );
    }

    /**
     * @param axis
     * @return
     */
    private boolean checkAxisOrientation( Axis[] axis ) {
        boolean result = false;
        if ( axis == null || axis.length != 2 ) {
            result = false;
        } else {
            Axis first = axis[0];
            Axis second = axis[1];
            LOG.debug( "First projected crs Axis: " + first );
            LOG.debug( "Second projected crs Axis: " + second );
            if ( first != null && second != null ) {
                if ( Axis.AO_WEST == Math.abs( second.getOrientation() ) ) {
                    result = true;
                    if ( Axis.AO_NORTH != Math.abs( first.getOrientation() ) ) {
                        LOG.warn( "The given projection uses a second axis which is not mappable (  " + second
                                  + ") please check your configuration, assuming y, x axis-order." );
                    }
                }
            }
        }
        LOG.debug( "Incoming ordinates will" + ( ( result ) ? " " : " not " ) + "be swapped." );
        return result;
    }

    /**
     * @param projectedCRS
     *            The crs containing a projection.
     */
    public ProjectionTransform( ProjectedCRS projectedCRS ) {
        this(
              projectedCRS,
              new CRSIdentifiable(
                                   CRSCodeType.valueOf( createFromTo(
                                                                      projectedCRS.getGeographicCRS().getCode().toString(),
                                                                      projectedCRS.getCode().toString() ) ) ) );
    }

    @Override
    public List<Point3d> doTransform( List<Point3d> srcPts )
                            throws TransformationException {
        // List<Point3d> result = new ArrayList<Point3d>( srcPts.size() );
        if ( LOG.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder( isInverseTransform() ? "An inverse" : "A" );
            sb.append( " projection transform with incoming points: " );
            sb.append( srcPts );
            sb.append( " and following projection: " );
            sb.append( projection.getImplementationName() );
            LOG.debug( sb.toString() );
        }
        TransformationException trans = new TransformationException( srcPts.size() );
        if ( isInverseTransform() ) {
            doInverseTransform( srcPts, trans );
        } else {
            doForwardTransform( srcPts, trans );
        }
        if ( !trans.getTransformErrors().isEmpty() ) {
            trans.setTransformedPoints( srcPts );
            throw trans;
        }
        return srcPts;
    }

    /**
     * @param srcPts
     * @param trans
     */
    private void doForwardTransform( List<Point3d> srcPts, TransformationException trans ) {
        int i = 0;
        if ( swapAxis ) {
            for ( Point3d p : srcPts ) {
                try {
                    Point2d tmp = projection.doProjection( p.y, p.x );
                    p.x = tmp.y;
                    p.y = tmp.x;
                } catch ( ProjectionException e ) {
                    trans.setTransformError( i, e.getMessage() );
                }
                ++i;
            }
        } else {
            for ( Point3d p : srcPts ) {
                try {
                    Point2d tmp = projection.doProjection( p.x, p.y );
                    p.x = tmp.x;
                    p.y = tmp.y;
                } catch ( ProjectionException e ) {
                    trans.setTransformError( i, e.getMessage() );
                }
                ++i;
            }
        }

    }

    /**
     * @param srcPts
     */
    private void doInverseTransform( List<Point3d> srcPts, TransformationException trans ) {

        int i = 0;
        if ( swapAxis ) {
            for ( Point3d p : srcPts ) {
                try {
                    Point2d tmp = projection.doInverseProjection( p.y, p.x );
                    p.x = tmp.y;
                    p.y = tmp.x;
                } catch ( ProjectionException e ) {
                    trans.setTransformError( i, e.getMessage() );
                }
                ++i;
            }
        } else {
            for ( Point3d p : srcPts ) {
                try {
                    Point2d tmp = projection.doInverseProjection( p.x, p.y );
                    p.x = tmp.x;
                    p.y = tmp.y;
                } catch ( ProjectionException e ) {
                    trans.setTransformError( i, e.getMessage() );
                }
                ++i;
            }
        }
    }

    @Override
    public boolean isIdentity() {
        // a projection cannot be an identity it doesn't make a lot of sense.
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " - Projection: " + projection.getImplementationName();
    }

    @Override
    public String getImplementationName() {
        return "Projection-Transform";
    }

}
