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

package org.deegree.coverage.raster.io.xyz;

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

/**
 * The <code>XYZWriter</code> writes xyz, separated text files. The separator can be defined in the RasterIOOptions, by
 * supplying a value with the key {@link XYZRasterIOProvider#XYZ_SEPARATOR}, if failing, the 'space' character will be
 * used.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class XYZWriter implements RasterWriter {

    private final static Set<String> outputFormat;
    static {
        outputFormat = new HashSet<String>();
        outputFormat.add( "XYZ" );
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
                                                                                                             OriginLocation.CENTER );

            RasterData data = simpleRaster.getRasterData();
            DataType type = data.getDataType();
            String newLine = System.getProperty( "line.separator" );
            String separator = options.get( XYZRasterIOProvider.XYZ_SEPARATOR );
            if ( separator == null ) {
                separator = " ";
            }
            switch ( type ) {
            case BYTE:
                writeBytes( data, writer, newLine, rasterReference, separator );
                break;
            case DOUBLE:
                writeDoubles( data, writer, newLine, rasterReference, separator );
                break;
            case FLOAT:
                writeFloats( data, writer, newLine, rasterReference, separator );
                break;
            case INT:
                writeInts( data, writer, newLine, rasterReference, separator );
                break;
            case SHORT:
                writeShorts( data, writer, newLine, rasterReference, separator );
                break;
            case USHORT:
                writeUShorts( data, writer, newLine, rasterReference, separator );
                break;
            case UNDEFINED:
                throw new UnsupportedOperationException( "The xyz writer does not know how to interpret your data." );
            }

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
    private void writeUShorts( RasterData data, BufferedWriter bw, String newLine, RasterGeoReference geoRef,
                               String separator )
                            throws IOException {
        StringBuilder sb = null;
        int height = data.getHeight() - 1;
        int width = data.getWidth();

        double[] worldCoords = new double[2];
        for ( int x = 0; x < width; ++x ) {
            // 30, assuming the values are 10 characters long
            sb = new StringBuilder( ( height + 1 ) * 30 );
            for ( int y = height; y >= 0; --y ) {
                short sample = data.getShortSample( x, y, 0 );
                worldCoords = geoRef.getWorldCoordinate( x + 0.5, y + 0.5 );
                sb.append( worldCoords[0] + separator + worldCoords[1] + separator + ( sample & 0xFFFF ) + newLine );
            }
            bw.write( sb.toString() );
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
    private void writeShorts( RasterData data, BufferedWriter bw, String newLine, RasterGeoReference geoRef,
                              String separator )
                            throws IOException {
        StringBuilder sb = null;
        int height = data.getHeight() - 1;
        int width = data.getWidth();

        double[] worldCoords = new double[2];
        for ( int x = 0; x < width; ++x ) {
            // 30, assuming the values are 10 characters long
            sb = new StringBuilder( ( height + 1 ) * 30 );
            for ( int y = height; y >= 0; --y ) {
                short sample = data.getShortSample( x, y, 0 );
                worldCoords = geoRef.getWorldCoordinate( x + 0.5, y + 0.5 );
                sb.append( worldCoords[0] + separator + worldCoords[1] + separator + sample + newLine );
            }
            bw.write( sb.toString() );
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
    private void writeInts( RasterData data, BufferedWriter bw, String newLine, RasterGeoReference geoRef,
                            String separator )
                            throws IOException {
        StringBuilder sb = null;
        int height = data.getHeight() - 1;
        int width = data.getWidth();

        double[] worldCoords = new double[2];
        for ( int x = 0; x < width; ++x ) {
            // 30, assuming the values are 10 characters long
            sb = new StringBuilder( ( height + 1 ) * 30 );
            for ( int y = height; y >= 0; --y ) {
                int sample = data.getIntSample( x, y, 0 );
                worldCoords = geoRef.getWorldCoordinate( x + 0.5, y + 0.5 );
                sb.append( worldCoords[0] + separator + worldCoords[1] + separator + sample + newLine );
            }
            bw.write( sb.toString() );
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
    private void writeFloats( RasterData data, BufferedWriter bw, String newLine, RasterGeoReference geoRef,
                              String separator )
                            throws IOException {
        StringBuilder sb = null;
        int height = data.getHeight() - 1;
        int width = data.getWidth();

        double[] worldCoords = new double[2];
        for ( int x = 0; x < width; ++x ) {
            // 30, assuming the values are 10 characters long
            sb = new StringBuilder( ( height + 1 ) * 30 );
            for ( int y = height; y >= 0; --y ) {
                float sample = data.getFloatSample( x, y, 0 );
                worldCoords = geoRef.getWorldCoordinate( x + 0.5, y + 0.5 );
                sb.append( worldCoords[0] + separator + worldCoords[1] + separator + sample + newLine );
            }
            bw.write( sb.toString() );
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
    private void writeBytes( RasterData data, BufferedWriter bw, String newLine, RasterGeoReference geoRef,
                             String separator )
                            throws IOException {
        StringBuilder sb = null;
        int height = data.getHeight() - 1;
        int width = data.getWidth();

        double[] worldCoords = new double[2];
        for ( int x = 0; x < width; ++x ) {
            // 30, assuming the values are 10 characters long
            sb = new StringBuilder( ( height + 1 ) * 30 );
            for ( int y = height; y >= 0; --y ) {
                byte sample = data.getByteSample( x, y, 0 );
                worldCoords = geoRef.getWorldCoordinate( x + 0.5, y + 0.5 );
                sb.append( worldCoords[0] + separator + worldCoords[1] + separator + ( sample & 0xFF ) + newLine );
            }
            bw.write( sb.toString() );
        }
    }

    /**
     * @param data
     * @param bw
     * @param separator
     * @throws IOException
     */
    private void writeDoubles( RasterData data, BufferedWriter bw, String newLine, RasterGeoReference geoRef,
                               String separator )
                            throws IOException {
        StringBuilder sb = null;
        int height = data.getHeight() - 1;
        int width = data.getWidth();

        double[] worldCoords = new double[2];
        for ( int x = 0; x < width; ++x ) {
            // 30, assuming the values are 10 characters long
            sb = new StringBuilder( ( height + 1 ) * 30 );
            for ( int y = height; y >= 0; --y ) {
                double sample = data.getDoubleSample( x, y, 0 );
                worldCoords = geoRef.getWorldCoordinate( x + 0.5, y + 0.5 );
                sb.append( worldCoords[0] + separator + worldCoords[1] + separator + sample + newLine );
            }
            bw.write( sb.toString() );
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
