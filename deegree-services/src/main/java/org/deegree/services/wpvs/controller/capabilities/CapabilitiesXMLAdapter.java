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

package org.deegree.services.wpvs.controller.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.services.jaxb.main.DCPType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.deegree.services.jaxb.wpvs.AbstractDataType;
import org.deegree.services.jaxb.wpvs.ColormapDatasetConfig;
import org.deegree.services.jaxb.wpvs.DEMDatasetConfig;
import org.deegree.services.jaxb.wpvs.DEMTextureDatasetConfig;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.deegree.services.jaxb.wpvs.RenderableDatasetConfig;
import org.deegree.services.jaxb.wpvs.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CapabilitiesXMLAdapter</code> class exports the capabilities of a wpvs.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CapabilitiesXMLAdapter extends OWSCapabilitiesXMLAdapter {

    private final static Logger LOG = LoggerFactory.getLogger( CapabilitiesXMLAdapter.class );

    private static final String WPVS_NS = "http://www.opengis.net/wpvs/1.0.0-pre";

    private static final String WPVS_PREFIX = "wpvs";

    /**
     * @param writer
     * @param request
     * @param serviceID
     * @param serviceProvider
     * @param operations
     * @param dcp
     * @param serviceConfig
     * @throws XMLStreamException
     */
    public void export040( XMLStreamWriter writer, GetCapabilities request, ServiceIdentificationType serviceID,
                           ServiceProviderType serviceProvider, List<String> operations, DCPType dcp,
                           ServiceConfiguration serviceConfig )
                            throws XMLStreamException {

        writer.setPrefix( WPVS_PREFIX, WPVS_NS );
        writer.setPrefix( OWS_PREFIX, OWS110_NS );
        writer.setPrefix( XSI_PREFIX, XSINS );
        writer.setPrefix( XLINK_PREFIX, XLNNS );

        writer.writeStartElement( WPVS_NS, "Capabilities" );
        writer.writeAttribute( "service", "WPVS" );
        writer.writeAttribute( "version", "0.5.0" );
        // writer.writeAttribute( XSINS, "schemaLocation",
        // "http://www.opengis.net/wpvs/1.0.0-pre http://schemas.opengis.net/ows/1.1.0/owsGetCapabilities.xsd" );

        List<Version> versions = new ArrayList<Version>();
        Set<String> sections = request.getSections();
        boolean all = sections.isEmpty() || sections.contains( "All" );
        if ( all || sections.contains( "ServiceIdentification" ) ) {
            versions.add( new Version( 0, 4, 0 ) );
            if ( serviceID != null ) {
                exportServiceIdentification110( writer, serviceID, "WPVS", versions );
            }
        }
        if ( all || sections.contains( "ServiceProvider" ) && serviceProvider != null ) {
            exportServiceProvider110( writer, serviceProvider );
        }
        if ( all || sections.contains( "OperationsMetadata" ) ) {
            exportOperationsMetadata110( writer, operations, dcp );
        }
        if ( all || sections.contains( "Dataset" ) ) {
            exportDatasets( writer, serviceConfig.getDatasetDefinitions() );
        }

        writer.writeEndElement(); // Capabilities
    }

    /**
     * @param dataSetDefinitions
     * @throws XMLStreamException
     */
    private static void exportDatasets( XMLStreamWriter writer, DatasetDefinitions datasetDefinitions )
                            throws XMLStreamException {
        if ( datasetDefinitions == null ) {
            LOG.warn( "No dataset definitions given, hence no data set section will be exported." );
        }
        writer.writeStartElement( WPVS_NS, "Dataset" );
        writer.writeAttribute( "queryable", "false" );
        if ( datasetDefinitions != null ) {
            exportTextureDataset( writer, datasetDefinitions.getDEMTextureDataset() );
            exportColormapDataset( writer, datasetDefinitions.getColormapDataset() );
            exportRenderableDatasets( writer, datasetDefinitions.getRenderableDataset() );
            exportElevationModel( writer, datasetDefinitions.getDEMDataset() );
        }

        writer.writeEndElement();// WPVS_NS, "Dataset"

    }

    /**
     * @param writer
     * @param renderables
     * @throws XMLStreamException
     */
    private static void exportRenderableDatasets( XMLStreamWriter writer, List<RenderableDatasetConfig> renderables )
                            throws XMLStreamException {
        if ( renderables != null ) {
            // TODO Rutger: Is this supposed to be like this?
            for ( RenderableDatasetConfig renderable : renderables ) {
                if ( renderable != null ) {
                    writer.writeStartElement( WPVS_NS, "Dataset" );
                    writer.writeAttribute( "queryable", "true" );
                    exportAbstractDataType( writer, renderable );
                    writer.writeEndElement();
                }
            }
        }
    }

    /**
     * @param writer
     * @param dem
     * @throws XMLStreamException
     */
    private static void exportElevationModel( XMLStreamWriter writer, DEMDatasetConfig dem )
                            throws XMLStreamException {
        if ( dem != null ) {
            writer.writeStartElement( WPVS_NS, "ElevationModel" );
            writer.writeAttribute( "queryable", "true" );
            exportAbstractDataType( writer, dem );
            writer.writeEndElement();// WPVS_NS, "ElevationModel"
        }

    }

    /**
     * @param writer
     * @param textureDataset
     * @throws XMLStreamException
     */
    private static void exportTextureDataset( XMLStreamWriter writer, List<DEMTextureDatasetConfig> textureDatasets )
                            throws XMLStreamException {
        if ( textureDatasets != null && !textureDatasets.isEmpty() ) {
            for ( DEMTextureDatasetConfig td : textureDatasets ) {
                if ( td != null ) {
                    writer.writeStartElement( WPVS_NS, "Dataset" );
                    writer.writeAttribute( "queryable", "true" );
                    exportAbstractDataType( writer, td );
                    writer.writeEndElement();
                }
            }
        }
    }

    /**
     * @param writer
     * @param colormapDatasets
     * @throws XMLStreamException
     */
    private static void exportColormapDataset( XMLStreamWriter writer, List<ColormapDatasetConfig> colormapDatasets )
                            throws XMLStreamException {
        if ( colormapDatasets != null && !colormapDatasets.isEmpty() ) {
            for ( ColormapDatasetConfig cd : colormapDatasets ) {
                if ( cd != null ) {
                    writer.writeStartElement( WPVS_NS, "Dataset" );
                    writer.writeAttribute( "queryable", "true" );
                    exportAbstractDataType( writer, cd );
                    writer.writeEndElement();
                }
            }
        }
    }

    /**
     * @param writer
     * @param td
     * @throws XMLStreamException
     */
    private static void exportAbstractDataType( XMLStreamWriter writer, AbstractDataType abstractDatatype )
                            throws XMLStreamException {
        if ( abstractDatatype != null ) {
            // try to do as much order as the wms_capabilities_1_3_0.xsd
            writeOptionalElement( writer, WPVS_NS, "Name", abstractDatatype.getName() );
            writeElement( writer, WPVS_NS, "Title", abstractDatatype.getTitle() );
            writeOptionalElement( writer, WPVS_NS, "Abstract", abstractDatatype.getAbstract() );
            exportKeyWords110( writer, abstractDatatype.getKeywords() );
            // exportBoundingBoxType110( writer, abstractDatatype.getBoundingBox() );
            exportSimpleStrings( writer, abstractDatatype.getMetadataURL(), WPVS_NS, "MetadataURL" );
            // exportScales( writer, abstractDatatype.getScaleDenominators() );
        }
    }
}
