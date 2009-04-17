// $HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/framework/xml/XMLFragment.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.commons.filter;

/**
 * A <code>Filter</code> is a boolean expression (often with spatial conditions) that can be tested against
 * {@link MatchableObject}s.
 * 
 * @see IdFilter
 * @see OperatorFilter
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface Filter {

    /**
     * Convenience enum type for discriminating the different filter types.
     */
    public enum Type {
        /** Filter that matches objects with certain ids. The object is an instance of {@link IdFilter}. */
        ID_FILTER,
        /**
         * Filter that matches objects that match a certain expression. The object is an instance of
         * {@link OperatorFilter}.
         */
        OPERATOR_FILTER;
    }

    /**
     * Returns the type of filter. Use this to safely determine the subtype of {@link Filter}.
     * 
     * @return type of filter (id or expression based)
     */
    public Type getType();

    /**
     * Determines if the given {@link MatchableObject} matches this <code>Filter</code>.
     * 
     * @param object
     *            {@link MatchableObject} to be tested
     * @return true, if the <code>Filter</code> evaluates to true, else false
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException;

}
