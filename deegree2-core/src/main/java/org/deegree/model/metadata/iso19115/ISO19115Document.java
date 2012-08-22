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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.Code;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * Parser class that can parse various elements defined in the OWS subset of the ISO 19115
 * specification.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class ISO19115Document extends XMLFragment {

    private static final long serialVersionUID = -5536802360612196021L;

    private static final String POWS = CommonNamespaces.OWS_PREFIX + ":";

    private static final String PXLINK = CommonNamespaces.XLINK_PREFIX + ":";

    /**
     * @param root
     *            an element of type ows:ResponsiblePartySubsetType
     * @return the data object
     * @throws XMLParsingException
     */
    public CitedResponsibleParty parseCitedResponsibleParty( Element root )
                            throws XMLParsingException {
        String individualName = XMLTools.getNodeAsString( root, POWS + "IndividualName", nsContext, null );

        String positionName = XMLTools.getNodeAsString( root, POWS + "PositionName", nsContext, null );

        Element roleElement = (Element) XMLTools.getNode( root, POWS + "Role", nsContext );
        RoleCode role = null;
        if ( roleElement != null )
            role = parseRoleCode( roleElement );

        Element contactElement = (Element) XMLTools.getNode( root, POWS + "ContactInfo", nsContext );
        ContactInfo contactInfo = null;
        if ( contactElement != null )
            contactInfo = parseContactInfo( contactElement );

        // why Lists/Arrays are necessary here is beyond my knowledge
        List<String> name = new ArrayList<String>();
        name.add( individualName );
        List<String> pName = new ArrayList<String>();
        pName.add( positionName );
        List<RoleCode> roles = new ArrayList<RoleCode>();
        roles.add( role );
        List<ContactInfo> contactInfos = new ArrayList<ContactInfo>();
        contactInfos.add( contactInfo );

        CitedResponsibleParty result = new CitedResponsibleParty( contactInfos, name, null, pName, roles );
        return result;
    }

    /**
     * @param root
     *            the ContactInfo element
     * @return the <code>ContactInfo</code> data object
     * @throws XMLParsingException
     */
    public ContactInfo parseContactInfo( Element root )
                            throws XMLParsingException {
        Element phoneElement = (Element) XMLTools.getNode( root, POWS + "Phone", nsContext );
        Phone phone = null;
        if ( phoneElement != null )
            phone = parsePhone( phoneElement );

        Address address = null;
        Element addressElement = (Element) XMLTools.getNode( root, POWS + "Address", nsContext );
        if ( addressElement != null )
            address = parseAddress( addressElement );

        OnlineResource onlineResource = null;
        Element onlineResourceElement = (Element) XMLTools.getNode( root, POWS + "OnlineResource", nsContext );
        if ( onlineResourceElement != null )
            onlineResource = parseOnlineResource( onlineResourceElement );

        String hoursOfService = XMLTools.getNodeAsString( root, POWS + "HoursOfService", nsContext, null );
        String contactInstructions = XMLTools.getNodeAsString( root, POWS + "ContactInstructions", nsContext, null );

        ContactInfo result = new ContactInfo( address, contactInstructions, hoursOfService, onlineResource, phone );
        return result;
    }

    /**
     * @param root
     *            the Address element
     * @return the <code>Address</code> data object
     * @throws XMLParsingException
     */
    public Address parseAddress( Element root )
                            throws XMLParsingException {
        String[] deliveryPoint = XMLTools.getNodesAsStrings( root, POWS + "DeliveryPoint", nsContext );
        String city = XMLTools.getNodeAsString( root, POWS + "City", nsContext, null );
        String administrativeArea = XMLTools.getNodeAsString( root, POWS + "AdministrativeArea", nsContext, null );
        String postalCode = XMLTools.getNodeAsString( root, POWS + "PostalCode", nsContext, null );
        String country = XMLTools.getNodeAsString( root, POWS + "Country", nsContext, null );
        String[] emails = XMLTools.getNodesAsStrings( root, POWS + "ElectronicMailAddress", nsContext );

        Address result = new Address( administrativeArea, city, country, deliveryPoint, emails, postalCode );
        return result;
    }

    /**
     * @param root
     *            the Phone element
     * @return a <code>Phone</code> data object
     * @throws XMLParsingException
     */
    public Phone parsePhone( Element root )
                            throws XMLParsingException {
        String[] voice = XMLTools.getNodesAsStrings( root, POWS + "Voice", nsContext );

        String[] facsimile = XMLTools.getNodesAsStrings( root, POWS + "Facsimile", nsContext );

        Phone result = new Phone( facsimile, voice );
        return result;
    }

    /**
     * @param root
     *            the element containing the xlink attributes
     * @return the <code>OnlineResource</data> data object
     * @throws XMLParsingException
     */
    public OnlineResource parseOnlineResource( Element root )
                            throws XMLParsingException {
        // This is just a preview version, not sure how to handle all the xlink attributes
        // correctly.

        URL href = null;
        String url = null;

        try {
            url = XMLTools.getNodeAsString( root, "@" + PXLINK + "href", nsContext, null );
            if ( url != null )
                href = new URL( url );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "'" + url + "' is not a valid URL." );
        }

        Linkage linkage = new Linkage( href );
        OnlineResource result = new OnlineResource( linkage );
        return result;
    }

    /**
     * @param root
     *            the Code element
     * @return a <code>Code</code> data object
     */
    public Code parseCode( Element root ) {
        URI codeSpace = null;
        try {
            codeSpace = new URI( XMLTools.getAttrValue( root, null, "codeSpace", null ) );
        } catch ( Exception e ) {
            // ignore codeSpace
        }

        String code = XMLTools.getStringValue( root );

        if ( codeSpace != null )
            return new Code( code, codeSpace );
        return new Code( code );
    }

    /**
     * @param root
     *            the Type element
     * @return the <code>TypeCode</code> data object
     */
    public TypeCode parseTypeCode( Element root ) {
        Code code = parseCode( root );
        // since the TypeCode class already existed, it is used. Deleting the TypeCode class and
        // just using the Code class would be the better solution, though.
        return new TypeCode( code.getCode(), code.getCodeSpace() );
    }

    /**
     * @param root
     *            the Role element
     * @return the <code>RoleCode</code> data object
     */
    public RoleCode parseRoleCode( Element root ) {
        Code code = parseCode( root );
        // since the RoleCode class already existed, it is used. Deleting the RoleCode class and
        // just using the Code class would be the better solution, though.
        return new RoleCode( code.getCode() );
    }

    /**
     * @param root
     *            the AccessConstraints element
     * @param fee
     * @return the <code>Constraints</code> object containing the parsed data
     * @throws XMLParsingException
     */
    public Constraints parseConstraint( Element root, String fee )
                            throws XMLParsingException {
        // please note that the same fee is used for all constraints

        List<String> constr = new ArrayList<String>();
        String str = XMLTools.getRequiredNodeAsString( root, ".", nsContext );
        constr.add( str );

        Constraints result = new Constraints( fee, null, null, null, constr, null, null, null );
        return result;
    }

    /**
     * @param root
     *            the Keywords element
     * @return the <code>Keywords</code> object
     * @throws XMLParsingException
     */
    public Keywords parseKeywords( Element root )
                            throws XMLParsingException {
        String[] keywords = XMLTools.getRequiredNodesAsStrings( root, POWS + "Keyword", nsContext );
        Element codeElem = (Element) XMLTools.getNode( root, POWS + "Type", nsContext );
        TypeCode type = null;
        if ( codeElem != null )
            type = parseTypeCode( codeElem );

        Keywords result = null;

        // the thesaurus name is ignored at the moment, as it is omitted by the OWS specification as
        // well
        if ( type != null )
            result = new Keywords( keywords );
        else
            result = new Keywords( keywords, "", type );

        return result;
    }

}
