package org.deegree.console.jdbc;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceState;
import org.deegree.console.Config;
import org.deegree.console.ResourceManagerMetadata2;
import org.deegree.console.WorkspaceBean;
import org.deegree.db.ConnectionProvider;
import org.deegree.workspace.ResourceLocation;

@ManagedBean
@SessionScoped
public class JdbcBean {

    private String dbType = "mssql";

    private String dbPort = "1433";

    private String dbHost;

    private String dbName;

    private String dbConn;

    private String dbUser;

    private String dbPwd;

    private Config config;

    public String getDbType() {
        return dbType;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbConn() {
        return dbConn;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbType( String dbType ) {
        this.dbType = dbType;
        if ( dbType.equals( "mssql" ) ) {
            dbPort = "1433";
            dbConn = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";databaseName=" + dbName;
            return;
        }
        if ( dbType.equals( "oracle" ) ) {
            dbPort = "1521";
            dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
            return;
        }
        if ( dbType.equals( "postgis" ) ) {
            dbPort = "5432";
            dbConn = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            return;
        }
    }

    public void setDbPort( String dbPort ) {
        this.dbPort = dbPort;
        update();
    }

    public void setDbHost( String dbHost ) {
        this.dbHost = dbHost;
        update();
    }

    public void setDbName( String dbName ) {
        this.dbName = dbName;
        update();
    }

    public void setDbUser( String dbUser ) {
        this.dbUser = dbUser;
        update();
    }

    public void setDbPwd( String dbPwd ) {
        this.dbPwd = dbPwd;
        update();
    }

    public void setDbConn( String dbConn ) {
        this.dbConn = dbConn;
        update();
    }

    public void update() {
        if ( dbType.equals( "mssql" ) ) {
            if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "1433";
            }
            dbConn = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";databaseName=" + dbName;
            return;
        }
        if ( dbType.equals( "oracle" ) ) {
            if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "1521";
            }
            dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
            return;
        }
        if ( dbType.equals( "postgis" ) ) {
            if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "5432";
            }
            dbConn = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            return;
        }
    }

    public String editAsXml()
                            throws IOException {
        create();
        if ( config != null ) {
            return config.edit();
        }
        return null;
    }

    public void testConnection() {

        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sMap = ctx.getSessionMap();
        String newId = (String) sMap.get( "newConfigId" );

        Connection conn = null;
        try {
            conn = DriverManager.getConnection( dbConn, dbUser, dbPwd );
            FacesMessage fm = new FacesMessage( SEVERITY_INFO, "Connection '" + newId + "' ok", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } catch ( SQLException e ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Connection '" + newId + "' unavailable: "
                                                                + e.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    // nothing to do
                }
            }
        }
    }

    public void cancel() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        // JDBCParamsManager mgr = ws.getSubsystemManager( JDBCParamsManager.class );
        Map<String, Object> sMap = ctx.getSessionMap();
        String newId = (String) sMap.get( "newConfigId" );
        // mgr.deleteResource( newId );
        clearFields();
    }

    private void clearFields() {
        dbType = "mssql";
        dbPort = "1433";
        dbHost = null;
        dbName = null;
        dbConn = null;
        dbUser = null;
        dbPwd = null;
    }

    public String save() {
        create();
        clearFields();
        return "/console/jdbc/index";
    }

    private void create() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        // JDBCParamsManager mgr = ws.getSubsystemManager( JDBCParamsManager.class );
        // StringBuffer sb = new StringBuffer();
        // sb.append( "<?xml version='1.0' encoding='UTF-8'?>\n" );
        // sb.append(
        // "<JDBCConnection configVersion='3.0.0'  xmlns='http://www.deegree.org/jdbc' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.deegree.org/jdbc http://schemas.deegree.org/jdbc/3.0.0/jdbc.xsd'>\n"
        // );
        // sb.append( "  <Url>" + dbConn + "</Url>\n" );
        // sb.append( "  <User>" + dbUser + "</User>\n" );
        // sb.append( "  <Password>" + dbPwd + "</Password>\n" );
        // sb.append( "  <ReadOnly>false</ReadOnly>\n" );
        // sb.append( "</JDBCConnection>\n" );
        ResourceState rs = null;
        InputStream is = null;
        try {
            // is = new ByteArrayInputStream( sb.toString().getBytes( "UTF-8" ) );
            Map<String, Object> sMap = ctx.getSessionMap();
            String newId = (String) sMap.get( "newConfigId" );

            ResourceLocation<ConnectionProvider> loc = getSyntheticProvider( newId, dbConn, dbUser, dbPwd );
            ws.getNewWorkspace().add( loc );
            ws.getNewWorkspace().init( loc.getIdentifier(), null );

            // rs = mgr.createResource( newId, is );
            // rs = mgr.activate( rs.getId() );
            ResourceManagerMetadata2 rsMetadata = (ResourceManagerMetadata2) sMap.get( "resourceManagerMetadata" );
            // this.config = new Config( rs, (ConfigManager) sMap.get( "configManager" ), mgr,
            // rsMetadata.getStartView(),
            // true );
            // ConnectionManager poolMgr = ws.getSubsystemManager( ConnectionManager.class );
            // poolMgr.shutdown();
            // poolMgr.startup( ws );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }
}
