//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.geometry.jtswrapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.deegree.crs.CRS;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.primitive.Point;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperMultiPoint extends JTSWrapperGeometry implements MultiPoint {

    private List<Point> points;

    /**
     * 
     * @param id 
     * @param points
     * @param coordinateDimension
     * @param precision
     * @param crs
     */
    public JTSWrapperMultiPoint( String id, List<Point> points, int coordinateDimension, double precision, CRS crs ) {
        super( id, precision, crs, coordinateDimension );
        this.points = points;
        com.vividsolutions.jts.geom.Point[] pts = new com.vividsolutions.jts.geom.Point[points.size()];
        int i = 0;
        for ( Point point : points ) {
            if ( point instanceof JTSWrapperPoint ) {
                pts[i++] = (com.vividsolutions.jts.geom.Point) ( (JTSWrapperPoint) point ).getJTSGeometry();
            } else {
                pts[i++] = (com.vividsolutions.jts.geom.Point) export( point );
            }
        }
        geometry = jtsFactory.createMultiPoint( pts );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.geometry.multi.MultiGeometry#getCentroid()
     */
    public Point getCentroid() {
        return toPoint( geometry.getCentroid().getCoordinate() );
    }

    // -----------------------------------------------------------------------
    // implementation of List<Curve>
    // -----------------------------------------------------------------------
    
    
    public void add( int index, Point element ) {
        points.add( index, element );
    }

    public boolean add( Point e ) {
        return points.add( e );
    }

    public boolean addAll( Collection<? extends Point> c ) {
        return points.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends Point> c ) {
        return points.addAll( index, c );
    }

    public void clear() {
        points.clear();
    }

    public boolean contains( Object o ) {
        return points.contains( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return points.containsAll( c );
    }

    public Point get( int index ) {
        return points.get( index );
    }
    
    public int indexOf( Object o ) {
        return points.indexOf( o );
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public Iterator<Point> iterator() {
        return points.iterator();
    }

    public int lastIndexOf( Object o ) {
        return points.lastIndexOf( o );
    }

    public ListIterator<Point> listIterator() {
        return points.listIterator();
    }

    public ListIterator<Point> listIterator( int index ) {
        return points.listIterator( index );
    }

    public Point remove( int index ) {
        return points.remove( index );
    }

    public boolean remove( Object o ) {
        return points.remove( o );
    }

    public boolean removeAll( Collection<?> c ) {
        return points.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return points.retainAll( c );
    }

    public Point set( int index, Point element ) {
        return points.set( index, element );
    }

    public int size() {
        return points.size();
    }

    public List<Point> subList( int fromIndex, int toIndex ) {
        return points.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return points.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return points.toArray( a );
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_GEOMETRY;
    }
}
