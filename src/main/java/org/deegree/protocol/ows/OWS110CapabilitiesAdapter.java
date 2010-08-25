//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.ows;

import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.metadata.Address;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceContact;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceMetadata;
import org.deegree.protocol.ows.metadata.ServiceProvider;

/**
 * Extracts metadata from OGC service capabilities documents that comply to the OWS 1.1.0 specification.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWS110CapabilitiesAdapter extends XMLAdapter {

    private static final String XML1998NS = "http://www.w3.org/XML/1998/namespace";

    private final NamespaceContext nsContext = new NamespaceContext();

    /**
     * Creates a new {@link OWS110CapabilitiesAdapter} instance.
     */
    public OWS110CapabilitiesAdapter() {
        nsContext.addNamespace( "wps", WPS_100_NS );
        nsContext.addNamespace( "ows", OWS_11_NS );
        nsContext.addNamespace( "xlink", XLNNS );
    }

    /**
     * Returns the service metadata.
     * 
     * @return the service metadata, never <code>null</code>
     */
    public ServiceMetadata parseMetadata() {

        ServiceMetadata metadata = new ServiceMetadata();

        OMElement rootEl = getRootElement();

        ServiceIdentification serviceId = null;
        OMElement serviceIdEl = getElement( rootEl, new XPath( "ows:ServiceIdentification", nsContext ) );
        if ( serviceIdEl != null ) {
            serviceId = parseServiceIdentification( serviceIdEl );
        }
        metadata.setServiceIdentification( serviceId );

        ServiceProvider serviceProvider = null;
        OMElement serviceProviderEl = getElement( rootEl, new XPath( "ows:ServiceProvider", nsContext ) );
        if ( serviceProviderEl != null ) {
            serviceProvider = parseServiceProvider( serviceProviderEl );
        }
        metadata.setServiceProvider( serviceProvider );

        OperationsMetadata opMetadata = null;
        OMElement opMetadataEl = getElement( rootEl, new XPath( "ows:OperationsMetadata", nsContext ) );
        if ( opMetadataEl != null ) {
            opMetadata = parseOperationsMetadata( opMetadataEl );
        }
        metadata.setOperationsMetadata( opMetadata );

        Version version = getRequiredNodeAsVersion( rootEl, new XPath( "@version", nsContext ) );
        metadata.setVersion( version );

        String sequence = getNodeAsString( rootEl, new XPath( "@updateSequence", nsContext ), null );
        metadata.setUpdateSequence( sequence );

        return metadata;
    }

    /**
     * @param opMetadataEl
     * @return
     */
    private OperationsMetadata parseOperationsMetadata( OMElement opMetadataEl ) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the URL for the specified operation and HTTP method.
     * 
     * @param operation
     *            name of the operation, must not be <code>null</code>
     * @param post
     *            if set to true, the URL for POST requests will be returned, otherwise the URL for GET requests will be
     *            returned
     * @return the operation URL (trailing question marks are stripped), can be <code>null</code> (if the
     *         operation/method is not announced by the service)
     * @throws MalformedURLException
     *             if the announced URL is malformed
     */
    public URL getOperationURL( String operation, boolean post )
                            throws MalformedURLException {

        String xpathStr = "ows:OperationsMetadata/ows:Operation[@name='" + operation + "']/ows:DCP/ows:HTTP/ows:"
                          + ( post ? "Post" : "Get" ) + "/@xlink:href";
        URL url = null;
        String href = getNodeAsString( getRootElement(), new XPath( xpathStr, nsContext ), null );
        if ( href != null ) {
            if ( href.endsWith( "?" ) ) {
                href = href.substring( 0, href.length() - 1 );
            }
            url = new URL( href );
        }
        return url;
    }

    private ServiceIdentification parseServiceIdentification( OMElement serviceIdEl ) {

        ServiceIdentification serviceId = new ServiceIdentification();

        // List<OMElement> titleEls = getElements( serviceIdEl, new XPath( "ows:Title", nsContext ) );
        // for ( OMElement titleEl : titleEls ) {
        // String lang = titleEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
        // serviceId.getTitle().add( new LanguageString( titleEl.getText(), lang ) );
        // }
        //
        // List<OMElement> abstractEls = getElements( serviceIdEl, new XPath( "ows:Abstract", nsContext ) );
        // for ( OMElement abstractEl : abstractEls ) {
        // String lang = abstractEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
        // serviceId.getAbstract().add( new LanguageString( abstractEl.getText(), lang ) );
        // }
        //
        // List<OMElement> keywordsEls = getElements( serviceIdEl, new XPath( "ows:Keywords", nsContext ) );
        // for ( OMElement keywordsEl : keywordsEls ) {
        // List<OMElement> keywordSeq = getRequiredElements( keywordsEl, new XPath( "ows:Keyword", nsContext ) );
        // List<LanguageString> keywordLS = new ArrayList<LanguageString>();
        // for ( OMElement keywordEl : keywordSeq ) {
        // String lang = keywordEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
        // keywordLS.add( new LanguageString( keywordEl.getText(), lang ) );
        // }
        // OMElement typeEl = getElement( keywordsEl, new XPath( "ows:Type", nsContext ) );
        // CodeType type = parseCodeSpace( typeEl );
        //
        // serviceId.getKeywords().add( new Pair( keywordLS, type ) );
        // }
        //
        // OMElement serviceTypeEl = getRequiredElement( serviceIdEl, new XPath( "ows:ServiceType", nsContext ) );
        // CodeType serviceType = parseCodeSpace( serviceTypeEl );
        // serviceId.setServiceType( serviceType );
        //
        // List<OMElement> serviceTypeVersionEls = getRequiredElements( serviceIdEl, new XPath(
        // "ows:ServiceTypeVersion",
        // nsContext ) );
        // for ( OMElement serviceTypeVersionEl : serviceTypeVersionEls ) {
        // Version version = getRequiredNodeAsVersion( serviceTypeVersionEl, new XPath( ".", nsContext ) );
        // serviceId.getServiceTypeVersion().add( version );
        // }
        //
        // List<OMElement> profilesEl = getNodes( serviceIdEl, new XPath( "ows:Profiles", nsContext ) );
        // for ( OMElement profileEl : profilesEl ) {
        // URL profile = getRequiredNodeAsURL( profileEl, new XPath( ".", nsContext ) );
        // serviceId.getProfiles().add( profile );
        // }
        //
        // String fees = getNodeAsString( serviceIdEl, new XPath( "ows:Fees", nsContext ), null );
        // serviceId.setFees( fees );
        //
        // String[] constraints = getNodesAsStrings( serviceIdEl, new XPath( "ows:AccessConstraints", nsContext ) );
        // for ( String constraint : constraints ) {
        // serviceId.getAccessConstraints().add( constraint );
        // }

        return serviceId;
    }

    /**
     * @param typeEl
     * @return
     */
    private CodeType parseCodeSpace( OMElement omelement ) {
        String codeSpace = getNodeAsString( omelement, new XPath( "codeSpace", nsContext ), null );
        if ( codeSpace != null ) {
            return new CodeType( omelement.getText(), codeSpace );
        }
        return new CodeType( omelement.getText() );
    }

    private ServiceProvider parseServiceProvider( OMElement serviceProviderEl ) {

        ServiceProvider serviceProvider = new ServiceProvider();

        XPath xpath = new XPath( "ows:ProviderName", nsContext );
        serviceProvider.setProviderName( getNodeAsString( serviceProviderEl, xpath, null ) );

        xpath = new XPath( "ows:ProviderSite/@xlink:href", nsContext );
        serviceProvider.setProviderSite( getRequiredNodeAsURL( serviceProviderEl, xpath ) );

        xpath = new XPath( "ows:ServiceContact", nsContext );
        OMElement serviceContactEl = getRequiredElement( serviceProviderEl, xpath );
        ServiceContact serviceContact = parseServiceContact( serviceContactEl );
        serviceProvider.setServiceContact( serviceContact );

        return serviceProvider;
    }

    private ServiceContact parseServiceContact( OMElement serviceContactEl ) {

        ServiceContact serviceContact = new ServiceContact();

        // XPath xpath = new XPath( "ows:IndividualName", nsContext );
        // serviceContact.setIndividualName( getRequiredNodeAsString( serviceContactEl, xpath ) );
        //
        // xpath = new XPath( "ows:PositionName", nsContext );
        // serviceContact.setPositionName( getRequiredNodeAsString( serviceContactEl, xpath ) );
        //
        // xpath = new XPath( "ows:Phone", nsContext );
        // serviceContact.setPhone( getNodeAsString( serviceContactEl, xpath, null ) );
        //
        // xpath = new XPath( "ows:Facsimile", nsContext );
        // serviceContact.setFacsimile( getNodeAsString( serviceContactEl, xpath, null ) );
        //
        // xpath = new XPath( "ows:ElectronicMailAddress", nsContext );
        // String[] eMails = getNodesAsStrings( serviceContactEl, xpath );
        // for ( String eMail : eMails ) {
        // serviceContact.getElectronicMailAddress().add( eMail );
        // }
        //
        // xpath = new XPath( "ows:Address", nsContext );
        // OMElement addressEl = getRequiredElement( serviceContactEl, xpath );
        // serviceContact.setAddress( parseAddress( addressEl ) );
        //
        // xpath = new XPath( "ows:OnlineResource/@xlink:href", nsContext );
        // serviceContact.setOnlineResource( getRequiredNodeAsURL( serviceContactEl, xpath ) );
        //
        // xpath = new XPath( "ows:HoursOfService", nsContext );
        // serviceContact.setHoursOfService( getNodeAsString( serviceContactEl, xpath, null ) );
        //
        // xpath = new XPath( "ows:ContactInstructions", nsContext );
        // serviceContact.setContactInstructions( getNodeAsString( serviceContactEl, xpath, null ) );
        //
        // xpath = new XPath( "ows:Role", nsContext );
        // serviceContact.setRole( getRequiredNodeAsString( serviceContactEl, xpath ) );

        return serviceContact;
    }

    private Address parseAddress( OMElement addressEl ) {

        Address address = new Address();

        XPath xpath = new XPath( "ows:DeliveryPoint", nsContext );
        String[] deliveryPoints = getNodesAsStrings( addressEl, xpath );
        for ( String deliveryPoint : deliveryPoints ) {
            address.getDeliveryPoint().add( deliveryPoint );
        }

        xpath = new XPath( "ows:City", nsContext );
        address.setCity( getNodeAsString( addressEl, xpath, null ) );

        xpath = new XPath( "ows:AdministrativeArea", nsContext );
        address.setAdministrativeArea( getNodeAsString( addressEl, xpath, null ) );

        xpath = new XPath( "ows:PostalCode", nsContext );
        address.setPostalCode( getNodeAsString( addressEl, xpath, null ) );

        xpath = new XPath( "ows:Country", nsContext );
        address.setCountry( getNodeAsString( addressEl, xpath, null ) );

        return address;
    }
}
