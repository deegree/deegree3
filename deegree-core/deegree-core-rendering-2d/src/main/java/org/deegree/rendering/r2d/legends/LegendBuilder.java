//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.rendering.r2d.legends;

import static java.lang.Math.max;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r2d.Java2DRasterRenderer;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * Responsible for building legend models.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
class LegendBuilder {

    private static final Logger LOG = getLogger( LegendBuilder.class );

    private GeometryFactory geofac = new GeometryFactory();

    private LegendOptions opts;

    LegendBuilder( LegendOptions opts ) {
        this.opts = opts;
    }

    List<LegendItem> prepareLegend( Style style, Graphics2D g, int width, int height ) {
        Pair<Integer, Integer> p = getLegendSize( style );
        Envelope box = geofac.createEnvelope( 0, 0, p.first, p.second, null );
        Java2DRenderer renderer = new Java2DRenderer( g, width, height, box );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );
        Java2DRasterRenderer rasterRenderer = new Java2DRasterRenderer( g, width, height, box );
        return LegendItemBuilder.prepareLegend( style, renderer, textRenderer, rasterRenderer );
    }

    Pair<Integer, Integer> getLegendSize( Style style ) {
        URL url = style.getLegendURL();
        File file = style.getLegendFile();
        if ( url == null ) {
            if ( file != null ) {
                try {
                    url = file.toURI().toURL();
                } catch ( MalformedURLException e ) {
                    // nothing to do
                }
            }
        }
        if ( url != null ) {
            try {
                BufferedImage legend = ImageIO.read( url );
                if ( legend != null ) {
                    return new Pair<Integer, Integer>( legend.getWidth(), legend.getHeight() );
                } else {
                    LOG.warn( "Legend file {} could not be read, using dynamic legend.", url );
                }
            } catch ( IOException e ) {
                LOG.warn( "Legend file {} could not be read, using dynamic legend: {}", url, e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
        Pair<Integer, Integer> res = new Pair<Integer, Integer>( 2 * opts.spacing + opts.baseWidth, 0 );

        for ( LegendItem item : LegendItemBuilder.prepareLegend( style, null, null, null ) ) {
            res.second += item.getHeight() * ( 2 * opts.spacing + opts.baseHeight );
            res.first = max( res.first, item.getMaxWidth( opts ) );
        }

        if ( res.second == 0 ) {
            // prevent >0 * 0 sized images
            res.second = 2 * opts.spacing + opts.baseWidth;
        }

        return res;
    }

}
