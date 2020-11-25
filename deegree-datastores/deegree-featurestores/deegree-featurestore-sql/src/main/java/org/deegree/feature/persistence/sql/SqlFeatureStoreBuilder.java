//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.feature.persistence.sql;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;

import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * <code>SqlFeatureStoreBuilder</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class SqlFeatureStoreBuilder implements ResourceBuilder<FeatureStore> {

    private static final Logger LOG = getLogger( SqlFeatureStoreBuilder.class );

    private SqlFeatureStoreMetadata metadata;

    private SQLFeatureStoreJAXB config;

    private Workspace workspace;

    public SqlFeatureStoreBuilder( SqlFeatureStoreMetadata metadata, SQLFeatureStoreJAXB config, Workspace workspace ) {
        this.metadata = metadata;
        this.config = config;
        this.workspace = workspace;
    }

    @Override
    public FeatureStore build() {
        ConnectionProvider conn = workspace.getResource( ConnectionProviderProvider.class,
                                                         config.getJDBCConnId().getValue() );
        checkConnection( conn );

        File file = metadata.getLocation().getAsFile();
        SQLFeatureStore fs = null;
        try {
            // TODO rewrite needed to properly resolve files using resource location
            fs = new SQLFeatureStore( config, file.toURI().toURL(), conn.getDialect(), metadata, workspace );
        } catch ( MalformedURLException e ) {
            LOG.trace( "Stack trace:", e );
        }
        return fs;
    }

    private void checkConnection( ConnectionProvider conn ) {
        if ( conn == null ) {
            String msg = "Unable to create SqlFeatureStore: Connection with identifier "
                         + config.getJDBCConnId().getValue() + " is not available.";
            LOG.error( msg );
            throw new ResourceInitException( msg );
        }
    }

}
