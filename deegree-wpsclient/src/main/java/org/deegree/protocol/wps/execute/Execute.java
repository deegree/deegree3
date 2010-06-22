//$HeadURL: https://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.protocol.wps.execute;

/**
 * Generates Execute Request
 * InputObject is used to assign one input
 * Identifier and InputObject are mandatory, input will be set on "as Reference" as default
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */

import static org.deegree.commons.xml.CommonNamespaces.getNamespaceContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.tools.BuildExecuteObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Execute {

    private String service;

    private String request;

    private String version;

    private String identifier;

    private List<DataInputExecute> dataInputExecuteList;

    private List<ResponseForm> responseFormList;

    private ResponseForm responseForm;

    private String language;

    private DescribeProcess desribeProcess;

    private ProcessDescription processDescription;

    private BuildExecuteObjects fillDataInput;

    private boolean store;

    private boolean status;

    private String schemaLocation;

    private static final NamespaceContext NS_CONTEXT;

    private OutputStream outputStream;
    
    private static Logger LOG = LoggerFactory.getLogger( Execute.class );


    static {
        NS_CONTEXT = new NamespaceContext();
        NS_CONTEXT.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        NS_CONTEXT.addNamespace( "wps", "http://www.opengis.net/wps/1.0.0" );
        NS_CONTEXT.addNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        NS_CONTEXT.addNamespace( "xlink", "http://www.w3.org/1999/xlink" );

    }

    private String wpsNamespace = "wps";

    private String owsNamespace = "ows";

    private String xsiNamespace = "xsi";

    public Execute( DescribeProcess describeProcess ) {
        this.desribeProcess = describeProcess;
    }

    /**
     * 
     * @param url
     * 
     */
    public Execute( URL url ) {
        DescribeProcess describeProcess = new DescribeProcess( url );

        this.desribeProcess = describeProcess;
    }


    /**
     * 
     * @param processDesription
     * 
     * @param dataInputExecuteList
     * 
     * @param responseFormList
     * 
     * @param url
     * 
     */
    public Execute( ProcessDescription processDesription, List<DataInputExecute> dataInputExecuteList,
                    List<ResponseForm> responseFormList, URL url ) {
        identifier = processDesription.getIdentifier();
        version = processDesription.getProcessVersion();
        service = processDesription.getService();
        schemaLocation = processDesription.getSchemaLocation();
        status = processDesription.isStatusSupported();
        store = processDesription.isStoreSupported();
        this.dataInputExecuteList = dataInputExecuteList;
        this.responseFormList = responseFormList;
    }

    /**
     * 
     * @return XMLStreamWriter
     * 
     * @param XMLStreamWriter
     * 
     */
    private XMLStreamWriter writeHeader( XMLStreamWriter writer )
                            throws XMLStreamException {

        writer.writeAttribute( "service", service );
        writer.writeAttribute( "version", version );
        writer.writeAttribute( "store", String.valueOf( store ) );
        writer.writeAttribute( "status", String.valueOf( status ) );
        writer.writeAttribute( xsiNamespace, NS_CONTEXT.getURI( xsiNamespace ), "schemaLocation", schemaLocation );

        writer.writeStartElement( owsNamespace, "Identifier", NS_CONTEXT.getURI( owsNamespace ) );
        writer.writeCharacters( identifier );
        writer.writeEndElement();

        return writer;

    }

    /**
     * 
     * @return XMLStreamWriter
     * 
     * @param XMLStreamWriter
     * 
     */
    private XMLStreamWriter writeInputs( XMLStreamWriter writer )
                            throws XMLStreamException {
        if ( dataInputExecuteList != null ) {
            writer.writeStartElement( wpsNamespace, "DataInputs", NS_CONTEXT.getURI( wpsNamespace ) );

            for ( int i = 0; i < dataInputExecuteList.size(); i++ ) {

                DataInputExecute dataInputExecute = dataInputExecuteList.get( i );

                writer.writeStartElement( wpsNamespace, "Input", NS_CONTEXT.getURI( wpsNamespace ) );

                writer.writeStartElement( owsNamespace, "Identifier", NS_CONTEXT.getURI( owsNamespace ) );
                writer.writeCharacters( dataInputExecute.getIdentifier() );
                writer.writeEndElement();

                InputFormChoiceExecute inputFormChoice = dataInputExecute.getInputFormChoice();

                if ( inputFormChoice.getReference() != null ) {
                    writer.writeStartElement( wpsNamespace, "Reference", NS_CONTEXT.getURI( wpsNamespace ) );

                    writer.writeAttribute( "href", dataInputExecute.getInputFormChoice().getReference().getHref() );

                    writer.writeEndElement();
                }

                if ( inputFormChoice.getData() != null ) {

                    if ( inputFormChoice.getData().getDataType() != null ) {
                        DataType dataType = inputFormChoice.getData().getDataType();
                        writer.writeStartElement( wpsNamespace, "Data", NS_CONTEXT.getURI( wpsNamespace ) );

                        if ( dataType.getLiteralData() != null ) {

                            writer.writeStartElement( wpsNamespace, "LiteralData", NS_CONTEXT.getURI( wpsNamespace ) );
                            writer.writeCharacters( dataType.getLiteralData().getLiteralData() );
                            writer.writeEndElement();
                        }

                        if ( dataType.getComplexData() != null ) {

                            writer.writeStartElement( wpsNamespace, "ComplexData", NS_CONTEXT.getURI( wpsNamespace ) );

                            // writer.writeStartElement(dataType.getComplexData().getObject().toString());
                            // writer.writeCData(dataType.getComplexData().getObject().toString());
                            writer.writeDTD( dataType.getComplexData().getObject().toString() );

                            // writer.writeCharacters( );
                            writer.writeEndElement();

                        }

                        if ( dataType.getBoundingBoxData() != null ) {

                            // TO DO
                        }

                        writer.writeEndElement();

                    }

                }

                writer.writeEndElement();

            }

        }

        return writer;
    }

    /**
     * 
     * @return XMLStreamWriter
     * 
     * @param XMLStreamWriter
     * 
     */
    private XMLStreamWriter writeOutputs( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( int j = 0; j < responseFormList.size(); j++ ) {
            writer.writeStartElement( wpsNamespace, "ResponseForm", NS_CONTEXT.getURI( wpsNamespace ) );

            ResponseForm responseForm = responseFormList.get( j );

            if ( responseForm.getRawOutputData() != null ) {

                writer.writeStartElement( wpsNamespace, "RawDataOutuput", NS_CONTEXT.getURI( wpsNamespace ) );
                writer.writeStartElement( owsNamespace, "Identifier", NS_CONTEXT.getURI( owsNamespace ) );
                writer.writeCharacters( responseForm.getRawOutputData().getIdentifier() );
                writer.writeEndElement();
                writer.writeEndElement();

            } else {
                writer.writeStartElement( wpsNamespace, "ResponseDocument", NS_CONTEXT.getURI( wpsNamespace ) );

                writer.writeAttribute( "storeExecuteResponse",
                                       String.valueOf( responseForm.getResponseDocument().isStoreExecuteResponse() ) );
                writer.writeAttribute( "lineage", String.valueOf( responseForm.getResponseDocument().isLineage() ) );

                writer.writeAttribute( "status", String.valueOf( responseForm.getResponseDocument().isStatus() ) );

                for ( int i = 0; i < responseForm.getResponseDocument().getOutput().size(); i++ ) {
                    writer.writeStartElement( wpsNamespace, "Output", NS_CONTEXT.getURI( wpsNamespace ) );

                    writer.writeAttribute(
                                           "asReference",
                                           String.valueOf( responseForm.getResponseDocument().getOutput().get( i ).isAsReference() ) );

                    writer.writeStartElement( owsNamespace, "Identifier", NS_CONTEXT.getURI( owsNamespace ) );
                    writer.writeCharacters( responseForm.getResponseDocument().getOutput().get( i ).getIdentifier() );
                    writer.writeEndElement();

                }

                writer.writeEndElement();

            }

            writer.writeEndElement();

        }

        return writer;

    }

    
    public void createExecuteRequest()
                            throws XMLStreamException, IOException {

        OutputStream out = null;
        try {
            out = new FileOutputStream( "execute" + identifier + ".xml" );
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try {
            writer = factory.createXMLStreamWriter( out );
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        writer.writeStartDocument( "1.0" );// encoding...

        writer.writeStartElement( wpsNamespace, "execute", NS_CONTEXT.getURI( wpsNamespace ) );

        writer.writeNamespace( wpsNamespace, NS_CONTEXT.getURI( wpsNamespace ) );
        writer.writeNamespace( owsNamespace, NS_CONTEXT.getURI( owsNamespace ) );
        writer.writeNamespace( xsiNamespace, NS_CONTEXT.getURI( xsiNamespace ) );

        writer = writeHeader( writer );
        writer = writeInputs( writer );
        writer = writeOutputs( writer );

        writer.writeEndElement();
        writer.writeEndDocument();

        writer.flush();
        writer.close();
        out.close();

        LOG.info("ExecuteRequest generated successfully");
    }

    /**
     * 
     * @return ByteArrayOutputStream
     * 
     */
    public ByteArrayOutputStream returnExecuteRequest()
                            throws XMLStreamException, IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try {
            writer = factory.createXMLStreamWriter( out );

        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        writer.writeStartDocument( "1.0" );// encoding...

        writer.writeStartElement( wpsNamespace, "execute", NS_CONTEXT.getURI( wpsNamespace ) );

        writer.writeNamespace( wpsNamespace, NS_CONTEXT.getURI( wpsNamespace ) );
        writer.writeNamespace( owsNamespace, NS_CONTEXT.getURI( owsNamespace ) );
        writer.writeNamespace( xsiNamespace, NS_CONTEXT.getURI( xsiNamespace ) );

        writer = writeHeader( writer );
        writer = writeInputs( writer );
        writer = writeOutputs( writer );

        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        writer.close();

        out.close();
        
        LOG.info("ExecuteRequest generated successfully");

        return out;
    }

}
