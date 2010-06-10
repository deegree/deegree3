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

package org.deegree.protocol.wps.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.stream.XMLStreamException;

import org.deegree.protocol.wps.describeprocess.DataInputDescribeProcess;
import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.execute.BodyReference;
import org.deegree.protocol.wps.execute.ComplexData;
import org.deegree.protocol.wps.execute.Data;
import org.deegree.protocol.wps.execute.DataInputExecute;
import org.deegree.protocol.wps.execute.DataType;
import org.deegree.protocol.wps.execute.Execute;
import org.deegree.protocol.wps.execute.InputFormChoiceExecute;
import org.deegree.protocol.wps.execute.LiteralData;
import org.deegree.protocol.wps.execute.OutputDefinition;
import org.deegree.protocol.wps.execute.RawOutputData;
import org.deegree.protocol.wps.execute.Reference;
import org.deegree.protocol.wps.execute.ResponseDocument;
import org.deegree.protocol.wps.execute.ResponseForm;

/**
 * CreateExecuteRequest creates the ExecuteRequest with the inputs assigned via a list of InputObjects and a list of
 * OutputConfigurations Each InputObject requires identifier and input as mandatory All outputs will be returned if no
 * further configuration are stated
 * 
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class BuildExecuteObjects {

    private List<DataInputDescribeProcess> dataInputs;

    private List<DataInputExecute> dataInputExecuteList;

    private List<OutputConfiguration> outputConfigurationList;

    private List<ComplexData> complexDataList;

    private String identifier;

    private String process;

    private String version;

    private boolean store;

    private boolean status;

    private String schemaLocation;

    private Object[] inputParams;

    private ProcessDescription processDescription;

    private List<ResponseForm> responseFormList = new ArrayList();

    public BuildExecuteObjects( List<InputObject> dataInputList, List<OutputConfiguration> outputConfigurationList,
                                ProcessDescription processDesription ) {

        this.processDescription = processDesription;
        this.identifier = processDesription.getIdentifier();
        this.version = processDesription.getVersion();
        this.status = processDesription.isStatusSupported();
        this.store = processDesription.isStoreSupported();
        this.schemaLocation = processDesription.getSchemaLocation();
        setInputs( dataInputList );
        System.out.println ("outputConfigurationList: " + outputConfigurationList.size());

        if ( outputConfigurationList.size() == 0 ) {
            this.outputConfigurationList = createOutputConfigurationList();
        }


        this.setOutputs( this.outputConfigurationList );

    }

    public BuildExecuteObjects( List<InputObject> dataInputList, ProcessDescription processDesription ) {

        this.processDescription = processDesription;
        this.identifier = processDesription.getIdentifier();
        this.version = processDesription.getVersion();
        this.status = processDesription.isStatusSupported();
        this.store = processDesription.isStoreSupported();
        this.schemaLocation = processDesription.getSchemaLocation();
        setInputs( dataInputList );

        if ( outputConfigurationList == null ) {
            this.outputConfigurationList = createOutputConfigurationList();
        }
        this.setOutputs( this.outputConfigurationList );

    }

    public BuildExecuteObjects( ProcessDescription processDesription ) {

        this.processDescription = processDesription;
        this.identifier = processDesription.getIdentifier();
        this.version = processDesription.getVersion();
        this.status = processDesription.isStatusSupported();
        this.store = processDesription.isStoreSupported();
        this.schemaLocation = processDesription.getSchemaLocation();

        if ( outputConfigurationList == null ) {
            this.outputConfigurationList = createOutputConfigurationList();
        }
        this.setOutputs( this.outputConfigurationList );

    }

    private List<OutputConfiguration> createOutputConfigurationList() {
        List<OutputConfiguration> outputConfigurationList = new ArrayList();
        for ( int i = 0; i < processDescription.getProcessOutputs().size(); i++ ) {
            System.out.println( "process...." );
            outputConfigurationList.add( createOutputConfiguration( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getIdentifier() ) );
        }
        return outputConfigurationList;

    }

    private OutputConfiguration createOutputConfiguration( String identifier ) {
        OutputConfiguration outputConfiguration = new OutputConfiguration( identifier );
        outputConfiguration.setAsReference( true );
        outputConfiguration.setLineage( true );
        outputConfiguration.setRawOrResp( false );
        outputConfiguration.setStatus( true );
        outputConfiguration.setStore( true );
        return outputConfiguration;

    }

    /**
     * writes the Inputs and its attribute into the respective Object of the Execute package
     * 
     * @param input
     *            detailed information on the input
     * 
     * 
     */
    private void setInput( InputObject input ) {

        int c = -99;

        for ( int i = 0; i < processDescription.getDataInputs().size(); i++ ) {
            if ( processDescription.getDataInputs().get( i ).getIdentifier().equalsIgnoreCase( input.getIdentifier() ) )
                c = i;

        }

        DataInputDescribeProcess dataInputDescribeProcess = processDescription.getDataInputs().get( c );

        DataInputExecute dataInputExecute = new DataInputExecute();

        dataInputExecute.setIdentifier( input.getIdentifier() );

        InputFormChoiceExecute inputFormChoice = new InputFormChoiceExecute();

        ComplexData complexData = null;
        if ( dataInputDescribeProcess.getInputFormChoice().getComplexData() != null ) {

            complexData = new ComplexData();
            complexData.setEncoding( dataInputDescribeProcess.getInputFormChoice().getComplexData().getDefaulT().getEncoding() );
            complexData.setMimeType( dataInputDescribeProcess.getInputFormChoice().getComplexData().getDefaulT().getMimeType() );
            complexData.setSchema( dataInputDescribeProcess.getInputFormChoice().getComplexData().getDefaulT().getSchema() );
            complexData.setObject( input.getInput() );

        }
        LiteralData literalData = null;
        if ( dataInputDescribeProcess.getInputFormChoice().getLiteralData() != null ) {

            literalData = new LiteralData();
            literalData.setDataType( dataInputDescribeProcess.getInputFormChoice().getLiteralData().getDataType() );
            if ( dataInputDescribeProcess.getInputFormChoice().getLiteralData().getUom() != null )
                literalData.setUom( dataInputDescribeProcess.getInputFormChoice().getLiteralData().getUom().getDefauLt() );
            literalData.setLiteralData( String.valueOf( input.getInput() ) );
        }

        if ( dataInputDescribeProcess.getInputFormChoice().getBoundingBoxData() != null ) {
            // TO DO
        }

        if ( input.isAsReference() == false ) {
            Data data = new Data();
            DataType dataType = new DataType();
            data.setDataType( dataType );
            dataType.setComplexData( complexData );
            dataType.setLiteralData( literalData );

            inputFormChoice.setData( data );

        } else {

            Reference reference = new Reference();

            BodyReference bodyReferenceObject = new BodyReference();

            reference.setBody( input.getBody() );

            reference.setBodyReference( bodyReferenceObject );

            bodyReferenceObject.setHref( input.getBodyReferenceHref() );

            reference.setHref( input.getInput().toString() );

            reference.setEncoding( input.getEncoding() );

            reference.setMethod( input.getMethod() );

            reference.setMimeType( input.getMimeType() );

            reference.setSchema( input.getSchema() );

            inputFormChoice.setReference( reference );
        }
        dataInputExecute.setInputFormChoice( inputFormChoice );

        dataInputExecuteList.add( dataInputExecute );

    }

    /**
     * starts the method setInput for each member of the list
     * 
     * @param inputList
     *            List of all the Inputs handed to the WPS
     */
    private void setInputs( List<InputObject> inputList ) {
        if ( inputList != null ) {
            dataInputExecuteList = new ArrayList();
            for ( int i = 0; i < inputList.size(); i++ ) {
                this.setInput( inputList.get( i ) );
            }
        }

    }

    /**
     * starts the method setOutput for each member of the list
     * 
     * @param outputConfiguraionList
     *            List of all the Configurations of the requested outputs
     */
    private void setOutputs( List<OutputConfiguration> outputConfiguraionList ) {
        for ( int i = 0; i < outputConfiguraionList.size(); i++ ) {
            setOutput( outputConfiguraionList.get( i ) );
        }
    }

    /**
     * writes the OutputConfiguration into the respective Object of the Execute package
     * 
     * @param outputConfiguration
     *            Configures how to return the output *
     * 
     */
    private void setOutput( OutputConfiguration outputConfiguration ) {

        ResponseForm responseForm = new ResponseForm();
        List<OutputDefinition> outputDefinitionList = new ArrayList();

        for ( int i = 0; i < processDescription.getProcessOutputs().size(); i++ )

        {
            if ( outputConfiguration.isRawOrResp() == false ) {
                ResponseDocument responseDocument = new ResponseDocument();

                responseDocument.setLineage( outputConfiguration.isLineage() );
                responseDocument.setStatus( outputConfiguration.isStatus() );
                responseDocument.setStoreExecuteResponse( outputConfiguration.isStore() );

                processDescription.getProcessOutputs().get( i ).getOutputDescripton();
                OutputDefinition outputDefinition = new OutputDefinition();

                outputDefinition.setAbstraCt( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getAbstraCt() );
                outputDefinition.setIdentifier( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getIdentifier() );
                outputDefinition.setTitle( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getTitle() );

                if ( processDescription.getProcessOutputs().get( 0 ).getOutputDescripton().getOutputFormChoice().getComplexOutput() != null ) {
                    outputDefinition.setEncoding( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getComplexOutput().getDefaulT().getEncoding() );
                    outputDefinition.setMimeType( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getComplexOutput().getDefaulT().getMimeType() );
                    outputDefinition.setSchema( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getComplexOutput().getDefaulT().getSchema() );

                }

                if ( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getLiteralOutput() != null ) {
                    outputDefinition.setUom( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getLiteralOutput().getUom().getDefauLt() );
                }

                outputDefinition.setAsReference( outputConfiguration.isAsReference() );

                outputDefinitionList.add( outputDefinition );

                responseDocument.setOutput( outputDefinitionList );
                responseForm.setResponseDocument( responseDocument );
            } else {
                RawOutputData rawDataOutput = new RawOutputData();
                rawDataOutput.setIdentifier( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getIdentifier() );

                responseForm.setRawOutputData( rawDataOutput );
            }

            if ( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getBoundingBoxData() != null ) {

            }

            processDescription.getProcessOutputs().get( i ).getOutputDescripton().getIdentifier();
        }

        responseFormList.add( responseForm );

    }

    public ByteArrayOutputStream createExecuteRequest() {
               Execute execute = new Execute( processDescription, dataInputExecuteList, responseFormList, null );
               ByteArrayOutputStream out= new ByteArrayOutputStream();

               try {
                   out=execute.returnExecuteRequest();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println (out);
        return out;
    }
    
    public void sendExecuteRequest(){
        
    }

}
