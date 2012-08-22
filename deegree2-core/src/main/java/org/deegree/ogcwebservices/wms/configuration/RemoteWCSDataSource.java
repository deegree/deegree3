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

import java.awt.Color;
import java.net.URL;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.wcs.RemoteWCService;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;

/**
 * Data source description for a REMOTEWCS datasource
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @version $Revision$, $Date$
 */
public class RemoteWCSDataSource extends LocalWCSDataSource {

    /**
     * Creates a new DataSource object.
     *
     * @param querable
     * @param failOnException
     * @param name
     * @param type
     * @param ows
     * @param capabilitiesURL
     * @param scaleHint
     * @param validArea
     * @param getCoverage
     * @param transparentColors
     * @param reqTimeLimit
     */
    public RemoteWCSDataSource( boolean querable, boolean failOnException, QualifiedName name, int type,
                                OGCWebService ows, URL capabilitiesURL, ScaleHint scaleHint, Geometry validArea,
                                GetCoverage getCoverage, Color[] transparentColors, int reqTimeLimit ) {
        super( querable, failOnException, name, type, ows, capabilitiesURL, scaleHint, validArea, getCoverage,
               transparentColors, reqTimeLimit );
    }

    /**
     * returns an instance of the <tt>OGCWebService</tt> that represents the datasource. Notice: if more than one layer
     * uses data that are offered by the same OWS the deegree WMS shall just use one instance for accessing the OWS
     *
     */
    @Override
    public OGCWebService getOGCWebService() {
        try {
            return new RemoteWCService( (WCSCapabilities) ows.getCapabilities() );
        } catch ( Exception ignore ) {
            // this is ignored, why?
        }
        return null;
    }

}
