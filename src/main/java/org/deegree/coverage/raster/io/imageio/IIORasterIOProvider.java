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
package org.deegree.coverage.raster.io.imageio;

import java.util.HashSet;
import java.util.Set;

import org.deegree.coverage.raster.io.RasterIOProvider;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.RasterWriter;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class IIORasterIOProvider implements RasterIOProvider {
    private static final Set<String> SUPPORTED_TYPES_READ;

    private static final Set<String> SUPPORTED_TYPES_WRITE;

    // private static Logger LOG = LoggerFactory.getLogger( IIORasterIOProvider.class );

    static {
        // SUPPORTED_TYPES_READ = new HashSet<String>();
        // SUPPORTED_TYPES_WRITE = new HashSet<String>();
        // for ( String commonType : new String[] { IIORasterReader.class.getCanonicalName(), "iio", "imageio" } ) {
        // SUPPORTED_TYPES_READ.add( commonType );
        // SUPPORTED_TYPES_WRITE.add( commonType );
        // }

        // SUPPORTED_TYPES_READ.add( "iio-reader" );
        // SUPPORTED_TYPES_WRITE.add( "iio-writer" );

        SUPPORTED_TYPES_READ = new HashSet<String>();
        SUPPORTED_TYPES_READ.addAll( new IIORasterReader().getSupportedFormats() );
        SUPPORTED_TYPES_READ.add( "geotiff" );
        SUPPORTED_TYPES_WRITE = new HashSet<String>();
        SUPPORTED_TYPES_WRITE.addAll( new IIORasterWriter().getSupportedFormats() );
        SUPPORTED_TYPES_WRITE.add( "geotiff" );

        // String[] readerFormatNames = ImageIO.getReaderFormatNames();
        // if ( readerFormatNames != null ) {
        // for ( String format : readerFormatNames ) {
        // SUPPORTED_TYPES_READ.add( format );
        // }
        // }
        //
        // String[] writerFormatNames = ImageIO.getWriterFormatNames();
        // if ( writerFormatNames != null ) {
        // for ( String format : writerFormatNames ) {
        // SUPPORTED_TYPES_WRITE.add( format );
        // }
        // }

        // String[] types = new String[] { "jpg", "jpeg", "png", "tif", "tiff", "jp2", "gif" };
        // for ( String type : types ) {
        // Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix( type );
        // if ( iter != null && iter.hasNext() ) {
        // SUPPORTED_TYPES_READ.add( type.toLowerCase() );
        // LOG.debug( "register ImageReader for " + type );
        // } else {
        // LOG.error( "no ImageReader for " + type + " found" );
        // }
        // }

        // for ( String type : types ) {
        // Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix( type );
        // if ( iter != null && iter.hasNext() ) {
        // SUPPORTED_TYPES_WRITE.add( type.toLowerCase() );
        // LOG.debug( "register ImageWriter for " + type );
        // } else {
        // LOG.error( "no ImageWriter for " + type + " found" );
        // }
        // }
    }

    @Override
    public RasterReader getRasterReader( String type ) {
        if ( SUPPORTED_TYPES_READ.contains( type.toLowerCase() ) ) {
            return new IIORasterReader();
        }
        return null;
    }

    @Override
    public RasterWriter getRasterWriter( String type ) {
        if ( SUPPORTED_TYPES_WRITE.contains( type.toLowerCase() ) ) {
            return new IIORasterWriter();
        }
        return null;
    }

    @Override
    public Set<String> getRasterReaderFormats() {
        return SUPPORTED_TYPES_READ;
    }

    @Override
    public Set<String> getRasterWriterFormats() {
        return SUPPORTED_TYPES_WRITE;
    }

}
