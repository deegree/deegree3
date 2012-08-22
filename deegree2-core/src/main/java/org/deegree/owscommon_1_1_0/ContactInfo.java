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

package org.deegree.owscommon_1_1_0;

import java.util.List;

import org.deegree.framework.util.Pair;

/**
 * <code>ContactInfo</code> wraps all ows 1.1.0 contact info in a bean.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class ContactInfo {

    private final Pair<List<String>, List<String>> phone;

    private final boolean hasAdress;

    private final List<String> deliveryPoint;

    private final String city;

    private final String administrativeArea;

    private final String postalCode;

    private final String country;

    private final List<String> electronicMailAddress;

    private final String onlineResource;

    private final String hoursOfService;

    private final String contactInstructions;

    /**
     * @param phone
     *            as &lt;list&lt;Voice&gt;,list&lt;fax&gt;&gt pair, or <code>null</code> if no
     *            phone was given.
     * @param hasAdress
     * @param deliveryPoint
     *            (s) from address.
     * @param city
     *            from address
     * @param administrativeArea
     *            from address
     * @param postalCode
     *            from address
     * @param country
     *            from address
     * @param electronicMailAddress
     *            from address
     * @param onlineResource
     * @param hoursOfService
     * @param contactInstructions
     */
    public ContactInfo( Pair<List<String>, List<String>> phone, boolean hasAdress, List<String> deliveryPoint,
                        String city, String administrativeArea, String postalCode, String country,
                        List<String> electronicMailAddress, String onlineResource, String hoursOfService,
                        String contactInstructions ) {
        this.phone = phone;
        this.hasAdress = hasAdress;
        this.deliveryPoint = deliveryPoint;
        this.city = city;
        this.administrativeArea = administrativeArea;
        this.postalCode = postalCode;
        this.country = country;
        this.electronicMailAddress = electronicMailAddress;
        this.onlineResource = onlineResource;
        this.hoursOfService = hoursOfService;
        this.contactInstructions = contactInstructions;
    }

    /**
     * @return the phone as &lt;list&lt;Voice&gt;,list&lt;fax&gt;&gt pair, or <code>null</code> if
     *         no phone was given.
     */
    public final Pair<List<String>, List<String>> getPhone() {
        return phone;
    }

    /**
     * @return true if this contactinfo has an adress.
     */
    public final boolean hasAdress() {
        return hasAdress;
    }

    /**
     * @return the deliveryPoint from address
     */
    public final List<String> getDeliveryPoint() {
        return deliveryPoint;
    }

    /**
     * @return the city from address
     */
    public final String getCity() {
        return city;
    }

    /**
     * @return the administrativeArea from address
     */
    public final String getAdministrativeArea() {
        return administrativeArea;
    }

    /**
     * @return the postalCode. from address
     */
    public final String getPostalCode() {
        return postalCode;
    }

    /**
     * @return the country from address
     */
    public final String getCountry() {
        return country;
    }

    /**
     * @return the electronicMailAddress from address
     */
    public final List<String> getElectronicMailAddress() {
        return electronicMailAddress;
    }

    /**
     * @return the onlineResource.
     */
    public final String getOnlineResource() {
        return onlineResource;
    }

    /**
     * @return the hoursOfService.
     */
    public final String getHoursOfService() {
        return hoursOfService;
    }

    /**
     * @return the contactInstructions.
     */
    public final String getContactInstructions() {
        return contactInstructions;
    }

}
