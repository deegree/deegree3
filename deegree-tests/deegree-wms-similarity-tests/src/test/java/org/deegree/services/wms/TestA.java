package org.deegree.services.wms;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TestA {

    @Test
    public void test()
                    throws IOException {
        BufferedImage actual = ImageIO.read( TestA.class.getResourceAsStream( "/actual_9.tif" ) );
        BufferedImage expected = ImageIO.read( TestA.class.getResourceAsStream( "/expected_9.tif" ) );
        double v = determineSimilarity( actual, expected );
        System.out.println( v );

    }

    public static double determineSimilarity( RenderedImage in1, RenderedImage in2 ) {
        Raster data1 = in1.getData();
        Raster data2 = in2.getData();
        long equal = 0;
        for ( int b = 0; b < data1.getNumBands(); b++ ) {
            for ( int x = 0; x < data1.getWidth(); x++ ) {
                for ( int y = 0; y < data1.getHeight(); y++ ) {
                    if ( b < data2.getNumBands() && x < data2.getWidth() && y < data2.getHeight() ) {
                        if ( data1.getSample( x, y, b ) == data2.getSample( x, y, b ) ) {
                            ++equal;
                        } else {
                            System.out.println( "b: " + b + " x: " + +x + " y: " + y );
                            System.out.println( "s1 + " + data1.getSample( x, y, b ) );
                            System.out.println( "s2 + " + data2.getSample( x, y, b ) );
                        }
                    }
                }
            }
        }
        int comparedPixels = data1.getNumBands() * data1.getWidth() * data1.getHeight();
        return equal / (double) comparedPixels;
    }
}
