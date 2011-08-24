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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.coverage.raster.io.RasterIOProvider;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.RasterWriter;

/**
 * This class is a RasterIOProvider and makes the JAIRasterReader and JAIRasterWriter available to the deegree raster
 * IO.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JAIRasterIOProvider implements RasterIOProvider {

    /** Holds the list of supported image formats of the JAI library. */
    protected static final Set<String> SUPPORTED_TYPES;

    // maps a file extension to the JAI type name
    private static Map<String, String> EXT_TO_FORMAT;

    static {

        // rb: where to get the supported file types from?
        EXT_TO_FORMAT = new HashMap<String, String>();
        EXT_TO_FORMAT.put( "bmp", "BMP" );
        EXT_TO_FORMAT.put( "gif", "GIF" );
        EXT_TO_FORMAT.put( "j2k", "JPEG2000" );
        EXT_TO_FORMAT.put( "jp2", "JPEG2000" );
        EXT_TO_FORMAT.put( "jpg", "JPEG" );
        EXT_TO_FORMAT.put( "jpeg", "JPEG" );
        EXT_TO_FORMAT.put( "png", "PNG" );
        EXT_TO_FORMAT.put( "pnm", "PNM" );
        EXT_TO_FORMAT.put( "tif", "TIFF" );
        EXT_TO_FORMAT.put( "tiff", "TIFF" );

        SUPPORTED_TYPES = new HashSet<String>();
        // SUPPORTED_TYPES.add( JAIRasterReader.class.getCanonicalName() );
        // SUPPORTED_TYPES.add( "jai" );
        // SUPPORTED_TYPES.add( "jai-reader" );
        SUPPORTED_TYPES.addAll( EXT_TO_FORMAT.keySet() );
    }

    /**
     * @param extension
     * @return the JAI format name for extension
     */
    static String getJAIFormat( String extension ) {
        return EXT_TO_FORMAT.get( extension );
    }

    @Override
    public RasterReader getRasterReader( String type ) {
        if ( !SUPPORTED_TYPES.contains( type.toLowerCase() ) ) {
            return null;
        }
        return new JAIRasterReader();
    }

    @Override
    public RasterWriter getRasterWriter( String type ) {
        if ( !SUPPORTED_TYPES.contains( type.toLowerCase() ) ) {
            return null;
        }
        return new JAIRasterWriter();
    }

    @Override
    public Set<String> getRasterReaderFormats() {
        return SUPPORTED_TYPES;
    }

    @Override
    public Set<String> getRasterWriterFormats() {
        return SUPPORTED_TYPES;
    }

}
