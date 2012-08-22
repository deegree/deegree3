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
package org.deegree.ogcwebservices.wps.execute;

import java.util.Map;

import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription;
import org.deegree.ogcwebservices.wps.execute.ExecuteResponse.ProcessOutputs;

/**
 * Process.java
 *
 * Created on 11.03.2006. 18:32:39h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class Process {

    /**
     *
     */
    protected ProcessDescription processDescription;

    /**
     *
     * @param processDescription
     */
    public Process( ProcessDescription processDescription ) {
        this.processDescription = processDescription;
    }

    /**
     *
     * @param inputs
     * @param outputDefinitions
     * @return the result of the process
     * @throws OGCWebServiceException
     */
    public abstract ProcessOutputs execute( Map<String, IOValue> inputs,
                                            OutputDefinitions outputDefinitions )
                            throws OGCWebServiceException;

    /**
     *
     * @return processDescription
     */
    public ProcessDescription getProcessDescription() {
        return this.processDescription;
    }

}

