package org.deegree.spring.db;

import java.sql.Connection;

import javax.sql.DataSource;

import org.deegree.db.ConnectionProvider;
import org.deegree.sqldialect.SQLDialect;

import org.springframework.jdbc.datasource.DataSourceUtils;

public class SpringConnectionProvider implements ConnectionProvider {
    
    private final SpringConnectionProviderMetadata metadata;
    private final DataSource dataSource;
    private final SQLDialect dialect;
    
    public SpringConnectionProvider( final SpringConnectionProviderMetadata metadata, final DataSource dataSource, final SQLDialect dialect ) {
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
