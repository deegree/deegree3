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

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.protocol.csw.CSWConstants.OutputSchema;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.CSWService;
import org.deegree.services.csw.getrecordbyid.GetRecordById;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link GetRecordById} request
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordByIdHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GetRecordByIdHandler.class );

    private CSWService service;

    /**
     * Creates a new {@link GetRecordByIdHandler} instance that uses the given service to lookup the
     * {@link MetadataStore} s.
     * 
     * @param service
     */
    public GetRecordByIdHandler( CSWService service ) {
        this.service = service;

    }

    /**
     * Preprocessing for the export of a {@link GetRecordById} request
     * 
     * @param getRecBI
     *            the parsed getRecordById request
     * @param response
     *            for the servlet request to the client
     * @param isSoap
     * @throws IOException
     * @throws XMLStreamException
     * @throws InvalidParameterValueException
     * @throws OWSException
     */
    public void doGetRecordById( GetRecordById getRecBI, HttpResponseBuffer response, boolean isSoap )
                            throws XMLStreamException, IOException, InvalidParameterValueException, OWSException {

        LOG.debug( "doGetRecordById: " + getRecBI );

        Version version = getRecBI.getVersion();

        response.setContentType( getRecBI.getOutputFormat() );

        // to be sure of a valid response
        String schemaLocation = "";
        if ( getRecBI.getVersion() == VERSION_202 ) {
            schemaLocation = CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA;
        }

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, schemaLocation );
        try {
            export( xmlWriter, getRecBI, version, isSoap );
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
     * @param getRecBI
     * @param response
     * @param version
     * @throws XMLStreamException
     * @throws SQLException
     * @throws OWSException
     * @throws MetadataStoreException
     */
    private void export( XMLStreamWriter xmlWriter, GetRecordById getRecBI, Version version, boolean isSoap )
                            throws XMLStreamException, OWSException, MetadataStoreException {
        if ( VERSION_202.equals( version ) ) {
            export202( xmlWriter, getRecBI, isSoap );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

    }

    /**
     * Exporthandling for the CSW version 2.0.2
     * 
     * @param xmlWriter
     * @param getRecBI
     * @throws XMLStreamException
     * @throws SQLException
     * @throws OWSException
     * @throws MetadataStoreException
     */
    private void export202( XMLStreamWriter writer, GetRecordById getRecBI, boolean isSoap )
                            throws XMLStreamException, OWSException, MetadataStoreException {

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );
        writer.writeStartElement( CSW_202_NS, "GetRecordByIdResponse" );

        MetadataResultSet resultSet = null;

        if ( service.getMetadataStore() != null ) {
            try {
                for ( MetadataStore rec : service.getMetadataStore() ) {
                    resultSet = rec.getRecordsById( getRecBI.getRequestedIds() );
                }
            } catch ( MetadataStoreException e ) {
                throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE, "outputFormat" );
            }
        }

        for ( MetadataRecord m : resultSet.getMembers() ) {
            if ( getRecBI.getOutputSchema().equals( OutputSchema.determineOutputSchema( OutputSchema.ISO_19115 ) ) ) {
                m.serialize( writer, getRecBI.getElementSetName() );
            } else {
                DCRecord dc = m.toDublinCore();
                dc.serialize( writer, getRecBI.getElementSetName() );
            }
        }

        writer.writeEndDocument();

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

}
