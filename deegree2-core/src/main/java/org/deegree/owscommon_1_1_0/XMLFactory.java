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

package org.deegree.owscommon_1_1_0;

import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.framework.xml.XMLTools.create;
import static org.deegree.ogcbase.CommonNamespaces.OWSNS;
import static org.deegree.ogcbase.CommonNamespaces.OWSNS_1_1_0;
import static org.deegree.ogcbase.CommonNamespaces.OWS_1_1_0PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.XLINK_PREFIX;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The <code>XMLFactory</code> is a convenience class, which exports ows-common 1.1.0 beans to their xml
 * representation.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class XMLFactory extends org.deegree.ogcbase.XMLFactory {

    private static String PRE = OWS_1_1_0PREFIX + ":";

    /**
     * make an xml representation of given capabilities bean.
     *
     * @param root
     *            to export to.
     * @param capabilities
     *            to export
     */
    public void exportCapabilities( Element root, OWSCommonCapabilities capabilities ) {
        if ( root != null && capabilities != null ) {
            root.setAttribute( "version", capabilities.getVersion() );
            root.setAttribute( "updateSequence", capabilities.getUpdateSequence() );
            appendServiceIdentification( root, capabilities.getServiceIdentification() );
            appendServiceProvider( root, capabilities.getServiceProvider() );
            appendOperationsMetadata( root, capabilities.getOperationsMetadata() );
        }
    }

    /**
     * Will create an XMLFragment which holds the ows:OperationResponse as the root element, values from the given
     * manifest will be appended.
     *
     * @param operationResponse
     *            to create the dom-xml representation from.
     * @return the xmlFragment as defined in ows 1.1.0 or <code>null</code> if the given parameter is
     *         <code>null</code>.
     */
    public XMLFragment createOperationResponse( Manifest operationResponse ) {
        if ( operationResponse == null ) {
            return null;
        }
        Document doc = create();
        Element root = doc.createElementNS( OWSNS_1_1_0.toASCIIString(), PRE + "OperationResponse" );
        appendManifest( root, operationResponse );
        return new XMLFragment( root );
    }

    /**
     * Exports the manifest type to its dom-xml representation. If either one of the params is <code>null</code>
     * nothing is done.
     *
     * @param root
     *            to append the manifest values to.
     * @param manifestType
     *            to export.
     */
    public void appendManifest( Element root, Manifest manifestType ) {
        if ( root != null && manifestType != null ) {
            BasicIdentification basicIdentification = manifestType.getBasicManifestIdentification();
            appendBasicIdentification( root, basicIdentification );
            List<ReferenceGroup> referenceGroups = manifestType.getReferenceGroups();
            if ( referenceGroups != null && referenceGroups.size() > 0 ) {
                for ( ReferenceGroup referenceGroup : referenceGroups ) {
                    if ( referenceGroup != null ) {
                        Element rgElement = appendElement( root, OWSNS_1_1_0, PRE + "ReferenceGroup" );
                        appendBasicIdentification( rgElement, referenceGroup.getBasicRGId() );
                        List<Reference> references = referenceGroup.getReferences();
                        if ( references.size() > 0 ) {
                            for ( Reference reference : references ) {
                                appendReference( rgElement, reference );
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Appends the given reference to the given root. If either one of the params is <code>null</code> nothing is
     * done.
     *
     * @param root
     *            to append too.
     * @param reference
     *            to append.
     */
    protected void appendReference( Element root, Reference reference ) {
        if ( root != null && reference != null ) {
            Element refElement = appendElement( root, OWSNS_1_1_0, PRE + "Reference" );
            String attrib = reference.getHrefAttribute();
            if ( attrib != null && !"".equals( attrib.trim() ) ) {
                refElement.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", attrib );
            }
            attrib = reference.getRoleAttribute();
            if ( attrib != null && !"".equals( attrib.trim() ) ) {
                refElement.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":role", attrib );
            }
            attrib = reference.getTypeAttribute();
            if ( attrib != null && !"".equals( attrib.trim() ) ) {
                refElement.setAttributeNS( OWSNS_1_1_0.toASCIIString(), PRE + "type", attrib );
            }
            Pair<String, String> identifier = reference.getIdentifier();
            if ( identifier != null ) {
                Element t = appendElement( refElement, OWSNS_1_1_0, PRE + "Identifier", identifier.first );
                if ( identifier.second != null ) {
                    t.setAttribute( "codeSpace", identifier.second );
                }
            }
            appendAbstracts( refElement, reference.getAbstracts() );
            String format = reference.getFormat();
            if ( format != null && !"".equals( format.trim() ) ) {
                appendElement( refElement, OWSNS_1_1_0, PRE + "Format", format );
            }
            appendMetadataAttribs( refElement, reference.getMetadatas() );
        }

    }

    /**
     * Appends the given basicIdentification to the given root. If either one of the params is <code>null</code>
     * nothing is done.
     *
     * @param root
     *            to append too.
     * @param basicIdentification
     *            to append.
     */
    protected void appendBasicIdentification( Element root, BasicIdentification basicIdentification ) {
        if ( root != null && basicIdentification != null ) {
            appendTitles( root, basicIdentification.getTitles() );
            appendAbstracts( root, basicIdentification.getAbstracts() );
            appendKeywords( root, basicIdentification.getKeywords() );

            Pair<String, String> identifier = basicIdentification.getIdentifier();
            if ( identifier != null ) {
                Element t = appendElement( root, OWSNS_1_1_0, PRE + "Identifier", identifier.first );
                if ( identifier.second != null ) {
                    t.setAttribute( "codeSpace", identifier.second );
                }
            }

            appendMetadataAttribs( root, basicIdentification.getMetadatas() );
        }
    }

    /**
     * @param capabilitiesElement
     *            to export to if <code>null</code> nothing will be done.
     * @param serviceIdentification
     *            to append if <code>null</code> nothing will be done (optional).
     */
    protected void appendServiceIdentification( Element capabilitiesElement, ServiceIdentification serviceIdentification ) {
        if ( capabilitiesElement == null || serviceIdentification == null ) {
            return;
        }
        Element sid = appendElement( capabilitiesElement, OWSNS_1_1_0, PRE + "ServiceIdentification" );
        List<String> strings = serviceIdentification.getTitles();
        if ( strings != null ) {
            appendTitles( sid, strings );
        }

        strings = serviceIdentification.getAbstracts();
        if ( strings != null ) {
            appendAbstracts( sid, strings );
        }

        List<Keywords> keywords = serviceIdentification.getKeywords();
        if ( keywords != null ) {
            appendKeywords( sid, keywords );
        }

        // ServiceType mandatory
        Pair<String, String> serviceType = serviceIdentification.getServicetype();
        Element ste = appendElement( sid, OWSNS_1_1_0, PRE + "ServiceType", serviceType.first );
        if ( serviceType.second != null ) {
            ste.setAttribute( "codeSpace", serviceType.second );
        }

        // ServiceTypeVersion(s) mandatory.
        List<String> serviceTypeVersions = serviceIdentification.getServiceTypeVersions();
        for ( String serviceTypeVersion : serviceTypeVersions ) {
            appendElement( sid, OWSNS_1_1_0, PRE + "ServiceTypeVersion", serviceTypeVersion );
        }

        List<String> profiles = serviceIdentification.getProfiles();
        for ( String profile : profiles ) {
            appendElement( sid, OWSNS_1_1_0, PRE + "Profile", profile );
        }

        String fee = serviceIdentification.getFees();
        if ( fee != null && !"".equals( fee.trim() ) ) {
            appendElement( sid, OWSNS_1_1_0, PRE + "Fees", fee );
        }

        List<String> accessConst = serviceIdentification.getAccessConstraints();
        if ( accessConst != null ) {
            for ( String constraint : accessConst ) {
                appendElement( sid, OWSNS_1_1_0, PRE + "AccessConstraints", constraint );
            }
        }

    }

    /**
     * Appends the given titles to the given root. If either one of the params is <code>null</code> nothing is done.
     *
     * @param root
     *            to append too.
     * @param titles
     *            to append.
     */
    protected void appendTitles( Element root, List<String> titles ) {
        if ( root != null && titles != null ) {
            for ( String title : titles ) {
                if ( title != null && !"".equals( title.trim() ) ) {
                    appendElement( root, OWSNS_1_1_0, PRE + "Title", title );
                }
            }
        }
    }

    /**
     * Appends the given abstracts to the given root. If either one of the params is <code>null</code> nothing is
     * done.
     *
     * @param root
     *            to append too.
     * @param abstracts
     *            to append.
     */
    protected void appendAbstracts( Element root, List<String> abstracts ) {
        if ( root != null && abstracts != null ) {
            for ( String string : abstracts ) {
                if ( string != null && !"".equals( string.trim() ) ) {
                    appendElement( root, OWSNS_1_1_0, PRE + "Abstract", string );
                }
            }
        }
    }

    /**
     * Appends the given keywords to the given root. If either one of the params is <code>null</code> nothing is done.
     *
     * @param root
     *            to append too.
     * @param keywords
     *            to append.
     */
    protected void appendKeywords( Element root, List<Keywords> keywords ) {
        if ( root != null && keywords != null ) {
            for ( Keywords kw : keywords ) {
                if ( kw != null ) {
                    Element keywordsElement = appendElement( root, OWSNS_1_1_0, PRE + "Keywords" );
                    List<String> kws = kw.getkeywords();
                    if ( kws != null ) {
                        for ( String s : kws ) {
                            appendElement( keywordsElement, OWSNS_1_1_0, PRE + "Keyword", s );
                        }
                    }
                    Pair<String, String> type = kw.getType();
                    if ( type != null ) {
                        Element t = appendElement( keywordsElement, OWSNS_1_1_0, PRE + "Type", type.first );
                        if ( type.second != null ) {
                            t.setAttribute( "codeSpace", type.second );
                        }
                    }
                }
            }
        }

    }

    /**
     *
     * @param capabilitiesElement
     *            to export to if <code>null</code> nothing will be done.
     * @param serviceProvider
     *            to append if <code>null</code> nothing will be done (optional).
     */
    protected void appendServiceProvider( Element capabilitiesElement, ServiceProvider serviceProvider ) {
        if ( capabilitiesElement == null || serviceProvider == null ) {
            return;
        }
        Element sp = appendElement( capabilitiesElement, OWSNS_1_1_0, PRE + "ServiceProvider" );
        appendElement( sp, OWSNS_1_1_0, PRE + "ProviderName", serviceProvider.getProviderName() );
        if ( serviceProvider.getProviderSite() != null && !"".equals( serviceProvider.getProviderSite() ) ) {
            Element provSite = appendElement( sp, OWSNS_1_1_0, PRE + "ProviderSite" );
            provSite.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", serviceProvider.getProviderSite() );
        }

        appendServiceContact( sp, serviceProvider.getServiceContact() );

    }

    /**
     * @param root
     *            to export to if <code>null</code> nothing will be done.
     * @param serviceContact
     *            to append if <code>null</code> nothing will be done (optional).
     */
    protected void appendServiceContact( Element root, ServiceContact serviceContact ) {
        if ( root == null || serviceContact == null ) {
            return;
        }
        Element sc = appendElement( root, OWSNS_1_1_0, PRE + "ServiceContact" );
        // optional IndividualName
        if ( serviceContact.getIndividualName() != null && !"".equals( serviceContact.getIndividualName() ) ) {
            appendElement( sc, OWSNS_1_1_0, PRE + "IndividualName", serviceContact.getIndividualName() );
        }
        // optional PositionName
        if ( serviceContact.getPositionName() != null && !"".equals( serviceContact.getPositionName() ) ) {
            appendElement( sc, OWSNS_1_1_0, PRE + "PositionName", serviceContact.getPositionName() );
        }
        // optional ContactInfo
        appendContactInfo( sc, serviceContact.getContactInfo() );

        // optional Role.
        if ( serviceContact.getRole() != null ) {
            Element r = appendElement( sc, OWSNS_1_1_0, PRE + "Role", serviceContact.getRole().first );
            if ( serviceContact.getRole().second != null ) {
                r.setAttribute( "codeSpace", serviceContact.getRole().second );
            }
        }

    }

    /**
     * @param root
     *            (usually a serviceContact) to export to if <code>null</code> nothing will be done.
     * @param contactInfo
     *            to append if <code>null</code> nothing will be done (optional).
     */
    protected void appendContactInfo( Element root, ContactInfo contactInfo ) {
        if ( root == null || contactInfo == null ) {
            return;
        }
        Element ci = appendElement( root, OWSNS_1_1_0, PRE + "ContactInfo" );
        if ( contactInfo.getPhone() != null ) {
            Pair<List<String>, List<String>> phone = contactInfo.getPhone();
            Element phoneE = appendElement( ci, OWSNS_1_1_0, PRE + "Phone" );
            if ( phone.first != null && phone.first.size() != 0 ) {
                for ( String t : phone.first ) {
                    appendElement( phoneE, OWSNS_1_1_0, PRE + "Voice", t );
                }
            }
            if ( phone.second != null && phone.second.size() != 0 ) {
                for ( String t : phone.second ) {
                    appendElement( phoneE, OWSNS_1_1_0, PRE + "Facsimile", t );
                }
            }
        }

        if ( contactInfo.hasAdress() ) {
            Element addr = appendElement( ci, OWSNS_1_1_0, PRE + "Address" );
            if ( contactInfo.getDeliveryPoint() != null ) {
                for ( String s : contactInfo.getDeliveryPoint() ) {
                    appendElement( addr, OWSNS_1_1_0, PRE + "DeliveryPoint", s );
                }
            }
            if ( contactInfo.getCity() != null && !"".equals( contactInfo.getCity().trim() ) ) {
                appendElement( addr, OWSNS_1_1_0, PRE + "City", contactInfo.getCity().trim() );
            }
            if ( contactInfo.getAdministrativeArea() != null && !"".equals( contactInfo.getAdministrativeArea().trim() ) ) {
                appendElement( addr, OWSNS_1_1_0, PRE + "AdministrativeArea",
                               contactInfo.getAdministrativeArea().trim() );
            }
            if ( contactInfo.getPostalCode() != null && !"".equals( contactInfo.getPostalCode().trim() ) ) {
                appendElement( addr, OWSNS_1_1_0, PRE + "PostalCode", contactInfo.getPostalCode().trim() );
            }
            if ( contactInfo.getCountry() != null && !"".equals( contactInfo.getCountry().trim() ) ) {
                appendElement( addr, OWSNS_1_1_0, PRE + "Country", contactInfo.getCountry().trim() );
            }
            if ( contactInfo.getElectronicMailAddress() != null ) {
                for ( String s : contactInfo.getElectronicMailAddress() ) {
                    appendElement( addr, OWSNS_1_1_0, PRE + "ElectronicMailAddress", s );
                }
            }
        }
        if ( contactInfo.getOnlineResource() != null && !"".equals( contactInfo.getOnlineResource().trim() ) ) {
            Element onlineResource = appendElement( ci, OWSNS_1_1_0, PRE + "OnlineResource" );
            onlineResource.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href",
                                           contactInfo.getOnlineResource().trim() );
        }

        if ( contactInfo.getHoursOfService() != null && !"".equals( contactInfo.getHoursOfService().trim() ) ) {
            appendElement( ci, OWSNS_1_1_0, PRE + "HoursOfService", contactInfo.getHoursOfService().trim() );
        }

        if ( contactInfo.getContactInstructions() != null && !"".equals( contactInfo.getContactInstructions().trim() ) ) {
            appendElement( ci, OWSNS_1_1_0, PRE + "ContactInstructions", contactInfo.getContactInstructions().trim() );
        }
    }

    /**
     * @param capabilitiesElement
     *            to export to if <code>null</code> nothing will be done.
     * @param operationsMetadata
     *            to append if <code>null</code> nothing will be done (optional).
     */
    public void appendOperationsMetadata( Element capabilitiesElement, OperationsMetadata operationsMetadata ) {
        if ( capabilitiesElement == null || operationsMetadata == null ) {
            return;
        }
        Element omd = appendElement( capabilitiesElement, OWSNS_1_1_0, PRE + "OperationsMetadata" );
        appendOperations( omd, operationsMetadata.getOperations() );
        if ( operationsMetadata.getParameters() != null ) {
            for ( DomainType domainType : operationsMetadata.getParameters() ) {
                Element parameter = appendElement( omd, OWSNS_1_1_0, PRE + "Parameter" );
                appendDomainType( parameter, domainType );
            }
        }

        if ( operationsMetadata.getConstraints() != null ) {
            for ( DomainType domainType : operationsMetadata.getConstraints() ) {
                Element constraint = appendElement( omd, OWSNS_1_1_0, PRE + "Constraint" );
                appendDomainType( constraint, domainType );
            }
        }
        if ( operationsMetadata.getExtendedCapabilities() != null ) {
            Node tmp = omd.getOwnerDocument().importNode( operationsMetadata.getExtendedCapabilities(), true );
            omd.appendChild( tmp );
        }
    }

    /**
     * @param root
     *            to append to.
     * @param operations
     *            may not be <code>null</code>
     * @throws IllegalArgumentException
     *             if the list of operations &lt; 2 or <code>null</code>.
     */
    protected void appendOperations( Element root, List<Operation> operations )
                            throws IllegalArgumentException {
        if ( root == null ) {
            return;
        }
        if ( operations == null || operations.size() < 2 ) {
            throw new IllegalArgumentException( "The list of operations must at least contain 2 operations." );
        }
        for ( Operation operation : operations ) {
            Element opElement = appendElement( root, OWSNS_1_1_0, PRE + "Operation" );
            List<Pair<String, List<DomainType>>> getURLs = operation.getGetURLs();
            List<Pair<String, List<DomainType>>> postURLs = operation.getPostURLs();
            Element dcp = appendElement( opElement, OWSNS_1_1_0, PRE + "DCP" );
            Element httpElement = appendElement( dcp, OWSNS_1_1_0, PRE + "HTTP" );
            if ( getURLs != null ) {
                for ( Pair<String, List<DomainType>> getURL : getURLs ) {
                    Element get = appendElement( httpElement, OWSNS_1_1_0, PRE + "Get" );
                    if ( getURL.first != null ) {
                        get.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", getURL.first );
                    }
                    if ( getURL.second != null && getURL.second.size() > 0 ) {
                        Element constraint = appendElement( get, OWSNS_1_1_0, PRE + "Constraint" );
                        for ( DomainType domainType : getURL.second ) {
                            appendDomainType( constraint, domainType );
                        }
                    }
                }
            }
            if ( postURLs != null ) {
                for ( Pair<String, List<DomainType>> postURL : postURLs ) {
                    Element post = appendElement( httpElement, OWSNS_1_1_0, PRE + "Post" );
                    if ( postURL.first != null ) {
                        post.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href", postURL.first );
                    }
                    if ( postURL.second != null && postURL.second.size() > 0 ) {
                        Element constraint = appendElement( post, OWSNS_1_1_0, PRE + "Constraint" );
                        for ( DomainType domainType : postURL.second ) {
                            appendDomainType( constraint, domainType );
                        }
                    }
                }
            }

            if ( operation.getParameters() != null ) {
                for ( DomainType domainType : operation.getParameters() ) {
                    Element parameter = appendElement( opElement, OWSNS_1_1_0, PRE + "Parameter" );
                    appendDomainType( parameter, domainType );
                }
            }

            if ( operation.getConstraints() != null ) {
                for ( DomainType domainType : operation.getConstraints() ) {
                    Element constraint = appendElement( opElement, OWSNS_1_1_0, PRE + "Constraint" );
                    appendDomainType( constraint, domainType );
                }
            }
            if ( operation.getMetadataAttribs() != null && operation.getMetadataAttribs().size() > 0 ) {
                appendMetadataAttribs( opElement, operation.getMetadataAttribs() );
            }
            opElement.setAttribute( "name", operation.getName() );
        }

    }

    /**
     * @param domainTypeElement
     *            to append the domainttype element to.
     * @param domainType
     *            to append.
     */
    protected void appendDomainType( Element domainTypeElement, DomainType domainType ) {
        if ( domainTypeElement == null || domainType == null ) {
            return;
        }
        domainTypeElement.setAttribute( "name", domainType.getName() );
        if ( domainType.hasAllowedValues() ) {
            Element allowedValuesElem = appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "AllowedValues" );
            List<String> values = domainType.getValues();
            if ( values != null ) {
                for ( String value : values ) {
                    appendElement( allowedValuesElem, OWSNS_1_1_0, PRE + "Value", value );
                }
            }
            List<Range> ranges = domainType.getRanges();
            if ( ranges != null ) {
                for ( Range r : ranges ) {
                    Element re = appendElement( allowedValuesElem, OWSNS_1_1_0, PRE + "Range" );
                    if ( r.getMinimumValue() != null && !"".equals( r.getMinimumValue().trim() ) ) {
                        appendElement( re, OWSNS_1_1_0, PRE + "MinimumValue", r.getMinimumValue() );
                    }
                    if ( r.getMaximumValue() != null && !"".equals( r.getMaximumValue().trim() ) ) {
                        appendElement( re, OWSNS_1_1_0, PRE + "MaximumValue", r.getMaximumValue() );
                    }
                    if ( r.getSpacing() != null && !"".equals( r.getSpacing().trim() ) ) {
                        appendElement( re, OWSNS_1_1_0, PRE + "Spacing", r.getSpacing() );
                    }
                    re.setAttribute( "rangeClosure", r.getRangeClosure() );
                }
            }
        } else if ( domainType.hasAnyValue() ) {
            appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "AnyValue" );
        } else if ( domainType.hasNoValues() ) {
            appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "NoValues" );
        } else { // a choice therefore this must be.
            Element vref = appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "ValuesReference",
                                          domainType.getValuesReference().first );
            if ( domainType.getValuesReference().second != null ) {
                vref.setAttribute( "reference", domainType.getValuesReference().second );
            }
        }
        if ( domainType.getDefaultValue() != null && !"".equals( domainType.getDefaultValue() ) ) {
            appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "DefaultValue", domainType.getDefaultValue().trim() );
        }
        if ( domainType.getMeaning() != null ) {
            Element tmp = appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "Meaning", domainType.getMeaning().first );
            if ( domainType.getMeaning().second != null ) {
                tmp.setAttribute( "reference", domainType.getMeaning().second );
            }
        }
        if ( domainType.getDataType() != null ) {
            Element tmp = appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "DataType",
                                         domainType.getDataType().first );
            if ( domainType.getDataType().second != null ) {
                tmp.setAttribute( "reference", domainType.getDataType().second );
            }
        }
        if ( domainType.hasValuesUnit() ) {
            Element valuesUnit = appendElement( domainTypeElement, OWSNS_1_1_0, PRE + "ValuesUnit" );
            if ( domainType.getUom() != null ) {
                Element tmp = appendElement( valuesUnit, OWSNS_1_1_0, PRE + "UOM", domainType.getUom().first );
                if ( domainType.getUom().second != null ) {
                    tmp.setAttribute( "reference", domainType.getUom().second );
                }
            } else {
                Element tmp = appendElement( valuesUnit, OWSNS_1_1_0, PRE + "ReferenceSystem",
                                             domainType.getReferenceSystem().first );
                if ( domainType.getReferenceSystem().second != null ) {
                    tmp.setAttribute( "reference", domainType.getReferenceSystem().second );
                }
            }
        }
        if ( domainType.getMetadataAttribs() != null && domainType.getMetadataAttribs().size() > 0 ) {
            appendMetadataAttribs( domainTypeElement, domainType.getMetadataAttribs() );
        }
    }

    /**
     * Appends the given metadata attributes to the given root. If either one of the params is <code>null</code>
     * nothing is done.
     *
     * @param root
     *            to append too.
     * @param metadatasAttribs
     *            to append. a list of &lt;xlink:href, about&gt; pairs.
     */
    protected void appendMetadataAttribs( Element root, List<Metadata> metadatasAttribs ) {
        if ( root != null && metadatasAttribs != null ) {
            for ( Metadata metadataElement : metadatasAttribs ) {
                if ( metadataElement != null ) {
                    Element metadata = appendElement( root, OWSNS_1_1_0, PRE + "Metadata" );
                    if ( metadataElement.getMetadataHref() != null ) {
                        metadata.setAttributeNS( XLNNS.toASCIIString(), XLINK_PREFIX + ":href",
                                                 metadataElement.getMetadataHref() );
                    }
                    if ( metadataElement.getMetadataAbout() != null ) {
                        metadata.setAttribute( "about", metadataElement.getMetadataAbout() );
                    }
                    Element abst = metadataElement.getAbstractElement();
                    if ( abst != null ) {
                        Node n = metadata.getOwnerDocument().importNode( abst, true );
                        metadata.appendChild( n );
                    }
                }
            }
        }
    }

    /**
     * Creates an ows 1.1.0 xml-Representation of the given ExceptionReport.
     *
     * @param exception
     *            containing the exceptions.
     * @return a new ows_1_1_0:ExceptionReport document
     */
    public static XMLFragment exportException( OGCWebServiceException exception ) {

        Document doc = XMLTools.create();
        Element root = doc.createElementNS( OWSNS_1_1_0.toASCIIString(), PRE + "ExceptionReport" );
        root.setAttribute( "version", "1.1.0" );
        XMLFragment result = new XMLFragment( root );
        appendException( root, exception );

        return result;

    }

    /**
     * Appends an xml representation of an <code>OGCWebServiceException</code> to the given <code>Element</code>.
     * If either one is <code>null</code> this method just returns.
     *
     * @param root
     *            the Element to append the exceptions to.
     * @param exception
     *            the Exception to append
     */
    protected static void appendException( Element root, OGCWebServiceException exception ) {
        if ( root == null || exception == null ) {
            return;
        }
        Element exceptionNode = XMLTools.appendElement( root, OWSNS_1_1_0, PRE + "Exception" );

        String exceptionCode = ExceptionCode.NOAPPLICABLECODE.value;
        if ( exception.getCode() != null && exception.getCode().value != null
             && !"".equals( exception.getCode().value.trim() ) ) {
            exceptionCode = exception.getCode().value;
        }
        exceptionNode.setAttribute( "exceptionCode", exceptionCode );

        String exceptionMessage = exception.getMessage();
        if ( exceptionMessage != null && !"".equals( exceptionMessage.trim() ) ) {
            appendElement( exceptionNode, OWSNS, PRE + "ExceptionText", exceptionMessage.trim() );
        }

        if ( exception.getLocator() != null && !"unknown".equalsIgnoreCase( exception.getLocator().trim() ) ) {
            try {
                String locator = URLEncoder.encode( exception.getLocator(), CharsetUtils.getSystemCharset() );
                exceptionNode.setAttribute( "locator", locator );
            } catch ( UnsupportedEncodingException e ) {
                // nottin
            }
        }
    }

}
