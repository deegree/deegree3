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

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.tom.sql.SQLValueMangler;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.version.VersionMapping;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class VersionQueryHandler {

    public String retrieveVersion( Connection conn, TableName table, VersionMapping versionMapping, PrimitiveValue idParticle )
                            throws SQLException {
        if ( versionMapping != null ) {
            String versionSql = getVersionSql( table, versionMapping );
            PreparedStatement stmt = conn.prepareStatement( versionSql );

            Pair<SQLIdentifier, PrimitiveType> idColumn = versionMapping.getIdColumn();
            DefaultPrimitiveConverter idConverter = new DefaultPrimitiveConverter( idColumn.getSecond(),
                                                                                   idColumn.getFirst().getName() );
            idConverter.setParticle( stmt, idParticle, 1 );

            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                Pair<SQLIdentifier, PrimitiveType> versionColumn = versionMapping.getVersionColumn();
                DefaultPrimitiveConverter versionConverter = new DefaultPrimitiveConverter(
                                                                                            versionColumn.getSecond(),
                                                                                            versionColumn.getFirst().getName() );
                PrimitiveValue resultParticle = versionConverter.toParticle( rs, 1 );
                if ( resultParticle != null )
                    return resultParticle.getAsText();
            }
            stmt.close();
        }
        return null;
    }

    private String getVersionSql( TableName table, VersionMapping versionMapping ) {
        StringBuilder sql = new StringBuilder();
        sql.append( "SELECT " );
        sql.append( versionMapping.getVersionColumn().getFirst() );
        sql.append( " FROM " );
        sql.append( table );
        sql.append( " WHERE " );
        sql.append( versionMapping.getIdColumn().getFirst() );
        sql.append( " = ? " );
        return sql.toString();
    }
    
}