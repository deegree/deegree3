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
package org.deegree.ogcwebservices.wmps;

import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wmps.operation.PrintMapResponseDocument;

/**
 * Interface for defining access to RequestManager. Default implementation to be used if no other is
 * specified in deegree WMPS configuration is
 *
 * @see org.deegree.ogcwebservices.wms.DefaultGetMapHandler
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @version 2.0
 */
public interface RequestManager {

    /**
     * @throws OGCWebServiceException
     */
    public void saveRequestToDB()
                            throws OGCWebServiceException;

    /**
     * @param message
     * @return the response document
     * @throws OGCWebServiceException
     */
    public PrintMapResponseDocument createInitialResponse( String message )
                            throws OGCWebServiceException;

    /**
     * @param response
     * @throws OGCWebServiceException
     */
    public void sendEmail( PrintMapResponseDocument response )
                            throws OGCWebServiceException;

    /**
     * @param message
     * @param exception
     * @return the response document
     * @throws OGCWebServiceException
     */
    public PrintMapResponseDocument createFinalResponse( String message, String exception )
                            throws OGCWebServiceException;
}
