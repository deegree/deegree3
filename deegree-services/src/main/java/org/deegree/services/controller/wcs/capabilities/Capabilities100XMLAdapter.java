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
package org.deegree.services.controller.wcs.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.protocol.wcs.WCSConstants.VERSION_100;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_NS;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_SCHEMA;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.configuration.CodeType;
import org.deegree.commons.configuration.KeywordsType;
import org.deegree.commons.configuration.LanguageStringType;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.services.controller.configuration.AddressType;
import org.deegree.services.controller.configuration.DCPType;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.configuration.ServiceContactType;
import org.deegree.services.controller.configuration.ServiceIdentificationType;
import org.deegree.services.controller.configuration.ServiceProviderType;
import org.deegree.services.controller.wcs.describecoverage.CoverageDescription100XMLAdapter;
import org.deegree.services.wcs.configuration.ServiceConfiguration.Coverage;
import org.deegree.services.wcs.coverages.WCSCoverage;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Capabilities100XMLAdapter extends XMLAdapter {

    private static final String GML_PREFIX = "gml";

    private static final String GML_NS = "http://www.opengis.net/gml";

    /**
     * The sections of the Capabilities document.
     */
    public enum Sections {
        /***/
        Service, /***/
        Capability, /***/
        ContentMetadata
    }

    /**
     * Export a WCS Capabilities document from a list of {@link Coverage}s.
     * 
     * @param allowedOperations
     * @param provider
     * @param identification
     * @param request
     * @param xmlWriter
     * 
     * @param sections
     *            the capabilities sections to export
     * @param coverages
     *            all coverages of this WCS web service
     * @param serviceMetadata
     *            metadata for the service
     * @param writer
     * @param updateSequence
     *            of this wcs
     * @throws XMLStreamException
     */
    public static void export( XMLStreamWriter xmlWriter, GetCapabilities request,
                               ServiceIdentificationType identification, ServiceProviderType provider,
                               List<String> allowedOperations, Set<Sections> sections, List<WCSCoverage> coverages,
                               DeegreeServicesMetadata serviceMetadata, XMLStreamWriter writer, int updateSequence )
                            throws XMLStreamException {
        writer.setDefaultNamespace( WCS_100_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xsi", XSINS );
        writer.setPrefix( "xlink", XLN_NS );

        if ( sections.isEmpty() ) {
            writer.writeStartElement( WCS_100_NS, "WCS_Capabilities" );
            writer.writeAttribute( XSINS, "schemaLocation", WCS_100_NS + " " + WCS_100_SCHEMA );
            writeVersionAndUpdateSequence( writer, updateSequence );
            exportService( writer, identification, provider, updateSequence, false );
            exportCapability( writer, serviceMetadata, allowedOperations, updateSequence, false );
            exportContentMetadata( writer, coverages, updateSequence, false );
        } else {
            if ( sections.contains( Sections.Service ) ) {
                exportService( writer, identification, provider, updateSequence, true );
            } else if ( sections.contains( Sections.Capability ) ) {
                exportCapability( writer, serviceMetadata, allowedOperations, updateSequence, true );
            } else if ( sections.contains( Sections.ContentMetadata ) ) {
                exportContentMetadata( writer, coverages, updateSequence, true );
            }
        }
        writer.writeEndElement(); // Capabilities
    }

    private static void exportContentMetadata( XMLStreamWriter writer, List<WCSCoverage> coverages, int updateSequence,
                                               boolean isSection )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "ContentMetadata" );
        if ( isSection ) {
            writeVersionAndUpdateSequence( writer, updateSequence );
        }

        // @xlink:simpleXlink
        // @gml:remoteSchema

        for ( WCSCoverage coverage : coverages ) {
            exportCoverage( writer, coverage );
        }
        writer.writeEndElement(); // ContentMetadata
    }

    private static void exportCoverage( XMLStreamWriter writer, WCSCoverage coverage )
                            throws XMLStreamException {
        if ( coverage != null ) {
            writer.writeStartElement( WCS_100_NS, "CoverageOfferingBrief" );
            CoverageDescription100XMLAdapter.exportBriefCoverageData( writer, coverage );
            writer.writeEndElement(); // CoverageOfferingBrief
        }
    }

    private static void writeVersionAndUpdateSequence( XMLStreamWriter writer, int updateSequence )
                            throws XMLStreamException {
        writer.writeAttribute( "version", VERSION_100.toString() );
        writer.writeAttribute( "updateSequence", Integer.toString( updateSequence ) );
    }

    private static void exportCapability( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata,
                                          List<String> allowedOperations, int updateSequence, boolean isSection )
                            throws XMLStreamException {

        writer.writeStartElement( WCS_100_NS, "Capability" );
        if ( isSection ) {
            // @version
            // @updateSequence
            writeVersionAndUpdateSequence( writer, updateSequence );
        }

        exportOperationsMetadata( writer, allowedOperations, serviceMetadata.getDCP() );

        writer.writeStartElement( WCS_100_NS, "Exception" );
        writeElement( writer, WCS_100_NS, "Format", "application/vnd.ogc.se_xml" );

        // VendorSpecificCapabilities [0,1]
        // any

        writer.writeEndElement(); // Exception
        writer.writeEndElement(); // Capability
    }

    private static void exportOperationsMetadata( XMLStreamWriter writer, List<String> operations, DCPType dcp )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "Request" );
        for ( String operation : operations ) {
            writer.writeStartElement( WCS_100_NS, operation );
            writer.writeStartElement( WCS_100_NS, "DCPType" );
            writer.writeStartElement( WCS_100_NS, "HTTP" );
            if ( !isEmpty( dcp.getHTTPGet() ) ) {
                writer.writeStartElement( WCS_100_NS, "Get" );
                writeElement( writer, WCS_100_NS, "OnlineResource", XLN_NS, "href", dcp.getHTTPGet() );
                writer.writeEndElement(); // Get
            }
            if ( !isEmpty( dcp.getHTTPPost() ) ) {
                writer.writeStartElement( WCS_100_NS, "Post" );
                writeElement( writer, WCS_100_NS, "OnlineResource", XLN_NS, "href", dcp.getHTTPPost() );
                writer.writeEndElement(); // Post
            }
            writer.writeEndElement(); // HTTP
            writer.writeEndElement(); // DCPType
            writer.writeEndElement(); // operation
        }
        writer.writeEndElement(); // Request
    }

    private static void exportService( XMLStreamWriter writer, ServiceIdentificationType identification,
                                       ServiceProviderType provider, int updateSequence, boolean isSection )
                            throws XMLStreamException {
        writer.writeStartElement( WCS_100_NS, "Service" );
        if ( isSection ) {
            // @version optional (1.0.0)
            // @updateSequence optional
            writeVersionAndUpdateSequence( writer, updateSequence );
        }

        if ( provider != null ) {
            // where to get metadata link from
            exportMetadataLink( writer, "other", provider.getProviderSite() );

            // description [0,1]
            // writeElement( writer, "description", provider.getProviderName() )

            // name [1]
            writeElement( writer, "name", provider.getProviderName() );
            // -> @codeSpace optional

            // label [1]
            writeElement( writer, "label", provider.getProviderName() );
            if ( identification != null ) {
                // keywords [0,n]
                exportKeywords( writer, identification.getKeywords() );
            }
            // responsibleParty [0,1]
            exportResponsibleParty( writer, provider.getServiceContact(), provider.getProviderName() );
            String fees = "NONE";
            if ( identification != null ) {
                // fees [1]
                fees = identification.getFees();
                if ( isEmpty( fees ) ) {
                    identification.setFees( "NONE" );
                }
                fees = identification.getFees();

            }
            fees = fees.replaceAll( "\\W", " " );
            writeElement( writer, WCS_100_NS, "fees", fees );

            // accessConstraints [1, n]
            List<String> accessConstraints = new ArrayList<String>( 1 );
            if ( identification != null ) {
                accessConstraints = identification.getAccessConstraints();
            }
            if ( accessConstraints.isEmpty() ) {
                accessConstraints.add( "NONE" );
            }
            for ( String ac : accessConstraints ) {
                if ( !isEmpty( ac ) ) {
                    writeElement( writer, WCS_100_NS, "accessConstraints", ac );
                }
            }
        }

        writer.writeEndElement();// WCS_100_NS, "Service" );
    }

    /**
     * @param writer
     * @param serviceContactType
     * @param provider
     * @throws XMLStreamException
     */
    private static void exportResponsibleParty( XMLStreamWriter writer, ServiceContactType serviceContactType,
                                                String providerName )
                            throws XMLStreamException {
        if ( serviceContactType != null ) {
            writer.writeStartElement( WCS_100_NS, "responsibleParty" );
            // choice{
            // --
            // -> individualName [1]
            // -> organisationName[0,1]
            // --
            // -> organisationName[1]
            // --
            // }
            if ( !isEmpty( serviceContactType.getIndividualName() ) ) {
                writeElement( writer, WCS_100_NS, "individualName", serviceContactType.getIndividualName() );
                if ( !isEmpty( providerName ) ) {
                    writeElement( writer, WCS_100_NS, "organisationName", providerName );
                }
            } else {
                writeElement( writer, WCS_100_NS, "organisationName", providerName );
            }
            // -> positionName[0,1]
            if ( !isEmpty( serviceContactType.getPositionName() ) ) {
                writeElement( writer, WCS_100_NS, "positionName", serviceContactType.getPositionName() );
            }
            if ( serviceContactType.getAddress() != null || !isEmpty( serviceContactType.getPhone() )
                 || !isEmpty( serviceContactType.getFacsimile() ) || !isEmpty( serviceContactType.getOnlineResource() ) ) {
                // -> contactInfo[0,1]
                writer.writeStartElement( WCS_100_NS, "contactInfo" );
                // --> phone [0,1]
                if ( !isEmpty( serviceContactType.getPhone() ) || !isEmpty( serviceContactType.getFacsimile() ) ) {
                    writer.writeStartElement( WCS_100_NS, "phone" );
                    if ( !isEmpty( serviceContactType.getPhone() ) ) {
                        // ---> voice [0,n]
                        writeElement( writer, WCS_100_NS, "voice", serviceContactType.getPhone() );
                    }
                    if ( !isEmpty( serviceContactType.getFacsimile() ) ) {
                        // ---> facsimile [0,n]
                        writeElement( writer, WCS_100_NS, "facsimile", serviceContactType.getFacsimile() );
                    }
                    writer.writeEndElement();// WCS_100_NS, "phone" );
                }
                // --> address [0,1]
                exportAddress( writer, serviceContactType.getAddress(), serviceContactType.getElectronicMailAddress() );

                if ( !isEmpty( serviceContactType.getOnlineResource() ) ) {
                    writeElement( writer, WCS_100_NS, "onlineResource", CommonNamespaces.XLNNS, "href",
                                  serviceContactType.getOnlineResource() );
                    // --> onlineResource [0,1]
                    // ---> @"xlink:simpleLink"
                }
                writer.writeEndElement();// WCS_100_NS, "contactInfo" );
            }

            writer.writeEndElement();// WCS_100_NS, "responsibleParty" );
        }
    }

    /**
     * export an address type in wcs 1.0.0 style
     * 
     * @param writer
     * @param addressType
     * @param emails
     * @throws XMLStreamException
     */
    public static void exportAddress( XMLStreamWriter writer, AddressType addressType, List<String> emails )
                            throws XMLStreamException {
        if ( addressType != null ) {
            writer.writeStartElement( WCS_100_NS, "address" );
            if ( !addressType.getDeliveryPoint().isEmpty() ) {
                for ( String dp : addressType.getDeliveryPoint() ) {
                    // ---> deliveryPoint [0,n]
                    if ( !isEmpty( dp ) ) {
                        writeElement( writer, WCS_100_NS, "deliveryPoint", dp );
                    }
                }
            }
            // ---> city [0,1]
            if ( !isEmpty( addressType.getCity() ) ) {
                writeElement( writer, WCS_100_NS, "city", addressType.getCity() );
            }
            // ---> administrativeArea [0,1]
            if ( !isEmpty( addressType.getAdministrativeArea() ) ) {
                writeElement( writer, WCS_100_NS, "administrativeArea", addressType.getAdministrativeArea() );
            }

            // ---> postalCode [0,1]
            if ( !isEmpty( addressType.getPostalCode() ) ) {
                writeElement( writer, WCS_100_NS, "postalCode", addressType.getPostalCode() );
            }
            // ---> country [0,1]
            if ( !isEmpty( addressType.getCountry() ) ) {
                writeElement( writer, WCS_100_NS, "country", addressType.getCountry() );
            }
            // ---> electronicMailAddress [0,n]
            if ( emails != null && !emails.isEmpty() ) {
                for ( String em : emails ) {
                    if ( !isEmpty( em ) ) {
                        writeElement( writer, WCS_100_NS, "electronicMailAddress", em );
                    }
                }
            }
            writer.writeEndElement();// WCS_100_NS, "address" );

        }
    }

    /**
     * @param writer
     * @param type
     * @param link
     * @throws XMLStreamException
     */
    public static void exportMetadataLink( XMLStreamWriter writer, String type, String link )
                            throws XMLStreamException {
        if ( link != null && !"".equals( link ) ) {
            // metadataLink [0,n]
            // -> some metadata from
            // -> @gml:AssociationAttributeGroup
            // -> @about optional
            // -> gml:_MetaData element [0,n]
            writer.writeStartElement( WCS_100_NS, "metadataLink" );
            writer.writeAttribute( CommonNamespaces.XLNNS, "href", link );
            String t = type;
            if ( !( "FGDC".equals( type ) || "TC211".equals( type ) ) ) {
                t = "other";
            }
            writer.writeAttribute( "metadataType", t );

            writer.writeEndElement();// WCS_100_NS, "metadataLink" );
        }
    }

    /**
     * write a list of keywords in wcs 1.0.0 style.
     * 
     * @param writer
     * @param keywords
     * @throws XMLStreamException
     */
    public static void exportKeywords( XMLStreamWriter writer, List<KeywordsType> keywords )
                            throws XMLStreamException {
        if ( !keywords.isEmpty() ) {
            for ( KeywordsType kwt : keywords ) {
                if ( kwt != null ) {
                    writer.writeStartElement( WCS_100_NS, "keywords" );
                    List<LanguageStringType> keyword = kwt.getKeyword();
                    for ( LanguageStringType lst : keyword ) {
                        exportKeyword( writer, lst );
                        // -> keyword [1, n]
                    }
                    // -> type [0,1]
                    exportCodeType( writer, kwt.getType() );
                    writer.writeEndElement();// WCS_100_NS, "keywords" );
                }
            }

        }

    }

    private final static boolean isEmpty( String value ) {
        return value == null || "".equals( value );
    }

    /**
     * @param writer
     * @param lst
     * @throws XMLStreamException
     */
    public static void exportKeyword( XMLStreamWriter writer, LanguageStringType lst )
                            throws XMLStreamException {
        if ( lst != null ) {
            writeElement( writer, WCS_100_NS, "keyword", lst.getValue() );
        }
    }

    /**
     * Code type to export
     * 
     * @param writer
     * @param ct
     * @throws XMLStreamException
     */
    public static void exportCodeType( XMLStreamWriter writer, CodeType ct )
                            throws XMLStreamException {
        if ( ct != null ) {
            writeElement( writer, WCS_100_NS, "type", ct.getValue(), "codeSpace", ct.getCodeSpace() );
            // --> @codSpace optional
        }
    }
}
