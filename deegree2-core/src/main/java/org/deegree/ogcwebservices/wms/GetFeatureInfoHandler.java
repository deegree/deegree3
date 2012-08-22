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
package org.deegree.ogcwebservices.wms;

import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfoResult;

/**
 * Interface for defining access to GetFeatureInfoHandlers. Default implementation to be used if no
 * other is specified in deegree WMS configuration is
 *
 * @see org.deegree.ogcwebservices.wms.DefaultGetFeatureInfoHandler
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 1.1
 */
public interface GetFeatureInfoHandler {
    /**
     * performs a GetFeatureInfo request and retruns the result encapsulated within a
     * <tt>WMSFeatureInfoResponse</tt> object.
     * <p>
     * The method throws an WebServiceException that only shall be thrown if an fatal error occurs
     * that makes it imposible to return a result. If something wents wrong performing the request
     * (none fatal error) The exception shall be encapsulated within the response object to be
     * returned to the client as requested (GetFeatureInfo-Request EXCEPTION-Parameter).
     *
     * @return response to the GetFeatureInfo response
     * @throws OGCWebServiceException
     */
    public abstract GetFeatureInfoResult performGetFeatureInfo()
                            throws OGCWebServiceException;
}
