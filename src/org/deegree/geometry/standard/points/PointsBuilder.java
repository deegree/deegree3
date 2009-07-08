//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.geometry.standard.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class PointsBuilder extends PointsList implements List<Point> { 

    public PointsBuilder () {
        super (new LinkedList<Point>());
    }
    
    public PointsBuilder (int size) {
        super (new ArrayList<Point>(size));
    }

    public void add( Points controlPoints ) {
        for ( Point point : controlPoints ) {
            this.points.add( point );
        }
    }       
    
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

    public boolean equals( Object o ) {
        return points.equals( o );
    }

    public Point get( int index ) {
        return points.get( index );
    }

    public int hashCode() {
        return points.hashCode();
    }

    public int indexOf( Object o ) {
        return points.indexOf( o );
    }

    public boolean isEmpty() {
        return points.isEmpty();
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
}
