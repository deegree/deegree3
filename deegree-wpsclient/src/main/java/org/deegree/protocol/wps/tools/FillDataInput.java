package org.deegree.protocol.wps.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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




public class FillDataInput { // Rename, Change Input Method

    ProcessDescription processDesription;

    List<DataInputDescribeProcess> dataInputs;

    List<DataInputExecute> dataInputExecuteList;

    List<Object> outputConfiguration;

    List<ComplexData> complexDataList;

    String identifier;

    String process;

    String version;

    boolean store;

    boolean status;

    String schemaLocation;

    ResponseForm responseForm;

    public FillDataInput( DescribeProcess describeProcess ) {

//        complexDataList = new ArrayList();
        List<ProcessDescription> processDescriptionList = describeProcess.getProcessDescriptions();
        this.processDesription = processDescriptionList.get( 0 );
        this.version = processDesription.getVersion();
        this.status = processDesription.isStatusSupported();
        this.store = processDesription.isStoreSupported();
        this.schemaLocation = processDesription.getSchemaLocation();
        this.identifier = processDesription.getIdentifier();
        dataInputExecuteList = new ArrayList();
    }

    public ProcessDescription getProcessDesription() {
        return processDesription;
    }

    public void setProcessDesription( ProcessDescription processDesription ) {
        this.processDesription = processDesription;
    }

    public List<DataInputExecute> getDataInputExecuteList() {
        return dataInputExecuteList;
    }

    public void setData( DataInputDescribeProcess dataInput, Object object ) {
        Scanner in = new Scanner( System.in );

        DataInputExecute dataInputExecute = new DataInputExecute();
        dataInputExecute.setIdentifier( dataInput.getIdentifier() );

        InputFormChoiceExecute inputFormChoice = new InputFormChoiceExecute();
        Data data = new Data();
        DataType dataType = new DataType();
        data.setDataType( dataType );

        inputFormChoice.setData( data );

        if ( dataInput.getInputFormChoice().getComplexData() != null ) {
            ComplexData complexData = new ComplexData();
            dataType.setComplexData( complexData );
            complexData.setEncoding( dataInput.getInputFormChoice().getComplexData().getDefaulT().getEncoding() );
            complexData.setMimeType( dataInput.getInputFormChoice().getComplexData().getDefaulT().getMimeType() );
            complexData.setSchema( dataInput.getInputFormChoice().getComplexData().getDefaulT().getSchema() );

            complexData.setObject( object );
            System.out.println( dataInput.getIdentifier() + ": " );
            complexData.setObject( in.nextLine() );

        }

        if ( dataInput.getInputFormChoice().getLiteralData() != null ) {
            LiteralData literalData = new LiteralData();
            dataType.setLiteralData( literalData );
            literalData.setDataType( dataInput.getInputFormChoice().getLiteralData().getDataType() );
            if ( dataInput.getInputFormChoice().getLiteralData().getUom() != null )
                literalData.setUom( dataInput.getInputFormChoice().getLiteralData().getUom().getDefauLt() );
            literalData.setLiteralData( String.valueOf( object ) );
            System.out.println( dataInput.getIdentifier() + ": " );
            literalData.setLiteralData( in.nextLine() );
        }

        System.out.println( "(r)eference or (d)ata? " );
        String g = ( in.nextLine() );
        if ( g.equalsIgnoreCase( "d" ) )

            inputFormChoice.setData( data );
        else {
            Reference reference = new Reference();
            BodyReference bodyReference = new BodyReference();

            System.out.println( "body: " );
            reference.setBody( in.nextLine() );
            System.out.println( "bodyReference: " );

            reference.setBodyReference( bodyReference );
            bodyReference.setHref( in.nextLine() );

            System.out.println( "encoding: " );
            reference.setEncoding( in.nextLine() );

            System.out.println( "body: " );
            reference.setHref( in.nextLine() );

            System.out.println( "method: " );
            reference.setMethod( in.nextLine() );

            System.out.println( "mimeType: " );
            reference.setMimeType( in.nextLine() );

            System.out.println( "schema: " );
            reference.setSchema( in.nextLine() );

            System.out.println( "reference: " );
            inputFormChoice.setReference( reference );
        }
        dataInputExecute.setInputFormChoice( inputFormChoice );

        dataInputExecuteList.add( dataInputExecute );

    }

    public void setComplexInput( DataInputDescribeProcess dataInput, Object object ) {
        Scanner in = new Scanner( System.in );

        ComplexData complexData = new ComplexData();
        complexData.setEncoding( dataInput.getInputFormChoice().getComplexData().getDefaulT().getEncoding() );
        complexData.setMimeType( dataInput.getInputFormChoice().getComplexData().getDefaulT().getMimeType() );
        complexData.setSchema( dataInput.getInputFormChoice().getComplexData().getDefaulT().getSchema() );
        System.out.println( dataInput.getIdentifier() );

        complexData.setObject( in.nextLine() );
        // complexData.setObject( object );

        DataType dataType = new DataType();
        dataType.setComplexData( complexData );
        complexDataList.add( complexData );
    }

    public void setLiteralInput( DataInputDescribeProcess dataInput, Object object ) {
        Scanner in = new Scanner( System.in );

        LiteralData literalData = new LiteralData();
        literalData.setDataType( dataInput.getInputFormChoice().getLiteralData().getDataType() );
        literalData.setDataType( dataInput.getInputFormChoice().getLiteralData().getUom().getDefauLt() );
        literalData.setLiteralData( in.nextLine() );
    }

