//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

package org.deegree.coverage.raster.io.asc;

import static org.deegree.coverage.raster.io.RasterIOOptions.OPT_TEXT_SEPARATOR;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterWriter;
import org.deegree.cs.CRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.UnknownCRSException;

/**
 * Writes esri asc/grd text files. The separator can be defined in the RasterIOOptions, by supplying a value with the
 * key {@link RasterIOOptions#OPT_TEXT_SEPARATOR}, if failing, the 'space' character will be used.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class ASCWriter implements RasterWriter {

    private final static Set<String> outputFormat;
    static {
        outputFormat = new HashSet<String>();
        outputFormat.addAll( ASCRasterIOProvider.FORMATS );
    }

    @Override
    public boolean canWrite( AbstractRaster raster, RasterIOOptions options ) {
        // only return true if the raster has one band.
        return raster != null && raster.getRasterDataInfo() != null && raster.getRasterDataInfo().bands == 1;
    }

    @Override
    public Set<String> getSupportedFormats() {
        return outputFormat;
    }

    @Override
    public void write( AbstractRaster raster, File file, RasterIOOptions options )
                            throws IOException {
        BufferedWriter bw = new BufferedWriter( new FileWriter( file ) );
        write( raster, bw, options );
        bw.close();
    }

    private void write( AbstractRaster raster, BufferedWriter writer, RasterIOOptions options )
                            throws IOException {
        if ( canWrite( raster, options ) ) {
            SimpleRaster simpleRaster = raster.getAsSimpleRaster();
            // convert to outer, because we iterate of x,y as whole values and not (0.5,0.5);
            RasterGeoReference rasterReference = simpleRaster.getRasterReference().createRelocatedReference(
                                                                                                             OriginLocation.OUTER );
            String newLine = System.getProperty( "line.separator" );
            writer.write( "ncols " + simpleRaster.getColumns() + newLine );
            writer.write( "nrows " + simpleRaster.getRows() + newLine );
            double[] worldCoordinate = rasterReference.getWorldCoordinate( 0, simpleRaster.getRows() );
            CRS crs = simpleRaster.getCoordinateSystem();
            int axis = 0;
            int northing = 1;
            if ( crs != null ) {
                CoordinateSystem wcrs = null;
                try {
                    wcrs = crs.getWrappedCRS();
                } catch ( UnknownCRSException e ) {
                    // nothing
                }
                if ( wcrs != null ) {
                    axis = wcrs.getEasting();
                    northing = wcrs.getNorthing();
                }
            }

            writer.write( "xllcorner " + worldCoordinate[axis] + newLine );
            writer.write( "yllcorner " + worldCoordinate[northing] + newLine );
            writer.write( "cellsize " + rasterReference.getResolutionX() + newLine );
            writer.write( "nodata_value " + simpleRaster.getRasterDataInfo().getFloatNoDataForBand( 0 ) + newLine );

            RasterData data = simpleRaster.getRasterData();
            DataType type = data.getDataType();

            String separator = options.get( OPT_TEXT_SEPARATOR );
            if ( separator == null ) {
                separator = " ";
            }
            switch ( type ) {
            case BYTE:
                writeBytes( data, writer, separator );
                break;
            case DOUBLE:
                writeDoubles( data, writer, separator );
                break;
            case FLOAT:
                writeFloats( data, writer, separator );
                break;
            case INT:
                writeInts( data, writer, separator );
                break;
            case SHORT:
                writeShorts( data, writer, separator );
                break;
            case USHORT:
                writeUShorts( data, writer, separator );
                break;
            case UNDEFINED:
                throw new UnsupportedOperationException( "The asc writer does not know how to interpret your data." );
            }

        }
    }

    /**
     * @param data
     * @param writer
     * @param separator
     */
    private void writeUShorts( RasterData data, BufferedWriter writer, String separator )
                            throws IOException {
        final int width = data.getColumns();
        final int height = data.getRows();
        short[] line = new short[width];
        for ( int y = 0; y < height; ++y ) {
            data.getShorts( 0, y, width, 1, 0, line );
            for ( int i = 0; i < width; ++i ) {
                writer.write( Integer.toString( 0xFFFF & line[i] ) );
                if ( i + 1 < width ) {
                    writer.write( separator );
                }
            }
            writer.newLine();
        }
    }

    /**
     * @param data
     * @param writer
     * @param separator
     */
    private void writeShorts( RasterData data, BufferedWriter writer, String separator )
                            throws IOException {
        final int width = data.getColumns();
        final int height = data.getRows();
        short[] line = new short[width];
        for ( int y = 0; y < height; ++y ) {
            data.getShorts( 0, y, width, 1, 0, line );
            for ( int i = 0; i < width; ++i ) {
                writer.write( Integer.toString( line[i] ) );
                if ( i + 1 < width ) {
                    writer.write( separator );
                }
            }
            writer.newLine();
        }
    }

    /**
     * @param data
     * @param writer
     * @param separator
     */
    private void writeInts( RasterData data, BufferedWriter writer, String separator )
                            throws IOException {
        final int width = data.getColumns();
        final int height = data.getRows();
        int[] line = new int[width];
        for ( int y = 0; y < height; ++y ) {
            data.getInts( 0, y, width, 1, 0, line );
            for ( int i = 0; i < width; ++i ) {
                writer.write( Integer.toString( line[i] ) );
                if ( i + 1 < width ) {
                    writer.write( separator );
                }
            }
            writer.newLine();
        }
    }

    /**
     * @param data
     * @param writer
     * @param separator
     */
    private void writeDoubles( RasterData data, BufferedWriter writer, String separator )
                            throws IOException {
        final int width = data.getColumns();
        final int height = data.getRows();
        double[] line = new double[width];
        for ( int y = 0; y < height; ++y ) {
            data.getDoubles( 0, y, width, 1, 0, line );
            for ( int i = 0; i < width; ++i ) {
                writer.write( Double.toString( line[i] ) );
                if ( i + 1 < width ) {
                    writer.write( separator );
                }
            }
            writer.newLine();
        }
    }

    /**
     * @param data
     * @param writer
     * @param separator
     * @throws IOException
     */
    private void writeBytes( RasterData data, BufferedWriter writer, String separator )
                            throws IOException {
        final int width = data.getColumns();
        final int height = data.getRows();
        byte[] line = new byte[width];
        for ( int y = 0; y < height; ++y ) {
            data.getBytes( 0, y, width, 1, 0, line );
            for ( int i = 0; i < width; ++i ) {
                writer.write( Integer.toString( 0xFF & line[i] ) );
                if ( i + 1 < width ) {
                    writer.write( separator );
                }
            }
            writer.newLine();
        }
    }

    /**
     * @param data
     * @param bw
     * @param newLine
     * @param origin
     * @param resX
     * @param resY
     * @param separator
     */
    private void writeFloats( RasterData data, BufferedWriter bw, String separator )
                            throws IOException {
        final int width = data.getColumns();
        final int height = data.getRows();
        float[] line = new float[width];
        for ( int y = 0; y < height; ++y ) {
            data.getFloats( 0, y, width, 1, 0, line );
            for ( int i = 0; i < width; ++i ) {
                bw.write( Float.toString( line[i] ) );
                if ( i + 1 < width ) {
                    bw.write( separator );
                }
            }
            bw.newLine();
        }
    }

    @Override
    public void write( AbstractRaster raster, OutputStream out, RasterIOOptions options )
                            throws IOException {
        BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( out ) );
        write( raster, bw, options );
        // don't close the stream.
    }
}
