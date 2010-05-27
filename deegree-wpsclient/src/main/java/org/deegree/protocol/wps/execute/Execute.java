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
 * 
 * InputObject is used to assign one input
 * Identifier and InputObject are mandatory, input will be set on "as Reference" as default
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Christian Kiehle</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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
import org.jdom.Namespace;

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


    private Namespace wpsNamespace = Namespace.getNamespace( "wps", "http://www.opengis.net/wps/1.0.0" );

    private Namespace owsNamespace = Namespace.getNamespace( "ows", "http://www.opengis.net/ows/1.1" );

    private Namespace xsiNamespace = Namespace.getNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );

    private Namespace xlinkNamespace = Namespace.getNamespace( "xlink", "http://www.w3.org/1999/xlink" );

    public Execute( DescribeProcess describeProcess ) {
        this.desribeProcess = describeProcess;
    }

    public Execute( URL url ) {
        DescribeProcess describeProcess = new DescribeProcess( url );

        this.desribeProcess = describeProcess;
    }

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

    private XMLStreamWriter writeHeader( XMLStreamWriter writer )
                            throws XMLStreamException {
               
        writer.writeAttribute( "service", service );
        writer.writeAttribute( "version", version );
        writer.writeAttribute( "store", String.valueOf( store ) );
        writer.writeAttribute( "status", String.valueOf( status ) );
        writer.writeAttribute( xsiNamespace.getPrefix(), xsiNamespace.getURI(), "schemaLocation", schemaLocation );

        writer.writeStartElement( owsNamespace.getPrefix(), "Identifier", owsNamespace.getURI() );
        writer.writeCharacters( identifier );
        writer.writeEndElement();

        return writer;

    }

    private XMLStreamWriter writeInputs( XMLStreamWriter writer )
                            throws XMLStreamException {
        if ( dataInputExecuteList != null ) {
            writer.writeStartElement( wpsNamespace.getPrefix(), "DataInputs", wpsNamespace.getURI() );

            for ( int i = 0; i < dataInputExecuteList.size(); i++ ) {

                DataInputExecute dataInputExecute = dataInputExecuteList.get( i );

                writer.writeStartElement( wpsNamespace.getPrefix(), "Input", wpsNamespace.getURI() );

                writer.writeStartElement( owsNamespace.getPrefix(), "Identifier", owsNamespace.getURI() );
                writer.writeCharacters( dataInputExecute.getIdentifier() );
                writer.writeEndElement();

                InputFormChoiceExecute inputFormChoice = dataInputExecute.getInputFormChoice();

                if ( inputFormChoice.getReference() != null ) {
                    writer.writeStartElement( wpsNamespace.getPrefix(), "Reference", wpsNamespace.getURI() );

                    writer.writeAttribute( "href", dataInputExecute.getInputFormChoice().getReference().getHref() );


                    writer.writeEndElement();
                }

                if ( inputFormChoice.getData() != null ) {

                    if ( inputFormChoice.getData().getDataType() != null ) {
                        DataType dataType = inputFormChoice.getData().getDataType();
                        writer.writeStartElement( wpsNamespace.getPrefix(), "Data", wpsNamespace.getURI() );

                        if ( dataType.getLiteralData() != null ) {

                            writer.writeStartElement( wpsNamespace.getPrefix(), "LiteralData", wpsNamespace.getURI() );
                            writer.writeCharacters( dataType.getLiteralData().getLiteralData() );
                            writer.writeEndElement();
                        }

                        if ( dataType.getComplexData() != null ) {

                            writer.writeStartElement( wpsNamespace.getPrefix(), "ComplexData", wpsNamespace.getURI() );

                            writer.writeCharacters( dataType.getComplexData().getObject().toString() );
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

    private XMLStreamWriter writeOutputs( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( int j = 0; j < responseFormList.size(); j++ ) {
            writer.writeStartElement( wpsNamespace.getPrefix(), "ResponseForm", wpsNamespace.getURI() );

            ResponseForm responseForm = responseFormList.get( j );

            if ( responseForm.getRawOutputData() != null ) {

                writer.writeStartElement( wpsNamespace.getPrefix(), "RawDataOutuput", wpsNamespace.getURI() );
                writer.writeStartElement( owsNamespace.getPrefix(), "Identifier", owsNamespace.getURI() );
                writer.writeCharacters( responseForm.getRawOutputData().getIdentifier() );
                writer.writeEndElement();
                writer.writeEndElement();

            } else {
                writer.writeStartElement( wpsNamespace.getPrefix(), "ResponseDocument", wpsNamespace.getURI() );

                writer.writeAttribute( "storeExecuteResponse",
                                       String.valueOf( responseForm.getResponseDocument().storeExecuteResponse ) );
                writer.writeAttribute( "lineage", String.valueOf( responseForm.getResponseDocument().lineage ) );

                writer.writeAttribute( "status", String.valueOf( responseForm.getResponseDocument().status ) );

                for ( int i = 0; i < responseForm.getResponseDocument().getOutput().size(); i++ ) {
                    writer.writeStartElement( wpsNamespace.getPrefix(), "Output", wpsNamespace.getURI() );

                    writer.writeAttribute(
                                           "asReference",
                                           String.valueOf( responseForm.getResponseDocument().getOutput().get( i ).isAsReference() ) );

                    writer.writeStartElement( owsNamespace.getPrefix(), "Identifier", owsNamespace.getURI() );
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

        writer.writeStartDocument( "1.0" );//encoding...
        

        writer.writeStartElement( wpsNamespace.getPrefix(), "execute", wpsNamespace.getURI() );

        System.out.println ("wpsNamespace " + wpsNamespace.getPrefix());
        writer.writeNamespace( wpsNamespace.getPrefix(), wpsNamespace.getURI() );
        writer.writeNamespace( owsNamespace.getPrefix(), owsNamespace.getURI() );
        writer.writeNamespace( xsiNamespace.getPrefix(), xsiNamespace.getURI() );
        
        writer = writeHeader( writer );
        writer = writeInputs( writer );
        writer = writeOutputs( writer );

        
        writer.writeEndElement();
        writer.writeEndDocument();

        writer.flush();
        writer.close();
        out.close();

    }


}
