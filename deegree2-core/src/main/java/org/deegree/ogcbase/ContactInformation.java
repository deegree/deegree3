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
 * Identification of, and means of communication with a person and/or organization associated with
 * the service/resource. based on ISO 19115.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$, $Date$
 * @since 1.0
 */

public class ContactInformation {

    private ContactAddress contactAddress = null;

    private ContactPersonPrimary contactPersonPrimary = null;

    private String contactElectronicMailAddress = null;

    private String contactFacsimileTelephone = null;

    private String contactPosition = null;

    private String contactVoiceTelephone = null;

    /**
     * constructor initializing the class with ContactInformation Strings
     *
     * @param contactPosition
     * @param contactVoiceTelephone
     * @param contactFacsimileTelephone
     * @param contactElectronicMailAddress
     * @param contactPersonPrimary
     * @param contactAddress
     */
    public ContactInformation( String contactPosition, String contactVoiceTelephone, String contactFacsimileTelephone,
                               String contactElectronicMailAddress, ContactPersonPrimary contactPersonPrimary,
                               ContactAddress contactAddress ) {
        setContactPosition( contactPosition );
        setContactVoiceTelephone( contactVoiceTelephone );
        setContactFacsimileTelephone( contactFacsimileTelephone );
        setContactElectronicMailAddress( contactElectronicMailAddress );
        setContactPersonPrimary( contactPersonPrimary );
        setContactAddress( contactAddress );
    }

    /**
     * returns a datastructure that contains the name of the contact person and the organization he
     * works for.
     *
     * @return a datastructure that contains the name of the contact person and the organization he
     *         works for.
     */
    public ContactPersonPrimary getContactPersonPrimary() {
        return contactPersonPrimary;
    }

    /**
     * sets a datastructure that contains the name of the contact person and the organization he
     * works for.
     *
     * @param contactPersonPrimary
     */
    public void setContactPersonPrimary( ContactPersonPrimary contactPersonPrimary ) {
        this.contactPersonPrimary = contactPersonPrimary;
    }

    /**
     * returns the positon of the contact person within its organization
     *
     * @return the positon of the contact person within its organization
     */
    public String getContactPosition() {
        return contactPosition;
    }

    /**
     * sets the positon of the contact person within its organization
     *
     * @param contactPosition
     */
    public void setContactPosition( String contactPosition ) {
        this.contactPosition = contactPosition;
    }

    /**
     * returns the address where to reach to contact person
     *
     * @return the address where to reach to contact person
     */
    public ContactAddress getContactAddress() {
        return contactAddress;
    }

    /**
     * sets the address where to reach to contact person
     *
     * @param contactAddress
     */
    public void setContactAddress( ContactAddress contactAddress ) {
        this.contactAddress = contactAddress;
    }

    /**
     * returns the voice Telephone number of the contact person
     *
     * @return the voice Telephone number of the contact person
     */
    public String getContactVoiceTelephone() {
        return contactVoiceTelephone;
    }

    /**
     * sets the voice Telephone number of the contact person
     *
     * @param contactVoiceTelephone
     */
    public void setContactVoiceTelephone( String contactVoiceTelephone ) {
        this.contactVoiceTelephone = contactVoiceTelephone;
    }

    /**
     * returns the facsimile Telephone number of the contact person
     *
     * @return the facsimile Telephone number of the contact person
     */
    public String getContactFacsimileTelephone() {
        return contactFacsimileTelephone;
    }

    /**
     * sets the facsimile Telephone number of the contact person
     *
     * @param contactFacsimileTelephone
     */
    public void setContactFacsimileTelephone( String contactFacsimileTelephone ) {
        this.contactFacsimileTelephone = contactFacsimileTelephone;
    }

    /**
     * returns the email address of the contact person
     *
     * @return the email address of the contact person
     */
    public String getContactElectronicMailAddress() {
        return contactElectronicMailAddress;
    }

    /**
     * sets the email address of the contact person
     *
     * @param contactElectronicMailAddress
     */
    public void setContactElectronicMailAddress( String contactElectronicMailAddress ) {
        this.contactElectronicMailAddress = contactElectronicMailAddress;
    }

}
