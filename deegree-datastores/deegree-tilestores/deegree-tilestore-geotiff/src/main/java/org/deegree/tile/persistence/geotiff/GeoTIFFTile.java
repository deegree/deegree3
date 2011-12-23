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
package org.deegree.tile.persistence.geotiff;

import static javax.imageio.ImageIO.createImageInputStream;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import static org.slf4j.LoggerFactory.getLogger;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.tile.Tile;
import org.slf4j.Logger;

/**
 * <code>GeoTIFFTile</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class GeoTIFFTile implements Tile {

    private static final Logger LOG = getLogger( GeoTIFFTile.class );

    private final int imageIndex, x, y;

    private final Envelope envelope;

    private final Pair<Integer, Integer> size;

    private final GenericObjectPool readerPool;

    public GeoTIFFTile( GenericObjectPool readerPool, int imageIndex, int x, int y, Envelope envelope,
                        Pair<Integer, Integer> size ) {
        this.readerPool = readerPool;
        this.imageIndex = imageIndex;
        this.x = x;
        this.y = y;
        this.envelope = envelope;
        this.size = size;
    }

    @Override
    public BufferedImage getAsImage() {
        ImageReader reader = null;
        try {
            reader = (ImageReader) readerPool.borrowObject();
            BufferedImage img = reader.readTile( imageIndex, x, y );
            if ( img.getWidth() != size.first || img.getHeight() != size.second ) {
                Hashtable table = new Hashtable();
                String[] props = img.getPropertyNames();
                if ( props != null ) {
                    for ( String p : props ) {
                        table.put( p, img.getProperty( p ) );
                    }
                }
                BufferedImage img2 = new BufferedImage( img.getColorModel(),
                                                        img.getData().createCompatibleWritableRaster( size.first,
                                                                                                      size.second ),
                                                        img.isAlphaPremultiplied(), table );
                Graphics2D g = img2.createGraphics();
                g.drawImage( img, 0, 0, null );
                g.dispose();
                img = img2;
            }
            return img;
        } catch ( Throwable e ) {
            LOG.error( "Could not read GeoTIFF tile: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace: ", e );
            return null;
        } finally {
            try {
                readerPool.returnObject( reader );
            } catch ( Exception e ) {
                // ignore closing error
            }
        }
    }

    @Override
    public Envelope getEnvelope() {
        return envelope;
    }

}
