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
package org.deegree.owscommon;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.metadata.iso19115.TypeCode;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Represents a configuration document for an OGC-Webservice according to the
 * <code>OWS Common Implementation Specification 0.3</code>.
 * <p>
 * It consists of the following elements: <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td>ServiceIdentification</td>
 * <td>corresponds to and expands the SV_ServiceIdentification class in ISO 19119</td>
 * </tr>
 * <tr>
 * <td>ServiceProvider</td>
 * <td>corresponds to and expands the SV_ServiceProvider class in ISO 19119 </td>
 * </tr>
 * <tr>
 * <td>OperationsMetadata</td>
 * <td>contains set of Operation elements that each corresponds to and expand the
 * SV_OperationsMetadata class in ISO 19119</td>
 * </tr>
 * <tr>
 * <td>Contents</td>
 * <td>whenever relevant, contains set of elements that each corresponds to the
 * MD_DataIdentification class in ISO 19119 and 19115</td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class OWSCommonCapabilitiesDocument extends OGCCapabilitiesDocument {

    /**
     *
     */
    public final static String ALL_NAME = "All";

    /**
     *
     */
    public final static String SERVICE_IDENTIFICATION_NAME = "ServiceIdentification";

    /**
     *
     */
    public final static String SERVICE_PROVIDER_NAME = "ServiceProvider";

    /**
     *
     */
    public final static String OPERATIONS_METADATA_NAME = "OperationsMetadata";

    /**
     *
     */
    public final static String CONTENTS_NAME = "Contents";

    protected static final URI OWSNS = CommonNamespaces.OWSNS;

    protected static final URI OGCNS = CommonNamespaces.OGCNS;

    /**
     * Returns the class representation for the <code>ServiceProvider</code> section of the
     * document.
     *
     * @return class representation for the <code>ServiceProvider</code> section
     * @throws XMLParsingException
     */
    public ServiceProvider getServiceProvider()
                            throws XMLParsingException {

        Element element = XMLTools.getRequiredChildElement( "ServiceProvider", OWSNS, getRootElement() );

        // 'ProviderName' element (optional, default value: 'deegree')
        String providerName = XMLTools.getStringValue( "ProviderName", OWSNS, element, "deegree" );

        // 'ProviderSite' element (optional)
        Element providerSiteElement = XMLTools.getChildElement( "ProviderSite", OWSNS, element );
        SimpleLink providerSite = null;
        if ( providerSiteElement != null ) {
            providerSite = parseSimpleLink( providerSiteElement );
        }

        // 'ServiceContact' element (mandatory)
        Element serviceContactElement = XMLTools.getRequiredChildElement( "ServiceContact", OWSNS, element );

        // 'IndividualName' element (optional)
        String individualName = XMLTools.getStringValue( "IndividualName", OWSNS, serviceContactElement, null );

        // 'PositionName' element (optional)
        String positionName = XMLTools.getStringValue( "PositionName", OWSNS, serviceContactElement, null );

        // 'ContactInfo' element (optional)
        ContactInfo contactInfo = null;
        Element contactInfoElement = XMLTools.getChildElement( "ContactInfo", OWSNS, serviceContactElement );
        if ( contactInfoElement != null ) {
            contactInfo = getContactInfo( contactInfoElement );
        }
        TypeCode role = null;
        Element roleElement = (Element) XMLTools.getNode( serviceContactElement, "ows:Role", nsContext );
        if ( roleElement != null ) {
            role = getCodeType( roleElement );
        }
        ServiceProvider serviceProvider = new ServiceProvider( providerName, providerSite, individualName,
                                                               positionName, contactInfo, role );

        return serviceProvider;
    }

    /**
     * Returns the class representation for the <code>ServiceIdentification</code> section of the
     * document.
     *
     * @return class representation for the <code>ServiceIdentification</code> section
     * @throws XMLParsingException
     */
    public ServiceIdentification getServiceIdentification()
                            throws XMLParsingException {

        Element element = XMLTools.getRequiredChildElement( "ServiceIdentification", OWSNS, getRootElement() );

        // 'ServiceType' element (mandatory)
        Element serviceTypeElement = XMLTools.getRequiredChildElement( "ServiceType", OWSNS, element );
        Code serviceType = null;
        try {
            String codeSpace = XMLTools.getAttrValue( serviceTypeElement, OWSNS, "codeSpace", null );
            URI uri = codeSpace != null ? new URI( codeSpace ) : null;
            serviceType = new Code( XMLTools.getStringValue( serviceTypeElement ), uri );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "Given value '"
                                           + XMLTools.getAttrValue( serviceTypeElement, OWSNS, "codeSpace", null )
                                           + "' in attribute 'codeSpace' of element 'ServiceType' " + "(namespace: '"
                                           + OWSNS + "') is not a valid URI." );
        }

        // 'ServiceTypeVersion' elements (mandatory)
        String[] serviceTypeVersions = XMLTools.getRequiredNodeAsStrings( element, "ows:ServiceTypeVersion", nsContext,
                                                                          ",;" );

        // 'Title' element (mandatory)
        String title = XMLTools.getRequiredStringValue( "Title", OWSNS, element );

        // 'Abstract' element (optional)
        String serviceAbstract = XMLTools.getRequiredStringValue( "Abstract", OWSNS, element );

        // 'Keywords' elements (optional)
        List<Element> keywordsList = XMLTools.getElements( element, "ows:Keywords", nsContext );
        Keywords[] keywords = getKeywords( keywordsList );

        // 'Fees' element (optional)
        String fees = XMLTools.getStringValue( "Fees", OWSNS, element, null );

        // 'AccessConstraints' elements (optional)
        String accessConstraints[] = XMLTools.getNodesAsStrings( element, "ows:AccessConstraints", nsContext );

        ServiceIdentification serviceIdentification = new ServiceIdentification( serviceType, serviceTypeVersions,
                                                                                 title, serviceAbstract, keywords,
                                                                                 fees, accessConstraints );

        return serviceIdentification;
    }

    /**
     * Creates a <code>Keywords</code> instance from the given element of type
     * <code>ows:KeywordsType</code>.
     *
     * NOTE: This method is redefined here (it is already defined in <code>OGCDocument</code>),
     * because the spelling of the first letter ('K') changed in the OWS Common Implementation
     * Specification 0.2 from lowercase to uppercase.
     *
     * @param element
     * @return created <code>Keywords</code>
     * @throws XMLParsingException
     */
    protected Keywords getKeywords( Element element )
                            throws XMLParsingException {
        TypeCode codeType = null;
        Element codeTypeElement = (Element) XMLTools.getNode( element, "ows:Type", nsContext );
        if ( codeTypeElement != null ) {
            codeType = getCodeType( codeTypeElement );
        }
        Keywords keywords = new Keywords( XMLTools.getNodesAsStrings( element, "ows:Keyword/text()", nsContext ), null,
                                          codeType );
        return keywords;
    }

    /**
     * Creates an array of <code> Keywords </code> instances from the passed list of elements of
     * type <code> ows:KeywordsType </code>.
     *
     * This may appear to be pretty superfluous (as one <code> ows:KeywordsType
     * </code> can hold
     * several elements of type <code> ows:Keyword
     * </code>.
     *
     * @param nl
     *            may be null
     * @return created array of <code> Keywords </code>, null if <code>NodeList</code> constains
     *         zero elements
     * @throws XMLParsingException
     */
    public Keywords[] getKeywords( List nl )
                            throws XMLParsingException {
        Keywords[] kws = null;
        if ( nl.size() > 0 ) {
            kws = new Keywords[nl.size()];
            for ( int i = 0; i < kws.length; i++ ) {
                kws[i] = getKeywords( (Element) nl.get( i ) );
            }
        }
        return kws;
    }

    /**
     * Creates a <code>DCPType</code> object from the passed <code>DCP</code> element.
     * <p>
     * NOTE: Currently the <code>OnlineResources</code> included in the <code>DCPType</code> are
     * just stored as simple <code>URLs</code> (not as <code>OnLineResource</code> instances)!
     * <p>
     * NOTE: In an <code>OGCStandardCapabilitiesDocument</code> the <code>XLinks</code> (the
     * <code>URLs</code>) are stored in separate elements (<code>OnlineResource</code>), in
     * an <code>OGCCommonCapabilitiesDocument</code> they are the
     * <code>Get<code>/<code>Post</code> elements themselves.
     *
     * @param element
     * @return created <code>DCPType</code>
     * @throws XMLParsingException
     * @see org.deegree.ogcwebservices.getcapabilities.OGCStandardCapabilities
     */
    protected DCPType getDCP( Element element )
                            throws XMLParsingException {

        DCPType dcpType = null;
        try {
            Element elem = (Element) XMLTools.getRequiredNode( element, "ows:HTTP", nsContext );
            List<Node> nl = XMLTools.getNodes( elem, "ows:Get", nsContext );

            URL[] get = new URL[nl.size()];
            for ( int i = 0; i < get.length; i++ ) {
                String s = XMLTools.getNodeAsString( nl.get( i ), "./@xlink:href", nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( nl.get( i ), "./ows:OnlineResource/@xlink:href", nsContext );
                }
                get[i] = new URL( s );
            }
            nl = XMLTools.getNodes( elem, "ows:Post", nsContext );

            URL[] post = new URL[nl.size()];
            for ( int i = 0; i < post.length; i++ ) {
                String s = XMLTools.getNodeAsString( nl.get( i ), "./@xlink:href", nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( nl.get( i ), "./ows:OnlineResource/@xlink:href", nsContext );
                }
                post[i] = new URL( s );
            }
            Protocol protocol = new HTTP( get, post );
            dcpType = new DCPType( protocol );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "Couldn't parse DCPType onlineresource URL about: "
                                           + StringTools.stackTraceToString( e ) );
        }

        return dcpType;
    }

    /**
     * Creates an array of <code>DCPType</code> objects from the passed element list.
     * <p>
     * NOTE: Currently the <code>OnlineResources</code> included in the <code>DCPType</code> are
     * just stored as simple <code>URLs</code> (not as <code>OnLineResource</code> instances)!
     *
     * @param el
     * @return array of <code>DCPType</code>
     * @throws XMLParsingException
     */
    protected DCPType[] getDCPs( List<Element> el )
                            throws XMLParsingException {

        DCPType[] dcpTypes = new DCPType[el.size()];
        for ( int i = 0; i < dcpTypes.length; i++ ) {
            dcpTypes[i] = getDCP( el.get( i ) );
        }

        return dcpTypes;
    }

    /**
     * Creates a class representation of an <code>ows:Operation</code>- element.
     *
     * @param name
     * @param isMandatory
     * @param operations
     * @return operation
     * @throws XMLParsingException
     */
    protected Operation getOperation( String name, boolean isMandatory, Map operations )
                            throws XMLParsingException {

        Operation operation = null;
        Element operationElement = (Element) operations.get( name );
        if ( operationElement == null ) {
            if ( isMandatory ) {
                throw new XMLParsingException( "Mandatory operation '" + name
                                               + "' not defined in 'OperationsMetadata'-section." );
            }
        } else {
            // "ows:Parameter"-elements
            ElementList parameterElements = XMLTools.getChildElements( "Parameter", OWSNS, operationElement );
            OWSDomainType[] parameters = new OWSDomainType[parameterElements.getLength()];
            for ( int i = 0; i < parameters.length; i++ ) {
                parameters[i] = getOWSDomainType( name, parameterElements.item( i ) );
            }
            DCPType[] dcps = getDCPs( XMLTools.getRequiredElements( operationElement, "ows:DCP", nsContext ) );
            operation = new Operation( name, dcps, parameters );

        }

        return operation;
    }

    /**
     *
     * @param root
     * @return constraints as array of OWSDomainType
     * @throws XMLParsingException
     */
    protected OWSDomainType[] getContraints( Element root )
                            throws XMLParsingException {

        OWSDomainType[] contraints = null;
        // "ows:Contraint"-elements
        ElementList contraintElements = XMLTools.getChildElements( "Constraint", OWSNS, root );
        contraints = new OWSDomainType[contraintElements.getLength()];
        for ( int i = 0; i < contraints.length; i++ ) {
            contraints[i] = getOWSDomainType( null, contraintElements.item( i ) );
        }

        return contraints;
    }

    /**
     * Creates a class representation of an element of type <code>ows:DomainType</code>.
     *
     * @param element
     * @return domainType
     * @throws XMLParsingException
     */
    protected OWSDomainType getOWSDomainType( String opname, Element element )
                            throws XMLParsingException {

        // "name"-attribute
        String name = XMLTools.getRequiredNodeAsString( element, "@name", nsContext );

        // "ows:Value"-elements
        String[] values = XMLTools.getNodesAsStrings( element, "ows:Value/text()", nsContext );
        if ( values.length < 1 ) {
            throw new XMLParsingException( "At least one 'ows:Value'-element must be defined in each "
                                           + "element of type 'ows:DomainType'." );
        }

        // TODO: "ows:Metadata"-elements
        OWSDomainType domainType = new OWSDomainType( name, values, null );

        return domainType;
    }

    /**
     * Creates a class representation of an element of type <code>ows:CodeType</code>.
     *
     * @param element
     *            an ows:CodeType element
     * @return the TypeCode (which is defined as something like a dictionary, thesaurus etc.)
     * @throws XMLParsingException
     */
    protected TypeCode getCodeType( Element element )
                            throws XMLParsingException {

        String code = XMLTools.getRequiredNodeAsString( element, "text()", nsContext );

        URI codeSpace = null;
        String codeSpaceString = XMLTools.getNodeAsString( element, "@codeSpace", nsContext, null );
        if ( codeSpaceString != null ) {
            try {
                codeSpace = new URI( codeSpaceString );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( "'" + codeSpaceString + "' does not denote a valid URI in: "
                                               + e.getMessage() );
            }
        }
        return new TypeCode( code, codeSpace );
    }

    /**
     * Creates a <code>ContactInfo</code> object from the given element of type
     * <code>ows:ContactInfoType</code>.
     *
     * @param element
     * @return ContactInfo
     * @throws XMLParsingException
     */
    private ContactInfo getContactInfo( Element element )
                            throws XMLParsingException {

        // 'Phone' element (optional)
        Phone phone = null;
        Element phoneElement = XMLTools.getChildElement( "Phone", OWSNS, element );
        if ( phoneElement != null ) {
            phone = parsePhone( phoneElement, OWSNS );
        }

        // 'Address' element (optional)
        Address address = null;
        Element addressElement = XMLTools.getChildElement( "Address", OWSNS, element );
        if ( addressElement != null ) {
            address = parseAddress( addressElement, OWSNS );
        }

        // 'OnlineResource' element (optional)
        OnlineResource onlineResource = null;
        Element onlineResourceElement = XMLTools.getChildElement( "OnlineResource", OWSNS, element );
        if ( onlineResourceElement != null ) {
            try {
                onlineResource = parseOnLineResource( onlineResourceElement );
            } catch ( Exception e ) {
                // exception will be logged by parseOnLineResource 
            }
        }

        String hoursOfService = XMLTools.getNodeAsString( element, "ows:HoursOfService/text()", nsContext, null );
        String contactInstructions = XMLTools.getNodeAsString( element, "ows:ContactInstructions/text()", nsContext,
                                                               null );

        return new ContactInfo( address, contactInstructions, hoursOfService, onlineResource, phone );
    }

    /**
     * Creates an <code>Envelope</code> object from the given element of type
     * <code>ows:WGS84BoundingBoxType</code>.
     *
     * @param element
     * @return an <code>Envelope</code> object
     * @throws XMLParsingException
     */
    protected Envelope getWGS84BoundingBoxType( Element element )
                            throws XMLParsingException {
        double[] lowerCorner = XMLTools.getRequiredNodeAsDoubles( element, "ows:LowerCorner/text()", nsContext, " " );
        if ( lowerCorner.length != 2 ) {
            throw new XMLParsingException( "Element 'ows:LowerCorner' must contain exactly two double values." );
        }
        double[] upperCorner = XMLTools.getRequiredNodeAsDoubles( element, "ows:UpperCorner/text()", nsContext, " " );
        if ( upperCorner.length != 2 ) {
            throw new XMLParsingException( "Element 'ows:UpperCorner' must contain exactly two double values." );
        }
        return GeometryFactory.createEnvelope( lowerCorner[0], lowerCorner[1], upperCorner[0], upperCorner[1], null );
    }
}
