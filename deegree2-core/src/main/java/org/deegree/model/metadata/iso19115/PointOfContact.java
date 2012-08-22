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

package org.deegree.model.metadata.iso19115;

import java.util.ArrayList;

/**
 * PointOfContact.java
 *
 * Created on 16. September 2002, 10:31
 */
public class PointOfContact {

    private ArrayList<ContactInfo> contactinfo = null;

    private ArrayList<String> individualname = null;

    private ArrayList<String> organisationname = null;

    private ArrayList<String> positionname = null;

    private ArrayList<RoleCode> rolecode = null;

    /** Creates a new instance of PointOfContact */
    public PointOfContact( ContactInfo[] contactinfo, String[] individualname, String[] organisationname,
                           String[] positionname, RoleCode[] rolecode ) {

        this.contactinfo = new ArrayList<ContactInfo>();
        this.individualname = new ArrayList<String>();
        this.organisationname = new ArrayList<String>();
        this.positionname = new ArrayList<String>();
        this.rolecode = new ArrayList<RoleCode>();

        setContactInfo( contactinfo );
        setIndividualName( individualname );
        setOrganisationName( organisationname );
        setPositionName( positionname );
        setRoleCode( rolecode );
    }

    /**
     * @return ContactInfo-Array
     *
     */
    public ContactInfo[] getContactInfo() {
        return contactinfo.toArray( new ContactInfo[contactinfo.size()] );
    }

    /**
     * @see #getContactInfo()
     */
    public void addContactInfo( ContactInfo contactinfo ) {
        this.contactinfo.add( contactinfo );
    }

    /**
     * @see #getContactInfo()
     */
    public void setContactInfo( ContactInfo[] contactinfo ) {
        this.contactinfo.clear();
        for ( int i = 0; i < contactinfo.length; i++ ) {
            this.contactinfo.add( contactinfo[i] );
        }
    }

    /**
     * @return String-Array
     *
     */
    public String[] getIndividualName() {
        return individualname.toArray( new String[individualname.size()] );
    }

    /**
     * @see #getIndividualName()
     */
    public void addIndividualName( String individualname ) {
        this.individualname.add( individualname );
    }

    /**
     * @see #getIndividualName()
     */
    public void setIndividualName( String[] individualname ) {
        this.individualname.clear();
        for ( int i = 0; i < individualname.length; i++ ) {
            this.individualname.add( individualname[i] );
        }
    }

    /**
     *
     * @return String-Array
     */
    public String[] getOrganisationName() {
        return organisationname.toArray( new String[organisationname.size()] );
    }

    /**
     * @see #getOrganisationName()
     */
    public void addOrganisationName( String organisationname ) {
        this.organisationname.add( organisationname );
    }

    /**
     * @see #getOrganisationName()
     */
    public void setOrganisationName( String[] organisationname ) {
        this.organisationname.clear();
        for ( int i = 0; i < organisationname.length; i++ ) {
            this.organisationname.add( organisationname[i] );
        }
    }

    /**
     * @return String-Array
     *
     */
    public String[] getPositionName() {
        return positionname.toArray( new String[positionname.size()] );
    }

    /**
     * @see #getPositionName()
     */
    public void addPositionName( String positionname ) {
        this.positionname.add( positionname );
    }

    /**
     * @see #getPositionName()
     */
    public void setPositionName( String[] positionname ) {
        this.positionname.clear();
        for ( int i = 0; i < positionname.length; i++ ) {
            this.positionname.add( positionname[i] );
        }
    }

    /**
     * @return RoleCode-Array
     *
     */
    public RoleCode[] getRoleCode() {
        return rolecode.toArray( new RoleCode[rolecode.size()] );
    }

    /**
     * @see #getRoleCode()
     */
    public void addRoleCode( RoleCode rolecode ) {
        this.rolecode.add( rolecode );
    }

    /**
     * @see #getRoleCode()
     */
    public void setRoleCode( RoleCode[] rolecode ) {
        this.rolecode.clear();
        for ( int i = 0; i < rolecode.length; i++ ) {
            this.rolecode.add( rolecode[i] );
        }
    }

    /**
     * to String method
     */
    public String toString() {
        String ret = null;
        ret = "contactinfo = " + contactinfo + "\n";
        ret += "individualname = " + individualname + "\n";
        ret += "organisationname = " + organisationname + "\n";
        ret += "positionname = " + positionname + "\n";
        ret += "rolecode = " + rolecode + "\n";
        return ret;
    }
}
