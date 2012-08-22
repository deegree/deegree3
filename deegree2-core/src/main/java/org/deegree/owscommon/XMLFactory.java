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

import static java.net.URLEncoder.encode;
import static org.deegree.framework.util.CharsetUtils.getSystemCharset;
import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.ogcbase.ExceptionCode.NOAPPLICABLECODE;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.metadata.iso19115.TypeCode;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.ExceptionDocument;
import org.deegree.ogcwebservices.ExceptionReport;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.w3c.dom.Element;

/**
 * Factory to create XML representations of components that are defined in the
 * <code>OWS Common Implementation Capabilities Specification 0.3</code>.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class XMLFactory extends org.deegree.ogcbase.XMLFactory {

    protected static final URI OWSNS = CommonNamespaces.OWSNS;

    private static final String POGC = CommonNamespaces.OGC_PREFIX + ':';

    protected static final URI DEEGREECSWNS = CommonNamespaces.DEEGREECSW;

    /**
     * Exports an <tt>ExceptionReport</tt> to an XML Document as defined in the
     * <code>OGC common implementation specification 0.2.0</code>.
     *
     * @param exr
     * @return a new ServiceException document
     */
    public static XMLFragment export( ExceptionReport exr ) {

        ExceptionDocument eDoc = new ExceptionDocument();
        eDoc.createEmptyDocument();
        Element node = eDoc.getRootElement();

        for ( int i = 0; i < exr.getExceptions().length; i++ ) {
            appendException( node, exr.getExceptions()[i], false );
        }

        return eDoc;
    }

    /**
     * @param exr
     * @return a new ServiceException document
     */
    public static XMLFragment exportNS( ExceptionReport exr ) {

        ExceptionDocument eDoc = new ExceptionDocument();
        eDoc.createEmptyDocumentNS();
        Element node = eDoc.getRootElement();

        for ( int i = 0; i < exr.getExceptions().length; i++ ) {
            appendException( node, exr.getExceptions()[i], true );
        }

        return eDoc;

    }

    /**
     * @param exr
     * @return a new ExceptionReport document according to OWS 1.0.0
     */
    public static XMLFragment exportExceptionReport( ExceptionReport exr ) {
        XMLFragment doc = new XMLFragment( new QualifiedName( "ows", "ExceptionReport", OWSNS ) );
        Element root = doc.getRootElement();

        for ( int i = 0; i < exr.getExceptions().length; i++ ) {
            OGCWebServiceException exc = exr.getExceptions()[i];
            Element e = XMLTools.appendElement( root, OWSNS, "ows:Exception" );
            XMLTools.appendElement( e, OWSNS, "ows:ExceptionText", exc.getMessage() );
            
            if ( exc.getCode() != null ) {
                e.setAttribute( "exceptionCode", exc.getCode().value );
            }

            String locator = exc.getLocator();
            try {
                if ( locator != null ) {
                    locator = URLEncoder.encode( locator, CharsetUtils.getSystemCharset() );
                } else {
                    locator = "unknown";
                }
            } catch ( UnsupportedEncodingException _ ) {
                // if catched why not do something -> setting locator to "unknown"
                locator = "unknown";
            }
            e.setAttribute( "locator", locator );
        }

        return doc;

    }

    /**
     * appends a xml representation of an <tt>OGCWebServiceException</tt> to the passed <tt>Element</tt>
     *
     * @param node
     * @param ex
     * @param namespace
     *            if true, the ogc prefix (bound to the ogc namespace) will be appended
     */
    protected static void appendException( Element node, OGCWebServiceException ex, boolean namespace ) {

        if ( namespace ) {
            node = XMLTools.appendElement( node, OGCNS, POGC + "ServiceException", ex.getMessage() );
        } else {
            node = XMLTools.appendElement( node, null, "ServiceException", ex.getMessage() );
        }

        if ( ex.getCode() != null ) {
            node.setAttribute( "code", ex.getCode().value );
        }
        String locator = ex.getLocator();
        try {
            if ( locator != null ) {
                locator = URLEncoder.encode( locator, CharsetUtils.getSystemCharset() );
            } else {
                locator = "unknown";
            }
        } catch ( UnsupportedEncodingException e ) {
            // if catched why not do something -> setting locator to "unknown"
            locator = "unknown";
        }
        node.setAttribute( "locator", locator );
    }

    /**
     * Appends the DOM representation of the <code>ServiceIdentification</code>- section to the passed
     * <code>Element</code>.
     *
     * @param root
     * @param serviceIdentification
     */
    protected static void appendServiceIdentification( Element root, ServiceIdentification serviceIdentification ) {

        // 'ServiceIdentification'-element
        Element serviceIdentificationNode = XMLTools.appendElement( root, OWSNS, "ows:ServiceIdentification" );

        // 'ServiceType'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:ServiceType",
                                serviceIdentification.getServiceType().getCode() );

        // 'ServiceTypeVersion'-elements
        String[] versions = serviceIdentification.getServiceTypeVersions();
        for ( int i = 0; i < versions.length; i++ ) {
            XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:ServiceTypeVersion", versions[i] );
        }

        // 'Title'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Title", serviceIdentification.getTitle() );

        // 'Abstract'-element
        if ( serviceIdentification.getAbstract() != null ) {
            XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Abstract",
                                    serviceIdentification.getAbstract() );
        }

        // 'Keywords'-element
        appendOWSKeywords( serviceIdentificationNode, serviceIdentification.getKeywords() );

        // 'Fees'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Fees", serviceIdentification.getFees() );

        // 'AccessConstraints'-element
        String[] constraints = serviceIdentification.getAccessConstraints();
        if ( constraints != null ) {
            for ( int i = 0; i < constraints.length; i++ ) {
                XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:AccessConstraints", constraints[i] );
            }
        }
    }

    /**
     * Appends a <code>ows:Keywords</code> -element for each <code>Keywords</code> object of the passed array to the
     * passed <code>Element</code>.
     *
     * @param xmlNode
     * @param keywords
     */
    protected static void appendOWSKeywords( Element xmlNode, Keywords[] keywords ) {
        if ( keywords != null ) {
            for ( int i = 0; i < keywords.length; i++ ) {
                Element node = XMLTools.appendElement( xmlNode, OWSNS, "ows:Keywords" );
                appendOWSKeywords( node, keywords[i] );
            }
        }
    }

    /**
     * Appends a <code>ows:Keywords</code> -element to the passed <code>Element</code> and fills it with the available
     * keywords.
     *
     * @param xmlNode
     * @param keywords
     */
    protected static void appendOWSKeywords( Element xmlNode, Keywords keywords ) {
        if ( keywords != null ) {
            String[] kw = keywords.getKeywords();
            for ( int i = 0; i < kw.length; i++ ) {
                XMLTools.appendElement( xmlNode, OWSNS, "ows:Keyword", kw[i] );
            }
            TypeCode typeCode = keywords.getTypeCode();
            if ( typeCode != null ) {
                Element node = XMLTools.appendElement( xmlNode, OWSNS, "ows:Type", typeCode.getCode() );
                if ( typeCode.getCodeSpace() != null ) {
                    node.setAttribute( "codeSpace", typeCode.getCodeSpace().toString() );
                }
            }
        }
    }

    /**
     * Appends the DOM representation of the <code>ServiceProvider</code>- section to the passed <code>Element</code>.
     *
     * @param root
     * @param serviceProvider
     */
    protected static void appendServiceProvider( Element root, ServiceProvider serviceProvider ) {

        // 'ServiceProvider'-element
        Element serviceProviderNode = XMLTools.appendElement( root, OWSNS, "ows:ServiceProvider" );

        // 'ProviderName'-element
        XMLTools.appendElement( serviceProviderNode, OWSNS, "ows:ProviderName", serviceProvider.getProviderName() );

        // 'ProviderSite'-element
        if ( serviceProvider.getProviderSite() != null ) {
            Element providerSiteNode = XMLTools.appendElement( serviceProviderNode, OWSNS, "ows:ProviderSite" );
            appendSimpleLinkAttributes( providerSiteNode, serviceProvider.getProviderSite() );
        }

        // 'ServiceContact'-element
        Element serviceContactNode = XMLTools.appendElement( serviceProviderNode, OWSNS, "ows:ServiceContact" );

        // 'IndividualName'-element
        XMLTools.appendElement( serviceContactNode, OWSNS, "ows:IndividualName", serviceProvider.getIndividualName() );

        // 'PositionName'-element
        if ( serviceProvider.getPositionName() != null ) {
            XMLTools.appendElement( serviceContactNode, OWSNS, "ows:PositionName", serviceProvider.getPositionName() );
        }

        // 'ContactInfo'-element
        ContactInfo contactInfo = serviceProvider.getContactInfo();
        if ( contactInfo != null ) {
            Element contactInfoNode = XMLTools.appendElement( serviceContactNode, OWSNS, "ows:ContactInfo" );
            Phone phone = contactInfo.getPhone();
            if ( phone != null ) {
                appendPhone( contactInfoNode, phone );
            }
            Address address = contactInfo.getAddress();
            if ( address != null ) {
                appendAddress( contactInfoNode, address );
            }
            OnlineResource onlineResource = contactInfo.getOnLineResource();
            if ( onlineResource != null ) {
                appendOnlineResource( contactInfoNode, "ows:OnlineResource", onlineResource, OWSNS );
            }
            String hoursOfService = contactInfo.getHoursOfService();
            if ( hoursOfService != null ) {
                XMLTools.appendElement( contactInfoNode, OWSNS, "ows:HoursOfService", hoursOfService );
            }
            String contactInstructions = contactInfo.getContactInstructions();
            if ( contactInstructions != null ) {
                XMLTools.appendElement( contactInfoNode, OWSNS, "ows:ContactInstructions", contactInstructions );
            }
        }
        TypeCode role = serviceProvider.getRole();
        if ( role != null ) {
            Element roleElement = XMLTools.appendElement( serviceContactNode, OWSNS, "ows:Role", role.getCode() );
            if ( role.getCodeSpace() != null ) {
                roleElement.setAttribute( "codeSpace", role.getCodeSpace().toString() );
            }
        }
    }

    /**
     * Appends the DOM representation of the <code>Phone</code> -section to the passed <code>Element</code>.
     *
     * @param root
     * @param phone
     */
    protected static void appendPhone( Element root, Phone phone ) {

        // 'Phone'-element
        Element phoneNode = XMLTools.appendElement( root, OWSNS, "ows:Phone" );

        // 'Voice'-elements
        String[] voiceNumbers = phone.getVoice();
        for ( int i = 0; i < voiceNumbers.length; i++ ) {
            XMLTools.appendElement( phoneNode, OWSNS, "ows:Voice", voiceNumbers[i] );
        }

        // 'Facsimile'-elements
        String[] facsimileNumbers = phone.getFacsimile();
        for ( int i = 0; i < facsimileNumbers.length; i++ ) {
            XMLTools.appendElement( phoneNode, OWSNS, "ows:Facsimile", facsimileNumbers[i] );
        }
    }

    /**
     * Appends the DOM representation of the <code>Address</code> -section to the passed <code>Element</code>.
     *
     * @param root
     * @param address
     */
    protected static void appendAddress( Element root, Address address ) {

        // 'Address'-element
        Element addressNode = XMLTools.appendElement( root, OWSNS, "ows:Address" );

        // 'DeliveryPoint'-elements
        String[] deliveryPoints = address.getDeliveryPoint();
        for ( int i = 0; i < deliveryPoints.length; i++ ) {
            XMLTools.appendElement( addressNode, OWSNS, "ows:DeliveryPoint", deliveryPoints[i] );
        }

        // 'City'-element
        if ( address.getCity() != null ) {
            XMLTools.appendElement( addressNode, OWSNS, "ows:City", address.getCity() );
        }

        // 'AdministrativeArea'-element
        if ( address.getAdministrativeArea() != null ) {
            XMLTools.appendElement( addressNode, OWSNS, "ows:AdministrativeArea", address.getAdministrativeArea() );
        }

        // 'PostalCode'-element
        if ( address.getPostalCode() != null ) {
            XMLTools.appendElement( addressNode, OWSNS, "ows:PostalCode", address.getPostalCode() );
        }

        // 'Country'-element
        if ( address.getCountry() != null ) {
            XMLTools.appendElement( addressNode, OWSNS, "ows:Country", address.getCountry() );
        }

        // 'ElectronicMailAddress'-elements
        String[] electronicMailAddresses = address.getElectronicMailAddress();
        if ( address.getElectronicMailAddress() != null ) {
            for ( int i = 0; i < electronicMailAddresses.length; i++ ) {
                XMLTools.appendElement( addressNode, OWSNS, "ows:ElectronicMailAddress", electronicMailAddresses[i] );
            }
        }
    }

    /**
     * Appends the DOM representation of the <code>OperationsMetadata</code>- section to the passed <code>Element</code>
     * .
     *
     * @param root
     */
    protected static void appendOperationsMetadata( Element root, OperationsMetadata operationsMetadata ) {

        // 'ows:OperationsMetadata'-element
        Element operationsMetadataNode = XMLTools.appendElement( root, OWSNS, "ows:OperationsMetadata" );

        // append all Operations
        Operation[] operations = operationsMetadata.getOperations();
        for ( int i = 0; i < operations.length; i++ ) {
            Operation operation = operations[i];

            // 'ows:Operation'-element
            Element operationElement = XMLTools.appendElement( operationsMetadataNode, OWSNS, "ows:Operation" );
            operationElement.setAttribute( "name", operation.getName() );

            // 'ows:DCP'-elements
            DCPType[] dcps = operation.getDCPs();
            for ( int j = 0; j < dcps.length; j++ ) {
                appendDCP( operationElement, dcps[j] );
            }

            // 'ows:Parameter'-elements
            OWSDomainType[] parameters = operation.getParameters();
            for ( int j = 0; j < parameters.length; j++ ) {
                appendParameter( operationElement, parameters[j], "ows:Parameter" );
            }

            // 'ows:Metadata'-elements
            Object[] metadata = operation.getMetadata();
            if ( metadata != null ) {
                for ( int j = 0; j < metadata.length; j++ ) {
                    appendMetadata( operationElement, metadata[j] );
                }
            }
        }

        // append general parameters
        OWSDomainType[] parameters = operationsMetadata.getParameter();
        for ( int i = 0; i < parameters.length; i++ ) {
            appendParameter( operationsMetadataNode, parameters[i], "ows:Parameter" );
        }

        // append constraints
        OWSDomainType[] constraints = operationsMetadata.getConstraints();
        for ( int i = 0; i < constraints.length; i++ ) {
            appendParameter( operationsMetadataNode, constraints[i], "ows:Constraint" );
        }
    }

    /**
     * Appends the DOM representation of a <code>DCPType</code> instance to the passed <code>Element</code>.
     *
     * @param root
     * @param dcp
     */
    protected static void appendDCP( Element root, DCPType dcp ) {

        // 'ows:DCP'-element
        Element dcpNode = XMLTools.appendElement( root, OWSNS, "ows:DCP" );

        // currently, the only supported DCP is HTTP!
        if ( dcp.getProtocol() instanceof HTTP ) {
            HTTP http = (HTTP) dcp.getProtocol();

            // 'ows:HTTP'-element
            Element httpNode = XMLTools.appendElement( dcpNode, OWSNS, "ows:HTTP" );

            // 'ows:Get'-elements
            URL[] getURLs = http.getGetOnlineResources();
            for ( int i = 0; i < getURLs.length; i++ ) {
                appendOnlineResource( httpNode, "ows:Get", new OnlineResource( new Linkage( getURLs[i] ) ), OWSNS );
            }

            // 'ows:Post'-elements
            URL[] postURLs = http.getPostOnlineResources();
            for ( int i = 0; i < postURLs.length; i++ ) {
                appendOnlineResource( httpNode, "ows:Post", new OnlineResource( new Linkage( postURLs[i] ) ), OWSNS );
            }
        }
    }

    /**
     * Appends the DOM representation of a <code>OWSDomainType</code> instance to the passed <code>Element</code>.
     *
     * @param root
     * @param parameter
     */
    protected static void appendParameter( Element root, OWSDomainType parameter, String elementName ) {

        // 'ows:Parameter'-element
        Element parameterNode = XMLTools.appendElement( root, OWSNS, elementName );
        parameterNode.setAttribute( "name", parameter.getName() );

        // 'ows:Value'-elements
        String[] values = parameter.getValues();
        for ( int i = 0; i < values.length; i++ ) {
            XMLTools.appendElement( parameterNode, OWSNS, "ows:Value", values[i] );
        }
    }

    /**
     * Appends the DOM representation of a <code>Metadata</code> instance to the passed <code>Element</code>.
     *
     * @param root
     * @param metadata
     */
    protected static void appendMetadata( Element root, Object metadata ) {

        // TODO

    }

    /**
     * @param exc
     * @return the exported new document
     */
    public static XMLFragment exportExceptionReportWFS( OGCWebServiceException exc ) {
        XMLFragment doc = new XMLFragment( new QualifiedName( "ows", "ExceptionReport", OWSNS ) );
        Element root = doc.getRootElement();
        root.setAttribute( "version", "1.1.0" );

        Element e = appendElement( root, OWSNS, "ows:Exception" );
        if ( exc.getCode() != null ) {
            e.setAttribute( "exceptionCode", exc.getCode().value );
        } else {
            e.setAttribute( "exceptionCode", NOAPPLICABLECODE.value );
        }

        String locator = exc.getLocator();
        try {
            if ( locator != null ) {
                locator = encode( locator, getSystemCharset() );
            } else {
                locator = "unknown";
            }
        } catch ( UnsupportedEncodingException _ ) {
            // if caught why not do something -> setting locator to "unknown"
            locator = "unknown";
        }
        e.setAttribute( "locator", locator );

        appendElement( e, OWSNS, "ows:ExceptionText", exc.getMessage() );

        return doc;
    }

    /**
     * @param exc
     * @return the exported exception
     */
    public static XMLFragment exportExceptionReportWFS100( OGCWebServiceException exc ) {
        XMLFragment doc = new XMLFragment( new QualifiedName( "ogc", "ServiceExceptionReport", OGCNS ) );
        Element root = doc.getRootElement();
        root.setAttribute( "version", "1.2.0" );

        Element e = appendElement( root, OGCNS, "ogc:ServiceException", exc.getMessage() );
        e.setAttribute( "code", exc.getCode() == null ? NOAPPLICABLECODE.value : exc.getCode().value );

        String locator = exc.getLocator();
        try {
            if ( locator != null ) {
                locator = encode( locator, getSystemCharset() );
            } else {
                locator = "unknown";
            }
        } catch ( UnsupportedEncodingException _ ) {
            // if caught why not do something -> setting locator to "unknown"
            locator = "unknown";
        }
        e.setAttribute( "locator", locator );

        return doc;
    }

    /**
     * @param elem
     * @param name
     * @param value
     */
    public static void maybeSetAttribute( Element elem, String name, String value ) {
        if ( value != null ) {
            elem.setAttribute( name, value );
        }
    }

}
