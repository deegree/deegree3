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
package org.deegree.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link Filter} that matches objects with certain ids.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class IdFilter implements Filter {

    private Set<String> matchingIds;

    /**
     * Creates a new {@link IdFilter} that matches the objects with the given ids.
     *
     * @param ids
     *            ids of the objects that the filter will match
     */
    public IdFilter( String...ids ) {
        this.matchingIds = new HashSet<String>();
        for ( String id : ids ) {
            matchingIds.add( id );
        }
    }

    /**
     * Creates a new {@link IdFilter} that matches the objects with the given ids.
     *
     * @param matchingIds
     *            ids of the objects that the filter will match
     */
    public IdFilter( Set<String> matchingIds ) {
        this.matchingIds = matchingIds;
    }

    /**
     * Always returns {@link Filter.Type#ID_FILTER} (for {@link IdFilter} instances).
     *
     * @return {@link Filter.Type#ID_FILTER}
     */
    @Override
    public Type getType() {
        return Type.ID_FILTER;
    }

    /**
     * Returns the ids of the objects that this filter matches.
     *
     * @return the ids of the objects that this filter matches
     */
    public Set<String> getMatchingIds() {
        return matchingIds;
    }

    @Override
    public boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException {
        String id = object.getId();
        if ( id != null ) {
            return matchingIds.contains( id );
        }
        return false;
    }
}
