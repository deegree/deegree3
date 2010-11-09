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
package org.deegree.services.controller.ows.capabilities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.protocol.ows.OWSCommonXMLAdapter;
import org.deegree.services.jaxb.main.AddressType;
import org.deegree.services.jaxb.main.CodeType;
import org.deegree.services.jaxb.main.DCPType;
import org.deegree.services.jaxb.main.KeywordsType;
import org.deegree.services.jaxb.main.LanguageStringType;
import org.deegree.services.jaxb.main.ServiceContactType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
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
     * @param serviceID
     *            configuration object that provides most of the required metadata, must not be <code>null</code>
     * @param serviceName
     *            OGC-style abbreviation of the service, e.g. WFS, must not be <code>null</code>
     * @param serviceVersions
     *            supported protocol versions, must not be <code>null</code> and contain at least one entry
     * @throws XMLStreamException
     *             if writing the XML fails
     */
    public static void exportServiceIdentification100( XMLStreamWriter writer, ServiceIdentificationType serviceID,
                                                       final String serviceName, final List<Version> serviceVersions )
                            throws XMLStreamException {

        writer.writeStartElement( OWS_PREFIX, "ServiceIdentification", OWS_NS );
        if ( !serviceID.getTitle().isEmpty() ) {
            // schema has maxOccurs=1, so only export the first entry
            writeElement( writer, OWS_NS, "Title", serviceID.getTitle().get( 0 ) );
        }
        if ( !serviceID.getAbstract().isEmpty() ) {
            // schema has maxOccurs=1, so only export the first entry
            writeElement( writer, OWS_NS, "Abstract", serviceID.getAbstract().get( 0 ) );
        }

        for ( KeywordsType keywords : serviceID.getKeywords() ) {
            writer.writeStartElement( OWS_PREFIX, "Keywords", OWS_NS );
            for ( LanguageStringType keyword : keywords.getKeyword() ) {
                writeElement( writer, OWS_NS, "Keyword", keyword.getValue() );
            }
            if ( keywords.getType() != null ) {
                exportCodeType( writer, keywords.getType(), "Type", OWS_NS );
            }
            writer.writeEndElement();
        }

        writeElement( writer, OWS_NS, "ServiceType", serviceName );
        exportVersions( writer, serviceVersions, OWS_NS, "ServiceTypeVersion" );

        if ( serviceID.getFees() != null ) {
            writeElement( writer, OWS_NS, "Fees", serviceID.getFees() );
        }

        exportSimpleStrings( writer, serviceID.getAccessConstraints(), OWS_NS, "AccessConstraints" );
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
    public static void exportServiceProvider100( XMLStreamWriter writer, ServiceProviderType serviceProvider )
                            throws XMLStreamException {
        exportServiceProvider( writer, serviceProvider, OWS_NS );
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
        exportServiceProvider( writer, serviceProvider, OWS110_NS );
    }

    /**
     * Exports a list of {@link OWSOperation}s as an OWS 1.0.0 <code>OperationsMetadata</code> element.
     * 
     * @param writer
     *            writer to append the xml, must not be <code>null</code>
     * @param operations
     *            operations, e.g. "GetCapabilities", must not be <code>null</code>
     * @throws XMLStreamException
     */
    public static void exportOperationsMetadata100( XMLStreamWriter writer, List<OWSOperation> operations )
                            throws XMLStreamException {
        exportOperationsMetadata( writer, operations, OWS_NS );
    }

    /**
     * Exports a list of {@link OWSOperation}s as an OWS 1.1.0 <code>OperationsMetadata</code> element.
     * 
     * @param writer
     *            writer to append the xml, must not be <code>null</code>
     * @param operations
     *            operations, e.g. "GetCapabilities", must not be <code>null</code>
     * @throws XMLStreamException
     */
    public static void exportOperationsMetadata110( XMLStreamWriter writer, List<OWSOperation> operations )
                            throws XMLStreamException {
        exportOperationsMetadata( writer, operations, OWS110_NS );
    }

    /**
     * Exports a {@link DCPType} as an OWS 1.0.0 <code>DCP</code> element.
     * 
     * @param writer
     *            writer to append the xml
     * @param dcp
     *            <code>DCPType</code> to export
     * @throws XMLStreamException
     */
    public static void exportDCP100( XMLStreamWriter writer, DCPType dcp )
                            throws XMLStreamException {
        exportDCP( writer, dcp, OWS_NS );
    }

    /**
     * Exports a {@link DCPType} as an OWS 1.1.0 <code>DCP</code> element.
     * 
     * @param writer
     *            writer to append the xml
     * @param dcp
     *            <code>DCPType</code> to export
     * @throws XMLStreamException
     */
    public static void exportDCP110( XMLStreamWriter writer, DCPType dcp )
                            throws XMLStreamException {
        exportDCP( writer, dcp, OWS110_NS );
    }

    /**
     * Exports a {@link ServiceProviderType} as an OWS <code>ServiceProvider</code> element.
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
     *            <code>ServiceProviderType</code> to export
     * @param owsNS
     *            namespace for the generated elements
     * @throws XMLStreamException
     */
    private static void exportServiceProvider( XMLStreamWriter writer, ServiceProviderType serviceProvider, String owsNS )
                            throws XMLStreamException {

        writer.setPrefix( OWS_PREFIX, owsNS );
        writer.writeStartElement( owsNS, "ServiceProvider" );

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
        exportServiceContact( writer, serviceProvider.getServiceContact(), owsNS );

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
     * Exports a {@link ServiceContactType} as an OWS <code>ServiceContact</code> element.
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
    private static void exportServiceContact( XMLStreamWriter writer, ServiceContactType serviceContact, String owsNS )
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
        writeElement( writer, owsNS, "Role", serviceContact.getRole() );

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
     * @param serviceContact
     *            <code>ServiceContactType</code> to export
     * @param owsNS
     *            namespace for the generated elements
     * @throws XMLStreamException
     */
    private static void exportContactInfo( XMLStreamWriter writer, ServiceContactType serviceContact, String owsNS )
                            throws XMLStreamException {

        // check if one of the given is set.
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
    }

    /**
     * Export a list of operation names as an OWS <code>OperationsMetadata</code> element.
     * <p>
     * Please note that the generated <code>OperationsMetadata</code> element only contains a minimum of information,
     * i.e. it has no information on parameters (<code>Parameter</code> elements) or constraints (
     * <code>Constraint</code> elements). If information in these sections are necessary, a service implementation has
     * to generate the <code>OperationsMetadata</code> element itself. It is however possible to use
     * {@link #exportDCP(XMLStreamWriter, DCPType, String)} in that case for the generation of the contained
     * <code>DCP</code> elements.
     * </p>
     * <p>
     * The namespace of the produced elements is given as a parameter so it is usable for different OWS versions. It has
     * been checked that this method produces the correct output for the following OWS versions/namespaces:
     * <p>
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
     * @see #exportDCP(XMLStreamWriter, DCPType, String)
     * 
     * @param writer
     *            writer to append the xml
     * @param operations
     *            operation names, e.g. "GetCapabilities"
     * @param dcp
     *            <code>DCPType</code> to export
     * @param owsNS
     *            namespace for the generated elements
     * @throws XMLStreamException
     */
    private static void exportOperationsMetadata( XMLStreamWriter writer, List<OWSOperation> operations, String owsNS )
                            throws XMLStreamException {

        writer.writeStartElement( owsNS, "OperationsMetadata" );

        for ( OWSOperation operation : operations ) {
            // ows:Operation
            writer.writeStartElement( owsNS, "Operation" );
            writer.writeAttribute( "name", operation.getName() );
            exportDCP( writer, operation.getDcp(), owsNS );
            for ( Pair<String, List<String>> param : operation.getParams() ) {
                writer.writeStartElement( owsNS, "Parameter" );
                writer.writeAttribute( "name", param.first );
                for ( String value : param.second ) {
                    writer.writeStartElement( owsNS, "Value" );
                    writer.writeCharacters( value );
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            for ( Pair<String, List<String>> constraint : operation.getConstraints() ) {
                writer.writeStartElement( owsNS, "Constraint" );
                writer.writeAttribute( "name", constraint.first );
                for ( String value : constraint.second ) {
                    writer.writeStartElement( owsNS, "Value" );
                    writer.writeCharacters( value );
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        writer.writeEndElement();
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
    public static void exportDCP( XMLStreamWriter writer, DCPType dcp, String owsNS )
                            throws XMLStreamException {

        writer.writeStartElement( owsNS, "DCP" );
        writer.writeStartElement( owsNS, "HTTP" );

        // ows:Get (type="ows:RequestMethodType")
        if ( dcp.getHTTPGet() != null ) {
            writer.writeStartElement( owsNS, "Get" );
            writer.writeAttribute( XLN_NS, "href", dcp.getHTTPGet() );
            writer.writeEndElement();
        }

        // ows:Post (type="ows:RequestMethodType")
        if ( dcp.getHTTPPost() != null ) {
            writer.writeStartElement( owsNS, "Post" );
            writer.writeAttribute( XLN_NS, "href", dcp.getHTTPPost() );
            writer.writeEndElement();
        }

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
}
