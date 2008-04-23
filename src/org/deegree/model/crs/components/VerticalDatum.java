//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.model.crs.components;

/**
 * A <code>VerticalDatum</code> is a datum which only has one axis. It is used for vertical measurements.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class VerticalDatum extends Datum {

    /**
     * @param identifiers
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public VerticalDatum( String[] identifiers, String[] names, String[] versions, String[] descriptions,
                          String[] areasOfUse ) {
        super( identifiers, names, versions, descriptions, areasOfUse );
    }

    /**
     * @param identifier
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public VerticalDatum( String identifier, String name, String version, String description, String areaOfUse ) {
        super( new String[] { identifier }, new String[] { name }, new String[] { version },
               new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * @param identifier
     */
    public VerticalDatum( String identifier ) {
        this( new String[] { identifier }, null, null, null, null );
    }

}