    public void setBoundingBoxInput( Object object ) {

    }

    private void setReference( String body, String encoding, String href, String method, String mimeType ) {

    }

    public void setDataInputs( List<DataInputDescribeProcess> dataInputs ) {
        for ( int i = 0; i < dataInputs.size(); i++ ) {
            this.setData( dataInputs.get( i ), "object" );

        }

    }

    public void setDataInput( DataInputDescribeProcess dataInput, Object object, Reference reference ) {

        for ( int i = 0; i < processDesription.getDataInputs().size(); i++ ) {
            DataInputExecute dataInputExecute = new DataInputExecute();
            dataInputExecute.setIdentifier( dataInput.getIdentifier() );

            InputFormChoiceExecute inputFormChoice = new InputFormChoiceExecute();

            Data data = new Data();
            inputFormChoice.setData( data );
            DataType dataType = new DataType();
            data.setDataType( dataType );

            dataInputExecute.setInputFormChoice( inputFormChoice );
            if ( dataInput.getInputFormChoice().getBoundingBoxData() != null ) {
                // dataType.setBoundingBoxData( boundingBoxData )

            }

            if ( dataInput.getInputFormChoice().getComplexData() != null ) {

            }

            if ( dataInput.getInputFormChoice().getLiteralData() != null ) {
                LiteralData literalData = new LiteralData();
                literalData.setDataType( object.toString() );
                literalData.setUom( dataInput.getInputFormChoice().getLiteralData().getUom().toString() );

            }

        }

    }

    public void setDataOutput() {

        Scanner in = new Scanner( System.in );

        responseForm = new ResponseForm();
        List<OutputDefinition> outputDefinitionList = new ArrayList();

        for ( int i = 0; i < processDesription.getProcessOutputs().size(); i++ )

        {
            System.out.println( "(R)awOutputData or Res(p)onsedocument" );
            String input = in.nextLine();   
            if ( input.equalsIgnoreCase( "p" ) ) {
                ResponseDocument responseDocument = new ResponseDocument();
                System.out.println( "lineage? (t) or (f) ?" );
                input = in.nextLine();
                if ( input.equalsIgnoreCase( "t" ) )
                    responseDocument.setLineage( true );
                else
                    responseDocument.setLineage( false );

                System.out.println( "status? (t) or (f) ?" );
                input = in.nextLine();
                if ( input.equalsIgnoreCase( "t" ) )
                    responseDocument.setStatus( true );
                else
                    responseDocument.setStatus( false );

                System.out.println( "store execute Response? (t) or (f) ?" );
                input = in.nextLine();
                if ( input.equalsIgnoreCase( "t" ) )
                    responseDocument.setStoreExecuteResponse( true );
                else
                    responseDocument.setStoreExecuteResponse( false );

                processDesription.getProcessOutputs().get( i ).getOutputDescripton();
                OutputDefinition outputDefinition = new OutputDefinition();

                outputDefinition.setAbstraCt( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getAbstraCt() );
                outputDefinition.setIdentifier( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getIdentifier() );
                outputDefinition.setTitle( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getTitle() );

                if ( processDesription.getProcessOutputs().get( 0 ).getOutputDescripton().getOutputFormChoice().getComplexOutput() != null ) {
                    outputDefinition.setEncoding( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getComplexOutput().getDefaulT().getEncoding() );
                    outputDefinition.setMimeType( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getComplexOutput().getDefaulT().getMimeType() );
                    outputDefinition.setSchema( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getComplexOutput().getDefaulT().getSchema() );

                }

                if ( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getLiteralOutput() != null ) {
                    outputDefinition.setUom( processDesription.getProcessOutputs().get( i ).getOutputDescripton().getOutputFormChoice().getLiteralOutput().getUom().getDefauLt() );
                }

                System.out.println( "as refernce? (t) or (f) ?" );
                input = in.nextLine();
                if ( input.equalsIgnoreCase( "t" ) )
                    outputDefinition.setAsReference( true );
                else
                    outputDefinition.setAsReference( false );

                outputDefinitionList.add( outputDefinition );

                responseDocument.setOutput( outputDefinitionList );
                responseForm.setResponseDocument( responseDocument );
            } else {
                RawOutputData rawDataOutput = new RawOutputData();
                rawDataOutput.setIdentifier( processDesription.getProcessOutputs().get( 0 ).getOutputDescripton().getIdentifier() );

                responseForm.setRawOutputData( rawDataOutput );
            }

            if ( processDesription.getProcessOutputs().get( 0 ).getOutputDescripton().getOutputFormChoice().getBoundingBoxData() != null ) {

            }

            processDesription.getProcessOutputs().get( i ).getOutputDescripton().getIdentifier();
        }

    }

    public void runExecute() {
        Execute execute = new Execute( processDesription, dataInputExecuteList, responseForm, null );
        execute.createPost();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess( String process ) {
        this.process = process;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion( String version ) {
        this.version = version;
    }

    public boolean isStore() {
        return store;
    }

    public void setStore( boolean store ) {
        this.store = store;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus( boolean status ) {
        this.status = status;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation( String schemaLocation ) {
        this.schemaLocation = schemaLocation;
    }

    public List<ComplexData> getComplexDataList() {
        return complexDataList;
    }

}
