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

package org.deegree.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;

/**
 * tool for creating a properties file mapping each property key of a defined source properties file to the class where
 * it is used
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ClassMessagesCreator {

    private Properties descProperties;

    private Properties classesProperties;

    private String[] roots;

    private String outputFile;

    /**
     *
     * @param propertiesFileName
     * @param outputFile
     * @param rootDirs
     * @throws Exception
     */
    public ClassMessagesCreator( String propertiesFileName, String outputFile, String... rootDirs ) throws Exception {
        File file = new File( propertiesFileName );
        descProperties = new Properties();
        FileInputStream fis = new FileInputStream( file );
        descProperties.load( fis );
        System.out.println( "defined properties: " + descProperties.size() );
        fis.close();
        classesProperties = new Properties();
        this.roots = rootDirs;
        this.outputFile = outputFile;
    }

    /**
     *
     * @throws IOException
     */
    public void run()
                            throws IOException {
        List<File> fileList = new ArrayList<File>( 100 );
        for ( int i = 0; i < roots.length; i++ ) {
            // '/' as path seperator
            roots[i] = StringTools.replace( roots[i], "\\", "/", true );
            File root = new File( roots[i] );
            File[] files = root.listFiles( new ConvenienceFileFilter( true, "JAVA", "JSP" ) );
            for ( File file : files ) {
                fileList.add( file );
            }
            // remove disk char (windows)
            if ( roots[i].indexOf( ':' ) > -1 ) {
                int p = roots[i].indexOf( ':' );
                roots[i] = roots[i].substring( p + 1 );
            }
        }

        collect( fileList.toArray( new File[fileList.size()] ) );

        FileOutputStream fos = new FileOutputStream( outputFile );
        // Iterator iterator = descProperties.keySet().iterator();
        for ( Object k : descProperties.keySet() ) {
            String key = k.toString();
            classesProperties.put( key, "------- N O T    U S E D --------" );
        }
        classesProperties.store( fos, "" );
        fos.close();
    }

    private void collect( File[] files )
                            throws IOException {
        for ( File file : files ) {
            if ( file.isDirectory() ) {
                System.out.println( file );
                collect( file.listFiles( new ConvenienceFileFilter( true, "JAVA", "JSP" ) ) );
            } else {
                String s = FileUtils.readTextFile( file ).toString();
                List<String> list = new ArrayList<String>( 100 );
                for ( Object k : descProperties.keySet() ) {
                    String key = k.toString();
                    if ( s.indexOf( "\"" + key + "\"" ) > -1 ) {
                        String tmp = file.getAbsolutePath();
                        tmp = StringTools.replace( tmp, "\\", "/", true );
                        if ( !tmp.toUpperCase().endsWith( ".JSP" ) ) {
                            int p = tmp.indexOf( "org\\deegree" );
                            if ( p < 0 ) {
                                p = tmp.indexOf( "org/deegree" );
                            }
                            if ( p < 0 ) {
                                p = 0;
                            }
                            tmp = tmp.substring( p, tmp.length() - 5 );
                            tmp = StringTools.replace( tmp, "/", ".", true );
                        } else {
                            for ( int i = 0; i < roots.length; i++ ) {
                                int p = tmp.indexOf( roots[i] );
                                if ( p > -1 ) {
                                    // cut root dir name from file path
                                    tmp = tmp.substring( p + roots[i].length() );
                                    break;
                                }
                            }
                        }
                        classesProperties.put( key, tmp );
                        list.add( key );
                    }
                }
                for ( String key : list ) {
                    descProperties.remove( key );
                }
            }
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            System.out.println( args[i + 1] );
            map.put( args[i], args[i + 1] );
        }
        try {
            validate( map );
        } catch ( Exception e ) {
            System.out.println( e.getMessage() );
            System.out.println();
            printHelp();
            return;
        }
        String[] rootDirs = StringTools.toArray( map.getProperty( "-rootDirs" ), ",;", true );

        ClassMessagesCreator cmc = new ClassMessagesCreator( map.getProperty( "-properties" ),
                                                             map.getProperty( "-outFile" ), rootDirs );
        cmc.run();
    }

    private static void printHelp() {
        System.out.println( "------------------------------------------------" );
        System.out.println( "usage/parameters:" );
        System.out.println();
        System.out.println( "-properties : name/path of the properties file to analyse." );
        System.out.println( "           example: -properties  /src/org/deegree/i18n/messages.properties" );
        System.out.println();
        System.out.println( "-outFile : name/path of the file to store the results" );
        System.out.println( "           example: -outFile /src/org/deegree/i18n/messages_classes.properties" );
        System.out.println();
        System.out.println( "-rootDirs name/path of directories from where to start searching for used properties" );
        System.out.println( "           example: -rootDirs /src/org/deegree,/src/igeoportal" );
    }

    private static void validate( Properties map )
                            throws Exception {
        if ( map.get( "-properties" ) == null ) {
            throw new Exception( "-properties (name/path of the properties file to analyse) must be set" );
        }
        if ( map.get( "-outFile" ) == null ) {
            throw new Exception( "-outFile (name/path of the file to store the results) must be set" );
        }
        if ( map.get( "-rootDirs" ) == null ) {
            throw new Exception( "-rootDirs (name/path of directories from where to start searching for "
                                 + "used properties) must be set" );
        }

    }

}
