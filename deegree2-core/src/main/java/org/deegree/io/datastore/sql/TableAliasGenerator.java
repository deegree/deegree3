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
package org.deegree.io.datastore.sql;

/**
 * Responsible for the generation of unique table aliases. This is needed in SQL queries in order to
 * built joins that use the same table more than once.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TableAliasGenerator {

    private static String DEFAULT_PREFIX = "X";

    private String prefix;

    private int currentIdx = 1;

    /**
     * Creates a new <code>TableAliasGenerator</code> instance that uses a default prefix for
     * generated table aliases.
     */
    public TableAliasGenerator() {
        this.prefix = DEFAULT_PREFIX;
    }

    /**
     * Creates a new <code>TableAliasGenerator</code> instance that uses the given prefix for
     * generated table aliases.
     *
     * @param prefix
     *            prefix for generated table aliases
     */
    public TableAliasGenerator( String prefix ) {
        this.prefix = prefix;
    }

    /**
     * Returns a unique alias.
     *
     * @return a unique alias
     */
    public String generateUniqueAlias() {
        return this.prefix
            + this.currentIdx++;
    }

    /**
     * Returns the specified number of unique aliases.
     *
     * @param n
     * @return the specified number of unique aliases
     */
    public String[] generateUniqueAliases( int n ) {
        String[] aliases = new String[n];
        for (int i = 0; i < aliases.length; i++) {
            aliases[i] = generateUniqueAlias();
        }
        return aliases;
    }

    /**
     * Resets the alias sequence.
     */
    public void reset() {
        this.currentIdx = 1;
    }
}
