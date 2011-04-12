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
package org.deegree.services.csw.exporthandling;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.OGC_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.filter.xml.FilterCapabilitiesExporter;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does the exportHandling for the Capabilities. This is a very static handling for explanation.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetCapabilitiesHandler extends OWSCapabilitiesXMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( GetCapabilitiesHandler.class );

    private final XMLStreamWriter writer;

    private final boolean isTransactionEnabled;

    private final DeegreeServicesMetadataType mainControllerConf;

    private final DeegreeServiceControllerType mainConf;

    private final Set<Sections> sections;

    private final ServiceIdentificationType identification;

    private final Version version;

    private final boolean isEnabledInspireExtension;

    private List<String> additionalQueryables = new ArrayList<String>();

    /**
     * additional queryable properties in ISO
     */
    private List<String> isoQueryables = new ArrayList<String>();

    private LinkedList<String> supportedOperations = new LinkedList<String>();

    private final Map<String, String> varToValue;

    private static final String[] outputFormats = new String[] { "text/xml", "application/xml" };

    private static final String[] outputSchemas = new String[] { CSW_202_NS, GMD_NS };

    private static final String[] typeNames = new String[] { CSW_PREFIX + ":Record", GMD_PREFIX + ":MD_Metadata" };

    private static final String[] elementSetNames = new String[] { "brief", "summary", "full" };

    private final GetCapabilitiesHelper gcHelper = new GetCapabilitiesHelper();

    public GetCapabilitiesHandler( XMLStreamWriter writer, DeegreeServicesMetadataType mainControllerConf,
                                   DeegreeServiceControllerType mainConf, Set<Sections> sections,
                                   ServiceIdentificationType identification, Version version,
                                   boolean isTransactionEnabled, boolean isEnabledInspireExtension ) {
        this.writer = writer;
        this.mainControllerConf = mainControllerConf;
        this.mainConf = mainConf;
        this.sections = sections;
        this.identification = identification;
        this.version = version;
        this.isTransactionEnabled = isTransactionEnabled;
        this.isEnabledInspireExtension = isEnabledInspireExtension;

        isoQueryables.add( "Format" );
        isoQueryables.add( "Type" );
        isoQueryables.add( "AnyText" );
        isoQueryables.add( "Modified" );
        isoQueryables.add( "Identifier" );
        isoQueryables.add( "Subject" );
        isoQueryables.add( "Title" );
        isoQueryables.add( "Abstract" );
        isoQueryables.add( "RevisionDate" );
        isoQueryables.add( "AlternateTitle" );
        isoQueryables.add( "CreationDate" );
        isoQueryables.add( "PublicationDate" );
        isoQueryables.add( "OrganisationName" );
        isoQueryables.add( "HasSecurityConstraints" );
        isoQueryables.add( "Language" );
        isoQueryables.add( "ResourceIdentifier" );
        isoQueryables.add( "ParentIdentifier" );
        isoQueryables.add( "KeywordType" );
        isoQueryables.add( "TopicCategory" );
        isoQueryables.add( "ResourceLanguage" );
        isoQueryables.add( "GeographicDescriptionCode" );
        isoQueryables.add( "Denominator" );
        isoQueryables.add( "DistanceValue" );
        isoQueryables.add( "DistanceUOM" );
        isoQueryables.add( "TempExtent_begin" );
        isoQueryables.add( "TempExtent_end" );
        isoQueryables.add( "ServiceType" );
        isoQueryables.add( "ServiceTypeVersion" );
        isoQueryables.add( "Operation" );
        isoQueryables.add( "OperatesOn" );
        isoQueryables.add( "OperatesOnIdentifier" );
        isoQueryables.add( "OperatesOnName" );
        isoQueryables.add( "CouplingType" );

        additionalQueryables.add( "AccessConstraints" );
        additionalQueryables.add( "Classification" );
        additionalQueryables.add( "ConditionApplyingToAccessAndUse" );
        additionalQueryables.add( "Degree" );
        additionalQueryables.add( "Lineage" );
        additionalQueryables.add( "MetadataPointOfContact" );
        additionalQueryables.add( "OtherConstraints" );
        additionalQueryables.add( "SpecificationTitle" );
        additionalQueryables.add( "SpecificationDate" );
        additionalQueryables.add( "SpecificationDateType" );

        supportedOperations.add( CSWRequestType.GetCapabilities.name() );
        supportedOperations.add( CSWRequestType.DescribeRecord.name() );
        supportedOperations.add( CSWRequestType.GetRecords.name() );
        supportedOperations.add( CSWRequestType.GetRecordById.name() );

        this.varToValue = new HashMap<String, String>();
        String serverAddress = OGCFrontController.getHttpGetURL();
        String systemStartDate = "2010-11-16";
        String organizationName = null;
        String emailAddress = null;
        try {
            organizationName = mainControllerConf.getServiceProvider().getProviderName();
            List<String> emailAddresses = mainControllerConf.getServiceProvider().getServiceContact().getElectronicMailAddress();
            if ( !emailAddresses.isEmpty() ) {
                emailAddress = emailAddresses.get( 0 ).trim();
            }
            varToValue.put( "${SERVER_ADDRESS}", serverAddress );
            varToValue.put( "${SYSTEM_START_DATE}", systemStartDate );
            varToValue.put( "${ORGANIZATION_NAME}", organizationName );
            varToValue.put( "${CI_EMAIL_ADDRESS}", emailAddress );
        } catch ( NullPointerException e ) {
            String msg = "There is somewhere a null?!?";
            LOG.debug( msg );
            throw new NullPointerException( msg );
        }

    }

    /**
     * Prepocessing for the xml export. Checks which version is requested and delegates it to the right versionexport.
     * In this case, version 2.0.2 of CSW is leaned on the 1.0.0 of the OGC specification.
     * 
     * @param writer
     * @param mainControllerConf
     * @param mainConf
     * @param sections
     * @param identification
     * @param version
     * @param isSoap
     * @throws XMLStreamException
     */
    public void export()
                            throws XMLStreamException {

        if ( VERSION_202.equals( version ) ) {
            export202( writer, sections, identification, mainControllerConf, mainConf );
        } else {
            throw new InvalidParameterValueException( "Supported versions are: '" + VERSION_202 + "'. Version '"
                                                      + version + "' instead is not supported." );
        }
    }

    private void export202( XMLStreamWriter writer, Set<Sections> sections, ServiceIdentificationType identification,
                            DeegreeServicesMetadataType mainControllerConf, DeegreeServiceControllerType mainConf )
                            throws XMLStreamException {

        writer.writeStartElement( CSW_PREFIX, "Capabilities", CSW_202_NS );
        writer.writeNamespace( "ows", OWS_NS );
        writer.writeNamespace( OGC_PREFIX, OGCNS );
        writer.writeNamespace( "xlink", XLN_NS );
        writer.writeAttribute( "version", "2.0.2" );
        writer.writeAttribute( "xsi", XSINS, "schemaLocation", CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA );

        // ows:ServiceIdentification
        if ( sections.isEmpty() || sections.contains( Sections.ServiceIdentification ) ) {
            gcHelper.exportServiceIdentification( writer, identification, "CSW", "2.0.2", null );

        }

        // ows:ServiceProvider
        if ( sections.isEmpty() || sections.contains( Sections.ServiceProvider ) ) {
            exportServiceProvider100( writer, mainControllerConf.getServiceProvider() );
        }

        // ows:OperationsMetadata
        if ( sections.isEmpty() || sections.contains( Sections.OperationsMetadata ) ) {

            exportOperationsMetadata( writer, OGCFrontController.getHttpGetURL(), OGCFrontController.getHttpPostURL(),
                                      OWS_NS );
        }

        // mandatory
        FilterCapabilitiesExporter.export110( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void exportOperationsMetadata( XMLStreamWriter writer, String get, String post, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "OperationsMetadata" );

        if ( isTransactionEnabled && !supportedOperations.contains( CSWRequestType.Transaction.name() ) ) {
            supportedOperations.add( CSWRequestType.Transaction.name() );
        }

        for ( String name : supportedOperations ) {
            writer.writeStartElement( owsNS, "Operation" );
            writer.writeAttribute( "name", name );
            exportDCP( writer, get, post, owsNS );

            if ( name.equals( CSWRequestType.GetCapabilities.name() ) ) {

                gcHelper.writeGetCapabilities( writer, owsNS );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.DescribeRecord.name() ) ) {

                gcHelper.writeDescribeRecord( writer, owsNS, typeNames, outputFormats, "XMLSCHEMA" );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.GetRecords.name() ) ) {

                gcHelper.writeGetRecords( writer, owsNS, typeNames, outputFormats, outputSchemas, elementSetNames );
                writeGetRecordsConstraints( writer, owsNS );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.GetRecordById.name() ) ) {

                gcHelper.writeGetRecordById( writer, owsNS, outputFormats, outputSchemas );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.Transaction.name() ) ) {
                // because there is the same output like for GetRecordById
                gcHelper.writeGetRecordById( writer, owsNS, outputFormats, outputSchemas );

                writer.writeEndElement();// Operation
                continue;
            }

        }

        // if xPathQueryables are allowed than this should be set
        // writer.writeStartElement( owsNS, "Constraint" );
        // writer.writeAttribute( "name", "XPathQueryables" );
        //
        // writer.writeStartElement( owsNS, "Value" );
        // writer.writeCharacters( "allowed" );
        // writer.writeEndElement();// Value
        //
        // writer.writeEndElement();// Constraint

        writer.writeStartElement( owsNS, "Constraint" );
        writer.writeAttribute( "name", "IsoProfiles" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( CSWConstants.GMD_NS );
        writer.writeEndElement();// Value

        writer.writeEndElement();// Constraint

        // if XML and/or SOAP is supported
        writer.writeStartElement( owsNS, "Constraint" );
        writer.writeAttribute( "name", "PostEncoding" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "XML" );
        writer.writeEndElement();// Value
        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "SOAP" );
        writer.writeEndElement();// Value

        writer.writeEndElement();// Constraint
        // additional inspire queryables
        if ( this.isEnabledInspireExtension ) {
            exportExtendedCapabilities( writer, owsNS );
        }
        writer.writeEndElement();// OperationsMetadata

    }

    private void exportExtendedCapabilities( XMLStreamWriter writer, String owsNS )
                            throws XMLStreamException {

        writer.writeStartElement( owsNS, "ExtendedCapabilities" );
        InputStream in = GetCapabilitiesHandler.class.getResourceAsStream( "extendedCapInspire.xml" );
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( in );
            reader.nextTag();
            writeTemplateElement( writer, reader );
            writer.writeEndElement();
        } finally {
            IOUtils.closeQuietly( in );
        }
    }

    private void writeTemplateElement( XMLStreamWriter writer, XMLStreamReader inStream )
                            throws XMLStreamException {

        if ( inStream.getEventType() != XMLStreamConstants.START_ELEMENT ) {
            throw new XMLStreamException( "Input stream does not point to a START_ELEMENT event." );
        }
        int openElements = 0;
        boolean firstRun = true;
        while ( firstRun || openElements > 0 ) {
            firstRun = false;
            int eventType = inStream.getEventType();

            switch ( eventType ) {
            case CDATA: {
                writer.writeCData( inStream.getText() );
                break;
            }
            case CHARACTERS: {
                String s = new String( inStream.getTextCharacters(), inStream.getTextStart(), inStream.getTextLength() );
                // TODO optimize
                for ( String param : varToValue.keySet() ) {
                    String value = varToValue.get( param );
                    s = s.replace( param, value );
                }
                writer.writeCharacters( s );

                break;
            }
            case END_ELEMENT: {
                writer.writeEndElement();
                openElements--;
                break;
            }
            case START_ELEMENT: {
                if ( inStream.getNamespaceURI() == NULL_NS_URI || inStream.getPrefix() == DEFAULT_NS_PREFIX
                     || inStream.getPrefix() == null ) {
                    writer.writeStartElement( inStream.getLocalName() );
                } else {
                    if ( writer.getNamespaceContext().getPrefix( inStream.getPrefix() ) == XMLConstants.NULL_NS_URI ) {
                        // TODO handle special cases for prefix binding, see
                        // http://download.oracle.com/docs/cd/E17409_01/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html#getNamespaceURI(java.lang.String)
                        writer.setPrefix( inStream.getPrefix(), inStream.getNamespaceURI() );
                    }
                    writer.writeStartElement( inStream.getPrefix(), inStream.getLocalName(), inStream.getNamespaceURI() );
                }
                // copy all namespace bindings
                for ( int i = 0; i < inStream.getNamespaceCount(); i++ ) {
                    String nsPrefix = inStream.getNamespacePrefix( i );
                    String nsURI = inStream.getNamespaceURI( i );
                    writer.writeNamespace( nsPrefix, nsURI );
                }

                // copy all attributes
                for ( int i = 0; i < inStream.getAttributeCount(); i++ ) {
                    String localName = inStream.getAttributeLocalName( i );
                    String nsPrefix = inStream.getAttributePrefix( i );
                    String value = inStream.getAttributeValue( i );
                    String nsURI = inStream.getAttributeNamespace( i );
                    if ( nsURI == null ) {
                        writer.writeAttribute( localName, value );
                    } else {
                        writer.writeAttribute( nsPrefix, nsURI, localName, value );
                    }
                }

                openElements++;
                break;
            }
            default: {
                break;
            }
            }
            if ( openElements > 0 ) {
                inStream.next();
            }
        }
    }

    private void writeGetRecordsConstraints( XMLStreamWriter writer, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "CONSTRAINTLANGUAGE" );
        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "Filter" );
        writer.writeEndElement();// Value
        writer.writeEndElement();// Parameter

        writer.writeStartElement( owsNS, "Constraint" );
        writer.writeAttribute( "name", "SupportedISOQueryables" );
        for ( String s : isoQueryables ) {
            writer.writeStartElement( owsNS, "Value" );
            writer.writeCharacters( s );
            writer.writeEndElement();// Value
        }
        writer.writeEndElement();// Constraint

        writer.writeStartElement( owsNS, "Constraint" );
        writer.writeAttribute( "name", "AdditionalQueryables" );
        for ( String val : additionalQueryables ) {
            writeElement( writer, owsNS, "Value", val );
        }
        writer.writeEndElement();// Constraint
    }

}
