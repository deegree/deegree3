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
import java.util.ArrayList;
import java.util.List;

import org.deegree.protocol.wps.describeprocess.DescribeProcess;
import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.getcapabilities.ProcessOffering;
import org.deegree.protocol.wps.tools.CreateExecuteRequest;
import org.deegree.protocol.wps.tools.InputObject;
import org.deegree.protocol.wps.tools.OutputConfiguration;

/**
 * 
 * ProcessInfo object containing all information relevant to a single process.
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessInfo {

    private String pi;

    private Object[] inputParams;

    private Object inputParam;

    private Object[] outputParams;

    private Object outputParam;

    ProcessDescription processDescription;

    /**
     * Constructs a ProcessInfo for a single process known by it's processIdentifier
     * 
     * @param processIdentifier
     *            identifier of a single process
     */
    public ProcessInfo( String processIdentifier ) {

        // übergabe URL anders Regeln...
        URL url = null;
        try {
            url = new URL( "http://ows7.lat-lon.de/d3WPS_JTS/services?service=WPS&version=1.0.0" + processIdentifier );
        } catch ( MalformedURLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DescribeProcess describeProcess = new DescribeProcess( url );
        this.processDescription = describeProcess.getProcessDescriptions().get( 0 );
        if ( processIdentifier != null && !processIdentifier.equals( "" ) ) {
            this.pi = processIdentifier;

        }
    }

    /**
     * TODO adjust return type
     * 
     * @return
     */
    public Object[] getInputParams() {
        return null;
    }

    /**
     * TODO implement me!
     * 
     * @param paramId
     * @return
     */
    private Object getInputParam( String paramId ) {
        return null;
    }

    /**
     * TODO implement me!
     * 
     * @return
     */
    private Object[] getOutputParams() {
        return null;
    }

    /**
     * TODO implement me!
     * 
     * @param paramId
     * @return
     */
    private Object getOutputParam( String paramId ) {

        return null;
    }

    /**
     * TODO adjust return type and implement me!
     * 
     * @param inputParams
     * @return
     */
    public void execute( List<InputObject> inputList, List<OutputConfiguration> outputConfigurationList ) {
        // übergibt an execute: Identifier + Datei (pfad, datei, .....)

        // exectuteProperties.load(defaultconfigs);

        CreateExecuteRequest fillDataInput = new CreateExecuteRequest( inputList, outputConfigurationList,
                                                                       processDescription );
        fillDataInput.runExecute();

        // TODO Implement synchronous execution here;
    }

    /**
     * TODO implement me!
     * 
     * @param inputParams
     * @return
     */
    public ProcessExecution executeAsync( Object[] inputParams ) {
        // TODO Implement asynchronous execution here;
        return new ProcessExecution();
    }

    /**
     * 
     * @return idetifier of this process
     */
    public String getPi() {
        return this.pi;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "ProcessIdentifier: " + this.pi + "\n" );
        return sb.toString();
    }
}
