//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.rendering.r3d.multiresolution.crit;

import org.deegree.commons.utils.math.VectorUtils;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.multiresolution.Arc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LODCriterion} for specifying LODs that are optimized for perspective visualization.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class ViewFrustumCrit implements LODCriterion {

    private static final Logger LOG = LoggerFactory.getLogger( ViewFrustumCrit.class );

    private final float pixelError;

    private final int screenX;

    private final int screenY;

    private final ViewFrustum viewRegion;
    
    private final float zScale;

    /**
     * Creates a new {@link ViewFrustumCrit} instance.
     * 
     * @param viewParams
     *            specifies the visible space volume (viewer position, view direction, etc.)
     * @param maxPixelError
     *            maximum tolerable screen space error in pixels (in the rendered image)
     * @param zScale
     *            scaling factor applied to z values of the mesh geometry (and bounding boxes)
     */
    public ViewFrustumCrit( ViewParams viewParams, float maxPixelError, float zScale ) {
        this.pixelError = maxPixelError;
        this.screenX = viewParams.getScreenPixelsX();
        this.screenY = viewParams.getScreenPixelsY();
        this.viewRegion = viewParams.getViewFrustum();
        this.zScale = zScale;
    }

    /**
     * Returns true, iff the region associated with the arc is inside the view frustum volume and the estimated screen
     * projection error is greater than the maximum tolerable error.
     * 
     * @param arc
     *            arc to be checked
     * @return true, iff the arc's region is inside the view frustum and the estimated screen projection error is
     *         greater than the maximum tolerable error
     */
    @Override
    public boolean needsRefinement( Arc arc ) {

        // step 1: only refine a region if it's inside the view volume
        float[][] scaledBBox = arc.getBBox();
        scaledBBox[0][2] *= zScale;
        scaledBBox[1][2] *= zScale;
        if ( !viewRegion.intersects( scaledBBox ) ) {
            return false;
        }

        // step 2: only refine if the region currently violates the screen-space constraint
        float[] eyePos = new float[3];
        eyePos[0] = (float) viewRegion.getEyePos().x;
        eyePos[1] = (float) viewRegion.getEyePos().y;
        eyePos[2] = (float) viewRegion.getEyePos().z;
        float dist = VectorUtils.getDistance( scaledBBox, eyePos ); 
            
        float projectionFactor = estimatePixelSizeForSpaceUnit( dist );
        float maxEdgeLen = pixelError * 1.0f;
        float edgeLen = getEdgeLen( arc ) * projectionFactor;

        LOG.debug( "Checking region (DAG arc) for refinement. Arc error=" + arc.getGeometricError() + ", distance="
                   + dist + ", projectionFactor=" + projectionFactor );
        LOG.debug( "Max acceptable edge length (pixels)=" + maxEdgeLen + ", estimated edge length=" + edgeLen );
        return edgeLen > maxEdgeLen;
    }

    private static final float SQR_2 = (float) Math.sqrt( 2 );

    private float getEdgeLen( Arc arc ) {

        float error = arc.getGeometricError();

        if ( error % 1 == 0 ) {
            return (float) Math.pow( 2, error / 2 );
        }
        return (float) Math.pow( 2, ( error - 1 ) / 2 ) * SQR_2;
    }

    /**
     * Returns a guaranteed upper bound for the size that a world-space unit (e.g. a line with length 1) has in pixels
     * after perspective projection, i.e. in pixels on the screen.
     * 
     * @param dist
     *            distance of the object (from the point-of-view)
     * @return maximum number of pixels that an object of size 1 will cover
     */
    private float estimatePixelSizeForSpaceUnit( float dist ) {
        float h = 2.0f * dist * (float) Math.tan( Math.toRadians( viewRegion.getFOVY() * 0.5f ) );
        return screenY / h;
    }

    @Override
    public String toString() {
        return "frustum=" + viewRegion + ",pixelsX=" + screenX + ",pixelsY=" + screenY + ",maxError=" + pixelError;
    }
}
