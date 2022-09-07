//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d;

import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.rendering.r2d.RenderHelper.renderMarkForFill;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.deegree.commons.utils.TunableParameter;
import org.deegree.commons.utils.TunableParameter;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.UOM;
import org.deegree.style.utils.UomCalculator;

/**
 * Responsible for applying fill stylings to a graphics 2d.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class Java2DFillRenderer {

    private UomCalculator uomCalculator;

    private Graphics2D graphics;

    private SvgRenderer svgRenderer;

    /**
     * Derive undefined image size Strictly according to OGC
     * 
     * SLD 02-070 Cap. 11.3.2 / SE 05-077r4 Cap. 11.3.2
     */
    private boolean strictSize = TunableParameter.get( "deegree.rendering.graphics.size.strict", false );

    private int undefinedImageHeight = TunableParameter.get( "deegree.redering.graphics.height.undefined", 6 );
    
    Java2DFillRenderer( UomCalculator uomCalculator, Graphics2D graphics, SvgRenderer svgRenderer ) {
        this.uomCalculator = uomCalculator;
        this.graphics = graphics;
        this.svgRenderer = svgRenderer;
    }

    void applyGraphicFill( Graphic graphic, UOM uom ) {
        BufferedImage img = graphic.image;
        Rectangle2D.Double rect = getGraphicBounds( graphic, 0.0d, 0.0d, uom );

        if ( img == null && graphic.imageURL != null ) {
            // create unscaled raster
            Rectangle2D.Double r = new Rectangle2D.Double( 0.0d, 0.0d, 0.0d, 0.0 );
            BufferedImage svgImg = svgRenderer.prepareSvg( r, graphic );
            if ( svgImg != null ) {
                img = svgImg;
                rect = getImageBoundsScaled( svgImg, graphic, 0.0d, 0.0d, uom );
            }
        }

        if ( img == null ) {
            int size = round( uomCalculator.considerUOM( graphic.size, uom ) );
            img = renderMarkForFill( graphic.mark, graphic.size < 0 ? 6 : size, uom, graphic.rotation,
                                     graphics != null ? graphics.getRenderingHints() : null );
            graphics.setPaint( new TexturePaint( img, getImageBounds( img, graphic, 0, 0, uom ) ) );
        } else {
            graphics.setPaint( new TexturePaint( img, rect ) );
        }
    }

    void applyFill( Fill fill, UOM uom ) {
        if ( fill == null ) {
            graphics.setPaint( new Color( 0, 0, 0, 0 ) );
            return;
        }

        if ( fill.graphic == null ) {
            graphics.setPaint( fill.color );
        } else {
            applyGraphicFill( fill.graphic, uom );
        }
    }

    /**
     * Returns the graphic bounds for an raster image without scaling.
     * 
     * <p>
     * Anchor point, displacement and uom are taken into account when determining the graphic boundaries.
     * </p>
     */
    Rectangle2D.Double getImageBounds( BufferedImage image, Graphic graphic, double x, double y, UOM uom ) {
        double width, height;
        width = image.getWidth();
        height = image.getHeight();
        double x0 = x - width * graphic.anchorPointX + uomCalculator.considerUOM( graphic.displacementX, uom );
        double y0 = y - height * graphic.anchorPointY + uomCalculator.considerUOM( graphic.displacementY, uom );

        return new Rectangle2D.Double( x0, y0, width, height );
    }
    
    /**
     * Returns the graphic bounds for an raster image with scaling.
     * 
     * <p>
     * Anchor point, displacement and uom are taken into account when determining the graphic boundaries.
     * </p>
     */
    Rectangle2D.Double getImageBoundsScaled( BufferedImage image, Graphic graphic, double x, double y, UOM uom ) {
        double width, height;
        double fac;
        if ( strictSize ) {
            fac = graphic.size / image.getHeight();
        } else {
            fac = graphic.size / Math.max( image.getWidth(), image.getHeight() );
        }
        width = fac * image.getWidth();
        height = fac * image.getHeight();
        double x0 = x - width * graphic.anchorPointX + uomCalculator.considerUOM( graphic.displacementX, uom );
        double y0 = y - height * graphic.anchorPointY + uomCalculator.considerUOM( graphic.displacementY, uom );

        return new Rectangle2D.Double( x0, y0, width, height );
    }

    /**
     * Returns the graphic bounds.
     * 
     * <p>
     * Anchor point, displacement and uom are taken into account when determining the graphic boundaries.
     * </p>
     * 
     * <p>
     * <b>Note:</b> For graphics without raster image a width of zero can be returned
     * </p>
     */
    Rectangle2D.Double getGraphicBounds( Graphic graphic, double x, double y, UOM uom ) {
        double width, height;
        if ( graphic.image != null ) {
            double fac;
            if ( strictSize ) {
                fac = graphic.size / graphic.image.getHeight();
            } else {
                fac = graphic.size / Math.max( graphic.image.getWidth(), graphic.image.getHeight() );
            }
            width = fac * graphic.image.getWidth();
            height = fac * graphic.image.getHeight();
        } else {
            width = strictSize ? 0 : graphic.size;
            height = graphic.size;
        }
        width = uomCalculator.considerUOM( width, uom );
        height = uomCalculator.considerUOM( height, uom );

        if ( height < 0 ) {
            if ( graphic.image == null ) {
                height = undefinedImageHeight;
                width = strictSize ? 0 : height;
            } else {
                width = graphic.image.getWidth();
                height = graphic.image.getHeight();
            }
        }

        double x0 = x - width * graphic.anchorPointX + uomCalculator.considerUOM( graphic.displacementX, uom );
        double y0 = y - height * graphic.anchorPointY + uomCalculator.considerUOM( graphic.displacementY, uom );

        return new Rectangle2D.Double( x0, y0, width, height );
    }

}
