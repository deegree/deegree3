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

package org.deegree.commons.index;

import static java.lang.Math.min;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.deegree.commons.dataaccess.shape.SHPReader;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;

/**
 * <code>RTree</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RTree {

    private Node root;

    private double[] bbox;

    /**
     * @param shape
     */
    public RTree( SHPReader shape ) {
        Envelope bbox = shape.getEnvelope();
        this.bbox = new double[] { bbox.getMin().getX(), bbox.getMin().getY(), bbox.getMax().getX(),
                                  bbox.getMax().getY() };
        try {
            // to work around Java's non-existent variant type
            ArrayList list = shape.readEnvelopes();
            root = buildTree( list, 10 );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static final boolean contained( final double[] box, final double x, final double y ) {
        return box[0] <= x && x <= box[2] && box[1] <= y && y <= box[3];
    }

    private boolean intersects( double[] box1, double[] box2 ) {
        return contained( box2, box1[0], box1[3] ) || contained( box2, box1[0], box1[1] )
               || contained( box2, box1[2], box1[3] ) || contained( box2, box1[2], box1[1] )
               || contained( box1, box2[0], box2[3] ) || contained( box1, box2[0], box2[1] )
               || contained( box1, box2[2], box2[3] ) || contained( box1, box2[2], box2[1] );

    }

    private LinkedList<Long> query( double[] bbox, Node node ) {
        LinkedList<Long> list = new LinkedList<Long>();

        for ( Entry e : node.entries ) {
            if ( intersects( bbox, e.bbox ) ) {
                if ( e.next == null ) {
                    list.add( e.pointer );
                } else {
                    list.addAll( query( bbox, e.next ) );
                }
            }
        }

        return list;
    }

    /**
     * @param env
     * @return a list of pointers
     */
    public LinkedList<Long> query( Envelope env ) {
        double[] bbox = new double[] { env.getMin().getX(), env.getMin().getY(), env.getMax().getX(),
                                      env.getMax().getY() };
        return query( bbox, root );
    }

    private TreeMap<Double, LinkedList<Pair<double[], ?>>> sort( Collection<Pair<double[], ?>> rects, int byIdx ) {
        TreeMap<Double, LinkedList<Pair<double[], ?>>> map = new TreeMap<Double, LinkedList<Pair<double[], ?>>>();

        for ( Pair<double[], ?> p : rects ) {
            double d = p.first[byIdx] + ( p.first[byIdx + 2] - p.first[byIdx] ) / 2;
            if ( !map.containsKey( d ) ) {
                map.put( d, new LinkedList<Pair<double[], ?>>() );
            }
            map.get( d ).add( p );
        }

        return map;
    }

    private LinkedList<LinkedList<Pair<double[], ?>>> slice( TreeMap<Double, LinkedList<Pair<double[], ?>>> map,
                                                             int limit ) {
        LinkedList<LinkedList<Pair<double[], ?>>> list = new LinkedList<LinkedList<Pair<double[], ?>>>();

        LinkedList<Pair<double[], ?>> cur = new LinkedList<Pair<double[], ?>>();
        Iterator<LinkedList<Pair<double[], ?>>> iter = map.values().iterator();
        LinkedList<Pair<double[], ?>> l = iter.next();
        while ( iter.hasNext() || l.size() > 0 ) {
            if ( cur.size() == limit ) {
                list.add( cur );
                cur = new LinkedList<Pair<double[], ?>>();
            }
            if ( l.isEmpty() ) {
                l = iter.next();
            }
            cur.add( l.poll() );
        }

        return list;
    }

    private Node buildTree( ArrayList<Pair<double[], ?>> rects, int limit ) {
        if ( rects.size() <= limit ) {
            Node node = new Node( rects.size() );
            for ( int i = 0; i < rects.size(); ++i ) {
                node.entries[i] = new Entry();
                node.entries[i].bbox = rects.get( i ).first;
                if ( rects.get( i ).second instanceof Long ) {
                    node.entries[i].pointer = (Long) rects.get( i ).second;
                } else {
                    node.entries[i].next = (Node) rects.get( i ).second;
                }
            }
            return node;
        }

        LinkedList<LinkedList<Pair<double[], ?>>> slices = slice( sort( rects, 0 ), limit * limit );
        ArrayList<Pair<double[], ?>> newRects = new ArrayList<Pair<double[], ?>>();

        for ( LinkedList<Pair<double[], ?>> slice : slices ) {
            TreeMap<Double, LinkedList<Pair<double[], ?>>> map = sort( slice, 1 );

            Iterator<LinkedList<Pair<double[], ?>>> iter = map.values().iterator();
            LinkedList<Pair<double[], ?>> list = iter.next();
            int idx = 0;
            while ( idx < slice.size() ) {
                Node node = new Node( min( limit, slice.size() - idx ) );
                double[] bbox = null;
                for ( int i = 0; i < limit; ++i, ++idx ) {
                    if ( idx < slice.size() ) {
                        if ( list.isEmpty() ) {
                            list = iter.next();
                        }
                        Pair<double[], ?> p = list.poll();
                        node.entries[i] = new Entry();
                        node.entries[i].bbox = p.first;
                        if ( p.second instanceof Long ) {
                            node.entries[i].pointer = (Long) p.second;
                        } else {
                            node.entries[i].next = (Node) p.second;
                        }
                        if ( bbox == null ) {
                            bbox = new double[] { p.first[0], p.first[1], p.first[2], p.first[3] };
                        } else {
                            for ( int k = 0; k < 2; ++k ) {
                                if ( bbox[k] > p.first[k] ) {
                                    bbox[k] = p.first[k];
                                }
                            }
                            for ( int k = 2; k < 4; ++k ) {
                                if ( bbox[k] < p.first[k] ) {
                                    bbox[k] = p.first[k];
                                }
                            }
                        }
                    }
                }
                newRects.add( new Pair<double[], Node>( bbox, node ) );
            }
        }

        return buildTree( newRects, limit );
    }

    static class Entry {
        double[] bbox;

        long pointer;

        Node next;
    }

    static class Node {
        Node( int num ) {
            entries = new Entry[num];
        }

        Entry[] entries;
    }

}
