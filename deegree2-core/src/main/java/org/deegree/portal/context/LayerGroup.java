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
package org.deegree.portal.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LayerGroup extends MapModelEntry {

    private boolean expanded;

    private List<MMLayer> layers;

    private List<LayerGroup> layerGroups;

    private List<MapModelEntry> entries;

    /**
     * 
     * @param identifier
     * @param title
     * @param hidden
     * @param expanded
     * @param parent
     * @param owner
     */
    public LayerGroup( String identifier, String title, boolean hidden, boolean expanded, LayerGroup parent,
                       MapModel owner ) {
        super( identifier, title, hidden, parent, owner );
        layers = new ArrayList<MMLayer>();
        layerGroups = new ArrayList<LayerGroup>();
        entries = new ArrayList<MapModelEntry>();
        this.expanded = expanded;
    }

    /**
     * 
     * @param layer
     * @param antecessor
     * @param first
     */
    public void insert( MMLayer layer, MapModelEntry antecessor, boolean first ) {
        if ( layer.getParent() == null || !layer.getParent().equals( parent ) ) {
            layers.remove( layer );
            layers.add( layer );
            layer.setParent( this );
            int i = 0;
            while ( i < entries.size() && !entries.get( i ).equals( antecessor ) ) {
                i++;
            }
            if ( i >= entries.size() - 1 ) {
                if ( first && antecessor == null ) {
                    entries.add( 0, layer );
                } else {
                    entries.add( layer );
                }
            } else {
                if ( first && antecessor == null ) {
                    entries.add( 0, layer );
                } else {
                    entries.add( i + 1, layer );
                }
            }
        }
    }

    /**
     * 
     * @param layerGroup
     * @param antecessor
     * @param first
     */
    public void insert( LayerGroup layerGroup, MapModelEntry antecessor, boolean first ) {
        if ( layerGroup.getParent() == null || !layerGroup.getParent().equals( parent ) ) {
            // register as child
            layerGroups.add( layerGroup );
            // that this a parent
            layerGroup.setParent( this );

            int i = 0;
            if ( entries.size() > 0 ) {
                while ( i < entries.size() && !entries.get( i ).equals( antecessor ) ) {
                    i++;
                }
            }
            if ( i >= entries.size() - 1 ) {
                if ( first && antecessor == null ) {
                    entries.add( 0, layerGroup );
                } else {
                    entries.add( layerGroup );
                }
            } else {
                if ( first && antecessor == null ) {
                    entries.add( 0, layerGroup );
                } else {
                    entries.add( i + 1, layerGroup );
                }
            }
        }
    }

    /**
     * 
     * @param layer
     */
    public void addLayer( MMLayer layer ) {
        if ( !layers.contains( layer ) ) {
            layers.add( layer );
            entries.add( layer );
        }
    }

    /**
     * 
     * @param layerGroup
     */
    public void addLayerGroup( LayerGroup layerGroup ) {
        layerGroups.add( layerGroup );
        entries.add( layerGroup );
    }

    /**
     * @return the layerGroups
     */
    public List<LayerGroup> getLayerGroups() {
        return Collections.unmodifiableList( layerGroups );
    }

    /**
     * @return the layers
     */
    public List<MMLayer> getLayers() {
        return Collections.unmodifiableList( layers );
    }

    /**
     * @return the entries
     */
    public List<MapModelEntry> getMapModelEntries() {
        return Collections.unmodifiableList( entries );
    }

    /**
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * @param expanded
     *            the expanded to set
     */
    public void setExpanded( boolean expanded ) {
        this.expanded = expanded;
    }
    
    /**
     * 
     * @param layer
     */
    public void removeLayer( MMLayer layer ) {
        layers.remove( layer );
        entries.remove( layer );
        layer.setParent( null );
    }

    /**
     * 
     * @param layerGroup
     */
    public void removeLayerGroup( LayerGroup layerGroup ) {
        layerGroups.remove( layerGroup );
        entries.remove( layerGroup );
        layerGroup.setParent( null );
    }

}
