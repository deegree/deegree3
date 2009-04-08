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

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperMultiCurve extends JTSWrapperGeometry implements MultiCurve {
    
    private List<Curve> curves;

    /**
     *
     * @param id
     * @param curves
     * @param coordinateDimension
     * @param precision
     * @param crs
     */
    public JTSWrapperMultiCurve( String id, List<Curve> curves, int coordinateDimension, double precision, CoordinateSystem crs ) {
        super( id, precision, crs, coordinateDimension );
        this.curves = curves;        
        LineString[] ls = new LinearRing[curves.size()];
        int i = 0;
        for ( Curve curve : curves ) {
            if ( curve instanceof JTSWrapperLineString ) {
                ls[i++] = (LineString) ((JTSWrapperGeometry) curve ).getJTSGeometry();
            } else {
                ls[i++] = (LineString)export( curve );
            }
        }
        geometry = jtsFactory.createMultiLineString( ls );
    }

    /* (non-Javadoc)
     * @see org.deegree.geometry.multi.MultiCurve#getLength()
     */
    public double getLength() {
        return ((MultiLineString)geometry).getLength();
    }

    /* (non-Javadoc)
     * @see org.deegree.geometry.multi.MultiGeometry#getCentroid()
     */
    public Point getCentroid() {
        return toPoint( ((MultiLineString)geometry).getCentroid().getCoordinate() );
    }

    // -----------------------------------------------------------------------
    // implementation of List<Curve>
    // -----------------------------------------------------------------------    

    @Override
    public boolean add( Curve e ) {
        return curves.add( e );
    }

    @Override
    public void add( int index, Curve element ) {
        curves.add( index, element );
    }

    @Override
    public boolean addAll( Collection<? extends Curve> c ) {
        return curves.addAll(c);
    }

    @Override
    public boolean addAll( int index, Collection<? extends Curve> c ) {
        return curves.addAll(index, c);
    }

    @Override
    public void clear() {
        curves.clear();
    }

    @Override
    public boolean contains( Object o ) {
        return curves.contains( o );
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        return curves.containsAll( c );
    }

    @Override
    public Curve get( int index ) {     
        return curves.get( index );
    }

    @Override
    public int indexOf( Object o ) {
        return curves.indexOf (o);
    }

    @Override
    public boolean isEmpty() {
        return curves.isEmpty();
    }

    @Override
    public Iterator<Curve> iterator() {     
        return curves.iterator();
    }

    @Override
    public int lastIndexOf( Object o ) {
        return curves.lastIndexOf( o );
    }

    @Override
    public ListIterator<Curve> listIterator() {
        return curves.listIterator();
    }

    @Override
    public ListIterator<Curve> listIterator( int index ) {
        return curves.listIterator(index);
    }

    @Override
    public boolean remove( Object o ) {        
        return curves.remove (o);
    }

    @Override
    public Curve remove( int index ) {
        return curves.remove (index);
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        return curves.removeAll (c);
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        return curves.retainAll (c);
    }

    @Override
    public Curve set( int index, Curve element ) {
        return curves.set (index, element);
    }

    @Override
    public int size() {
        return curves.size();
    }

    @Override
    public List<Curve> subList( int fromIndex, int toIndex ) {
        return curves.subList( fromIndex, toIndex );
    }

    @Override
    public Object[] toArray() {
        return curves.toArray();
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        return curves.toArray(a);
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_GEOMETRY;
    }
}
