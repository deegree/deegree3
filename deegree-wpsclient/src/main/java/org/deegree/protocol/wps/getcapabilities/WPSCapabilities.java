//$HeadURL: https://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

package org.deegree.protocol.wps.getcapabilities;

import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * WPSCapabilities encapsulates information contained within an WPS Capabilities response
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPSCapabilities {

    private static final NamespaceContext NS_CONTEXT;

    private static Logger LOG = LoggerFactory.getLogger( WPSCapabilities.class );

    // TODO OperationsMetadata
    // TODO ServiceProvider / ServiceContact

    private XMLAdapter capabilitiesDoc;

    private String service;

    private String version;

    private String updateSequence;

    private String lang;

    private String schemaLocation;

    /*
     * Add namespaces relevant for the WPSCapabilities, i.e. wps and ows
     */
    static {
        NS_CONTEXT = new NamespaceContext();
        NS_CONTEXT.addNamespace( "wps", WPS_100_NS );
        NS_CONTEXT.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
    }

    /**
     * Public constructor to be initialized with a capabilities document
     * 
     * @param capabilitiesDoc
     *            an WPS capabilities document to be parsed
     */
    public WPSCapabilities( XMLAdapter capabilitiesDoc ) {
        LOG.debug( "WPSCapabilities initialized" );
        this.capabilitiesDoc = capabilitiesDoc;
        OMElement rootElement = capabilitiesDoc.getRootElement();
        this.service = rootElement.getAttributeValue( new QName( "service" ) );
        this.lang = rootElement.getAttributeValue( new QName( "lang" ) );
        this.version = rootElement.getAttributeValue( new QName( "version" ) );
        this.schemaLocation = rootElement.getAttributeValue( new QName( "schemaLocation" ) );
        this.updateSequence = rootElement.getAttributeValue( new QName( "updateSequence" ) );
    }

    /**
     * 
     * gets the Information out of the ServiceIdentification section of the getCapabilitiesResponse and writes the
     * elements in the class ServiceIdentificaiton which is build up according to Subclause 7.4.4 of OGC Web Services
     * Common Specification as stated in the WPS Spec 1.0 for ServiceIdentification
     * 
     * @return serviceIdentification
     */
    public ServiceIdentification getServiceIdentification() {

        ServiceIdentification serviceIdentification = new ServiceIdentification();

        /*
         * Gets the elements via XPath and writes them into the Object serviceIdentification
         */
        OMElement serviceIdentificationOMElement = null;

        serviceIdentificationOMElement = capabilitiesDoc.getElement(
                                                                     capabilitiesDoc.getRootElement(),
                                                                     new XPath( "ows:ServiceIdentification", NS_CONTEXT ) );

        serviceIdentification.setServiceType( capabilitiesDoc.getNodeAsString(
                                                                               serviceIdentificationOMElement,
                                                                               new XPath( "ows:ServiceType", NS_CONTEXT ),
                                                                               null ) );

        serviceIdentification.setServiceTypeVersion( capabilitiesDoc.getNodesAsStrings(
                                                                                        serviceIdentificationOMElement,
                                                                                        new XPath(
                                                                                                   "ows:ServiceTypeVersion",
                                                                                                   NS_CONTEXT ) ) );

        serviceIdentification.setFees( capabilitiesDoc.getNodeAsString( serviceIdentificationOMElement,
                                                                        new XPath( "ows:Fees", NS_CONTEXT ), null ) );

        serviceIdentification.setAccessConstraints( capabilitiesDoc.getNodesAsStrings(
                                                                                       serviceIdentificationOMElement,
                                                                                       new XPath(
                                                                                                  "ows:AccessConstraints",
                                                                                                  NS_CONTEXT ) ) );

        serviceIdentification.setProfile( capabilitiesDoc.getNodesAsStrings( serviceIdentificationOMElement,
                                                                             new XPath( "ows:Profile", NS_CONTEXT ) ) );

        serviceIdentification.setTitle( capabilitiesDoc.getNodesAsStrings( serviceIdentificationOMElement,
                                                                           new XPath( "ows:Title", NS_CONTEXT ) ) );

        serviceIdentification.setAbstraCt( capabilitiesDoc.getNodesAsStrings( serviceIdentificationOMElement,
                                                                              new XPath( "ows:Abstract", NS_CONTEXT ) ) );

        serviceIdentification.setKeywords( capabilitiesDoc.getNodesAsStrings( serviceIdentificationOMElement,
                                                                              new XPath( "ows:Keywords/ows:Keyword",
                                                                                         NS_CONTEXT ) ) );
        return serviceIdentification;

    }

    /**
     * gets the Information out of the ServiceProvider section of the getCapabilitiesResponse and writes the elements in
     * the class ServiceProvider which is build up according to Subclause 7.4.5 of OGC Web Services Common Specification
     * as stated in the WPS Spec 1.0 for ServiceIdentification
     * 
     * @return service Provicer
     */
    public ServiceProvider getServiceProvider() {

        ServiceProvider serviceProvider = new ServiceProvider();

        OMElement serviceProviderOMElement = null;
        serviceProviderOMElement = capabilitiesDoc.getElement( capabilitiesDoc.getRootElement(),
                                                               new XPath( "ows:ServiceProvider", NS_CONTEXT ) );

        OMElement providerNameOMElement = null;
        OMElement providerSiteOMElement = null;
        OMElement serviceContactOMElement = null;

        providerNameOMElement = capabilitiesDoc.getElement( serviceProviderOMElement, new XPath( "ows:ProviderName",
                                                                                                 NS_CONTEXT ) );
        if ( providerNameOMElement != null )
            serviceProvider.setProviderName( providerNameOMElement.getText() );

        providerSiteOMElement = capabilitiesDoc.getElement( serviceProviderOMElement, new XPath( "ows:ProviderSite",
                                                                                                 NS_CONTEXT ) );

        if ( providerSiteOMElement != null ) {
            for ( Iterator iterator = providerSiteOMElement.getAllAttributes(); iterator.hasNext(); ) {
                OMAttribute o = (OMAttribute) iterator.next();
                serviceProvider.setProviderSite( o.getAttributeValue() );
            }
        }

        serviceContactOMElement = capabilitiesDoc.getElement( serviceProviderOMElement,
                                                              new XPath( "ows:ServiceContact", NS_CONTEXT ) );
        if ( serviceContactOMElement != null ) {
            ServiceContact serviceContact = new ServiceContact();

            serviceProvider.setServiceContact( serviceContact );

        }

        return serviceProvider;

    }

    /**
     * 
     * @return operation metadata
     */
    public OperationsMetadata getOperationsMetadata() {

        OperationsMetadata operationsMetadata = new OperationsMetadata();

        // TODO OperationsMetadata needs to be implemented
        return operationsMetadata;

    }

    /**
     * 
     * @param String
     *            declaring the operation, usually GetCapabilities, DescribeProcess or Execute TODO case-sensitiveness
     *            handling of operation parameter needs to be implemented
     * @param get
     *            true means HTTP GET, false means HTTP POST
     * @return String representation of <ows:Get xlink:href=""/> attribute
     */
    public String getOperationURLasString( String operation, boolean get ) {
        String operationURL = null;
        OMElement operationsMetadataOMElement = null;
        StringBuilder sb = new StringBuilder();
        sb.append( "ows:OperationsMetadata/ows:Operation[@name=\"" );
        sb.append( operation );
        if ( get ) {
            sb.append( "\"]/ows:DCP/ows:HTTP/ows:Get" );
            operationsMetadataOMElement = capabilitiesDoc.getElement( capabilitiesDoc.getRootElement(),
                                                                      new XPath( sb.toString(), NS_CONTEXT ) );
        } else {
            sb.append( "\"]/ows:DCP/ows:HTTP/ows:Post" );
            operationsMetadataOMElement = capabilitiesDoc.getElement( capabilitiesDoc.getRootElement(),
                                                                      new XPath( sb.toString(), NS_CONTEXT ) );
        }
        operationURL = operationsMetadataOMElement.getAttribute( new QName( CommonNamespaces.XLNNS, "href" ) ).getAttributeValue();
        LOG.debug( "DescribeProcess reachable via: " + operationURL );
        return operationURL;
    }

    /**
     * gets the Information out of the ProcessOfferings section of the getCapabilitiesResponse and writes the elements
     * in a List of Objects of the class ProcessOfferings which is build up according to Subclause 8.3.3. of OGC Web
     * Processing Service Spec 1.0 for ProcessOfferings
     * 
     * @return List of ProcessingOfferings
     */
    public List<ProcessBrief> getProcessOfferings() {

        LOG.info( "parsing process offerings..." );

        List<ProcessBrief> processOfferingsList = new ArrayList<ProcessBrief>();

        OMElement processOfferingsOMElement = capabilitiesDoc.getElement(
                                                                          capabilitiesDoc.getRootElement(),
                                                                          new XPath( "wps:ProcessOfferings", NS_CONTEXT ) );

        List<OMElement> process = capabilitiesDoc.getElements( processOfferingsOMElement, new XPath( "wps:Process",
                                                                                                     NS_CONTEXT ) );

        for ( Iterator iterator = processOfferingsOMElement.getChildElements(); iterator.hasNext(); ) {

            ProcessBrief processBrief = new ProcessBrief();

            OMElement processOMElement = (OMElement) iterator.next();

            Iterator attributeIterator = processOMElement.getAllAttributes();

            for ( Iterator iterator2 = attributeIterator; iterator2.hasNext(); ) {

                OMAttribute omAttribute = (OMAttribute) iterator2.next();

                if ( omAttribute.getQName().getLocalPart().equalsIgnoreCase( "processVersion" ) )
                    processBrief.setProcessVersion( omAttribute.getAttributeValue() );
            }

            processBrief.setIdentifier( capabilitiesDoc.getNodeAsString( processOMElement, new XPath( "ows:Identifier",
                                                                                                      NS_CONTEXT ),
                                                                         null ) );

            processBrief.setAbstract( capabilitiesDoc.getNodeAsString( processOMElement, new XPath( "ows:Abstract",
                                                                                                    NS_CONTEXT ), null ) );

            processBrief.setMetadata( capabilitiesDoc.getNodesAsStrings( processOMElement, new XPath( "ows:Abstract",
                                                                                                      NS_CONTEXT ) ) );

            processBrief.setProcessVersion( capabilitiesDoc.getNodeAsString( processOMElement,
                                                                             new XPath( "ows:ProcessVersion",
                                                                                        NS_CONTEXT ), null ) );

            processBrief.setProfiles( capabilitiesDoc.getNodesAsStrings( processOMElement,
                                                                         new XPath( "ows:ProcessVersion", NS_CONTEXT ) ) );

            processBrief.setTitle( capabilitiesDoc.getNodeAsString( processOMElement, new XPath( "ows:Title",
                                                                                                 NS_CONTEXT ), null ) );

            processBrief.setWsdl( capabilitiesDoc.getNodeAsString( processOMElement,
                                                                   new XPath( "ows:WSDS", NS_CONTEXT ), null ) );

            processOfferingsList.add( processBrief );

        }

        LOG.info( "parsing process offerings done" );

        return processOfferingsList;
    }

    /**
     * 
     * gets the Information out of the Language section of the getCapabilitiesResponse and writes the elements in the
     * class Languages which is build up according to WPS Spec 1.0 Subclause 8.3.4
     * 
     */
    public Languages getLanguages() {
        Languages languages = new Languages();

        OMElement languagesOMElement = capabilitiesDoc.getElement( capabilitiesDoc.getRootElement(),
                                                                   new XPath( "wps:Languages", NS_CONTEXT ) );

        OMElement languagesDefaultOMElement = capabilitiesDoc.getElement( languagesOMElement, new XPath( "wps:Default",
                                                                                                         NS_CONTEXT ) );

        OMElement languagesSupportedOMElement = capabilitiesDoc.getElement( languagesOMElement,
                                                                            new XPath( "wps:Default", NS_CONTEXT ) );

        languages.setDefauLt( capabilitiesDoc.getNodeAsString( languagesDefaultOMElement, new XPath( "ows:Language",
                                                                                                     NS_CONTEXT ), null ) );

        languages.setSupported( capabilitiesDoc.getNodesAsStrings( languagesSupportedOMElement,
                                                                   new XPath( "ows:Language", NS_CONTEXT ) ) );
        return languages;

    }

    /**
     * @return String containing the WSDL URL, <code>null</code> if nor URL available
     */
    public String getWSDL() {

        OMElement wsdlOMElement = capabilitiesDoc.getElement( capabilitiesDoc.getRootElement(), new XPath( "wps:WSDL",
                                                                                                           NS_CONTEXT ) );

        return wsdlOMElement.getAttributeValue( new QName( "http://www.w3.org/1999/xlink", "href" ) );

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "Service: " + this.service + "\n" );
        sb.append( "Version: " + this.version + "\n" );
        sb.append( "updateSequence: " + this.updateSequence + "\n" );
        sb.append( "lang: " + this.lang + "\n" );
        sb.append( "schemaLocation: " + this.schemaLocation + "\n" );
        return sb.toString();
    }

}
