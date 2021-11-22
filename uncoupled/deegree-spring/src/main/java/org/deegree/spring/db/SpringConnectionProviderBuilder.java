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

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.spring.AbstractSpringResourceBuilder;
import org.deegree.spring.db.jaxb.SpringConnectionProviderConfig;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.workspace.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpringConnectionProviderBuilder is used to build a 
 * @{link org.deegree.spring.db.SpringConnectionProvider}.
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SpringConnectionProviderBuilder extends AbstractSpringResourceBuilder<ConnectionProvider> {

    private static final Logger LOG = LoggerFactory.getLogger( SpringConnectionProviderBuilder.class );

    private final SpringConnectionProviderConfig config;

    private final SpringConnectionProviderMetadata metadata;

    public SpringConnectionProviderBuilder( final Workspace workspace, final SpringConnectionProviderMetadata metadata,
                                            final SpringConnectionProviderConfig config ) {
        super( workspace, config.getApplicationContextHolder() );

        this.metadata = metadata;
        this.config = config;
    }

    @Override
    public ConnectionProvider build() {
        LOG.debug( "Building SpringConnectionProvider" );

        final DataSource dataSource = getBean( DataSource.class, config.getDataSourceName(), "dataSource" );
        final SQLDialect dialect = getBean( SQLDialect.class, config.getSQLDialectName() );
        return new SpringConnectionProvider( metadata, dataSource, dialect );
    }
}
