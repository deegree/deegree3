//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/
package org.deegree.coverage.io.imageio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.WorldFileAccess;
import org.deegree.coverage.raster.data.container.RasterDataContainer;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.geom.RasterEnvelope;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterIOProvider;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.RasterWriter;
import org.deegree.crs.CRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class IIORasterReader implements RasterIOProvider, RasterReader {

    private File file;

    private static final Set<String> SUPPORTED_TYPES;

    private static Logger LOG = LoggerFactory.getLogger( IIORasterReader.class );

    static {
        SUPPORTED_TYPES = new HashSet<String>();
        SUPPORTED_TYPES.add( IIORasterReader.class.getCanonicalName() );
        SUPPORTED_TYPES.add( "iio" );
        SUPPORTED_TYPES.add( "imageio" );
        SUPPORTED_TYPES.add( "iio-reader" );
        String[] types = new String[] { "jpg", "jpeg", "png", "tif", "tiff", "jp2", "gif" };

        for ( String type : types ) {
            Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix( type );
            if ( iter != null && iter.hasNext() ) {
                SUPPORTED_TYPES.add( type.toLowerCase() );
                LOG.debug( "register ImageReader for " + type );
            } else {
                LOG.error( "no ImageReader for " + type + " found" );
            }
        }
    }

    @Override
    public RasterReader getRasterReader( String type ) {
        if ( SUPPORTED_TYPES.contains( type ) ) {
            return this;
        }
        return null;
    }

    @Override
    public RasterWriter getRasterWriter( String type ) {
        return null;
    }

    public boolean canLoad( File filename ) {
        // TODO Auto-generated method stub
        return true;
    }

    public AbstractRaster load( File file, RasterIOOptions options )
                            throws IOException {
        LOG.debug( "reading " + file + " with ImageIO" );
        this.file = file;
        IIORasterDataReader reader = new IIORasterDataReader( file, options.get( RasterIOOptions.OPT_FORMAT ) );
        AbstractRaster r = loadFromReader( reader, options );
        return r;
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        IIORasterDataReader reader = new IIORasterDataReader( stream, options.get( RasterIOOptions.OPT_FORMAT ) );
        return loadFromReader( reader, options );
    }

    private AbstractRaster loadFromReader( IIORasterDataReader reader, RasterIOOptions options ) {

        int width = reader.getWidth();
        int height = reader.getHeight();

        RasterEnvelope rasterEnvelope;

        MetaDataReader metaDataReader = new MetaDataReader( reader.getMetaData() );
        CoordinateSystem crs = metaDataReader.getCRS();
        rasterEnvelope = metaDataReader.getRasterEnvelope();

        if ( rasterEnvelope == null ) {
            if ( options.hasEnvelope() ) {
                rasterEnvelope = options.getEnvelope();
            } else {
                rasterEnvelope = new RasterEnvelope( 0.5, height - 0.5, 1.0, -1.0 );
                if ( options.readWorldFile() ) {
                    try {
                        if ( file != null ) {
                            rasterEnvelope = WorldFileAccess.readWorldFile( file, WorldFileAccess.TYPE.CENTER );
                        }
                    } catch ( IOException e ) {
                        // 
                    }
                }
            }
        }

        reader.close();

        Envelope envelope = rasterEnvelope.getEnvelope( width, height, new CRS (crs) );

        // RasterDataContainer source = RasterDataContainerFactory.withDefaultLoadingPolicy( reader );
        RasterDataContainer source = RasterDataContainerFactory.withLoadingPolicy( reader, options.getLoadingPolicy() );
        return new SimpleRaster( source, envelope, rasterEnvelope );
    }

}
