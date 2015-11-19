//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.feature.persistence.sql.version;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.version.VersionMapping;

/**
 * Handles queries to request the version of a feature if versioning is enabled.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class VersionQueryHandler {

    /**
     * @param conn
     *            the connection used to request the database, never <code>null</code>
     * @param featureTypeMapping
     *            the configured mapping of the feature type containing the version and fid mapping, never
     *            <code>null</code>
     * @param idAnalysis
     *            the analyzed id to retrieve the version for, never <code>null</code>
     * @return the version of the feature with the analyzed id, <code>null</code> if versioning is not enabled for the
     *         feature type or a feature with the analyzed id could not be found
     * @throws SQLException
     *             if an error occurred during communication with the db
     */
    public String retrieveVersion( Connection conn, FeatureTypeMapping featureTypeMapping, IdAnalysis idAnalysis )
                            throws SQLException {
        VersionMapping versionMapping = featureTypeMapping.getVersionMapping();
        if ( versionMapping != null ) {
            FIDMapping fidMapping = featureTypeMapping.getFidMapping();
            String versionSql = getVersionSql( versionMapping, fidMapping, featureTypeMapping.getFtTable() );
            PreparedStatement stmt = prepareStatement( conn, fidMapping, idAnalysis, versionSql );

            ResultSet rs = stmt.executeQuery();
            String version = null;
            if ( rs.next() ) {
                version = retrieveVersionFromResultSet( versionMapping, rs );
            }
            stmt.close();
            return version;
        }
        return null;
    }

    private String getVersionSql( VersionMapping versionMapping, FIDMapping fidMapping, TableName ftTable ) {
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT " );
        sql.append( versionMapping.getVersionColumn().getFirst() );
        sql.append( " FROM " );
        sql.append( ftTable );
        sql.append( " WHERE " );
        List<Pair<SQLIdentifier, BaseType>> columns = fidMapping.getColumns();
        for ( Pair<SQLIdentifier, BaseType> column : columns ) {
            sql.append( column.getFirst() );
        }
        sql.append( " = ? " );
        return sql.toString();
    }

    private PreparedStatement prepareStatement( Connection conn, FIDMapping fidMapping, IdAnalysis idAnalysis,
                                                String versionSql )
                            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement( versionSql );

        String[] idKernels = idAnalysis.getIdKernels();
        int i = 1;
        for ( String idKernel : idKernels ) {
            Pair<SQLIdentifier, BaseType> column = fidMapping.getColumns().get( i - 1 );
            PrimitiveType type = new PrimitiveType( column.getSecond() );
            DefaultPrimitiveConverter converter = new DefaultPrimitiveConverter( type, column.getFirst().getName() );
            PrimitiveValue value = new PrimitiveValue( idKernel, type );
            converter.setParticle( stmt, value, i++ );
        }
        return stmt;
    }

    private String retrieveVersionFromResultSet( VersionMapping versionMapping, ResultSet rs )
                            throws SQLException {
        Pair<SQLIdentifier, PrimitiveType> versionColumn = versionMapping.getVersionColumn();
        DefaultPrimitiveConverter versionConverter = new DefaultPrimitiveConverter( versionColumn.getSecond(),
                                                                                    versionColumn.getFirst().getName() );
        PrimitiveValue resultParticle = versionConverter.toParticle( rs, 1 );
        if ( resultParticle != null )
            return resultParticle.getAsText();
        return null;
    }

}