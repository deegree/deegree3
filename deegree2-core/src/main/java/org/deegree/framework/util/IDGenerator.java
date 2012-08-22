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

package org.deegree.framework.util;

/**
 * Produces unique IDs (used to generate Request-IDs, for example).
 * <p>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */
public class IDGenerator {

    /**
     * The only instance of this class.
     */
    private static IDGenerator instance = null;

    /** The current ID. */
    private long id = 0;

    /**
     * Returns the only instance of this class.
     *
     * @return the only instance of IDGenerator
     *
     */
    synchronized public static IDGenerator getInstance() {
        if ( instance == null )
            instance = new IDGenerator();
        return instance;
    }

    /**
     * Generates a completly unique ID.
     *
     * @return a unique ID
     */
    synchronized public long generateUniqueID() {
        return id++;
    }
}
