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

package org.deegree.ogcwebservices.wms.configuration;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class ExternalDataAccessDataSource extends AbstractDataSource {

    private ExternalDataAccess externalDataAccess;

    /**
     *
     * @param queryable
     * @param failOnException
     * @param name
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @param externalDataAccess
     */
    public ExternalDataAccessDataSource( boolean queryable, boolean failOnException, QualifiedName name,
                                     ScaleHint scaleHint, Geometry validArea, int reqTimeLimit,
                                     ExternalDataAccess externalDataAccess ) {
        super( queryable, failOnException, name, AbstractDataSource.EXTERNALDATAACCESS, null, null, scaleHint, validArea,
               null, reqTimeLimit, null );
        this.externalDataAccess = externalDataAccess;

    }

    @Override
    public OGCWebService getOGCWebService()
                            throws OGCWebServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *
     * @return instance of class for accessing a data source not known by deegree WMS
     */
    public ExternalDataAccess getExternalDataAccess() {
        return externalDataAccess;
    }

}
