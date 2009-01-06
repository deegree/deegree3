//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage.raster.interpolation;

import static org.deegree.model.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.model.coverage.raster.interpolation.InterpolationType.NEAREST_NEIGHBOR;

import org.deegree.model.coverage.raster.data.DataType;
import org.deegree.model.coverage.raster.data.RasterData;

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
