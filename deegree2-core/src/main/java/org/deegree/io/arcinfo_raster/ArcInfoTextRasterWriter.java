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
package org.deegree.io.arcinfo_raster;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.deegree.model.coverage.grid.WorldFile;

/**
 * writes a raster in ArcInfo text format:<br>
 *
 * <pre>
 *  ncols         1600
 *  nrows         1600
 *  xllcorner     3540000
 *  yllcorner     5730000
 *  cellsize      25
 *  NODATA_value  -9999
 *  120.4 132.5 99.9 ... 98.32
 *  122.5 111.6 110.9 ... 88.77
 *  ...
 *  234.23 233.4 265.9 ... 334.7
 * </pre>
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class ArcInfoTextRasterWriter {

    private String outFile;

    private float[][] data;

    private WorldFile wf;

    private double noData = -9999;

    /**
     * it will be assumed that -9999 indicates noData values
     *
     * @param outFile
     *            name of the output file
     * @param data
     *            data to write
     * @param wf
     *            metadata describing spatial location and resolution
     */
    public ArcInfoTextRasterWriter( String outFile, float[][] data, WorldFile wf ) {
        this( outFile, data, wf, -9999 );
    }

    /**
     *
     * @param outFile
     *            name of the output file
     * @param data
     *            data to write
     * @param wf
     *            metadata describing spatial location and resolution
     * @param noData
     *            value used for marking noData values
     */
    public ArcInfoTextRasterWriter( String outFile, float[][] data, WorldFile wf, double noData ) {
        this.outFile = outFile;
        this.data = data;
        this.wf = wf;
        this.noData = noData;
    }

    /**
     * writes data to file
     *
     * @throws IOException
     */
    public void write()
                            throws IOException {
        PrintWriter pw = new PrintWriter( new FileOutputStream( outFile ) );
        pw.println( "ncols " + data[0].length );
        pw.println( "nrows " + data.length );
        pw.println( "xllcorner " + wf.getEnvelope().getMin().getX() );
        pw.println( "yllcorner " + wf.getEnvelope().getMin().getY() );
        pw.println( "cellsize " + wf.getResx() );
        pw.println( "nodata_value " + noData );
        for ( int i = 0; i < data.length; i++ ) {
            for ( int j = 0; j < data[i].length; j++ ) {
                pw.print( data[i][j] );
                pw.print( ' ' );
            }
            pw.println();
        }
        pw.close();
    }

}
