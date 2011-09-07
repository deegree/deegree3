//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.metadata.Address;
import org.deegree.protocol.ows.metadata.ContactInfo;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceContact;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.ows.metadata.Telephone;

/**
 * Abstract base class for {@link OWSCapabilitiesAdapter} implementations that process <a
 * href="http://www.opengeospatial.org/standards/common">OWS Common</a>-based capabilities documents.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
abstract class AbstractOWSCommonCapabilitiesAdapter extends XMLAdapter implements OWSCapabilitiesAdapter {

    protected static final String XML1998NS = "http://www.w3.org/XML/1998/namespace";

    protected final NamespaceBindings nsContext = new NamespaceBindings();

    protected AbstractOWSCommonCapabilitiesAdapter( String owsNs ) {
        nsContext.addNamespace( "ows", owsNs );
        nsContext.addNamespace( "xlink", XLNNS );
    }

    /**
     * @return a {@link OWSCapabilities} instance, never <code>null</code>
     */
    public OWSCapabilities parseMetadata() {

        Version version = getNodeAsVersion( rootElement, new XPath( "@version", nsContext ), null );
        String sequence = getNodeAsString( rootElement, new XPath( "@updateSequence", nsContext ), null );

        ServiceIdentification serviceId = parseServiceIdentification();
        ServiceProvider serviceProvider = parseServiceProvider();
        OperationsMetadata opMetadata = parseOperationsMetadata();

        return new OWSCapabilities( version, sequence, serviceId, serviceProvider, opMetadata );
    }

    @Override
    public ServiceIdentification parseServiceIdentification() {

        OMElement serviceIdEl = getElement( getRootElement(), new XPath( "ows:ServiceIdentification", nsContext ) );
        if ( serviceIdEl == null ) {
            return null;
        }

        ServiceIdentification serviceId = new ServiceIdentification();

        Description description = parseDescription( serviceIdEl );
        serviceId.setDescription( description );

        OMElement serviceTypeEl = getElement( serviceIdEl, new XPath( "ows:ServiceType", nsContext ) );
        CodeType serviceType = parseCodeSpace( serviceTypeEl );
        serviceId.setServiceType( serviceType );

        XPath xpath = new XPath( "ows:ServiceTypeVersion", nsContext );
        List<OMElement> serviceTypeVersionEls = getElements( serviceIdEl, xpath );
        if ( serviceTypeVersionEls != null ) {
            for ( OMElement serviceTypeVersionEl : serviceTypeVersionEls ) {
                Version version = getNodeAsVersion( serviceTypeVersionEl, new XPath( ".", nsContext ), null );
                serviceId.getServiceTypeVersion().add( version );
            }
        }

        String[] profiles = getNodesAsStrings( serviceIdEl, new XPath( "ows:Profiles", nsContext ) );
        for ( int i = 0; i < profiles.length; i++ ) {
            serviceId.getProfiles().add( profiles[i] );
        }

        String fees = getNodeAsString( serviceIdEl, new XPath( "ows:Fees", nsContext ), null );
        serviceId.setFees( fees );

        String[] constraints = getNodesAsStrings( serviceIdEl, new XPath( "ows:AccessConstraints", nsContext ) );
        for ( String constraint : constraints ) {
            serviceId.getAccessConstraints().add( constraint );
        }

        return serviceId;
    }

    /**
     * @param serviceIdEl
     *            context {@link OMElement}
     * @return an {@link Description} instance, never <code>null</code>
     */
    protected Description parseDescription( OMElement serviceIdEl ) {

        List<OMElement> titleEls = getElements( serviceIdEl, new XPath( "ows:Title", nsContext ) );
        List<LanguageString> titles = new ArrayList<LanguageString>( titleEls.size() );
        for ( OMElement titleEl : titleEls ) {
            String lang = titleEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
            titles.add( new LanguageString( titleEl.getText(), lang ) );
        }

        List<OMElement> abstractEls = getElements( serviceIdEl, new XPath( "ows:Abstract", nsContext ) );
        List<LanguageString> abstracts = new ArrayList<LanguageString>( abstractEls.size() );
        for ( OMElement abstractEl : abstractEls ) {
            String lang = abstractEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
            abstracts.add( new LanguageString( abstractEl.getText(), lang ) );
        }

        List<OMElement> keywordsEls = getElements( serviceIdEl, new XPath( "ows:Keywords", nsContext ) );
        List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>(
                                                                                                                   keywordsEls.size() );
        for ( OMElement keywordsEl : keywordsEls ) {
            List<OMElement> keywordEls = getElements( keywordsEl, new XPath( "ows:Keyword", nsContext ) );
            List<LanguageString> keywordLS = new ArrayList<LanguageString>();
            if ( keywordEls != null ) {
                for ( OMElement keywordEl : keywordEls ) {
                    String lang = keywordEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
                    keywordLS.add( new LanguageString( keywordEl.getText(), lang ) );
                }
            }
            OMElement typeEl = getElement( keywordsEl, new XPath( "ows:Type", nsContext ) );
            CodeType type = null;
            if ( typeEl != null ) {
                type = parseCodeSpace( typeEl );
            }
            keywords.add( new Pair<List<LanguageString>, CodeType>( keywordLS, type ) );
        }

        return new Description( null, titles, abstracts, keywords );
    }

    @Override
    public ServiceProvider parseServiceProvider() {

        OMElement serviceProviderEl = getElement( getRootElement(), new XPath( "ows:ServiceProvider", nsContext ) );
        if ( serviceProviderEl == null ) {
            return null;
        }

        String providerName = getNodeAsString( serviceProviderEl, new XPath( "ows:ProviderName", nsContext ), null );
        String providerSite = getNodeAsString( serviceProviderEl,
                                               new XPath( "ows:ProviderSite/@xlink:href", nsContext ), null );

        OMElement serviceContactEl = getElement( serviceProviderEl, new XPath( "ows:ServiceContact", nsContext ) );
        ServiceContact serviceContact = null;
        if ( serviceContactEl != null ) {
            serviceContact = parseServiceContact( serviceContactEl );
        }
        return new ServiceProvider( providerName, providerSite, serviceContact );
    }

    /**
     * @param serviceContactEl
     *            context {@link OMElement}
     * @return an {@link ServiceContact} instance, never <code>null</code>
     */
    protected ServiceContact parseServiceContact( OMElement serviceContactEl ) {
        ServiceContact serviceContact = new ServiceContact();

        XPath xpath = new XPath( "ows:IndividualName", nsContext );
        serviceContact.setIndividualName( getNodeAsString( serviceContactEl, xpath, null ) );

        xpath = new XPath( "ows:PositionName", nsContext );
        serviceContact.setPositionName( getNodeAsString( serviceContactEl, xpath, null ) );

        xpath = new XPath( "ows:ContactInfo", nsContext );
        ContactInfo contactInfo = parseContactInfo( getElement( serviceContactEl, xpath ) );
        serviceContact.setContactInfo( contactInfo );

        xpath = new XPath( "ows:Role", nsContext );
        OMElement roleEl = getElement( serviceContactEl, xpath );
        if ( roleEl != null ) {
            serviceContact.setRole( parseCodeSpace( roleEl ) );
        }
        return serviceContact;
    }

    /**
     * @param contactInfoEl
     *            context {@link OMElement}
     * @return an {@link ContactInfo} instance, never <code>null</code>
     */
    protected ContactInfo parseContactInfo( OMElement contactInfoEl ) {
        ContactInfo contactInfo = new ContactInfo();

        XPath xpath = new XPath( "ows:Phone", nsContext );
        Telephone phone = parsePhone( getElement( contactInfoEl, xpath ) );
        contactInfo.setPhone( phone );

        xpath = new XPath( "ows:Address", nsContext );
        OMElement addressEl = getElement( contactInfoEl, xpath );
        contactInfo.setAddress( parseAddress( addressEl ) );

        xpath = new XPath( "ows:OnlineResource/@xlink:href", nsContext );
        contactInfo.setOnlineResource( getNodeAsURL( contactInfoEl, xpath, null ) );

        xpath = new XPath( "ows:HoursOfService", nsContext );
        contactInfo.setHoursOfService( getNodeAsString( contactInfoEl, xpath, null ) );

        xpath = new XPath( "ows:ContactInstructions", nsContext );
        contactInfo.setContactInstructions( getNodeAsString( contactInfoEl, xpath, null ) );

        return contactInfo;
    }

    /**
     * @param phoneEl
     *            context {@link OMElement}
     * @return an {@link Telephone} instance, never <code>null</code>
     */
    protected Telephone parsePhone( OMElement phoneEl ) {
        Telephone phone = new Telephone();

        XPath xpath = new XPath( "ows:Voice", nsContext );
        String[] voices = getNodesAsStrings( phoneEl, xpath );
        for ( int i = 0; i < voices.length; i++ ) {
            phone.getVoice().add( voices[i] );
        }

        xpath = new XPath( "ows:Facsimile", nsContext );
        String[] faxes = getNodesAsStrings( phoneEl, xpath );
        for ( int i = 0; i < faxes.length; i++ ) {
            phone.getFacsimile().add( faxes[i] );
        }

        return phone;
    }

    /**
     * @param addressEl
     *            context {@link OMElement}
     * @return an {@link Address} instance, never <code>null</code>
     */
    protected Address parseAddress( OMElement addressEl ) {
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

        xpath = new XPath( "ows:ElectronicMailAddress", nsContext );
        String[] eMails = getNodesAsStrings( addressEl, xpath );
        for ( int i = 0; i < eMails.length; i++ ) {
            address.getElectronicMailAddress().add( eMails[i] );
        }

        return address;
    }

    /**
     * @param omelement
     *            context {@link OMElement}
     * @return an {@link CodeType} instance, never <code>null</code>
     */
    protected CodeType parseCodeSpace( OMElement omelement ) {
        String codeSpace = getNodeAsString( omelement, new XPath( "codeSpace", nsContext ), null );
        if ( codeSpace != null ) {
            return new CodeType( omelement.getText(), codeSpace );
        }
        return new CodeType( omelement.getText() );
    }
}
