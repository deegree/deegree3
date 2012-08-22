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
package org.deegree.framework.concurrent;

import java.util.concurrent.Callable;

import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalRasterDataAccess;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalVectorDataAccess;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * <code>DoServiceTask</code> is the Callable class that should be used by all services to invoke other services.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DoExternalAccessTask implements Callable<Object> {

    private ExternalDataAccess externalDataAccess;

    private OGCWebServiceRequest request;

    /**
     *
     * @param externalDataAccess
     * @param request
     */
    public DoExternalAccessTask( ExternalDataAccess externalDataAccess, OGCWebServiceRequest request ) {
        this.externalDataAccess = externalDataAccess;
        this.request = request;
    }

    /**
     * @return the result of the execution or <code>null</code> if incoming request is not understood by assigned
     *         {@link ExternalDataAccess}
     */
    public Object call()
                            throws Exception {
        if ( request instanceof GetMap ) {
            if ( externalDataAccess instanceof ExternalRasterDataAccess ) {
                return ( (ExternalRasterDataAccess) externalDataAccess ).perform( (GetMap) request );
            }
            return ( (ExternalVectorDataAccess) externalDataAccess ).perform( (GetMap) request );
        } else if ( request instanceof GetFeatureInfo ) {
            return externalDataAccess.perform( (GetFeatureInfo) request );
        } else if ( request instanceof GetLegendGraphic ) {
            return externalDataAccess.perform( (GetLegendGraphic) request );
        }
        return null;
    }
}
