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

package org.deegree.framework.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
public class FileMemory {

    private static ILogger LOG = LoggerFactory.getLogger( FileMemory.class );

    /**
     *
     * @param type
     *            any string that is valid to be part of a file name
     * @return last directory
     */
    public static File getLastDirectory( String type ) {
        Properties prop = new Properties();
        try {
            File tmpFile = File.createTempFile( "deegree_file_memory", ".txt" );
            String s = tmpFile.getAbsolutePath();
            s = s.substring( 0, s.lastIndexOf( File.separator ) + 1 );
            tmpFile = new File( s + "deegree_file_memory.txt" );
            if ( tmpFile.exists() ) {
                FileInputStream fis = new FileInputStream( tmpFile );
                prop.load( fis );
                fis.close();
            }
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
        }
        String s = prop.getProperty( type );
        if ( s == null ) {
            return new File( "." );
        }
        return new File( s );
    }

    /**
     *
     * @param type
     *            any string that is valid to be part of a file name
     * @param dir
     */
    public static void setLastDirectory( String type, File dir ) {
        Properties prop = new Properties();
        try {
            File tmpFile = File.createTempFile( "deegree_file_memory", ".txt" );
            String s = tmpFile.getAbsolutePath();
            s = s.substring( 0, s.lastIndexOf( File.separator ) + 1 );
            tmpFile = new File( s + "deegree_file_memory.txt" );
            if ( tmpFile.exists() ) {
                FileInputStream fis = new FileInputStream( tmpFile );
                prop.load( fis );
                fis.close();
            }
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
        }
        prop.put( type, dir.getAbsolutePath() );
        try {
            File tmpFile = File.createTempFile( "deegree_file_memory", ".txt" );
            String s = tmpFile.getAbsolutePath();
            s = s.substring( 0, s.lastIndexOf( File.separator ) + 1 );
            tmpFile = new File( s + "deegree_file_memory.txt" );
            FileOutputStream fos = new FileOutputStream( tmpFile );
            prop.store( fos, null );
            fos.close();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     *
     * @param type
     *            any string that is valid to be part of a file name
     * @return last file
     */
    public static File getLastFile( String type ) {
        // TODO
        return null;
    }

    /**
     *
     * @param type
     *            any string that is valid to be part of a file name
     * @param file
     */
    public static void setLastFile( String type, File file ) {
        // TODO
    }

}
