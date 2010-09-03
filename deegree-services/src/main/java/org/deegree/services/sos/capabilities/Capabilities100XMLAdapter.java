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
package org.deegree.services.sos.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.observation.model.Offering;
import org.deegree.observation.persistence.ObservationDatastoreException;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.services.jaxb.main.CodeType;
import org.deegree.services.jaxb.main.DCPType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.KeywordsType;
import org.deegree.services.jaxb.main.LanguageStringType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;

/**
 * This is an xml adapter for SOS 1.0.0 Capabilities documents.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Capabilities100XMLAdapter extends OWSCapabilitiesXMLAdapter {

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    private static final String OWS_PREFIX = "ows";

    private static final String SOS_NS = "http://www.opengis.net/sos/1.0";

    private static final String SOS_SCHEMA = "http://schemas.opengis.net/sos/1.0.0/sosAll.xsd";

    private static final String SOS_PREFIX = "sos";

    private static final String GML_PREFIX = "gml";

    private static final String GML_NS = "http://www.opengis.net/gml";

    /**
     * The sections of the Capabilities document.
     */
    public enum Sections {
        /***/
        ServiceIdentification, /***/
        ServiceProvider, /***/
        OperationsMetadata, /***/
        FilterCapabilities, /***/
        Contents
    }

    /**
     * Export an SOS Capabilities document from a list of {@link ObservationOffering}s.
     * 
     * @param sections
     * @param offerings
     *            all offerings of this SOS web service
     * @param serviceMetadata
     *            metadata for the service
     * @param writer
     * @throws XMLStreamException
     * @throws ObservationDatastoreException
     */
    public static void export( Set<Sections> sections, List<Offering> offerings,
                               DeegreeServicesMetadataType serviceMetadata, ServiceIdentificationType identification,
                               XMLStreamWriter writer )
                            throws XMLStreamException, ObservationDatastoreException {
        writer.setPrefix( SOS_PREFIX, SOS_NS );
        writer.setPrefix( OWS_PREFIX, OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xsi", XSINS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartElement( SOS_NS, "Capabilities" );
        writer.writeAttribute( XSINS, "schemaLocation", SOS_NS + " " + SOS_SCHEMA );
        writer.writeAttribute( "version", "1.0.0" );

        if ( sections.contains( Sections.ServiceIdentification ) ) {
            exportServiceIdentification( writer, identification );
        }
        if ( sections.contains( Sections.ServiceProvider ) ) {
            exportServiceProvider110( writer, serviceMetadata.getServiceProvider() );
        }
        if ( sections.contains( Sections.OperationsMetadata ) ) {
            exportOperationsMetadata( writer );
        }
        if ( sections.contains( Sections.FilterCapabilities ) ) {
            exportFilterCapabilities( writer );
        }
        if ( sections.contains( Sections.Contents ) ) {
            exportContents( writer, offerings );
        }
        writer.writeEndElement(); // Capabilities
        writer.writeEndDocument();
    }

    private static void exportContents( XMLStreamWriter writer, List<Offering> offerings )
                            throws XMLStreamException, ObservationDatastoreException {
        writer.writeStartElement( SOS_NS, "Contents" );
        writer.writeStartElement( SOS_NS, "ObservationOfferingList" );

        for ( Offering offering : offerings ) {
            Offering100XMLAdapter.export( writer, offering,
                                          ObservationStoreManager.getDatastoreById( offering.getOfferingName() ) );
        }
        writer.writeEndElement(); // ObservationOfferingList
        writer.writeEndElement(); // Contents
    }

    private static void exportOperationsMetadata( XMLStreamWriter writer )
                            throws XMLStreamException {
        List<String> operations = new LinkedList<String>();
        operations.add( "GetCapabilities" );
        operations.add( "DescribeSensor" );
        operations.add( "GetObservation" );

        // TODO use the configured one to override
        DCPType dcp = new DCPType();
        dcp.setHTTPGet( OGCFrontController.getHttpGetURL() );
        dcp.setHTTPPost( OGCFrontController.getHttpPostURL() );

        writer.writeStartElement( OWS110_NS, "OperationsMetadata" );

        for ( String operation : operations ) {
            writer.writeStartElement( OWS110_NS, "Operation" );
            writer.writeAttribute( "name", operation );
            exportDCP( writer, dcp, OWS110_NS );
            if ( operation.equals( "DescribeSensor" ) ) {
                writer.writeStartElement( OWS110_NS, "Parameter" );
                writer.writeAttribute( "name", "outputFormat" );
                writer.writeStartElement( OWS110_NS, "AllowedValues" );
                writeElement( writer, OWS110_NS, "Value", "text/xml;subtype=\"sensorML/1.0.1\"" );
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private static void exportFilterCapabilities( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( SOS_NS, "Filter_Capabilities" );

        exportSpatialFilterCapabilities( writer );
        exportTemporalFilterCapabilities( writer );
        exportScalarFilterCapabilities( writer );

        writer.writeStartElement( OGC_NS, "Id_Capabilities" );
        writeElement( writer, OGC_NS, "EID", "" );
        writer.writeEndElement(); // Id_Capabilities

        writer.writeEndElement(); // Filter_Capabilities
    }

    private static void exportScalarFilterCapabilities( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( OGC_NS, "Scalar_Capabilities" );
        writer.writeStartElement( OGC_NS, "ComparisonOperators" );
        for ( String type : new String[] { "LessThan", "LessThanEqualTo", "EqualTo", "GreaterThanEqualTo",
                                          "GreaterThan", "NotEqualTo", "Between", "NullCheck" } ) {
            writeElement( writer, OGC_NS, "ComparisonOperator", type );
        }
        writer.writeEndElement(); // ComparisonOperators
        writer.writeEndElement(); // Scalar_Capabilities

    }

    private static void exportSpatialFilterCapabilities( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( OGC_NS, "Spatial_Capabilities" );
        writer.writeStartElement( OGC_NS, "GeometryOperands" );
        writeElement( writer, OGC_NS, "GeometryOperand", "gml:Envelope" );
        // TODO add more operands
        writer.writeEndElement(); // GeometryOperands
        writer.writeStartElement( OGC_NS, "SpatialOperators" );
        writeElement( writer, OGC_NS, "SpatialOperator", "" );
        // TODO add operators
        writer.writeEndElement(); // SpatialOperators
        writer.writeEndElement(); // Spatial_Capabilities
    }

    private static void exportTemporalFilterCapabilities( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( OGC_NS, "Temporal_Capabilities" );

        writer.writeStartElement( OGC_NS, "TemporalOperands" );
        writeElement( writer, OGC_NS, "TemporalOperand", GML_PREFIX + ":TimeInstant" );
        writeElement( writer, OGC_NS, "TemporalOperand", GML_PREFIX + ":TimePeriod" );
        writer.writeEndElement(); // TemporalOperands

        writer.writeStartElement( OGC_NS, "TemporalOperators" );
        for ( String type : new String[] { "TM_After", "TM_Before", "TM_During", "TM_Begins", "TM_Ends", "TM_Equals" } ) {
            writeElement( writer, OGC_NS, "TemporalOperator", null, "name", type );
        }
        writer.writeEndElement(); // TemporalOperators
        writer.writeEndElement(); // Temporal_Capabilities
    }

    private static void exportServiceIdentification( XMLStreamWriter writer, ServiceIdentificationType identification )
                            throws XMLStreamException {
        writer.writeStartElement( OWS_NS, "ServiceIdentification" );
        if ( identification != null ) {
            if ( identification.getTitle() != null ) {
                for ( String oneTitle : identification.getTitle() ) {
                    writeElement( writer, OWS_NS, "Title", oneTitle );
                }
            }
            if ( identification.getAbstract() != null ) {
                for ( String oneAbstract : identification.getAbstract() ) {
                    writeElement( writer, OWS_NS, "Abstract", oneAbstract );
                }
            }
            if ( identification.getKeywords() != null ) {
                List<KeywordsType> keywords = identification.getKeywords();
                if ( !keywords.isEmpty() ) {
                    for ( KeywordsType kwt : keywords ) {
                        if ( kwt != null ) {
                            writer.writeStartElement( OWS_NS, "Keywords" );
                            List<LanguageStringType> keyword = kwt.getKeyword();
                            for ( LanguageStringType lst : keyword ) {
                                exportKeyword( writer, lst );
                                // -> keyword [1, n]
                            }
                            // -> type [0,1]
                            exportCodeType( writer, kwt.getType() );
                            writer.writeEndElement();// OWS_NS, "keywords" );
                        }
                    }
                }
            }
        }
        writeElement( writer, OWS_NS, "ServiceType", "observation service" );
        writeElement( writer, OWS_NS, "ServiceTypeVersion", "1.0.0" );

        if ( identification != null ) {
            if ( identification.getFees() != null ) {
                writeElement( writer, OWS_NS, "Fees", identification.getFees() );
                for ( String oneAccessCons : identification.getAccessConstraints() ) {
                    writeElement( writer, OWS_NS, "AccessConstraints", oneAccessCons );
                }
            }
        }

        writer.writeEndElement();
    }

    /**
     * @param writer
     * @param lst
     * @throws XMLStreamException
     */
    public static void exportKeyword( XMLStreamWriter writer, LanguageStringType lst )
                            throws XMLStreamException {
        if ( lst != null ) {
            writeElement( writer, OWS_NS, "Keyword", lst.getValue() );
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
            writeElement( writer, OWS_NS, "Type", null, "codeSpace", ct.getCodeSpace() );
            // --> @codSpace optional
        }
    }

}
