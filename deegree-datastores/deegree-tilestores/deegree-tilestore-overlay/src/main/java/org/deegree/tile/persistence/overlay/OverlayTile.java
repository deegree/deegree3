/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.overlay;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
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

/**
 * {@link Tile} implementation for the {@link OverlayTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class OverlayTile implements Tile {

    private static final Logger LOG = getLogger( OverlayTile.class );

    private final List<Tile> tiles;

    /**
     * Creates a new {@link OverlayTile} instance.
     * 
     * @param tiles
     *            tiles to overlay, must not be <code>null</code> and contain at least two entries
     */
    OverlayTile( final List<Tile> tiles ) {
        this.tiles = tiles;
    }

    @Override
    public BufferedImage getAsImage()
                            throws TileIOException {
        LOG.debug( "Creating overlay" );
        final Iterator<Tile> itr = tiles.iterator();
        final Tile firstTile = itr.next();
        final BufferedImage firstImage = firstTile.getAsImage();
        final BufferedImage overlay = new BufferedImage( firstImage.getWidth(), firstImage.getHeight(), TYPE_3BYTE_BGR );
        final Graphics g = overlay.getGraphics();
        g.drawImage( firstImage, 0, 0, null );
        int count = 1;
        while ( itr.hasNext() ) {
            final Tile nextTile = itr.next();
            final BufferedImage nextImage = nextTile.getAsImage();
            g.drawImage( nextImage, 0, 0, null );
            count++;
        }
        LOG.debug( "Number of overlayed tiles: " + count );
        return overlay;
    }

    @Override
    public InputStream getAsStream()
                            throws TileIOException {
        LOG.debug( "Writing image" );
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
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
        throw new UnsupportedOperationException( "OverlayTile does not support getFeatures operation" );
    }
}
