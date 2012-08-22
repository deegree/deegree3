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

package org.deegree.portal.standard.csw.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShoppingCart {

    /**
     * A List of SessionRecord elements.
     */
    private List<SessionRecord> contents;

    /**
     *
     *
     */
    public ShoppingCart() {
        this.contents = new ArrayList<SessionRecord>( 10 );
    }

    /**
     * @param contents
     *            A List of SessionRecord elements.
     */
    public ShoppingCart( List contents ) {
        this();
        setContents( contents );
    }

    /**
     * @param sr
     *            A SessionRecord to add to the ShoppingCart contents. Cannot be null.
     */
    public void add( SessionRecord sr ) {
        if ( sr == null ) {
            throw new NullPointerException( "Session record cannot be null" );
        }
        if ( !this.contents.contains( sr ) ) {
            this.contents.add( sr );
        }
    }

    /**
     * @param sr
     */
    public void remove( SessionRecord sr ) {
        this.contents.remove( sr );
    }

    /**
     * @param sessionRecords
     */
    public void removeAll( List sessionRecords ) {
        this.contents.removeAll( sessionRecords );
    }

    /**
     *
     */
    public void clear() {
        this.contents.clear();
    }

    /**
     * @return Returns the contents of the ShoppingCart.
     */
    public List getContents() {
        return contents;
    }

    /**
     * @param contents
     *            The contents to set.
     * @throws RuntimeException
     *             if the passed List contains elements other than SessionRecord.
     */
    public void setContents( List contents ) {

        for ( int i = 0; i < contents.size(); i++ ) {
            if ( contents.get( i ) instanceof SessionRecord ) {
                add( (SessionRecord) contents.get( i ) );
            } else {
                throw new RuntimeException( "The list passed to the constructor contains elements "
                                            + "that are not of the type SessionRecord." );
            }
        }

    }

}
