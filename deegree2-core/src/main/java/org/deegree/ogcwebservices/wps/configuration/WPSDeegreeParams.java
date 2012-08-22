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

package org.deegree.ogcwebservices.wps.configuration;

import org.deegree.enterprise.DeegreeParams;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcwebservices.wps.execute.RequestQueueManager;

/**
 * WPSDeegreeParams.java
 *
 * Created on 08.03.2006. 18:40:24h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WPSDeegreeParams extends DeegreeParams {

    /**
	 *
	 */
	private static final long serialVersionUID = 5341980035537747859L;

	private String[] processDirectories;

    private RequestQueueManager requestQueueManager;

    /**
     *
     * @param defaultOnlineResource
     * @param cacheSize
     * @param requestTimeLimit
     * @param processDirectories
     * @param requestQueueManager
     */
    public WPSDeegreeParams( OnlineResource defaultOnlineResource, int cacheSize,
                             int requestTimeLimit, String[] processDirectories,
                             RequestQueueManager requestQueueManager ) {
        super( defaultOnlineResource, cacheSize, requestTimeLimit );
        this.processDirectories = processDirectories;
        this.requestQueueManager = requestQueueManager;
    }

    /**
     * Returns the resolved (absolute) process directory paths.
     *
     * @return the resolved (absolute) process directory paths
     */
    public String[] getProcessDirectories() {
        return processDirectories;
    }

    /**
     *
     * @return requestQueueManager
     */
    public RequestQueueManager getRequestQueueManager() {
        return requestQueueManager;
    }
}
