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
package org.deegree.tile.layer.persistence;

import org.deegree.commons.utils.MapUtils;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.Layer;
import org.deegree.layer.LayerQuery;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileMatrix;

/**
 * The <code>GeoTIFFTileMatrix</code> is a tile matrix handing out GeoTIFFTile tiles. It uses an object pool shared
 * among all tiles created by this matrix.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class LayerTileDataLevel implements TileDataLevel {

    private final TileMatrix metadata;

    private final GeometryFactory fac = new GeometryFactory();

    private Layer lay;

    public LayerTileDataLevel( TileMatrix metadata, Layer lay ) {
        this.metadata = metadata;
        this.lay = lay;
    }

    @Override
    public TileMatrix getMetadata() {
        return metadata;
    }

    @Override
    public LayerTile getTile( long x, long y ) {
        if ( metadata.getNumTilesX() <= x || metadata.getNumTilesY() <= y || x < 0 || y < 0 ) {
            return null;
        }

        double width = metadata.getTileWidth();
        double height = metadata.getTileHeight();
        Envelope env = metadata.getSpatialMetadata().getEnvelope();
        double minx = width * x + env.getMin().get0();
        double miny = env.getMax().get1() - height * y;

        Envelope envelope = fac.createEnvelope( minx, miny, minx + width, miny - height, env.getCoordinateSystem() );
        LayerQuery q = new LayerQuery( envelope, (int) metadata.getTilePixelsX(), (int) metadata.getTilePixelsY(),
                                       null, null, null, null, MapUtils.DEFAULT_PIXEL_SIZE, null, envelope );
        return new LayerTile( q, metadata, lay );
    }
}
