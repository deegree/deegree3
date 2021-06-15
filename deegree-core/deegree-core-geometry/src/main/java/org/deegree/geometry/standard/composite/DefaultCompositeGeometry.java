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
package org.deegree.geometry.standard.composite;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

import org.locationtech.jts.geom.GeometryCollection;

/**
 * Default implementation of {@link CompositeGeometry}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultCompositeGeometry extends AbstractDefaultGeometry implements CompositeGeometry<GeometricPrimitive> {

    private List<GeometricPrimitive> memberPrimitives;

    /**
     * Creates a new {@link DefaultCompositeGeometry} from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param memberPrimitives
     */
    public DefaultCompositeGeometry( String id, ICRS crs, PrecisionModel pm,
                                     List<GeometricPrimitive> memberPrimitives ) {
        super( id, crs, pm );
        this.memberPrimitives = memberPrimitives;
    }

    @Override
    public int getCoordinateDimension() {
        return memberPrimitives.get( 0 ).getCoordinateDimension();
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

    @Override
    protected GeometryCollection buildJTSGeometry() {
        org.locationtech.jts.geom.Geometry[] jtsMembers = new org.locationtech.jts.geom.Geometry[size()];
        int i = 0;
        for ( Geometry geometry : memberPrimitives ) {
            jtsMembers[i++] = getAsDefaultGeometry( geometry ).getJTSGeometry();
        }
        return jtsFactory.createGeometryCollection( jtsMembers );
    }
}
