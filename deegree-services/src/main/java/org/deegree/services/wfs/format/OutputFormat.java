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
package org.deegree.services.wfs.format;

import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WFSController;

/**
 * Implementations provide output formats for the {@link WFSController}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface OutputFormat {

    /**
     * Invoked by the {@link WFSController} when this output format instance is responsible for handling the request.
     * 
     * @param request
     * @param response
     *            sink for writing the respone, never <code>null</code>
     */
    public void doDescribeFeatureType( DescribeFeatureType request, HttpResponseBuffer response )
                            throws Exception;

    /**
     * Invoked by the {@link WFSController} when this output format instance is responsible for handling the request.
     * 
     * @param request
     * @param response
     *            sink for writing the respone, never <code>null</code>
     */
    public void doGetFeature( GetFeature request, HttpResponseBuffer response )
                            throws Exception;

    /**
     * @param request
     * @param response
     * @throws Exception
     */
    public void doGetGmlObject( GetGmlObject request, HttpResponseBuffer response )
                            throws Exception;
}