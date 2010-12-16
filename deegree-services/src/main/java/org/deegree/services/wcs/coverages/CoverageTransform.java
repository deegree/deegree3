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
package org.deegree.services.wcs.coverages;

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.geometry.Envelope;
import org.deegree.services.wcs.WCServiceException;
import org.deegree.services.wcs.model.Grid;
import org.slf4j.Logger;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CoverageTransform {

    private static final Logger LOG = getLogger( CoverageTransform.class );

    /**
     * Returns a subset of the raster, transformed into to SRS of the target envelope.
     * 
     * @param raster
     * @param env
     * @param grid
     * @param interpolation
     * @return the transformation result
     * @throws WCServiceException
     *             if the transformation fails
     */
    public static AbstractRaster transform( AbstractRaster raster, Envelope env, Grid grid, String interpolation )
                            throws WCServiceException {
        LOG.debug( "Transforming raster with envelope '{}' and grid '{}', interpolation method '{}'.",
                   new Object[] { env, grid, interpolation } );
        AbstractRaster result;
        try {
            RasterTransformer transf = new RasterTransformer( env.getCoordinateSystem().getWrappedCRS() );
            result = transf.transform( raster, env, grid.getWidth(), grid.getHeight(),
                                       InterpolationType.fromString( interpolation ) );
        } catch ( Exception e ) {
            LOG.debug( "Original stack trace", e );
            throw new WCServiceException( "error while transforming raster result: " + e.getMessage(), e );
        }
        return result;
    }
}
