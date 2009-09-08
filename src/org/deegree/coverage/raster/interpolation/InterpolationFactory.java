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
package org.deegree.coverage.raster.interpolation;

import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.interpolation.InterpolationType.NEAREST_NEIGHBOR;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.DataType;

/**
 * Factory for {@link Interpolation}s.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class InterpolationFactory {

    /**
     * Creates a new interpolation of the given type and for the given raster.
     *
     * @param type
     *            the interpolation type
     * @param rasterData
     * @return interpolation for given type and raster
     * @throws UnsupportedOperationException
     *             if no interpolation is found for the given type and raster
     */
    public static Interpolation getInterpolation( InterpolationType type, RasterData rasterData ) {
        if ( type == NEAREST_NEIGHBOR ) {
            return new NearestNeighborInterpolation( rasterData );
        } else if ( type == BILINEAR ) {
            if ( rasterData.getDataType() == DataType.BYTE ) {
                return new BiLinearByteInterpolation( rasterData );
            } else if ( rasterData.getDataType() == DataType.FLOAT ) {
                return new BiLinearFloatInterpolation( rasterData );
            } else if ( rasterData.getDataType() == DataType.SHORT ) {
                return new BiLinearShortInterpolation( rasterData );
            } else {
                throw new UnsupportedOperationException( "no bilinear interpolation implementation for "
                                                         + rasterData.getDataType() + " found." );
            }
        } else {
            throw new UnsupportedOperationException( "no " + type + " interpolation implementation found." );

        }
    }
}
