//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-services/deegree-services-commons/src/main/java/org/deegree/services/controller/ows/capabilities/OWSOperation.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.services.ows.capabilities;

import static org.deegree.commons.xml.stax.XMLStreamUtils.copy;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.domain.AllowedValues;
import org.deegree.commons.ows.metadata.domain.AnyValue;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.domain.NoValues;
import org.deegree.commons.ows.metadata.domain.PossibleValues;
import org.deegree.commons.ows.metadata.domain.Range;
import org.deegree.commons.ows.metadata.domain.Value;
import org.deegree.commons.ows.metadata.domain.Values;
import org.deegree.commons.ows.metadata.domain.ValuesReference;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.ows.metadata.party.Address;
import org.deegree.commons.ows.metadata.party.ContactInfo;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.protocol.ows.OWSCommonXMLAdapter;
import org.deegree.services.jaxb.controller.DCPType;
import org.deegree.services.jaxb.metadata.AddressType;
import org.deegree.services.jaxb.metadata.CodeType;
import org.deegree.services.jaxb.metadata.KeywordsType;
import org.deegree.services.jaxb.metadata.LanguageStringType;
import org.deegree.services.jaxb.metadata.ServiceContactType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for exporting
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class OWSCapabilitiesXMLAdapter extends OWSCommonXMLAdapter {

    private final static Logger LOG = LoggerFactory.getLogger( OWSCapabilitiesXMLAdapter.class );

    /**
     * Exports the given {@link ServiceIdentificationType} as an OWS 1.0.0 <code>ServiceIdentification</code> element.
     * 
     * @param writer
     *            used to append the XML, must not be <code>null</code>
     * @param serviceIdentification
     *            configuration object that provides most of the required metadata, must not be <code>null</code>
     * @param serviceName
     *            OGC-style abbreviation of the service, e.g. WFS, must not be <code>null</code>
     * @param serviceVersions
     *            supported protocol versions, must not be <code>null</code> and contain at least one entry
     * @throws XMLStreamException
     *             if writing the XML fails
     */
    public static void exportServiceIdentification100( XMLStreamWriter writer,
                                                       ServiceIdentification serviceIdentification,
                                                       final String serviceName, final List<Version> serviceVersions )
                            throws XMLStreamException {

        writer.writeStartElement( OWS_PREFIX, "ServiceIdentification", OWS_NS );
        if ( serviceIdentification.getTitle( null ) != null ) {
            // schema has maxOccurs=1, so only export the first entry
            writeElement( writer, OWS_NS, "Title", serviceIdentification.getTitle( null ).getString() );
        }
        if ( serviceIdentification.getAbstract( null ) != null ) {
            // schema has maxOccurs=1, so only export the first entry
            writeElement( writer, OWS_NS, "Abstract", serviceIdentification.getAbstract( null ).getString() );
        }
        if ( serviceIdentification.getKeywords() != null ) {
            for ( Pair<List<LanguageString>, org.deegree.commons.tom.ows.CodeType> keywords : serviceIdentification.getKeywords() ) {
                writer.writeStartElement( OWS_PREFIX, "Keywords", OWS_NS );
                for ( LanguageString keyword : keywords.first ) {
                    writeElement( writer, OWS_NS, "Keyword", keyword.getString() );
                }
                if ( keywords.second != null ) {
                    exportCodeTypeNew( writer, keywords.second, "Type", OWS_NS );
                }
                writer.writeEndElement();
            }
        }

        writeElement( writer, OWS_NS, "ServiceType", serviceName );
        exportVersions( writer, serviceVersions, OWS_NS, "ServiceTypeVersion" );

        if ( serviceIdentification.getFees() != null ) {
            writeElement( writer, OWS_NS, "Fees", serviceIdentification.getFees() );
        }

        exportSimpleStrings( writer, serviceIdentification.getAccessConstraints(), OWS_NS, "AccessConstraints" );
        writer.writeEndElement();
    }

    /**
     * Exports the given {@link ServiceIdentificationType} as an OWS 1.1.0 <code>ServiceIdentification</code> element.
     * 
     * @param writer
     *            used to append the XML, must not be <code>null</code>
     * @param serviceID
     *            configuration object that provides most of the required metadata, must not be <code>null</code>
     * @param serviceName
     *            OGC-style abbreviation of the service, e.g. WFS, must not be <code>null</code>
     * @param serviceVersions
     *            supported protocol versions, must not be <code>null</code> and contain at least one entry
     * @throws XMLStreamException
     *             if writing the XML fails
     */
    public static void exportServiceIdentification110New( XMLStreamWriter writer, ServiceIdentification serviceID,
                                                          final String serviceName, final List<Version> serviceVersions )
                            throws XMLStreamException {

        writer.writeStartElement( OWS110_NS, "ServiceIdentification" );
        if ( serviceID.getTitle( null ) != null ) {
            // schema has maxOccurs=1, so only export the first entry
            writeElement( writer, OWS110_NS, "Title", serviceID.getTitle( null ).getString() );
        }
        if ( serviceID.getAbstract( null ) != null ) {
            // schema has maxOccurs=1, so only export the first entry
            writeElement( writer, OWS110_NS, "Abstract", serviceID.getAbstract( null ).getString() );
        }
        exportKeyWords110New( writer, serviceID.getKeywords() );

        String srvn = serviceName;
        if ( serviceName == null || "".equals( serviceName ) ) {
            LOG.warn( "Service name may not be null, wrong call to exportServiceIdentification110, setting to unknown" );
            srvn = "unknown";
        }
        writeElement( writer, OWS110_NS, "ServiceType", srvn, null, null, "codeSpace", "http://www.opengeospatial.org/" );
        List<Version> versions = serviceVersions;
        if ( serviceVersions == null || serviceVersions.isEmpty() ) {
            LOG.warn( "Service versions name may not be null, wrong call to exportServiceIdentification110, setting to unknown" );
            versions = new ArrayList<Version>();
            versions.add( new Version( 1, 0, 0 ) );
        }

        exportVersions( writer, versions, OWS110_NS, "ServiceTypeVersion" );

        // No support for profiles ???
        if ( serviceID.getFees() != null && !"".equals( serviceID.getFees() ) ) {
            writeElement( writer, OWS110_NS, "Fees", serviceID.getFees() );
        }

        exportSimpleStrings( writer, serviceID.getAccessConstraints(), OWS110_NS, "AccessConstraints" );
        writer.writeEndElement();// OWS110_NS, ServiceIdentification
    }

    /**
     * Exports the given {@link ServiceIdentificationType} as an OWS 1.1.0 <code>ServiceIdentification</code> element.
     * 
     * @param writer
     *            used to append the XML, must not be <code>null</code>
     * @param serviceID
     *            configuration object that provides most of the required metadata, must not be <code>null</code>
     * @param serviceName
     *            OGC-style abbreviation of the service, e.g. WFS, must not be <code>null</code>
     * @param serviceVersions
     *            supported protocol versions, must not be <code>null</code> and contain at least one entry
     * @throws XMLStreamException
     *             if writing the XML fails
     */
    public static void exportServiceIdentification110( XMLStreamWriter writer, ServiceIdentificationType serviceID,
                                                       final String serviceName, final List<Version> serviceVersions )
                            throws XMLStreamException {

        writer.writeStartElement( OWS110_NS, "ServiceIdentification" );
        exportSimpleStrings( writer, serviceID.getTitle(), OWS110_NS, "Title" );
        exportSimpleStrings( writer, serviceID.getAbstract(), OWS110_NS, "Abstract" );
        exportKeyWords110( writer, serviceID.getKeywords() );

        String srvn = serviceName;
        if ( serviceName == null || "".equals( serviceName ) ) {
            LOG.warn( "Service name may not be null, wrong call to exportServiceIdentification110, setting to unknown" );
            srvn = "unknown";
        }
        writeElement( writer, OWS110_NS, "ServiceType", srvn );
        List<Version> versions = serviceVersions;
        if ( serviceVersions == null || serviceVersions.isEmpty() ) {
            LOG.warn( "Service versions name may not be null, wrong call to exportServiceIdentification110, setting to unknown" );
            versions = new ArrayList<Version>();
            versions.add( new Version( 1, 0, 0 ) );
        }

        exportVersions( writer, versions, OWS110_NS, "ServiceTypeVersion" );

        // No support for profiles ???
        if ( serviceID.getFees() != null && !"".equals( serviceID.getFees() ) ) {
            writeElement( writer, OWS110_NS, "Fees", serviceID.getFees() );
        }

        exportSimpleStrings( writer, serviceID.getAccessConstraints(), OWS110_NS, "AccessConstraints" );
        writer.writeEndElement();// OWS110_NS, ServiceIdentification
    }

    /**
     * Exports the given (commons) keywords to ows 1.1.0 format
     * 
     * @param writer
     * @param list
     * @throws XMLStreamException
     */
    public static void exportKeyWords110New( XMLStreamWriter writer,
                                             List<Pair<List<LanguageString>, org.deegree.commons.tom.ows.CodeType>> list )
                            throws XMLStreamException {
        if ( list != null && list.size() > 0 ) {
            for ( Pair<List<LanguageString>, org.deegree.commons.tom.ows.CodeType> keywords : list ) {
                writer.writeStartElement( OWS_PREFIX, "Keywords", OWS110_NS );
                for ( LanguageString keyword : keywords.first ) {
                    writeElement( writer, OWS110_NS, "Keyword", keyword.getString() );
                }
                if ( keywords.second != null ) {
                    exportCodeTypeNew( writer, keywords.second, "Type", OWS110_NS );
                }
                writer.writeEndElement();
            }
        }
    }

    /**
     * Exports the given (commons) keywords to ows 1.1.0 format
     * 
     * @param writer
     * @param keywords
     * @throws XMLStreamException
     */
    public static void exportKeyWords110( XMLStreamWriter writer, List<KeywordsType> keywords )
                            throws XMLStreamException {
        if ( keywords != null && keywords.size() > 0 ) {
            exportKeyWords( writer, keywords, OWS110_NS );
        }
    }

    /**
     * Exports a {@link ServiceProviderType} as an OWS 1.0.0 <code>ServiceProvider</code> element.
     * 
     * @param writer
     *            writer to append the xml
     * @param serviceProvider
     *            <code>ServiceProviderType</code> to export
     * @throws XMLStreamException
     */
    public static void exportServiceProvider100( XMLStreamWriter writer, ServiceProvider serviceProvider )
                            throws XMLStreamException {
        if ( serviceProvider != null ) {
            exportServiceProvider( writer, serviceProvider, OWS_NS );
        }
    }

    /**
     * Exports a {@link ServiceProviderType} as an OWS 1.1.0 <code>ServiceProvider</code> element.
     * 
     * @param writer
     *            writer to append the xml
     * @param serviceProvider
     *            <code>ServiceProviderType</code> to export
     * @throws XMLStreamException
     */
    public static void exportServiceProvider110New( XMLStreamWriter writer, ServiceProvider serviceProvider )
                            throws XMLStreamException {
        if ( serviceProvider != null ) {
            exportServiceProvider( writer, serviceProvider, OWS110_NS );
        }
    }

    /**
     * Exports a {@link ServiceProviderType} as an OWS 1.0.0 <code>ServiceProvider</code> element.
     * 
     * @param writer
     *            writer to append the xml
     * @param serviceProvider
     *            <code>ServiceProviderType</code> to export
     * @throws XMLStreamException
     */
    public static void exportServiceProvider100Old( XMLStreamWriter writer, ServiceProviderType serviceProvider )
                            throws XMLStreamException {
        String owsNS = OWS_NS;
        writer.writeStartElement( OWS_PREFIX, "ServiceProvider", owsNS );

        // ows:ProviderName (type="string")
        writer.writeStartElement( owsNS, "ProviderName" );
        writer.writeCharacters( serviceProvider.getProviderName() );
        writer.writeEndElement();

        if ( serviceProvider.getProviderSite() != null && !"".equals( serviceProvider.getProviderSite().trim() ) ) {
            // ows:ProviderSite (type="ows:OnlineResourceType")
            writer.writeStartElement( owsNS, "ProviderSite" );
            writer.writeAttribute( XLN_NS, "href", serviceProvider.getProviderSite() );
            writer.writeEndElement();
        }

        // ows:ProviderSite (type="ows:ResponsiblePartySubsetType")
        ServiceContactType serviceContact = serviceProvider.getServiceContact();
        writer.writeStartElement( owsNS, "ServiceContact" );

        if ( serviceContact.getIndividualName() != null && !"".equals( serviceContact.getIndividualName().trim() ) ) {
            // ows:IndividualName (type="string")
            writeElement( writer, owsNS, "IndividualName", serviceContact.getIndividualName() );
        }

        if ( serviceContact.getPositionName() != null && !"".equals( serviceContact.getPositionName().trim() ) ) {
            // ows:PositionName (type="string")
            writeElement( writer, owsNS, "PositionName", serviceContact.getPositionName() );
        }

        // ows:ContactInfo
        if ( serviceContact.getPhone() != null || serviceContact.getFacsimile() != null
             || serviceContact.getAddress() != null || serviceContact.getElectronicMailAddress() != null
             || serviceContact.getOnlineResource() != null || serviceContact.getHoursOfService() != null
             || serviceContact.getContactInstructions() != null ) {
            writer.writeStartElement( owsNS, "ContactInfo" );

            // ows:Phone (type="ows:PhoneType")
            if ( serviceContact.getPhone() != null || serviceContact.getFacsimile() != null ) {
                writer.writeStartElement( owsNS, "Phone" );
                // ows:Voice (type="string")
                writeOptionalElement( writer, owsNS, "Voice", serviceContact.getPhone() );
                // ows:Facsimile (type="string")
                writeOptionalElement( writer, owsNS, "Facsimile", serviceContact.getFacsimile() );
                writer.writeEndElement();
            }

            // ows:Address (type="ows:AddressType")
            AddressType address = serviceContact.getAddress();
            if ( address != null ) {
                writer.writeStartElement( owsNS, "Address" );
                exportSimpleStrings( writer, address.getDeliveryPoint(), owsNS, "DeliveryPoint" );
                writeOptionalElement( writer, owsNS, "City", address.getCity() );
                writeOptionalElement( writer, owsNS, "AdministrativeArea", address.getAdministrativeArea() );
                writeOptionalElement( writer, owsNS, "PostalCode", address.getPostalCode() );
                writeOptionalElement( writer, owsNS, "Country", address.getCountry() );
                exportSimpleStrings( writer, serviceContact.getElectronicMailAddress(), owsNS, "ElectronicMailAddress" );
                writer.writeEndElement();
            }

            if ( serviceContact.getOnlineResource() != null && !"".equals( serviceContact.getOnlineResource().trim() ) ) {
                // ows:OnlineResource (type="ows:OnlineResourceType")
                writer.writeStartElement( owsNS, "OnlineResource" );
                writer.writeAttribute( XLN_NS, "href", serviceContact.getOnlineResource() );
                writer.writeEndElement();
            }

            // ows:HoursOfService (type="string")
            writeOptionalElement( writer, owsNS, "HoursOfService", serviceContact.getHoursOfService() );
            // ows:ContactInstructions (type="string")
            writeOptionalElement( writer, owsNS, "ContactInstructions", serviceContact.getContactInstructions() );

            writer.writeEndElement(); // ContactInfo
        }

        // ows:Role (type="ows:CodeType)
        writeElement( writer, owsNS, "Role", serviceContact.getRole() );

        writer.writeEndElement();

        writer.writeEndElement(); // ServiceProvider
    }

    /**
     * Exports a {@link ServiceProviderType} as an OWS 1.1.0 <code>ServiceProvider</code> element. Validated against ows
     * schema by rb at 23.02.2009.
     * 
     * @param writer
     *            writer to append the xml
     * @param serviceProvider
     *            <code>ServiceProviderType</code> to export
     * @throws XMLStreamException
     */
    public static void exportServiceProvider110( XMLStreamWriter writer, ServiceProviderType serviceProvider )
                            throws XMLStreamException {
        String owsNS = OWS110_NS;
        writer.writeStartElement( OWS_PREFIX, "ServiceProvider", owsNS );

        // ows:ProviderName (type="string")
        writer.writeStartElement( owsNS, "ProviderName" );
        writer.writeCharacters( serviceProvider.getProviderName() );
        writer.writeEndElement();

        if ( serviceProvider.getProviderSite() != null && !"".equals( serviceProvider.getProviderSite().trim() ) ) {
            // ows:ProviderSite (type="ows:OnlineResourceType")
            writer.writeStartElement( owsNS, "ProviderSite" );
            writer.writeAttribute( XLN_NS, "href", serviceProvider.getProviderSite() );
            writer.writeEndElement();
        }

        // ows:ProviderSite (type="ows:ResponsiblePartySubsetType")
        ServiceContactType serviceContact = serviceProvider.getServiceContact();
        writer.writeStartElement( owsNS, "ServiceContact" );

        if ( serviceContact.getIndividualName() != null && !"".equals( serviceContact.getIndividualName().trim() ) ) {
            // ows:IndividualName (type="string")
            writeElement( writer, owsNS, "IndividualName", serviceContact.getIndividualName() );
        }

        if ( serviceContact.getPositionName() != null && !"".equals( serviceContact.getPositionName().trim() ) ) {
            // ows:PositionName (type="string")
            writeElement( writer, owsNS, "PositionName", serviceContact.getPositionName() );
        }

        // ows:ContactInfo
        if ( serviceContact.getPhone() != null || serviceContact.getFacsimile() != null
             || serviceContact.getAddress() != null || serviceContact.getElectronicMailAddress() != null
             || serviceContact.getOnlineResource() != null || serviceContact.getHoursOfService() != null
             || serviceContact.getContactInstructions() != null ) {
            writer.writeStartElement( owsNS, "ContactInfo" );

            // ows:Phone (type="ows:PhoneType")
            if ( serviceContact.getPhone() != null || serviceContact.getFacsimile() != null ) {
                writer.writeStartElement( owsNS, "Phone" );
                // ows:Voice (type="string")
                writeOptionalElement( writer, owsNS, "Voice", serviceContact.getPhone() );
                // ows:Facsimile (type="string")
                writeOptionalElement( writer, owsNS, "Facsimile", serviceContact.getFacsimile() );
                writer.writeEndElement();
            }

            // ows:Address (type="ows:AddressType")
            AddressType address = serviceContact.getAddress();
            if ( address != null ) {
                writer.writeStartElement( owsNS, "Address" );
                exportSimpleStrings( writer, address.getDeliveryPoint(), owsNS, "DeliveryPoint" );
                writeOptionalElement( writer, owsNS, "City", address.getCity() );
                writeOptionalElement( writer, owsNS, "AdministrativeArea", address.getAdministrativeArea() );
                writeOptionalElement( writer, owsNS, "PostalCode", address.getPostalCode() );
                writeOptionalElement( writer, owsNS, "Country", address.getCountry() );
                exportSimpleStrings( writer, serviceContact.getElectronicMailAddress(), owsNS, "ElectronicMailAddress" );
                writer.writeEndElement();
            }

            if ( serviceContact.getOnlineResource() != null && !"".equals( serviceContact.getOnlineResource().trim() ) ) {
                // ows:OnlineResource (type="ows:OnlineResourceType")
                writer.writeStartElement( owsNS, "OnlineResource" );
                writer.writeAttribute( XLN_NS, "href", serviceContact.getOnlineResource() );
                writer.writeEndElement();
            }

            // ows:HoursOfService (type="string")
            writeOptionalElement( writer, owsNS, "HoursOfService", serviceContact.getHoursOfService() );
            // ows:ContactInstructions (type="string")
            writeOptionalElement( writer, owsNS, "ContactInstructions", serviceContact.getContactInstructions() );

            writer.writeEndElement(); // ContactInfo
        }

        // ows:Role (type="ows:CodeType)
        writeElement( writer, owsNS, "Role", serviceContact.getRole() );

        writer.writeEndElement();

        writer.writeEndElement(); // ServiceProvider
    }

    /**
     * Exports the {@link OperationsMetadata} as an OWS 1.0.0 <code>OperationsMetadata</code> element.
     * 
     * @param writer
     *            writer to append the xml, must not be <code>null</code>
     * @param operationsMd
     *            operations metadata, must not be <code>null</code>
     * @throws XMLStreamException
     */
    public static void exportOperationsMetadata100( XMLStreamWriter writer, OperationsMetadata operationsMd )
                            throws XMLStreamException {

        writer.writeStartElement( OWS_NS, "OperationsMetadata" );

        for ( Operation operation : operationsMd.getOperation() ) {

            // ows:Operation
            writer.writeStartElement( OWS_NS, "Operation" );
            writer.writeAttribute( "name", operation.getName() );

            // <element ref="ows:DCP" maxOccurs="unbounded">
            for ( DCP dcp : operation.getDCPs() ) {
                exportDCP( writer, dcp, OWS_NS );
            }

            // <element name="Parameter" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
            for ( Domain param : operation.getParameters() ) {
                writer.writeStartElement( OWS_NS, "Parameter" );
                exportDomainType100( writer, param );
                writer.writeEndElement();
            }

            // <element name="Constraint" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
            for ( Domain constraint : operation.getConstraints() ) {
                writer.writeStartElement( OWS_NS, "Constraint" );
                exportDomainType100( writer, constraint );
                writer.writeEndElement();
            }

            // <element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded">
            for ( OMElement md : operation.getMetadata() ) {
                copy( writer, md.getXMLStreamReader() );
            }

            writer.writeEndElement();
        }

        // <element name="Parameter" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
        for ( Domain param : operationsMd.getParameters() ) {
            writer.writeStartElement( OWS_NS, "Parameter" );
            exportDomainType100( writer, param );
            writer.writeEndElement();
        }

        // <element name="Constraint" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
        for ( Domain constraint : operationsMd.getConstraints() ) {
            writer.writeStartElement( OWS_NS, "Constraint" );
            exportDomainType100( writer, constraint );
            writer.writeEndElement();
        }

        // <element ref="ows:ExtendedCapabilities" minOccurs="0"/>
        exportExtendedCapabilities( writer, new QName( OWS_NS, "ExtendedCapabilities" ), operationsMd );

        writer.writeEndElement();
    }

    /**
     * Exports an {@link OperationsMetadata} instance as an OWS 1.1.0 <code>OperationsMetadata</code> element.
     * 
     * @param writer
     *            writer to append the xml, must not be <code>null</code>
     * @param operationsMd
     *            operations metadata, must not be <code>null</code>
     * @throws XMLStreamException
     */
    public static void exportOperationsMetadata110( XMLStreamWriter writer, OperationsMetadata operationsMd )
                            throws XMLStreamException {

        writer.writeStartElement( OWS110_NS, "OperationsMetadata" );

        for ( Operation operation : operationsMd.getOperation() ) {

            // ows:Operation
            writer.writeStartElement( OWS110_NS, "Operation" );
            writer.writeAttribute( "name", operation.getName() );

            // <element ref="ows:DCP" maxOccurs="unbounded">
            for ( DCP dcp : operation.getDCPs() ) {
                exportDCP( writer, dcp, OWS110_NS );
            }

            // <element name="Parameter" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
            for ( Domain param : operation.getParameters() ) {
                writer.writeStartElement( OWS110_NS, "Parameter" );
                exportDomainType110( writer, param );
                writer.writeEndElement();
            }

            // <element name="Constraint" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
            for ( Domain constraint : operation.getConstraints() ) {
                writer.writeStartElement( OWS110_NS, "Constraint" );
                exportDomainType110( writer, constraint );
                writer.writeEndElement();
            }

            // <element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded">
            for ( OMElement md : operation.getMetadata() ) {
                copy( writer, md.getXMLStreamReader() );
            }

            writer.writeEndElement();
        }

        // <element name="Parameter" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
        for ( Domain param : operationsMd.getParameters() ) {
            writer.writeStartElement( OWS110_NS, "Parameter" );
            exportDomainType110( writer, param );
            writer.writeEndElement();
        }

        // <element name="Constraint" type="ows:DomainType" minOccurs="0" maxOccurs="unbounded">
        for ( Domain constraint : operationsMd.getConstraints() ) {
            writer.writeStartElement( OWS110_NS, "Constraint" );
            exportDomainType110( writer, constraint );
            writer.writeEndElement();
        }

        // <element ref="ows:ExtendedCapabilities" minOccurs="0"/>
        exportExtendedCapabilities( writer, new QName( OWS110_NS, "ExtendedCapabilities" ), operationsMd );

        writer.writeEndElement();
    }

    /**
     * Exports a {@link ServiceProvider} as an OWS <code>ServiceProvider</code> element.
     * <p>
     * The namespace of the produced elements is given as a parameter so it is usable for different OWS versions. It has
     * been checked that this method produces the correct output for the following OWS versions/namespaces:
     * </p>
     * <table border="1">
     * <tr>
     * <th>OWS version</th>
     * <th>OWS namespace</th>
     * </tr>
     * <tr>
     * <td><center>1.0.0</center></td>
     * <td><center>http://www.opengis.net/ows</center></td>
     * </tr>
     * <tr>
     * <td><center>1.1.0</center></td>
     * <td><center>http://www.opengis.net/ows/1.1</center></td>
     * </tr>
     * </table>
     * 
     * @param writer
     *            writer to append the xml
     * @param serviceProvider
     *            metadata to export
     * @param owsNS
     *            namespace for the generated elements
     * @throws XMLStreamException
     */
    private static void exportServiceProvider( XMLStreamWriter writer, ServiceProvider serviceProvider, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( OWS_PREFIX, "ServiceProvider", owsNS );

        // ows:ProviderName (type="string")
        if ( serviceProvider.getProviderName() != null ) {
            writer.writeStartElement( owsNS, "ProviderName" );
            writer.writeCharacters( serviceProvider.getProviderName() );
            writer.writeEndElement();
        }

        if ( serviceProvider.getProviderSite() != null && !"".equals( serviceProvider.getProviderSite().trim() ) ) {
            // ows:ProviderSite (type="ows:OnlineResourceType")
            writer.writeStartElement( owsNS, "ProviderSite" );
            writer.writeAttribute( XLN_NS, "href", serviceProvider.getProviderSite() );
            writer.writeEndElement();
        }

        // ows:ProviderSite (type="ows:ResponsiblePartySubsetType")
        if ( serviceProvider.getServiceContact() != null ) {
            exportServiceContact( writer, serviceProvider.getServiceContact(), owsNS );
        }

        writer.writeEndElement(); // ServiceProvider
    }

    /**
     * @param writer
     * @param versions
     * @param owsNS
     * @throws XMLStreamException
     */
    private static void exportVersions( XMLStreamWriter writer, List<Version> versions, String owsNS,
                                        String versionElementName )
                            throws XMLStreamException {
        if ( versions != null && !versions.isEmpty() ) {
            for ( Version version : versions ) {
                writeElement( writer, owsNS, versionElementName, version.toString() );
            }
        }

    }

    /**
     * Exports a {@link ResponsibleParty} as an OWS <code>ServiceContact</code> element.
     * <p>
     * The namespace of the produced elements is given as a parameter so it is usable for different OWS versions. It has
     * been checked that this method produces the correct output for the following OWS versions/namespaces:
     * </p>
     * <table border="1">
     * <tr>
     * <th>OWS version</th>
     * <th>OWS namespace</th>
     * </tr>
     * <tr>
     * <td><center>1.0.0</center></td>
     * <td><center>http://www.opengis.net/ows</center></td>
     * </tr>
     * <tr>
     * <td><center>1.1.0</center></td>
     * <td><center>http://www.opengis.net/ows/1.1</center></td>
     * </tr>
     * </table>
     * 
     * @param writer
     *            writer to append the xml
     * @param serviceContact
     *            <code>ServiceContactType</code> to export
     * @param owsNS
     *            namespace for the generated elements
     * @throws XMLStreamException
     */
    private static void exportServiceContact( XMLStreamWriter writer, ResponsibleParty serviceContact, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "ServiceContact" );

        if ( serviceContact.getIndividualName() != null && !"".equals( serviceContact.getIndividualName().trim() ) ) {
            // ows:IndividualName (type="string")
            writeElement( writer, owsNS, "IndividualName", serviceContact.getIndividualName() );
        }

        if ( serviceContact.getPositionName() != null && !"".equals( serviceContact.getPositionName().trim() ) ) {
            // ows:PositionName (type="string")
            writeElement( writer, owsNS, "PositionName", serviceContact.getPositionName() );
        }

        // ows:ContactInfo
        exportContactInfo( writer, serviceContact, owsNS );

        // ows:Role (type="ows:CodeType)
        if ( serviceContact.getRole() != null ) {
            writeElement( writer, owsNS, "Role", serviceContact.getRole().getCode() );
        }

        writer.writeEndElement();
    }

    /**
     * Exports a {@link ServiceContactType} as an OWS <code>ContactInfo</code> element.
     * <p>
     * The namespace of the produced elements is given as a parameter so it is usable for different OWS versions. It has
     * been checked that this method produces the correct output for the following OWS versions/namespaces:
     * </p>
     * <table border="1">
     * <tr>
     * <th>OWS version</th>
     * <th>OWS namespace</th>
     * </tr>
     * <tr>
     * <td><center>1.0.0</center></td>
     * <td><center>http://www.opengis.net/ows</center></td>
     * </tr>
     * <tr>
     * <td><center>1.1.0</center></td>
     * <td><center>http://www.opengis.net/ows/1.1</center></td>
     * </tr>
     * </table>
     * 
     * @param writer
     *            writer to append the xml
     * @param party
     *            <code>ResponsibleParty</code> to export
     * @param owsNS
     *            namespace for the generated elements
     * @throws XMLStreamException
     */
    private static void exportContactInfo( XMLStreamWriter writer, ResponsibleParty party, String owsNS )
                            throws XMLStreamException {

        ContactInfo serviceContact = party.getContactInfo();
        if ( serviceContact != null ) {
            writer.writeStartElement( owsNS, "ContactInfo" );

            // ows:Phone (type="ows:PhoneType")
            if ( serviceContact.getPhone() != null ) {
                writer.writeStartElement( owsNS, "Phone" );
                if ( !serviceContact.getPhone().getVoice().isEmpty() ) {
                    // ows:Voice (type="string")
                    writeOptionalElement( writer, owsNS, "Voice", serviceContact.getPhone().getVoice().get( 0 ) );
                }
                if ( !serviceContact.getPhone().getFacsimile().isEmpty() ) {
                    // ows:Facsimile (type="string")
                    writeOptionalElement( writer, owsNS, "Facsimile", serviceContact.getPhone().getFacsimile().get( 0 ) );
                }
                writer.writeEndElement();
            }

            // ows:Address (type="ows:AddressType")
            Address address = serviceContact.getAddress();
            if ( address != null ) {
                writer.writeStartElement( owsNS, "Address" );
                exportSimpleStrings( writer, address.getDeliveryPoint(), owsNS, "DeliveryPoint" );
                writeOptionalElement( writer, owsNS, "City", address.getCity() );
                writeOptionalElement( writer, owsNS, "AdministrativeArea", address.getAdministrativeArea() );
                writeOptionalElement( writer, owsNS, "PostalCode", address.getPostalCode() );
                writeOptionalElement( writer, owsNS, "Country", address.getCountry() );
                exportSimpleStrings( writer, address.getElectronicMailAddress(), owsNS, "ElectronicMailAddress" );
                writer.writeEndElement();
            }

            if ( serviceContact.getOnlineResource() != null
                 && !"".equals( serviceContact.getOnlineResource().toString().trim() ) ) {
                // ows:OnlineResource (type="ows:OnlineResourceType")
                writer.writeStartElement( owsNS, "OnlineResource" );
                writer.writeAttribute( XLN_NS, "href", serviceContact.getOnlineResource().toString().trim() );
                writer.writeEndElement();
            }

            // ows:HoursOfService (type="string")
            writeOptionalElement( writer, owsNS, "HoursOfService", serviceContact.getHoursOfService() );
            // ows:ContactInstructions (type="string")
            writeOptionalElement( writer, owsNS, "ContactInstructions", serviceContact.getContactInstruction() );

            writer.writeEndElement(); // ContactInfo
        }
    }

    /**
     * Exports a {@link DCPType} as an OWS <code>DCP</code> element.
     * <p>
     * The namespace of the produced elements is given as a parameter so it is usable for different OWS versions. It has
     * been checked that this method produces the correct output for the following OWS versions/namespaces:
     * </p>
     * <table border="1">
     * <tr>
     * <th>OWS version</th>
     * <th>OWS namespace</th>
     * </tr>
     * <tr>
     * <td><center>1.0.0</center></td>
     * <td><center>http://www.opengis.net/ows</center></td>
     * </tr>
     * <tr>
     * <td><center>1.1.0</center></td>
     * <td><center>http://www.opengis.net/ows/1.1</center></td>
     * </tr>
     * </table>
     * 
     * @param writer
     *            writer to append the xml
     * @param dcp
     *            <code>DCPType</code> to export
     * @param owsNS
     *            namespace for the generated elements
     * @throws XMLStreamException
     */
    public static void exportDCP( XMLStreamWriter writer, DCP dcp, String owsNS )
                            throws XMLStreamException {

        writer.writeStartElement( owsNS, "DCP" );
        writer.writeStartElement( owsNS, "HTTP" );

        // ows:Get (type="ows:RequestMethodType")
        if ( dcp.getGetEndpoints() != null ) {
            for ( Pair<URL, List<Domain>> getEndpoint : dcp.getGetEndpoints() ) {
                writer.writeEmptyElement( owsNS, "Get" );
                writer.writeAttribute( XLN_NS, "href", getEndpoint.first.toString() );
                // TODO: constraints
            }
        }

        // ows:Post (type="ows:RequestMethodType")
        if ( dcp.getPostEndpoints() != null ) {
            for ( Pair<URL, List<Domain>> postEndpoint : dcp.getPostEndpoints() ) {
                writer.writeEmptyElement( owsNS, "Post" );
                writer.writeAttribute( XLN_NS, "href", postEndpoint.first.toString() );
                // TODO: constraints
            }
        }

        writer.writeEndElement(); // HTTP
        writer.writeEndElement(); // DCP
    }

    public static void exportDCP( XMLStreamWriter writer, String get, String post, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "DCP" );
        writer.writeStartElement( owsNS, "HTTP" );
        writer.writeEmptyElement( owsNS, "Get" );
        writer.writeAttribute( XLN_NS, "href", get );
        writer.writeEmptyElement( owsNS, "Post" );
        writer.writeAttribute( XLN_NS, "href", post );
        writer.writeEndElement(); // HTTP
        writer.writeEndElement(); // DCP
    }

    private static void exportKeyWords( XMLStreamWriter writer, List<KeywordsType> keywords, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "Keywords" );
        for ( KeywordsType kwt : keywords ) {
            if ( kwt != null ) {
                List<LanguageStringType> keyword = kwt.getKeyword();
                // must actually be >0
                if ( keyword != null && !keyword.isEmpty() ) {
                    for ( LanguageStringType lst : keyword ) {
                        exportLanguageStringType( writer, lst, "Keyword", owsNS );
                    }
                }
                CodeType codeType = kwt.getType();
                exportCodeType( writer, codeType, "Type", owsNS );
            }
        }
        writer.writeEndElement(); // Keywords
    }

    private static void exportCodeTypeNew( XMLStreamWriter writer, org.deegree.commons.tom.ows.CodeType codeType,
                                           String localName, String owsNS )
                            throws XMLStreamException {
        if ( codeType != null ) {
            writer.writeStartElement( owsNS, localName );
            if ( codeType.getCodeSpace() != null && !"".equals( codeType.getCodeSpace() ) ) {
                writer.writeAttribute( "codeSpace", codeType.getCodeSpace() );
            }
            writer.writeCharacters( codeType.getCode() );
            writer.writeEndElement(); // localName
        }

    }

    /**
     * @param writer
     * @param codeType
     * @param owsNS
     * @throws XMLStreamException
     */
    private static void exportCodeType( XMLStreamWriter writer, CodeType codeType, String localName, String owsNS )
                            throws XMLStreamException {
        if ( codeType != null ) {
            writer.writeStartElement( owsNS, localName );
            if ( codeType.getCodeSpace() != null && !"".equals( codeType.getCodeSpace() ) ) {
                writer.writeAttribute( "codeSpace", codeType.getCodeSpace() );
            }
            writer.writeCharacters( codeType.getValue() );
            writer.writeEndElement(); // localName
        }

    }

    private static void exportLanguageStringType( XMLStreamWriter writer, LanguageStringType lst, String localName,
                                                  String owsNS )
                            throws XMLStreamException {

        if ( lst != null ) {
            writer.writeStartElement( owsNS, localName );
            if ( lst.getLang() != null && !"".equals( lst.getLang() ) ) {
                writer.writeAttribute( "xml:lang", lst.getLang() );
            }
            writer.writeCharacters( lst.getValue() );
            writer.writeEndElement(); // Keyword
        }
    }

    /**
     * Write a list of strings to the given namespace and with the given element name.
     * 
     * @param writer
     * @param strings
     *            to export
     * @param owsNS
     *            the name space to use
     * @param elementName
     *            to use
     * @throws XMLStreamException
     */
    public static void exportSimpleStrings( XMLStreamWriter writer, List<String> strings, String owsNS,
                                            String elementName )
                            throws XMLStreamException {
        if ( strings != null && strings.size() > 0 ) {
            for ( String t : strings ) {
                writeElement( writer, owsNS, elementName, t );
            }
        }
    }

    public static void exportDomainType100( XMLStreamWriter writer, Domain domain )
                            throws XMLStreamException {

        if ( domain.getName() != null ) {
            writer.writeAttribute( "name", domain.getName() );
        }

        PossibleValues values = domain.getPossibleValues();
        if ( values instanceof AllowedValues ) {
            for ( Values value : ( (AllowedValues) values ).getValues() ) {
                if ( value instanceof Value ) {
                    writer.writeStartElement( OWS_NS, "Value" );
                    writer.writeCharacters( ( (Value) value ).getValue() );
                    writer.writeEndElement();
                } else if ( value instanceof Range ) {
                    throw new IllegalArgumentException( "Ranges are not allowed in OWS 1.0.0 domains." );
                }
            }
        } else if ( values instanceof AnyValue ) {
            throw new IllegalArgumentException( "AnyValue is not allowed in OWS 1.0.0 domains." );
        } else if ( values instanceof NoValues ) {
            throw new IllegalArgumentException( "NoValues is not allowed in OWS 1.0.0 domains." );
        } else if ( values instanceof ValuesReference ) {
            throw new IllegalArgumentException( "ValuesReference is not allowed in OWS 1.0.0 domains." );
        }

        if ( domain.getDefaultValue() != null ) {
            writer.writeStartElement( OWS110_NS, "DefaultValue" );
            writer.writeCharacters( domain.getDefaultValue() );
            writer.writeEndElement();
        }

        if ( domain.getMetadata() != null ) {
            for ( OMElement metadataEl : domain.getMetadata() ) {
                copy( writer, metadataEl.getXMLStreamReader() );
            }
        }
    }

    public static void exportDomainType110( XMLStreamWriter writer, Domain domain )
                            throws XMLStreamException {

        if ( domain.getName() != null ) {
            writer.writeAttribute( "name", domain.getName() );
        }

        PossibleValues values = domain.getPossibleValues();
        if ( values instanceof AllowedValues ) {
            writer.writeStartElement( OWS110_NS, "AllowedValues" );
            for ( Values value : ( (AllowedValues) values ).getValues() ) {
                if ( value instanceof Value ) {
                    writer.writeStartElement( OWS110_NS, "Value" );
                    writer.writeCharacters( ( (Value) value ).getValue() );
                    writer.writeEndElement();
                } else if ( value instanceof Range ) {
                    Range range = (Range) value;
                    writer.writeStartElement( OWS110_NS, "Range" );
                    if ( range.getClosure() != null ) {
                        switch ( range.getClosure() ) {
                        case CLOSED:
                            writer.writeAttribute( "rangeClosure", "closed" );
                            break;
                        case CLOSED_OPEN:
                            writer.writeAttribute( "rangeClosure", "closed-open" );
                            break;
                        case OPEN:
                            writer.writeAttribute( "rangeClosure", "open" );
                            break;
                        case OPEN_CLOSED:
                            writer.writeAttribute( "rangeClosure", "open-closed" );
                            break;
                        }
                    }
                    if ( range.getMin() != null ) {
                        writer.writeStartElement( OWS110_NS, "MinimumValue" );
                        writer.writeCharacters( range.getMin() );
                        writer.writeEndElement();
                    }
                    if ( range.getMax() != null ) {
                        writer.writeStartElement( OWS110_NS, "MaximumValue" );
                        writer.writeCharacters( range.getMax() );
                        writer.writeEndElement();
                    }
                    if ( range.getSpacing() != null ) {
                        writer.writeStartElement( OWS110_NS, "Spacing" );
                        writer.writeCharacters( range.getSpacing() );
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        } else if ( values instanceof AnyValue ) {
            writer.writeEmptyElement( OWS110_NS, "AnyValue" );
        } else if ( values instanceof NoValues ) {
            writer.writeEmptyElement( OWS110_NS, "NoValues" );
        } else if ( values instanceof ValuesReference ) {
            ValuesReference valRef = (ValuesReference) values;
            writer.writeStartElement( OWS110_NS, "ValuesReference" );
            if ( valRef.getRef() != null ) {
                writer.writeAttribute( OWS110_NS, "reference", valRef.getRef() );
            }
            writer.writeCharacters( valRef.getName() );
            writer.writeEndElement();
        }

        if ( domain.getDefaultValue() != null ) {
            writer.writeStartElement( OWS110_NS, "DefaultValue" );
            writer.writeCharacters( domain.getDefaultValue() );
            writer.writeEndElement();
        }

        if ( domain.getMeaning() != null ) {
            writer.writeStartElement( OWS110_NS, "Meaning" );
            if ( domain.getMeaning().getRef() != null ) {
                writer.writeAttribute( OWS110_NS, "reference", domain.getMeaning().getRef() );
            }
            writer.writeCharacters( domain.getMeaning().getString() );
            writer.writeEndElement();
        }

        if ( domain.getDataType() != null ) {
            writer.writeStartElement( OWS110_NS, "DataType" );
            if ( domain.getDataType().getRef() != null ) {
                writer.writeAttribute( OWS110_NS, "reference", domain.getDataType().getRef() );
            }
            writer.writeCharacters( domain.getDataType().getString() );
            writer.writeEndElement();
        }

        if ( domain.getValuesUnitUom() != null ) {
            writer.writeStartElement( OWS110_NS, "UOM" );
            if ( domain.getValuesUnitUom().getRef() != null ) {
                writer.writeAttribute( OWS110_NS, "reference", domain.getValuesUnitUom().getRef() );
            }
            writer.writeCharacters( domain.getValuesUnitUom().getString() );
            writer.writeEndElement();
        } else if ( domain.getValuesUnitRefSys() != null ) {
            writer.writeStartElement( OWS110_NS, "ReferenceSystem" );
            if ( domain.getValuesUnitRefSys().getRef() != null ) {
                writer.writeAttribute( OWS110_NS, "reference", domain.getValuesUnitRefSys().getRef() );
            }
            writer.writeCharacters( domain.getValuesUnitRefSys().getString() );
            writer.writeEndElement();
        }

        if ( domain.getMetadata() != null ) {
            for ( OMElement metadataEl : domain.getMetadata() ) {
                copy( writer, metadataEl.getXMLStreamReader() );
            }
        }
    }

    private static void exportExtendedCapabilities( XMLStreamWriter writer, QName extendedCapabilities,
                                                    OperationsMetadata operationsMd )
                            throws XMLStreamException {
        if ( !operationsMd.getExtendedCapabilities().isEmpty() ) {
            OMElement extendedCapabilitiesToExport = operationsMd.getExtendedCapabilities().get( 0 );
            boolean isOwsExtCapabilities = extendedCapabilities.equals( extendedCapabilitiesToExport.getQName() );
            if ( !isOwsExtCapabilities ) {
                writer.writeStartElement( extendedCapabilities.getNamespaceURI(), extendedCapabilities.getLocalPart() );
            }
            copy( writer, extendedCapabilitiesToExport.getXMLStreamReader() );
            if ( !isOwsExtCapabilities ) {
                writer.writeEndElement();
            }
        }
    }

}
