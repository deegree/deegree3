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
package org.deegree.layer.persistence;

import java.util.Collections;
import java.util.List;

import org.deegree.layer.Layer;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SingleLayerStore implements LayerStore {

    private final Layer layer;

    private ResourceMetadata<LayerStore> metadata;

    public SingleLayerStore( Layer layer, ResourceMetadata<LayerStore> metadata ) {
        this.layer = layer;
        this.metadata = metadata;
    }

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public void destroy() {
        layer.destroy();
    }

    @Override
    public List<Layer> getAll() {
        return Collections.singletonList( get( layer.getMetadata().getName() ) );
    }

    @Override
    public Layer get( String identifier ) {
        if ( layer.getMetadata().getName().equals( identifier ) ) {
            return layer;
        }
        return null;
    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

}
