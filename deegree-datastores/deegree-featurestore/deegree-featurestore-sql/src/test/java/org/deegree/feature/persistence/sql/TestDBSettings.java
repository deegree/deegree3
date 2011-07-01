//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.sql;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TestDBSettings {

    private static Logger LOG = LoggerFactory.getLogger( TestDBSettings.class );

    private static final String TESTDB_PROPERTY = "deegree.testdb.dir";

    private final String id;

    private final String adminUrl;

    private final String adminUser;

    private final String adminPass;

    private final String dbName;

    private final String url;

    private final String user;

    private final String pass;

    private TestDBSettings( String id, Properties props ) throws IllegalArgumentException {
        this.id = id;
        this.adminUrl = props.getProperty( "adminurl" );
        this.adminUser = props.getProperty( "adminuser" );
        this.adminPass = props.getProperty( "adminpass" );
        this.dbName = props.getProperty( "name" );
        this.url = props.getProperty( "url" );
        this.user = props.getProperty( "user" );
        this.pass = props.getProperty( "pass" );
    }

    public String getId() {
        return id;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPass() {
        return adminPass;
    }

    public String getDbName() {
        return dbName;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public static List<TestDBSettings> getAll()
                            throws IllegalArgumentException, IOException {

        String propValue = System.getProperty( TESTDB_PROPERTY );
        if ( propValue == null || propValue.isEmpty() ) {
            String msg = "Required system property '" + TESTDB_PROPERTY + "' is not set.";
            throw new IllegalArgumentException( msg );
        }

        File dir = new File( propValue );
        if ( !dir.exists() ) {
            String msg = "Directory '" + propValue + "' specified by system property '" + TESTDB_PROPERTY
                         + "' does not point to an existing directory.";
            throw new IllegalArgumentException( msg );
        }

        if ( !dir.isDirectory() ) {
            String msg = "Directory '" + propValue + "' specified by system property '" + TESTDB_PROPERTY
                         + "' does not point to a directory.";
            throw new IllegalArgumentException( msg );
        }

        File[] files = dir.listFiles( new FileFilter() {
            @Override
            public boolean accept( File pathname ) {
                return pathname.isFile() && pathname.getName().endsWith( ".properties" );
            }
        } );

        List<TestDBSettings> settings = new ArrayList<TestDBSettings>( files.length );
        for ( File propsFile : files ) {
            String id = propsFile.getName().substring( 0, propsFile.getName().length() - ".properties".length() );
            LOG.info( "Using test db config '" + id + "' from '" + propsFile + "'" );
            Properties props = new Properties();
            FileReader reader = new FileReader( propsFile );
            props.load( reader );
            reader.close();
            settings.add( new TestDBSettings( id, props ) );
        }
        return settings;
    }
}
