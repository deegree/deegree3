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

package org.deegree.rendering.r2d;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.utils.Raster2RawData;
import org.slf4j.Logger;

/**
 * <code>Java2DRasterRenderer</code>
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author: aaiordachioaie $
 * 
 * @version $Revision: 19497 $, $Date: 2009-09-11 $
 */
public class Java2DRasterRenderer implements RasterRenderer {

    private static final Logger LOG = getLogger( Java2DRenderer.class );

    private Graphics2D graphics;

    private AffineTransform worldToScreen = new AffineTransform();

    /**
     * @param graphics
     * @param width
     * @param height
     * @param bbox
     */
    public Java2DRasterRenderer( Graphics2D graphics, int width, int height, Envelope bbox ) {
        this.graphics = graphics;

        if ( bbox != null ) {
            double scalex = width / bbox.getSpan0();
            double scaley = height / bbox.getSpan1();

            // we have to flip horizontally, so invert y scale and add the screen height
            worldToScreen.translate( -bbox.getMin().get0() * scalex, bbox.getMin().get1() * scaley + height );
            worldToScreen.scale( scalex, -scaley );

            LOG.debug( "For coordinate transformations, scaling by x = {} and y = {}", scalex, -scaley );
            LOG.trace( "Final transformation was {}", worldToScreen );
        } else {
            LOG.warn( "No envelope given, proceeding with a scale of 1." );
        }
    }

    /**
     * @param graphics
     */
    public Java2DRasterRenderer( Graphics2D graphics ) {
        this.graphics = graphics;
    }

    public void render( RasterStyling styling, AbstractRaster raster ) {
        LOG.trace( "Rendering raster with style..." );
        BufferedImage img = null;
        if ( raster == null ) {
            LOG.warn( "Trying to render null raster." );
            return;
        }
        if ( styling == null ) {
            LOG.warn( "Raster style is null, rendering without style" );
            render( raster );
            return;
        }

        if ( styling.categorize != null || styling.interpolate != null ) {
            LOG.trace( "Creating raster ColorMap..." );
            if ( styling.categorize != null )
                img = styling.categorize.evaluateRaster( raster);
            else if ( styling.interpolate != null )
                img = styling.interpolate.evaluateRaster( raster );
        }

        if ( styling.opacity != 1 ) {
            LOG.debug( "Using opacity: " + styling.opacity );
            graphics.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, (float) styling.opacity ) );
        }

        LOG.trace( "Rendering ..." );
        if ( img != null )
            render( img );
        else
            render( raster );
        LOG.trace( "Done rendering raster." );
    }

    private void render( AbstractRaster raster ) {
        RasterData data = raster.getAsSimpleRaster().getRasterData();
        render( RasterFactory.imageFromRaster( raster ) );
    }

    private void render( BufferedImage img ) {
        graphics.drawImage( img, worldToScreen, null );
    }
}
