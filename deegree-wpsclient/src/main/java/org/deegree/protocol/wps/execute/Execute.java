package org.deegree.protocol.wps.execute;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.tools.FillDataInput;
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

    ResponseForm responseForm;

    String language;

    DescribeProcess desribeProcess;

    ProcessDescription processDescription;

    FillDataInput fillDataInput;

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
        DescribeProcess describeProcess = new DescribeProcess( url);

        this.desribeProcess = describeProcess;
    }

    public Execute( ProcessDescription processDesription, List<DataInputExecute> dataInputExecuteList,
                    ResponseForm responseForm, URL url ) {
        identifier = processDesription.getIdentifier();
        version = processDesription.getProcessVersion();
        service = processDesription.getService();
        schemaLocation = processDesription.getSchemaLocation();
        status = processDesription.isStatusSupported();
        store = processDesription.isStoreSupported();
        this.dataInputExecuteList = dataInputExecuteList;
        this.responseForm = responseForm;
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

        Element dataInputs = new Element( "DataInputs", wpsNamespace );

        for ( int i = 0; i < dataInputExecuteList.size(); i++ ) {
            DataInputExecute dataInputExecute = dataInputExecuteList.get( i );

            Element input = new Element( "Input", wpsNamespace );

            Element identifier = new Element( "Identifier", owsNamespace );
            // identifier.setText(processDescription.getDataInputs().get(i).getIdentifier());

            input.addContent( identifier );

            identifier.setText( dataInputExecute.getIdentifier() );

            if ( dataInputExecute.getInputFormChoice().getData().getDataType().getComplexData() != null ) {

                Element complexValueElement = new Element( "ComplexValue", wpsNamespace );
                // input.addContent( complexValueElement );

                ComplexData complexData = dataInputExecute.getInputFormChoice().getData().getDataType().getComplexData();

                if ( complexData.getEncoding() != null ) {
                    Attribute formatAttribute = new Attribute( "encoding", complexData.getEncoding() );
                    complexValueElement.setAttribute( formatAttribute );
                }

                if ( complexData.getMimeType() != null ) {
                    Attribute mimeTypeAttribute = new Attribute( "format", complexData.getMimeType() );
                    complexValueElement.setAttribute( mimeTypeAttribute );
                }

                if ( complexData.getSchema() != null ) {
                    Attribute schemaAttribute = new Attribute( "schema", complexData.getSchema() );
                    complexValueElement.setAttribute( schemaAttribute );
                }

                if ( dataInputExecute.getInputFormChoice().getReference() != null ) {

                }

                if ( dataInputExecute.getInputFormChoice().getData() != null ) {

                }

                Element complexValueElementReference = new Element( "Reference", wpsNamespace );
                String wfs = "http://demo.deegree.org/deegree-wfs/services?SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TypeName=app:Springs&namespace=xmlns%28app=http://www.deegree.org/app%29&FILTER=%28%3CFilter%20xmlns:app=%22http://www.deegree.org/app%22%3E%3CPropertyIsEqualTo%3E%3CPropertyName%3Eapp:objectid%3C/PropertyName%3E%3CLiteral%3E3%3C/Literal%3E%3C/PropertyIsEqualTo%3E%3C/Filter%3E%29";

                Attribute referenceAttribute = new Attribute( "href", complexData.getObject().toString(),
                                                              xlinkNamespace );
                complexValueElementReference.setAttribute( referenceAttribute );
                // complexValueElement.addContent( complexValueElementReference );
                input.addContent( complexValueElementReference );

            }

            if ( dataInputExecute.getInputFormChoice().getData().getDataType().getLiteralData() != null ) {

                LiteralData literalData = dataInputExecute.getInputFormChoice().getData().getDataType().getLiteralData();
                Element dataElement = new Element( "Data", wpsNamespace );
                input.addContent( dataElement );

                Element literalValueElement = new Element( "LiteralData", wpsNamespace );
                dataElement.addContent( literalValueElement );

                if ( literalData.getDataType() != null ) {

                    Attribute dataTypeAttribute = new Attribute( "dataType", literalData.getDataType() );
                    // literalValueElement.setAttribute( dataTypeAttribute );
                }

                if ( literalData.getUom() != null ) {
                    Attribute uomTypeAttribute = new Attribute( "uom", literalData.getUom() );
                    // literalValueElement.setAttribute( uomTypeAttribute );fillDataInput
                }

                // if (literalData.getLiteralData()()!=null){
                // Attribute defaultValueAttribute = new
                // Attribute("defaultValue",fillDataInput.getDataInputs().get(i).getInputFormChoice().getLiteralData().getDefaulValue());
                // // literalValueElement.setAttribute( defaultValueAttribute );
                // }

            }

            dataInputs.addContent( input );

        }

        root.addContent( dataInputs );

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
            Attribute lineage = new Attribute( "lineage", String.valueOf( responseForm.getResponseDocument().lineage ) );
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

            System.out.println( "responseForm added..." );
            root.addContent( responseFormElement );

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
