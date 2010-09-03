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

import org.deegree.gml.props.GMLStdProps;

/**
 * Default implementation of {@link Dictionary}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenericDictionary extends GenericDefinition implements Dictionary {

    private final List<Definition> members;

    private final boolean isDefinitionCollection;

    /**
     * Creates a new {@link GenericDictionary} instance.
     * 
     * @param id
     *            id of the dictionary, can be <code>null</code>
     * @param gmlProps
     *            GML standard properties (which contain description and names), must not be <code>null</code>
     * @param members
     *            dictionary entries, must not be <code>null</code>
     * @param isDefinitionCollection
     *            true, if this is a defininition collection, false otherwise
     */
    public GenericDictionary( String id, GMLStdProps gmlProps, List<Definition> members, boolean isDefinitionCollection ) {
        super( id, gmlProps );
        this.members = members;
        this.isDefinitionCollection = isDefinitionCollection;
    }

    @Override
    public boolean isDefinitionCollection() {
        return isDefinitionCollection;
    }

    @Override
    public boolean add( Definition e ) {
        return members.add( e );
    }

    @Override
    public void add( int index, Definition element ) {
        members.add( index, element );
    }

    @Override
    public boolean addAll( Collection<? extends Definition> c ) {
        return members.addAll( c );
    }

    @Override
    public boolean addAll( int index, Collection<? extends Definition> c ) {
        return members.addAll( index, c );
    }

    @Override
    public void clear() {
        members.clear();
    }

    @Override
    public boolean contains( Object o ) {
        return members.contains( o );
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        return members.containsAll( c );
    }

    @Override
    public boolean equals( Object o ) {
        return members.equals( o );
    }

    @Override
    public Definition get( int index ) {
        return members.get( index );
    }

    @Override
    public int hashCode() {
        return members.hashCode();
    }

    @Override
    public int indexOf( Object o ) {
        return members.indexOf( o );
    }

    @Override
    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public Iterator<Definition> iterator() {
        return members.iterator();
    }

    @Override
    public int lastIndexOf( Object o ) {
        return members.lastIndexOf( o );
    }

    @Override
    public ListIterator<Definition> listIterator() {
        return members.listIterator();
    }

    @Override
    public ListIterator<Definition> listIterator( int index ) {
        return members.listIterator( index );
    }

    @Override
    public Definition remove( int index ) {
        return members.remove( index );
    }

    @Override
    public boolean remove( Object o ) {
        return members.remove( o );
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        return members.removeAll( c );
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        return members.retainAll( c );
    }

    @Override
    public Definition set( int index, Definition element ) {
        return members.set( index, element );
    }

    @Override
    public int size() {
        return members.size();
    }

    @Override
    public List<Definition> subList( int fromIndex, int toIndex ) {
        return members.subList( fromIndex, toIndex );
    }

    @Override
    public Object[] toArray() {
        return members.toArray();
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        return members.toArray( a );
    }
}
