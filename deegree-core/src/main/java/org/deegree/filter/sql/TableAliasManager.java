//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/src/test/java/org/deegree/feature/persistence/postgis/PostGISFeatureStoreTest.java $
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
package org.deegree.filter.sql;

import org.deegree.filter.expression.PropertyName;

/**
 * Creates and tracks table aliases that are needed for mapping {@link PropertyName}s to a relational schema.
 * 
 * @see AbstractWhereBuilder
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 25462 $, $Date: 2010-07-21 18:45:40 +0200 (Mi, 21. Jul 2010) $
 */
public class TableAliasManager {

    private final String rootTableAlias;

    private int currentIdx = 1;

    /**
     * Creates a new {@link TableAliasManager} instance.
     */
    public TableAliasManager() {
        rootTableAlias = generateNew();
    }

    /**
     * Returns the table alias for the root table.
     * 
     * @return the table alias for the root table, never <code>null</code>
     */
    public String getRootTableAlias() {
        return rootTableAlias;
    }

    /**
     * Returns a new unique table alias.
     * 
     * @return a new unique table alias, never <code>null</code>
     */
    public String generateNew() {
        return "X" + ( currentIdx++ );
    }
}
