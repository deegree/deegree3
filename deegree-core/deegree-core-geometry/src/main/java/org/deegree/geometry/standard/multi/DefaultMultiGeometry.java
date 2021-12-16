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
package org.deegree.geometry.standard.multi;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryException;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

import org.locationtech.jts.geom.GeometryCollection;

/**
 * Default implementation of {@link MultiGeometry}.
 * 
 * @param <T>
 *            type of contained geometry objects
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultMultiGeometry<T extends Geometry> extends AbstractDefaultGeometry implements MultiGeometry<T> {

    protected List<T> members;

    /**
     * Creates a new {@link DefaultMultiGeometry} from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param members
     */
    public DefaultMultiGeometry( String id, ICRS crs, PrecisionModel pm, List<T> members ) {
        super( id, crs, pm );
        this.members = members;
    }

    @Override
    public int getCoordinateDimension() {
        if ( isEmpty() )
            throw new GeometryException( "MultiGeometry is empty, coordinate dimension can not be calculated." );
        return get( 0 ).getCoordinateDimension();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_GEOMETRY;
    }

    @Override
    protected GeometryCollection buildJTSGeometry() {
        org.locationtech.jts.geom.Geometry[] jtsMembers = new org.locationtech.jts.geom.Geometry[size()];
        int i = 0;
        for ( Geometry geometry : members ) {
            jtsMembers[i++] = getAsDefaultGeometry( geometry ).getJTSGeometry();
        }
        return jtsFactory.createGeometryCollection( jtsMembers );
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
        if ( env == null ) {
            if ( isEmpty() )
                throw new GeometryException( "MultiGeometry is empty, coordinate envelope can not be calculated." );
            env = get( 0 ).getEnvelope();
            for ( Geometry geom : this ) {
                env = env.merge( geom.getEnvelope() );
            }
            if ( env.getCoordinateSystem() == null ) {
                env.setCoordinateSystem( crs );
            }
        }
        return env;
    }

    @Override
    public boolean isSFSCompliant() {
        for ( Geometry member : this ) {
            if ( !( member.isSFSCompliant() ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MultiGeometryType getMultiGeometryType() {
        return MultiGeometryType.MULTI_GEOMETRY;
    }
}
