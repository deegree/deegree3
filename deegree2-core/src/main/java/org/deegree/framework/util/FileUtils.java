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
package org.deegree.framework.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * the class offeres several static methods for handling file access
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
public class FileUtils {

    /**
     * writes the the passed string to a file created using the passed file name. For writing to the resource an
     * <code>OutputStreamReader</code> with encoding read from <code>CharsetUtils.getSystemCharset()</code>.
     *
     * @param fileName
     * @param data
     * @throws IOException
     */
    public static final void writeToFile( String fileName, String data )
                            throws IOException {
        writeToFile( fileName, data, CharsetUtils.getSystemCharset() );
    }

    /**
     * writes the the passed string to a file created using the passed file name using the defined character encoding.
     *
     * @param fileName
     * @param data
     * @param encoding
     * @throws IOException
     */
    public static final void writeToFile( String fileName, String data, String encoding )
                            throws IOException {
        FileOutputStream fos = new FileOutputStream( fileName );
        OutputStreamWriter osr = new OutputStreamWriter( fos, encoding );
        osr.write( data );
        osr.close();
    }

    /**
     * appends the passed string to the file identified by the passed name. If the file does not exist an exception will
     * be thrown.
     *
     * @param fileName
     * @param data
     * @throws IOException
     */
    public static final void appendsToFile( String fileName, String data )
                            throws IOException {
        File file = new File( fileName );
        if ( !file.exists() ) {
            throw new IOException( "file: " + fileName + " does not exist" );
        }
        RandomAccessFile raf = new RandomAccessFile( file, "rw" );
        raf.seek( raf.length() );
        raf.writeChars( data );
        raf.close();
    }

    /**
     * reads a Text file from its resource. For accessing the resource an <code>InputStreamReader</code> with encoding
     * read from <code>CharsetUtils.getSystemCharset()</code>
     *
     * @param file
     * @return contents of the file as a {@link StringBuffer}
     * @throws IOException
     */
    public static StringBuffer readTextFile( File file )
                            throws IOException {
        return readTextFile( file.toURL() );
    }

    /**
     * reads a Text file from its resource. For accessing the resource an <code>InputStreamReader</code> with encoding
     * read from <code>CharsetUtils.getSystemCharset()</code>
     *
     * @param url
     * @return contents of the url as a {@link StringBuffer}
     * @throws IOException
     */
    public static StringBuffer readTextFile( URL url )
                            throws IOException {
        return readTextFile( url.openStream() );
    }

    /**
     * reads a Text file from its resource. For accessing the resource an <code>InputStreamReader</code> with encoding
     * read from <code>CharsetUtils.getSystemCharset()</code>
     *
     * @param is
     * @return contents of the input stream as a {@link StringBuffer}
     * @throws IOException
     */
    public static StringBuffer readTextFile( InputStream is )
                            throws IOException {
        InputStreamReader isr = new InputStreamReader( is, CharsetUtils.getSystemCharset() );
        return readTextFile( isr );
    }

    /**
     * reads a Text file from its resource.
     *
     * @param reader
     * @return contents of the reader as a {@link StringBuffer}
     * @throws IOException
     */
    public static StringBuffer readTextFile( Reader reader )
                            throws IOException {
        StringBuffer sb = new StringBuffer( 10000 );
        int c = 0;
        while ( ( c = reader.read() ) > -1 ) {
            sb.append( (char) c );
        }
        reader.close();

        return sb;
    }

    /**
     * copies a file to another
     *
     * @param from
     * @param to
     * @throws IOException
     */
    public static void copy( File from, File to )
                            throws IOException {
        RandomAccessFile rafIn = new RandomAccessFile( from, "r" );
        RandomAccessFile rafOut = new RandomAccessFile( to, "rw" );
        byte[] b = new byte[500000];
        int r = 0;
        do {
            r = rafIn.read( b );
            if ( r > 0 ) {
                rafOut.write( b, 0, r );
            }
        } while ( r == b.length );
        rafIn.close();
        rafOut.close();
    }

    /**
     * @param oldRegex
     * @param replacement
     * @param inFile
     *            file.encoding encoded text file
     * @param outFile
     *            file.encoding encoded text file
     * @throws IOException
     */
    public static void replace( String oldRegex, String replacement, File inFile, File outFile )
                            throws IOException {
        replace( oldRegex, replacement, new BufferedReader( new FileReader( inFile ) ),
                 new BufferedWriter( new FileWriter( outFile ) ) );
    }

    /**
     * Closes the streams!
     *
     * @param oldRegex
     * @param replacement
     * @param reader
     * @param writer
     * @throws IOException
     */
    public static void replace( String oldRegex, String replacement, Reader reader, Writer writer )
                            throws IOException {
        BufferedReader in = new BufferedReader( reader );
        PrintWriter out = new PrintWriter( writer );
        String s;
        while ( ( s = in.readLine() ) != null ) {
            out.println( s.replaceAll( oldRegex, replacement ) );
        }
        in.close();
        out.close();
    }

    /**
     *
     * Resolves the given URL (which may be relative) against a rootPath into an <code>URL</code> (which is always
     * absolute).
     *
     * @param rootPath
     * @param url
     * @return the resolved URL object
     */
    public static final URL resolt( URL rootPath, String url )
                            throws MalformedURLException {
        // check if url is an absolut path
        File file = new File( url );
        if ( file.isAbsolute() ) {
            return file.toURI().toURL();
        }
        // remove leading '/' because otherwise
        // URL resolvedURL = new URL( systemId, url ); will fail
        if ( url.startsWith( "/" ) ) {
            url = url.substring( 1, url.length() );

        }
        return new URL( rootPath, url );
    }

}
