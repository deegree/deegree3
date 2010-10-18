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
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataResultType;
import org.deegree.metadata.persistence.MetadataCollection;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.protocol.csw.CSWConstants.OutputSchema;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.CSWController;
import org.deegree.services.csw.CSWService;
import org.deegree.services.csw.getrecords.GetRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link GetRecords} request
 * 
 * @see CSWController
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GetRecordsHandler.class );

    private CSWService service;

    /**
     * Creates a new {@link GetRecordsHandler} instance that uses the given service to lookup the {@link MetadataStore}
     * s.
     * 
     * @param service
     */
    public GetRecordsHandler( CSWService service ) {
        this.service = service;

    }

    /**
     * Preprocessing for the export of a {@link GetRecords} request
     * 
     * @param getRec
     * @param response
     * @throws XMLStreamException
     * @throws IOException
     * @throws SQLException
     * @throws OWSException
     */
    public void doGetRecords( GetRecords getRec, HttpResponseBuffer response, boolean isSoap )
                            throws XMLStreamException, IOException, OWSException {

        LOG.debug( "doGetRecords: " + getRec );

        Version version = getRec.getVersion();

        response.setContentType( getRec.getOutputFormat() );

        // to be sure of a valid response
        String schemaLocation = "";
        if ( getRec.getVersion() == VERSION_202 ) {
            schemaLocation = CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA + " " + GMD_NS + " "
                             + "http://schemas.opengis.net/iso/19139/20070417/gmd/gmd.xsd";
        }

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, schemaLocation );
        try {
            export( xmlWriter, getRec, version, isSoap );
        } catch ( OWSException e ) {
            LOG.debug( e.getMessage() );
            throw new InvalidParameterValueException( e.getMessage() );
        } catch ( MetadataStoreException e ) {
            LOG.debug( e.getMessage() );
            throw new OWSException( e.getMessage(), ControllerException.NO_APPLICABLE_CODE );
        }
        xmlWriter.flush();
    }

    /**
     * Exports the correct recognized request and determines to which version export it should delegate the request
     * 
     * @param xmlWriter
     * @param getRec
     * @param response
     * @param version
     * @throws XMLStreamException
     * @throws SQLException
     * @throws OWSException
     * @throws MetadataStoreException
     */
    private void export( XMLStreamWriter xmlWriter, GetRecords getRec, Version version, boolean isSoap )
                            throws XMLStreamException, OWSException, MetadataStoreException {

        if ( VERSION_202.equals( version ) ) {
            export202( xmlWriter, getRec, isSoap );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

    }

    /**
     * 
     * Exporthandling for the version 2.0.2
     * 
     * @param xmlWriter
     * @param getRec
     * @throws XMLStreamException
     * @throws SQLException
     * @throws OWSException
     * @throws MetadataStoreException
     */
    private void export202( XMLStreamWriter writer, GetRecords getRec, boolean isSoap )
                            throws XMLStreamException, OWSException, MetadataStoreException {
        Version version = new Version( 2, 0, 2 );

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );
        if ( getRec.getResultType() != ResultType.validate ) {
            writer.writeStartElement( CSW_202_NS, "GetRecordsResponse" );

            searchStatus( writer, version );
            searchResult( writer, getRec, version );

        } else {
            if ( validate( getRec.getHoleRequest() ).size() != 0 ) {
                String errorMessage = "VALIDATION-ERROR: ";
                for ( String error : validate( getRec.getHoleRequest() ) ) {
                    errorMessage += error + "; \n";
                }

                throw new IllegalArgumentException( errorMessage );

            }
            writer.writeStartElement( CSW_202_NS, "Acknowledgement" );
            writer.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
            writer.writeStartElement( CSW_202_NS, "EchoedRequest" );
            readXMLFragment( getRec.getHoleRequest().toString(), writer );

            writer.writeEndElement();
            writer.writeEndElement();

        }

        writer.writeEndDocument();

    }

    /**
     * Exports the timestamp of the request
     * 
     * @param writer
     * @param version
     * @throws XMLStreamException
     */
    private void searchStatus( XMLStreamWriter writer, Version version )
                            throws XMLStreamException {

        if ( VERSION_202.equals( version ) ) {
            writer.writeStartElement( CSW_202_NS, "SearchStatus" );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

        writer.writeAttribute( "timestamp", DateUtils.formatISO8601Date( new Date() ) );

        writer.writeEndElement();// SearchStatus

    }

    /**
     * Exports the result container that contains the requested elements
     * 
     * @param writer
     * @param getRec
     * @param version
     * @throws XMLStreamException
     * @throws OWSException
     * @throws MetadataStoreException
     */
    private void searchResult( XMLStreamWriter writer, GetRecords getRec, Version version )
                            throws XMLStreamException, OWSException, MetadataStoreException {
        boolean isElementName = false;
        if ( getRec.getElementName() != null ) {
            isElementName = true;
        }
        String elementSetValue = getRec.getElementSetName() != null ? getRec.getElementSetName().name() : "custom";

        MetadataResultType type = null;

        MetadataResultSet storeSet = null;
        MetadataCollection col = null;

        if ( VERSION_202.equals( version ) ) {

            writer.writeStartElement( CSW_202_NS, "SearchResults" );

            MetadataStore rec = service.getStore();
            MetadataQuery query = new MetadataQuery( getRec.getConstraint(), getRec.getSortBy(),
                                                     getRec.getResultType(), getRec.getMaxRecords(),
                                                     getRec.getStartPosition() );

            try {
                storeSet = rec.getRecords( query );
            } catch ( MetadataStoreException e ) {
                throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE );
            }

            type = storeSet.getResultType();
            col = storeSet.getMembers();

            writer.writeAttribute( "elementSet", elementSetValue );

            writer.writeAttribute( "recordSchema", getRec.getOutputSchema().toString() );

            writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( type.getNumberOfRecordsMatched() ) );

            writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( type.getNumberOfRecordsReturned() ) );

            writer.writeAttribute( "nextRecord", Integer.toString( type.getNextRecord() ) );

            writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );

        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

        for ( MetadataRecord m : col ) {
            if ( isElementName == false ) {
                if ( getRec.getOutputSchema().equals( OutputSchema.determineOutputSchema( OutputSchema.ISO_19115 ) ) ) {
                    m.serialize( writer, getRec.getElementSetName() );
                } else {
                    DCRecord dc = m.toDublinCore();
                    dc.serialize( writer, getRec.getElementSetName() );
                }
            } else {
                if ( getRec.getOutputSchema().equals( OutputSchema.determineOutputSchema( OutputSchema.ISO_19115 ) ) ) {
                    m.serialize( writer, getRec.getElementName() );
                } else {
                    DCRecord dc = m.toDublinCore();
                    dc.serialize( writer, getRec.getElementName() );
                }
            }
        }

        writer.writeEndElement();// SearchResult

    }

    /**
     * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
     * 
     * @param writer
     *            writer to write the XML to, must not be null
     * @param schemaLocation
     *            allows to specify a value for the 'xsi:schemaLocation' attribute in the root element, must not be null
     * @return {@link XMLStreamWriter}
     * @throws XMLStreamException
     * @throws IOException
     */
    static XMLStreamWriter getXMLResponseWriter( HttpResponseBuffer writer, String schemaLocation )
                            throws XMLStreamException, IOException {

        if ( schemaLocation == null ) {
            return writer.getXMLWriter();
        }
        return new XMLStreamWriterWrapper( writer.getXMLWriter(), schemaLocation );
    }

    /**
     * Validates the client request
     * 
     * @param elem
     * @return a list of error messages
     */
    private List<String> validate( OMElement elem ) {
        StringWriter s = new StringWriter();
        try {
            elem.serialize( s );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        }
        InputStream is = new ByteArrayInputStream( s.toString().getBytes() );
        return SchemaValidator.validate( is, "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" );

    }

    /**
     * Reads a valid XML fragment
     * 
     * @param elementString
     * @param xmlWriter
     */
    private void readXMLFragment( String elementString, XMLStreamWriter xmlWriter ) {

        StringReader r = new StringReader( elementString );

        XMLStreamReader xmlReader;
        try {
            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( r );

            // skip START_DOCUMENT
            xmlReader.nextTag();

            XMLAdapter.writeElement( xmlWriter, xmlReader );
            xmlReader.close();

        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            e.printStackTrace();
        }

    }

}
