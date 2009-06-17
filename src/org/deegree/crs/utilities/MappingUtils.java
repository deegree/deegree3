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

package org.deegree.crs.utilities;

/**
 * The <code>MappingUtils</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class MappingUtils {
    private static String EPSG_SINGLE = "EPSG:";

    private static String EPSG_DOUBLE = "EPSG::";

    private static String X_OGC = "urn:x-ogc:def:";

    private static String OGC = "urn:ogc:def:";

    /**
     * Match the given code to all known epsg representations, currently:
     * <ul>
     * <li>urn:x-ogc:def:${operationName}:EPSG::${epsgCode} </li>
     * <li>urn:x-ogc:def:${operationName}:EPSG:${epsgCode} </li>
     * <li>urn:ogc:def:${operationName}:EPSG::${epsgCode} </li>
     * <li>urn:ogc:def:${operationName}:EPSG:${epsgCode} </li>
     * <li>EPSG::${epsgCode} </li>
     * <li>EPSG:${epsgCode} </li>
     * <li>Any string containing EPSG:${epsgCode} or EPSG::${epsgCode}
     * </ul>
     *
     * @param compare
     *            the String to compare
     * @param operationName
     *            the name of the 'operation', normally an epsg urn is something like this:
     *            urn:ogc:def:${operationName}:EPSG::1234
     *
     * @param epsgCode
     *            to check
     * @return true if the given code matches the given String.
     */
    public static boolean matchEPSGString( String compare, String operationName, String epsgCode ) {
        return compare != null
               && ( ( EPSG_DOUBLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( EPSG_SINGLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( X_OGC + operationName + ":" + EPSG_SINGLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( X_OGC + operationName + ":" + EPSG_DOUBLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( OGC + operationName + ":" + EPSG_SINGLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( OGC + operationName + ":" + EPSG_DOUBLE + epsgCode ).equalsIgnoreCase( compare )
                    || ( compare.toUpperCase().contains( EPSG_SINGLE + epsgCode ) ) || ( compare.toUpperCase().contains( EPSG_DOUBLE
                                                                                                                         + epsgCode ) ) );

    }
}
