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

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.CRS;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link CompositeSolid}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultCompositeSolid extends AbstractDefaultGeometry implements CompositeSolid {

    private List<Solid> memberSolids;

    /**
     * Creates a new {@link DefaultCompositeSolid} from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param memberSolids
     * 
     */
    public DefaultCompositeSolid( String id, CRS crs, PrecisionModel pm, List<Solid> memberSolids ) {
        super( id, crs, pm );
        this.memberSolids = memberSolids;
    }

    @Override
    public int getCoordinateDimension() {
        return memberSolids.get( 0 ).getCoordinateDimension();
    }  

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Solid;
    }

    @Override
    public SolidType getSolidType() {
        return SolidType.CompositeSolid;
    }

    @Override
    public Surface getExteriorSurface() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Surface> getInteriorSurfaces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Measure getVolume( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }

    // -----------------------------------------------------------------------
    // delegate methods for List<Solid>
    // -----------------------------------------------------------------------

    public void add( int index, Solid element ) {
        memberSolids.add( index, element );
    }

    public boolean add( Solid e ) {
        return memberSolids.add( e );
    }

    public boolean addAll( Collection<? extends Solid> c ) {
        return memberSolids.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends Solid> c ) {
        return memberSolids.addAll( index, c );
    }

    public void clear() {
        memberSolids.clear();
    }

    public boolean contains( Object o ) {
        return memberSolids.contains( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return memberSolids.containsAll( c );
    }

    public Solid get( int index ) {
        return memberSolids.get( index );
    }

    public int indexOf( Object o ) {
        return memberSolids.indexOf( o );
    }

    public boolean isEmpty() {
        return memberSolids.isEmpty();
    }

    public Iterator<Solid> iterator() {
        return memberSolids.iterator();
    }

    public int lastIndexOf( Object o ) {
        return memberSolids.lastIndexOf( o );
    }

    public ListIterator<Solid> listIterator() {
        return memberSolids.listIterator();
    }

    public ListIterator<Solid> listIterator( int index ) {
        return memberSolids.listIterator( index );
    }

    public Solid remove( int index ) {
        return memberSolids.remove( index );
    }

    public boolean remove( Object o ) {
        return memberSolids.remove( o );
    }

    public boolean removeAll( Collection<?> c ) {
        return memberSolids.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return memberSolids.retainAll( c );
    }

    public Solid set( int index, Solid element ) {
        return memberSolids.set( index, element );
    }

    public int size() {
        return memberSolids.size();
    }

    public List<Solid> subList( int fromIndex, int toIndex ) {
        return memberSolids.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return memberSolids.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return memberSolids.toArray( a );
    }
}
