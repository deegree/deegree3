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

package org.deegree.ogcwebservices.wass.common;

/**
 * Encapsulates a Uniform Resource Name (URN) which encodes an authentication method according to
 * the GDI NRW access control specification.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class URN {

    private String urn;

    /**
     * Creates new one from a String.
     *
     * @param urn
     *            the string
     */
    public URN( String urn ) {
        this.urn = urn;
    }

    /**
     * Returns the last part of the name, or null, if it is not a wellformed GDI NRW authentication.
     * method URN.
     *
     * @return the name, or null
     */
    public String getAuthenticationMethod() {
        if ( !isWellformedGDINRW() )
            return null;
        return getLastName();
    }

    /**
     * Returns the last part of the name, or null, if it is not a URN.
     *
     * @return the last part of this URN
     */
    public String getLastName() {
        if ( urn == null )
            return null;
        if ( !urn.startsWith( "urn:" ) )
            return null;
        return urn.substring( urn.lastIndexOf( ':' ) + 1 );
    }

    /**
     * Returns, whether this is a wellformed GDI NRW authentication method URN.
     *
     * @return true, if it is
     */
    public boolean isWellformedGDINRW() {
        if ( urn == null )
            return false;
        String lastName = getLastName();
        if ( urn.startsWith( "urn:x-gdi-nrw:authnMethod:1.0:" ) )
            if ( lastName.equalsIgnoreCase( "password" ) || lastName.equalsIgnoreCase( "was" )
                 || lastName.equalsIgnoreCase( "session" )
                 || lastName.equalsIgnoreCase( "anonymous" ) )
                return true;
        return false;
    }

    /**
     * @param other
     * @return true if other equals this URN
     */
    public boolean equals ( URN other ){
        if( other == null )
            return false;
        if( !other.isWellformedGDINRW() || !this.isWellformedGDINRW() )
            return false;
        return other.getLastName().equals( this.getLastName() );
    }

    @Override
    public String toString() {
        return urn;
    }

}
