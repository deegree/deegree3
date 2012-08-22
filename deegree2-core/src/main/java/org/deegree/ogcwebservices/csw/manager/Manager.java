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

package org.deegree.ogcwebservices.csw.manager;

import org.deegree.ogcwebservices.EchoRequest;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfiguration;
import org.deegree.ogcwebservices.wfs.WFService;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface Manager {

    /**
     * @return the wfsService.
     */
    public WFService getWFService();

    /**
     *
     * @param wfsService
     * @param catalogueConfiguration
     * @throws MissingParameterValueException
     */
    public void init( WFService wfsService, CatalogueConfiguration catalogueConfiguration )
                            throws MissingParameterValueException;

    /**
     * @param request
     * @return An echorequest (e.g. a response to an harvest request).
     * @throws OGCWebServiceException
     *             if something went wrong while processing the request
     */
    public EchoRequest harvestRecords( Harvest request )
                            throws OGCWebServiceException;

    /**
     * performs a transaction request by transforming and forwarding it to the WFS used as backend
     *
     * @param request
     * @return Created by sending the request to the underlying wfs.
     * @throws OGCWebServiceException
     */
    public TransactionResult transaction( Transaction request )
                            throws OGCWebServiceException;

}
