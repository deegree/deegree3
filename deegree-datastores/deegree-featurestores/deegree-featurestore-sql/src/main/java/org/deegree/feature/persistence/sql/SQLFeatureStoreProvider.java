//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.NewFeatureStoreProvider;
import org.deegree.sqldialect.SQLDialectManager;
import org.deegree.sqldialect.SQLDialectProvider;

/**
 * {@link FeatureStoreProvider} for {@link SQLFeatureStore} implementations.
 * <p>
 * This {@link FeatureStoreProvider} needs registered {@link SQLDialectProvider} implementations in order to actually
 * create {@link SQLFeatureStore} instances.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLFeatureStoreProvider implements FeatureStoreProvider {

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/sql";

    static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.sql.jaxb";

    static final URL CONFIG_SCHEMA = SQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/sql/3.2.0/sql.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public FeatureStore create( URL configURL )
                            throws ResourceInitException {
        String id = workspace.determineId( configURL, "datasources.feature" );
        return workspace.getNewWorkspace().getResource( NewFeatureStoreProvider.class, id );
    }

    /**
     * Returns the SQL statements for creating the database tables.
     * 
     * @return the SQL statements for creating the database tables, never <code>null</code>
     */
    public String[] getDDL() {
        return null;
    }

    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { SQLDialectManager.class };
    }
}