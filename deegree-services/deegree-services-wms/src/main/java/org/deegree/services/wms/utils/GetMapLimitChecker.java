/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wms.utils;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;

import java.util.List;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.model.layers.Layer;

/**
 * Checks whether a {@link GetMap} request is valid with regard to the requested image size and layer count.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class GetMapLimitChecker {

    public void checkRequestedSizeAndLayerCount( final GetMap request, final ServiceConfigurationType config )
                            throws OWSException {        
        checkWidth( request.getWidth(), null );
        checkHeight( request.getHeight(), null );
        checkLayerCount( request.getLayers(), null );
    }

    void checkWidth( final int requestedWidth, final Integer maxWidth )
                            throws OWSException {
        if ( requestedWidth <= 0 ) {
            final String msg = "Width must be positive.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "width" );
        }
        if ( maxWidth == null ) {
            return;
        }
        if ( requestedWidth > maxWidth ) {
            final String msg = "Width out of range. Maximum width: " + maxWidth;
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "width" );
        }
    }

    void checkHeight( final int requestedHeight, final Integer maxHeight )
                            throws OWSException {
        if ( requestedHeight <= 0 ) {
            final String msg = "Height must be positive.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "height" );
        }
        if ( maxHeight == null ) {
            return;
        }
        if ( requestedHeight > maxHeight ) {
            final String msg = "Height out of range. Maximum height: " + maxHeight;
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "height" );
        }
    }

    void checkLayerCount( final List<Layer> layers, final Integer maxLayers ) throws OWSException {
        if ( layers == null ) {
            return;
        }
        if ( layers.size() > maxLayers ) {
            final String msg = "Too many layers requested. Maximum number of layers: " + maxLayers;
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "layer" );
        }
    }
    
}
