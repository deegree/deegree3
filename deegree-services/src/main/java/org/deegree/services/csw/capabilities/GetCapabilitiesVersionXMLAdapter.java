//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.csw.capabilities;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.ows.capabilities.GetCapabilitiesXMLParser;

/**
 * Encapsulates the method for parsing a XML request via Http-POST. Due to the fact that the GetCapabilities operation
 * is common OWS specific for all webservices the parsing is delegated to the deegree core module.
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetCapabilitiesVersionXMLAdapter extends GetCapabilitiesXMLParser {

    /**
     * Parses an incoming XML request via Http-POST
     * 
     * @param version
     *            that is parsed from the request in the GetCapabilities
     * @return {@link GetCapabilities} request
     */
    public GetCapabilities parse( Version version ) {
        GetCapabilities request = null;
        if ( version != null ) {
            // @version present -> treat as CSW [version] request

            request = new GetCapabilities( version );
        } else {
            // else treat as OWS 1.0.0
            request = parse100();
        }
        return request;
    }

}
