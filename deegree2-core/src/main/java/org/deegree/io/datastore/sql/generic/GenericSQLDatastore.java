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
package org.deegree.io.datastore.sql.generic;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.sql.AbstractSQLDatastore;
import org.deegree.io.datastore.sql.QueryHandler;
import org.deegree.io.datastore.sql.SQLDatastoreConfiguration;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.VirtualContentProvider;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * {@link Datastore} implementation for any SQL database that can be accessed through a jdbc connection (even the
 * odbc-jdbc bridge is supported) and that supports the storing of BLOBs.
 * <p>
 * The spatial information is assumed to be stored in a BLOB field as a serialized deegree geometry. It also will be
 * assumed that a spatial index exists and that it has been created using deegree's quadtree api.
 *
 * @see org.deegree.io.quadtree
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GenericSQLDatastore extends AbstractSQLDatastore {

    private static final ILogger LOG = LoggerFactory.getLogger( GenericSQLDatastore.class );

    @Override
    public WhereBuilder getWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                         SortProperty[] sortProperties, TableAliasGenerator aliasGenerator,
                                         VirtualContentProvider vcProvider )
                            throws DatastoreException {
        JDBCConnection jdbc = ( (SQLDatastoreConfiguration) getConfiguration() ).getJDBCConnection();
        return new GenericSQLWhereBuilder( rootFts, aliases, filter, sortProperties, aliasGenerator, vcProvider, jdbc );
    }

    @Override
    protected FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts, Connection conn )
                            throws DatastoreException {

        Query transformedQuery = transformQuery( query );

        FeatureCollection result = null;
        try {
            QueryHandler queryHandler = new QueryHandler( this, new TableAliasGenerator(), conn, rootFts, query );
            result = queryHandler.performQuery();
        } catch ( SQLException e ) {
            String msg = "SQL error while performing query: " + e.getMessage();
            LOG.logError( msg, e );
            throw new DatastoreException( msg, e );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new DatastoreException( e );
        }

        if ( transformedQuery.getFilter() != null ) {
            try {
                LOG.logDebug( "Features (before refinement): " + result.size() );
                result = filterCollection( result, transformedQuery.getFilter() );
                LOG.logDebug( "Features (after refinement): " + result.size() );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new DatastoreException( e.getMessage(), e );
            }
        }

        result = transformResult( result, transformedQuery.getSrsName() );
        return result;
    }

    /**
     * Filters the feature collection using the given filter.
     * <p>
     * This is required because spatial filtering performed in the {@link GenericSQLWhereBuilder} just considers the
     * BBOX and non-spatial filter conditions.
     *
     * TODO remove BBOX + non-spatial conditions from the filter to speed up evaluation
     *
     * @param fc
     * @param filter
     * @return filtered feature collection
     */
    private FeatureCollection filterCollection( FeatureCollection fc, Filter filter )
                            throws FilterEvaluationException {
        for ( int i = fc.size() - 1; i >= 0; i-- ) {
            Feature feat = fc.getFeature( i );
            if ( !filter.evaluate( feat ) ) {
                fc.remove( i );
            }
        }
        return fc;
    }

    @Override
    public Geometry convertDBToDeegreeGeometry( Object value, CoordinateSystem targetCS, Connection conn )
                            throws SQLException {

        Geometry geometry = null;
        if ( value != null ) {
            try {
                if ( targetCS == null ) {
                    targetCS = CRSFactory.create( "EPSG:4326" );
                }
                if ( value instanceof String ) {
                    geometry = GMLGeometryAdapter.wrap( (String) value, null );
                } else if ( value instanceof InputStream ) {
                    StringBuffer sb = new StringBuffer( 10000 );
                    BufferedReader br = new BufferedReader( new InputStreamReader( (InputStream) value ) );
                    String line = null;
                    while ( ( line = br.readLine() ) != null ) {
                        sb.append( line );
                    }
                    geometry = GMLGeometryAdapter.wrap( sb.toString(), null );
                } else if ( value instanceof Reader ) {
                    StringBuffer sb = new StringBuffer( 10000 );
                    BufferedReader br = new BufferedReader( (Reader) value );
                    String line = null;
                    while ( ( line = br.readLine() ) != null ) {
                        sb.append( line );
                    }
                    geometry = GMLGeometryAdapter.wrap( sb.toString(), null );
                } else {
                    geometry = GMLGeometryAdapter.wrap( new String( (byte[]) value ), null );
                }
                ( (GeometryImpl) geometry ).setCoordinateSystem( targetCS );
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "DATASTORE_GENERICSQL_GEOM_READ_ERROR" );
                LOG.logError( msg, e );
                throw new SQLException( msg + e.getMessage() );
            }
        }
        return geometry;
    }

    @Override
    public Object convertDeegreeToDBGeometry( Geometry geometry, int nativeSRSCode, Connection conn )
                            throws DatastoreException {
        String sqlObject = null;
        try {
            sqlObject = GMLGeometryAdapter.export( geometry ).toString();
            sqlObject = StringTools.replace( sqlObject, ">", " xmlns:gml=\"http://www.opengis.net/gml\">", false );
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
            throw new DatastoreException( e.getMessage(), e );
        }
        JDBCConnection jdbc = ( (SQLDatastoreConfiguration) getConfiguration() ).getJDBCConnection();
        Object result = null;
        // concrete from depends on backend. At the moment special mapping just is defined
        // for HSQLDB, Postgres und INGRES. Default will work for MS SQLSERVER
        if ( jdbc.getDriver().toLowerCase().indexOf( "ingres" ) > -1
             || jdbc.getDriver().equalsIgnoreCase( "ca.edbc.jdbc.EdbcDriver" ) ) {
            result = new StringReader( sqlObject );
        } else if ( jdbc.getDriver().toLowerCase().indexOf( "hsqldb" ) > -1
                    || jdbc.getDriver().toLowerCase().indexOf( "postgres" ) > -1 ) {
            // only postgres is able to handle large strings as they are
            result = sqlObject;
        } else {
            result = sqlObject.getBytes();
        }
        return result;
    }

    @Override
    protected GenericSQLTransaction createTransaction()
                            throws DatastoreException {

        return new GenericSQLTransaction( this, new TableAliasGenerator(), acquireConnection() );
    }
}
