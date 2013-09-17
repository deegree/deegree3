package org.deegree.tile.persistence.merge;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;
import org.slf4j.Logger;

public class MergingTile implements Tile {

    private static final Logger LOG = getLogger( MergingTile.class );

    private final List<Tile> tiles;

    public MergingTile( List<Tile> tiles ) {
        this.tiles = tiles;
    }

    @Override
    public BufferedImage getAsImage()
                            throws TileIOException {
        LOG.debug( "Merging tiles" );

        Iterator<Tile> itr = tiles.iterator();

        Tile firstTile = itr.next();
        BufferedImage firstImage = firstTile.getAsImage();

        BufferedImage mergedImage = new BufferedImage( firstImage.getWidth(), firstImage.getHeight(),
                                                       BufferedImage.TYPE_3BYTE_BGR );
        Graphics g = mergedImage.getGraphics();

        g.drawImage( firstImage, 0, 0, null );

        int count = 1;
        while ( itr.hasNext() ) {
            Tile nextTile = itr.next();
            BufferedImage nextImage = nextTile.getAsImage();

            g.drawImage( nextImage, 0, 0, null );

            count++;
        }

        LOG.debug( "Number of tiles merged: " + count );

        return mergedImage;
    }

    @Override
    public InputStream getAsStream()
                            throws TileIOException {

        LOG.debug( "Writing image" );

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            ImageIO.write( getAsImage(), "jpeg", output );
        } catch ( IOException e ) {
            throw new TileIOException( e );
        }

        LOG.debug( "Output size: " + output.size() );

        return new ByteArrayInputStream( output.toByteArray() );
    }

    @Override
    public Envelope getEnvelope() {
        return tiles.get( 0 ).getEnvelope();
    }

    @Override
    public FeatureCollection getFeatures( int i, int j, int limit )
                            throws UnsupportedOperationException {
        throw new UnsupportedOperationException( "MergingTile does not support getFeatures" );
    }
}
