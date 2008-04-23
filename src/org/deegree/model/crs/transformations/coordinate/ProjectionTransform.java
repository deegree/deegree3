//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.model.crs.transformations.coordinate;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.exceptions.ProjectionException;
import org.deegree.model.crs.exceptions.TransformationException;
import org.deegree.model.crs.projections.Projection;

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

public class ProjectionTransform extends CRSTransformation {

    private static Log LOG = LogFactory.getLog( ProjectionTransform.class );

    private Projection projection;

    /**
     * @param projectedCRS
     *            The crs containing a projection.
     */
    public ProjectionTransform( ProjectedCRS projectedCRS ) {
        super( projectedCRS.getGeographicCRS(), projectedCRS );
        this.projection = projectedCRS.getProjection();
    }

    @Override
    public List<Point3d> doTransform( List<Point3d> srcPts )
                            throws TransformationException {
        List<Point3d> result = new ArrayList<Point3d>(srcPts.size());
        if ( LOG.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder( isInverseTransform() ? "An inverse" : "A" );
            sb.append( " projection transform with incoming points: " );
            sb.append( srcPts );
            sb.append( " and following projection: " );
            sb.append( projection.getName() );
            LOG.debug( sb.toString() );
        }
        TransformationException trans = new TransformationException( srcPts.size() );
        if ( isInverseTransform() ) {
            int i = 0;
            for ( Point3d p : srcPts ) {
                try {
                    Point2d tmp = projection.doInverseProjection( p.x, p.y );
                    result.add( new Point3d( tmp.x, tmp.y, p.z ) );
                } catch ( ProjectionException e ) {
                    trans.setTransformError( i, e.getMessage() );
                    result.add( p );
                }
                ++i;
            }
        } else {
            int i = 0;
            for ( Point3d p : srcPts ) {
                try {
                    Point2d tmp = projection.doProjection( p.x, p.y );
                    result.add( new Point3d( tmp.x, tmp.y, p.z ) );
                } catch ( ProjectionException e ) {
                    trans.setTransformError( i, e.getMessage() );
                    result.add( p );
                }
                ++i;
            }
        }
        if ( !trans.getTransformErrors().isEmpty() ) {
            trans.setTransformedPoints( result );
            throw trans;
        }
        return result;
    }

    @Override
    public boolean isIdentity() {
        // a projection cannot be an identity it doesn't make a lot of sense.
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " - Projection: " + projection.getName();
    }

    @Override
    public String getName() {
        return "Projection-Transform";
    }

}
