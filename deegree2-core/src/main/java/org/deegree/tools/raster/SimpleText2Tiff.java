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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;

/**
 * This class ist similar to Text2Tiff. The major difference is that SimpleText2Tiff just is able to
 * transform x y z formateted textfiles into 16BIT tiff images if the text files contains equal
 * distance rasters. Missing raster cells will be filled with '0'.<br>
 * The major advantage of SimpleText2Tiff is its speed. It is significantly faster than Text2Tiff
 * because several checks and calculations can be skippted.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SimpleText2Tiff {

    private File[] files;

    private double resolution;

    private float offset = 0;

    private float scaleFactor = 1;

    private boolean use32Bit = false;

    /**
     *
     * @param files
     *            list of text files to tranform
     * @param resolution
     *            desired target resolution
     * @param offset
     *            desired z-value offset
     * @param scaleFactor
     *            desired z-value scale factor [value = (z + offset) * scaleFactor]
     * @param use32Bit
     */
    public SimpleText2Tiff( File[] files, double resolution, float offset, float scaleFactor, boolean use32Bit ) {
        this.files = files;
        this.resolution = resolution;
        this.offset = offset;
        this.scaleFactor = scaleFactor;
        this.use32Bit = use32Bit;
    }

    /**
     * starts transformation
     *
     * @throws Exception
     */
    public void perform()
                            throws Exception {
        for ( int i = 0; i < files.length; i++ ) {
            System.out.println( "process: " + files[i] );
            text2tiff( files[i] );
        }
    }

    /**
     *
     * @param file
     * @throws Exception
     */
    private void text2tiff( File file )
                            throws Exception {
        Envelope bbox = getBoundingBox( file );
        int width = (int) Math.round( bbox.getWidth() / resolution ) + 1;
        int height = (int) Math.round( bbox.getHeight() / resolution ) + 1;

        BufferedImage out = null;
        if ( use32Bit ) {
            out = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
        } else {
            ComponentColorModel ccm = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_GRAY ), null,
                                                               false, false, BufferedImage.OPAQUE,
                                                               DataBuffer.TYPE_USHORT );
            WritableRaster wr = ccm.createCompatibleWritableRaster( width, height );
            out = new BufferedImage( ccm, wr, false, new Hashtable<String, Object>() );
        }
        DataBuffer buffer = out.getRaster().getDataBuffer();

        BufferedReader br = new BufferedReader( new FileReader( file ) );
        String line = null;
        while ( ( line = br.readLine() ) != null ) {
            double[] d = StringTools.toArrayDouble( line.trim(), " \t" );
            int x = (int) Math.round( ( d[0] - bbox.getMin().getX() ) / resolution );
            int y = height - (int) Math.round( ( d[1] - bbox.getMin().getY() ) / resolution ) - 1;
            int pos = width * y + x;

            try {
                if ( use32Bit ) {
                    buffer.setElem( pos, Float.floatToIntBits( (float) ( ( d[2] + offset ) * scaleFactor ) ) );
                } else {
                    buffer.setElem( pos, (int) Math.round( ( d[2] + offset ) * scaleFactor ) );
                }
            } catch ( Exception e ) {
                System.out.println( "-------------------------------" );
                System.out.println( buffer.getSize() );
                System.out.println( "file bbox: " + bbox );
                System.out.println( "last line read: " + line );
                throw e;
            }
        }
        br.close();
        out.setData( Raster.createRaster( out.getSampleModel(), buffer, null ) );

        int pos = file.getAbsolutePath().lastIndexOf( '.' );
        if ( pos < 0 ) {
            pos = file.getAbsolutePath().length();
        }
        String fileName = file.getAbsolutePath().substring( 0, pos ) + ".tif";
        ImageUtils.saveImage( out, fileName, 1 );
        WorldFile wf = new WorldFile( resolution, resolution, 0, 0, bbox );
        WorldFile.writeWorldFile( wf, file.getAbsolutePath().substring( 0, pos ) );

    }

    /**
     *
     * @param file
     * @return the boundingbox of the geometry read from the given file as an envelope.
     * @throws IOException
     */
    private Envelope getBoundingBox( File file )
                            throws IOException {
        BufferedReader br = new BufferedReader( new FileReader( file ) );
        String line = null;
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double maxy = Double.MIN_VALUE;

        while ( ( line = br.readLine() ) != null ) {
            double[] d = StringTools.toArrayDouble( line.trim(), " \t" );
            if ( d[0] < minx ) {
                minx = d[0];
            }
            if ( d[0] > maxx ) {
                maxx = d[0];
            }

            if ( d[1] < miny ) {
                miny = d[1];
            }
            if ( d[1] > maxy ) {
                maxy = d[1];
            }
        }
        br.close();
        return GeometryFactory.createEnvelope( minx, miny, maxx, maxy, null );
    }

    /**
     * @param args
     * @throws Exception
     *             if something went wrong.
     */
    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }
        if ( !validate( map ) ) {
            System.out.println( "Parameters: -rootDir, -resolution, -offset and -scaleFactor must be set" );
            return;
        }

        List<File> fileList = new ArrayList<File>( 1000 );
        File dir = new File( map.getProperty( "-rootDir" ) );
        File[] files = null;
        ConvenienceFileFilter cff = null;
        if ( map.getProperty( "-extension" ) != null ) {
            cff = new ConvenienceFileFilter( true, map.getProperty( "-extension" ) );
            files = dir.listFiles( cff );
        } else {
            files = dir.listFiles();
        }
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].isDirectory() ) {
                readSubDirs( files[i], fileList, cff );
            } else {
                fileList.add( files[i] );
            }
        }

        double resolution = Double.parseDouble( map.getProperty( "-resolution" ) );
        float offset = Float.parseFloat( map.getProperty( "-offset" ) );
        float scaleFactor = Float.parseFloat( map.getProperty( "-scaleFactor" ) );
        boolean use32Bit = "true".equals( map.getProperty( "-use32Bit" ) );
        SimpleText2Tiff t2t = new SimpleText2Tiff( fileList.toArray( new File[fileList.size()] ), resolution, offset,
                                                   scaleFactor, use32Bit );
        t2t.perform();

    }

    /**
     *
     * @param file
     * @param list
     * @param cff
     * @return list of files
     */
    private static List<File> readSubDirs( File file, List<File> list, ConvenienceFileFilter cff ) {
        File[] entries = null;
        if ( cff != null ) {
            entries = file.listFiles( cff );
        } else {
            entries = file.listFiles();
        }
        if ( entries != null ) {
            for ( int i = 0; i < entries.length; i++ ) {
                if ( entries[i].isDirectory() ) {
                    list = readSubDirs( entries[i], list, cff );
                } else {
                    list.add( entries[i] );
                }
            }
        }
        return list;
    }

    /**
     *
     * @param param
     * @return true if everything is correct
     * @throws Exception
     */
    private static boolean validate( Properties param )
                            throws Exception {

        if ( param.get( "-rootDir" ) == null ) {
            System.out.println( "parameter -rootDir must be set" );
            return false;
        }
        if ( param.get( "-resolution" ) == null ) {
            System.out.println( "parameter -resolution must be set" );
            return false;
        }
        if ( param.get( "-offset" ) == null ) {
            System.out.println( "parameter -offset must be set" );
            return false;
        }
        if ( param.get( "-scaleFactor" ) == null ) {
            System.out.println( "parameter -scaleFactor must be set" );
            return false;
        }

        return true;
    }

}
