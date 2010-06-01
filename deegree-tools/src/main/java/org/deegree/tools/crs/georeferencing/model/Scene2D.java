//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.tools.crs.georeferencing.model;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.vecmath.Point2d;

import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.RasterIOOptions;

/**
 * Base interface for the model layer
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Scene2D {

    /**
     * Returns the requested image.
     * 
     * @return
     */
    public BufferedImage getGeneratedImage();

    /**
     * Resets all of the variables to initial value.
     */
    public void reset();

    /**
     * Initializes the image generation from a rasterinput. There will be generated the information that is needed for
     * every image generation like the {@link RasterGeoReference}.
     * 
     * @param options
     *            for the request, must not be <Code>null</Code>
     * @param bounds
     *            of the size of the scene, must not be <Code>null</Code>
     */
    public void init( RasterIOOptions options, Rectangle bounds );

    /**
     * 
     * @param startPoint
     *            can be <Code>null</Code>. If not specified the defaultValue of initialisation will be taken.
     * @return
     */
    public BufferedImage generateImage( Point2d startPoint );

    /**
     * To set the resolution interactively.
     * 
     * @param resolution
     */
    public void setResolution( double resolution );

    public void generatePredictedImage( Point2d changePoint );

    public BufferedImage getPredictedImage();

}
