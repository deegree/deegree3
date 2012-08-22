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

package org.deegree.framework.concurrent;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import org.deegree.io.JDBCConnection;
import org.deegree.io.databaseloader.PostgisDataLoader;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wms.configuration.DatabaseDataSource;
import org.deegree.ogcwebservices.wms.operation.DimensionValues;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.DimensionValues.DimensionValue;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DoDatabaseQueryTask implements Callable<Object> {

    private DatabaseDataSource datasource;

    private Envelope envelope;

    private String sql;

    private Map<String, String> dimProps;

    private GetMap request;

    /**
     * Uses the sql template from the data source.
     * 
     * @param datasource
     * @param envelope
     */
    public DoDatabaseQueryTask( DatabaseDataSource datasource, Envelope envelope ) {
        this.datasource = datasource;
        this.envelope = envelope;
    }

    /**
     * Uses the given sql template
     * 
     * @param datasource
     * @param envelope
     * @param sql
     *            custom sql template to use for fetching the data
     */
    public DoDatabaseQueryTask( DatabaseDataSource datasource, Envelope envelope, String sql ) {
        this.datasource = datasource;
        this.envelope = envelope;
        this.sql = sql;
    }

    /**
     * @param datasource
     * @param envelope
     * @param sql
     * @param dimProps
     * @param request
     */
    public DoDatabaseQueryTask( DatabaseDataSource datasource, Envelope envelope, String sql,
                                Map<String, String> dimProps, GetMap request ) {
        this.datasource = datasource;
        this.envelope = envelope;
        this.sql = sql;
        this.dimProps = dimProps;
        this.request = request;
    }

    public FeatureResult call()
                            throws Exception {
        JDBCConnection jdbc = datasource.getJDBCConnection();
        String driver = jdbc.getDriver();
        FeatureCollection fc = null;

        String dimClause = null;
        if ( dimProps != null && dimProps.size() > 0 && ( request.getDimTime() != null || request.getDimElev() != null ) ) {
            DimensionValues val;
            String name;
            if ( request.getDimTime() != null ) {
                name = dimProps.get( "time" );
                val = request.getDimTime();
            } else {
                name = dimProps.get( "elevation" );
                val = request.getDimElev();
            }

            dimClause = "";

            // TODO support ranges?
            for ( DimensionValue v : val.values ) {
                if ( v.value != null ) {
                    if ( driver.toUpperCase().indexOf( "MYSQL" ) > -1 ) {
                        dimClause += " and " + name + " = \"" + v.value.replace( "T", " " ).replace( "Z", " " ).trim()
                                     + "\"";
                    } else {
                        dimClause += " and " + name + " = '" + v.value + "'";
                    }
                }
            }
        }

        if ( driver.toUpperCase().indexOf( "POSTGRES" ) > -1 ) {
            fc = PostgisDataLoader.load( datasource, envelope, sql, dimClause );
        } else if ( driver.toUpperCase().indexOf( "ORACLE" ) > -1 ) {
            Class<?> cls = Class.forName( "org.deegree.io.databaseloader.OracleDataLoader" );
            Method method = cls.getMethod( "load", DatabaseDataSource.class, Envelope.class, String.class, String.class );
            fc = (FeatureCollection) method.invoke( cls, datasource, envelope, sql, dimClause );
        } else if ( driver.toUpperCase().indexOf( "MYSQL" ) > -1 ) {
            Class<?> cls = Class.forName( "org.deegree.io.databaseloader.MySQLDataLoader" );
            Method method = cls.getMethod( "load", DatabaseDataSource.class, Envelope.class, String.class, String.class );
            fc = (FeatureCollection) method.invoke( cls, datasource, envelope, sql, dimClause );
        } else {
            throw new Exception( "unsuported database type: " + driver );
        }
        FeatureResult result = new FeatureResult( null, fc );
        return result;
    }
}
