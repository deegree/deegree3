//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.geometry.standard.aggregate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link MultiGeometry}.
 * 
 * @param <T>
 *            type of contained geometry objects
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultMultiGeometry<T extends Geometry> extends AbstractDefaultGeometry implements MultiGeometry<T> {

    private List<T> members;

    /**
     * Creates a new {@link DefaultMultiGeometry} from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param members
     */
    public DefaultMultiGeometry( String id, CoordinateSystem crs, List<T> members ) {
        super( id, crs );
        this.members = members;
    }

    @Override
    public Point getCentroid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_GEOMETRY;
    }    
    
    // -----------------------------------------------------------------------
    // delegate methods for List<T>
    // -----------------------------------------------------------------------

    public void add( int index, T element ) {
        members.add( index, element );
    }

    public boolean add( T e ) {
        return members.add( e );
    }

    public boolean addAll( Collection<? extends T> c ) {
        return members.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends T> c ) {
        return members.addAll( index, c );
    }

    public void clear() {
        members.clear();
    }

    public boolean contains( Object o ) {
        return members.contains( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return members.containsAll( c );
    }

    public T get( int index ) {
        return members.get( index );
    }

    public int indexOf( Object o ) {
        return members.indexOf( o );
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public Iterator<T> iterator() {
        return members.iterator();
    }

    public int lastIndexOf( Object o ) {
        return members.lastIndexOf( o );
    }

    public ListIterator<T> listIterator() {
        return members.listIterator();
    }

    public ListIterator<T> listIterator( int index ) {
        return members.listIterator( index );
    }

    public T remove( int index ) {
        return members.remove( index );
    }

    public boolean remove( Object o ) {
        return members.remove( o );
    }

    public boolean removeAll( Collection<?> c ) {
        return members.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return members.retainAll( c );
    }

    public T set( int index, T element ) {
        return members.set( index, element );
    }

    public int size() {
        return members.size();
    }

    public List<T> subList( int fromIndex, int toIndex ) {
        return members.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return members.toArray();
    }

    @SuppressWarnings("hiding")
    public <T> T[] toArray( T[] a ) {
        return members.toArray( a );
    }
    
    @Override
    public Envelope getEnvelope() {
        // TODO NullEnvelope for emtpy aggregates? or throw an exception?
        Envelope result = get( 0 ).getEnvelope();
        for ( Geometry geom : this ) {
            result = result.merge( geom.getEnvelope() );
        }
        return result;
    }
}
