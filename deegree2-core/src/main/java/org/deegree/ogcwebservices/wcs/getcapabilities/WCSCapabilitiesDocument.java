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
package org.deegree.ogcwebservices.wcs.getcapabilities;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.deegree.datatypes.CodeList;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.CitedResponsibleParty;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.OGCException;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.MetadataLink;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.Capability;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OGCStandardCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Service;
import org.deegree.ogcwebservices.wcs.CoverageOfferingBrief;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class WCSCapabilitiesDocument extends OGCStandardCapabilitiesDocument {

    /**
     *
     */
    private static final long serialVersionUID = -8934399061168275172L;

    /**
     * the WCSCapabilities xml template filename
     */
    public static final String XML_TEMPLATE = "WCSCapabilitiesTemplate.xml";

    /**
     * WCS namespace
     */
    protected static URI WCSNS = CommonNamespaces.WCSNS;

    /**
     * deegree WCS namespace
     */
    protected static URI DGRNS = CommonNamespaces.DEEGREEWCS;

    /**
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = WCSCapabilitiesDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /* (non-Javadoc)
     * @see org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument#parseCapabilities()
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {
        String version = parseVersion();
        String updateSeq = parseUpdateSequence();
        Service service = parseServiceSection();
        Capability capabilitiy = getCapabilitySection( WCSNS );
        ContentMetadata contentMetadata = parseContentMetadataSection();
        return new WCSCapabilities( version, updateSeq, service, capabilitiy, contentMetadata );
    }

    /**
     * returns the service section of the WCS configuration/capabilities
     *
     * @return created <tt>CapabilitiesService</tt>
     * @throws InvalidCapabilitiesException
     */
    public Service parseServiceSection()
                            throws InvalidCapabilitiesException {
        try {
            Element element = XMLTools.getRequiredChildElement( "Service", WCSNS, getRootElement() );
            Element elem = XMLTools.getChildElement( "metadataLink", WCSNS, element );
            MetadataLink mLink = parseMetadataLink( elem );
            String desc = XMLTools.getStringValue( "description", WCSNS, element, null );
            String name = XMLTools.getRequiredStringValue( "name", WCSNS, element );
            String label = XMLTools.getRequiredStringValue( "label", WCSNS, element );
            ElementList el = XMLTools.getChildElements( "keywords", WCSNS, element );
            Keywords[] keywords = parseKeywords( el, WCSNS );
            elem = XMLTools.getChildElement( "responsibleParty", WCSNS, element );
            CitedResponsibleParty crp = parseResponsibleParty( elem );
            elem = XMLTools.getChildElement( "fees", WCSNS, element );
            CodeList fees = parseCodeList( elem );
            el = XMLTools.getChildElements( "accessConstraints", WCSNS, element );
            CodeList[] accessConstraints = parseCodeListArray( el );

            String version = element.getAttribute( "version" );
            if ( version == null || version.equals( "" ) ) {
                version = this.parseVersion();
            }
            String updateSequence = element.getAttribute( "updateSequence" );
            if ( updateSequence == null || updateSequence.equals( "" ) ) {
                updateSequence = this.getRootElement().getAttribute( "updateSequence" );
            }

            Service service = new Service( desc, name, mLink, label, keywords, crp, fees, accessConstraints, version,
                                           updateSequence );
            return service;
        } catch ( XMLParsingException e ) {
            String s = e.getMessage();
            throw new InvalidCapabilitiesException( "Error while parsing the Service Section "
                                                    + "of the WCS capabilities\n" + s
                                                    + StringTools.stackTraceToString( e ) );
        } catch ( DOMException e ) {
            String s = e.getMessage();
            throw new InvalidCapabilitiesException( "Error handling the DOM object of the "
                                                    + "Service Section of the WCS capabilities\n" + s
                                                    + StringTools.stackTraceToString( e ) );
        } catch ( OGCException e ) {
            String s = e.getMessage();
            throw new InvalidCapabilitiesException( "Error initializing the Service object from "
                                                    + "the Service Section of the WCS capabilities\n" + s
                                                    + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * returns the contentMetadata section of the WCS configuration/capabilities
     *
     * @return the content metadata
     * @throws InvalidCapabilitiesException
     */
    public ContentMetadata parseContentMetadataSection()
                            throws InvalidCapabilitiesException {
        try {
            Element element = XMLTools.getRequiredChildElement( "ContentMetadata", WCSNS, getRootElement() );
            ElementList el = XMLTools.getChildElements( "CoverageOfferingBrief", WCSNS, element );
            CoverageOfferingBrief[] cob = parseCoverageOfferingBrief( el );

            String version = element.getAttribute( "version" );
            if ( version == null || version.equals( "" ) ) {
                version = this.parseVersion();
            }
            String updateSequence = element.getAttribute( "updateSequence" );
            if ( updateSequence == null || updateSequence.equals( "" ) ) {
                updateSequence = this.getRootElement().getAttribute( "updateSequence" );
            }

            return new ContentMetadata( version, updateSequence, cob );
        } catch ( XMLParsingException e ) {
            String s = e.getMessage();
            throw new InvalidCapabilitiesException( "Error while parsing the ContentMetadata "
                                                    + "Section of the WCS capabilities\n" + s
                                                    + StringTools.stackTraceToString( e ) );
        } catch ( OGCException e ) {
            String s = e.getMessage();
            throw new InvalidCapabilitiesException( "Error while parsing the ContentMetadata "
                                                    + "Section of the WCS capabilities\n" + s
                                                    + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * creates a <tt>CitedResponsibleParty</tt> object from the passed element
     *
     * @param element
     * @return the cited responsible party
     * @throws XMLParsingException
     */
    private CitedResponsibleParty parseResponsibleParty( Element element )
                            throws XMLParsingException {
        if ( element == null )
            return null;
        String indName = XMLTools.getStringValue( "individualName", WCSNS, element, null );
        String orgName = XMLTools.getStringValue( "organisationName", WCSNS, element, null );
        String posName = XMLTools.getStringValue( "positionName", WCSNS, element, null );
        Element elem = XMLTools.getChildElement( "contactInfo", WCSNS, element );
        ContactInfo contactInfo = parseContactInfo( elem );
        return new CitedResponsibleParty( new ContactInfo[] { contactInfo }, new String[] { indName },
                                          new String[] { orgName }, new String[] { posName }, null );
    }

    /**
     * creates a <tt>ContactInfo</tt> object from the passed element
     *
     * @param element
     * @return the contact information
     * @throws XMLParsingException
     */
    private ContactInfo parseContactInfo( Element element )
                            throws XMLParsingException {
        if ( element == null )
            return null;
        Element elem = XMLTools.getChildElement( "phone", WCSNS, element );
        Phone phone = parsePhone( elem );
        elem = XMLTools.getChildElement( "address", WCSNS, element );
        Address addr = parseAddress( elem, WCSNS );
        elem = XMLTools.getChildElement( "onlineResource", WCSNS, element );
        OnlineResource olr = parseOnLineResource( elem );
        return new ContactInfo( addr, null, null, olr, phone );
    }

    /**
     * Creates an <code>Address</code> instance from the passed element.
     *
     * @param element
     *            Address-element
     * @param namespaceURI
     *            namespace-prefix of all elements
     * @return the parsed Address
     */
    @Override
    protected Address parseAddress( Element element, URI namespaceURI ) {
        ElementList el = XMLTools.getChildElements( "deliveryPoint", namespaceURI, element );
        String[] deliveryPoint = new String[el.getLength()];
        for ( int i = 0; i < deliveryPoint.length; i++ ) {
            deliveryPoint[i] = XMLTools.getStringValue( el.item( i ) );
        }
        String city = XMLTools.getStringValue( "city", namespaceURI, element, null );
        String adminArea = XMLTools.getStringValue( "administrativeArea", namespaceURI, element, null );
        String postalCode = XMLTools.getStringValue( "postalCode", namespaceURI, element, null );
        String country = XMLTools.getStringValue( "country", namespaceURI, element, null );
        el = XMLTools.getChildElements( "electronicMailAddress", namespaceURI, element );
        String[] eMailAddresses = new String[el.getLength()];
        for ( int i = 0; i < eMailAddresses.length; i++ ) {
            eMailAddresses[i] = XMLTools.getStringValue( el.item( i ) );
        }
        return new Address( adminArea, city, country, deliveryPoint, eMailAddresses, postalCode );
    }

    /**
     * creates a <tt>Phone</tt> object from the passed element
     *
     * @param element
     * @return the phone
     */
    private Phone parsePhone( Element element ) {
        if ( element == null )
            return null;
        ElementList el = XMLTools.getChildElements( "voice", WCSNS, element );
        String[] voice = new String[el.getLength()];
        for ( int i = 0; i < voice.length; i++ ) {
            voice[i] = XMLTools.getStringValue( el.item( i ) );
        }
        el = XMLTools.getChildElements( "facsimile", WCSNS, element );
        String[] facsimile = new String[el.getLength()];
        for ( int i = 0; i < facsimile.length; i++ ) {
            facsimile[i] = XMLTools.getStringValue( el.item( i ) );
        }
        return new Phone( facsimile, null, null, voice );
    }

    /**
     * creates a <tt>Request</tt> object (instance of WCSCapabilityRequest) from the passed
     * element encapsulating the Request part of the WCS Capabiliy section
     *
     * @param element
     * @return created <tt>Request</tt>
     * @throws XMLParsingException
     */
    @Override
    protected OperationsMetadata parseOperations( Element element, URI namespaceURI )
                            throws XMLParsingException {

        Element gCapa = XMLTools.getRequiredChildElement( "GetCapabilities", WCSNS, element );
        ElementList el = XMLTools.getChildElements( "DCPType", WCSNS, gCapa );
        DCPType[] dcp = getDCPTypes( el, WCSNS );
        Operation getCapaOperation = new Operation( "GetCapabilities", dcp );

        Element dCover = XMLTools.getRequiredChildElement( "DescribeCoverage", WCSNS, element );
        el = XMLTools.getChildElements( "DCPType", WCSNS, dCover );
        dcp = getDCPTypes( el, WCSNS );
        Operation descCoverOperation = new Operation( "DescribeCoverage", dcp );

        Element gCover = XMLTools.getRequiredChildElement( "GetCoverage", WCSNS, element );
        el = XMLTools.getChildElements( "DCPType", WCSNS, gCover );
        dcp = getDCPTypes( el, WCSNS );
        Operation getCoverOperation = new Operation( "GetCoverage", dcp );

        return new WCSCapabilityOperations( getCapaOperation, descCoverOperation, getCoverOperation );

    }

    /**
     * creates an array of <tt>CoverageOfferingBrief</tt> objects from the passed element list
     * encapsulating all CoverageOfferingBrief parts of the WCS ContentMetadata section
     *
     * @param el
     * @return creates array of <tt>CoverageOfferingBrief</tt>
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws OGCException
     */
    private CoverageOfferingBrief[] parseCoverageOfferingBrief( ElementList el )
                            throws XMLParsingException, OGCWebServiceException, OGCException {
        if ( el == null )
            return null;
        CoverageOfferingBrief[] cob = new CoverageOfferingBrief[el.getLength()];
        for ( int i = 0; i < cob.length; i++ ) {
            cob[i] = parseCoverageOfferingBrief( el.item( i ) );
        }
        return cob;
    }

    /**
     * creates a <tt>CoverageOfferingBrief</tt> object from the passed element encapsulating one
     * CoverageOfferingBrief part of the WCS ContentMetadata section
     *
     * @param element
     * @return created <tt>CoverageOfferingBrief</tt>
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws OGCException
     */
    protected CoverageOfferingBrief parseCoverageOfferingBrief( Element element )
                            throws XMLParsingException, OGCWebServiceException, OGCException {
        Element elem = XMLTools.getChildElement( "metadataLink", WCSNS, element );
        MetadataLink mLink = parseMetadataLink( elem );
        String desc = XMLTools.getStringValue( "description", WCSNS, element, null );
        String name = XMLTools.getRequiredStringValue( "name", WCSNS, element );
        String label = XMLTools.getRequiredStringValue( "label", WCSNS, element );
        elem = XMLTools.getChildElement( "lonLatEnvelope", WCSNS, element );
        LonLatEnvelope llEnv = parseLonLatEnvelope( elem );
        ElementList el = XMLTools.getChildElements( "keywords", WCSNS, element );
        Keywords[] keywords = parseKeywords( el, WCSNS );

        return new CoverageOfferingBrief( name, label, desc, mLink, llEnv, keywords, null );
    }

}
