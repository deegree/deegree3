//$HeadURL$
/*
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

package org.deegree.model.metadata.iso19115;

import java.util.ArrayList;

/**
 * Address object.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Address {

    private String administrativearea = null;

    private String city = null;

    private String country = null;

    private ArrayList<String> deliverypoint = null;

    private ArrayList<String> electronicmailaddress = null;

    private String postalcode = null;

    /**
     * Address
     *
     * @param administrativearea
     * @param city
     * @param country
     * @param deliverypoint
     * @param electronicmailaddress
     * @param postalcode
     */
    public Address( String administrativearea, String city, String country, String[] deliverypoint,
                    String[] electronicmailaddress, String postalcode ) {

        this.deliverypoint = new ArrayList<String>();
        this.electronicmailaddress = new ArrayList<String>();

        setAdministrativeArea( administrativearea );
        setCity( city );
        setCountry( country );
        setDeliveryPoint( deliverypoint );
        setElectronicMailAddress( electronicmailaddress );
        setPostalCode( postalcode );
    }

    /**
     *
     * @return Administrative Area
     */
    public String getAdministrativeArea() {
        return administrativearea;
    }

    /**
     * @param administrativearea
     */
    public void setAdministrativeArea( String administrativearea ) {
        this.administrativearea = administrativearea;
    }

    /**
     *
     * @return city name
     */
    public String getCity() {
        return city;
    }

    /**
     * @see Address#getCity()
     *
     * @param city
     */
    public void setCity( String city ) {
        this.city = city;
    }

    /**
     *
     * @return country name
     */
    public String getCountry() {
        return country;
    }

    /**
     * @see Address#getCountry()
     *
     * @param country
     */
    public void setCountry( String country ) {
        this.country = country;
    }

    /**
     * @return Delivery Points
     */
    public String[] getDeliveryPoint() {
        return deliverypoint.toArray( new String[deliverypoint.size()] );
    }

    /**
     * @see Address#getDeliveryPoint()
     * @param deliverypoint
     */
    public void addDeliveryPoint( String deliverypoint ) {
        this.deliverypoint.add( deliverypoint );
    }

    /**
     * @see Address#getDeliveryPoint()
     * @param deliverypoint
     */
    public void setDeliveryPoint( String[] deliverypoint ) {
        this.deliverypoint.clear();
        for ( int i = 0; i < deliverypoint.length; i++ ) {
            this.deliverypoint.add( deliverypoint[i] );
        }
    }

    /**
     * @return Electronic Mail Addresses
     */
    public String[] getElectronicMailAddress() {
        return electronicmailaddress.toArray( new String[electronicmailaddress.size()] );
    }

    /**
     * @see Address#getElectronicMailAddress()
     * @param electronicmailaddress
     */
    public void addElectronicMailAddress( String electronicmailaddress ) {
        this.electronicmailaddress.add( electronicmailaddress );
    }

    /**
     * @see Address#getElectronicMailAddress()
     * @param electronicmailaddress
     */
    public void setElectronicMailAddress( String[] electronicmailaddress ) {
        this.electronicmailaddress.clear();
        for ( int i = 0; i < electronicmailaddress.length; i++ ) {
            this.electronicmailaddress.add( electronicmailaddress[i] );
        }
    }

    /**
     * @return postal code
     */
    public String getPostalCode() {
        return postalcode;
    }

    /**
     * @see Address#getPostalCode()
     * @param postalcode
     */
    public void setPostalCode( String postalcode ) {
        this.postalcode = postalcode;
    }

    /**
     * tpString method
     *
     * @return string representation
     */
    public String toString() {
        String ret = "administrativearea = " + administrativearea + "\n" + "city = " + city + "\n" + "country = "
                     + country + "\n" + "deliverypoint = " + deliverypoint + "\n" + "electronicmailaddress = "
                     + electronicmailaddress + "\n" + "postalcode =" + postalcode + "\n";
        return ret;
    }

}
