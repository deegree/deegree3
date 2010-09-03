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

import org.deegree.coverage.filter.raster.RasterFilter;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.geometry.Envelope;
import org.deegree.services.wcs.WCServiceException;
import org.deegree.services.wcs.model.CoverageOptions;
import org.deegree.services.wcs.model.CoverageResult;
import org.deegree.services.wcs.model.Grid;

/**
 * This is a Coverage implementation for multi-resolution raster.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class MultiResolutionCoverage extends WCSCoverage {

    /**
     * @param id
     * @param name
     * @param raster
     * @param options
     */
    public MultiResolutionCoverage( String id, String name, MultiResolutionRaster raster, CoverageOptions options ) {
        this( id, name, raster, options, null );
    }

    /**
     * @param id
     * @param name
     * @param raster
     * @param options
     * @param rangeSet
     */
    public MultiResolutionCoverage( String id, String name, MultiResolutionRaster raster, CoverageOptions options,
                                    RangeSet rangeSet ) {
        super( id, name, raster, options, rangeSet );
    }

    @Override
    public CoverageResult getCoverageResult( Envelope env, Grid grid, String format, String interpolation,
                                             RangeSet requestedRangeset )
                            throws WCServiceException {
        AbstractRaster rasterLevel = ( (MultiResolutionRaster) coverage ).getRaster( grid.getResolution() );

        AbstractRaster result = CoverageTransform.transform( rasterLevel, env, grid, interpolation );
        if ( requestedRangeset != null ) {
            RasterFilter filter = new RasterFilter( result );
            result = filter.apply( getRangeSet(), requestedRangeset );
        }
        return new SimpleRasterResult( result, format );
    }

}
