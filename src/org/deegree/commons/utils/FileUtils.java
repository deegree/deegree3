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
package org.deegree.commons.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains static utility methods for handling files and filenames.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileUtils {

    private static Logger LOG = LoggerFactory.getLogger( FileUtils.class );

    /**
     * Returns the filename, without any extension and path (Eg. /tmp/foo.txt -> foo)
     * 
     * @param file
     * @return the basename or <code>null</code> if the file is null;
     */
    public static String getFilename( File file ) {
        if ( file == null ) {
            return null;
        }
        String path = splitFilename( file ).first;
        String parent = file.getParent();
        return ( parent != null ) ? ( path.substring( file.getParent().length() + 1 ) ) : path;
    }

    /**
     * Returns the filename, without any extension. (Eg. /tmp/foo.txt -> /tmp/foo)
     * 
     * @param file
     * @return the basename
     */
    public static String getBasename( File file ) {
        return splitFilename( file ).first;
    }

    /**
     * Returns the file extension (Eg. /tmp/foo.txt -> txt)
     * 
     * @param file
     * @return the file extension
     */
    public static String getFileExtension( File file ) {
        return splitFilename( file ).second;
    }

    /**
     * A helper method which will try to load the given configuration file (name) from the root directory before trying
     * to load it from the package of the given configuration class. Consider following example:
     * <p>
     * The org.deegree.geometry.GeometryFactory wants to load the File geometry_config.xml located in
     * org.deegree.geometry.configuration (hence the filename will be <i>configuration/geometry_config.xml</i>)<br />
     * . This method will first try to read geometry_config.xml (<b>without</b> the 'configuration' directory from the
     * given fileName) from the root directory '/' (e.g. WEB-INF/classes in a serlvet environment)<br />
     * If this was unsuccessful this method will try to load the file from the given packageName with the relative
     * fileName appended to it.
     * 
     * @param configurationClass
     *            will be used to read the stream from.
     * @param fileName
     *            name of the file to read.
     * @return the given file handle, or <code>null</code> if the given file could not be read (in either location) or
     *         either of the given parameters is <code>null</code>.
     */
    public static URL loadDeegreeConfiguration( Class<?> configurationClass, String fileName ) {
        if ( configurationClass == null ) {
            LOG.debug( "Configuration class is null" );
            return null;
        }
        if ( fileName == null || "".equals( fileName.trim() ) ) {
            LOG.debug( "The given fileName is null or emtpy" );
            return null;
        }
        int index = fileName.lastIndexOf( "/" );
        String rootFile = fileName;
        if ( index != -1 ) {
            rootFile = fileName.substring( index );
        }

        URL fileLocation = configurationClass.getResource( "/" + rootFile );
        if ( fileLocation == null ) {
            fileLocation = configurationClass.getResource( fileName );
        }
        return fileLocation;
    }

    /**
     * Split a filename in basename and extension.
     * 
     * @param file
     * @return a StringPair with basename and extension
     */
    private static StringPair splitFilename( File file ) {
        String filename = file.getName();
        File path = file.getParentFile();
        int pos = filename.lastIndexOf( "." );
        if ( pos != -1 ) {
            String basename = filename.substring( 0, pos );
            String suffix = filename.substring( pos + 1 );
            if ( path != null ) {
                basename = path.getPath() + File.separator + basename;
            }
            return new StringPair( basename, suffix );
        }
        return new StringPair( file.getPath(), "" );
    }

    /**
     * Writes the given {@link String} to the specified file.
     * 
     * @param file
     *            file to write to
     * @param content
     */
    public static void writeFile( File file, String content ) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file ) );
            writer.write( content );
        } catch ( IOException e ) {
            LOG.error( "Could not write to file '" + file.getAbsolutePath() + "'.", e );
        } finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch ( IOException e ) {
                    LOG.error( "Error closing file '" + file.getAbsolutePath() + "'.", e );
                }
            }
        }
    }

    /**
     * Writes the given {@link String} to a temporary file (created from specified prefix and suffix).
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param content
     */
    public static void writeTempFile( String filePrefix, String fileSuffix, String content ) {
        try {
            File tmpFile = File.createTempFile( filePrefix, fileSuffix );
            writeFile( tmpFile, content );
        } catch ( IOException e ) {
            LOG.error( "Cannot create temporary file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".",
                       e );
        }
    }

    /**
     * Writes the given binary data to the specified file.
     * 
     * @param file
     *            file to write to
     * @param data
     *            binary data to be written
     */
    public static void writeBinaryFile( File file, byte[] data ) {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream( new FileOutputStream( file ) );
            out.write( data );
        } catch ( IOException e ) {
            LOG.error( "Could not write to file '" + file.getAbsolutePath() + "'.", e );
        } finally {
            if ( out != null ) {
                try {
                    out.close();
                } catch ( IOException e ) {
                    LOG.error( "Error closing file '" + file.getAbsolutePath() + "'.", e );
                }
            }
        }
    }

    /**
     * Writes the given binary data to a temporary file (created from specified prefix and suffix).
     * 
     * @see File#createTempFile(String, String)
     * @param filePrefix
     *            prefix for the temp file name, must be at least three characters long
     * @param fileSuffix
     *            suffix for the temp file name, can be null (then ".tmp" is used)
     * @param data
     *            binary data to be written
     */
    public static void writeBinaryTempFile( String filePrefix, String fileSuffix, byte[] data ) {
        try {
            File tmpFile = File.createTempFile( filePrefix, fileSuffix );
            writeBinaryFile( tmpFile, data );
        } catch ( IOException e ) {
            LOG.error( "Cannot create temporary file for prefix '" + filePrefix + "' and suffix '" + fileSuffix + ".",
                       e );
        }
    }

    /**
     * Converts a <code>file:/...</code> <code>URL</code> into a file.
     * <p>
     * NOTE: The implementation uses an idea from <a
     * href="http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html">Kohsuke Kawaguchi</a>.
     * </p>
     * 
     * @param url
     *            <code>file:/...</code> URL
     * @return corresponding file object
     * @throws IllegalArgumentException
     *             if the given URL is not a <code>file:/...</code> URL
     */
    public static File getAsFile( URL url )
                            throws IllegalArgumentException {

        LOG.debug( "Protocol: '" + url.getProtocol() + "'" );

        File f;
        try {
            f = new File( url.toURI() );
        } catch ( URISyntaxException e ) {
            f = new File( url.getPath() );
        }
        return f;
    }

    /**
     * Find the files in the given directory (and sub-directories) which match the given extension pattern(s).
     * 
     * @param topDirectory
     *            to start the search from.
     * @param recurse
     * @param patternList
     *            comma seperated list of extensions to search for.
     * @return the list of files matching the given extension.
     */
    public static List<File> findFilesForExtensions( File topDirectory, boolean recurse, String patternList ) {
        List<File> result = new LinkedList<File>();
        if ( topDirectory.isDirectory() ) {
            FilenameFilter f = buildFilenameFilter( patternList );
            findFiles( topDirectory, result, recurse, f );
        }
        return result;
    }

    private static void findFiles( File dir, List<File> result, boolean recurse, FilenameFilter filter ) {
        for ( File file : dir.listFiles() ) {
            if ( file.isFile() ) {
                if ( filter.accept( dir, file.getName() ) ) {
                    result.add( file );
                }
            } else if ( file.isDirectory() && recurse ) {
                findFiles( file, result, recurse, filter );
            }
        }
    }

    private static FilenameFilter buildFilenameFilter( String patternList ) {

        String[] pats = null;
        if ( patternList == null || "".equals( patternList.trim() ) || "*.*".equals( patternList.trim() )
             || ".*".equals( patternList.trim() ) ) {
            pats = new String[] { "*" };
        } else {
            pats = patternList.split( "," );
            for ( int i = 0; i < pats.length; ++i ) {
                String ext = pats[i];
                if ( ext != null ) {
                    int index = ext.lastIndexOf( "." );
                    if ( index != -1 ) {
                        if ( index != ext.length() ) {
                            ext = ext.substring( index );
                        }
                    }
                    pats[i] = ext.toLowerCase();
                }
            }
        }
        final String[] patterns = Arrays.copyOf( pats, pats.length );

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept( File dir, String name ) {
                if ( name != null ) {
                    for ( String pattern : patterns ) {
                        if ( "*".equals( pattern ) || name.toLowerCase().endsWith( pattern ) ) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        return filter;
    }
}
