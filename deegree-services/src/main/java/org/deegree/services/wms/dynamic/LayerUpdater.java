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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.dynamic;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.model.layers.EmptyLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.slf4j.Logger;

/**
 * <code>LayerUpdater</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs which empty/unavailable layers have been removed")
public abstract class LayerUpdater {

    private static final Logger LOG = getLogger( LayerUpdater.class );

    protected boolean cleanup( Layer root, MapService service ) {
        boolean changed = false;
        synchronized ( root ) {
            for ( Layer l : new LinkedList<Layer>( root.getChildren() ) ) {
                changed |= cleanup( l, service );
                if ( !l.isAvailable() ) {
                    root.remove( l );
                    changed = true;
                    LOG.debug( "Removed unavailable layer {}", l.getName() );
                    if ( service.layers.get( l.getName() ) == l ) {
                        service.layers.remove( l.getName() );
                    }
                }
                if ( l instanceof EmptyLayer && l.getChildren().isEmpty() && l.getParent() != null ) {
                    root.remove( l );
                    changed = true;
                    LOG.debug( "Removed empty layer {}", l.getName() );
                    if ( service.layers.get( l.getName() ) == l ) {
                        service.layers.remove( l.getName() );
                    }
                }
            }
        }
        return changed;
    }

    /**
     * @return whether layers have been updated
     */
    public abstract boolean update();

}
