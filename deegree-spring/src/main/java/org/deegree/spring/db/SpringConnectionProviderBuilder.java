package org.deegree.spring.db;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.spring.AbstractSpringResourceBuilder;
import org.deegree.spring.db.jaxb.SpringConnectionProviderConfig;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.deegree.workspace.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return new SpringConnectionProvider( metadata, dataSource, new PostGISDialect( false ) );
    }
}
