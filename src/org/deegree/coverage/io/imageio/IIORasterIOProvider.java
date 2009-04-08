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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import org.deegree.coverage.raster.io.RasterIOProvider;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.RasterWriter;
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
public class IIORasterIOProvider implements RasterIOProvider {
    private static final Set<String> SUPPORTED_TYPES_READ;

    private static final Set<String> SUPPORTED_TYPES_WRITE;

    private static Logger LOG = LoggerFactory.getLogger( IIORasterIOProvider.class );

    static {
        SUPPORTED_TYPES_READ = new HashSet<String>();
        SUPPORTED_TYPES_WRITE = new HashSet<String>();
        for ( String commonType : new String[] { IIORasterReader.class.getCanonicalName(), "iio", "imageio" } ) {
            SUPPORTED_TYPES_READ.add( commonType );
            SUPPORTED_TYPES_WRITE.add( commonType );
        }

        SUPPORTED_TYPES_READ.add( "iio-reader" );
        SUPPORTED_TYPES_WRITE.add( "iio-writer" );

        String[] types = new String[] { "jpg", "jpeg", "png", "tif", "tiff", "jp2", "gif" };

        for ( String type : types ) {
            Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix( type );
            if ( iter != null && iter.hasNext() ) {
                SUPPORTED_TYPES_READ.add( type.toLowerCase() );
                LOG.debug( "register ImageReader for " + type );
            } else {
                LOG.error( "no ImageReader for " + type + " found" );
            }
        }

        for ( String type : types ) {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix( type );
            if ( iter != null && iter.hasNext() ) {
                SUPPORTED_TYPES_WRITE.add( type.toLowerCase() );
                LOG.debug( "register ImageWriter for " + type );
            } else {
                LOG.error( "no ImageWriter for " + type + " found" );
            }
        }
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

}
