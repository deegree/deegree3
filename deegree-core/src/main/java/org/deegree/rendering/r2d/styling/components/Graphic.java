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

package org.deegree.rendering.r2d.styling.components;

import static org.deegree.commons.utils.JavaUtils.generateToString;

import java.awt.image.BufferedImage;

import org.deegree.rendering.r2d.styling.Copyable;

/**
 * <code>Graphic</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Graphic implements Copyable<Graphic> {

    /**
     * Default is 1.
     */
    public double opacity = 1;

    /**
     * Default is -1.
     */
    public double size = -1;

    /**
     * Default is 0.
     */
    public double rotation;

    /**
     * Default is 0.5.
     */
    public double anchorPointX = 0.5;

    /**
     * Default is 0.5.
     */
    public double anchorPointY = 0.5;

    /**
     * Default is 0.
     */
    public double displacementX;

    /**
     * Default is 0.
     */
    public double displacementY;

    /**
     * Default is null.
     */
    public BufferedImage image;

    /**
     * Is set to the image's URL. Image may still be null if image format is a vector format.
     */
    public String imageURL;

    /**
     * Default is a default mark.
     */
    public Mark mark = new Mark();

    public Graphic copy() {
        Graphic other = new Graphic();
        other.opacity = opacity;
        other.size = size;
        other.rotation = rotation;
        other.anchorPointX = anchorPointX;
        other.anchorPointY = anchorPointY;
        other.displacementX = displacementX;
        other.displacementY = displacementY;
        other.image = image;
        other.mark = mark.copy();
        return other;
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
