//$HeadURL$
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
package org.deegree.protocol.wps;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.tools.BuildExecuteObjects;
import org.deegree.protocol.wps.tools.DataInputParameter;
import org.deegree.protocol.wps.tools.DataOutputParameter;
import org.deegree.protocol.wps.tools.InputObject;
import org.deegree.protocol.wps.tools.OutputConfiguration;

/**
 * ProcessInfo object containing all information relevant to a single process.
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessInfo {

    private DataInputParameter[] inputParameters;

    private DataInputParameter inputParameter;

    private DataOutputParameter[] outputParameters;

    private DataOutputParameter outputParameter;

    private ProcessDescription processDescription;

    private String abstraCt;

    private String title;

    private String identifier;

    private String language;

    private String version;

    private String metaData;

    private String profile;

    private String schemaLocation;

    private String request;

    private String service;

    private String wsdl;

    private String processVersion;

    /**
     * Constructs a ProcessInfo for a single process known by it's processIdentifier
     * 
     * @param processIdentifier
     *            identifier of a single process
     */
    public ProcessInfo( String baseURL, String processIdentifier ) {

        URL url = null;
        try {
            url = new URL( baseURL + "service=WPS&version=1.0.0&request=DescribeProcess&identifier="
                           + processIdentifier );
        } catch ( MalformedURLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DescribeProcess describeProcess = new DescribeProcess( url );
        this.processDescription = describeProcess.getProcessDescriptions().get( 0 );
        if ( processIdentifier != null && !processIdentifier.equals( "" ) ) {
            this.identifier = processIdentifier;

        }

        this.abstraCt = processDescription.getAbstraCt();
        this.identifier = processDescription.getIdentifier();
        this.language = processDescription.getLanguage();
        this.language = processDescription.getMetadata();
        this.processVersion = processDescription.getProcessVersion();
        this.profile = processDescription.getProfile();
        this.request = processDescription.getRequest();
        this.schemaLocation = processDescription.getSchemaLocation();
        this.service = processDescription.getService();
        this.title = processDescription.getTitle();
        this.version = processDescription.getVersion();
        this.wsdl = processDescription.getWSDL();

        this.inputParameters = getInputParams();
        this.outputParameters = getOutputParams();

    }

    /**
     * TODO adjust return type
     * 
     * @return
     */
    private DataInputParameter[] getInputParams() {
        DataInputParameter[] dataInputParamaters = new DataInputParameter[processDescription.getDataInputs().size()];
        for ( int i = 0; i < processDescription.getDataInputs().size(); i++ ) {
            dataInputParamaters[i] = new DataInputParameter( processDescription.getDataInputs().get( i ) );
        }
        return dataInputParamaters;
    }

    /**
     * TODO implement me!
     * 
     * @param paramId
     * @return
     */
    private DataInputParameter getInputParam( String paramId ) {
        DataInputParameter dataInputParameter = null;
        for ( int i = 0; i < processDescription.getDataInputs().size(); i++ ) {
            if ( processDescription.getDataInputs().get( i ).getIdentifier().equalsIgnoreCase( paramId ) )

                dataInputParameter = new DataInputParameter( processDescription.getDataInputs().get( i ) );
        }

        return dataInputParameter;
    }

    /**
     * TODO implement me!
     * 
     * @return
     */
    private DataOutputParameter[] getOutputParams() {
        DataOutputParameter[] dataOutputParameters = new DataOutputParameter[processDescription.getProcessOutputs().size()];
        for ( int i = 0; i < processDescription.getProcessOutputs().size(); i++ ) {
            dataOutputParameters[i] = new DataOutputParameter(
                                                               processDescription.getProcessOutputs().get( i ).getOutputDescripton() );
        }
        return dataOutputParameters;
    }

    /**
     * TODO implement me!
     * 
     * @param paramId
     * @return
     */
    private DataOutputParameter getOutputParam( String paramId ) {
        DataOutputParameter dataOutputParameter = null;
        for ( int i = 0; i < processDescription.getProcessOutputs().size(); i++ ) {
            if ( processDescription.getProcessOutputs().get( i ).getOutputDescripton().getIdentifier().equalsIgnoreCase(
                                                                                                                         paramId ) ) {
                dataOutputParameter = new DataOutputParameter(
                                                               processDescription.getProcessOutputs().get( i ).getOutputDescripton() );
            }
        }
        return dataOutputParameter;
    }

    /**
     * TODO adjust return type and implement me!
     * 
     * @param inputParams
     * @return
     */
    public void execute( List<InputObject> inputList, List<OutputConfiguration> outputConfigurationList ) {
        // �bergibt an execute: Identifier + Datei (pfad, datei, .....)

        // exectuteProperties.load(defaultconfigs);

        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputList, outputConfigurationList,
                                                                           processDescription );
        buildExecuteObjects.createExecuteRequest();

        // TODO Implement synchronous execution here;
    }

    /**
     * TODO implement me!
     * 
     * @param inputParams
     * @return
     */
    // public ProcessExecution executeAsync( Object[] inputParams ) {
    // // TODO Implement asynchronous execution here;
    // return new ProcessExecution( this.processDescription, this.b );
    // }

    /**
     * 
     * @return idetifier of this process
     */
    public String getProcessIdentifier() {
        return this.identifier;
    }

    // TODO implementMe
    public void fetchProcessInfoFromService() {
        try {
            DescribeProcess dp = new DescribeProcess(
                                                      new URL(
                                                               "http://ows7.lat-lon.de/d3WPS_JTS/services?service=WPS&version=1.0.0&request=GetCapabilities" ) );
        } catch ( MalformedURLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public DataInputParameter[] getInputParameters() {
        return inputParameters;
    }

    public DataInputParameter getInputParameter() {
        return inputParameter;
    }

    public DataOutputParameter[] getOutputParameters() {
        return outputParameters;
    }

    public DataOutputParameter getOutputParameter() {
        return outputParameter;
    }

    public ProcessDescription getProcessDescription() {
        return processDescription;
    }

    public String getAbstraCt() {
        return abstraCt;
    }

    public String getTitle() {
        return title;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLanguage() {
        return language;
    }

    public String getVersion() {
        return version;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getProfile() {
        return profile;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public String getRequest() {
        return request;
    }

    public String getService() {
        return service;
    }

    public String getWsdl() {
        return wsdl;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "ProcessIdentifier: " + this.identifier + "\n" );
        return sb.toString();
    }
}
