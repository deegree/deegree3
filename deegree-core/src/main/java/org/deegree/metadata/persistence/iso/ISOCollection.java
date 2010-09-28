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
package org.deegree.metadata.persistence.iso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataCollection;

/**
 * Implementation of the {@link MetadataCollection} for the ISO Application Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOCollection implements MetadataCollection {

    List<MetadataRecord> memberList = new ArrayList<MetadataRecord>();

    public ISOCollection() {

    }

    @Override
    public boolean add( MetadataRecord o ) {

        return memberList.add( o );
    }

    @Override
    public boolean addAll( Collection<? extends MetadataRecord> c ) {

        return memberList.addAll( c );
    }

    @Override
    public void clear() {
        memberList.clear();

    }

    @Override
    public boolean contains( Object o ) {

        return memberList.contains( o );
    }

    @Override
    public boolean containsAll( Collection<?> c ) {

        return memberList.containsAll( c );
    }

    @Override
    public boolean isEmpty() {

        return memberList.isEmpty();
    }

    @Override
    public Iterator<MetadataRecord> iterator() {

        return memberList.iterator();
    }

    @Override
    public boolean remove( Object o ) {

        return memberList.remove( o );
    }

    @Override
    public boolean removeAll( Collection<?> c ) {

        return memberList.removeAll( c );
    }

    @Override
    public boolean retainAll( Collection<?> c ) {

        return memberList.retainAll( c );
    }

    @Override
    public int size() {

        return memberList.size();
    }

    @Override
    public Object[] toArray() {

        return memberList.toArray();
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        return memberList.toArray( a );
    }

}
