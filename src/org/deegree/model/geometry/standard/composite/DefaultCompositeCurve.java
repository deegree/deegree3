//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.composite.CompositeCurve;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.LineString;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link CompositeCurve}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultCompositeCurve extends AbstractDefaultGeometry implements CompositeCurve {

    private List<Curve> memberCurves;

    /**
     * Creates a new {@link DefaultCompositeCurve} from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param memberCurves
     *            curves that constitute the composited curve, each curve must end at the start point of the subsequent
     *            curve in the list
     */
    public DefaultCompositeCurve( String id, CoordinateSystem crs, List<Curve> memberCurves ) {
        super( id, crs );
        this.memberCurves = memberCurves;
    }
    
    @Override
    public CurveType getCurveType() {
        return CurveType.CompositeCurve;
    }

    @Override
    public List<Point> getBoundary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CurveSegment> getCurveSegments() {
        List<CurveSegment> allSegments = new LinkedList<CurveSegment>();
        for ( Curve member : memberCurves ) {
            allSegments.addAll( member.getCurveSegments() );
        }
        return allSegments;
    }

    @Override
    public Point getStartPoint() {
        return memberCurves.get( 0 ).getStartPoint();
    }

    @Override
    public Point getEndPoint() {
        return memberCurves.get( memberCurves.size() - 1 ).getEndPoint();
    }

    @Override
    public double getLength() {
        double sum = 0.0;
        for ( Curve member : memberCurves ) {
            sum += member.getLength();
        }
        return sum;
    }

    @Override
    public boolean isClosed() {
        return getStartPoint().equals( getEndPoint() );
    }

    // -----------------------------------------------------------------------
    // delegate methods for List<Curve>
    // -----------------------------------------------------------------------

    public boolean add( Curve e ) {
        return memberCurves.add( e );
    }

    public void add( int index, Curve element ) {
        memberCurves.add( index, element );
    }

    public boolean addAll( Collection<? extends Curve> c ) {
        return memberCurves.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends Curve> c ) {
        return memberCurves.addAll( index, c );
    }

    public void clear() {
        memberCurves.clear();
    }

    public boolean contains( Object o ) {
        return memberCurves.contains( o );
    }

    public boolean containsAll( Collection<?> c ) {
        return memberCurves.containsAll( c );
    }

    public Curve get( int index ) {
        return memberCurves.get( index );
    }

    public int indexOf( Object o ) {
        return memberCurves.indexOf( o );
    }

    public boolean isEmpty() {
        return memberCurves.isEmpty();
    }

    public Iterator<Curve> iterator() {
        return memberCurves.iterator();
    }

    public int lastIndexOf( Object o ) {
        return memberCurves.lastIndexOf( o );
    }

    public ListIterator<Curve> listIterator() {
        return memberCurves.listIterator();
    }

    public ListIterator<Curve> listIterator( int index ) {
        return memberCurves.listIterator( index );
    }

    public Curve remove( int index ) {
        return memberCurves.remove( index );
    }

    public boolean remove( Object o ) {
        return memberCurves.remove( o );
    }

    public boolean removeAll( Collection<?> c ) {
        return memberCurves.removeAll( c );
    }

    public boolean retainAll( Collection<?> c ) {
        return memberCurves.retainAll( c );
    }

    public Curve set( int index, Curve element ) {
        return memberCurves.set( index, element );
    }

    public int size() {
        return memberCurves.size();
    }

    public List<Curve> subList( int fromIndex, int toIndex ) {
        return memberCurves.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return memberCurves.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return memberCurves.toArray( a );
    }

    @Override
    public LineString getAsLineString() {
        // TODO Auto-generated method stub
        return null;
    }
}
