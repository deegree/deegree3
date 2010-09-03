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
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.metadata.Address;
import org.deegree.protocol.ows.metadata.ContactInfo;
import org.deegree.protocol.ows.metadata.DCP;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.ows.metadata.Domain;
import org.deegree.protocol.ows.metadata.Operation;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.PossibleValues;
import org.deegree.protocol.ows.metadata.Range;
import org.deegree.protocol.ows.metadata.ServiceContact;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceMetadata;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.ows.metadata.Telephone;
import org.deegree.protocol.ows.metadata.ValuesUnit;

/**
 * Extracts metadata from OGC service capabilities documents that comply to the OWS 1.1.0 specification.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Markus Schneider</a>
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
     * @return a {@link ServiceMetadata} instance, never <code>null</code>
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

        Version version = getNodeAsVersion( rootEl, new XPath( "@version", nsContext ), null );
        metadata.setVersion( version );

        String sequence = getNodeAsString( rootEl, new XPath( "@updateSequence", nsContext ), null );
        metadata.setUpdateSequence( sequence );

        return metadata;
    }

    /**
     * @param opMetadataEl
     *            context {@link OMElement}
     * @return an {@link OperationMetadata} instance, never <code>null</code>
     */
    private OperationsMetadata parseOperationsMetadata( OMElement opMetadataEl ) {
        OperationsMetadata opMetadata = new OperationsMetadata();

        XPath xpath = new XPath( "ows:Operation", nsContext );
        List<OMElement> opEls = getElements( opMetadataEl, xpath );
        if ( opEls != null ) {
            for ( OMElement opEl : opEls ) {
                Operation op = parseOperation( opEl );
                opMetadata.getOperation().add( op );
            }
        }

        xpath = new XPath( "ows:Parameter", nsContext );
        List<OMElement> paramEls = getElements( opMetadataEl, xpath );
        if ( paramEls != null ) {
            for ( OMElement paramEl : paramEls ) {
                Domain parameter = parseDomain( paramEl );
                opMetadata.getParameter().add( parameter );
            }
        }

        xpath = new XPath( "ows:Constraint", nsContext );
        List<OMElement> constaintEls = getElements( opMetadataEl, xpath );
        if ( constaintEls != null ) {
            for ( OMElement constaintEl : constaintEls ) {
                Domain constraint = parseDomain( constaintEl );
                opMetadata.getConstraint().add( constraint );
            }
        }

        xpath = new XPath( "ows:ExtendedCapabilities", nsContext );
        Object extededCapab = getNode( opMetadataEl, xpath );
        opMetadata.setExtendedCapabilies( extededCapab );

        return opMetadata;
    }

    /**
     * @param opEl
     *            context {@link OMElement}
     * @return an {@link Operation} instance, never <code>null</code>
     */
    private Operation parseOperation( OMElement opEl ) {
        Operation operation = new Operation();

        XPath xpath = new XPath( "@name", nsContext );
        String name = getNodeAsString( opEl, xpath, null );
        operation.setName( name );

        xpath = new XPath( "ows:DCP", nsContext );
        List<OMElement> dcpEls = getElements( opEl, xpath );
        if ( dcpEls != null ) {
            for ( OMElement dcpEl : dcpEls ) {
                DCP dcp = parseDCP( dcpEl );
                operation.getDCP().add( dcp );
            }
        }

        xpath = new XPath( "ows:Parameter", nsContext );
        List<OMElement> paramEls = getElements( opEl, xpath );
        if ( paramEls != null ) {
            for ( OMElement paramEl : paramEls ) {
                Domain parameter = parseDomain( paramEl );
                operation.getParameter().add( parameter );
            }
        }

        xpath = new XPath( "ows:Constraint", nsContext );
        List<OMElement> constaintEls = getElements( opEl, xpath );
        if ( constaintEls != null ) {
            for ( OMElement constaintEl : constaintEls ) {
                Domain constraint = parseDomain( constaintEl );
                operation.getConstraint().add( constraint );
            }
        }

        xpath = new XPath( "ows:Metadata", nsContext );
        List<OMElement> metadataEls = getElements( opEl, xpath );
        if ( metadataEls != null ) {
            for ( OMElement metadataEl : metadataEls ) {
                xpath = new XPath( "@xlink:href", nsContext );
                URL ref = getNodeAsURL( metadataEl, xpath, null );

                xpath = new XPath( "@about", nsContext );
                URL about = getNodeAsURL( metadataEl, xpath, null );
                operation.getMetadata().add( new Pair<URL, URL>( ref, about ) );
            }
        }

        return operation;
    }

    /**
     * @param domainEl
     *            context {@link OMElement}
     * @return an {@link Operation} instance, never <code>null</code>
     */
    private Domain parseDomain( OMElement domainEl ) {
        Domain domain = new Domain();

        XPath xpath = new XPath( "@name", nsContext );
        domain.setName( getNodeAsString( domainEl, xpath, null ) );

        PossibleValues possbileVals = parsePossibleValues( domainEl );
        domain.setPossibleValues( possbileVals );

        xpath = new XPath( "ows:DefaultValue", nsContext );
        domain.setDefaultValue( getNodeAsString( domainEl, xpath, null ) );

        xpath = new XPath( "ows:Meaning", nsContext );
        OMElement meaningEl = getElement( domainEl, xpath );
        String meaningRef = meaningEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        domain.setMeaningURL( meaningRef );
        String meangingText = meaningEl.getText();
        domain.setMeaningName( meangingText );

        xpath = new XPath( "ows:DataType", nsContext );
        OMElement datatypeEl = getElement( domainEl, xpath );
        String datatypeRef = datatypeEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        domain.setDataTypeURL( datatypeRef );
        String datatypeText = datatypeEl.getText();
        domain.setDataTypeName( datatypeText );

        ValuesUnit vals = parseValuesUnit( domainEl );
        domain.setValuesUnit( vals );

        xpath = new XPath( "ows:Metadata", nsContext );
        List<OMElement> metadataEls = getElements( domainEl, xpath );
        for ( OMElement metadataEl : metadataEls ) {
            xpath = new XPath( "@xlink:href", nsContext );
            URL ref = getNodeAsURL( metadataEl, xpath, null );

            xpath = new XPath( "@about", nsContext );
            URL about = getNodeAsURL( metadataEl, xpath, null );
            domain.getMetadata().add( new Pair<URL, URL>( ref, about ) );
        }

        return domain;
    }

    /**
     * @param domainEl
     *            context {@link OMElement}
     * @return an {@link ValuesUnit} instance, never <code>null</code>
     */
    private ValuesUnit parseValuesUnit( OMElement domainEl ) {
        ValuesUnit values = new ValuesUnit();

        XPath xpath = new XPath( "ows:ValueUnit", nsContext );
        OMElement valueUnitEl = getElement( domainEl, xpath );
        xpath = new XPath( "ows:UOM", nsContext );
        OMElement uomEl = getElement( valueUnitEl, xpath );
        String uomReference = uomEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        values.setUomURI( uomReference );
        String uomText = uomEl.getText();
        values.setUomName( uomText );

        xpath = new XPath( "ows:ReferenceSystem", nsContext );
        OMElement refSysEl = getElement( valueUnitEl, xpath );
        String refSysReference = refSysEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        values.setReferenceSystemURL( refSysReference );
        String refSysText = refSysEl.getText();
        values.setReferenceSystemName( refSysText );

        return values;
    }

    /**
     * @param domainEl
     *            context {@link OMElement}
     * @return an {@link PossibleValues} instance, never <code>null</code>
     */
    private PossibleValues parsePossibleValues( OMElement domainEl ) {
        PossibleValues possibleVals = new PossibleValues();

        XPath xpath = new XPath( "ows:AllowedValues", nsContext );
        OMElement allowedEl = getElement( domainEl, xpath );
        xpath = new XPath( "ows:Value", nsContext );
        String[] values = getNodesAsStrings( allowedEl, xpath );
        for ( int i = 0; i < values.length; i++ ) {
            possibleVals.getValue().add( values[i] );
        }

        xpath = new XPath( "ows:Range", nsContext );
        List<OMElement> rangeEls = getElements( domainEl, xpath );
        for ( OMElement rangeEl : rangeEls ) {
            Range range = parseRange( rangeEl );
            possibleVals.getRange().add( range );
        }

        xpath = new XPath( "ows:AnyValue", nsContext );
        if ( getNode( domainEl, xpath ) != null ) {
            possibleVals.setAnyValue();
        }

        xpath = new XPath( "ows:NoValues", nsContext );
        if ( getNode( domainEl, xpath ) != null ) {
            possibleVals.setNoValue();
        }

        xpath = new XPath( "ows:ValuesReference", nsContext );
        OMElement valuesRefEl = getElement( domainEl, xpath );
        String valuesRef = valuesRefEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        possibleVals.setReferenceURL( valuesRef );
        String valuesRefName = valuesRefEl.getText();
        possibleVals.setReferenceName( valuesRefName );

        return possibleVals;
    }

    /**
     * @param rangeEl
     *            context {@link OMElement}
     * @return an {@link Range} instance, never <code>null</code>
     */
    private Range parseRange( OMElement rangeEl ) {
        Range range = new Range();

        XPath xpath = new XPath( "ows:MinimumValue", nsContext );
        range.setMinimumValue( getNodeAsString( rangeEl, xpath, null ) );
        xpath = new XPath( "ows:MaximumValue", nsContext );
        range.setMaximumValue( getNodeAsString( rangeEl, xpath, null ) );
        xpath = new XPath( "ows:Spacing", nsContext );
        range.setSpacing( getNodeAsString( rangeEl, xpath, null ) );
        xpath = new XPath( "@ows:rangeClosure", nsContext );
        range.setRangeClosure( getNodeAsString( rangeEl, xpath, null ) );

        return range;
    }

    /**
     * @param dcpEl
     *            context {@link OMElement}
     * @return an {@link DCP} instance, never <code>null</code>
     */
    private DCP parseDCP( OMElement dcpEl ) {
        DCP dcp = new DCP();

        XPath xpath = new XPath( "ows:HTTP/ows:Get", nsContext );
        List<OMElement> getEls = getElements( dcpEl, xpath );
        if ( getEls != null ) {
            for ( OMElement getEl : getEls ) {
                xpath = new XPath( "@xlink:href", nsContext );
                URL href = getNodeAsURL( getEl, xpath, null );

                xpath = new XPath( "ows:Constraint", nsContext );
                List<OMElement> constaintEls = getElements( getEl, xpath );
                List<Domain> domains = new ArrayList<Domain>();
                for ( OMElement constaintEl : constaintEls ) {
                    Domain constraint = parseDomain( constaintEl );
                    domains.add( constraint );
                }

                dcp.getGetURLs().add( new Pair<URL, List<Domain>>( href, domains ) );
            }
        }

        xpath = new XPath( "ows:HTTP/ows:Post", nsContext );
        List<OMElement> postEls = getElements( dcpEl, xpath );
        if ( postEls != null ) {
            for ( OMElement postEl : postEls ) {
                xpath = new XPath( "@xlink:href", nsContext );
                URL href = getNodeAsURL( postEl, xpath, null );

                xpath = new XPath( "ows:Constraint", nsContext );
                List<OMElement> constaintEls = getElements( postEl, xpath );
                List<Domain> domains = new ArrayList<Domain>();
                for ( OMElement constaintEl : constaintEls ) {
                    Domain constraint = parseDomain( constaintEl );
                    domains.add( constraint );
                }

                dcp.getPostURLs().add( new Pair<URL, List<Domain>>( href, domains ) );
            }
        }

        return dcp;
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

    /**
     * @param serviceIdEl
     *            context {@link OMElement}
     * @return an {@link ServiceIdentification} instance, never <code>null</code>
     */
    private ServiceIdentification parseServiceIdentification( OMElement serviceIdEl ) {

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
    private Description parseDescription( OMElement serviceIdEl ) {
        Description description = new Description();

        List<OMElement> titleEls = getElements( serviceIdEl, new XPath( "ows:Title", nsContext ) );
        for ( OMElement titleEl : titleEls ) {
            String lang = titleEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
            description.getTitle().add( new LanguageString( titleEl.getText(), lang ) );
        }

        List<OMElement> abstractEls = getElements( serviceIdEl, new XPath( "ows:Abstract", nsContext ) );
        for ( OMElement abstractEl : abstractEls ) {
            String lang = abstractEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
            description.getAbstract().add( new LanguageString( abstractEl.getText(), lang ) );
        }

        List<OMElement> keywordsEls = getElements( serviceIdEl, new XPath( "ows:Keywords", nsContext ) );
        for ( OMElement keywordsEl : keywordsEls ) {
            List<OMElement> keywordSeq = getElements( keywordsEl, new XPath( "ows:Keyword", nsContext ) );
            List<LanguageString> keywordLS = new ArrayList<LanguageString>();
            if ( keywordSeq != null ) {
                for ( OMElement keywordEl : keywordSeq ) {
                    String lang = keywordEl.getAttributeValue( new QName( XML1998NS, "lang" ) );
                    keywordLS.add( new LanguageString( keywordEl.getText(), lang ) );
                }
            }
            OMElement typeEl = getElement( keywordsEl, new XPath( "ows:Type", nsContext ) );
            CodeType type = parseCodeSpace( typeEl );

            description.getKeywords().add( new Pair( keywordLS, type ) );
        }

        return description;
    }

    /**
     * @param omelement
     *            context {@link OMElement}
     * @return an {@link CodeType} instance, never <code>null</code>
     */
    private CodeType parseCodeSpace( OMElement omelement ) {
        String codeSpace = getNodeAsString( omelement, new XPath( "codeSpace", nsContext ), null );
        if ( codeSpace != null ) {
            return new CodeType( omelement.getText(), codeSpace );
        }
        return new CodeType( omelement.getText() );
    }

    /**
     * @param serviceProviderEl
     *            context {@link OMElement}
     * @return an {@link ServiceProvider} instance, never <code>null</code>
     */
    private ServiceProvider parseServiceProvider( OMElement serviceProviderEl ) {

        ServiceProvider serviceProvider = new ServiceProvider();

        XPath xpath = new XPath( "ows:ProviderName", nsContext );
        serviceProvider.setProviderName( getNodeAsString( serviceProviderEl, xpath, null ) );

        xpath = new XPath( "ows:ProviderSite/@xlink:href", nsContext );
        serviceProvider.setProviderSite( getNodeAsURL( serviceProviderEl, xpath, null ) );

        xpath = new XPath( "ows:ServiceContact", nsContext );
        OMElement serviceContactEl = getElement( serviceProviderEl, xpath );
        ServiceContact serviceContact = null;
        if ( serviceContactEl != null ) {
            serviceContact = parseServiceContact( serviceContactEl );
        }
        serviceProvider.setServiceContact( serviceContact );

        return serviceProvider;
    }

    /**
     * @param serviceContactEl
     *            context {@link OMElement}
     * @return an {@link ServiceContact} instance, never <code>null</code>
     */
    private ServiceContact parseServiceContact( OMElement serviceContactEl ) {
        ServiceContact serviceContact = new ServiceContact();

        XPath xpath = new XPath( "ows:IndividualName", nsContext );
        serviceContact.setIndividualName( getNodeAsString( serviceContactEl, xpath, null ) );

        xpath = new XPath( "ows:PositionName", nsContext );
        serviceContact.setPositionName( getNodeAsString( serviceContactEl, xpath, null ) );

        xpath = new XPath( "ows:ContactInfo", nsContext );
        ContactInfo contactInfo = parseContactInfo( getElement( serviceContactEl, xpath ) );
        serviceContact.setContactInfo( contactInfo );

        xpath = new XPath( "ows:Role", nsContext );
        CodeType role = parseCodeSpace( getElement( serviceContactEl, xpath ) );
        serviceContact.setRole( role );

        return serviceContact;
    }

    /**
     * @param contactInfoEl
     *            context {@link OMElement}
     * @return an {@link ContactInfo} instance, never <code>null</code>
     */
    private ContactInfo parseContactInfo( OMElement contactInfoEl ) {
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
    private Telephone parsePhone( OMElement phoneEl ) {
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

        xpath = new XPath( "ows:ElectronicMailAddress", nsContext );
        String[] eMails = getNodesAsStrings( addressEl, xpath );
        for ( int i = 0; i < eMails.length; i++ ) {
            address.getElectronicMailAddress().add( eMails[i] );
        }

        return address;
    }
}
