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
package org.deegree.ogcwebservices.getcapabilities;

/**
 * Represents the capabilities for an OGC-Webservice <u>prior</u> to the
 * <code>OWS Common Implementation Specification 0.2</code>.
 * <p>
 * It consists of the following parts:<table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Occurences</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td>Service</td>
 * <td align="center">1</td>
 * <td>Provides metadata of the service.</td>
 * </tr>
 * <tr>
 * <td>Capability</td>
 * <td align="center">1</td>
 * <td>Provides properties and capabilities of the service.</td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public abstract class OGCStandardCapabilities extends OGCCapabilities {

    private static final long serialVersionUID = -5476562066676740906L;

    private Service service = null;

    private Capability capabilitiy = null;

    /**
     * @param version
     * @param updateSequence
     * @param service
     * @param capabilitiy
     */
    public OGCStandardCapabilities( String version, String updateSequence, Service service, Capability capabilitiy ) {
        super( version, updateSequence );
        this.service = service;
        this.capabilitiy = capabilitiy;
    }

    /**
     * Returns the Capabilitiy part of the configuration.
     *
     * @return the Capabilitiy part of the configuration.
     */
    public Capability getCapabilitiy() {
        return capabilitiy;
    }

    /**
     * Sets the Capabilitiy part of the configuration.
     *
     * @param capabilitiy
     */
    public void setCapabilitiy( Capability capabilitiy ) {
        this.capabilitiy = capabilitiy;
    }

    /**
     * Returns the Service part of the configuration.
     *
     * @return the Service part of the configuration.
     */
    public Service getService() {
        return service;
    }

    /**
     * Sets the Service part of the configuration.
     *
     * @param service
     */
    public void setService( Service service ) {
        this.service = service;
    }

}
