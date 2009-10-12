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
package org.deegree.protocol.wcs.capabilities;

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
        String version = kvpParams.get( "VERSION" );
        if ( version == null || version.length() == 0 ) {
            version = "1.0.0";
        }

        // SECTION (optional)
        List<String> sections = KVPUtils.splitAll( kvpParams, "SECTION" );

        // UPDATESEQUENCE (optional)
        String updateSequence = KVPUtils.getDefault( kvpParams, "UPDATESEQUENCE", "" );

        return new GetCapabilities( version, sections, null, updateSequence, null );
    }
}
