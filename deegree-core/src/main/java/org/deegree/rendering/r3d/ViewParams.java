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

package org.deegree.rendering.r3d;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Encapsulates the relevant viewing and projection parameters that are needed for performing view frustum culling and
 * LOD (level-of-detail) adaptation.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class ViewParams {

    private ViewFrustum vf;

    private int projectionWidth = -1;

    private int projectionHeight = -1;

    /**
     *
     * @param eye
     * @param lookingAt
     * @param viewerUp
     * @param fovy
     * @param zNear
     * @param zFar
     */
    public ViewParams( Point3d eye, Point3d lookingAt, Vector3d viewerUp, double fovy, double zNear, double zFar ) {
        this.vf = new ViewFrustum( eye, lookingAt, viewerUp, fovy, 1.0, zNear, zFar );
    }

    /**
     * Creates a new {@link ViewParams} instance from the given parameters.
     *
     * @param vf
     *            view frustum (volume visible to the viewer)
     * @param projectionWidth
     *            number of pixels of the projected image in the x direction
     * @param projectionHeight
     *            number of pixels of the projected image in the y direction
     *
     */
    public ViewParams( ViewFrustum vf, int projectionWidth, int projectionHeight ) {
        this.vf = vf;
        this.projectionWidth = projectionWidth;
        this.projectionHeight = projectionHeight;
    }

    /**
     * Returns the view frustum (volume visible to the viewer).
     *
     * @return view frustum
     */
    public ViewFrustum getViewFrustum() {
        return vf;
    }

    /**
     * Returns the number of pixels of the projected image in x direction.
     *
     * @return number of pixels in x direction
     */
    public int getScreenPixelsX() {
        return projectionWidth;
    }

    /**
     * Returns the number of pixels of the projected image in y direction.
     *
     * @return number of pixels in y direction
     */
    public int getScreenPixelsY() {
        return projectionHeight;
    }

    /**
     * Returns a guaranteed upper bound for the size that a world-space unit (e.g. a line with length 1) has in pixels
     * after perspective projection, i.e. in pixels on the screen.
     *
     * @param dist
     *            distance of the object (from the point-of-view)
     * @return maximum number of pixels that an object of size 1 will cover
     */
    public double estimatePixelSizeForSpaceUnit( double dist ) {
        double h = 2.0 * dist * (float) Math.tan( Math.toRadians( vf.getFOVY() * 0.5f ) );
        return projectionHeight / h;
    }

    /**
     * Set the new projection plane dimensions
     *
     * @param width
     * @param height
     */
    public void setProjectionPlaneDimensions( int width, int height ) {
        projectionWidth = width;
        projectionHeight = height;
        double aspect = (double) width / height;
        vf.setPerspectiveParams( vf.getFOVY(), aspect, vf.getZNear(), vf.getZFar() );
        vf.setCameraParams( vf.getEyePos(), vf.getLookingAt(), vf.getUp() );
    }

    @Override
    public String toString() {
        String s = "{frustum=" + vf + ",pixelsX=" + projectionWidth + ",pixelsY=" + projectionHeight + "}";
        return s;
    }
}
