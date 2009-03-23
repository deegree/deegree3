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

    private Frustum vf;

    private int screenSizeX;

    private int screenSizeY;

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
    public ViewParams( Frustum vf, int screenSizeX, int screenSizeY ) {
        this.vf = vf;
        this.screenSizeX = screenSizeX;
        this.screenSizeY = screenSizeY;
    }

    /**
     * Returns the view frustum (volume visible to the viewer).
     * 
     * @return view frustum
     */
    public Frustum getViewFrustum() {
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
}
