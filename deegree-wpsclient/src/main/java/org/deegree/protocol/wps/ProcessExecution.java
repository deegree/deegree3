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

import java.util.ArrayList;
import java.util.List;

import org.deegree.protocol.wps.describeprocess.ProcessDescription;
import org.deegree.protocol.wps.tools.BuildExecuteObjects;
import org.deegree.protocol.wps.tools.InputObject;
import org.deegree.protocol.wps.tools.OutputConfiguration;

/**
 * 
 * ProcessExecution provides WPS process execution information
 * 
 * TODO impelement me!
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessExecution {

    private OutputConfiguration outputConfiguration;

    private InputObject inputObject;

    private List<InputObject> inputObjectList = new ArrayList();

    private List<OutputConfiguration> outputConfigurationList = new ArrayList();

    private ProcessDescription processDescription;

    public ProcessExecution( ProcessDescription processDescription ) {
        this.processDescription = processDescription;
    }

    /**
     * 
     * @return percentage completed
     */
    public int getPercentComplete() {
        // TODO impelement me!
        return -1;
    }

    /**
     * 
     * @return outputParams
     */
    public Object[] getOutputParams() {
        // TODO impelement me!
        return null;
    }

    public void addInput( String identifier, String input, boolean asRef ) {
        InputObject inputObject = new InputObject( identifier, input, asRef );
        this.inputObject = inputObject;
        inputObjectList.add( inputObject );
    }

    public void addInput( InputObject inputObject ) {
        this.inputObject = inputObject;
        inputObjectList.add( inputObject );
    }

    public void addOutput( String identifier ) {
        OutputConfiguration outputConfiguration = new OutputConfiguration( "identifier" );
        this.outputConfiguration = outputConfiguration;
        outputConfigurationList.add( outputConfiguration );
    }

    public void addOutput( OutputConfiguration outputConfiguration ) {
        this.outputConfiguration = outputConfiguration;
        outputConfigurationList.add( outputConfiguration );
    }

    public void buildExecuteRequest() {
        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );
        buildExecuteObjects.createExecuteRequest();
//        buildExecuteObjects.sendExecuteRequest();

    }

    public String returnExecuteRequest() {
        BuildExecuteObjects buildExecuteObjects = new BuildExecuteObjects( inputObjectList, outputConfigurationList,
                                                                           processDescription );
        buildExecuteObjects.createExecuteRequest();

        return null;
    }

    public void sendExecuteRequest() {

    }
}