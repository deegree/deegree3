//$HeadURL$
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
package org.deegree.coverage.raster.io.jai;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.WorldFileAccess;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JAI based raster reader, rb: should be refactored to use the 'new' tiling raster api.
 * 
 * @version $Revision$
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 */
public class JAIRasterReader implements RasterReader {

    private static Logger LOG = LoggerFactory.getLogger( JAIRasterReader.class );

    private File file = null;

    private RasterGeoReference rasterReference;

    private int width;

    private int height;

    private JAIRasterDataReader reader;

    private String dataLocationId;

    @Override
    public AbstractRaster load( File file, RasterIOOptions options ) {
        LOG.debug( "reading " + file + " with JAI" );
        reader = new JAIRasterDataReader( file, options );

        return loadFromReader( reader, options );
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        LOG.debug( "reading from stream with JAI" );
        reader = new JAIRasterDataReader( stream, options );
        return loadFromReader( reader, options );
    }

    private AbstractRaster loadFromReader( JAIRasterDataReader reader, RasterIOOptions options ) {
        width = reader.getColumns();
        height = reader.getRows();
        setID( options );
        reader.close();
        OriginLocation definedRasterOrigLoc = options.getRasterOriginLocation();
        // create a 1:1 mapping
        rasterReference = new RasterGeoReference( definedRasterOrigLoc, 1, -1, 0.5, height - 0.5 );

        if ( options.hasRasterGeoReference() ) {
            rasterReference = options.getRasterGeoReference();
        } else {
            if ( options.readWorldFile() ) {
                try {
                    if ( file != null ) {
                        rasterReference = WorldFileAccess.readWorldFile( file, options );
                    }
                } catch ( IOException e ) {
                    //
                }
            }
        }

        Envelope envelope = rasterReference.getEnvelope( width, height, null );
        // RasterDataContainer source = RasterDataContainerFactory.withDefaultLoadingPolicy( reader );
        // RasterDataContainer source = RasterDataContainerFactory.withLoadingPolicy( reader, options.getLoadingPolicy()
        // );
        RasterDataInfo rdi = reader.getRasterDataInfo();
        return RasterFactory.createEmptyRaster( rdi, envelope, rasterReference, this, true, options );
    }

    private void setID( RasterIOOptions options ) {
        this.dataLocationId = options != null ? options.get( RasterIOOptions.ORIGIN_OF_RASTER ) : null;
        if ( dataLocationId == null ) {
            if ( this.file != null ) {
                this.dataLocationId = RasterCache.getUniqueCacheIdentifier( this.file );
            }
        }
    }

    @Override
    public boolean canLoad( File filename ) {
        return true;
    }

    @Override
    public Set<String> getSupportedFormats() {
        return JAIRasterIOProvider.SUPPORTED_TYPES;
    }

    @Override
    public File file() {
        return reader == null ? null : reader.file();
    }

    @Override
    public RasterGeoReference getGeoReference() {
        return rasterReference;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer buffer )
                            throws IOException {
        throw new UnsupportedOperationException( "Not yet implemented for the JAI readers" );
    }

    @Override
    public boolean shouldCreateCacheFile() {
        return true;
    }

    @Override
    public boolean canReadTiles() {
        return false;
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return ( reader != null ) ? reader.getRasterDataInfo() : null;
    }

    @Override
    public String getDataLocationId() {
        return dataLocationId;
    }

    @Override
    public void dispose() {
        if ( reader != null ) {
            reader.dispose();
        }
    }
}
