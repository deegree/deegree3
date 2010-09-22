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

import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.filter.xml.FilterCapabilitiesExporter;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.csw.CSWConstants.Sections;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.services.jaxb.main.DCPType;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.KeywordsType;
import org.deegree.services.jaxb.main.LanguageStringType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;

/**
 * Does the exportHandling for the Capabilities. This is a very static handling for explanation.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetCapabilitiesHandler extends OWSCapabilitiesXMLAdapter {

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static final String ISO_NS = "http://www.isotc211.org/2005/gmd";

    private static final String ISO_PREFIX = "gmd";

    private static LinkedList<String> parameterValues;

    /**
     * additional queryable properties in ISO
     */
    private static List<String> isoQueryables = new ArrayList<String>();

    private static LinkedList<String> supportedOperations = new LinkedList<String>();

    static {
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
        isoQueryables.add( "SpatialResolution" );
        isoQueryables.add( "TemporalExtent" );
        isoQueryables.add( "ServiceType" );
        isoQueryables.add( "ServiceTypeVersion" );
        isoQueryables.add( "Operation" );
        isoQueryables.add( "OperatesOnData" );
        isoQueryables.add( "CouplingType" );

        supportedOperations.add( CSWRequestType.GetCapabilities.name() );
        supportedOperations.add( CSWRequestType.DescribeRecord.name() );
        supportedOperations.add( CSWRequestType.GetRecords.name() );
        supportedOperations.add( CSWRequestType.GetRecordById.name() );
        supportedOperations.add( CSWRequestType.Transaction.name() );

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
    public static void export( XMLStreamWriter writer, DeegreeServicesMetadataType mainControllerConf,
                               DeegreeServiceControllerType mainConf, Set<Sections> sections,
                               ServiceIdentificationType identification, Version version, boolean isSoap )
                            throws XMLStreamException {

        if ( VERSION_202.equals( version ) ) {
            export202( writer, sections, identification, mainControllerConf, mainConf, isSoap );
        } else {
            throw new InvalidParameterValueException( "Supported versions are: '" + VERSION_202 + "'. Version '"
                                                      + version + "' instead is not supported." );
        }
    }

    private static void export202( XMLStreamWriter writer, Set<Sections> sections,
                                   ServiceIdentificationType identification,
                                   DeegreeServicesMetadataType mainControllerConf,
                                   DeegreeServiceControllerType mainConf, boolean isSoap )
                            throws XMLStreamException {
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );
        writer.setPrefix( "ows", OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartElement( CSW_202_NS, "Capabilities" );
        writer.writeAttribute( "version", "2.0.2" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", CSW_202_NS + " "
                                                                                + CSW_202_DISCOVERY_SCHEMA );

        // ows:ServiceIdentification
        if ( sections.isEmpty() || sections.contains( Sections.ServiceIdentification ) ) {
            exportServiceIdentification( writer, identification );

        }

        // ows:ServiceProvider
        if ( sections.isEmpty() || sections.contains( Sections.ServiceProvider ) ) {
            exportServiceProvider100( writer, mainControllerConf.getServiceProvider() );
        }

        // ows:OperationsMetadata
        if ( sections.isEmpty() || sections.contains( Sections.OperationsMetadata ) ) {

            exportOperationsMetadata( writer, mainConf.getDCP(), OWS_NS );
        }

        // mandatory
        FilterCapabilitiesExporter.export110( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void exportOperationsMetadata( XMLStreamWriter writer, DCPType dcp, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "OperationsMetadata" );

        for ( String name : supportedOperations ) {
            writer.writeStartElement( owsNS, "Operation" );
            writer.writeAttribute( "name", name );
            exportDCP( writer, dcp, owsNS );

            if ( name.equals( CSWRequestType.GetCapabilities.name() ) ) {

                writeGetCapabilities( writer, owsNS );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.DescribeRecord.name() ) ) {

                writeDescribeRecord( writer, owsNS );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.GetRecords.name() ) ) {

                writeGetRecords( writer, owsNS );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.GetRecordById.name() ) ) {

                writeGetRecordById( writer, owsNS );

                writer.writeEndElement();// Operation
                continue;
            } else if ( name.equals( CSWRequestType.Transaction.name() ) ) {

                // because there is the same output like for GetRecordById
                writeGetRecordById( writer, owsNS );

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
        writer.writeCharacters( ISO_NS );
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
        writer.writeStartElement( owsNS, "Constraint" );
        writer.writeAttribute( "name", "AdditionalQueryables" );

        for ( String val : new String[] { "Degree", "AccessConstraints", "OtherConstraints", "Classification",
                                         "ConditionApplyingToAccessAndUse", "Lineage", "SpecificationTitle",
                                         "SpecificationDate", "SpecificationDateType" } ) {
            writeElement( writer, owsNS, "Value", val );
        }

        writer.writeEndElement();// Constraint

        writer.writeEndElement();// OperationsMetadata
    }

    /*
     * private static void writeVersionAndUpdateSequence( XMLStreamWriter writer, int updateSequence ) throws
     * XMLStreamException { writer.writeAttribute( "version", VERSION_202.toString() ); writer.writeAttribute(
     * "updateSequence", Integer.toString( updateSequence ) ); }
     */

    private static void exportServiceIdentification( XMLStreamWriter writer, ServiceIdentificationType identification )
                            throws XMLStreamException {
        writer.writeStartElement( "http://www.opengis.net/ows", Sections.ServiceIdentification.toString() );

        for ( String oneTitle : identification.getTitle() ) {
            writeElement( writer, "http://www.opengis.net/ows", "Title", oneTitle );
        }

        for ( String oneAbstract : identification.getAbstract() ) {
            writeElement( writer, "http://www.opengis.net/ows", "Abstract", oneAbstract );
        }
        String fees = "NONE";

        // keywords [0,n]
        exportKeywords( writer, identification.getKeywords() );

        writeElement( writer, "http://www.opengis.net/ows", "ServiceType", "CSW" );
        writeElement( writer, "http://www.opengis.net/ows", "ServiceTypeVersion", "2.0.2" );

        // fees [1]
        fees = identification.getFees();
        if ( isEmpty( fees ) ) {
            identification.setFees( "NONE" );
        }
        fees = identification.getFees();

        // fees = fees.replaceAll( "\\W", " " );
        writeElement( writer, "http://www.opengis.net/ows", "Fees", fees );

        // accessConstraints [0,n]
        exportAccessConstraints( writer, identification );

        writer.writeEndElement();
    }

    private final static boolean isEmpty( String value ) {
        return value == null || "".equals( value );
    }

    /**
     * write a list of keywords in csw 2.0.2 style.
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
                    writer.writeStartElement( "http://www.opengis.net/ows", "Keywords" );
                    List<LanguageStringType> keyword = kwt.getKeyword();
                    for ( LanguageStringType lst : keyword ) {
                        exportKeyword( writer, lst );
                        // -> keyword [1, n]
                    }
                    // -> type [0,1]
                    // exportCodeType( writer, kwt.getType() );
                    writer.writeEndElement();// WCS_100_NS, "keywords" );
                }
            }

        }

    }

    /**
     * @param writer
     * @param lst
     * @throws XMLStreamException
     */
    public static void exportKeyword( XMLStreamWriter writer, LanguageStringType lst )
                            throws XMLStreamException {
        if ( lst != null ) {
            writeElement( writer, "http://www.opengis.net/ows", "Keyword", lst.getValue() );
        }
    }

    private static void exportAccessConstraints( XMLStreamWriter writer, ServiceIdentificationType identification )
                            throws XMLStreamException {
        List<String> accessConstraints = identification.getAccessConstraints();

        if ( accessConstraints.isEmpty() ) {
            accessConstraints.add( "NONE" );

        } else {
            for ( String ac : accessConstraints ) {
                if ( !ac.isEmpty() ) {
                    writeElement( writer, "http://www.opengis.net/ows", "AccessConstraints", ac );
                }
            }
        }

    }

    /**
     * Writes the parameter and attributes for the mandatory GetCapabilities operation to the output.
     * 
     * @param writer
     *            to write the output
     * @param owsNS
     *            the OWS namespace
     * @throws XMLStreamException
     */
    private static void writeGetCapabilities( XMLStreamWriter writer, String owsNS )
                            throws XMLStreamException {

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "sections" );

        parameterValues = new LinkedList<String>();

        parameterValues.add( "ServiceIdentification" );
        parameterValues.add( "ServiceProvider" );
        parameterValues.add( "OperationsMetadata" );
        parameterValues.add( "Filter_Capabilities" );

        for ( String value : parameterValues ) {
            writer.writeStartElement( owsNS, "Value" );
            writer.writeCharacters( value );
            writer.writeEndElement();// Value
        }
        writer.writeEndElement();// Parameter

        // Constraints...
    }

    /**
     * Writes the parameter and attributes for the mandatory DescribeRecord operation to the output.
     * 
     * @param writer
     *            to write the output
     * @param owsNS
     *            the OWS namespace
     * @throws XMLStreamException
     */
    private static void writeDescribeRecord( XMLStreamWriter writer, String owsNS )
                            throws XMLStreamException {

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "typeName" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( CSW_PREFIX + ":Record" );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "gmd" + ":MD_Metadata" );
        writer.writeEndElement();// Value

        // writer.writeStartElement( owsNS, "Value" );
        // writer.writeCharacters( CSW_PREFIX + ":Service" );
        // writer.writeEndElement();// Value

        writer.writeEndElement();// Parameter

        writeOutputFormat( writer, owsNS );

        // writer.writeStartElement( owsNS, "Parameter" );
        // writer.writeAttribute( "name", "schemaLocation" );
        // writer.writeStartElement( owsNS, "Value" );
        // writer.writeCharacters( "http://www.w3.org/TR/xmlschema-1/" );
        // writer.writeEndElement();// Value
        // writer.writeEndElement();// Parameter

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "schemaLanguage" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "XMLSCHEMA" );
        writer.writeEndElement();// Value

        writer.writeEndElement();// Constraint

        // Constraints...[0..*]
    }

    private static void writeOutputFormat( XMLStreamWriter writer, String owsNS )
                            throws XMLStreamException {
        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "outputFormat" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "application/xml" );
        writer.writeEndElement();// Value

        // writer.writeStartElement( owsNS, "Value" );
        // writer.writeCharacters( "text/plain" );
        // writer.writeEndElement();// Value
        //
        // writer.writeStartElement( owsNS, "Value" );
        // writer.writeCharacters( "text/html" );
        // writer.writeEndElement();// Value

        writer.writeEndElement();// Parameter

    }

    /**
     * Writes the parameter and attributes for the mandatory GetRecords operation to the output.
     * 
     * @param writer
     *            to write the output
     * @param owsNS
     *            the OWS namespace
     * @throws XMLStreamException
     */
    private static void writeGetRecords( XMLStreamWriter writer, String owsNS )
                            throws XMLStreamException {

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "typeNames" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( CSW_PREFIX + ":Record" );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( ISO_PREFIX + ":MD_Metadata" );
        writer.writeEndElement();// Value

        // writer.writeStartElement( owsNS, "Value" );
        // writer.writeCharacters( CSW_PREFIX + ":Service" );
        // writer.writeEndElement();// Value

        writer.writeEndElement();// Parameter

        writeOutputFormat( writer, owsNS );

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "outputSchema" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( CSWConstants.CSW_202_NS );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( ISO_NS );
        writer.writeEndElement();// Value

        writer.writeEndElement();// Parameter

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "resultType" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "hits" );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "results" );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "validate" );
        writer.writeEndElement();// Value

        writer.writeEndElement();// Parameter

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "ElementSetName" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "brief" );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "summary" );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "full" );
        writer.writeEndElement();// Value

        writer.writeEndElement();// Parameter

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "CONSTRAINTLANGUAGE" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( "Filter" );
        writer.writeEndElement();// Value

        // writer.writeStartElement( owsNS, "Value" );
        // writer.writeCharacters( "CQL_Text" );
        // writer.writeEndElement();// Value

        writer.writeEndElement();// Parameter

        writer.writeStartElement( owsNS, "Constraint" );
        writer.writeAttribute( "name", "SupportedISOQueryables" );

        for ( String s : isoQueryables ) {
            writer.writeStartElement( owsNS, "Value" );
            writer.writeCharacters( s );
            writer.writeEndElement();// Value
        }

        writer.writeEndElement();// Constraint

    }

    /**
     * Writes the parameter and attributes for the mandatory GetRecordById operation to the output.<br>
     * In this case the optional transaction operation uses this writing to the output, as well.
     * 
     * @param writer
     *            to write the output
     * @param owsNS
     *            the OWS namespace
     * @throws XMLStreamException
     */
    private static void writeGetRecordById( XMLStreamWriter writer, String owsNS )
                            throws XMLStreamException {

        writeOutputFormat( writer, owsNS );

        writer.writeStartElement( owsNS, "Parameter" );
        writer.writeAttribute( "name", "outputSchema" );

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( CSW_202_NS );
        writer.writeEndElement();// Value

        writer.writeStartElement( owsNS, "Value" );
        writer.writeCharacters( ISO_NS );
        writer.writeEndElement();// Value

        writer.writeEndElement(); // Parameter

    }

}
