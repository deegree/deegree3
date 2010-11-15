//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

package org.deegree.coverage.raster.data;

import java.util.HashMap;
import java.util.Map;

import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterRect;

/**
 * The <code>DataView</code> class defines a view on Rasterdata.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class DataView extends RasterRect {

    /** the view info on the raster data */
    public final RasterDataInfo dataInfo;

    Map<BandType, Integer> map = null;

    /**
     * Constructor if the info on the view on the data equals the original raster data info.
     * 
     * @param xOffset
     *            offset in the original raster data x-axis
     * @param yOffset
     *            offset in the original raster data y-axis
     * @param width
     *            the width of the new view on the original data
     * @param height
     *            the height of the view on the original data
     * @param viewOnData
     *            information on the view of this data.
     */
    public DataView( int xOffset, int yOffset, int width, int height, RasterDataInfo viewOnData ) {
        this( xOffset, yOffset, width, height, viewOnData, null );
    }

    /**
     * Constructor if the info on the view on the data equals the original raster data info.
     * 
     * @param rasterRect
     *            to get the views parameters from.
     * @param viewOnData
     *            information on the view of this data.
     */
    public DataView( RasterRect rasterRect, RasterDataInfo viewOnData ) {
        this( rasterRect.x, rasterRect.y, rasterRect.width, rasterRect.height, viewOnData, null );
    }

    /**
     * @param rasterRect
     *            to get the views parameters from.
     * @param viewOnData
     *            information on the view of this data.
     * @param originalInfo
     *            used to calculate the band offsets for this view.
     */
    public DataView( RasterRect rasterRect, RasterDataInfo viewOnData, RasterDataInfo originalInfo ) {
        this( rasterRect.x, rasterRect.y, rasterRect.width, rasterRect.height, viewOnData, originalInfo );
    }

    /**
     * @param xOffset
     *            offset in the original raster data x-axis
     * @param yOffset
     *            offset in the original raster data y-axis
     * @param width
     *            the width of the new view on the original data
     * @param height
     *            the height of the view on the original data
     * @param viewOnData
     *            information on the view of this data.
     * @param originalInfo
     *            used to calculate the band offsets for this view.
     */
    public DataView( int xOffset, int yOffset, int width, int height, RasterDataInfo viewOnData,
                     RasterDataInfo originalInfo ) {
        super( xOffset, yOffset, width, height );
        this.dataInfo = viewOnData;
        if ( originalInfo != null && !viewOnData.equals( originalInfo ) ) {
            createBandMapping( originalInfo );
        }
    }

    /**
     * Returns the band number of the original data for a requested band in this view on the data.
     * 
     * @param requestedBand
     *            of the view
     * @return the band number in the original data.
     */
    public int getBandOffset( int requestedBand ) {
        if ( 0 > requestedBand || requestedBand > dataInfo.bands ) {
            throw new IndexOutOfBoundsException( "The requested band does not exist on this view of the data." );
        }
        // the map == null, so this is original.
        if ( map == null ) {
            return requestedBand;
        }

        BandType b = dataInfo.bandInfo[requestedBand];
        if ( map.containsKey( b ) ) {
            return map.get( b );
        }
        throw new IndexOutOfBoundsException( "The requested band does not exist on this view of the data." );
    }

    /**
     * @param originalDataInfo
     */
    private void createBandMapping( RasterDataInfo originalDataInfo ) {
        map = new HashMap<BandType, Integer>();
        for ( BandType b : dataInfo.bandInfo ) {
            for ( int i = 0; i < originalDataInfo.bands; ++i ) {
                if ( originalDataInfo.bandInfo[i] == b ) {
                    map.put( b, i );
                }
            }
        }
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof DataView ) {
            final DataView that = (DataView) other;
            return super.equals( that ) && this.dataInfo.equals( that.dataInfo );
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long result = 32452843;
        result = result * 37 + dataInfo.hashCode();
        return (int) ( result >>> 32 ) ^ (int) result;
    }

}
