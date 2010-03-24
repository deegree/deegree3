//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.rendering.r3d.multiresolution.crit;

import java.util.Arrays;

import org.deegree.commons.utils.math.VectorUtils;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.multiresolution.Arc;
import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TextureManager;

/**
 * {@link LODCriterion} for specifying LODs that are optimized for perspective rendering.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class ViewFrustumCrit implements LODCriterion {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( ViewFrustumCrit.class );

    private final float maxPixelError;

    private final ViewParams viewParams;

    private final ViewFrustum viewRegion;

    private final float zScale;

    private int maxTextureSize;

    private final TextureManager[] textureManagers;

    private final float maxProjectedTexelSize;

    /**
     * Creates a new {@link ViewFrustumCrit} instance.
     * 
     * @param viewParams
     *            specifies the visible space volume (viewer position, view direction, etc.)
     * @param maxPixelError
     *            maximum tolerable screen space error in pixels (in the rendered image)
     * @param zScale
     *            scaling factor applied to z values of the mesh geometry (and bounding boxes)
     * @param maxTextureSize
     *            maximum texture size (all fragments of the extracted LOD must have a size that is small enough, so
     *            they can be textured regarding this value and the <code>finestTextureResolution</code> parameter
     * @param textureManagers
     *            texture managers that will be used to provide the textures
     * @param maxProjectedTexelSize
     */
    public ViewFrustumCrit( ViewParams viewParams, float maxPixelError, float zScale, int maxTextureSize,
                            TextureManager[] textureManagers, float maxProjectedTexelSize ) {
        this.maxPixelError = maxPixelError;
        this.viewParams = viewParams;
        this.viewRegion = viewParams.getViewFrustum();
        this.zScale = zScale;
        this.maxTextureSize = maxTextureSize;
        this.textureManagers = textureManagers;
        this.maxProjectedTexelSize = maxProjectedTexelSize;
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

        // step 1: only refine if region is inside the view volume
        if ( !isInViewVolume( arc ) ) {
            return true;
        }
        if ( !isScreenSpaceErrorAcceptable( arc ) ) {
            // step 2: only refine if the region currently violates the screen-space constraint
            return true;
        }

        // step 3: refine, if fragment size is not suitable for texturing (too large)
        return !isTexturable( arc );
    }

    /**
     * Checks whether all fragments denoted by the given {@link Arc} are texturable with respect to the max texture size
     * and texture resolutions.
     * 
     * @param arc
     * @return true, if all are texturable, false otherwise (at least one is not texturable)
     */
    private boolean isTexturable( Arc arc ) {
        if ( textureManagers.length == 0 ) {
            return true;
        }
        for ( MeshFragment fragment : arc.getFragments() ) {
            if ( !isTexturable( fragment ) ) {
                return false;
            }
        }
        return true;
    }

    private boolean isTexturable( MeshFragment fragment ) {
        float[][] scaledBBox = new float[2][3];
        scaledBBox[0] = Arrays.copyOf( fragment.bbox[0], 3 );
        scaledBBox[1] = Arrays.copyOf( fragment.bbox[1], 3 );
        scaledBBox[0][2] *= zScale;
        scaledBBox[1][2] *= zScale;

        float[] eyePos = new float[3];
        eyePos[0] = (float) viewRegion.getEyePos().x;
        eyePos[1] = (float) viewRegion.getEyePos().y;
        eyePos[2] = (float) viewRegion.getEyePos().z;

        float dist = VectorUtils.getDistance( scaledBBox, eyePos );
        double pixelSize = viewParams.estimatePixelSizeForSpaceUnit( dist );
        double metersPerPixel = maxProjectedTexelSize / pixelSize;
        double resolution = getFinestTextureResolution( metersPerPixel );
        if ( resolution <= 0.00001 ) {
            resolution = 0.00001;
        }
        double textureSize = getMaxSideLen( fragment ) / resolution;
        LOG.debug( "Side len: " + getMaxSideLen( fragment ) + ", resolution: " + resolution + ", texture size: "
                   + textureSize );
        if ( textureSize > maxTextureSize ) {
            LOG.debug( "Side len: " + getMaxSideLen( fragment ) + ", resolution: " + resolution + ", texture size: "
                       + textureSize );
        } else {
            LOG.debug( "No refinement needed, Side len: " + getMaxSideLen( fragment ) + ", resolution: " + resolution
                       + ", texture size: " + textureSize );
        }

        return textureSize <= maxTextureSize;
    }

    private double getFinestTextureResolution( double requiredResolution ) {
        double res = Double.MAX_VALUE;
        for ( TextureManager texManager : textureManagers ) {
            double matchingRes = texManager.getMatchingResolution( requiredResolution );
            if ( !Double.isNaN( matchingRes ) && matchingRes < res ) {
                res = matchingRes;
            }
        }
        // System.out.println( "Crit, finest res: " + res );
        return res;
    }

    private float getMaxSideLen( MeshFragment fragment ) {
        float width = fragment.bbox[1][0] - fragment.bbox[0][0];
        float height = fragment.bbox[1][1] - fragment.bbox[0][1];
        return width > height ? width : height;
    }

    private boolean isInViewVolume( Arc arc ) {
        float[][] scaledBBox = arc.getBBox();
        scaledBBox[0][2] *= zScale;
        scaledBBox[1][2] *= zScale;
        return viewRegion.intersects( scaledBBox );
    }

    /**
     * Checks whether the screen-space error (after perspective projection) introduced by the fragments of the
     * {@link Arc} is acceptable.
     * 
     * @param arc
     * @return true, if all fragments are fine
     */
    private boolean isScreenSpaceErrorAcceptable( Arc arc ) {

        float[][] scaledBBox = arc.getBBox();
        scaledBBox[0][2] *= zScale;
        scaledBBox[1][2] *= zScale;

        float[] eyePos = new float[3];
        eyePos[0] = (float) viewRegion.getEyePos().x;
        eyePos[1] = (float) viewRegion.getEyePos().y;
        eyePos[2] = (float) viewRegion.getEyePos().z;

        float dist = VectorUtils.getDistance( scaledBBox, eyePos );
        double projectionFactor = viewParams.estimatePixelSizeForSpaceUnit( dist );
        double screenError = projectionFactor * arc.geometricError;
        // System.out.println ("error: " + arc.geometryError);
        // System.out.println ("screen error: " + screenError);
        return screenError <= maxPixelError;
    }

    @Override
    public String toString() {
        return "frustum=" + viewRegion + ",maxError=" + maxPixelError;
    }
}
