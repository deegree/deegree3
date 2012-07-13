//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.tile.persistence.remotewmts;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import org.deegree.geometry.Envelope;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.protocol.wmts.ops.GetTile;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;

/**
 * {@link Tile} implementation for {@link RemoteWMTSTileDataLevel}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class RemoteWMTSTile implements Tile {

    private final WMTSClient client;

    private final GetTile request;

    private final String outputFormat;

    /**
     * Creates a new {@link RemoteWMTSTile} instance.
     * 
     * @param client
     *            client to use for performing the {@link GetTile} request, must not be <code>null</code>
     * @param request
     *            request for retrieving the tile image, must not be <code>null</code>
     * @param outputFormat
     *            if not <code>null</code>, images will be recoded into specified output format (use ImageIO like
     *            formats, eg. 'png')
     */
    RemoteWMTSTile( WMTSClient client, GetTile request, String outputFormat ) {
        this.client = client;
        this.request = request;
        this.outputFormat = outputFormat;
    }

    @Override
    public BufferedImage getAsImage()
                            throws TileIOException {
        // try {
        // return ImageIO.read( getAsStream() );
        // } catch ( IOException e ) {
        // throw new TileIOException( "Error decoding image : " + e.getMessage(), e );
        // }
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getAsStream()
                            throws TileIOException {
        // try {
        // if ( outputFormat != null ) {
        // BufferedImage img = ImageIO.read( client.getMap( request ) );
        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        // ImageIO.write( img, outputFormat, out );
        // out.close();
        // return new ByteArrayInputStream( out.toByteArray() );
        // }
        // return client.getMap( request );
        // } catch ( IOException e ) {
        // throw new TileIOException( "Error performing GetMap request: " + e.getMessage(), e );
        // }
        throw new UnsupportedOperationException();
    }

    @Override
    public Envelope getEnvelope() {
        throw new UnsupportedOperationException();
    }
}
