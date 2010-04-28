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
package org.deegree.protocol.wps.describeprocess;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * 
 * TODO  This class parses the DescribeProcess Document and writes the elements into the class "ProcessDescription"
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeProcess {
    
    private static Logger LOG = LoggerFactory.getLogger( DescribeProcess.class );

    private XMLAdapter capabilities;
    
    private XMLAdapter describeProcessDoc;

    private String service;

    private String version;

    private String lang;
    
    private String schemaLocation;

    private static final NamespaceContext NS_CONTEXT;
    
    static {
        NS_CONTEXT = new NamespaceContext();
        NS_CONTEXT.addNamespace("ows", "http://www.opengis.net/ows/1.1" );
        
    }


    /**
     * 
     * @param url
     */
    public DescribeProcess( URL url ) {
        try {
            this.capabilities = new XMLAdapter( url );
        } catch ( XMLProcessingException e ) {
            LOG.error( e.getMessage() );
        }
   }


    public DescribeProcess(XMLAdapter describeProcessDoc){
        this.describeProcessDoc = describeProcessDoc;
        OMElement rootElement = describeProcessDoc.getRootElement();
        this.service = rootElement.getAttributeValue( new QName( "service" ) );
        this.version = rootElement.getAttributeValue( new QName( "version" ) );
        this.lang = rootElement.getAttributeValue( new QName( "lang" ) );
        this.schemaLocation = rootElement.getAttributeValue( new QName( "schemaLocation" ) );
        
        
        
    }

    /**
     * 
     * @return List of processDescription
     */
    public List<ProcessDescription> getProcessDescriptions() {
        List<ProcessDescription> processDescriptionList = new LinkedList<ProcessDescription>();
        ProcessDescription processDescription = null;
        List<OMElement> processOMElementList = capabilities.getElements( capabilities.getRootElement(),
                                                                         new XPath( "ProcessDescription",
                                                                                    NS_CONTEXT ) );

        for ( Iterator iterator = processOMElementList.iterator(); iterator.hasNext(); ) {

            OMElement element = (OMElement) iterator.next();

            NS_CONTEXT.addNamespace( element.getQName().getPrefix(), element.getQName().getNamespaceURI() );

        }

        processDescription = new ProcessDescription();

        for ( Iterator iterator2 = capabilities.getRootElement().getAllAttributes(); iterator2.hasNext(); ) {
            OMAttribute attribute = (OMAttribute) iterator2.next();

            if ( attribute.getLocalName().equalsIgnoreCase( "service" ) )
                processDescription.setService( attribute.getAttributeValue() );

            if ( attribute.getLocalName().equalsIgnoreCase( "version" ) )
                processDescription.setVersion( attribute.getAttributeValue() );

            if ( attribute.getLocalName().equalsIgnoreCase( "lang" ) )
                processDescription.setLanguage( attribute.getAttributeValue() );

            if ( attribute.getLocalName().equalsIgnoreCase( "schemaLocation" ) )
                processDescription.setSchemaLocation( attribute.getAttributeValue() );

           LOG.debug( "processDescription.getSchemaLocation: " + processDescription.getSchemaLocation() );

        }

        for ( Iterator iterator = processOMElementList.iterator(); iterator.hasNext(); ) {

            OMElement processOMElement = (OMElement) iterator.next();

            for ( Iterator iterator2 = processOMElement.getAllAttributes(); iterator2.hasNext(); ) {

                OMAttribute attribute = (OMAttribute) iterator2.next();

                if ( attribute.getLocalName().equalsIgnoreCase( "statusSupported" ) )
                    processDescription.setStatusSupported( Boolean.getBoolean( attribute.getAttributeValue() ) );

                if ( attribute.getLocalName().equalsIgnoreCase( "storeSupported" ) )
                    processDescription.setStoreSupported( Boolean.getBoolean( attribute.getAttributeValue() ) );

                if ( attribute.getLocalName().equalsIgnoreCase( "processVersion" ) )
                    processDescription.setProcessVersion( attribute.getAttributeValue() );
            }

            processDescription.setAbstraCt( capabilities.getNodeAsString(
                                                                          processOMElement,
                                                                          new XPath( "ows:Abstract", NS_CONTEXT ),
                                                                          null ) );

            processDescription.setIdentifier( capabilities.getNodeAsString( processOMElement,
                                                                            new XPath( "ows:Identifier",
                                                                                       NS_CONTEXT ), null ) );

            processDescription.setProfile( capabilities.getNodeAsString( processOMElement,
                                                                         new XPath( "ows:Profile", NS_CONTEXT ),
                                                                         null ) );

            processDescription.setTitle( capabilities.getNodeAsString( processOMElement, new XPath( "ows:Title",
                                                                                                    NS_CONTEXT ),
                                                                       null ) );

            List<DataInputDescribeProcess> dataInputList = new LinkedList();

            OMElement dataInputs = capabilities.getElement( processOMElement,
                                                            new XPath( "DataInputs", NS_CONTEXT ) );

            List<OMElement> inputOMElementList = capabilities.getElements( dataInputs, new XPath( "Input",
                                                                                                  NS_CONTEXT ) );

            for ( int i = 0; i < inputOMElementList.size(); i++ )

            {
                DataInputDescribeProcess dataInput = new DataInputDescribeProcess();

                OMElement inputOmelement = (OMElement) inputOMElementList.get( i );

                dataInput.setAbstraCt( capabilities.getNodeAsString( inputOmelement, new XPath( "ows:Abstract",
                                                                                                NS_CONTEXT ),
                                                                     null ) );

                dataInput.setIdentifier( capabilities.getNodeAsString( inputOmelement, new XPath( "ows:Identifier",
                                                                                                  NS_CONTEXT ),
                                                                       null ) );

                dataInput.setMaxOccurs( inputOmelement.getAttributeValue( new QName( "maxOccurs" ) ) );

                dataInput.setMinOccurs( inputOmelement.getAttributeValue( new QName( "minOccurs" ) ) );

                dataInput.setTitle( capabilities.getNodeAsString( inputOmelement, new XPath( "ows:Title",
                                                                                             NS_CONTEXT ), null ) );

                InputFormChoiceDescribeProcess inputFormChoice = new InputFormChoiceDescribeProcess();

                if ( capabilities.getElement( inputOmelement, new XPath( "LiteralData", NS_CONTEXT ) ) != null ) {

                    OMElement literalDataOMELement = capabilities.getElement( inputOmelement,
                                                                              new XPath( "LiteralData",
                                                                                         NS_CONTEXT ) );

                    LiteralInputData literalData = new LiteralInputData();

                    OMElement dataTypeOMELement = capabilities.getElement( literalDataOMELement,
                                                                           new XPath( "ows:DataType", NS_CONTEXT ) );

                    literalData.setDefaulValue( capabilities.getNodeAsString( literalDataOMELement,
                                                                              new XPath( "DefaultValue",
                                                                                         NS_CONTEXT ), null ) );

                    if ( dataTypeOMELement != null ) {
                        for ( Iterator itera = dataTypeOMELement.getAllAttributes(); itera.hasNext(); ) {
                            OMAttribute dataTypeAttribute = (OMAttribute) itera.next();
                            if ( dataTypeAttribute.getLocalName().equalsIgnoreCase( "reference" ) ) {
                                literalData.setDataType( dataTypeAttribute.getAttributeValue() );

                            }

                        }

                    }

                    OMElement uomOmeElement = capabilities.getElement( literalDataOMELement,
                                                                       new XPath( "UOMs", NS_CONTEXT ) );

                    if ( uomOmeElement != null ) {

                        OMElement defaulOMElement = capabilities.getElement( uomOmeElement,
                                                                             new XPath( "Default", NS_CONTEXT ) );

                        OMElement owsUOMDefaulOMElement = capabilities.getElement( defaulOMElement,
                                                                                   new XPath( "ows:UOM",
                                                                                              NS_CONTEXT ) );

                        Uom uom = new Uom();
                        uom.setDefauLt( owsUOMDefaulOMElement.getText() );

                        // uom.setSupported( supported ):

                        literalData.setUom( uom );
                    }
                    inputFormChoice.setLiteralData( literalData );

                }

                BoundingBoxData boundingBoxData = new BoundingBoxData();
                inputFormChoice.setBoundingBoxData( boundingBoxData );

                ComplexData complexData = new ComplexData();

                Format defaulT = new Format();

                if ( capabilities.getElement( inputOmelement, new XPath( "ComplexData", NS_CONTEXT ) ) != null ) {
                    OMElement comlexDataInput = capabilities.getElement( inputOmelement, new XPath( "ComplexData",
                                                                                                    NS_CONTEXT ) );

                    OMElement comlexDataDefaultInput = capabilities.getElement( comlexDataInput,
                                                                                new XPath( "Default", NS_CONTEXT ) );

                    OMElement formatOmelement = capabilities.getElement( comlexDataDefaultInput,
                                                                         new XPath( "Format", NS_CONTEXT ) );

                    defaulT.setMimeType( capabilities.getNodeAsString( formatOmelement, new XPath( "MimeType",
                                                                                                   NS_CONTEXT ),
                                                                       null ) );

                    defaulT.setEncoding( capabilities.getNodeAsString( formatOmelement, new XPath( "Encoding",
                                                                                                   NS_CONTEXT ),
                                                                       null ) );

                    defaulT.setSchema( capabilities.getNodeAsString( formatOmelement, new XPath( "Schema",
                                                                                                 NS_CONTEXT ),
                                                                     null ) );

                    complexData.setDefaulT( defaulT );

                    OMElement comlexDataSupprotedInput = capabilities.getElement( comlexDataInput,
                                                                                  new XPath( "Supported",
                                                                                             NS_CONTEXT ) );

                    List<OMElement> formatList = capabilities.getElements( comlexDataSupprotedInput,
                                                                           new XPath( "Format", NS_CONTEXT ) );

                    for ( int ii = 0; ii < formatList.size(); ii++ ) {

                        formatOmelement = formatList.get( ii );

                        Format supported = new Format();

                        supported.setMimeType( capabilities.getNodeAsString( formatOmelement,
                                                                             new XPath( "MimeType", NS_CONTEXT ),
                                                                             null ) );

                        supported.setEncoding( capabilities.getNodeAsString( formatOmelement,
                                                                             new XPath( "Encoding", NS_CONTEXT ),
                                                                             null ) );

                        supported.setSchema( capabilities.getNodeAsString( formatOmelement,
                                                                           new XPath( "Schema", NS_CONTEXT ),
                                                                           null ) );

                        complexData.addSupported( supported );

                    }

                    inputFormChoice.setComplexData( complexData );

                }

                dataInput.setInputFormChoice( inputFormChoice );

                dataInputList.add( dataInput );

            }

            processDescription.setDataInputs( dataInputList );

            OMElement processOutputsOMElement = capabilities.getElement( processOMElement, new XPath( "ProcessOutputs",
                                                                                                      NS_CONTEXT ) );

            List<OMElement> processOutputOMElementList = capabilities.getElements( processOutputsOMElement,
                                                                                   new XPath( "Output",
                                                                                              NS_CONTEXT ) );

            List<ProcessOutput> processOutputList = new LinkedList();

            for ( int i = 0; i < processOutputOMElementList.size(); i++ ) {
                ProcessOutput procssOutput = new ProcessOutput();

                OMElement processOutputOMElement = processOutputOMElementList.get( i );

                OutputDescription outputDescription = new OutputDescription();

                outputDescription.setIdentifier( capabilities.getNodeAsString( processOutputOMElement,
                                                                               new XPath( "ows:Identifier",
                                                                                          NS_CONTEXT ), null ) );

                outputDescription.setAbstraCt( capabilities.getNodeAsString( processOutputOMElement,
                                                                             new XPath( "ows:Abstract",
                                                                                        NS_CONTEXT ), null ) );

                outputDescription.setTitle( capabilities.getNodeAsString( processOutputOMElement,
                                                                          new XPath( "ows:Title", NS_CONTEXT ),
                                                                          null ) );

                OMElement complexOutputOMElement = capabilities.getElement( processOutputOMElement,
                                                                            new XPath( "ComplexOutput",
                                                                                       NS_CONTEXT ) );

                OMElement defaultComplexOutputOMElement = capabilities.getElement( complexOutputOMElement,
                                                                                   new XPath( "Default",
                                                                                              NS_CONTEXT ) );

                OMElement formatDefaultComplexOutputOMElement = capabilities.getElement( defaultComplexOutputOMElement,
                                                                                         new XPath( "Format",
                                                                                                    NS_CONTEXT ) );

                OutputFormChoice outputFormChoice = new OutputFormChoice();
                ComplexData complexOutput = new ComplexData();

                Format defaulT = new Format();
                defaulT.setEncoding( capabilities.getNodeAsString( formatDefaultComplexOutputOMElement,
                                                                   new XPath( "Encoding", NS_CONTEXT ), null ) );
                defaulT.setMimeType( capabilities.getNodeAsString( formatDefaultComplexOutputOMElement,
                                                                   new XPath( "MimeType", NS_CONTEXT ), null ) );
                defaulT.setSchema( capabilities.getNodeAsString( formatDefaultComplexOutputOMElement,
                                                                 new XPath( "Schema", NS_CONTEXT ), null ) );

                complexOutput.setDefaulT( defaulT );

                OMElement supportedComplexOutputOMElement = capabilities.getElement( complexOutputOMElement,
                                                                                     new XPath( "Supported",
                                                                                                NS_CONTEXT ) );

                List<OMElement> formatsupportedComplexOutputOMElementList = capabilities.getElements(
                                                                                                      supportedComplexOutputOMElement,
                                                                                                      new XPath(
                                                                                                                 "Format",
                                                                                                                 NS_CONTEXT ) );

                // 

                for ( int ii = 0; ii < formatsupportedComplexOutputOMElementList.size(); ii++ ) {
                    OMElement formatsupportedComplexOutputOMElement = formatsupportedComplexOutputOMElementList.get( ii );
                    Format supported = new Format();
                    supported.setEncoding( capabilities.getNodeAsString( formatsupportedComplexOutputOMElement,
                                                                         new XPath( "Encoding", NS_CONTEXT ),
                                                                         null ) );
                    supported.setMimeType( capabilities.getNodeAsString( formatsupportedComplexOutputOMElement,
                                                                         new XPath( "MimeType", NS_CONTEXT ),
                                                                         null ) );
                    supported.setSchema( capabilities.getNodeAsString( formatsupportedComplexOutputOMElement,
                                                                       new XPath( "Schema", NS_CONTEXT ), null ) );

                    LOG.debug( "supported.getSchema: " + supported.getSchema() );
                    complexOutput.addSupported( supported );

                }

                outputFormChoice.setComplexOutput( complexOutput );

                LiteralOutputData literalOutputData = new LiteralOutputData();

                OMElement literalOutputOMElement = capabilities.getElement( processOutputOMElement,
                                                                            new XPath( "LiteralOutput",
                                                                                       NS_CONTEXT ) );

                outputDescription.setOutputFormChoice( outputFormChoice );

                OMElement BoundingBoxOutputOMElement = capabilities.getElement( processOutputOMElement,
                                                                                new XPath( "BoudingBoxOutput",
                                                                                           NS_CONTEXT ) );
                procssOutput.setOutputDescription( outputDescription );
                processOutputList.add( procssOutput );

            }

            processDescription.setProcessOutputs( processOutputList );

            String wsdl = null;;
            processDescription.setWSDL( wsdl );

        }

        processDescriptionList.add( processDescription );

        return processDescriptionList;
    }
}