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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.tile.persistence.gpkg;

import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;
import org.deegree.tile.TileMatrix;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A GpkgTile.
 *
 * @author <a href="mailto:migliavacca@lat-lon.de">Diego Migliavacca</a>
 * @since 3.5
 */
public class GpkgTile implements Tile {

    private static final Logger LOG = getLogger( GpkgTile.class );

    private TileMatrix tm;

    private byte[] bytes;

    public GpkgTile( TileMatrix tm, byte[] bytes ) {
        this.tm = tm;
        this.bytes = bytes;
    }

    @Override
    public BufferedImage getAsImage()
                            throws TileIOException {
        try {
            return ImageIO.read( new ByteArrayInputStream( bytes ) );
        } catch ( IOException e ) {
            String msg = "Error decoding image from byte array: " + e.getMessage();
            LOG.trace( msg, e );
            throw new TileIOException( e.getMessage(), e );
        }
    }

    @Override
    public InputStream getAsStream() {
        return new ByteArrayInputStream( bytes );
    }

    @Override
    public Envelope getEnvelope() {
        return tm.getSpatialMetadata().getEnvelope();
    }

    @Override
    public FeatureCollection getFeatures( int i, int j, int limit )
                            throws UnsupportedOperationException {
        throw new UnsupportedOperationException( "Feature retrieval is not supported by the GpkgTileStore." );
    }
}
