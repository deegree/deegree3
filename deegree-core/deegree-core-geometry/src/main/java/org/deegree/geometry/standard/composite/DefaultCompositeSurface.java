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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link CompositeSurface}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultCompositeSurface extends AbstractDefaultGeometry implements CompositeSurface {

    private List<Surface> memberSurfaces;

    /**
     * Creates a new {@link DefaultCompositeSurface} from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param memberSurfaces
     *            surfaces that constitute the composited surface, the surfaces must join in pairs on common boundary
     *            curves and must, when considered as
     */
    public DefaultCompositeSurface( String id, ICRS crs, PrecisionModel pm, List<Surface> memberSurfaces ) {
        super( id, crs, pm );
        this.memberSurfaces = memberSurfaces;
    }

    @Override
    public int getCoordinateDimension() {
        return memberSurfaces.get( 0 ).getCoordinateDimension();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Surface;
    }

    @Override
    public SurfaceType getSurfaceType() {
        return SurfaceType.CompositeSurface;
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Measure getPerimeter( Unit requestedUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getCentroid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SurfacePatch> getPatches() {
        List<SurfacePatch> allPatches = new LinkedList<SurfacePatch>();
        for ( Surface member : memberSurfaces ) {
            allPatches.addAll( member.getPatches() );
        }
        return allPatches;
    }

    
    @Override
    protected org.locationtech.jts.geom.MultiPolygon buildJTSGeometry() {
        org.locationtech.jts.geom.Polygon [] jtsMembers = new org.locationtech.jts.geom.Polygon[size()];
        int i = 0;
        for ( Surface geometry : memberSurfaces ) {
            jtsMembers[i++] = (org.locationtech.jts.geom.Polygon) getAsDefaultGeometry( geometry ).getJTSGeometry();
        }
        return jtsFactory.createMultiPolygon( jtsMembers );
    }    

    // -----------------------------------------------------------------------
    // delegate methods for List<Surface>
    // -----------------------------------------------------------------------

    public void add( int index, Surface element ) {
        memberSurfaces.add( index, element );
    }

    public boolean add( Surface e ) {
        return memberSurfaces.add( e );
    }

    public boolean addAll( Collection<? extends Surface> c ) {
        return memberSurfaces.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends Surface> c ) {
        return memberSurfaces.addAll( index, c );
    }

    public void clear() {
        memberSurfaces.clear();
    }

    public boolean contains( Object o ) {
        return memberSurfaces.contains( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return memberSurfaces.containsAll( c );
    }

    public Surface get( int index ) {
        return memberSurfaces.get( index );
    }

    public int indexOf( Object o ) {
        return memberSurfaces.indexOf( o );
    }

    public boolean isEmpty() {
        return memberSurfaces.isEmpty();
    }

    public Iterator<Surface> iterator() {
        return memberSurfaces.iterator();
    }

    public int lastIndexOf( Object o ) {
        return memberSurfaces.lastIndexOf( o );
    }

    public ListIterator<Surface> listIterator() {
        return memberSurfaces.listIterator();
    }

    public ListIterator<Surface> listIterator( int index ) {
        return memberSurfaces.listIterator( index );
    }

    public Surface remove( int index ) {
        return memberSurfaces.remove( index );
    }

    public boolean remove( Object o ) {
        return memberSurfaces.remove( o );
    }

    public boolean removeAll( Collection<?> c ) {
        return memberSurfaces.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return memberSurfaces.retainAll( c );
    }

    public Surface set( int index, Surface element ) {
        return memberSurfaces.set( index, element );
    }

    public int size() {
        return memberSurfaces.size();
    }

    public List<Surface> subList( int fromIndex, int toIndex ) {
        return memberSurfaces.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return memberSurfaces.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return memberSurfaces.toArray( a );
    }

    @Override
    public Points getExteriorRingCoordinates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Points> getInteriorRingsCoordinates() {
        throw new UnsupportedOperationException();
    }
}
