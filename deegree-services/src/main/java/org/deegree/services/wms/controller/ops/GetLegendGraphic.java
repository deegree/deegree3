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
package org.deegree.services.wms.controller.ops;

import static java.lang.Integer.parseInt;
import static org.deegree.services.controller.ows.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.services.controller.ows.OWSException.LAYER_NOT_DEFINED;
import static org.deegree.services.controller.ows.OWSException.MISSING_PARAMETER_VALUE;
import static org.deegree.services.controller.ows.OWSException.STYLE_NOT_DEFINED;
import static org.deegree.services.i18n.Messages.get;

import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.model.layers.Layer;

/**
 * <code>GetLegendGraphic</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetLegendGraphic {

    private Style style;

    private String format;

    private int width;

    private int height;

    /**
     * @param map
     * @param service
     * @throws OWSException
     */
    public GetLegendGraphic( Map<String, String> map, MapService service ) throws OWSException {
        String layer = map.get( "LAYER" );
        if ( layer == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "LAYER" ), MISSING_PARAMETER_VALUE );
        }
        Layer l = service.getLayer( layer );
        if ( l == null ) {
            throw new OWSException( get( "WMS.LAYER_NOT_KNOWN", layer ), LAYER_NOT_DEFINED );
        }
        String s = map.get( "STYLE" );
        if ( s == null ) {
            s = "default";
        }
        style = service.getStyles().get( layer, s );
        if ( style == null ) {
            throw new OWSException( get( "WMS.UNDEFINED_STYLE", s, layer ), STYLE_NOT_DEFINED );
        }
        format = map.get( "FORMAT" );
        if ( format == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "FORMAT" ), MISSING_PARAMETER_VALUE );
        }

        Pair<Integer, Integer> bestSize = service.getLegendSize( style );

        String w = map.get( "WIDTH" );
        if ( w == null ) {
            width = bestSize.first;
        } else {
            try {
                width = parseInt( w );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "WIDTH", w ), INVALID_PARAMETER_VALUE );
            }
        }
        String h = map.get( "HEIGHT" );
        if ( h == null ) {
            height = bestSize.second;
        } else {
            try {
                height = parseInt( h );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "HEIGHT", h ), INVALID_PARAMETER_VALUE );
            }
        }
    }

    /**
     * @return the style selected by the request
     */
    public Style getStyle() {
        return style;
    }

    /**
     * @return the image format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @return the desired width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the desired height
     */
    public int getHeight() {
        return height;
    }

}
