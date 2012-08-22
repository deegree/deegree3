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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Properties;

import org.deegree.framework.util.StringTools;
import org.deegree.io.arcinfo_raster.ArcInfoTextRasterReader;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.spatialschema.Envelope;

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
 * into its XYZ representation:
 *
 * <pre>
 * 2627130.0 5709682.0 206.36
 * 2627140.0 5709682.0 205.93
 * 2627150.0 5709682.0 205.69
 * 2627160.0 5709682.0 205.73
 * 2627170.0 5709682.0 205.71
 * 2627180.0 5709682.0 205.64
 * 2627190.0 5709682.0 205.64
 * 2627200.0 5709682.0 205.58
 * </pre>
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ArcInfo2xyz {

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
        File xyz = new File( map.getProperty( "-outFile" ) );

        ArcInfoTextRasterReader reader = new ArcInfoTextRasterReader( arcInfo );
        WorldFile wf = reader.readMetadata();
        Envelope env = wf.getEnvelope();
        double res = wf.getResx();

        BufferedReader in = null;
        PrintWriter pw = null;
        try {

            in = new BufferedReader( new FileReader( arcInfo ) );
            for ( int i = 0; i < 6; i++ ) {
                // skip header
                in.readLine();
            }

            pw = new PrintWriter( xyz );

            String line = null;
            double y = env.getMax().getY();
            while ( ( line = in.readLine() ) != null ) {
                System.out.print( y + "\r" );
                float[] data = StringTools.toArrayFloat( line, " \t" );
                double x = env.getMin().getX();
                for ( int i = 0; i < data.length; i++ ) {
                    pw.print( x );
                    pw.print( ' ' );
                    pw.print( y );
                    pw.print( ' ' );
                    pw.print( data[i] );
                    pw.println();
                    x = x + res;
                }
                y = y - res;
            }
        } catch ( Exception e ) {
            throw e;
        } finally {
            pw.close();
            in.close();
        }

    }

}
