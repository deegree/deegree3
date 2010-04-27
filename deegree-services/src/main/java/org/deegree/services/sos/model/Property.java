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
package org.deegree.services.sos.model;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Property {
    private final String name;

    private final String shortName;

    private final String uom;

    /**
     * @param name
     */
    public Property( String name ) {
        this( name, name );
    }

    /**
     * @param name
     * @param shortName
     */
    public Property( String name, String shortName ) {
        this( name, shortName, null );
    }

    /**
     * @param name
     * @param shortName
     * @param uom
     */
    public Property( String name, String shortName, String uom ) {
        this.name = name;
        this.shortName = shortName;
        this.uom = uom;
    }

    /**
     * @return the short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @return the name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * @return the name of the Unit of Measure
     */
    public String getUOM() {
        return uom;
    }

    /**
     * @return true if the property has a UOM
     */
    public boolean hasUOM() {
        return uom != null;
    }
}
