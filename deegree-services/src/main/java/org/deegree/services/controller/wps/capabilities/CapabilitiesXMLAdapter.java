//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

package org.deegree.services.controller.wps.capabilities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.configuration.DCPType;
import org.deegree.services.controller.configuration.DeegreeServicesMetadata;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.services.wps.processdefinition.ProcessDefinition;
import org.deegree.services.wps.processdefinition.ProcessDefinition.Metadata;

/**
 * Responsible for the generation of WPS GetCapabilities response documents.
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
 */
public class CapabilitiesXMLAdapter extends OWSCapabilitiesXMLAdapter {

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    private static final String OWS_PREFIX = "ows";

    private static final String WPS_NS = "http://www.opengis.net/wps/1.0.0";

    private static final String WPS_PREFIX = "wps";

    private static final String GML_PREFIX = "gml";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    private CapabilitiesXMLAdapter() {
        //
    }

    /**
     * @param writer
     * @param processes
     * @param serviceMetadata
     * @param serviceWSDLURL
     *            location of a WSDL document which describes the entire service, may be null
     * @throws XMLStreamException
     */
    public static void export100( XMLStreamWriter writer, ProcessDefinition[] processes,
                                  DeegreeServicesMetadata serviceMetadata, String serviceWSDLURL )
                            throws XMLStreamException {

        writer.setPrefix( WPS_PREFIX, WPS_NS );
        writer.setPrefix( OWS_PREFIX, OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "xlink", XLN_NS );
        writer.setPrefix( "xsi", XSI_NS );

        writer.writeStartElement( WPS_NS, "Capabilities" );
        writer.writeAttribute( "service", "WPS" );
        writer.writeAttribute( "version", "1.0.0" );
        writer.writeAttribute( "xml:lang", "en" );
        writer.writeAttribute( XSI_NS, "schemaLocation",
                               "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_response.xsd" );

        exportServiceIdentification( writer );
        exportServiceProvider110( writer, serviceMetadata.getServiceProvider() );
        exportOperationsMetadata( writer, serviceMetadata );

        exportProcessOfferings( writer, processes );
        exportLanguages( writer );

        if ( serviceWSDLURL != null ) {
            writer.writeStartElement( WPS_NS, "WSDL" );
            writer.writeAttribute( "xlink:href", serviceWSDLURL );
            writer.writeEndElement();
        }

        writer.writeEndElement(); // Capabilities
    }

    private static void exportProcessOfferings( XMLStreamWriter writer, ProcessDefinition[] processes )
                            throws XMLStreamException {

        writer.writeStartElement( WPS_NS, "ProcessOfferings" );
        for ( ProcessDefinition process : processes ) {
            writer.writeStartElement( WPS_NS, "Process" );
            writer.writeAttribute( WPS_NS, "processVersion", process.getProcessVersion() );

            // "ows:Identifier" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( OWS_NS, "Identifier" );
            if ( process.getIdentifier().getCodeSpace() != null ) {
                writer.writeAttribute( "codeSpace", process.getIdentifier().getCodeSpace() );
            }
            writer.writeCharacters( process.getIdentifier().getValue() );
            writer.writeEndElement();

            // "ows:Title" (minOccurs="1", maxOccurs="1")
            if ( process.getTitle() != null ) {
                writer.writeStartElement( OWS_NS, "Title" );
                if ( process.getTitle().getLang() != null ) {
                    writer.writeAttribute( "xml:lang", process.getTitle().getLang() );
                }
                writer.writeCharacters( process.getTitle().getValue() );
                writer.writeEndElement();
            }

            // "ows:Abstract" (minOccurs="0", maxOccurs="1")
            if ( process.getAbstract() != null ) {
                writer.writeStartElement( OWS_NS, "Abstract" );
                if ( process.getAbstract().getLang() != null ) {
                    writer.writeAttribute( "xml:lang", process.getAbstract().getLang() );
                }
                writer.writeCharacters( process.getAbstract().getValue() );
                writer.writeEndElement();
            }

            // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
            if ( process.getMetadata() != null ) {
                for ( Metadata metadata : process.getMetadata() ) {
                    writer.writeStartElement( OWS_NS, "Metadata" );
                    if ( metadata.getAbout() != null ) {
                        writer.writeAttribute( "about", metadata.getAbout() );
                    }
                    if ( metadata.getHref() != null ) {
                        writer.writeAttribute( XLN_NS, "href", metadata.getHref() );
                    }
                    writer.writeEndElement();
                }
            }

            // "wps:Profile" (minOccurs="0", maxOccurs="unbounded")
            if ( process.getProfile() != null ) {
                for ( String profile : process.getProfile() ) {
                    writeElement( writer, WPS_NS, "Profile", profile );
                }
            }

            // "wps:WSDL" (minOccurs="0", maxOccurs="unbounded")
            if ( process.getWSDL() != null ) {
                writeElement( writer, WPS_NS, "WSDL", XLN_NS, "href", process.getWSDL() );
            }

            writer.writeEndElement(); // Process
        }
        writer.writeEndElement(); // ProcessOfferings
    }

    private static void exportOperationsMetadata( XMLStreamWriter writer, DeegreeServicesMetadata serviceMetadata )
                            throws XMLStreamException {
        List<String> operations = new LinkedList<String>();
        operations.add( "GetCapabilities" );
        operations.add( "DescribeProcess" );
        operations.add( "Execute" );

        DCPType dcp = new DCPType();
        dcp.setHTTPGet( OGCFrontController.getHttpGetURL() );
        dcp.setHTTPPost( OGCFrontController.getHttpPostURL() );

        exportOperationsMetadata110( writer, operations, dcp );
    }

    private static void exportServiceIdentification( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( OWS_NS, "ServiceIdentification" );
        writeElement( writer, OWS_NS, "Title", "deegree 3 WPS" );
        writeElement( writer, OWS_NS, "Abstract", "deegree 3 WPS implementation" );
        writeElement( writer, OWS_NS, "ServiceType", "WPS" );
        writeElement( writer, OWS_NS, "ServiceTypeVersion", "1.0.0" );
        writer.writeEndElement();
    }

    private static void exportLanguages( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( WPS_NS, "Languages" );
        writer.writeStartElement( WPS_NS, "Default" );
        writeElement( writer, OWS_NS, "Language", "en" );
        writer.writeEndElement(); // Default
        writer.writeStartElement( WPS_NS, "Supported" );
        writeElement( writer, OWS_NS, "Language", "en" );
        writer.writeEndElement(); // Supported
        writer.writeEndElement(); // Languages
    }
}
