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
package org.deegree.tools.shape;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.sql.Types;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.quadtree.DBQuadtreeManager;
import org.deegree.io.quadtree.IndexException;
import org.deegree.io.shpapi.HasNoDBaseFileException;

/**
 *
 *
 *
 * @version August 13th 2007
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version August 13th 2007
 *
 * @since 2.0
 */
public class GenericSQLShapeImporter {
    private static ILogger LOG = LoggerFactory.getLogger( GenericSQLShapeImporter.class );

    private void validate( Properties map ) {
        if ( null == map.get( "-driver" ) || "".equals( map.get( "-driver" ) ) ) {
            throw new InvalidParameterException( "-driver must be set" );
        }

        if ( null == map.get( "-url" ) || "".equals( map.get( "-url" ) ) ) {
            throw new InvalidParameterException( "-url must be set" );
        }

        if ( null == map.get( "-user" ) || "".equals( map.get( "-user" ) ) ) {
            throw new InvalidParameterException( "-user must be set" );
        }

        if ( null == map.get( "-password" ) || "".equals( map.get( "-password" ) ) ) {
            LOG.logInfo( "You supplied no password, is this correct?" );
        }

        if ( null == map.get( "-indexName" ) || "".equals( map.get( "-indexName" ) ) ) {
            throw new InvalidParameterException( "-indexName must be set" );
        }

        if ( null == map.get( "-table" ) || "".equals( map.get( "-table" ) ) ) {
            throw new InvalidParameterException( "-table must be set" );
        }

        if ( null == map.get( "-shapeFile" ) || "".equals( map.get( "-shapeFile" ) ) ) {
            throw new InvalidParameterException( "-shapeFile must be set" );
        }

        if ( null == map.get( "-maxTreeDepth" ) || "".equals( map.get( "-maxTreeDepth" ) ) ) {
            map.put( "-maxTreeDepth", new Integer( 6 ) );
        }

    }

    private void printHelp() {
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_DRIVER" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_URL" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_USER" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_PASSWORD" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_INDEXNAME" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_TABLE" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_OWNER" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_SHAPEFILE" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_MAXTREEDEPTH" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP_IDTYPE" ) );
        System.out.println( Messages.getMessage( "DATASTORE_GENERICSQLSHAPEIMPORTER.HELP" ) );
        System.exit( 1 );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; ) {
            String first = args[i++];
            if ( first != null && !"".equals( first.trim() ) && first.startsWith( "-" ) ) {
                // do we have no argument left
                if ( i + 1 >= args.length ) {
                    if ( first != null && !"".equals( first.trim() ) ) {
                        if ( first.trim().startsWith( "-" ) ) {
                            map.put( first, "" );
                        } else {
                            System.out.println( "The last commandline parameter doesn't start with a '-' sign, I'm confused, please check for previous errors (e.g. unescaped characters like spaces etc.) please quote all commandline arguments with spaces in them." );
                            System.exit( 1 );
                        }
                    }
                } else {
                    StringBuilder tmp = new StringBuilder( args[i] );
                    while ( i++ < args.length && !args[i].startsWith( "-" ) ) {
                        tmp.append( " " );
                        tmp.append( args[i] );
                    }
                    String second = tmp.toString();
                    map.put( first, second );
                }
            } else {
                i++;
            }
        }

        LOG.logInfo( "You supplied following command line values: " + map );

        GenericSQLShapeImporter gs = new GenericSQLShapeImporter();
        if ( map.get( "-?" ) != null || map.get( "-h" ) != null ) {
            gs.printHelp();
        }

        try {
            gs.validate( map );
        } catch ( InvalidParameterException ipe ) {
            LOG.logError( ipe.getMessage() + "\n" );
            gs.printHelp();
        }

        int depth = Integer.parseInt( map.getProperty( "-maxTreeDepth" ) );
        DBQuadtreeManager qtm = null;
        if ( "INTEGER".equalsIgnoreCase( (String) map.get( "-idType" ) ) ) {
            LOG.logInfo( "Using Integer as id type" );
            qtm = new DBQuadtreeManager<Integer>( map.getProperty( "-driver" ), map.getProperty( "-url" ),
                                                  map.getProperty( "-user" ), map.getProperty( "-password" ),
                                                  Charset.defaultCharset().name(), map.getProperty( "-indexName" ),
                                                  map.getProperty( "-table" ), "geometry", map.getProperty( "-owner" ),
                                                  depth, Types.INTEGER );
        } else {
            LOG.logInfo( "Using VARCHAR as id type" );
            qtm = new DBQuadtreeManager<String>( map.getProperty( "-driver" ), map.getProperty( "-url" ),
                                                 map.getProperty( "-user" ), map.getProperty( "-password" ),
                                                 Charset.defaultCharset().name(), map.getProperty( "-indexName" ),
                                                 map.getProperty( "-table" ), "geometry", map.getProperty( "-owner" ),
                                                 depth, Types.VARCHAR );
        }

        try {
            qtm.importShape( map.getProperty( "-shapeFile" ) );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage() );
            e.printStackTrace();
        } catch ( IndexException e ) {
            LOG.logError( e.getMessage() );
            e.printStackTrace();
        } catch ( HasNoDBaseFileException e ) {
            LOG.logError( e.getMessage() );
            e.printStackTrace();
        } catch ( DBaseException e ) {
            LOG.logError( e.getMessage() );
            e.printStackTrace();
        }
        System.exit( 0 );

    }

}
