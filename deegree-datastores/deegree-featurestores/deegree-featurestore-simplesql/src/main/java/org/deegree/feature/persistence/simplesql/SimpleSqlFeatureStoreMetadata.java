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

package org.deegree.feature.persistence.simplesql;

import static org.deegree.feature.persistence.simplesql.SimpleSqlFeatureStoreProvider.CONFIG_SCHEMA;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.simplesql.jaxb.SimpleSQLFeatureStoreConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>SimpleSqlFeatureStoreMetadata</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class SimpleSqlFeatureStoreMetadata extends AbstractResourceMetadata<FeatureStore> {

    private static final Logger LOG = LoggerFactory.getLogger( SimpleSqlFeatureStoreMetadata.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.simplesql.jaxb";

    public SimpleSqlFeatureStoreMetadata( Workspace workspace, ResourceLocation<FeatureStore> location,
                                          AbstractResourceProvider<FeatureStore> provider ) {
        super( workspace, location, provider );
    }

    @Override
    public ResourceBuilder<FeatureStore> prepare() {
        SimpleSQLFeatureStoreConfig config;
        try {
            config = (SimpleSQLFeatureStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                         location.getAsStream(), workspace );
            String connId = config.getConnectionPoolId();
            if ( connId == null ) {
                connId = config.getJDBCConnId();
            }
            dependencies.add( new DefaultResourceIdentifier<ConnectionProvider>( ConnectionProviderProvider.class,
                                                                                 connId ) );
            return new SimpleSqlFeatureStoreBuilder( this, config, workspace );
        } catch ( Exception e ) {
            LOG.trace( "Stack trace:", e );
            throw new ResourceInitException( e.getLocalizedMessage(), e );
        }
    }

}
