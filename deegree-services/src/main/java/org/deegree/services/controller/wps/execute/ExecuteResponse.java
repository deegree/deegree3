//$HeadURL$
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

package org.deegree.services.controller.wps.execute;

import java.net.URL;
import java.util.List;

import org.deegree.services.controller.wps.ProcessletExecution;
import org.deegree.services.controller.wps.storage.StorageLocation;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.processdefinition.ProcessDefinition;

/**
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public class ExecuteResponse {

    private StorageLocation statusLocation;

    private URL serviceInstance;

    private ProcessletExecution executionStatus;

    private List<RequestedOutput> outputDefinitions;

    private ProcessletOutputs processOutputs;

    private ExecuteRequest request;

    /**
     * Creates a new <code>ExecuteResponse</code> instance from the given parameters.
     *
     * @param statusLocation
     *            location where updated ExecuteResponse documents can be polled from or null if the response document
     *            should not be stored as a web-accessible resource
     * @param serviceInstance
     *            GetCapabilities URL of the service that produced the request
     * @param status
     *            execution status of the process
     * @param outputDefinitions
     * @param outputs
     *            outputs produced by the process
     * @param request
     */
    public ExecuteResponse( StorageLocation statusLocation, URL serviceInstance,
                            ProcessletExecution status, List<RequestedOutput> outputDefinitions,
                            ProcessletOutputs outputs, ExecuteRequest request ) {
        this.statusLocation = statusLocation;
        this.serviceInstance = serviceInstance;
        this.executionStatus = status;
        this.outputDefinitions = outputDefinitions;
        this.processOutputs = outputs;
        this.request = request;
    }

    /**
     * Returns whether the inputs and requested outputs should be included in the response document.
     *
     * @return true, if they shall be included, false otherwise
     */
    public boolean getLineage () {
        if (request.getResponseForm() == null) {
            return false;
        }
        return ((ResponseDocument) request.getResponseForm()).getLineage();
    }

    /**
     * @return the dataInputs
     */
    public ProcessletInputs getDataInputs() {
        return request.getDataInputs();
    }

    /**
     * @return the executionStatus
     */
    public ProcessletExecution getExecutionStatus() {
        return executionStatus;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return request.getLanguage();
    }

    /**
     * @return the outputDefinitions
     */
    public List<RequestedOutput> getOutputDefinitions() {
        return outputDefinitions;
    }

    /**
     * @return the processDescription
     */
    public ProcessDefinition getProcessDefinition() {
        return request.getProcessDefinition();
    }

    /**
     * @return the processOutputs
     */
    public ProcessletOutputs getProcessOutputs() {
        return processOutputs;
    }

    /**
     * @return the serviceInstance
     */
    public URL getServiceInstance() {
        return serviceInstance;
    }

    /**
     * @return the statusLocation
     */
    public StorageLocation getStatusLocation() {
        return statusLocation;
    }

    public ExecuteRequest getRequest () {
        return request;
    }
}
