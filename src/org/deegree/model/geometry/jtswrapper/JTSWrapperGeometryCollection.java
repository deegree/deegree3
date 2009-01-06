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

package org.deegree.model.geometry.jtswrapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.primitive.Point;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperGeometryCollection extends JTSWrapperGeometry implements MultiGeometry<Geometry> {

    private List<Geometry> geometries;
    private Collection<?> c;

    /**
     * @param id 
     * @param precision
     * @param crs
     * @param coordinateDimension
     * @param geometries
     */
    public JTSWrapperGeometryCollection( String id, double precision, CoordinateSystem crs, int coordinateDimension,
                                         List<Geometry> geometries ) {
        super( id, precision, crs, coordinateDimension );
        this.geometries = geometries;
        com.vividsolutions.jts.geom.Geometry[] gs = new com.vividsolutions.jts.geom.Geometry[geometries.size()];
        int i = 0;
        for ( Geometry geom : geometries ) {
            if ( geom instanceof JTSWrapperGeometry ) {
                gs[i++] = ((JTSWrapperGeometry) geom ).getJTSGeometry();
            } else {
                gs[i++] = export( geom );
            }
        }
        geometry = jtsFactory.createGeometryCollection( gs );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.multi.MultiGeometry#getCentroid()
     */
    public Point getCentroid() {
        return toPoint( geometry.getCentroid().getCoordinate() );
    }

    // -----------------------------------------------------------------------
    // implementation of List<Geometry>
    // -----------------------------------------------------------------------    
    
    public boolean add( Geometry e ) {
        return geometries.add( e );
    }

    public void add( int index, Geometry element ) {
        geometries.add( index, element );
    }

    public boolean addAll( Collection<? extends Geometry> c ) {
        return geometries.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends Geometry> c ) {
        return geometries.addAll( index, c );
    }

    public void clear() {
        geometries.clear();
    }

    public boolean contains( Object o ) {
        return geometries.contains( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return geometries.containsAll( c );
    }

    public Geometry get( int index ) {
        return geometries.get( index );
    }

    public int indexOf( Object o ) {
        return geometries.indexOf( o );
    }

    public boolean isEmpty() {
        return geometries.isEmpty();
    }

    public Iterator<Geometry> iterator() {
        return geometries.iterator();
    }

    public int lastIndexOf( Object o ) {
        return geometries.lastIndexOf( o );
    }

    public ListIterator<Geometry> listIterator() {
        return geometries.listIterator();
    }

    public ListIterator<Geometry> listIterator( int index ) {
        return geometries.listIterator( index );
    }

    public Geometry remove( int index ) {
        return geometries.remove( index );
    }

    public boolean remove( Object o ) {
        return geometries.remove( o );
    }

    public boolean removeAll( Collection<?> c ) {
        return geometries.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return geometries.retainAll( c );
    }

    public Geometry set( int index, Geometry element ) {
        return geometries.set( index, element );
    }

    public int size() {
        return geometries.size();
    }

    public List<Geometry> subList( int fromIndex, int toIndex ) {
        return geometries.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return geometries.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return geometries.toArray( a );
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_GEOMETRY;
    }
}
