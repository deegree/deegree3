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

import static java.util.Collections.singletonList;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.FES_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.OGC_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_CAPABILITIES_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.WFSRequestType.CreateStoredQuery;
import static org.deegree.protocol.wfs.WFSRequestType.DescribeFeatureType;
import static org.deegree.protocol.wfs.WFSRequestType.DescribeStoredQueries;
import static org.deegree.protocol.wfs.WFSRequestType.DropStoredQuery;
import static org.deegree.protocol.wfs.WFSRequestType.GetCapabilities;
import static org.deegree.protocol.wfs.WFSRequestType.GetFeature;
import static org.deegree.protocol.wfs.WFSRequestType.GetFeatureWithLock;
import static org.deegree.protocol.wfs.WFSRequestType.GetGmlObject;
import static org.deegree.protocol.wfs.WFSRequestType.GetPropertyValue;
import static org.deegree.protocol.wfs.WFSRequestType.ListStoredQueries;
import static org.deegree.protocol.wfs.WFSRequestType.LockFeature;
import static org.deegree.protocol.wfs.WFSRequestType.Transaction;
import static org.deegree.services.controller.OGCFrontController.getHttpGetURL;
import static org.deegree.services.controller.OGCFrontController.getHttpPostURL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.MetadataUrl;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
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
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.wfs.WFSRequestType;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.encoding.SupportedEncodings;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a single {@link GetCapabilities} request for the {@link WebFeatureService}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
class GetCapabilitiesHandler extends OWSCapabilitiesXMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( GetCapabilitiesHandler.class );

    private static GeometryTransformer transformer;

    // used for formatting WGS84 bounding box coordinates
    private static CoordinateFormatter formatter = new DecimalCoordinateFormatter();

    static {
        try {
            transformer = new GeometryTransformer( "EPSG:4326" );
        } catch ( Exception e ) {
            LOG.error( "Could not initialize GeometryTransformer." );
        }
    }

    private final Version version;

    // in descending order
    private final List<Version> offeredVersions = new ArrayList<Version>();

    private final List<String> offeredVersionStrings = new ArrayList<String>();

    private final XMLStreamWriter writer;

    private final Collection<FeatureType> servedFts;

    private final Set<String> sections;

    private final boolean enableTransactions;

    private final List<ICRS> querySRS;

    private SupportedEncodings supportedEncodings;

    private final WfsFeatureStoreManager service;

    private final WebFeatureService master;

    private final OWSMetadataProvider mdProvider;

    GetCapabilitiesHandler( WebFeatureService master, WfsFeatureStoreManager service, Version version,
                            XMLStreamWriter xmlWriter, Collection<FeatureType> servedFts, Set<String> sections,
                            boolean enableTransactions, List<ICRS> querySRS, SupportedEncodings supportedEncodings,
                            OWSMetadataProvider mdProvider ) {
        this.master = master;
        this.service = service;
        this.version = version;
        this.writer = xmlWriter;
        this.servedFts = servedFts;
        this.sections = sections;
        this.enableTransactions = enableTransactions;
        this.querySRS = querySRS;
        this.supportedEncodings = supportedEncodings;
        this.mdProvider = mdProvider;

        List<String> offeredVersions = master.getOfferedVersions();
        for ( int i = offeredVersions.size() - 1; i >= 0; i-- ) {
            this.offeredVersionStrings.add( offeredVersions.get( i ) );
            this.offeredVersions.add( Version.parseVersion( offeredVersions.get( i ) ) );
        }
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document compliant to the specified WFS version.
     * 
     * @throws XMLStreamException
     * @throws IllegalArgumentException
     *             if the specified version is not 1.0.0, 1.1.0 or 2.0.0
     */
    void export()
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
    void export100()
                            throws XMLStreamException {

        writer.setDefaultNamespace( WFS_NS );
        writer.writeStartElement( WFS_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "1.0.0" );
        writer.writeDefaultNamespace( WFS_NS );
        writer.writeNamespace( OWS_PREFIX, OWS_NS );
        writer.writeNamespace( OGC_PREFIX, OGCNS );
        writer.writeNamespace( GML_PREFIX, GMLNS );
        writer.writeNamespace( XLINK_PREFIX, XLN_NS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        writer.writeAttribute( XSINS, "schemaLocation", WFS_NS + " " + WFS_100_CAPABILITIES_SCHEMA_URL );

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
            DatasetMetadata ftMd = mdProvider.getDatasetMetadata( ftName );

            String prefix = null;
            if ( ftName.getNamespaceURI() != "" ) {
                prefix = ftName.getPrefix();
                if ( ftName.getPrefix() == null || ftName.getPrefix().equals( "" ) ) {
                    LOG.warn( "Feature type '" + ftName + "' has no prefix!? This should not happen." );
                    prefix = "app";
                }
                writer.writeNamespace( prefix, ftName.getNamespaceURI() );
                writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
            } else {
                writer.writeCharacters( ftName.getLocalPart() );
            }
            writer.writeEndElement();

            // wfs:Title (minOccurs=0, maxOccurs=1)
            writer.writeStartElement( WFS_NS, "Title" );
            if ( ftMd != null && ftMd.getTitle( null ) != null ) {
                writer.writeCharacters( ftMd.getTitle( null ).getString() );
            } else {
                if ( prefix != null ) {
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                } else {
                    writer.writeCharacters( ftName.getLocalPart() );
                }
            }
            writer.writeEndElement();

            // wfs:Abstract (minOccurs=0, maxOccurs=1)
            if ( ftMd != null && ftMd.getAbstract( null ) != null ) {
                writer.writeStartElement( WFS_NS, "Abstract" );
                writer.writeCharacters( ftMd.getAbstract( null ).getString() );
                writer.writeEndElement();
            }

            // wfs:Keywords (minOccurs=0, maxOccurs=1)
            // if ( ft.getKeywords() != null ) {
            // writer.writeStartElement( WFS_NS, "Keywords" );
            // writer.writeCharacters( ft.getKeywords() );
            // writer.writeEndElement();
            // }

            // wfs:SRS (minOccurs=1, maxOccurs=1)
            ICRS querySrs = querySRS.get( 0 );
            writer.writeStartElement( WFS_NS, "SRS" );
            writer.writeCharacters( querySrs.getAlias() );
            writer.writeEndElement();

            // wfs:Operations (minOccurs=0, maxOccurs=1)
            exportOperations100();

            // wfs:LatLongBoundingBox (minOccurs=0, maxOccurs=unbounded)
            Envelope env = null;
            try {
                FeatureStore fs = service.getStore( ftName );
                env = fs.getEnvelope( ftName );
            } catch ( FeatureStoreException e ) {
                LOG.error( "Error retrieving envelope from FeatureStore: " + e.getMessage(), e );
            }
            if ( env != null ) {
                try {
                    env = transformEnvelopeIntoQuerySrs( querySrs, env );
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

            // wfs:MetadataURL (minOccurs=0, maxOccurs=unbounded)
            List<DatasetMetadata> ftMds = mdProvider.getAllDatasetMetadata( ftName );
            if ( ftMds != null ) {
                for ( DatasetMetadata datasetMetadata : ftMds ) {
                    for ( final MetadataUrl metadataUrl : datasetMetadata.getMetadataUrls() ) {
                        writer.writeStartElement( WFS_NS, "MetadataURL" );
                        writer.writeAttribute( "type", "TC211" );
                        writer.writeAttribute( "format", "XML" );
                        writer.writeCharacters( metadataUrl.getUrl() );
                        writer.writeEndElement();
                    }
                }
            }

            writer.writeEndElement();
        }
        writer.writeEndElement();

        // ogc:Filter_Capabilities
        FilterCapabilitiesExporter.export100( writer );

        writer.writeEndElement();
    }

    private void exportCapability100()
                            throws XMLStreamException {

        writer.writeStartElement( WFS_NS, "Capability" );
        writer.writeStartElement( WFS_NS, "Request" );

        String getURL = OGCFrontController.getHttpGetURL();
        String postURL = OGCFrontController.getHttpPostURL();

        // wfs:GetCapabilities
        if ( isPostSupported( GetCapabilities ) || isGetSupported( GetCapabilities ) ) {
            writer.writeStartElement( WFS_NS, WFSRequestType.GetCapabilities.name() );
            if ( isGetSupported( WFSRequestType.GetCapabilities ) )
                exportGetDCPType100( getURL );
            if ( isPostSupported( WFSRequestType.GetCapabilities ) )
                exportPostDCPType100( postURL );
            writer.writeEndElement();
        }

        // wfs:DescribeFeatureType
        if ( isPostSupported( DescribeFeatureType ) || isGetSupported( DescribeFeatureType ) ) {
            writer.writeStartElement( WFS_NS, WFSRequestType.DescribeFeatureType.name() );
            writer.writeStartElement( WFS_NS, "SchemaDescriptionLanguage" );
            writer.writeStartElement( WFS_NS, "XMLSCHEMA" );
            writer.writeEndElement();
            writer.writeEndElement();
            if ( isGetSupported( WFSRequestType.DescribeFeatureType ) )
                exportGetDCPType100( getURL );
            if ( isPostSupported( WFSRequestType.DescribeFeatureType ) )
                exportPostDCPType100( postURL );
            writer.writeEndElement();
        }

        if ( enableTransactions
             && ( isPostSupported( WFSRequestType.Transaction ) || isGetSupported( WFSRequestType.Transaction ) ) ) {
            // wfs:Transaction
            writer.writeStartElement( WFS_NS, WFSRequestType.Transaction.name() );
            if ( isGetSupported( WFSRequestType.Transaction ) )
                exportGetDCPType100( getURL );
            if ( isPostSupported( WFSRequestType.Transaction ) )
                exportPostDCPType100( postURL );
            writer.writeEndElement();
        }

        // wfs:GetFeature
        if ( isPostSupported( WFSRequestType.GetFeature ) || isGetSupported( WFSRequestType.GetFeature ) ) {
            writer.writeStartElement( WFS_NS, WFSRequestType.GetFeature.name() );
            writer.writeStartElement( WFS_NS, "ResultFormat" );
            writer.writeEmptyElement( WFS_NS, "GML2" );
            writer.writeEndElement();
            if ( isGetSupported( WFSRequestType.GetFeature ) )
                exportGetDCPType100( getURL );
            if ( isPostSupported( WFSRequestType.GetFeature ) )
                exportPostDCPType100( postURL );
            writer.writeEndElement();
        }

        if ( enableTransactions ) {
            // wfs:GetFeatureWithLock
            if ( isPostSupported( WFSRequestType.GetFeatureWithLock )
                 || isGetSupported( WFSRequestType.GetFeatureWithLock ) ) {
                writer.writeStartElement( WFS_NS, WFSRequestType.GetFeatureWithLock.name() );
                writer.writeStartElement( WFS_NS, "ResultFormat" );
                writer.writeEmptyElement( WFS_NS, "GML2" );
                writer.writeEndElement();
                if ( isGetSupported( WFSRequestType.GetFeatureWithLock ) )
                    exportGetDCPType100( getURL );
                if ( isPostSupported( WFSRequestType.GetFeatureWithLock ) )
                    exportPostDCPType100( postURL );
                writer.writeEndElement();
            }

            // wfs:LockFeature
            if ( isPostSupported( WFSRequestType.LockFeature ) || isGetSupported( WFSRequestType.LockFeature ) ) {
                writer.writeStartElement( WFS_NS, WFSRequestType.LockFeature.name() );
                if ( isGetSupported( WFSRequestType.LockFeature ) )
                    exportGetDCPType100( getURL );
                if ( isPostSupported( WFSRequestType.LockFeature ) )
                    exportPostDCPType100( postURL );
                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void exportService100()
                            throws XMLStreamException {

        writer.writeStartElement( WFS_NS, "Service" );

        if ( mdProvider.getServiceIdentification() != null
             && mdProvider.getServiceIdentification().getTitle( null ) != null ) {
            // wfs:Name (type="string")
            writeElement( writer, WFS_NS, "Name", mdProvider.getServiceIdentification().getTitle( null ).getString() );
            // wfs:Title (type="string)
            writeElement( writer, WFS_NS, "Title", mdProvider.getServiceIdentification().getTitle( null ).getString() );
        } else {
            writeElement( writer, WFS_NS, "Name", "" );
            writeElement( writer, WFS_NS, "Title", "" );
        }

        if ( mdProvider.getServiceIdentification() != null
             && mdProvider.getServiceIdentification().getAbstract( null ) != null ) {
            // wfs:Abstract
            writeElement( writer, WFS_NS, "Abstract",
                          mdProvider.getServiceIdentification().getAbstract( null ).getString() );
        }

        // wfs:Keywords

        // wfs:OnlineResource (type=???)
        if ( mdProvider.getServiceProvider() != null && mdProvider.getServiceProvider().getProviderSite() != null ) {
            writeElement( writer, WFS_NS, "OnlineResource", mdProvider.getServiceProvider().getProviderSite() );
        }

        // wfs:Fees
        if ( mdProvider.getServiceIdentification() != null && mdProvider.getServiceIdentification().getFees() != null ) {
            writeElement( writer, WFS_NS, "Fees", mdProvider.getServiceIdentification().getFees() );
        }

        // wfs:AccessConstraints

        writer.writeEndElement();
    }

    private void exportOperations100()
                            throws XMLStreamException {
        writer.writeStartElement( WFS_NS, "Operations" );
        writer.writeEmptyElement( WFS_NS, "Query" );
        if ( enableTransactions ) {
            writer.writeEmptyElement( WFS_NS, "Insert" );
            writer.writeEmptyElement( WFS_NS, "Update" );
            writer.writeEmptyElement( WFS_NS, "Delete" );
        }
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
    void export110()
                            throws XMLStreamException {

        writer.setDefaultNamespace( WFS_NS );
        writer.writeStartElement( WFS_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "1.1.0" );
        writer.writeDefaultNamespace( WFS_NS );
        writer.writeNamespace( OWS_PREFIX, OWS_NS );
        writer.writeNamespace( OGC_PREFIX, OGCNS );
        writer.writeNamespace( GML_PREFIX, GMLNS );
        writer.writeNamespace( XLINK_PREFIX, XLNNS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        writer.writeAttribute( XSINS, "schemaLocation", WFS_NS + " " + WFS_110_SCHEMA_URL );

        // ows:ServiceIdentification
        if ( sections == null || sections.contains( "SERVICEIDENTIFICATION" ) ) {
            exportServiceIdentification100( writer, mdProvider.getServiceIdentification(), "WFS", offeredVersions );
        }

        // ows:ServiceProvider
        if ( sections == null || sections.contains( "SERVICEPROVIDER" ) ) {
            exportServiceProvider100( writer, mdProvider.getServiceProvider() );
        }

        // ows:OperationsMetadata
        if ( sections == null || sections.contains( "OPERATIONSMETADATA" ) ) {
            List<Operation> operations = new ArrayList<Operation>();
            List<DCP> getAndPost = null;
            List<DCP> get = null;
            List<DCP> post = null;
            try {
                getAndPost = Collections.singletonList( new DCP( new URL( getHttpGetURL() ), new URL( getHttpPostURL() ) ) );
                post = singletonList( new DCP( null, new URL( getHttpPostURL() ) ) );
                get = singletonList( new DCP( new URL( getHttpGetURL() ), null ) );
            } catch ( MalformedURLException e ) {
                // should never happen
            }

            // DescribeFeatureType
            List<Domain> params = new ArrayList<Domain>();
            List<String> outputFormats = new ArrayList<String>( master.getOutputFormats() );
            params.add( new Domain( "outputFormat", outputFormats ) );
            addOperation( DescribeFeatureType, params, getAndPost, post, get, operations );

            // GetCapabilities
            params = new ArrayList<Domain>();
            params.add( new Domain( "AcceptVersions", offeredVersionStrings ) );
            params.add( new Domain( "AcceptFormats", Collections.singletonList( "text/xml" ) ) );
            // List<String> sections = new ArrayList<String>();
            // sections.add( "ServiceIdentification" );
            // sections.add( "ServiceProvider" );
            // sections.add( "OperationsMetadata" );
            // sections.add( "FeatureTypeList" );
            // sections.add( "Filter_Capabilities" );
            // params.add( new Domain( "Sections", sections ) );
            addOperation( GetCapabilities, params, getAndPost, post, get, operations );

            // GetFeature
            params = new ArrayList<Domain>();
            params.add( new Domain( "resultType", Arrays.asList( new String[] { "results", "hits" } ) ) );
            params.add( new Domain( "outputFormat", outputFormats ) );
            addOperation( GetFeature, params, getAndPost, post, get, operations );

            // GetFeatureWithLock
            if ( enableTransactions ) {
                params = new ArrayList<Domain>();
                params.add( new Domain( "resultType", Arrays.asList( new String[] { "results", "hits" } ) ) );
                params.add( new Domain( "outputFormat", outputFormats ) );
                addOperation( GetFeatureWithLock, params, getAndPost, post, get, operations );
            }

            // GetGmlObject
            params = new ArrayList<Domain>();
            params.add( new Domain( "outputFormat", outputFormats ) );
            addOperation( GetGmlObject, params, getAndPost, post, get, operations );

            if ( enableTransactions ) {

                // LockFeature
                params = new ArrayList<Domain>();
                params.add( new Domain( "lockAction", Arrays.asList( new String[] { "ALL", "SOME" } ) ) );
                addOperation( LockFeature, params, getAndPost, post, get, operations );

                // Transaction
                params = new ArrayList<Domain>();
                params.add( new Domain( "inputFormat", outputFormats ) );
                params.add( new Domain( "idgen", Arrays.asList( new String[] { "GenerateNew", "UseExisting",
                                                                              "ReplaceDuplicate" } ) ) );
                params.add( new Domain( "releaseAction", Arrays.asList( new String[] { "ALL", "SOME" } ) ) );
                addOperation( Transaction, params, getAndPost, post, get, operations );
            }
            Map<String, List<OMElement>> versionToExtendedCaps = mdProvider.getExtendedCapabilities();
            exportOperationsMetadata100( writer,
                                         new OperationsMetadata( operations, null, null,
                                                                 versionToExtendedCaps.get( "1.1.0" ) ) );
        }

        // wfs:FeatureTypeList
        if ( sections == null || sections.contains( "FEATURETYPELIST" ) ) {
            writer.writeStartElement( WFS_NS, "FeatureTypeList" );
            for ( FeatureType ft : servedFts ) {
                QName ftName = ft.getName();
                DatasetMetadata ftMd = mdProvider.getDatasetMetadata( ftName );
                writer.writeStartElement( WFS_NS, "FeatureType" );
                // wfs:Name
                writer.writeStartElement( WFS_NS, "Name" );
                String prefix = ftName.getPrefix();
                if ( prefix == null || prefix.equals( "" ) ) {
                    LOG.warn( "Feature type '" + ftName + "' has no prefix!? This should not happen." );
                    prefix = "app";
                }
                if ( !"".equals( ftName.getNamespaceURI() ) ) {
                    // TODO what about the namespace prefix?
                    writer.writeNamespace( prefix, ftName.getNamespaceURI() );
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                } else {
                    writer.writeCharacters( ftName.getLocalPart() );
                }
                writer.writeEndElement();

                // wfs:Title
                writer.writeStartElement( WFS_NS, "Title" );
                if ( ftMd != null && ftMd.getTitle( null ) != null ) {
                    writer.writeCharacters( ftMd.getTitle( null ).getString() );
                } else {
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                }
                writer.writeEndElement();

                // wfs:Abstract (minOccurs=0, maxOccurs=1)
                if ( ftMd != null && ftMd.getAbstract( null ) != null ) {
                    writer.writeStartElement( WFS_NS, "Abstract" );
                    writer.writeCharacters( ftMd.getAbstract( null ).getString() );
                    writer.writeEndElement();
                }

                // ows:Keywords (minOccurs=0, maxOccurs=unbounded)
                // writer.writeStartElement( OWS_NS, "Keywords" );
                // writer.writeCharacters( "keywords" );
                // writer.writeEndElement();

                // wfs:DefaultSRS / wfs:NoSRS
                FeatureStore fs = service.getStore( ftName );
                writeElement( writer, WFS_NS, "DefaultSRS", querySRS.get( 0 ).getAlias() );

                // wfs:OtherSRS
                for ( int i = 1; i < querySRS.size(); i++ ) {
                    writeElement( writer, WFS_NS, "OtherSRS", querySRS.get( i ).getAlias() );
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
                        env = transformer.transform( env );
                    } catch ( Exception e ) {
                        LOG.error( "Cannot transform feature type envelope to WGS84." );
                    }
                } else {
                    env = new SimpleGeometryFactory().createEnvelope( -180, -90, 180, 90,
                                                                      CRSManager.getCRSRef( "EPSG:4326" ) );
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

                // wfs:MetadataURL (minOccurs=0, maxOccurs=unbounded)
                List<DatasetMetadata> ftMds = mdProvider.getAllDatasetMetadata( ftName );
                if ( ftMds != null ) {
                    for ( DatasetMetadata datasetMetadata : ftMds ) {
                        for ( final MetadataUrl metadataUrl : datasetMetadata.getMetadataUrls() ) {
                            writer.writeStartElement( WFS_NS, "MetadataURL" );
                            String type = metadataUrl.getType() != null ? metadataUrl.getType() : "19139";
                            String format = metadataUrl.getFormat() != null ? metadataUrl.getFormat() : "text/xml";
                            writer.writeAttribute( "type", type );
                            writer.writeAttribute( "format", format );
                            writer.writeCharacters( metadataUrl.getUrl() );
                            writer.writeEndElement();
                        }
                    }
                }

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
    }

    private void writeOutputFormats110( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( WFS_NS, "OutputFormats" );
        for ( String format : master.getOutputFormats() ) {
            writer.writeStartElement( WFS_NS, "Format" );
            writer.writeCharacters( format );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Produces a <code>WFS_Capabilities</code> document that complies to the WFS 2.0.0 specification.
     * 
     * @throws XMLStreamException
     */
    void export200()
                            throws XMLStreamException {

        writer.setDefaultNamespace( WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "WFS_Capabilities" );
        writer.writeAttribute( "version", "2.0.0" );
        writer.writeDefaultNamespace( WFS_200_NS );
        // for QNames in QueryExpressions constraint
        writer.writeNamespace( "wfs", WFS_200_NS );
        writer.writeNamespace( OWS_PREFIX, OWS110_NS );
        writer.writeNamespace( OGC_PREFIX, OGCNS );
        writer.writeNamespace( FES_PREFIX, FES_20_NS );
        writer.writeNamespace( GML_PREFIX, GMLNS );
        writer.writeNamespace( XLINK_PREFIX, XLN_NS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        writer.writeAttribute( XSINS, "schemaLocation", WFS_200_NS + " " + WFS_200_SCHEMA_URL );

        // ows:ServiceIdentification
        if ( sections == null || sections.contains( "ServiceIdentification" ) ) {
            exportServiceIdentification110New( writer, mdProvider.getServiceIdentification(), "WFS", offeredVersions );
        }

        // ows:ServiceProvider
        if ( sections == null || sections.contains( "ServiceProvider" ) ) {
            exportServiceProvider110New( writer, mdProvider.getServiceProvider() );
        }

        // ows:OperationsMetadata
        if ( sections == null || sections.contains( "OPERATIONSMETADATA" ) ) {
            List<Operation> operations = new ArrayList<Operation>();
            List<DCP> getAndPost = null;
            List<DCP> post = null;
            List<DCP> get = null;
            try {
                getAndPost = singletonList( new DCP( new URL( getHttpGetURL() ), new URL( getHttpPostURL() ) ) );
                post = singletonList( new DCP( null, new URL( getHttpPostURL() ) ) );
                get = singletonList( new DCP( new URL( getHttpGetURL() ), null ) );
            } catch ( MalformedURLException e ) {
                // should never happen
            }

            // GetCapabilities
            List<Domain> params = new ArrayList<Domain>();
            params.add( new Domain( "AcceptVersions", offeredVersionStrings ) );
            params.add( new Domain( "AcceptFormats", Collections.singletonList( "text/xml" ) ) );
            List<String> sections = new ArrayList<String>();
            sections.add( "ServiceIdentification" );
            sections.add( "ServiceProvider" );
            sections.add( "OperationsMetadata" );
            sections.add( "FeatureTypeList" );
            sections.add( "Filter_Capabilities" );
            params.add( new Domain( "Sections", sections ) );

            // GetCapabilities
            addOperation( GetCapabilities, params, getAndPost, post, get, operations );

            // DescribeFeatureType
            addOperation( DescribeFeatureType, getAndPost, post, get, operations );

            // ListStoredQueries
            addOperation( ListStoredQueries, getAndPost, post, get, operations );

            // DescribeStoredQueries
            addOperation( DescribeStoredQueries, getAndPost, post, get, operations );

            // GetFeature
            addOperation( GetFeature, getAndPost, post, get, operations );

            // GetPropertyValue
            addOperation( GetPropertyValue, getAndPost, post, get, operations );

            // CreateStoredQuery

            if ( master.getStoredQueryHandler().isManagedStoredQuerySupported() ) {
                List<Domain> createStoredQueryParams = new ArrayList<Domain>();
                createStoredQueryParams.add( new Domain( "language", Collections.singletonList(
                                "urn:ogc:def:queryLanguage:OGC-WFS::WFSQueryExpression" ) ) );
                addOperation( CreateStoredQuery, createStoredQueryParams, getAndPost, post, get, operations );
                // DropStoredQuery
                addOperation( DropStoredQuery, getAndPost, post, get, operations );
            }
            
            if ( enableTransactions ) {
                // Transaction
                List<Domain> constraints = new ArrayList<Domain>();
                constraints.add( new Domain( "AutomaticDataLocking", "TRUE" ) );
                constraints.add( new Domain( "PreservesSiblingOrder", "TRUE" ) );
                if ( supportedEncodings.isEncodingSupported( Transaction, "POST" ) )
                    operations.add( new Operation( Transaction.name(), post, null, constraints, null ) );

                // GetFeatureWithLock
                addOperation( GetFeatureWithLock, getAndPost, post, get, operations );

                // LockFeature
                addOperation( LockFeature, getAndPost, post, get, operations );
            }

            // global parameter domains
            List<Domain> globalParams = new ArrayList<Domain>();

            // version
            globalParams.add( new Domain( "version", offeredVersionStrings ) );

            // srsName
            List<String> srsNames = new ArrayList<String>();
            for ( ICRS crs : querySRS ) {
                srsNames.add( crs.getAlias() );
            }
            globalParams.add( new Domain( "srsName", srsNames ) );

            // outputFormat
            globalParams.add( new Domain( "outputFormat", new ArrayList<String>( master.getOutputFormats() ) ) );

            // resolve
            List<String> resolve = new ArrayList<String>();
            resolve.add( "none" );
            resolve.add( "local" );
            resolve.add( "remote" );
            resolve.add( "all" );
            globalParams.add( new Domain( "resolve", resolve ) );

            List<Domain> constraints = new ArrayList<Domain>();

            // Service constraints
            constraints.add( new Domain( "ImplementsSimpleWFS", "TRUE" ) );
            constraints.add( new Domain( "ImplementsBasicWFS", "TRUE" ) );
            if ( enableTransactions ) {
                constraints.add( new Domain( "ImplementsTransactionalWFS", "TRUE" ) );
                constraints.add( new Domain( "ImplementsLockingWFS", "TRUE" ) );
            } else {
                constraints.add( new Domain( "ImplementsTransactionalWFS", "FALSE" ) );
                constraints.add( new Domain( "ImplementsLockingWFS", "FALSE" ) );
            }
            constraints.add( new Domain( "KVPEncoding", "TRUE" ) );
            constraints.add( new Domain( "XMLEncoding", "TRUE" ) );
            if ( master.isSoapSupported() ) {
                constraints.add( new Domain( "SOAPEncoding", "TRUE" ) );
            } else {
                constraints.add( new Domain( "SOAPEncoding", "FALSE" ) );
            }
            constraints.add( new Domain( "ImplementsInheritance", "FALSE" ) );
            constraints.add( new Domain( "ImplementsRemoteResolve", "FALSE" ) );
            if ( master.isEnableResponsePaging() ) {
                constraints.add( new Domain( "ImplementsResultPaging", "TRUE" ) );
                constraints.add( new Domain( "PagingIsTransactionSafe", "FALSE" ) );
            } else
                constraints.add( new Domain( "ImplementsResultPaging", "FALSE" ) );
            constraints.add( new Domain( "ImplementsStandardJoins", "FALSE" ) );
            constraints.add( new Domain( "ImplementsSpatialJoins", "FALSE" ) );
            constraints.add( new Domain( "ImplementsTemporalJoins", "FALSE" ) );
            constraints.add( new Domain( "ImplementsFeatureVersioning", "FALSE" ) );
            if ( master.getStoredQueryHandler().isManagedStoredQuerySupported() ) {
                constraints.add( new Domain( "ManageStoredQueries", "TRUE" ) );
            } else {
                constraints.add( new Domain( "ManageStoredQueries", "FALSE" ) );
            }

            // capacity constraints
            if ( master.getQueryMaxFeatures() != -1 ) {
                constraints.add( new Domain( "CountDefault", "" + master.getQueryMaxFeatures() ) );
            }
            if ( master.getResolveTimeOutInSeconds() != null ) {
                constraints.add( new Domain( "ResolveTimeoutDefault", master.getResolveTimeOutInSeconds().toString() ) );
            }
            constraints.add( new Domain( "ResolveLocalScope", "*" ) );

            List<String> queryExprs = new ArrayList<String>();
            queryExprs.add( "wfs:Query" );
            queryExprs.add( "wfs:StoredQuery" );
            constraints.add( new Domain( "QueryExpressions", queryExprs ) );

            OperationsMetadata operationsMd = new OperationsMetadata(
                                                                      operations,
                                                                      globalParams,
                                                                      constraints,
                                                                      mdProvider.getExtendedCapabilities().get( "2.0.0" ) );
            exportOperationsMetadata110( writer, operationsMd );
        }

        // wfs:WSDL
        if ( sections == null || sections.contains( "WSDL" ) ) {
            // TODO
        }

        // wfs:FeatureTypeList
        if ( sections == null || sections.contains( "FeatureTypeList" ) ) {
            writer.writeStartElement( WFS_200_NS, "FeatureTypeList" );
            for ( FeatureType ft : servedFts ) {
                QName ftName = ft.getName();
                DatasetMetadata ftMd = mdProvider.getDatasetMetadata( ftName );
                writer.writeStartElement( WFS_200_NS, "FeatureType" );
                // wfs:Name
                writer.writeStartElement( WFS_200_NS, "Name" );
                String prefix = ftName.getPrefix();
                if ( prefix == null || prefix.equals( "" ) ) {
                    LOG.warn( "Feature type '" + ftName + "' has no prefix!? This should not happen." );
                    prefix = "app";
                }
                if ( !"".equals( ftName.getNamespaceURI() ) ) {
                    // TODO what about the namespace prefix?
                    writer.writeNamespace( prefix, ftName.getNamespaceURI() );
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                } else {
                    writer.writeCharacters( ftName.getLocalPart() );
                }
                writer.writeEndElement();

                // wfs:Title
                writer.writeStartElement( WFS_200_NS, "Title" );
                if ( ftMd != null && ftMd.getTitle( null ) != null ) {
                    writer.writeCharacters( ftMd.getTitle( null ).getString() );
                } else {
                    writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                }
                writer.writeEndElement();

                // wfs:Abstract (minOccurs=0, maxOccurs=1)
                if ( ftMd != null && ftMd.getAbstract( null ) != null ) {
                    writer.writeStartElement( WFS_200_NS, "Abstract" );
                    writer.writeCharacters( ftMd.getAbstract( null ).getString() );
                    writer.writeEndElement();
                }

                exportKeywords200( ftMd );

                // wfs:DefaultCRS / wfs:NoCRS
                FeatureStore fs = service.getStore( ftName );
                writeElement( writer, WFS_200_NS, "DefaultCRS", querySRS.get( 0 ).getAlias() );

                // wfs:OtherCRS
                for ( int i = 1; i < querySRS.size(); i++ ) {
                    writeElement( writer, WFS_200_NS, "OtherCRS", querySRS.get( i ).getAlias() );
                }

                writeOutputFormats200( writer );

                // ows:WGS84BoundingBox (minOccurs=0, maxOccurs=unbounded)
                Envelope env = null;
                try {
                    env = fs.getEnvelope( ftName );
                } catch ( FeatureStoreException e ) {
                    LOG.error( "Error retrieving envelope from FeatureStore: " + e.getMessage(), e );
                }

                if ( env != null ) {
                    try {
                        env = transformer.transform( env );
                    } catch ( Exception e ) {
                        LOG.error( "Cannot transform feature type envelope to WGS84." );
                    }
                } else {
                    env = new SimpleGeometryFactory().createEnvelope( -180, -90, 180, 90,
                                                                      CRSManager.getCRSRef( "EPSG:4326" ) );
                }

                writer.writeStartElement( OWS110_NS, "WGS84BoundingBox" );
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
                writer.writeStartElement( OWS110_NS, "LowerCorner" );
                writer.writeCharacters( formatter.format( minX ) + " " + formatter.format( minY ) );
                writer.writeEndElement();
                writer.writeStartElement( OWS110_NS, "UpperCorner" );
                writer.writeCharacters( formatter.format( maxX ) + " " + formatter.format( maxY ) );
                writer.writeEndElement();
                writer.writeEndElement();

                // wfs:MetadataURL (minOccurs=0, maxOccurs=unbounded)
                List<DatasetMetadata> ftMds = mdProvider.getAllDatasetMetadata( ftName );
                if ( ftMds != null ) {
                    for ( DatasetMetadata datasetMetadata : ftMds ) {
                        for ( final MetadataUrl metadataUrl : datasetMetadata.getMetadataUrls() ) {
                            writer.writeEmptyElement( WFS_200_NS, "MetadataURL" );
                            writer.writeAttribute( XLN_NS, "href", metadataUrl.getUrl() );
                        }
                    }
                }

                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        // fes:Filter_Capabilities
        if ( sections == null || sections.contains( "Filter_Capabilities" ) ) {
            FilterCapabilitiesExporter.export200( writer );
        }

        writer.writeEndElement();
    }

    private void exportKeywords200( DatasetMetadata ftMd )
                            throws XMLStreamException {
        if ( ftMd != null ) {
            List<Pair<List<LanguageString>, CodeType>> keywords = ftMd.getKeywords();
            if ( keywords != null && !keywords.isEmpty() ) {
                writer.writeStartElement( OWS110_NS, "Keywords" );
                for ( Pair<List<LanguageString>, CodeType> keywordsPerCodeType : keywords ) {
                    for ( LanguageString keyword : keywordsPerCodeType.getFirst() ) {
                        writer.writeStartElement( OWS110_NS, "Keyword" );
                        writer.writeCharacters( keyword.getString() );
                        writer.writeEndElement();
                    }
                }
                writer.writeEndElement();
            }
        }
    }

    private void addOperation( WFSRequestType wfsRequestType, List<DCP> getAndPost, List<DCP> post, List<DCP> get,
                               List<Operation> operations ) {
        addOperation( wfsRequestType, null, getAndPost, post, get, operations );
    }

    private void addOperation( WFSRequestType wfsRequestType, List<Domain> params, List<DCP> getAndPost,
                               List<DCP> post, List<DCP> get, List<Operation> operations ) {
        if ( isGetSupported( wfsRequestType ) && isPostSupported( wfsRequestType ) )
            operations.add( new Operation( wfsRequestType.name(), getAndPost, params, null, null ) );
        else if ( isPostSupported( wfsRequestType ) )
            operations.add( new Operation( wfsRequestType.name(), post, params, null, null ) );
        else if ( isGetSupported( wfsRequestType ) )
            operations.add( new Operation( wfsRequestType.name(), get, params, null, null ) );
    }

    private void writeOutputFormats200( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( WFS_200_NS, "OutputFormats" );
        for ( String format : master.getOutputFormats() ) {
            writer.writeStartElement( WFS_200_NS, "Format" );
            writer.writeCharacters( format );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private boolean isGetSupported( WFSRequestType requestType ) {
        return supportedEncodings.isEncodingSupported( requestType, "KVP" );
    }

    private boolean isPostSupported( WFSRequestType requestType ) {
        return supportedEncodings.isEncodingSupported( requestType, "XML" )
               || supportedEncodings.isEncodingSupported( requestType, "SOAP" );
    }

    private Envelope transformEnvelopeIntoQuerySrs( ICRS querySrs, Envelope env )
                            throws TransformationException, UnknownCRSException {
        GeometryTransformer transformer = new GeometryTransformer( querySrs );
        return transformer.transform( env );
    }

}