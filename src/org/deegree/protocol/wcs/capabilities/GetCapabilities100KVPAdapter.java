//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.protocol.wcs.capabilities;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.protocol.ows.capabilities.GetCapabilities;

/**
 * This is a KVP adapter for WCS 1.0.0 GetCapabilities requests.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetCapabilities100KVPAdapter {

    /**
     * @param kvpParams
     * @return {@link GetCapabilities} request
     * @throws InvalidParameterValueException
     *             if VERSION parameter contains a syntactical error
     */
    public static GetCapabilities parse( Map<String, String> kvpParams )
                            throws InvalidParameterValueException {

        // VERSION (optional)
        String versionString = kvpParams.get( "VERSION" );
        if ( versionString == null || versionString.length() == 0 ) {
            versionString = "1.0.0";
        }
        List<Version> acceptVersions = new LinkedList<Version>();
        acceptVersions.add( Version.parseVersion( versionString ) );

        // SECTION (optional)
        List<String> sections = KVPUtils.splitAll( kvpParams, "SECTION" );

        // UPDATESEQUENCE (optional)
        String updateSequence = KVPUtils.getDefault( kvpParams, "UPDATESEQUENCE", "" );

        return new GetCapabilities( acceptVersions, sections, null, updateSequence, null );
    }
}
