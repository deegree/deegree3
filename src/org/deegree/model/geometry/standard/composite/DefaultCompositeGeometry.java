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
package org.deegree.model.geometry.standard.composite;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.composite.CompositeGeometry;
import org.deegree.model.geometry.primitive.GeometricPrimitive;
import org.deegree.model.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link CompositeGeometry}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultCompositeGeometry extends AbstractDefaultGeometry implements CompositeGeometry<GeometricPrimitive> {

    private List<GeometricPrimitive> memberPrimitives;

    /**
     * Creates a new {@link DefaultCompositeGeometry} from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param memberPrimitives
     */
    public DefaultCompositeGeometry( String id, CoordinateSystem crs, List<GeometricPrimitive> memberPrimitives ) {
        super( id, crs );
        this.memberPrimitives = memberPrimitives;
    }

    public boolean add( GeometricPrimitive e ) {
        return memberPrimitives.add( e );
    }

    public void add( int index, GeometricPrimitive element ) {
        memberPrimitives.add( index, element );
    }

    public boolean addAll( Collection<? extends GeometricPrimitive> c ) {
        return memberPrimitives.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends GeometricPrimitive> c ) {
        return memberPrimitives.addAll( index, c );
    }

    public void clear() {
        memberPrimitives.clear();
    }

    public boolean contains( Object o ) {
        return memberPrimitives.contains( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return memberPrimitives.containsAll( c );
    }

    public GeometricPrimitive get( int index ) {
        return memberPrimitives.get( index );
    }

    public int indexOf( Object o ) {
        return memberPrimitives.indexOf( o );
    }

    public boolean isEmpty() {
        return memberPrimitives.isEmpty();
    }

    public Iterator<GeometricPrimitive> iterator() {
        return memberPrimitives.iterator();
    }

    public int lastIndexOf( Object o ) {
        return memberPrimitives.lastIndexOf( o );
    }

    public ListIterator<GeometricPrimitive> listIterator() {
        return memberPrimitives.listIterator();
    }

    public ListIterator<GeometricPrimitive> listIterator( int index ) {
        return memberPrimitives.listIterator( index );
    }

    public GeometricPrimitive remove( int index ) {
        return memberPrimitives.remove( index );
    }

    public boolean remove( Object o ) {
        return memberPrimitives.remove( o );
    }

    public boolean removeAll( Collection<?> c ) {
        return memberPrimitives.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return memberPrimitives.retainAll( c );
    }

    public GeometricPrimitive set( int index, GeometricPrimitive element ) {
        return memberPrimitives.set( index, element );
    }

    public int size() {
        return memberPrimitives.size();
    }

    public List<GeometricPrimitive> subList( int fromIndex, int toIndex ) {
        return memberPrimitives.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return memberPrimitives.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return memberPrimitives.toArray( a );
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.COMPOSITE_GEOMETRY;
    }
}
