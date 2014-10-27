package org.deegree.rendering.r2d;

import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.tile.Tile;
import org.junit.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link org.deegree.rendering.r2d.Java2DTileRenderer}.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: stenger $
 *
 * @version $Revision$, $Date$
 */
public class Java2DTileRendererTest {

    private final Java2DTileRenderer renderer = createRenderer();

    @Test
    public void testRenderWithEmptyIteratorShouldNotFail()
                            throws Exception {
        Iterator<Tile> emptyIterator = Collections.emptyIterator();
        renderer.render( emptyIterator );
    }

    @Test
    public void testRenderWithIteratorWithNullValueShouldNotFail()
                            throws Exception {
        ArrayList<Tile> list = new ArrayList<Tile>();
        list.add( null );
        Iterator<Tile> iteratorWithNullValue = list.iterator();
        renderer.render( iteratorWithNullValue );
    }

    private Java2DTileRenderer createRenderer() {
        BufferedImage image = new BufferedImage( 500, 500, TYPE_4BYTE_ABGR );
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        return new Java2DTileRenderer( graphics, 500, 500,
                                       new DefaultEnvelope( mock( Point.class ), mock( Point.class ) ) );
    }

}