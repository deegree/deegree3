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

    private int screenSizeX = -1;

    private int screenSizeY = -1;

    public ViewParams( Point3d eye, Point3d lookingAt, Vector3d viewerUp, double fovy, double zNear, double zFar ) {
        this.vf = new ViewFrustum( fovy, 1.0, zNear, zFar, eye, lookingAt, viewerUp );
    }

    /**
     * Creates a new {@link ViewParams} instance from the given parameters.
     * 
     * @param vf
     *            view frustum (volume visible to the viewer)
     * @param screenSizeX
     *            number of pixels of the projected image in the x direction
     * @param screenSizeY
     *            number of pixels of the projected image in the y direction
     */
    public ViewParams( ViewFrustum vf, int screenSizeX, int screenSizeY ) {
        this.vf = vf;
        this.screenSizeX = screenSizeX;
        this.screenSizeY = screenSizeY;
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
        return screenSizeX;
    }

    /**
     * Returns the number of pixels of the projected image in y direction.
     * 
     * @return number of pixels in y direction
     */
    public int getScreenPixelsY() {
        return screenSizeY;
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
        return screenSizeY / h;
    }    
    
    public void setScreenDimensions( int width, int height ) {
        screenSizeX = width;
        screenSizeY = height;
        double aspect = (double) width / height;
        vf.setPerspectiveParams( vf.getFOVY(), aspect, vf.getZNear(), vf.getZFar() );
        vf.setCameraParams( vf.getEyePos(), vf.getLookingAt(), vf.getViewerUp() );
    }

    @Override
    public String toString() {
        String s = "{frustum=" + vf + ",pixelsX=" + screenSizeX + ",pixelsY=" + screenSizeY + "}";
        return s;
    }
}
