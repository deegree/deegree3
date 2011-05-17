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

import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_RECORD;
import static org.deegree.protocol.csw.CSWConstants.DC_LOCAL_PART;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.GMD_LOCAL_PART;
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;
import static org.deegree.services.i18n.Messages.get;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.OutputSchema;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.CSWController;
import org.deegree.services.csw.describerecord.DescribeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link DescribeRecord} request.
 * <p>
 * NOTE:<br>
 * Due to the architecture of this CSW implementation there should be a typeName available which recordStore is
 * requested. But in the describeRecord operation there exists the possibility to get all the recordStores without any
 * typeName available. So at the moment there is a HACK for this UseCase.
 * 
 * @see CSWController
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeRecordHandler {

    private static final Logger LOG = LoggerFactory.getLogger( DescribeRecordHandler.class );

    private DescribeRecordHelper dcHelper = new DescribeRecordHelper();

    /**
     * Preprocessing for the export of a {@link DescribeRecord} request to determine which recordstore is requested.
     * 
     * @param descRec
     *            the parsed describeRecord request
     * @param response
     *            for the servlet request to the client
     * @throws XMLStreamException
     * @throws IOException
     * @throws OWSException
     */
    public void doDescribeRecord( DescribeRecord descRec, HttpResponseBuffer response )
                            throws XMLStreamException, IOException, OWSException {

        QName[] typeNames = descRec.getTypeNames();

        Version version = descRec.getVersion();
        response.setContentType( descRec.getOutputFormat() );

        XMLStreamWriter xmlWriter = dcHelper.getXMLResponseWriter( response, null );

        try {
            export( xmlWriter, typeNames, version );
        } catch ( MetadataStoreException e ) {
            LOG.debug( e.getMessage() );
            throw new OWSException( e.getMessage(), ControllerException.NO_APPLICABLE_CODE );
        }
        xmlWriter.flush();

    }

    /**
     * 
     * Exports the correct recognized request and determines to which version export it should delegate it.
     * 
     * @param writer
     *            to write the XML to
     * @param record
     *            the recordStore that is requested
     * @throws XMLStreamException
     * @throws MetadataStoreException
     */
    private void export( XMLStreamWriter writer, QName[] typeNames, Version version )
                            throws XMLStreamException, MetadataStoreException {

        if ( VERSION_202.equals( version ) ) {
            export202( writer, typeNames );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

    }

    /**
     * Exporthandling for the CSW version 2.0.2. <br>
     * It is a container for zero or more SchemaComponent elements.
     * 
     * @param writer
     * @param record
     * @throws XMLStreamException
     * @throws MetadataStoreException
     */
    private void export202( XMLStreamWriter writer, QName[] typeNames )
                            throws XMLStreamException, MetadataStoreException {
        writer.setDefaultNamespace( CSW_202_NS );
        writer.writeStartElement( CSW_202_NS, "DescribeRecordResponse" );
        writer.writeDefaultNamespace( CSW_202_NS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        writer.writeAttribute( XSINS, "schemaLocation", CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA );

        try {
            if ( typeNames.length == 0 ) {
                writeDC( writer, new QName( DC_NS, DC_LOCAL_PART, DC_PREFIX ) );
                exportISO( writer );
            }
            for ( QName typeName : typeNames ) {
                // if typeName is csw:Record
                if ( OutputSchema.determineByTypeName( typeName ) == OutputSchema.DC ) {
                    writeDC( writer, typeName );
                }
                // if typeName is gmd:MD_Metadata
                else if ( OutputSchema.determineByTypeName( typeName ) == OutputSchema.ISO_19115 ) {
                    exportISO( writer );
                }
                // if the typeName is no registered in this recordprofile
                else {
                    String errorMessage = "The typeName " + typeName + "is not supported. ";
                    LOG.debug( errorMessage );
                    throw new InvalidParameterValueException( errorMessage );
                }
            }
        } catch ( IOException e ) {
            LOG.debug( "error: " + e.getMessage(), e );
            throw new MetadataStoreException( e.getMessage() );
        } catch ( Exception e ) {
            LOG.debug( "error: " + e.getMessage(), e );
        }
        writer.writeEndElement();// DescribeRecordResponse
        writer.writeEndDocument();
    }

    private void writeDC( XMLStreamWriter writer, QName typeName )
                            throws XMLStreamException, UnsupportedEncodingException {
        URL url = null;
        try {
            url = new URL( CSW_202_RECORD );
            URLConnection urlConn = url.openConnection();
            BufferedInputStream bais = new BufferedInputStream( urlConn.getInputStream() );
            InputStreamReader isr = new InputStreamReader( bais, "UTF-8" );
            dcHelper.exportSchemaComponent( writer, typeName, isr );
        } catch ( Exception e ) {
            LOG.info( "Could not get connection to " + CSW_202_RECORD + ". Export schema as reference." );
            InputStream is = DescribeRecordHandler.class.getResourceAsStream( "dublinCore.xml" );
            if ( is != null ) {
                InputStreamReader isr = new InputStreamReader( is, "UTF-8" );
                dcHelper.exportSchemaComponent( writer, typeName, isr );
            } else {
                String msg = get( "CSW_NO_FILE", "dublinCore.xml" );
                LOG.debug( msg );
            }
        }
    }

    private void exportISO( XMLStreamWriter writer )
                            throws XMLStreamException, UnsupportedEncodingException {
        InputStream in_data = DescribeRecordHandler.class.getResourceAsStream( "iso_data.xml" );
        InputStream in_service = DescribeRecordHandler.class.getResourceAsStream( "iso_service.xml" );
        InputStreamReader isr = null;

        if ( in_data != null ) {
            isr = new InputStreamReader( in_data, "UTF-8" );
            dcHelper.exportSchemaComponent( writer, new QName( GMD_NS, GMD_LOCAL_PART, GMD_PREFIX ), isr );
        } else {
            String msg = get( "CSW_NO_FILE", "iso_data.xml" );
            LOG.debug( msg );
        }

        if ( in_service != null ) {
            isr = new InputStreamReader( in_service, "UTF-8" );
            dcHelper.exportSchemaComponent( writer, new QName( CSWConstants.SRV_NS, CSWConstants.SRV_LOCAL_PART,
                                                               CSWConstants.SRV_PREFIX ), isr );
        } else {
            String msg = get( "CSW_NO_FILE", "iso_service.xml" );
            LOG.debug( msg );
        }

    }

    public static void main( String[] args ) {
        URLConnection urlConn;
        try {
            urlConn = new URL( CSW_202_RECORD ).openConnection();
            System.out.println( urlConn );
            urlConn.getInputStream();
        } catch ( MalformedURLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
