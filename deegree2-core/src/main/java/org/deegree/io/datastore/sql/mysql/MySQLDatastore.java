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

package org.deegree.io.datastore.sql.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.sql.AbstractSQLDatastore;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.VirtualContentProvider;
import org.deegree.io.datastore.sql.postgis.PGgeometryAdapter;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.SortProperty;
import org.postgis.PGgeometry;
import org.postgis.binary.BinaryParser;
import org.postgis.binary.BinaryWriter;

/**
 * {@link Datastore} implementation for MySQL spatial databases.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MySQLDatastore extends AbstractSQLDatastore {

    private static final ILogger LOG = LoggerFactory.getLogger( MySQLDatastore.class );

    /**
     * Returns a specific {@link WhereBuilder} implementation for MySQL.
     *
     * @param rootFts
     *            involved (requested) feature types
     * @param aliases
     *            aliases for the feature types, may be null
     * @param filter
     *            filter that restricts the matched features
     * @param sortProperties
     *            sort criteria for the result, may be null or empty
     * @param aliasGenerator
     *            used to generate unique table aliases
     * @param vcProvider
     * @return <code>WhereBuilder</code> implementation for MySQL
     * @throws DatastoreException
     */
    @Override
    public MySQLWhereBuilder getWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                              SortProperty[] sortProperties, TableAliasGenerator aliasGenerator,
                                              VirtualContentProvider vcProvider )
                            throws DatastoreException {
        return new MySQLWhereBuilder( rootFts, aliases, filter, sortProperties, aliasGenerator, vcProvider );
    }

    /**
     * Converts a MySQL specific geometry <code>Object</code> (a byte[] containing WKB) from the <code>ResultSet</code>
     * to a deegree <code>Geometry</code>.
     *
     * @param value
     * @param targetCS
     * @param conn
     * @return corresponding deegree geometry
     * @throws SQLException
     */
    @Override
    public Geometry convertDBToDeegreeGeometry( Object value, CoordinateSystem targetCS, Connection conn )
                            throws SQLException {
        Geometry geometry = null;
        if ( value != null && value instanceof byte[] ) {
            try {
                LOG.logDebug( "Converting MySQL geometry (WKB byte array) to deegree geometry ('"
                              + targetCS.getIdentifier() + "')" );
                byte[] wkb = (byte[]) value;
                // parsing WKB using PostGIS' binary parser to generate PostGIS' geometry
                org.postgis.Geometry pgGeometry = new BinaryParser().parse( wkb );
                geometry = PGgeometryAdapter.wrap( pgGeometry, targetCS );
            } catch ( Exception e ) {
                e.printStackTrace();
                throw new SQLException( "Error converting MySQL geometry to deegree geometry: " + e.getMessage() );
            }
        }
        return geometry;
    }

    /**
     * Converts a deegree <code>Geometry</code> to a MySQL specific geometry object (a byte[] containing WKB).
     *
     * @param geometry
     * @param targetSRS
     * @param conn
     * @return corresponding PostGIS specific geometry object
     * @throws DatastoreException
     */
    @Override
    public byte[] convertDeegreeToDBGeometry( Geometry geometry, int targetSRS, Connection conn )
                            throws DatastoreException {
        byte[] wkb;
        try {
            PGgeometry pgGeometry = PGgeometryAdapter.export( geometry, targetSRS );
            wkb = new BinaryWriter().writeBinary( pgGeometry.getGeometry() );
        } catch ( GeometryException e ) {
            throw new DatastoreException( "Error converting deegree geometry to MySQL geometry: " + e.getMessage(), e );
        }
        return wkb;
    }

    @Override
    public void appendGeometryColumnGet( StatementBuffer query, String tableAlias, String column ) {
        query.append( "AsBinary(" );
        query.append( tableAlias );
        query.append( '.' );
        query.append( column );
        query.append( ')' );
    }
}
