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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ZipUtils {

    private static ILogger LOG = LoggerFactory.getLogger( ZipUtils.class );

    private StringBuffer details = null;

   

    /**
     * packs the passed files into a zip-archive, deletes the files if desired and returns the name
     * of the archive as absolute path
     *
     * @param dirName
     *            name of the directory where the files are found (see fileNames) and where the
     *            archive will be stored. Needs to be an absolute path.
     * @param archiveName
     *            desired name of the archive. It will be stored in the directory given in dirName.
     * @param fileNames
     *            names of the files to be packed into the zip archive. The files are expected to be
     *            in the directory given in dirName.
     * @param deleteFiles
     *            if true all files will be deleted after zip-file has been created
     * @param storeFolderPathInZip
     *            if true, the files are stored in the zip according to their folder structur (with
     *            absolute paths); if false, the files are stored under their file name only.
     * @return the name of the zip file (combined dirName and archiveName)
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String doZip( String dirName, String archiveName, String[] fileNames, boolean deleteFiles,
                         boolean storeFolderPathInZip )
                            throws FileNotFoundException, IOException {

        byte[] b = new byte[512];

        // make sure, that directory name ends with a file separator ("/" on Linux, or "\" on
        // Windows)
        if ( !dirName.endsWith( "/" ) && !dirName.endsWith( "\\" ) ) {
            dirName = dirName + File.separator;
        }

        File file = new File( archiveName );
        String archive = archiveName;
        if ( !file.isAbsolute() ) {
            archive = dirName + archiveName;
        }
        LOG.logDebug( "archive name: " + archive );

        ZipOutputStream zout = new ZipOutputStream( new FileOutputStream( archive ) );
        details = new StringBuffer();
        String[] absFileNames = new String[fileNames.length];

        for ( int i = 0; i < fileNames.length; i++ ) {
            file = new File( fileNames[i] );
            if ( !file.isAbsolute() ) {
                absFileNames[i] = dirName + fileNames[i];
            } else {
                absFileNames[i] = fileNames[i];
            }

            InputStream in = new FileInputStream( absFileNames[i] );
            ZipEntry e = null;
            if ( storeFolderPathInZip ) {
                e = new ZipEntry( absFileNames[i] );
            } else {
                e = new ZipEntry( file.getName() );
            }
            zout.putNextEntry( e );

            int len = 0;
            while ( ( len = in.read( b ) ) != -1 ) {
                zout.write( b, 0, len );
            }
            in.close();
            zout.closeEntry();
            details.append( createZipDetails( e ) + "\n" );
        }

        if ( deleteFiles ) {
            for ( int i = 0; i < absFileNames.length; i++ ) {
                file = new File( absFileNames[i] );
                LOG.logInfo( absFileNames[i] + " deleted: " + file.delete() );
            }
        }

        zout.close();
        return archive;
    }

    /**
     * @return details about the zipping
     */
    public String getZipDetails() {
        return details.toString();
    }

    /**
     * returns some information about the zip process of the current <code>ZipEntry</code>.
     *
     * @param e
     * @return information on the zip process
     */
    private StringBuffer createZipDetails( ZipEntry e ) {

        StringBuffer sb = new StringBuffer();

        sb.append( "added " + e.getName() );

        if ( e.getMethod() == ZipEntry.DEFLATED ) {
            long size = e.getSize();

            if ( size > 0 ) {
                long csize = e.getCompressedSize();
                long ratio = ( ( size - csize ) * 100 ) / size;
                sb.append( " (deflated " + ratio + "%)" );
            } else {
                sb.append( " (deflated 0%)" );
            }
        } else {
            sb.append( " (stored 0%)" );
        }

        return sb;
    }

    /**
     * @param file
     * @param outdir
     * @throws IOException
     */
    public void doUnzip( File file, String outdir )
                            throws IOException {
        this.doUnzip( new FileInputStream( file ), outdir );
    }

    /**
     * @param is
     * @param outdir
     * @throws IOException
     */
    public void doUnzip( InputStream is, String outdir )
                            throws IOException {
        int read = 0;
        byte[] data = new byte[1024];
        ZipEntry entry;
        // Archiv öffnen und mit Stream verbinden
        ZipInputStream in = new ZipInputStream( is );
        // Alle Einträge des Archivs auslesen
        while ( ( entry = in.getNextEntry() ) != null ) {
            if ( entry.getMethod() == ZipEntry.DEFLATED )
                System.out.println( "  Inflating: " + entry.getName() );
            else
                System.out.println( " Extracting: " + entry.getName() );
            // Anlegen der Ausgabedatei für den aktuellen Eintrag
            FileOutputStream out = new FileOutputStream( outdir + entry.getName() );
            // Daten des Eintrags aus dem Archiv lesen und in die
            // Ausgabedatei schreiben
            while ( ( read = in.read( data, 0, 1024 ) ) != -1 )
                out.write( data, 0, read );
            out.close();
        }
        in.close();
        System.out.println();
    }

}
