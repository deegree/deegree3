package org.deegree.protocol.wps.execute;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.tools.CreateExecuteRequest;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

public class Execute {

    String service;

    String request;

    String version;

    String identifier;

    List<DataInputExecute> dataInputExecuteList;

    List<ResponseForm> responseFormList;

    ResponseForm responseForm;

    String language;

    DescribeProcess desribeProcess;

    ProcessDescription processDescription;

    CreateExecuteRequest fillDataInput;

    boolean store;

    boolean status;

    String schemaLocation;

    Namespace wpsNamespace = Namespace.getNamespace( "wps", "http://www.opengis.net/wps/1.0.0" );

    Namespace owsNamespace = Namespace.getNamespace( "ows", "http://www.opengis.net/ows/1.1" );

    Namespace xsiNamespace = Namespace.getNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );

    Namespace xlinkNamespace = Namespace.getNamespace( "xlink", "http://www.w3.org/1999/xlink" );

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

    public void createPost() {

        Element root = new Element( "execute", wpsNamespace );

        Attribute serviceAttribute = new Attribute( "service", service );
        root.setAttribute( serviceAttribute );

        Attribute versionAttribute = new Attribute( "version", version );
        root.setAttribute( versionAttribute );

        Attribute storeAttribute = new Attribute( "store", String.valueOf( store ) );
        root.setAttribute( storeAttribute );

        Attribute statusAttribute = new Attribute( "status", String.valueOf( status ) );
        root.setAttribute( statusAttribute );

        Attribute schemaLocationAttribute = new Attribute( "schemaLocation", schemaLocation, xsiNamespace ); // namespace
        // setzen

        root.setAttribute( schemaLocationAttribute );

        Element identifierProcess = new Element( "Identifier", owsNamespace );

        root.addContent( identifierProcess );

        identifierProcess.setText( identifier );

        if ( dataInputExecuteList != null ) {
            Element dataInputs = new Element( "DataInputs", wpsNamespace );

            for ( int i = 0; i < dataInputExecuteList.size(); i++ ) {
                DataInputExecute dataInputExecute = dataInputExecuteList.get( i );

                Element input = new Element( "Input", wpsNamespace );

                Element identifier = new Element( "Identifier", owsNamespace );
                // identifier.setText(processDescription.getDataInputs().get(i).getIdentifier());

                input.addContent( identifier );

                identifier.setText( dataInputExecute.getIdentifier() );

                InputFormChoiceExecute inputFormChoice = dataInputExecute.getInputFormChoice();

                if ( inputFormChoice.getReference() != null ) {
                    Element referenceElement = new Element( "Reference", wpsNamespace );
                    Attribute referenceAttribute = new Attribute(
                                                                  "href",
                                                                  dataInputExecute.getInputFormChoice().getReference().getHref() );
                    referenceElement.setAttribute( referenceAttribute );
                    input.addContent( referenceElement );
                }

                if ( inputFormChoice.getData() != null ) {
                    if ( inputFormChoice.getData().getDataType() != null ) {
                        DataType dataType = inputFormChoice.getData().getDataType();
                        if ( dataType.getLiteralData() != null ) {
                            Element dataElement = new Element( "Data", wpsNamespace );
                            input.addContent( dataElement );

                            Element literalValueElement = new Element( "LiteralData", wpsNamespace );
                            dataElement.addContent( literalValueElement );
                            literalValueElement.addContent( dataType.getLiteralData().getLiteralData() );
                        }
                        if ( dataType.getComplexData() != null ) {
                            System.out.println( "complexData" );
                            Element complexValueElementRawData = new Element( "Data", wpsNamespace );
                            Element complexValueElementComplexData = new Element( "ComplexData", wpsNamespace );
                            complexValueElementRawData.addContent( complexValueElementComplexData );
                            complexValueElementComplexData.addContent( dataType.getComplexData().getObject().toString() );

                            input.addContent( complexValueElementRawData );

                        }
                        if ( dataType.getBoundingBoxData() != null ) {

                            // TO DO
                        }

                    }
                }

                dataInputs.addContent( input );

            }

            root.addContent( dataInputs );
        }

        for ( int j = 0; j < responseFormList.size(); j++ ) {
            ResponseForm responseForm = responseFormList.get( j );

            if ( responseForm.getRawOutputData() != null ) {
                Element responseFormElement = new Element( "ReponseForm", wpsNamespace );
                Element rawDataOutputElement = new Element( "RawDataOutuput", wpsNamespace );
                responseFormElement.addContent( rawDataOutputElement );
                Element identifier = new Element( "Identifier", owsNamespace );
                identifier.setText( responseForm.getRawOutputData().getIdentifier() );
                rawDataOutputElement.addContent( identifier );
                root.addContent( responseFormElement );

            } else {
                Element responseFormElement = new Element( "ReponseForm", wpsNamespace );
                Element responseDocumentElement = new Element( "ResponseDocument", wpsNamespace );
                responseFormElement.addContent( responseDocumentElement );
                Attribute storeExecuteResponse = new Attribute(
                                                                "storeExecuteResponse",
                                                                String.valueOf( responseForm.getResponseDocument().storeExecuteResponse ) );
                Attribute lineage = new Attribute( "lineage",
                                                   String.valueOf( responseForm.getResponseDocument().lineage ) );
                Attribute status = new Attribute( "status", String.valueOf( responseForm.getResponseDocument().status ) );
                responseDocumentElement.setAttribute( storeExecuteResponse );
                responseDocumentElement.setAttribute( lineage );
                responseDocumentElement.setAttribute( status );

                for ( int i = 0; i < responseForm.getResponseDocument().getOutput().size(); i++ ) {
                    Element outputElement = new Element( "Output", wpsNamespace );
                    Attribute asReference = new Attribute(
                                                           "asReference",
                                                           String.valueOf( responseForm.getResponseDocument().getOutput().get(
                                                                                                                               i ).isAsReference() ) );
                    outputElement.setAttribute( asReference );
                    Element identifier = new Element( "Identifier", owsNamespace );
                    identifier.setText( responseForm.getResponseDocument().getOutput().get( i ).getIdentifier() );
                    outputElement.addContent( identifier );
                    responseDocumentElement.addContent( outputElement );

                }

                root.addContent( responseFormElement );
            }
        }

        writeFile( root, identifier );

    }

    void writeFile( Element root, String identifier ) {
        Document doc = new Document( root );
        // serialize it onto System.out

        XMLOutputter serializer = new XMLOutputter();

        Writer fw = null;

        try {
            fw = new FileWriter( "execute" + identifier + ".xml" );

            fw.write( serializer.outputString( doc ) );
            fw.append( System.getProperty( "line.separator" ) ); // e.g. "\n"
        } catch ( IOException e ) {
            System.err.println( "Konnte Datei nicht erstellen" );
        } finally {
            if ( fw != null )
                try {
                    fw.close();
                } catch ( IOException e ) {
                }
        }

    }

    public void out() {
        ProcessDescription processDescription = desribeProcess.getProcessDescriptions().get( 0 );

        for ( int i = 0; i < processDescription.getDataInputs().size(); i++ ) {
            System.out.println( processDescription.getDataInputs().get( i ).getIdentifier() );
        }
    }
}
