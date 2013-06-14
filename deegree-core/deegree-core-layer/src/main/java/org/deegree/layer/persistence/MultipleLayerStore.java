//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.layer.Layer;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class MultipleLayerStore implements LayerStore {

    private Map<String, Layer> map;

    private ResourceMetadata<LayerStore> metadata;

    public MultipleLayerStore( Map<String, Layer> map, ResourceMetadata<LayerStore> metadata ) {
        this.map = map;
        this.metadata = metadata;
    }

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public void destroy() {
        for ( Layer l : map.values() ) {
            l.destroy();
        }
    }

    @Override
    public List<Layer> getAll() {
        return new ArrayList<Layer>( map.values() );
    }

    @Override
    public Layer get( String identifier ) {
        return map.get( identifier );
    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

}
