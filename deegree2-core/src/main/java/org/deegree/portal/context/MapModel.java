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

import org.deegree.portal.PortalException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MapModel {

    private List<LayerGroup> layerGroups;

    /**
     * 
     * @param layerGroups
     */
    public void setLayerGroups( List<LayerGroup> layerGroups ) {
        this.layerGroups = layerGroups;
    }

    /**
     * @return the layerGroups
     */
    public List<LayerGroup> getLayerGroups() {
        return layerGroups;
    }

    /**
     * 
     * @param action
     * @return list of {@link Layer} selected for the passed action
     */
    public List<MMLayer> getLayersSelectedForAction( String action ) {
        List<MMLayer> tmp = new ArrayList<MMLayer>();
        getLayersForAction( layerGroups, action, tmp );
        return Collections.unmodifiableList( tmp );
    }

    private void getLayersForAction( List<LayerGroup> lgs, String action, List<MMLayer> collector ) {
        for ( LayerGroup layerGroup : lgs ) {
            List<MMLayer> mapModelEntries = layerGroup.getLayers();
            for ( MMLayer mapModelEntrry : mapModelEntries ) {
                if ( mapModelEntrry.getSelectedFor().contains( action ) ) {
                    collector.add( mapModelEntrry );
                }
            }
            getLayersForAction( layerGroup.getLayerGroups(), action, collector );
        }
    }

    /**
     * 
     * @param mapModelEntry
     *            {@link MapModelEntry} to be inserted
     * @param parent
     *            if <code>null</code> root node of layertree will be used as parent
     * @param antecessor
     *            if <code>null</code> layer will be inserted directly underneath its parent
     * @param first
     *            if true layer will be inserted as first layer of a group if antecessor == null
     * @throws Exception
     */
    public void insert( final MapModelEntry mapModelEntry, LayerGroup parent, MapModelEntry antecessor, boolean first )
                            throws Exception {

        // check if layer already exists in map model
        walkLayerTree( new MapModelVisitor() {

            public void visit( MMLayer layer )
                                    throws Exception {
                if ( layer.getIdentifier().equals( mapModelEntry.getIdentifier() ) ) {
                    throw new PortalException( "layer: " + layer.getTitle() + " already contained in tree" );
                }
            }

            public void visit( LayerGroup layerGroup )
                                    throws Exception {
                if ( layerGroup.getIdentifier().equals( mapModelEntry.getIdentifier() ) ) {
                    throw new PortalException( "layergroup: " + layerGroup.getTitle() + " already contained in tree" );
                }
            }

        } );

        if ( mapModelEntry instanceof MMLayer ) {
            insertLayer( (MMLayer) mapModelEntry, parent, antecessor, first );
        } else if ( mapModelEntry instanceof LayerGroup ) {
            insertLayerGroup( (LayerGroup) mapModelEntry, parent, antecessor, first );
        }
    }

    /**
     * 
     * @param layer
     * @param parent
     *            if <code>null</code> root node of layertree will be used as parent
     * @param antecessor
     *            if <code>null</code> layer will be inserted directly underneath its parent
     * @param first
     *            if true layer will be inserted as first layer of a group if antecessor == null
     */
    private void insertLayer( MMLayer layer, LayerGroup parent, MapModelEntry antecessor, boolean first ) {
        insertLayer( layer, parent, antecessor, layerGroups, first );
    }

    private void insertLayer( MMLayer layer, LayerGroup parent, MapModelEntry antecessor, List<LayerGroup> lgs,
                              boolean first ) {
        for ( LayerGroup layerGroup : lgs ) {
            if ( parent != null && parent.equals( layerGroup ) ) {
                layerGroup.insert( layer, antecessor, first );
                break;
            } else {
                insertLayer( layer, parent, antecessor, layerGroup.getLayerGroups(), first );
            }
        }
    }

    /**
     * 
     * @param layerGroup
     * @param parent
     *            if <code>null</code> root node of layer tree will be used as parent
     * @param antecessor
     *            if <code>null</code> layer will be inserted directly underneath its parent
     * @param first
     *            if true layer will be inserted as first layer group of a group if antecessor == null
     */
    private void insertLayerGroup( LayerGroup layerGroup, LayerGroup parent, MapModelEntry antecessor, boolean first ) {
        if ( parent == null ) {
            layerGroups.add( layerGroup );
        }
        insertLayerGroup( layerGroup, parent, antecessor, layerGroups, first );
    }

    private void insertLayerGroup( LayerGroup lg, LayerGroup parent, MapModelEntry antecessor, List<LayerGroup> lgs,
                                   boolean first ) {
        for ( LayerGroup layerGroup : lgs ) {
            if ( parent != null && parent.equals( layerGroup ) ) {
                layerGroup.insert( lg, antecessor, first );
                break;
            } else {
                insertLayerGroup( lg, parent, antecessor, layerGroup.getLayerGroups(), first );
            }
        }
    }

    /**
     * moves the passed layer underneath a new parent and before the passed antecessor.
     * 
     * @param mapModelEntry
     * @param parent
     *            if <code>null</code> root node of layer tree will be used as parent
     * @param antecessor
     *            if <code>null</code> layer will be inserted directly underneath its parent
     * @param first
     */
    public void move( MapModelEntry mapModelEntry, LayerGroup parent, MapModelEntry antecessor, boolean first ) {
        if ( mapModelEntry instanceof MMLayer ) {
            move( (MMLayer) mapModelEntry, parent, antecessor, first );
        } else if ( mapModelEntry instanceof LayerGroup ) {
            move( (LayerGroup) mapModelEntry, parent, antecessor, first );
        }
    }

    /**
     * moves the passed layer underneath a new parent and before the passed antecessor.
     * 
     * @param layer
     * @param parent
     *            if <code>null</code> root node of layertree will be used as parent
     * @param antecessor
     *            if <code>null</code> layer will be inserted directly underneath its parent
     * @param first
     *            if true layer will be inserted as first layer of a group if antecessor == null
     */
    private void move( MMLayer layer, LayerGroup parent, MapModelEntry antecessor, boolean first ) {
        if ( !layer.equals( antecessor ) ) {
            layer.getParent().removeLayer( layer );
            insertLayer( layer, parent, antecessor, layerGroups, first );
        }
    }

    /**
     * moves the passed layergroup underneath a new parent and before the passed antecessor.
     * 
     * @param layerGroup
     * @param parent
     *            if <code>null</code> root node of layertree will be used as parent
     * @param antecessor
     *            if <code>null</code> layergroup will be inserted directly underneath its parent
     */
    private void move( LayerGroup layerGroup, LayerGroup parent, MapModelEntry antecessor, boolean first ) {
        if ( !layerGroup.equals( antecessor ) ) {
            layerGroup.getParent().removeLayerGroup( layerGroup );
            insertLayerGroup( layerGroup, parent, antecessor, layerGroups, first );
        }
    }

    /**
     * 
     * @param visitor
     * @throws Exception
     */
    public void walkLayerTree( MapModelVisitor visitor )
                            throws Exception {
        for ( LayerGroup layerGroup : layerGroups ) {
            applyVisitor( layerGroup, visitor );
        }
    }

    private void applyVisitor( LayerGroup layerGroup, MapModelVisitor visitor )
                            throws Exception {
        visitor.visit( layerGroup );
        List<MapModelEntry> entries = layerGroup.getMapModelEntries();
        for ( MapModelEntry entry : entries ) {
            if ( entry instanceof MMLayer ) {
                visitor.visit( (MMLayer) entry );
            } else {
                applyVisitor( (LayerGroup) entry, visitor );
            }
        }
    }

    /**
     * 
     * @param identifier
     * @return {@link MapModelEntry} matching passed identifier
     */
    public MapModelEntry getMapModelEntryByIdentifier( final String identifier ) {
        final List<MapModelEntry> list = new ArrayList<MapModelEntry>();
        try {
            walkLayerTree( new MapModelVisitor() {

                public void visit( LayerGroup layerGroup )
                                        throws Exception {
                    if ( identifier.equals( layerGroup.getIdentifier() ) ) {
                        list.add( layerGroup );
                    }

                }

                public void visit( MMLayer layer )
                                        throws Exception {
                    if ( identifier.equals( layer.getIdentifier() ) ) {
                        list.add( layer );
                    }

                }
            } );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        if ( list.size() > 0 ) {
            return list.get( 0 );
        } else {
            return null;
        }
    }

    /**
     * 
     * @param mapModelEntry
     *            {@link MapModelEntry} to remove
     */
    public void remove( MapModelEntry mapModelEntry ) {
        if ( mapModelEntry instanceof MMLayer ) {
            ( (MMLayer) mapModelEntry ).getParent().removeLayer( (MMLayer) mapModelEntry );
        } else if ( mapModelEntry instanceof LayerGroup ) {
            LayerGroup layerGroup = (LayerGroup) mapModelEntry;
            if ( layerGroup.getParent() != null ) {
                layerGroup.getParent().removeLayerGroup( layerGroup );
            }
        }
    }
}
