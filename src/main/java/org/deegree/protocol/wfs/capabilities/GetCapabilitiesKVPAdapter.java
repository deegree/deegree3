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
package org.deegree.protocol.wfs.capabilities;

import java.util.Map;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.ows.capabilities.GetCapabilitiesKVPParser;

/**
 * Adapter between KVP encoded <code>GetCapabilities</code> requests (WFS) and {@link GetCapabilities} objects.
 * <p>
 * TODO code for exporting
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetCapabilitiesKVPAdapter {

    /**
     * Parses a normalized KVP-map as a WFS {@link GetCapabilities} request.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.0.0</li>
     * <li>WFS 1.1.0</li>
     * </ul>
     * 
     * @param version
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return {@link GetCapabilities} request
     * @throws InvalidParameterValueException
     *             if a parameter (e.g. ACCEPTVERSIONS) contains a syntactical error
     */
    public static GetCapabilities parse( Version version, Map<String, String> kvpParams )
                            throws InvalidParameterValueException {
        GetCapabilities request = null;
        if ( version == null ) {
            // @version not present -> treat as WFS 1.0.0 request
            request = new GetCapabilities( version );
        } else {
            // else treat as WFS 1.1.0 request (-> OWS 1.0.0)
            request = GetCapabilitiesKVPParser.parse( kvpParams );
        }
        return request;
    }
}
