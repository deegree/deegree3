//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.rendering.r2d.legends;

import static org.deegree.coverage.raster.data.info.BandType.BAND_0;
import static org.deegree.coverage.raster.data.info.DataType.FLOAT;
import static org.deegree.coverage.raster.data.info.InterleaveType.PIXEL;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.deegree.coverage.raster.utils.RasterFactory.createEmptyRaster;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r2d.styling.RasterStyling;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterLegendItem implements LegendItem {

    private static final GeometryFactory geofac = new GeometryFactory();
    
    private RasterStyling styling;

    private Graphics2D graphics;
    
    public RasterLegendItem(RasterStyling styling, Graphics2D g){
        this.styling = styling;
        this.graphics = g;
    }
    
    public int getHeight() {
        if(styling.interpolate != null){
            return styling.interpolate.getDatas().length - 1;
        }
        return 0;
    }

    public int getMaxWidth( LegendOptions options ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void paint( int origin, LegendOptions opts ) {
        if ( styling.interpolate != null ) {
            Double[] datas = styling.interpolate.getDatas();
            int rasterHeight = datas.length * ( opts.baseHeight + 2 * opts.spacing );
            RasterDataInfo info = new RasterDataInfo( new BandType[] { BAND_0 }, FLOAT, PIXEL );
            Envelope bbox = geofac.createEnvelope( 0, 0, opts.baseWidth, rasterHeight, new CRS( "CRS:1" ) );
            RasterGeoReference rref = new RasterGeoReference( CENTER, 1, 1, 0, 0 );
            SimpleRaster raster = createEmptyRaster( info, bbox, rref );
            RasterData rasterData = raster.getRasterData();
            int row = 0;
            for ( int i = 0; i < datas.length - 2; ++i ) {
                double a = datas[i], b = datas[i + 1];
                double res = ( b - a ) / opts.baseHeight;
                for ( int y = 0; y < opts.baseHeight; ++y ) {
                    for ( int k = 0; k < opts.baseWidth; ++k ) {
                        rasterData.setFloatSample( k, row, 0, (float) ( a + res * y ) );
                    }
                    ++row;
                }
            }
            BufferedImage img = styling.interpolate.evaluateRaster( raster, styling );
            graphics.drawImage( img, opts.spacing, 0, null );
        }
    }

}

