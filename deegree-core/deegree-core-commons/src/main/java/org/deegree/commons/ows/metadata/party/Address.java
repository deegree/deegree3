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
package org.deegree.commons.ows.metadata.party;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>Address</code> bean encapsulates the corresponding GetCapabilities response metadata element.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Address {

    private List<String> deliveryPoint;

    private String city;

    private String administrativeArea;

    private String postalCode;

    private String country;

    private List<String> electronicMailAddress;

    /**
     * @param deliveryPoint
     *            the deliveryPoint to set
     */
    public void setDeliveryPoint( List<String> deliveryPoint ) {
        this.deliveryPoint = deliveryPoint;
    }

    /**
     * @param electronicMailAddress
     *            the electronicMailAddress to set
     */
    public void setElectronicMailAddress( List<String> electronicMailAddress ) {
        this.electronicMailAddress = electronicMailAddress;
    }

    /**
     * @return deliveryPoint, never <code>null</code>
     */
    public List<String> getDeliveryPoint() {
        if ( deliveryPoint == null ) {
            deliveryPoint = new ArrayList<String>();
        }
        return deliveryPoint;
    }

    /**
     * @param city
     */
    public void setCity( String city ) {
        this.city = city;
    }

    /**
     * @return city, may be <code>null</code>
     */
    public String getCity() {
        return city;
    }

    /**
     * @param administrativeArea
     */
    public void setAdministrativeArea( String administrativeArea ) {
        this.administrativeArea = administrativeArea;
    }

    /**
     * @return administrativeArea, may be <code>null</code>
     */
    public String getAdministrativeArea() {
        return administrativeArea;
    }

    /**
     * @param postalCode
     */
    public void setPostalCode( String postalCode ) {
        this.postalCode = postalCode;
    }

    /**
     * @return postalCode, may be <code>null</code>
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @param country
     */
    public void setCountry( String country ) {
        this.country = country;
    }

    /**
     * @return country, may be <code>null</code>
     */
    public String getCountry() {
        return country;
    }

    /**
     * @return electronicMailAddress, never <code>null</code>
     */
    public List<String> getElectronicMailAddress() {
        if ( electronicMailAddress == null ) {
            electronicMailAddress = new ArrayList<String>();
        }
        return electronicMailAddress;
    }

    /**
     * Returns whether any information on the physical address is available.
     * 
     * @returns <code>true</code>, if any physical information is available, <code>false</code> otherwise
     */
    public boolean isPhysicalInfoAvailable() {
        return !getDeliveryPoint().isEmpty() || getCity() != null || getAdministrativeArea() != null
               || getPostalCode() != null || getCountry() != null;
    }
}
