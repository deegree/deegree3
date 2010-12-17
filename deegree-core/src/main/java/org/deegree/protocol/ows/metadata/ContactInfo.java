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
package org.deegree.protocol.ows.metadata;

import java.net.URL;

/**
 * The <code>ContactInfo</code> bean encapsulates the corresponding GetCapabilities response metadata element.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ContactInfo {

    private Telephone phone;

    private Address address;

    private URL onlineResource;

    private String hoursOfService;

    private String contactInstructions;

    /**
     * @param phone
     */
    public void setPhone( Telephone phone ) {
        this.phone = phone;
    }

    /**
     * @return phone, may be <code>null</code>
     */
    public Telephone getPhone() {
        return phone;
    }

    /**
     * @param address
     */
    public void setAddress( Address address ) {
        this.address = address;
    }

    /**
     * @return address, may be <code>null</code>
     */
    public Address getAddress() {
        return address;
    }

    /**
     * @param onlineResource
     */
    public void setOnlineResource( URL onlineResource ) {
        this.onlineResource = onlineResource;
    }

    /**
     * @return onlineResource, may be <code>null</code>
     */
    public URL getOnlineResource() {
        return onlineResource;
    }

    /**
     * @param hoursOfService
     */
    public void setHoursOfService( String hoursOfService ) {
        this.hoursOfService = hoursOfService;
    }

    /**
     * @return hoursOfService, may be <code>null</code>
     */
    public String getHoursOfService() {
        return hoursOfService;
    }

    /**
     * @param contactInstructions
     */
    public void setContactInstructions( String contactInstructions ) {
        this.contactInstructions = contactInstructions;
    }

    /**
     * @return contactInstructions, may be <code>null</code>
     */
    public String getContactInstruction() {
        return contactInstructions;
    }

}
