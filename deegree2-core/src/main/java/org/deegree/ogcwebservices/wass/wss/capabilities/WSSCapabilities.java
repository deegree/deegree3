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

package org.deegree.ogcwebservices.wass.wss.capabilities;

import java.util.ArrayList;

import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wass.common.OWSCapabilitiesBaseType_1_0;
import org.deegree.ogcwebservices.wass.common.OperationsMetadata_1_0;
import org.deegree.ogcwebservices.wass.common.SupportedAuthenticationMethod;

/**
 * A <code>WSSCapabilities</code> class encapsulates all the data which can be requested with a
 * GetCapabilities request. It's base class is OWSCapabilitiesBaseType_1_0 which is conform the
 * gdi-nrw access control version 1.0 specification.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class WSSCapabilities extends OWSCapabilitiesBaseType_1_0 {
    /**
     *
     */
    private static final long serialVersionUID = 2181625093642200664L;

    private String securedServiceType = null;

    /**
     * @param version
     * @param updateSequence
     * @param sf
     * @param sp
     * @param om
     * @param securedServiceType
     * @param am
     */
    public WSSCapabilities( String version, String updateSequence, ServiceIdentification sf,
                           ServiceProvider sp, OperationsMetadata_1_0 om, String securedServiceType,
                           ArrayList<SupportedAuthenticationMethod> am ) {
        super( version, updateSequence, sf, sp, om, am );

        this.securedServiceType = securedServiceType;
    }

    /**
     * @return the securedServiceType.
     */
    public String getSecuredServiceType() {
        return securedServiceType;
    }

}
