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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.WKTAdapter;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Shp2MySQL {

    private static final ILogger LOG = LoggerFactory.getLogger( Shp2MySQL.class );

    private ArrayList<String> fileList = new ArrayList<String>();

    private String driver;

    private String url;

    private String user;

    private String password;

    /**
     * Creates a new Shp2MySQL object.
     *
     * @param file
     * @param driver
     * @param url
     * @param user
     * @param password
     */
    public Shp2MySQL( String file, String driver, String url, String user, String password ) {
        fileList.add( file );
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * @throws Exception
     */
    public void run()
                            throws Exception {

        for ( int i = 0; i < fileList.size(); i++ ) {

            DBConnectionPool pool = DBConnectionPool.getInstance();
            Connection conn = pool.acquireConnection( driver, url, user, password );
            PreparedStatement stmt = conn.prepareStatement( "insert into sos_tab (id, value1,value2,value3) values (?,?,?,?)" );

            ShapeFile sf = new ShapeFile( fileList.get( i ) );

            // delete table if already exists
            String tabName = sf.getFeatureByRecNo( 1 ).getFeatureType().getName().getLocalName();
            try {
                stmt.execute( "drop table " + tabName );
            } catch ( Exception e ) {
                System.out.println( "table " + tabName + " does not exist!" );
            }

            // get createtable sql statement and write it to the file
            String createTable = getCreateTableStatement( sf.getFeatureByRecNo( 1 ).getFeatureType() );
            stmt.execute( createTable );

            // create an insert statement for each feature conained in
            // the shapefile
            for ( int j = 0; j < sf.getRecordNum(); j++ ) {
                if ( j % 50 == 0 ) {
                    System.out.print( "." );
                }

                StringBuffer names = new StringBuffer( "(" );
                StringBuffer values = new StringBuffer( " VALUES (" );

                Feature feature = sf.getFeatureByRecNo( j + 1 );
                FeatureType ft = feature.getFeatureType();
                PropertyType ftp[] = ft.getProperties();
                for ( int k = 0; k < ftp.length; k++ ) {
                    if ( ftp[k].getType() == Types.GEOMETRY ) {
                        values.append( "GeomFromText(?)" );
                    } else {
                        values.append( '?' );
                    }
                    QualifiedName name = ftp[k].getName();
                    names.append( name.getLocalName() );
                    if ( k < ftp.length - 1 ) {
                        names.append( "," );
                        values.append( "," );
                    }
                }
                names.append( ")" );
                values.append( ")" );
                LOG.logDebug( "Insert into " + tabName + " " + names + values );
                stmt = conn.prepareStatement( "Insert into " + tabName + " " + names + values );
                for ( int k = 0; k < ftp.length; k++ ) {
                    Object value = feature.getProperties( ftp[k].getName() )[0].getValue();
                    if ( ftp[k].getType() == Types.GEOMETRY ) {
                        value = WKTAdapter.export( (Geometry) value ).toString();
                        stmt.setObject( k + 1, value, Types.VARCHAR );
                    } else if ( ftp[k].getType() == Types.VARCHAR || ftp[k].getType() == Types.CHAR ) {
                        if ( value != null ) {
                            value = StringTools.replace( (String) value, "'", "\\'", true );
                            value = StringTools.replace( (String) value, "\"", "\\\"", true );
                        }
                        stmt.setObject( k + 1, value, Types.VARCHAR );
                    } else if ( ftp[k].getType() == Types.DOUBLE || ftp[k].getType() == Types.FLOAT ) {
                        if ( value != null ) {
                            value = Double.parseDouble( value.toString() );
                        }
                        stmt.setObject( k + 1, value, Types.DOUBLE );
                    } else if ( ftp[k].getType() == Types.INTEGER || ftp[k].getType() == Types.BIGINT ) {
                        if ( value != null ) {
                            value = Integer.parseInt( value.toString() );
                        }
                        stmt.setObject( k + 1, value, Types.INTEGER );
                    } else if ( ftp[k].getType() == Types.DATE ) {
                        stmt.setObject( k + 1, value, Types.DATE );
                    }

                }
                stmt.execute();
            }
            sf.close();
            stmt.close();
            pool.releaseConnection( conn, driver, url, user, password );
        }

        LOG.logInfo( "finished!" );

    }

    /**
     * creates a create table sql statement from the passed <tt>FeatureType</tt>
     *
     * @param ft
     *            feature type
     * @return the created SQL statement
     */
    private String getCreateTableStatement( FeatureType ft ) {

        StringBuffer sb = new StringBuffer();
        String name = ft.getName().getLocalName();

        PropertyType[] ftp = ft.getProperties();

        sb.append( "CREATE TABLE " ).append( name ).append( " (" );
        for ( int i = 0; i < ftp.length; i++ ) {
            sb.append( ftp[i].getName().getLocalName() ).append( " " );
            int type = ftp[i].getType();
            try {
                LOG.logDebug( Types.getTypeNameForSQLTypeCode( type ) + " " + ftp[i].getName().getLocalName() );
            } catch ( UnknownTypeException e ) {
                // just for debugging purposes
            }
            if ( type == Types.VARCHAR ) {
                sb.append( " VARCHAR(255) " );
            } else if ( type == Types.DOUBLE || type == Types.FLOAT ) {
                sb.append( " DOUBLE(20,8) " );
            } else if ( type == Types.INTEGER || type == Types.BIGINT ) {
                sb.append( " INT(12) " );
            } else if ( type == Types.DATE ) {
                sb.append( " Date " );
            } else if ( type == Types.GEOMETRY || type == Types.POINT || type == Types.CURVE || type == Types.SURFACE
                        || type == Types.MULTIPOINT || type == Types.MULTICURVE || type == Types.MULTISURFACE ) {
                sb.append( " GEOMETRY NOT NULL" );
            }
            if ( i < ftp.length - 1 ) {
                sb.append( "," );
            }
        }
        sb.append( ")" );
        LOG.logDebug( "Create table statement: ", sb );
        return sb.toString();
    }

    /**
     * prints out helping application-information.
     *
     * @param n
     *            an integer parameter, which determines which help-information should be given out.
     */
    private static void usage( int n ) {
        switch ( n ) {
        case 0:
            System.out.println( "usage: java -classpath .;deegree.jar de.tools.Shp2MySQL "
                                + "[-f shapefile -driver driver -url url -user user -password password \n"
                                + " -d sourcedirectory] [--version] [--help]\n\n arguments:\n"
                                + "    -f shapefile  reads the input shapefile. must be set\n"
                                + "                  if -d is not set.\n"
                                + "    -d inputdir   name of the directory that contains the.\n"
                                + "                  source shapefiles. must be set if -f is\n"
                                + "                  not set.\n"
                                + "    -driver database driver class  JDBC driver class name \n"
                                + "                  (default: com.mysql.jdbc.Driver) \n"
                                + "    -url database connection  URL for connecting target database. \n"
                                + "                   Example: jdbc:mysql://localhost:3306/deegree \n"
                                + "    -user user database user name  \n"
                                + "    -password password database user's password  \n\n\n" + "information options:\n"
                                + "    --help      shows this help.\n"
                                + "    --version   shows the version and exits.\n" );
            break;
        case 1:
            System.out.println( "Try 'java -classpath .;deegree.jar de.tools.Shp2MySQL --help'\n"
                                + "for more information." );
            break;

        default:
            System.out.println( "Unknown usage: \n" + "Try 'java -classpath .;deegree.jar de.tools.Shp2MySQL --help'\n"
                                + "for more information." );
            break;
        }
    }

    /**
     * @param args
     *            the command line arguments
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        if ( args == null || args.length < 6 ) {
            usage( 0 );
            System.exit( 1 );
            return;
        }

        try {
            HashMap<String, String> map = new HashMap<String, String>();

            for ( int i = 0; i < args.length; i += 2 ) {
                map.put( args[i], args[i + 1] );
            }

            if ( map.get( "-url" ) == null ) {
                usage( 0 );
                System.exit( 0 );
            }

            if ( map.get( "-driver" ) == null ) {
                map.put( "-driver", "com.mysql.jdbc.Driver" );
            }

            if ( map.get( "--help" ) != null ) {
                usage( 0 );
                System.exit( 0 );
            }

            if ( map.get( "--version" ) != null ) {
                System.out.println( "Shp2MySQL version 1.0.0" );
                System.exit( 0 );
            }

            // one single file shall be transformed
            if ( map.get( "-f" ) != null ) {

                String f = map.get( "-f" );
                if ( f.toUpperCase().endsWith( ".SHP" ) ) {
                    f = f.substring( 0, f.length() - 4 );
                }
                Shp2MySQL shp = new Shp2MySQL( f, map.get( "-driver" ), map.get( "-url" ), map.get( "-user" ),
                                               map.get( "-password" ) );
                shp.run();
            } else {
                System.out.println( "option -d is not supported at the moment" );
                // TODO
                // the files of a whole directory shall be inserted
            }
        } catch ( Throwable e ) {
            e.printStackTrace();
        } finally {
            System.exit( 0 );
        }
    }
}
