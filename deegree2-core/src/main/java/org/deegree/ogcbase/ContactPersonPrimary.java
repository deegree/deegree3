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
 * Names the contact person based on ISO 19115.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$, $Date$
 * @since 1.0
 */
public class ContactPersonPrimary {

    private String contactOrganization = null;

    private String contactPerson = null;

    /**
     * constructor initializing the class with ContactPersonPrimary Strings
     *
     * @param contactPerson
     * @param contactOrganization
     */
    public ContactPersonPrimary( String contactPerson, String contactOrganization ) {
        setContactPerson( contactPerson );
        setContactOrganization( contactOrganization );
    }

    /**
     * returns the name of the contact person
     *
     * @return the name of the contact person
     *
     */
    public String getContactPerson() {
        return contactPerson;
    }

    /**
     * sets the name of the contact person
     *
     * @param contactPerson
     *
     */
    public void setContactPerson( String contactPerson ) {
        this.contactPerson = contactPerson;
    }

    /**
     * returns the name of the organization that can be contacted / the contact person works at.
     *
     * @return the name of the organization that can be contacted / the contact person works at.
     *
     */
    public String getContactOrganization() {
        return contactOrganization;
    }

    /**
     * sets the name of the organization that can be contacted / the contact person works at.
     *
     * @param contactOrganization
     *
     */
    public void setContactOrganization( String contactOrganization ) {
        this.contactOrganization = contactOrganization;
    }

}
