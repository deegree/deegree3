//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2013 by:

 IDgis bv

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

 IDgis bv
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 http://idgis.nl/ 

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
package org.deegree.spring.db;

import java.sql.Connection;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.sqldialect.SQLDialect;

import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * A SpringConnectionProvider provides a {@link java.sql.Connection} from a
 * {@link javax.sql.DataSource} available as bean within the configured 
 * application context using 
 * {@link org.springframework.jdbc.datasource.DataSourceUtils}. 
 * 
 * The {@link org.deegree.sqldialect.SQLDialect} is also
 * expected to be available as bean.
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SpringConnectionProvider implements ConnectionProvider {

    private final SpringConnectionProviderMetadata metadata;

    private final DataSource dataSource;

    private final SQLDialect dialect;

    public SpringConnectionProvider( final SpringConnectionProviderMetadata metadata, final DataSource dataSource,
                                     final SQLDialect dialect ) {
        this.metadata = metadata;
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    @Override
    public SpringConnectionProviderMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Connection getConnection() {
        return DataSourceUtils.getConnection( dataSource );
    }

    @Override
    public SQLDialect getDialect() {
        return dialect;
    }

    @Override
    public void invalidate( Connection conn ) {
        DataSourceUtils.releaseConnection( conn, dataSource );
    }
}
