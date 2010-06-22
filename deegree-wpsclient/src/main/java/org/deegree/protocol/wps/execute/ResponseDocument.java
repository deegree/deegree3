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
import java.util.List;

/**
 * 
 * Represents the ResponseDocument section of the Execute chapter of the WPS specification 1.0
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class ResponseDocument {

    private boolean storeExecuteResponse;

    private boolean lineage;

    private  boolean status;

    private List<OutputDefinition> output = new ArrayList();

    /**
     *  
     * @return storeExecuteResponse
     */
    public boolean isStoreExecuteResponse() {
        return storeExecuteResponse;
    }

    /**
     *  
     * @param storeExecuteResponse
     */
    public void setStoreExecuteResponse( boolean storeExecuteResponse ) {
        this.storeExecuteResponse = storeExecuteResponse;
    }

    /**
     *  
     * @return lineage
     */
    public boolean isLineage() {
        return lineage;
    }

    /**
     *  
     * @param lineage
     */
    public void setLineage( boolean lineage ) {
        this.lineage = lineage;
    }

    /**
     *  
     * @return status
     */
    public boolean isStatus() {
        return status;
    }

    /**
     *  
     * @param status
     */
    public void setStatus( boolean status ) {
        this.status = status;
    }

    /**
     *  
     * @return output
     */
    public List<OutputDefinition> getOutput() {
        return output;
    }

    /**
     *  
     * @param output
     */
    public void setOutput( List<OutputDefinition> output ) {
        this.output = output;
    }

}
