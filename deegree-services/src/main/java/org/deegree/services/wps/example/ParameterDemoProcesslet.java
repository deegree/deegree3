//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.example;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.BoundingBoxOutput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.output.LiteralOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this {@link Processlet} is to provide a demonstration for the use of different
 * input and output parameter types in a deegree 3 WPS process.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ParameterDemoProcesslet implements Processlet {

    private static final Logger LOG = LoggerFactory.getLogger( ParameterDemoProcesslet.class );

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {

        LOG.trace( "BEGIN ParameterDemoProcesslet#execute(), context: " + OGCFrontController.getContext() );

        /**
         * 
         * 1.) READ INPUT PARAMETERS FROM REQUEST INTO VARIABLES
         * 
         * The relevant input parameters have been defined inside the ParameterDemoProcess.xml
         * configuration file. The relevant snippet from this file is the <InputParameters> tag and
         * its child nodes: <InputParameters> <LiteralInput> <Identifier>LiteralInput</Identifier>
         * <Title>Example literal input </Title> <Abstract>This parameter specifies how long the
         * execution of the process takes (the process sleeps for this time). May be specified in
         * seconds or minutes.</Abstract> <DataType
         * reference="http://www.w3.org/TR/xmlschema-2/#integer">integer</DataType>
         * <DefaultUOM>seconds</DefaultUOM> <OtherUOM>minutes</OtherUOM> </LiteralInput>
         * <BoundingBoxInput> <Identifier>BBOXInput</Identifier> <Title>BBOXInput</Title>
         * <DefaultCRS>EPSG:4326</DefaultCRS> </BoundingBoxInput> <ComplexInput>
         * <Identifier>XMLInput</Identifier> <Title>XMLInput</Title> <DefaultFormat
         * mimeType="text/xml" /> </ComplexInput> <ComplexInput>
         * <Identifier>BinaryInput</Identifier> <Title>BinaryInput</Title> <DefaultFormat
         * mimeType="image/png" encoding="base64" /> </ComplexInput> </InputParameters>
         * 
         * There are four input parameters: a literal input, a bounding-box input, and two complex
         * inputs. Each parameter will be read into a variable of the corresponding type.
         * 
         **/

        /**
         * 
         * 1.1) READ INPUT PARAMETER OF TYPE LITERAL INPUT
         * 
         * determine the sleep time in seconds. Read the first parameter with the
         * <Identifier>LiteralInput</Identifier> using the ProcessletInput object in. The method
         * getParameter returns an object of the type
         * {@link org.deegree.services.wps.ProcessletInputs} which has to be casted to the correct
         * type. The correct type is also defined inside the configuration file (<LiteralInput>).
         * 
         */

        LiteralInput li = (LiteralInput) in.getParameter( "LiteralInput" );
        LOG.debug( "- LiteratlInput: " + li );

        /**
         * 
         * 1.2) READ INPUT PARAMETER OF TYPE BOUNDINGBOXINPUT
         * 
         * works just like literal input, but uses another interface for type casting.
         */

        BoundingBoxInput bboxInput = (BoundingBoxInput) in.getParameter( "BBOXInput" );
        LOG.debug( "- BBOXInput: " + bboxInput );

        /**
         * 
         * 1.3) READ INPUT PARAMETER OF TYPE COMPLEXINPUT
         * 
         * works just like literal or boundingbox input, but uses another interface for type
         * casting. ComplexInput is a complex data structure, i.e. an object encoded in XML (like
         * GML) or a raw binary stream (like an image).
         * 
         */

        ComplexInput xmlInput = (ComplexInput) in.getParameter( "XMLInput" );
        LOG.debug( "- XMLInput: " + xmlInput );

        ComplexInput binaryInput = (ComplexInput) in.getParameter( "BinaryInput" );
        LOG.debug( "- BinaryInput: " + binaryInput );

        /**
         * 
         * 2.) COMPUTATION After reading all input parameters into variables, the computation can be
         * performed (or delegated).
         * 
         */

        int sleepSeconds = determineSleepTime( li );

        // sleep a total of sleepSeconds (but update the percent completed information for every
        // percent)
        try {
            float sleepMillis = sleepSeconds * 1000;
            int sleepStep = (int) ( sleepMillis / 99.0f );
            LOG.debug( "Sleep step (millis): " + sleepStep );
            for ( int percentCompleted = 0; percentCompleted <= 99; percentCompleted++ ) {
                LOG.debug( "Setting percent completed: " + percentCompleted );
                info.setPercentCompleted( percentCompleted );
                Thread.sleep( sleepStep );
            }
        } catch ( InterruptedException e ) {
            throw new ProcessletException( e.getMessage() );
        }

        /**
         * 3.) SET OUTPUT PARAMETERS
         * 
         * After the computation is complete, the results need to assigned to the corresponding
         * output data types. This is done similar to reading input types. Please consult the
         * configuration document for the definition of output types:
         * 
         * <OutputParameters> <LiteralOutput> <Identifier>LiteralOutput</Identifier> <Title>A
         * literal output parameter</Title> <DataType
         * reference="http://www.w3.org/TR/xmlschema-2/#integer">integer</DataType>
         * <DefaultUOM>seconds</DefaultUOM> </LiteralOutput> <BoundingBoxOutput>
         * <Identifier>BBOXOutput</Identifier> <Title>A bounding box output parameter</Title>
         * <DefaultCRS>EPSG:4326</DefaultCRS> </BoundingBoxOutput> <ComplexOutput>
         * <Identifier>XMLOutput</Identifier> <Title>An XML output parameter</Title> <DefaultFormat
         * mimeType="text/xml" /> </ComplexOutput> <ComplexOutput>
         * <Identifier>BinaryOutput</Identifier> <Title>A binary output parameter</Title>
         * <DefaultFormat mimeType="image/png" encoding="base64" /> </ComplexOutput>
         * </OutputParameters>
         * 
         * Here we define once again four types: LiteralOutput, BoundingBoxOutput, and two
         * ComplexOutput tpyes.
         * 
         */

        // Literal outputs will just be retrieved from the ProcessletOutput object...
        LiteralOutput literalOutput = (LiteralOutput) out.getParameter( "LiteralOutput" );
        LOG.debug( "Setting literal output (requested=" + literalOutput.isRequested() + ")" );
        // ...and set to the required result
        literalOutput.setValue( "" + sleepSeconds );

        // BoundingBoxOutput will just be retrieved from the ProcessletOutput object...
        BoundingBoxOutput bboxOutput = (BoundingBoxOutput) out.getParameter( "BBOXOutput" );
        LOG.debug( "Setting bbox output (requested=" + bboxOutput.isRequested() + ")" );
        // ...and set to the required result
        bboxOutput.setValue( bboxInput.getValue() );

        // ComplexOutput objects can become very huge. Therefore it is essential to stream the
        // result.
        // Initially we have to get a ComplexOutput object from the ProcessletOutput...
        ComplexOutput xmlOutput = (ComplexOutput) out.getParameter( "XMLOutput" );
        LOG.debug( "Setting XML output (requested=" + xmlOutput.isRequested() + ")" );

        // .. and use the XMLStreamWriter to write the results to. This case handles XML based data.
        try {
            XMLStreamWriter writer = xmlOutput.getXMLStreamWriter();
            XMLAdapter.writeElement( writer, xmlInput.getValueAsXMLStream() );
        } catch ( XMLStreamException e ) {
            LOG.error( e.getMessage() );
        } catch ( IOException e ) {
            LOG.error( e.getMessage() );
        }

        // Another ComplexOutput is binary, e.g. an image...
        ComplexOutput binaryOutput = (ComplexOutput) out.getParameter( "BinaryOutput" );
        LOG.debug( "Setting binary output (requested=" + binaryOutput.isRequested() + ")" );

        // which is streamed using a binary stream.
        try {
            InputStream is = binaryInput.getValueAsBinaryStream();
            BufferedOutputStream os = new BufferedOutputStream( binaryOutput.getBinaryOutputStream() );
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
                os.write( buffer, 0, bytesRead );
            }
            os.flush();
        } catch ( IOException e1 ) {
            LOG.error( e1.getMessage() );
        }

        /**
         * 
         * 4.) THAT'S ALL!
         * 
         * deegreeWPS will take care about delivering the result to the client in an OGC-compliant
         * fashion.
         * 
         * 
         */

        LOG.trace( "END ParameterDemoProcesslet#execute()" );
    }

    private int determineSleepTime( LiteralInput input ) {

        int seconds = -1;
        String uom = input.getUOM();

        LOG.debug( "dataType: " + input.getDataType() + ", uom: " + input.getUOM() );

        // NOTE: it is guaranteed (by the deegree WPS) that the UOM is always
        // one of the UOMs specified in the process definition
        if ( "seconds".equals( uom ) ) {
            LOG.debug( "Sleep time given in seconds" );
            seconds = (int) Double.parseDouble( input.getValue() );
        } else if ( "minutes".equals( uom ) ) {
            LOG.debug( "Sleep time given in minutes" );
            seconds = (int) ( Double.parseDouble( input.getValue() ) * 60 );
        }
        return seconds;
    }

    @Override
    public void destroy() {
        LOG.debug( "ParameterDemoProcesslet#destroy() called" );
    }

    @Override
    public void init() {
        LOG.debug( "ParameterDemoProcesslet#init() called" );
    }
}
