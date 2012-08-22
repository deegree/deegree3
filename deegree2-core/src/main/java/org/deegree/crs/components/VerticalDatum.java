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

package org.deegree.crs.components;

import org.deegree.crs.Identifiable;

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

    private static final long serialVersionUID = 3867174369619805982L;

    /**
     * @param id
     *            of this datum.
     */
    public VerticalDatum( Identifiable id ) {
        super( id );
    }

    /**
     * @param identifiers
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public VerticalDatum( String[] identifiers, String[] names, String[] versions, String[] descriptions,
                          String[] areasOfUse ) {
        this( new Identifiable( identifiers, names, versions, descriptions, areasOfUse ) );
    }

    /**
     * @param identifier
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public VerticalDatum( String identifier, String name, String version, String description, String areaOfUse ) {
        this( new String[] { identifier }, new String[] { name }, new String[] { version },
              new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * @param identifier
     */
    public VerticalDatum( String identifier ) {
        this( new String[] { identifier }, null, null, null, null );
    }

}
