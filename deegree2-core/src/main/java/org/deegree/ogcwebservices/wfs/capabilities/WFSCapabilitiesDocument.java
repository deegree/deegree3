// $HeadURL$
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
package org.deegree.ogcwebservices.wfs.capabilities;

import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;

/**
 * Represents a capabilities document for an OGC WFS 1.1.0 compliant web service.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSCapabilitiesDocument extends WFSCapabilitiesDocument_1_1_0 {

    private static final long serialVersionUID = 3975709039097932869L;

    /**
     * Creates a class representation of the document.
     *
     * @return class representation of the capabilities document
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {
        OGCCapabilities capabilities = null;

        String version = getRootElement().getAttribute( "version" );
        if ( "1.0.0".equals( version ) ) {
            WFSCapabilitiesDocument_1_0_0 capabilitiesDoc = new WFSCapabilitiesDocument_1_0_0();
            capabilitiesDoc.setRootElement( getRootElement() );
            capabilities = capabilitiesDoc.parseCapabilities();
        } else {
            // treat as 1.1.0 by default
            WFSCapabilitiesDocument_1_1_0 capabilitiesDoc = new WFSCapabilitiesDocument_1_1_0();
            capabilitiesDoc.setRootElement( getRootElement() );
            capabilities = capabilitiesDoc.parseCapabilities();
        }
        return capabilities;
    }
}
