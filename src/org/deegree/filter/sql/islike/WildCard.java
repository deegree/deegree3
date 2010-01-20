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
package org.deegree.filter.sql.islike;

/**
 * Part of a {@link IsLikeString} -- represents any number of characters (in SQL: '%').
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
final class WildCard implements IsLikeStringPart {

    /**
     * Returns an encoding that is suitable for arguments of "IS LIKE"-clauses in SQL.
     * <p>
     * This means:
     * <ul>
     * <li>wildCard: encoded as the '%'-character</li>
     * <li>singleChar: encoded as the '_'-character</li>
     * <li>escape: encoded as the '\'-character</li>
     * </ul>
     *
     * @return encoded string
     */
    public String toSQL() {
        return "%";
    }

    /**
     * Returns an encoding that is suitable for arguments of "IS LIKE"-clauses in SQL.
     * <p>
     * This means:
     * <ul>
     * <li>wildCard: encoded as the '%'-character</li>
     * <li>singleChar: encoded as the '_'-character</li>
     * <li>escape: encoded as the '\'-character</li>
     * </ul>
     *
     * @param toLowerCase
     *            true means: convert to lowercase letters
     * @return encoded string
     */
    public String toSQL( boolean toLowerCase ) {
        return "%";
    }

    @Override
    public String toString() {
        return "WildCard";
    }
}
