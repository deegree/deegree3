//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.execute;

import java.util.List;

/**
 * {@link ResponseForm} that indicates that the outputs shall be included as part of a WPS response document and defines
 * which parameters actually have to be included.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ResponseDocument implements ResponseForm {

    private List<RequestedOutput> outputDefinitions;

    private boolean storeExecuteResponse;

    private boolean lineage;

    private boolean status;

    /**
     * Creates a new {@link ResponseDocument} instance.
     * 
     * @param outputDefinitions
     * @param storeExecuteResponse
     * @param lineage
     * @param status
     */
    ResponseDocument( List<RequestedOutput> outputDefinitions, boolean storeExecuteResponse, boolean lineage,
                      boolean status ) {
        this.outputDefinitions = outputDefinitions;
        this.storeExecuteResponse = storeExecuteResponse;
        this.lineage = lineage;
        this.status = status;
    }

    /**
     * Returns the requirements for the output parameters to be included.
     * 
     * @return the requirements for the output parameters
     */
    public List<RequestedOutput> getOutputDefinitions() {
        return outputDefinitions;
    }

    /**
     * Returns whether the output document should be stored as a web-accessible resource (asynchronous execution).
     * 
     * @return true, if the output document should be stored as a web-accessible resource, false otherwise
     */
    public boolean getStoreExecuteResponse() {
        return storeExecuteResponse;
    }

    /**
     * Returns whether the execution input parameters shall be included in the response document.
     * 
     * @return true, if the execution input parameters shall be included, false otherwise
     */
    public boolean getLineage() {
        return lineage;
    }

    /**
     * Returns whether updated respsonse documents shall be provided.
     * 
     * @return true, if updated respsonse documents shall be provided, false otherwise
     */
    public boolean getStatus() {
        return status;
    }
}
