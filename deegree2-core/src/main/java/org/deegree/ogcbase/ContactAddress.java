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
package org.deegree.ogcbase;

/**
 * Specifies the data structure of a address and the access to its components based on ISO 19115.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$, $Date$
 * @since 1.0
 */
public class ContactAddress {

    private String address = null;

    private String addressType = null;

    private String city = null;

    private String country = null;

    private String postCode = null;

    private String stateOrProvince = null;

    /**
     * constructor initializing the class with ContactAddress Strings
     *
     * @param addressType
     * @param address
     * @param city
     * @param stateOrProvince
     * @param postCode
     * @param country
     */
    public ContactAddress( String addressType, String address, String city, String stateOrProvince, String postCode,
                           String country ) {
        setAddressType( addressType );
        setAddress( address );
        setCity( city );
        setStateOrProvince( stateOrProvince );
        setPostCode( postCode );
        setCountry( country );
    }

    /**
     * returns the address type. e.g. 'postal'
     *
     * @return the address type. e.g. 'postal'
     */
    public String getAddressType() {
        return addressType;
    }

    /**
     * sets the address type. e.g. 'postal'
     *
     * @param addressType
     */
    public void setAddressType( String addressType ) {
        this.addressType = addressType;
    }

    /**
     * returns the address. usally this is the street and number of a building. It also can be a
     * p.o. box
     *
     * @return the address. usally this is the street and number of a building. It also can be a
     *         p.o. box
     */
    public String getAddress() {
        return address;
    }

    /**
     * sets the address. usally this is the street and number of a building. It also can be a p.o.
     * box
     *
     * @param address
     */
    public void setAddress( String address ) {
        this.address = address;
    }

    /**
     * returns the name of the city
     *
     * @return the name of the city
     */
    public String getCity() {
        return city;
    }

    /**
     * sets the name of the city
     *
     * @param city
     */
    public void setCity( String city ) {
        this.city = city;
    }

    /**
     * returns the name of the state or province of the address.
     *
     * @return the name of the state or province of the address.
     */
    public String getStateOrProvince() {
        return stateOrProvince;
    }

    /**
     * sets the name of the state or province of the address.
     *
     * @param stateOrProvince
     */
    public void setStateOrProvince( String stateOrProvince ) {
        this.stateOrProvince = stateOrProvince;
    }

    /**
     * returns the post code. This doesn't contain an abbreviation for the country
     *
     * @return the post code. This doesn't contain an abbreviation for the country
     */
    public String getPostCode() {
        return postCode;
    }

    /**
     * sets the post code. This doesn't contain an abbreviation for the country
     *
     * @param postCode
     */
    public void setPostCode( String postCode ) {
        this.postCode = postCode;
    }

    /**
     * returns the name of the country. this should be the complete name and not an abbreviation.
     *
     * @return the name of the country. this should be the complete name and not an abbreviation.
     */
    public String getCountry() {
        return country;
    }

    /**
     * sets the name of the country. this should be the complete name and not an abbreviation.
     *
     * @param country
     */
    public void setCountry( String country ) {
        this.country = country;
    }

}
