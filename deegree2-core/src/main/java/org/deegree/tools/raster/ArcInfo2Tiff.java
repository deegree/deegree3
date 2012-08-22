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
package org.deegree.tools.raster;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Properties;

import org.deegree.framework.util.ImageUtils;
import org.deegree.io.arcinfo_raster.ArcInfoTextRasterReader;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.processing.raster.converter.RawData2Image;

/**
 * converts an ArcInfor raster file (text format):
 * 
 * <pre>
 *  ncols         2404
 *  nrows         2307
 *  xllcorner     2627130
 *  yllcorner     5686612
 *  cellsize      10
 *  NODATA_value  -9999
 *  20636 20593 20569 20573 20571 20564 20564 20558 ...
 *  ...
 * </pre>
 * 
 * into a Tiff image that may have 16 or 32 bit pixel depth
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ArcInfo2Tiff {

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }
        File arcInfo = new File( map.getProperty( "-inFile" ) );
        String o = map.getProperty( "-outFile" );
        if ( !o.toLowerCase().endsWith( ".tiff" ) && !o.toLowerCase().endsWith( ".tif" ) ) {
            throw new Exception( "output image format must be tiff or tiff" );
        }
        File out = new File( o );

        String depth = map.getProperty( "-type" );

        boolean outer = map.getProperty( "-refType" ) == null || map.getProperty( "-refType" ).equalsIgnoreCase( "outer" );

        ArcInfoTextRasterReader reader = new ArcInfoTextRasterReader( arcInfo );
        WorldFile wf = reader.readMetadata( !outer );
        o = o.substring( 0, o.lastIndexOf( '.' ) );
        WorldFile.writeWorldFile( wf, o );
        float[][] data = reader.readData();
        BufferedImage bi = RawData2Image.rawData2Image( data, "32".equals( depth ) );

        ImageUtils.saveImage( bi, out, 1 );
    }

}
