//$HeadURL: svn+ssh://georg@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.getcapabilities.ProcessBrief;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the Execute Response and generates objects
 * 
 * @author <a href="mailto:walencia@uni-heidelberg.de">Your Name</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExecuteResponse {
    
   
    private static NamespaceContext NS_CONTEXT;

    private XMLAdapter xmlAdapter;

    private String service;

    private String version;

    private String lang;

    private String statusLocation;

    private String serviceInstance;

    private ProcessBrief process;

    private Status status;

    private DataInputExecute dataInputExecute;

    private OutputDefinition outputDefinition;

    private ProcessOutputs processOutputs;
    
    private static Logger LOG = LoggerFactory.getLogger( ExecuteResponse.class );


    static {
        NS_CONTEXT = new NamespaceContext();
        NS_CONTEXT.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        NS_CONTEXT.addNamespace( "wps", "http://www.opengis.net/wps/1.0.0" );

    }

    /**
     *  
     * @param xmlAdapter
     */
    public ExecuteResponse( XMLAdapter xmlAdapter ) {
        
        LOG.info("parsing response...");
        

        try{
        this.xmlAdapter = xmlAdapter;

        OMElement rootElement = xmlAdapter.getRootElement();

 
        this.service = rootElement.getAttributeValue( new QName( "service" ) );
        this.version = rootElement.getAttributeValue( new QName( "version" ) );
        this.lang = rootElement.getAttributeValue( new QName( "http://www.w3.org/XML/1998/namespace", "lang", "xml" ) );
        this.statusLocation = rootElement.getAttributeValue( new QName( "http://www.w3.org/2001/XMLSchema-instance",
                                                                        "schemaLocation", "xsi" ) );
        this.serviceInstance = rootElement.getAttributeValue( new QName( "serviceInstance" ) );

        OMElement processOMElement = xmlAdapter.getElement( rootElement, new XPath( "wps:Process", NS_CONTEXT ) );

        OMAttribute processVersionOMAttribute = processOMElement.getAttribute( new QName(
                                                                                          "http://www.opengis.net/wps/1.0.0",
                                                                                          "processVersion", "wps" ) );

        ProcessBrief processBrief = new ProcessBrief();

        processBrief.setProcessVersion( " processVersionOMAttribute.getAttributeValue()" );
        processBrief.setIdentifier( xmlAdapter.getNodeAsString( processOMElement, new XPath( "ows:Identifier",
                                                                                             NS_CONTEXT ), null ) );

        processBrief.setTitle( xmlAdapter.getNodeAsString( processOMElement, new XPath( "ows:Title", NS_CONTEXT ), null ) );
        processBrief.setAbstract( xmlAdapter.getNodeAsString( processOMElement,
                                                              new XPath( "ows:Abstract", NS_CONTEXT ), null ) );

        processBrief.setMetadata( xmlAdapter.getNodesAsStrings( processOMElement,
                                                                new XPath( "ows:Metadata", NS_CONTEXT ) ) );

        processBrief.setWsdl( xmlAdapter.getNodeAsString( processOMElement, new XPath( "ows:WSDL", NS_CONTEXT ), null ) );
        processBrief.setVersionType( xmlAdapter.getNodeAsString( processOMElement, new XPath( "ows:VersionType",
                                                                                              NS_CONTEXT ), null ) );

        processBrief.setProfiles( xmlAdapter.getNodesAsStrings( processOMElement, new XPath( "ows:Profile", NS_CONTEXT ) ) );

        // TO DO: DATA Inputs

        // TO DO: OutputDefinition

        OMElement processOutputsOMElement = xmlAdapter.getElement( rootElement, new XPath( "wps:ProcessOutputs",
                                                                                           NS_CONTEXT ) );
        List<OMElement> outputsElements = xmlAdapter.getElements( processOutputsOMElement, new XPath( "wps:Output",
                                                                                                      NS_CONTEXT ) );
        ProcessOutputs procssOutputs = new ProcessOutputs();
        Output output;
        for ( Iterator iterator = outputsElements.iterator(); iterator.hasNext(); ) {

            OMElement outputOMElement = (OMElement) iterator.next();
            output = new Output();
            output.setIdentifier( xmlAdapter.getNodeAsString( outputOMElement,
                                                              new XPath( "ows:Identifier", NS_CONTEXT ), null ) );
            output.setIdentifier( xmlAdapter.getNodeAsString( outputOMElement, new XPath( "ows:Title", NS_CONTEXT ),
                                                              null ) );

            if ( xmlAdapter.getElement( outputOMElement, new XPath( "wps:Data", NS_CONTEXT ) ) != null ) {
                OMElement dataOMElement = xmlAdapter.getElement( outputOMElement, new XPath( "wps:Data", NS_CONTEXT ) );
                DataType dataType = new DataType();
                output.setDataType( dataType );

                if ( xmlAdapter.getElement( dataOMElement, new XPath( "wps:ComplexData", NS_CONTEXT ) ) != null ) {
                    OMElement complexDataOMElement = ( xmlAdapter.getElement( dataOMElement,
                                                                              new XPath( "wps:ComplexData", NS_CONTEXT ) ) );

                    ComplexData complexData = new ComplexData();
                    dataType.setComplexData( complexData );
                    complexData.setEncoding( dataOMElement.getAttributeValue( new QName( "encoding" ) ) );
                    complexData.setMimeType( dataOMElement.getAttributeValue( new QName( "mimeType" ) ) );
                    complexData.setSchema( dataOMElement.getAttributeValue( new QName( "schema" ) ) );

                    OMElement complexDataObjectOMElement = (OMElement) dataOMElement.getFirstElement();
                    Object object = complexDataObjectOMElement.getFirstElement();
                    complexData.setObject( object );
                }

                if ( xmlAdapter.getElement( dataOMElement, new XPath( "wps:LiteralData", NS_CONTEXT ) ) != null ) {
                    OMElement literalDataOMElement = ( xmlAdapter.getElement( dataOMElement,
                                                                              new XPath( "wps:LiteralData", NS_CONTEXT ) ) );
                    LiteralData literalData = new LiteralData();
                    dataType.setLiteralData( literalData );
                    literalData.setDataType( literalDataOMElement.getAttribute( new QName( "dataType" ) ).getLocalName() );

                    literalData.setLiteralData( literalDataOMElement.getAttribute( new QName( "dataType" ) ).getAttributeValue() );
                    // literalData.setUom( uom );

                }

                if ( xmlAdapter.getElement( dataOMElement, new XPath( "wps:BoundingBox", NS_CONTEXT ) ) != null ) {

                }

            }

            procssOutputs.addOutput( output );

        }
        this.processOutputs = procssOutputs;
        
        LOG.info("parsing response successfully");
        }
        catch(Exception e){
            LOG.error( e.toString() );
            LOG.info( "xmlAdapter: " + xmlAdapter.getRootElement());
           
            
        }


    }

    /**
     *  
     * @return service
     */
    public String getService() {
        return service;
    }

    /**
     *  
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     *  
     * @return language
     */
    public String getLang() {
        return lang;
    }

    /**
     *  
     * @return statusLocation
     */
    public String getStatusLocation() {
        return statusLocation;
    }

    /**
     *  
     * @return serviceInstance
     */
    public String getSericeInstance() {
        return serviceInstance;
    }

    /**
     *  
     * @return process
     */
    public ProcessBrief getProcessBrief() {
        return process;
    }

    /**
     *  
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     *  
     * @return dataInputExecute
     */
    public DataInputExecute getDataInputExecute() {
        return dataInputExecute;
    }

    /**
     *  
     * @return outputDefinition
     */
    public OutputDefinition getOutputDefinition() {
        return outputDefinition;
    }

    /**
     *  
     * @return processOutputs
     */
    public ProcessOutputs getProcessOutputs() {
        return processOutputs;
    }

}
