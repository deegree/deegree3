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
package org.deegree.services.wfs;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_CAPABILITIES_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.cs.CRS;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.xml.FilterCapabilitiesExporter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.geometry.primitive.Point;
import org.deegree.protocol.wfs.WFSConstants.WFSRequestType;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.services.jaxb.main.DCPType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.deegree.services.jaxb.wfs.FeatureTypeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates WFS <code>GetCapabilities</code> response documents.
 * <p>
 * Supported WFS protocol versions:
 * <ul>
 * <li>1.0.0 (in implementation, nearly finished)</li>
 * <li>1.1.0 (in implementation, nearly finished)</li>
 * <li>2.0.0 (started, standard is still tentative)</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GetCapabilitiesHandler extends OWSCapabilitiesXMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( GetCapabilitiesHandler.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final String GML_PREFIX = "gml";

    private static final String WFS_PREFIX = "wfs";

    private static GeometryTransformer transformer;

    // used for formatting WGS84 bounding box coordinates (6 decimal places appears to be reasonable for most use-cases)
    private static CoordinateFormatter formatter = new DecimalCoordinateFormatter( 6 );

    static {
        try {
            transformer = new GeometryTransformer( "EPSG:4326" );
        } catch ( Exception e ) {
            LOG.error( "Could not initialize GeometryTransformer." );
        }
    }

    private Version version;

    private XMLStreamWriter writer;

    private ServiceIdentificationType serviceId;

    private ServiceProviderType serviceProvider;

    private Collection<FeatureType> servedFts;

    private Map<QName, FeatureTypeMetadata> ftNameToFtMetadata;

    private Set<String> sections;

    private boolean enableTransactions;

    private List<CRS> querySRS;

    private WFSController master;

    private WFService service;

    public GetCapabilitiesHandler( WFSController master, WFService service, Version version, XMLStreamWriter xmlWriter,
                                   ServiceIdentificationType serviceId, ServiceProviderType serviceProvider,
                                   Collection<FeatureType> servedFts,
                                   Map<QName, FeatureTypeMetadata> ftNameToFtMetadata, Set<String> sections,
                                   boolean enableTransactions, List<CRS> querySRS ) {
        this.master = master;
        this.service = service;
        this.version = version;
        this.writer = xmlWriter;
        this.serviceId = serviceId;
        this.serviceProvider = serviceProvider;
        this.servedFts = servedFts;
        this.ftNameToFtMetadata = ftNameToFtMetadata;
        this.sections = sections;
        this.enableTransactions = enableTransactions;
        this.querySRS = querySRS;
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document compliant to the specified WFS version.
     * 
     * @throws XMLStreamException
     * @throws IllegalArgumentException
     *             if the specified version is not 1.0.0, 1.1.0 or 2.0.0
     */
    public void export()
                            throws XMLStreamException {
        if ( VERSION_100.equals( version ) ) {
            export100();
        } else if ( VERSION_110.equals( version ) ) {
            export110();
        } else if ( VERSION_200.equals( version ) ) {
            export200();
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document that complies to the WFS 1.0.0 specification.
     * 
     * @throws XMLStreamException
     */
    public void export100()
                            throws XMLStreamException {
        writer.setPrefix( WFS_PREFIX, WFS_NS );
        writer.setPrefix( "ows", OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartElement( WFS_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "1.0.0" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", WFS_NS + " "
                                                                                + WFS_100_CAPABILITIES_SCHEMA_URL );

        // wfs:Service (type="wfs:ServiceType")
        exportService100();

        // wfs:Capability (type="wfs:CapabilityType")
        exportCapability100();

        // wfs:FeatureTypeList (type="wfs:FeatureTypeListType")
        writer.writeStartElement( WFS_NS, "FeatureTypeList" );

        exportOperations100();

        for ( FeatureType ft : servedFts ) {

            // wfs:FeatureType
            writer.writeStartElement( WFS_NS, "FeatureType" );

            // wfs:Name
            writer.writeStartElement( WFS_NS, "Name" );
            QName ftName = ft.getName();
            FeatureTypeMetadata ftMd = ftNameToFtMetadata.get( ftName );

            String prefix = null;
            if ( ftName.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
                prefix = ftName.getPrefix();
                if ( ftName.getPrefix() == null || ftName.getPrefix().equals( "" ) ) {
                    LOG.warn( "Feature type '" + ftName + "' has no prefix!? This should not happen." );
                    prefix = "app";
                }
                writer.setPrefix( prefix, ftName.getNamespaceURI() );
                writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
            } else {
                writer.writeCharacters( ftName.getLocalPart() );
            }
            writer.writeEndElement();

            // wfs:Title (minOccurs=0, maxOccurs=1)
            writer.writeStartElement( WFS_NS, "Title" );
            if ( ftMd != null && ftMd.getTitle() != null ) {
                writer.writeCharacters( ftMd.getTitle() );
            } else {
                if ( prefix != null ) {
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                } else {
                    writer.writeCharacters( ftName.getLocalPart() );
                }
            }
            writer.writeEndElement();

            // wfs:Abstract (minOccurs=0, maxOccurs=1)
            if ( ftMd != null && ftMd.getAbstract() != null ) {
                writer.writeStartElement( WFS_NS, "Abstract" );
                writer.writeCharacters( ftMd.getAbstract() );
                writer.writeEndElement();
            }

            // wfs:Keywords (minOccurs=0, maxOccurs=1)
            // if ( ft.getKeywords() != null ) {
            // writer.writeStartElement( WFS_NS, "Keywords" );
            // writer.writeCharacters( ft.getKeywords() );
            // writer.writeEndElement();
            // }

            // wfs:SRS (minOccurs=1, maxOccurs=1)
            writer.writeStartElement( WFS_NS, "SRS" );
            FeatureStore fs = service.getStore( ftName );
            if ( fs.getStorageSRS() == null ) {
                LOG.warn( "No default CRS for feature type '" + ftName + "' defined, using 'EPSG:4326'." );
                writer.writeCharacters( "EPSG:4326" );
            } else {
                writer.writeCharacters( fs.getStorageSRS().getName() );
            }
            writer.writeEndElement();

            // wfs:Operations (minOccurs=0, maxOccurs=1)
            exportOperations100();

            // wfs:LatLongBoundingBox (minOccurs=0, maxOccurs=unbounded)
            Envelope env = null;
            try {
                env = fs.getEnvelope( ftName );
            } catch ( FeatureStoreException e ) {
                LOG.error( "Error retrieving envelope from FeatureStore: " + e.getMessage(), e );
            }
            if ( env != null ) {
                try {
                    env = (Envelope) transformer.transform( env );
                    Point min = env.getMin();
                    Point max = env.getMax();
                    double minX = min.get0();
                    double minY = min.get1();
                    double maxX = max.get0();
                    double maxY = max.get1();
                    writer.writeStartElement( WFS_NS, "LatLongBoundingBox" );
                    writer.writeAttribute( "minx", "" + formatter.format( minX ) );
                    writer.writeAttribute( "miny", "" + formatter.format( minY ) );
                    writer.writeAttribute( "maxx", "" + formatter.format( maxX ) );
                    writer.writeAttribute( "maxy", "" + formatter.format( maxY ) );
                    writer.writeEndElement();
                } catch ( Exception e ) {
                    LOG.error( "Cannot generate WGS84 envelope for feature type '" + ftName + "'.", e );
                }
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();

        // ogc:Filter_Capabilities
        FilterCapabilitiesExporter.export100( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void exportCapability100()
                            throws XMLStreamException {

        writer.writeStartElement( WFS_NS, "Capability" );
        writer.writeStartElement( WFS_NS, "Request" );

        String getURL = OGCFrontController.getHttpGetURL();
        String postURL = OGCFrontController.getHttpPostURL();

        // wfs:GetCapabilities
        writer.writeStartElement( WFS_NS, WFSRequestType.GetCapabilities.name() );
        exportGetDCPType100( getURL );
        exportPostDCPType100( postURL );
        writer.writeEndElement();

        // wfs:DescribeFeatureType
        writer.writeStartElement( WFS_NS, WFSRequestType.DescribeFeatureType.name() );
        writer.writeStartElement( WFS_NS, "SchemaDescriptionLanguage" );
        writer.writeStartElement( WFS_NS, "XMLSCHEMA" );
        writer.writeEndElement();
        writer.writeEndElement();
        exportGetDCPType100( getURL );
        exportPostDCPType100( postURL );
        writer.writeEndElement();

        if ( enableTransactions ) {
            // wfs:Transaction
            writer.writeStartElement( WFS_NS, WFSRequestType.Transaction.name() );
            exportGetDCPType100( getURL );
            exportPostDCPType100( postURL );
            writer.writeEndElement();
        }

        // wfs:GetFeature
        writer.writeStartElement( WFS_NS, WFSRequestType.GetFeature.name() );
        writer.writeStartElement( WFS_NS, "ResultFormat" );
        writer.writeEmptyElement( WFS_NS, "GML2" );
        writer.writeEndElement();
        exportGetDCPType100( getURL );
        exportPostDCPType100( postURL );
        writer.writeEndElement();

        if ( enableTransactions ) {
            // wfs:GetFeatureWithLock
            writer.writeStartElement( WFS_NS, WFSRequestType.GetFeatureWithLock.name() );
            writer.writeStartElement( WFS_NS, "ResultFormat" );
            writer.writeEmptyElement( WFS_NS, "GML2" );
            writer.writeEndElement();
            exportGetDCPType100( getURL );
            exportPostDCPType100( postURL );
            writer.writeEndElement();

            // wfs:LockFeature
            writer.writeStartElement( WFS_NS, WFSRequestType.LockFeature.name() );
            exportGetDCPType100( getURL );
            exportPostDCPType100( postURL );
            writer.writeEndElement();
        }

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void exportService100()
                            throws XMLStreamException {

        writer.writeStartElement( WFS_NS, "Service" );

        if ( serviceId != null && serviceId.getTitle() != null && !serviceId.getTitle().isEmpty() ) {
            // wfs:Name (type="string")
            writeElement( writer, WFS_NS, "Name", serviceId.getTitle().get( 0 ) );
            // wfs:Title (type="string)
            writeElement( writer, WFS_NS, "Title", serviceId.getTitle().get( 0 ) );
        } else {
            writeElement( writer, WFS_NS, "Name", "" );
            writeElement( writer, WFS_NS, "Title", "" );
        }

        if ( serviceId != null && serviceId.getAbstract() != null && !serviceId.getAbstract().isEmpty() ) {
            // wfs:Abstract
            writeElement( writer, WFS_NS, "Abstract", serviceId.getAbstract().get( 0 ) );
        }

        // wfs:Keywords

        // wfs:OnlineResource (type=???)
        writeElement( writer, WFS_NS, "OnlineResource", serviceProvider.getServiceContact().getOnlineResource() );

        // wfs:Fees
        if ( serviceId != null && serviceId.getFees() != null ) {
            writeElement( writer, WFS_NS, "Fees", serviceId.getFees() );
        }

        // wfs:AccessConstraints

        writer.writeEndElement();
    }

    private void exportOperations100()
                            throws XMLStreamException {
        writer.writeStartElement( WFS_NS, "Operations" );
        if ( enableTransactions ) {
            writer.writeEmptyElement( WFS_NS, "Insert" );
            writer.writeEmptyElement( WFS_NS, "Update" );
            writer.writeEmptyElement( WFS_NS, "Delete" );
        }
        writer.writeEmptyElement( WFS_NS, "Query" );
        if ( enableTransactions ) {
            writer.writeEmptyElement( WFS_NS, "Lock" );
        }
        writer.writeEndElement();
    }

    private void exportGetDCPType100( String getURL )
                            throws XMLStreamException {

        if ( getURL != null ) {
            writer.writeStartElement( WFS_NS, "DCPType" );
            writer.writeStartElement( WFS_NS, "HTTP" );

            // ows:Get (type="ows:GetType")
            writer.writeStartElement( WFS_NS, "Get" );
            writer.writeAttribute( "onlineResource", getURL );
            writer.writeEndElement();

            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private void exportPostDCPType100( String postURL )
                            throws XMLStreamException {
        if ( postURL != null ) {
            writer.writeStartElement( WFS_NS, "DCPType" );
            writer.writeStartElement( WFS_NS, "HTTP" );

            // ows:Post (type="ows:PostType")
            writer.writeStartElement( WFS_NS, "Post" );
            writer.writeAttribute( "onlineResource", postURL );
            writer.writeEndElement();

            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document that complies to the WFS 1.1.0 specification.
     * 
     * @throws XMLStreamException
     */
    public void export110()
                            throws XMLStreamException {

        writer.setPrefix( WFS_PREFIX, WFS_NS );
        writer.setPrefix( OWS_PREFIX, OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartElement( WFS_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "1.1.0" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", WFS_NS + " " + WFS_110_SCHEMA_URL );

        // ows:ServiceIdentification
        if ( sections == null || sections.contains( "SERVICEIDENTIFICATION" ) ) {
            List<Version> serviceVersions = new ArrayList<Version>();
            serviceVersions.add( Version.parseVersion( "1.0.0" ) );
            exportServiceIdentification100( writer, serviceId, "WFS", serviceVersions );
        }

        // ows:ServiceProvider
        if ( sections == null || sections.contains( "SERVICEPROVIDER" ) ) {
            exportServiceProvider100( writer, serviceProvider );
        }

        // ows:OperationsMetadata
        if ( sections == null || sections.contains( "OPERATIONSMETADATA" ) ) {
            List<String> operations = new LinkedList<String>();
            operations.add( WFSRequestType.DescribeFeatureType.name() );
            operations.add( WFSRequestType.GetCapabilities.name() );
            operations.add( WFSRequestType.GetFeature.name() );
            if ( enableTransactions ) {
                operations.add( WFSRequestType.GetFeatureWithLock.name() );
            }
            operations.add( WFSRequestType.GetGmlObject.name() );
            if ( enableTransactions ) {
                operations.add( WFSRequestType.LockFeature.name() );
                operations.add( WFSRequestType.Transaction.name() );
            }
            // TODO
            DCPType dcp = new DCPType();
            dcp.setHTTPGet( OGCFrontController.getHttpGetURL() );
            dcp.setHTTPPost( OGCFrontController.getHttpPostURL() );
            exportOperationsMetadata100( writer, operations, dcp );
        }

        // wfs:FeatureTypeList
        if ( sections == null || sections.contains( "FEATURETYPELIST" ) ) {
            writer.writeStartElement( WFS_NS, "FeatureTypeList" );
            for ( FeatureType ft : servedFts ) {
                QName ftName = ft.getName();
                FeatureTypeMetadata ftMd = ftNameToFtMetadata.get( ftName );
                writer.writeStartElement( WFS_NS, "FeatureType" );
                // wfs:Name
                writer.writeStartElement( WFS_NS, "Name" );
                String prefix = ftName.getPrefix();
                if ( prefix == null || prefix.equals( "" ) ) {
                    LOG.warn( "Feature type '" + ftName + "' has no prefix!? This should not happen." );
                    prefix = "app";
                }
                if ( ftName.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
                    // TODO what about the namespace prefix?
                    writer.setPrefix( prefix, ftName.getNamespaceURI() );
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                } else {
                    writer.writeCharacters( ftName.getLocalPart() );
                }
                writer.writeEndElement();

                // wfs:Title
                writer.writeStartElement( WFS_NS, "Title" );
                if ( ftMd != null && ftMd.getTitle() != null ) {
                    writer.writeCharacters( ftMd.getTitle() );
                } else {
                    if ( prefix != null ) {
                        writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                    } else {
                        writer.writeCharacters( ftName.getLocalPart() );
                    }
                }
                writer.writeEndElement();

                // wfs:Abstract (minOccurs=0, maxOccurs=1)
                if ( ftMd != null && ftMd.getAbstract() != null ) {
                    writer.writeStartElement( WFS_NS, "Abstract" );
                    writer.writeCharacters( ftMd.getAbstract() );
                    writer.writeEndElement();
                }

                // ows:Keywords (minOccurs=0, maxOccurs=unbounded)
                // writer.writeStartElement( OWS_NS, "Keywords" );
                // writer.writeCharacters( "keywords" );
                // writer.writeEndElement();

                // wfs:DefaultSRS / wfs:NoSRS
                FeatureStore fs = service.getStore( ftName );
                if ( querySRS.isEmpty() ) {
                    if ( fs.getStorageSRS() == null ) {
                        LOG.warn( "No default CRS for feature type '" + ftName + "' defined, using 'EPSG:4326'." );
                        writeElement( writer, WFS_NS, "DefaultSRS", "EPSG:4326" );
                    } else {
                        writeElement( writer, WFS_NS, "DefaultSRS", fs.getStorageSRS().getName() );
                    }
                } else {
                    writeElement( writer, WFS_NS, "DefaultSRS", querySRS.get( 0 ).getName() );
                }

                // wfs:OtherSRS
                for ( int i = 1; i < querySRS.size(); i++ ) {
                    writeElement( writer, WFS_NS, "OtherSRS", querySRS.get( i ).getName() );
                }

                writeOutputFormats110( writer );

                // ows:WGS84BoundingBox (minOccurs=0, maxOccurs=unbounded)
                Envelope env = null;
                try {
                    env = fs.getEnvelope( ftName );
                } catch ( FeatureStoreException e ) {
                    LOG.error( "Error retrieving envelope from FeatureStore: " + e.getMessage(), e );
                }

                if ( env != null ) {
                    try {
                        env = (Envelope) transformer.transform( env );
                    } catch ( Exception e ) {
                        LOG.error( "Cannot transform feature type envelope to WGS84." );
                    }
                } else {
                    env = new SimpleGeometryFactory().createEnvelope( -180, -90, 180, 90, new CRS( "EPSG:4326" ) );
                }

                writer.writeStartElement( OWS_NS, "WGS84BoundingBox" );
                Point min = env.getMin();
                Point max = env.getMax();
                double minX = -180.0;
                double minY = -90.0;
                double maxX = 180.0;
                double maxY = 90.0;
                try {
                    minX = min.get0();
                    minY = min.get1();
                    maxX = max.get0();
                    maxY = max.get1();
                } catch ( ArrayIndexOutOfBoundsException e ) {
                    LOG.error( "Cannot generate WGS84 envelope for feature type '" + ftName + "'. Using full extent.",
                               e );
                    minX = -180.0;
                    minY = -90.0;
                    maxX = 180.0;
                    maxY = 90.0;
                }
                writer.writeStartElement( OWS_NS, "LowerCorner" );
                writer.writeCharacters( formatter.format( minX ) + " " + formatter.format( minY ) );
                writer.writeEndElement();
                writer.writeStartElement( OWS_NS, "UpperCorner" );
                writer.writeCharacters( formatter.format( maxX ) + " " + formatter.format( maxY ) );
                writer.writeEndElement();
                writer.writeEndElement();

                // TODO Operations

                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        // wfs:ServesGMLObjectTypeList
        if ( sections == null || sections.contains( "SERVESGMLOBJECTTYPELIST" ) ) {
            // TODO
        }

        // wfs:SupportsGMLObjectTypeList
        if ( sections == null || sections.contains( "SUPPORTSGMLOBJECTTYPELIST" ) ) {
            // TODO
        }

        // 'ogc:Filter_Capabilities' (mandatory)
        FilterCapabilitiesExporter.export110( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void writeOutputFormats110( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( WFS_PREFIX, "OutputFormats", WFS_NS );
        writer.writeStartElement( WFS_PREFIX, "Format", WFS_NS );
        writer.writeCharacters( "GML2" );
        writer.writeEndElement();
        writer.writeStartElement( WFS_PREFIX, "Format", WFS_NS );
        writer.writeCharacters( "GML3" );
        writer.writeEndElement();
        writer.writeStartElement( WFS_PREFIX, "Format", WFS_NS );
        writer.writeCharacters( "text/xml; subtype=gml/2.1.2" );
        writer.writeEndElement();
        writer.writeStartElement( WFS_PREFIX, "Format", WFS_NS );
        writer.writeCharacters( "text/xml; subtype=gml/3.0.1" );
        writer.writeEndElement();
        writer.writeStartElement( WFS_PREFIX, "Format", WFS_NS );
        writer.writeCharacters( "text/xml; subtype=gml/3.1.1" );
        writer.writeEndElement();
        writer.writeStartElement( WFS_PREFIX, "Format", WFS_NS );
        writer.writeCharacters( "text/xml; subtype=gml/3.2.1" );
        writer.writeEndElement();
        writer.writeEndElement();
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document that complies to the WFS 2.0.0 specification (tentative).
     * 
     * @throws XMLStreamException
     */
    public void export200()
                            throws XMLStreamException {

        writer.setPrefix( WFS_PREFIX, WFS_200_NS );
        writer.setPrefix( "ows", OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartElement( WFS_200_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "2.0.0" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", WFS_200_NS + " " + WFS_200_SCHEMA_URL );

        // ows:ServiceIdentification
        if ( sections.size() == 0 || sections.contains( "ServiceIdentification" ) ) {
            // exportServiceIdentification( writer );
        }

        // ows:ServiceProvider
        if ( sections.size() == 0 || sections.contains( "ServiceProvider" ) ) {
            exportServiceProvider100( writer, serviceProvider );
        }

        // ows:OperationsMetadata
        if ( sections.size() == 0 || sections.contains( "OperationsMetadata" ) ) {
            List<String> operations = new LinkedList<String>();
            operations.add( WFSRequestType.DescribeFeatureType.name() );
            operations.add( WFSRequestType.GetCapabilities.name() );
            operations.add( WFSRequestType.GetFeature.name() );
            if ( enableTransactions ) {
                operations.add( WFSRequestType.GetFeatureWithLock.name() );
            }
            operations.add( WFSRequestType.GetGmlObject.name() );
            if ( enableTransactions ) {
                operations.add( WFSRequestType.LockFeature.name() );
                operations.add( WFSRequestType.Transaction.name() );
            }
            exportOperationsMetadata100( writer, operations, null );
        }

        // wfs:FeatureTypeList
        if ( sections.size() == 0 || sections.contains( "FeatureTypeList" ) ) {
            // for ( QName ftName : servedFts ) {
            // writer.writeStartElement( WFS_200_NS, "FeatureType" );
            // // wfs:Name
            // writer.writeStartElement( WFS_200_NS, "Name" );
            // if ( ftName.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
            // // TODO evaluate namespace prefix generation strategies
            // writer.writeAttribute( "xmlns:app", ftName.getNamespaceURI() );
            // writer.writeCharacters( "app:" + ftName.getLocalPart() );
            // } else {
            // writer.writeCharacters( ftName.getLocalPart() );
            // }
            // writer.writeEndElement();
            //
            // // wfs:Title
            // writer.writeStartElement( WFS_200_NS, "Title" );
            // writer.writeCharacters( "title" );
            // writer.writeEndElement();
            //
            // // wfs:Abstract (minOccurs=0, maxOccurs=1)
            // writer.writeStartElement( WFS_200_NS, "Abstract" );
            // writer.writeCharacters( "abstract" );
            // writer.writeEndElement();
            //
            // // wfs:Keywords (minOccurs=0, maxOccurs=1)
            // writer.writeStartElement( WFS_200_NS, "Keywords" );
            // writer.writeCharacters( "keywords" );
            // writer.writeEndElement();
            //
            // // TODO Operations, OutputFormats, ...
            //
            // writer.writeEndElement();
            // }
        }

        // wfs:ServesGMLObjectTypeList
        if ( sections.size() == 0 || sections.contains( "ServesGMLObjectTypeList" ) ) {
            // TODO
        }

        // wfs:SupportsGMLObjectTypeList
        if ( sections.size() == 0 || sections.contains( "SupportsGMLObjectTypeList" ) ) {
            // TODO
        }

        // ogc:Filter_Capabilities
        if ( sections.size() == 0 || sections.contains( "Filter_Capabilities" ) ) {
            // TODO
        }

        writer.writeEndElement();
        writer.writeEndDocument();
    }
}
