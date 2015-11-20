//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.sql.version;

/**
 * Parses supported versions from string;
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class VersionParser {

    /**
     * Discriminates the allowed version strings from Filter Encoding 2.0.
     * 
     * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
     */
    public enum VersionCode {
        FIRST, LATEST, NEXT, PREVIOUS, ALL
    }

    /**
     * Tries to parse the version as integer value.
     * 
     * @param version
     *            to parse, may be <code>null</code>
     * @return the version as int value if it is an int value, -1 otherwise
     */
    public static int parseVersionInteger( String version ) {
        try {
            int parsedVersion = Integer.parseInt( version );
            if ( parsedVersion > 0 )
                return parsedVersion;
        } catch ( NumberFormatException e ) {
        }
        return -1;
    }

    /**
     * Tries to parse the version as {@link VersionCode}.
     * 
     * @param version
     *            to parse, may be <code>null</code>
     * @return the version as VersionCode if it is an supported VersionCode (case insensitive), <code>null</code>
     *         otherwise
     */
    public static VersionCode getVersionCode( String version ) {
        if ( version != null ) {
            try {
                return VersionCode.valueOf( version.toUpperCase() );
            } catch ( IllegalArgumentException e ) {
            }
        }
        return null;
    }

}