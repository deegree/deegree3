package org.deegree.commons.utils.io;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UtilsTest {

    @Test
    public void testDetermineSimilarity_SameImage()
                    throws IOException {
        RenderedImage image = ImageIO.read( UtilsTest.class.getResourceAsStream( "image.png" ) );
        double similarity = Utils.determineSimilarity( image, image );
        assertThat( similarity, is( 1.0 ) );
    }

}
