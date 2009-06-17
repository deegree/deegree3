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
package org.deegree.coverage.io.jai;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.WorldFileAccess;
import org.deegree.coverage.raster.data.container.RasterDataContainer;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.geom.RasterReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision$
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 */
public class JAIRasterReader implements RasterReader {

    private static Logger LOG = LoggerFactory.getLogger( JAIRasterReader.class );

    private File file = null;

    @Override
    public AbstractRaster load( File file, RasterIOOptions options ) {
        LOG.debug( "reading " + file + " with JAI" );
        JAIRasterDataReader reader = new JAIRasterDataReader( file );
        this.file = file;
        return loadFromReader( reader, options );
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        LOG.debug( "reading from stream with JAI" );
        JAIRasterDataReader reader = new JAIRasterDataReader( stream );
        return loadFromReader( reader, options );
    }

    private AbstractRaster loadFromReader( JAIRasterDataReader reader, RasterIOOptions options ) {
        int width = reader.getWidth();
        int height = reader.getHeight();

        reader.close();

        RasterReference rasterReference = new RasterReference( 0.5, height - 0.5, 1.0, -1.0 );

        if ( options.hasEnvelope() ) {
            rasterReference = options.getEnvelope();
        } else {
            rasterReference = new RasterReference( 0.5, height - 0.5, 1.0, -1.0 );
            if ( options.readWorldFile() ) {
                try {
                    if ( file != null ) {
                        rasterReference = WorldFileAccess.readWorldFile( file, WorldFileAccess.TYPE.CENTER );
                    }
                } catch ( IOException e ) {
                    //
                }
            }
        }

        Envelope envelope = rasterReference.getEnvelope( width, height );
        // RasterDataContainer source = RasterDataContainerFactory.withDefaultLoadingPolicy( reader );
        RasterDataContainer source = RasterDataContainerFactory.withLoadingPolicy( reader, options.getLoadingPolicy() );
        return new SimpleRaster( source, envelope, rasterReference );
    }

    @Override
    public boolean canLoad( File filename ) {
        return true;
    }
}
