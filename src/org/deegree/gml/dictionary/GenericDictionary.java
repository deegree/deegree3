//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.gml.dictionary;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.deegree.gml.props.StandardGMLProps;

/**
 * Default implementation of {@link Dictionary}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenericDictionary extends GenericDefinition implements Dictionary {

    private List<Definition> members;

    public GenericDictionary( String id, StandardGMLProps gmlProps, List<Definition> members) {
        super( id, gmlProps );
        this.members = members;
    }

    public boolean add( Definition e ) {
        return members.add( e );
    }

    public void add( int index, Definition element ) {
        members.add( index, element );
    }

    public boolean addAll( Collection<? extends Definition> c ) {
        return members.addAll( c );
    }

    public boolean addAll( int index, Collection<? extends Definition> c ) {
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

    public boolean equals( Object o ) {
        return members.equals( o );
    }

    public Definition get( int index ) {
        return members.get( index );
    }

    public int hashCode() {
        return members.hashCode();
    }

    public int indexOf( Object o ) {
        return members.indexOf( o );
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public Iterator<Definition> iterator() {
        return members.iterator();
    }

    public int lastIndexOf( Object o ) {
        return members.lastIndexOf( o );
    }

    public ListIterator<Definition> listIterator() {
        return members.listIterator();
    }

    public ListIterator<Definition> listIterator( int index ) {
        return members.listIterator( index );
    }

    public Definition remove( int index ) {
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

    public Definition set( int index, Definition element ) {
        return members.set( index, element );
    }

    public int size() {
        return members.size();
    }

    public List<Definition> subList( int fromIndex, int toIndex ) {
        return members.subList( fromIndex, toIndex );
    }

    public Object[] toArray() {
        return members.toArray();
    }

    public <T> T[] toArray( T[] a ) {
        return members.toArray( a );
    }
}
