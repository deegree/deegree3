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

package org.deegree.coverage.filter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.coverage.filter.raster.RasterFilter;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.RangeSetBuilder;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.rangeset.ValueType;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.junit.Test;

/**
 * The <code>RasterRangeSetTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterRangeSetTest {

    private GeometryFactory geomFac = new GeometryFactory();

    /**
     * @return
     * @throws IOException
     */
    private BufferedImage createBufferedImage() {
        int height = 200;
        int width = 200;
        BufferedImage im = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        int heightStep = 20;
        int steps = height / 20;
        short color = 255;
        int step = color / steps;
        int c = color;
        c <<= 8;
        c |= color;
        c <<= 8;
        c |= color;
        // create a color ramp for each band.
        for ( int y = 0; y < height; ++y ) {
            if ( y != 0 && y % heightStep == 0 ) {
                color -= step;
                c = color;
                c <<= 8;
                c |= color;
                c <<= 8;
                c |= color;
                System.out.println( Integer.toHexString( c ) );
            }

            for ( int x = 0; x < width; ++x ) {
                im.setRGB( x, y, c );
            }
        }
        return im;
    }

    @Test
    public void bandTest()
                            throws IOException {
        BufferedImage image = createBufferedImage();
        Envelope env = geomFac.createEnvelope( 0, 0, 2000, 2000, null );
        ImageIO.write( image, "png", new File( "/tmp/orig_image.png" ) );
        AbstractRaster raster = RasterFactory.createRasterFromImage( image, env );
        RasterFactory.saveRasterToFile( raster, new File( "/tmp/orig_raster.png" ) );
        RangeSet bandRaster = RangeSetBuilder.createBandRangeSetFromRaster( "any", "kind", raster );
        RasterFilter filter = new RasterFilter( raster );

        List<AxisSubset> target = new ArrayList<AxisSubset>();
        SingleValue<Byte> val = new SingleValue<Byte>( ValueType.Byte, (byte) -26 );
        SingleValue<Byte> val2 = new SingleValue<Byte>( ValueType.Byte, (byte) 0x82 );

        List<SingleValue<?>> sv = new ArrayList<SingleValue<?>>();
        sv.add( val );
        sv.add( val2 );
        AxisSubset red = new AxisSubset( "red", "yes", null, sv );
        target.add( red );
        // AxisSubset blue = new AxisSubset( "blue", "blue", null, null );
        // target.add( blue );
        RangeSet rs = new RangeSet( "target", "target", target, null );

        AbstractRaster subset = filter.getSubset( bandRaster, rs, env );

        RasterFactory.saveRasterToFile( subset, new File( "/tmp/red_blue.tiff" ) );
    }

}
