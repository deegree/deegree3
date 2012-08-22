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
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.media.jai.TiledImage;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class RasterSplitter {

    private static final ILogger LOG = LoggerFactory.getLogger( RasterSplitter.class );

    private static final URI DEEGREEAPP = CommonNamespaces.buildNSURI( "http://www.deegree.org/app" );

    private static final String APP_PREFIX = "app";

    private String outDir;

    private int tileWidth = 0;

    private int tileHeight = 0;

    private String format;

    private List<String> fileList;

    /**
     *
     * @param rootDir
     * @param outDir
     * @param tileWidth
     * @param tileHeight
     * @param format
     * @param subDirs
     */
    RasterSplitter( String rootDir, String outDir, int tileWidth, int tileHeight, String format, boolean subDirs ) {
        this.outDir = outDir;
        this.format = format;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        fileList = getFileList( rootDir, subDirs );
    }

    /**
     * @param rootDir
     * @param outDir
     * @param tileWidth
     * @param tileHeight
     * @param dBase
     * @param format
     * @param fileColumn
     * @throws Exception
     */
    RasterSplitter( String rootDir, String outDir, int tileWidth, int tileHeight, String dBase, String format,
                    String fileColumn ) throws Exception {

        this.outDir = outDir;
        this.format = format;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        fileList = getFileList( dBase, fileColumn, rootDir );
    }

    /**
     * splits all files identified by the root dir or dbase file
     *
     * @throws Exception
     */
    public void perform()
                            throws Exception {
        for ( int i = 0; i < fileList.size(); i++ ) {
            System.out.print( "processing: " + fileList.get( i ) );
            splitFile( fileList.get( i ) );
        }
    }

    /**
     * returns the list of image map files to consider read from a dbase file defined by the dbase
     * parameter
     *
     * @param dbaseFile
     *            name of the dbase file
     * @param fileColumn
     *            name of the column containing the image map files names
     * @param baseDir
     *            name of the directory where the image map files are stored if this parameter is
     *            <code>null</code> it is assumed that the image map files are full referenced
     *            within the dbase
     * @return the list of image map files to consider read from a dbase file defined by the dbase
     *         parameter
     * @throws Exception
     */
    private static List<String> getFileList( String dBaseFile, String fileColumn, String baseDir )
                            throws Exception {

        System.out.println( "reading file list ..." );

        // handle dbase file extension and file location/reading problems
        if ( dBaseFile.endsWith( ".dbf" ) ) {
            dBaseFile = dBaseFile.substring( 0, dBaseFile.lastIndexOf( "." ) );
        }
        DBaseFile dbf = new DBaseFile( dBaseFile );

        // sort dbase file contents chronologicaly (oldest first)
        int cnt = dbf.getRecordNum();

        Object[] mapItems = new Object[cnt];
        QualifiedName fileC = new QualifiedName( APP_PREFIX, fileColumn.toUpperCase(), DEEGREEAPP );

        for ( int i = 0; i < cnt; i++ ) {
            // name of map file
            mapItems[i] = dbf.getFRow( i + 1 ).getDefaultProperty( fileC ).getValue();
        }

        // extract names of image files from dBase file and attach them to rootDir
        if ( baseDir == null ) {
            baseDir = "";
        } else if ( !baseDir.endsWith( "/" ) && !baseDir.endsWith( "\\" ) ) {
            baseDir = baseDir + '/';
        }
        List<String> imageFiles = new ArrayList<String>( mapItems.length );
        for ( int i = 0; i < mapItems.length; i++ ) {
            if ( mapItems[i] != null ) {
                imageFiles.add( baseDir + mapItems[i] );
            }
        }

        return imageFiles;
    }

    /**
     * returns the list of image map files to consider read from a defined root directory.
     *
     * @param rootDir
     *            root directory where to read image map files
     * @param subdirs
     *            true if subdirectories of the root directory shall be parsed for image maps too
     * @return the list of image map files to consider read from a defined root directory.
     */
    private static List<String> getFileList( String rootDir, boolean subdirs ) {
        System.out.println( "reading file list ..." );
        List<String> list = new ArrayList<String>( 10000 );
        File file = new File( rootDir );
        String[] entries = file.list( new DFileFilter() );
        if ( entries != null ) {
            for ( int i = 0; i < entries.length; i++ ) {
                File entry = new File( rootDir + '/' + entries[i] );
                if ( entry.isDirectory() && subdirs ) {
                    list = readSubDirs( entry, list );
                } else {
                    list.add( rootDir + '/' + entries[i] );
                }
            }
        }
        return list;
    }

    /**
     *
     * @param file
     * @param list
     * @return a list of strings
     */
    private static List<String> readSubDirs( File file, List<String> list ) {

        String[] entries = file.list( new DFileFilter() );
        if ( entries != null ) {
            for ( int i = 0; i < entries.length; i++ ) {
                File entry = new File( file.getAbsolutePath() + '/' + entries[i] );
                if ( entry.isDirectory() ) {
                    list = readSubDirs( entry, list );
                } else {
                    list.add( file.getAbsolutePath() + '/' + entries[i] );
                }
            }
        }
        return list;
    }

    private void splitFile( String inFile )
                            throws Exception {

        System.out.println( "processing: " + inFile );
        BufferedImage bi = ImageUtils.loadImage( inFile );
        WorldFile wf = WorldFile.readWorldFile( inFile, WorldFile.TYPE.CENTER, bi.getWidth(), bi.getHeight() );
        TiledImage ti = new TiledImage( bi, tileWidth, tileHeight );

        int cntx = bi.getWidth() / tileWidth;
        if ( cntx * tileWidth < bi.getWidth() ) {
            cntx++;
        }
        int cnty = bi.getHeight() / tileHeight;
        if ( cnty * tileHeight < bi.getHeight() ) {
            cnty++;
        }

        int p = inFile.lastIndexOf( '/' );
        String base = inFile.substring( p + 1, inFile.length() );
        base = StringTools.replace( base, ".", "_", true );

        double res = wf.getResx();
        Envelope env = wf.getEnvelope();
        for ( int i = 0; i < cntx; i++ ) {
            for ( int j = 0; j < cnty; j++ ) {
                String s = StringTools.concat( 200, "processing tile: ", i, ' ', j, " of ", cntx, '/', cnty, "\r" );
                System.out.print( s );
                System.gc();
                int w = tileWidth;
                int h = tileHeight;
                if ( i * tileWidth + tileWidth > bi.getWidth() ) {
                    w = bi.getWidth() - i * tileWidth;
                }
                if ( i * tileHeight + tileHeight > bi.getHeight() ) {
                    h = bi.getHeight() - i * tileHeight;
                }
                if ( w > 0 && h > 0 ) {
                    BufferedImage sub = ti.getSubImage( i * tileWidth, j * tileHeight, w, h ).getAsBufferedImage();
                    double x1 = env.getMin().getX() + i * tileWidth * res;
                    double y1 = env.getMax().getY() - ( j + 1 ) * tileHeight * res;
                    double x2 = env.getMin().getX() + ( i + 1 ) * tileWidth * res;
                    double y2 = env.getMax().getY() - j * tileHeight * res;
                    ;
                    Envelope subEnv = GeometryFactory.createEnvelope( x1, y1, x2, y2, null );
                    WorldFile subWf = new WorldFile( res, res, 0, 0, subEnv );
                    s = StringTools.concat( 300, outDir, '/', base, '_', i, '_', j, '.', format );
                    ImageUtils.saveImage( sub, s, 1 );
                    s = StringTools.concat( 300, outDir, '/', base, '_', i, '_', j );
                    WorldFile.writeWorldFile( subWf, s );
                }
            }
        }
        bi = null;
        System.gc();
    }

    private static void printHelp() {
        System.out.println();
        System.out.println( "Parameter description for RasterSplitter:" );
        System.out.println( "-tileWidth : desired width of result images (mandatory)" );
        System.out.println( "-tileHeight : desired width of result images (mandatory)" );
        System.out.println( "-format : desired image format of result images (mandatory)" );
        System.out.println( "-outDir : directory where result images shall be stored (mandatory)" );

        System.out.println( "-rootDir : directory from where images to split will be read (mandatory)" );
        System.out.println( "-subDirs : (true|false). If 'true' all sub directories of the 'rootDir' " );
        System.out.println( "            will be searched for images too (optional; default = false)" );
    }

    private static boolean validate( Properties map ) {
        if ( map.getProperty( "-tileWidth" ) == null ) {
            System.out.println( "-tileWidth must be set!" );
            return false;
        }
        if ( map.getProperty( "-tileHeight" ) == null ) {
            System.out.println( "-tileHeight must be set!" );
            return false;
        }
        if ( map.getProperty( "-format" ) == null ) {
            System.out.println( "-format must be set!" );
            return false;
        }
        if ( map.getProperty( "-outDir" ) == null ) {
            System.out.println( "-outDir must be set!" );
            return false;
        }
        if ( map.getProperty( "-rootDir" ) == null ) {
            System.out.println( "-rootDir must be set!" );
            return false;
        }
        if ( map.getProperty( "-subDirs" ) != null && !"true".equals( map.getProperty( "-subDirs" ) )
             && !"false".equals( map.getProperty( "-subDirs" ) ) ) {
            System.out.println( "if -subDirs is set it must be true or false!" );
            return false;
        }
        return true;
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }
        if ( !validate( map ) ) {
            printHelp();
            return;
        }

        int tileWidth = Integer.parseInt( map.getProperty( "-tileWidth" ) );
        int tileHeight = Integer.parseInt( map.getProperty( "-tileHeight" ) );
        String format = map.getProperty( "-format" );
        String outDir = map.getProperty( "-outDir" );

        RasterSplitter rs = null;
        if ( map.get( "-dbaseFile" ) != null ) {
            String dBaseFile = map.getProperty( "-dbaseFile" );
            String fileColum = map.getProperty( "-fileColumn" );
            String baseDir = map.getProperty( "-baseDir" );
            if ( baseDir == null ) {
                baseDir = map.getProperty( "-rootDir" );
            }
            rs = new RasterSplitter( baseDir, outDir, tileWidth, tileHeight, dBaseFile, format, fileColum );
        } else if ( map.get( "-rootDir" ) != null ) {
            String rootDir = map.getProperty( "-rootDir" );
            boolean subDirs = "true".equals( map.get( "-subDirs" ) );
            rs = new RasterSplitter( rootDir, outDir, tileWidth, tileHeight, format, subDirs );

        } else {
            LOG.logInfo( map.toString() );
            System.out.println( "-rootDir or -dbaseFile parameter must be defined" );
            return;
        }
        rs.perform();
    }

    /**
     * class: official version of a FilenameFilter
     */
    static class DFileFilter implements FilenameFilter {

        private List<String> extensions = null;

        /**
         *
         */
        public DFileFilter() {
            extensions = new ArrayList<String>();
            extensions.add( "JPEG" );
            extensions.add( "JPG" );
            extensions.add( "BMP" );
            extensions.add( "PNG" );
            extensions.add( "GIF" );
            extensions.add( "TIF" );
            extensions.add( "TIFF" );
            extensions.add( "GEOTIFF" );
        }

        /**
         * @return the String "*.*"
         */
        public String getDescription() {
            return "*.*";
        }

        /*
         * (non-Javadoc)
         *
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept( java.io.File file, String name ) {
            int pos = name.lastIndexOf( "." );
            String ext = name.substring( pos + 1 ).toUpperCase();
            if ( file.isDirectory() ) {
                String s = file.getAbsolutePath() + '/' + name;
                File tmp = new File( s );
                if ( tmp.isDirectory() ) {
                    return true;
                }
            }
            return extensions.contains( ext );
        }
    }
}
