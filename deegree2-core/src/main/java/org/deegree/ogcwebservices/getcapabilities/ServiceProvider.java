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
package org.deegree.ogcwebservices.getcapabilities;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.TypeCode;

/**
 * Represents the ServiceProvider section of the capabilities of an OGC compliant web service
 * according to the OGC Common Implementation Specification 0.3.
 *
 * This section corresponds to and expands the SV_ServiceProvider class in ISO 19119.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$
 *
 * @since 2.0
 */

public class ServiceProvider {

    private String providerName;

    private SimpleLink providerSite;

    private String individualName;

    private String positionName;

    private ContactInfo contactInfo;

    private TypeCode role;

    /**
     * Constructs a new ServiceProvider object.
     *
     * @param providerName
     * @param providerSite
     * @param individualName
     * @param positionName
     * @param contactInfo
     * @param role
     */
    public ServiceProvider( String providerName, SimpleLink providerSite, String individualName, String positionName,
                            ContactInfo contactInfo, TypeCode role ) {
        this.providerName = providerName;
        this.providerSite = providerSite;
        this.individualName = individualName;
        this.positionName = positionName;
        this.contactInfo = contactInfo;
        this.role = role;
    }

    /**
     * @return Returns the contactInfo.
     *
     */
    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    /**
     * @param contactInfo
     *            The contactInfo to set.
     *
     */
    public void setContactInfo( ContactInfo contactInfo ) {
        this.contactInfo = contactInfo;
    }

    /**
     * @return Returns the individualName.
     *
     */
    public String getIndividualName() {
        return individualName;
    }

    /**
     * @param individualName
     *            The individualName to set.
     *
     */
    public void setIndividualName( String individualName ) {
        this.individualName = individualName;
    }

    /**
     * @return Returns the positionName.
     *
     */
    public String getPositionName() {
        return positionName;
    }

    /**
     * @param positionName
     *            The positionName to set.
     *
     */
    public void setPositionName( String positionName ) {
        this.positionName = positionName;
    }

    /**
     * @return Returns the providerName.
     *
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * @param providerName
     *            The providerName to set.
     *
     */
    public void setProviderName( String providerName ) {
        this.providerName = providerName;
    }

    /**
     * @return Returns the providerSite.
     *
     */
    public SimpleLink getProviderSite() {
        return providerSite;
    }

    /**
     * @param providerSite
     *            The providerSite to set.
     *
     */
    public void setProviderSite( SimpleLink providerSite ) {
        this.providerSite = providerSite;
    }

    /**
     * @return Returns the role.
     */
    public TypeCode getRole() {
        return role;
    }

    /**
     * @param role
     *            The role to set.
     */
    public void setRole( TypeCode role ) {
        this.role = role;
    }
}
