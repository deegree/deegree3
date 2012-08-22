//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.io.quadtree;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;

/**
 * <code>MemPointNode</code> is the node class of a memory based implementation of a quadtree.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 * @param <T>
 *            the datatype to be used as id
 *
 * @since 2.0
 */

public class MemPointNode<T> implements Node<T> {

    private final Envelope envelope;

    private final int level;

    private MemPointNode<T>[] subnodes;

    private List<T> items;

    private List<Envelope> itemsEnvelope;

    private Quadtree owner;

    /**
     * Constructs a new node with the given envelope, object, location and level.
     *
     * @param owner
     * @param env
     *            the envelope
     * @param lvl
     *            the level
     */
    public MemPointNode( Quadtree owner, Envelope env, int lvl ) {
        envelope = env;
        level = lvl;
        this.owner = owner;
    }

    /**
     * @return the deepest level of this subtree
     */
    public int getDepth() {
        if ( subnodes == null ) {
            return level;
        }

        int max = 0;
        int d = 0;

        for ( MemPointNode node : subnodes ) {
            if ( node != null ) {
                d = node.getDepth();
                if ( d > max ) {
                    max = d;
                }
            }
        }

        return max;
    }

    /**
     * @return the region of this node
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * This method does not make sense for the memory implementation.
     *
     * @return null
     */
    public String getId() {
        return null;
    }

    /**
     * Inserts the item into the quadtree.
     *
     * @param item
     *            the item
     * @param itemEnv
     *            the envelope of the item
     */
    public boolean insert( T item, Envelope itemEnv )
                            throws IndexException {

        if ( !envelope.intersects( itemEnv ) ) {
            throw new IndexException( "Item envelope does not intersect with node envelope!" );
        }

        if ( level < ( (MemPointQuadtree) owner ).maxDepth ) {
            Envelope[] envs = split();

            if ( subnodes == null ) {
                subnodes = (MemPointNode<T>[]) new MemPointNode[4];
            }

            for ( int i = 0; i < 4; ++i ) {
                if ( envs[i].intersects( itemEnv ) ) {
                    if ( subnodes[i] == null ) {
                        subnodes[i] = new MemPointNode<T>( owner, envs[i], level + 1 );
                    }
                    subnodes[i].insert( item, itemEnv );
                }
            }
        } else {
            if ( items == null ) {
                items = new ArrayList<T>( 50 );
                itemsEnvelope = new ArrayList<Envelope>( 50 );
            }
            items.add( item );
            itemsEnvelope.add( itemEnv );
        }
        return true;
    }

    /**
     * Searches for all items intersecting the search envelope.
     *
     * @param searchEnv
     *            the search envelope
     * @param visitor
     *            the resulting list
     * @param level
     *            unused by this implementation
     * @return a list with all found items
     */
    public List<T> query( Envelope searchEnv, List<T> visitor, int level )
                            throws IndexException {

        if ( subnodes == null ) {
            return visitor;
        }

        for ( int i = 0; i < 4; ++i ) {
            if ( subnodes[i] != null ) {
                MemPointNode<T> node = subnodes[i];
                if ( node.items != null ) {
                    if ( subnodes[i].envelope.intersects( searchEnv ) ) {
                        for ( int j = 0; j < node.itemsEnvelope.size(); j++ ) {
                            Envelope env = node.itemsEnvelope.get( j );
                            if ( env.intersects( searchEnv ) ) {
                                visitor.addAll( node.items );
                            }
                        }
                    }
                } else {
                    if ( node.envelope.intersects( searchEnv ) ) {
                        node.query( searchEnv, visitor, level );
                    }
                }
            }
        }

        return visitor;
    }

    /**
     * Deletes the item from the quadtree. Untested method!
     *
     * @param item
     *            the item to be deleted
     */
    public boolean delete( T item, Envelope env )
                            throws IndexException {

        if ( subnodes != null ) {
            for ( int i = 0; i < 4; ++i ) {
                if ( subnodes[i] != null ) {
                    MemPointNode<T> node = subnodes[i];
                    if ( node.items.contains( item ) ) {
                        node.items.remove( item );
                    } else {
                        return node.delete( item, env );
                    }
                }
            }
        }
        return true;

    }

    public boolean update( T item, Envelope newBBox ) {
        throw new UnsupportedOperationException(
                                                 "This method is not implemented for the mempoint Node, maybe use a delete and insert combination?" );
    }

    /**
     * Deletes all items intersecting the envelope. Untested method!
     *
     * @param envelope
     */
    public void deleteRange( Envelope envelope ) {

        if ( subnodes == null ) {
            return;
        }

        for ( int i = 0; i < 4; ++i ) {
            if ( subnodes[i] != null ) {
                MemPointNode node = subnodes[i];
                if ( node.envelope.intersects( envelope ) ) {
                    subnodes[i] = null;
                } else {
                    if ( node.envelope.intersects( envelope ) ) {
                        node.deleteRange( envelope );
                    }
                }
            }
        }

    }

    // splits the envelope of this node in four pieces
    private Envelope[] split() {
        Envelope[] envs = new Envelope[4];
        double nW = envelope.getWidth() / 2d;
        double nH = envelope.getHeight() / 2d;

        envs[0] = GeometryFactory.createEnvelope( envelope.getMin().getX(), envelope.getMin().getY(),
                                                  envelope.getMin().getX() + nW, envelope.getMin().getY() + nH, null );
        envs[1] = GeometryFactory.createEnvelope( envelope.getMin().getX() + nW, envelope.getMin().getY(),
                                                  envelope.getMin().getX() + ( 2 * nW ), envelope.getMin().getY() + nH,
                                                  null );
        envs[2] = GeometryFactory.createEnvelope( envelope.getMin().getX() + nW, envelope.getMin().getY() + nH,
                                                  envelope.getMin().getX() + ( 2 * nW ), envelope.getMin().getY()
                                                                                         + ( 2 * nH ), null );
        envs[3] = GeometryFactory.createEnvelope( envelope.getMin().getX(), envelope.getMin().getY() + nH,
                                                  envelope.getMin().getX() + nW, envelope.getMin().getY() + ( 2 * nH ),
                                                  null );

        return envs;
    }

}
