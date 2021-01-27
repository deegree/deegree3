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
package org.deegree.services.wms;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static org.deegree.cs.i18n.Messages.get;
import static org.deegree.style.utils.ImageUtils.postprocessPng8bit;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.utils.Pair;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.wms.ops.GetLegendGraphic;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.style.StyleRef;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;

/**
 * Produces legends for the map service.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class GetLegendHandler {

    private HashMap<Style, Pair<Integer, Integer>> legendSizes = new HashMap<Style, Pair<Integer, Integer>>();

    private HashMap<Style, HashMap<String, BufferedImage>> legends = new HashMap<Style, HashMap<String, BufferedImage>>();

    private MapService service;

    GetLegendHandler( MapService service ) {
        this.service = service;
    }

    BufferedImage getLegend( GetLegendGraphic req ) throws OWSException {
        Legends renderer = new Legends( req.getLegendOptions() );

        Style style = findLegendStyle( req.getLayer(), req.getStyle() );

        Pair<Integer, Integer> size;
        if ( renderer.getLegendOptions().isDefault() ) {
            size = getLegendSize( style );
        } else {
            size = renderer.getLegendSize( style );
        }

        if ( req.getWidth() == -1 ) {
            req.setWidth( size.first );
        }
        if ( req.getHeight() == -1 ) {
            req.setHeight( size.second );
        }

        boolean originalSize = req.getWidth() == size.first && req.getHeight() == size.second
                               && renderer.getLegendOptions().isDefault();

        HashMap<String, BufferedImage> legendMap = legends.get( style );
        if ( originalSize && legendMap != null && legendMap.get( req.getFormat() ) != null ) {
            return legendMap.get( req.getFormat() );
        }
        if ( legendMap == null ) {
            legendMap = new HashMap<String, BufferedImage>();
            legends.put( style, legendMap );
        }

        return buildLegend( req, renderer, style, originalSize, legendMap );
    }

    Pair<Integer, Integer> getLegendSize( Style style ) {
        Pair<Integer, Integer> res = legendSizes.get( style );
        if ( res != null ) {
            return res;
        }

        legendSizes.put( style, res = new Legends().getLegendSize( style ) );
        return res;
    }

    private Style findLegendStyle( LayerRef layer, StyleRef styleRef )
                            throws OWSException {
        Style style;
        Theme theme = service.themeMap.get( layer.getName() );
        if ( theme == null ) {
            throw new OWSException( get( "WMS.LAYER_NOT_KNOWN", layer.getName() ), OWSException.LAYER_NOT_DEFINED );
        }

        style = theme.getLayerMetadata().getLegendStyles().get( styleRef.getName() );
        if ( style == null ) {
            style = theme.getLayerMetadata().getStyles().get( styleRef.getName() );
        }

        if ( style == null ) {
            throw new OWSException( get( "WMS.UNDEFINED_STYLE", styleRef.getName(), layer.getName() ),
                                    OWSException.STYLE_NOT_DEFINED );
        }

        return style;
    }

    private BufferedImage buildLegend( GetLegendGraphic req, Legends renderer, Style style, boolean originalSize,
                                       HashMap<String, BufferedImage> legendMap ) {
        BufferedImage img = MapService.prepareImage( req );
        Graphics2D g = img.createGraphics();
        g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
        g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON );

        renderer.paintLegend( style, req.getWidth(), req.getHeight(), g );

        g.dispose();

        if ( req.getFormat().equals( "image/png; mode=8bit" ) || req.getFormat().equals( "image/png; subtype=8bit" )
             || req.getFormat().equals( "image/gif" ) ) {
            img = postprocessPng8bit( img );
        }

        if ( originalSize ) {
            legendMap.put( req.getFormat(), img );
        }
        return img;
    }

}
