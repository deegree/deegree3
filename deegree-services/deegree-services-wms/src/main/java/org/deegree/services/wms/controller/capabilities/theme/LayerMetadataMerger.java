/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wms.controller.capabilities.theme;

import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;

/**
 * Merges {@link LayerMetadata} of {@link Theme} objects.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.3
 */
class LayerMetadataMerger {

    /**
     * Returns the combined layer metadata for the given theme/sublayers.
     *
     * @see LayerMetadata#merge(LayerMetadata)
     *
     * @param theme
     *            must not be <code>null</code>
     * @return combined layer metadata, never <code>null</code>
     */
    LayerMetadata merge( final Theme theme ) {
        final LayerMetadata themeMetadata = theme.getLayerMetadata();
        LayerMetadata layerMetadata = null;
        for ( final Layer l : Themes.getAllLayers( theme ) ) {
            if ( layerMetadata == null ) {
                layerMetadata = l.getMetadata();
            } else {
                layerMetadata.merge( l.getMetadata() );
            }
        }
        themeMetadata.merge( layerMetadata );
        return themeMetadata;
    }

}
