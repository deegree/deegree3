//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.GriddedBlobTileContainer;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class RasterAPITextureTileProvider implements TextureTileProvider {

    private static Logger LOG = LoggerFactory.getLogger( RasterAPITextureTileProvider.class );

    private final GeometryFactory fac;

    private final TiledRaster raster;

    private final double res;

    private final static RasterIOOptions options = new RasterIOOptions( OriginLocation.CENTER );

    /**
     * Read a texture from a file and load it using the raster api.
     * 
     * @param file
     * @param res
     * @throws IOException
     */
    public RasterAPITextureTileProvider( File file, double res ) throws IOException {
        fac = new GeometryFactory();

        raster = new TiledRaster( GriddedBlobTileContainer.create( file, options ) );
        this.res = res;
    }

    public TextureTile getTextureTile( float minX, float minY, float maxX, float maxY ) {

        Envelope subsetEnv = fac.createEnvelope( minX, minY, maxX, maxY, null );

        TiledRaster subset = raster.getSubRaster( subsetEnv, null, OriginLocation.OUTER );

        // extract raw byte buffer (RGB, pixel interleaved)
        long begin2 = System.currentTimeMillis();
        SimpleRaster simpleRaster = subset.getAsSimpleRaster();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "#getTextureTile(): as simple raster: " + ( System.currentTimeMillis() - begin2 ) + ", env: "
                       + simpleRaster.getEnvelope() );
        }

        PixelInterleavedRasterData rasterData = (PixelInterleavedRasterData) simpleRaster.getRasterData();
        ByteBuffer pixelBuffer = rasterData.getByteBuffer();

        TextureTile tile = new TextureTile( minX, minY, maxX, maxY, subset.getColumns(), subset.getRows(), pixelBuffer,
                                            false );
        return tile;
    }

    @Override
    public double getNativeResolution() {
        return res;
    }

}
