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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.remotewms;

import java.util.List;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

/**
 * {@link TileDataLevel} that is backed by a {@link RemoteWMSTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class RemoteWMSTileDataLevel implements TileDataLevel {

    private static final GeometryFactory fac = new GeometryFactory();

    private final TileMatrix metadata;

    private final int tileSizeX, tileSizeY;

    private final String format;

    private final List<String> layers;

    private final List<String> styles;

    private WMSClient client;

    private final String outputFormat;

    /**
     * Creates a new {@link RemoteWMSTileDataLevel} instance.
     * 
     * @param tileMd
     *            matrix metadata, must not be <code>null</code>
     * @param format
     *            format to request tile images, must not be <code>null</code>
     * @param layers
     *            WMS layers to request, must not be <code>null</code>
     * @param styles
     *            WMS styles to request, must not be <code>null</code>
     * @param client
     *            the WMS client to use, must not be <code>null</code>
     * @param outputFormat
     *            if not null, images will be recoded into specified output format (use ImageIO like formats, eg. 'png')
     */
    RemoteWMSTileDataLevel( TileMatrix tileMd, String format, List<String> layers, List<String> styles,
                         WMSClient client, String outputFormat ) {
        this.metadata = tileMd;
        this.format = format;
        this.layers = layers;
        this.styles = styles;
        this.outputFormat = outputFormat;
        this.tileSizeX = tileMd.getTilePixelsX();
        this.tileSizeY = tileMd.getTilePixelsY();
        this.client = client;
    }

    @Override
    public TileMatrix getMetadata() {
        return metadata;
    }

    @Override
    public Tile getTile( int x, int y ) {
        if ( metadata.getNumTilesX() <= x || metadata.getNumTilesY() <= y || x < 0 || y < 0 ) {
            return null;
        }
        double width = metadata.getTileWidth();
        double height = metadata.getTileHeight();
        Envelope env = metadata.getSpatialMetadata().getEnvelope();
        double minx = width * x + env.getMin().get0();
        double miny = env.getMax().get1() - height * y;
        Envelope envelope = fac.createEnvelope( minx, miny - height, minx + width, miny, env.getCoordinateSystem() );
        GetMap gm = new GetMap( layers, styles, tileSizeX, tileSizeY, envelope, envelope.getCoordinateSystem(), format,
                                true );
        return new RemoteWMSTile( client, gm, outputFormat );
    }
}
