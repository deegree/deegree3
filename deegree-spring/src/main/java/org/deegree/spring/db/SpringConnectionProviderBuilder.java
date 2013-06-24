package org.deegree.spring.db;

import java.util.Map;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.ApplicationContextHolderProvider;
import org.deegree.spring.db.jaxb.SpringConnectionProviderConfig;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;

public class SpringConnectionProviderBuilder implements ResourceBuilder<ConnectionProvider>{
    
    private static final Logger LOG = LoggerFactory.getLogger( SpringConnectionProviderBuilder.class );
    
    private final SpringConnectionProviderMetadata metadata;
    private final Workspace workspace;
    private final SpringConnectionProviderConfig config;
    
    public SpringConnectionProviderBuilder ( final SpringConnectionProviderMetadata metadata, final Workspace workspace, final SpringConnectionProviderConfig config ) {
        this.metadata = metadata;
        this.workspace = workspace;
        this.config = config;
    }

    @Override
    public ConnectionProvider build() {
        final String applicationContextHolderId = config.getApplicationContextHolder();
        final String dataSourceName = config.getDataSourceName();
        
        LOG.debug( "Building SpringConnectionProvider (applicationContextHolder: {}, dataSourceName: {})", applicationContextHolderId, dataSourceName );
        
        final ApplicationContextHolder applicationContextHolder = workspace.getResource( ApplicationContextHolderProvider.class, applicationContextHolderId );        
        final ApplicationContext applicationContext = applicationContextHolder.getApplicationContext();
        
        final DataSource dataSource;
        if( dataSourceName != null) {
            try {
                dataSource = applicationContext.getBean( dataSourceName, DataSource.class );
                
                LOG.info( "Bean with name '{}' fetched from ApplicationContext", dataSourceName );
            } catch(Exception e) {
                throw new ResourceInitException( "Couldn't fetch bean with type DataSource and name '" + dataSourceName + "' from ApplicationContext", e );
            }
        } else {
            final Map<String, DataSource> dataSources = applicationContext.getBeansOfType(DataSource.class);
            if( dataSources.size() == 1 ) {
                dataSource = dataSources.values().iterator().next();
                
                LOG.info( "Single DataSource bean fetched from ApplicationContext" );
            } else {
                if( dataSources.containsKey( "dataSource" ) ) {
                    dataSource = dataSources.get( "dataSource" );
                    
                    LOG.info( "Multiple DataSource beans found in ApplicationContext, bean named 'dataSource' selected." ); 
                } else {
                    throw new ResourceInitException( "Multiple beans with type DataSource are found in ApplicationContext, non of them are named 'dataSource'. Suggestion: add bean name to configuration" );
                }
            }
        }

        return new SpringConnectionProvider( metadata, dataSource, new PostGISDialect( false ) );
    }
}
