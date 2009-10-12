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
package org.deegree.commons.types.ows;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;

/**
 * Version of an OWS operation or specification. Comparability of <code>Version</code> objects simplifies tasks like
 * version negotiation.
 * <p>
 * Description from <code>owsCommon.xsd</code>, version 1.1.0:
 *
 * The string value shall contain one x.y.z "version" value (e.g., "2.1.3"). A version number shall contain three
 * non-negative integers separated by decimal points, in the form "x.y.z". The integers y and z shall not exceed 99.
 * <p>
 * Each version shall be for the Implementation Specification (document) and the associated XML Schemas to which
 * requested operations will conform. An Implementation Specification version normally specifies XML Schemas against
 * which an XML encoded operation response must conform and should be validated.
 *
 * @see Comparable
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class Version implements Comparable<Version> {

    private int x;

    private int y;

    private int z;

    /**
     * Constructs a <code>Version</code> for an OWS operation.
     *
     * @param x
     *            major version. Must be a positive integer.
     * @param y
     *            minor version. Must be between 0 and 99.
     * @param z
     *            minor sub version. Must be between 0 and 99.
     * @throws InvalidParameterValueException
     *             if a parameters exceed the allowed range
     */
    public Version( int x, int y, int z ) throws InvalidParameterValueException {

        if ( x < 0 || y < 0 || z < 0 || y > 99 || z > 99 ) {
            String msg = x + "." + y + "." + z + " is not a valid OGC/OWS version value.";
            throw new InvalidParameterValueException( msg );
        }

        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Parses the string argument as a <code>Version</code>.
     * <p>
     * The string value shall contain one x.y.z "version" value (e.g., "2.1.3"). A version number shall contain three
     * non-negative integers separated by decimal points, in the form "x.y.z". The integers y and z shall not exceed 99.
     *
     * @param s
     *            a <code>String</code> containing the <code>Version</code> representation to be parsed
     * @return a corresponding <code>Version</code> object
     * @throws InvalidParameterValueException
     *             if the string does not contain a parsable <code>Version</code>
     */
    public static Version parseVersion( String s )
                            throws InvalidParameterValueException {
        String[] parts = s.split( "\\." );
        if ( parts.length != 3 ) {
            String msg = "String '" + s + " is not a valid OGC/OWS version value.";
            throw new InvalidParameterValueException( msg );
        }

        int x = -1;
        int y = -1;
        int z = -1;

        try {
            x = Integer.parseInt( parts[0] );
            y = Integer.parseInt( parts[1] );
            z = Integer.parseInt( parts[2] );
        } catch ( NumberFormatException e ) {
            String msg = "String '" + s + " is not a valid OGC/OWS version value.";
            throw new InvalidParameterValueException( msg );
        }
        return new Version( x, y, z );
    }

    public int compareTo( Version version ) {
        if ( this.x > version.x ) {
            return 1;
        } else if ( this.x < version.x ) {
            return -1;
        }
        if ( this.y > version.y ) {
            return 1;
        } else if ( this.y < version.y ) {
            return -1;
        }
        if ( this.z > version.z ) {
            return 1;
        } else if ( this.z < version.z ) {
            return -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        // note: 7, 11 and 13 are prime numbers
        int hash = 7 * ( x + 1 );
        hash *= 11 * ( y + 1 );
        hash *= 13 * ( z + 1 );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof Version ) ) {
            return false;
        }
        Version that = (Version) obj;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public String toString() {
        return x + "." + y + "." + z;
    }

    /**
     * Returns a formatted string for presenting a series of versions to a human.
     *
     * @param versions
     *            versions to be listed
     * @return formatted, human-readable string
     */
    public static String getVersionsString( Version... versions ) {
        int i = 0;
        String s = "";
        for ( Version version : versions ) {
            s += "'" + version + "'";
            if ( i++ != versions.length - 1 ) {
                s += ", ";
            }
        }
        return s;
    }
}
