/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.capabilities.theme;

import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.theme.Theme;

/**
 * Merges {@link LayerMetadata} of {@link Theme} objects.
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class LayerMetadataQueryable {
    
    private static final int QUERYABLE_DEFAULT_MASK = 1;
    
    private static final int QUERYABLE_DISABLED_MASK = 2;
    
    private static final int QUERYABLE_ENABLED_MASK = 4;
    
    public static int analyseQueryable( LayerMetadata m ) {
        if ( m.getMapOptions() == null )
            return QUERYABLE_DEFAULT_MASK;
        
        int r = m.getMapOptions().getFeatureInfoRadius();
        
        if ( r < 0 )
            return QUERYABLE_DEFAULT_MASK;
        else if ( r == 0 )
            return QUERYABLE_DISABLED_MASK;
        else
            return QUERYABLE_ENABLED_MASK;
    }
    
    public static void applyQueryable( LayerMetadata themeMetadata, int analyseResult ) {
        if ( themeMetadata.getMapOptions() == null ) {
            themeMetadata.setMapOptions( new MapOptions( null, null, null, -1, -1 ) );
        }
        
        if ( analyseResult == QUERYABLE_DISABLED_MASK ) {
            themeMetadata.getMapOptions().setFeatureInfoRadius( 0 );
        } else {
            themeMetadata.getMapOptions().setFeatureInfoRadius( -1 );
        }
    }
    
}
