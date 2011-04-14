package org.deegree.console.jdbc;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lombok.Getter;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.console.Config;
import org.deegree.console.ConfigManager;
import org.deegree.console.ResourceManagerMetadata2;
import org.deegree.console.WorkspaceBean;

@ManagedBean
@SessionScoped
public class JdbcBean {

    @Getter
    private String dbType = "mssql";

    @Getter
    private String dbPort;

    @Getter
    private String dbHost;

    @Getter
    private String dbName;

    @Getter
    private String dbConn;

    @Getter
    private String dbUser;

    @Getter
    private String dbPwd = null;

    private Config config;

    public void setDbType( String dbType ) {
        this.dbType = dbType;
        update();
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
        create();
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        Map<String, Object> sMap = ctx.getSessionMap();
        String newId = (String) sMap.get( "newConfigId" );
        try {
            mgr.get( newId ).close();
            FacesMessage fm = new FacesMessage( SEVERITY_INFO, "Connection '" + newId + "' ok", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Connection '" + newId + "' unavailable: "
                                                                + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
        mgr.deleteResource( newId );
    }

    public void cancel() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        Map<String, Object> sMap = ctx.getSessionMap();
        String newId = (String) sMap.get( "newConfigId" );
        mgr.deleteResource( newId );
        clearFields();
    }

    private void clearFields() {
        dbType = "mssql";
        dbPort = null;
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
        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        StringBuffer sb = new StringBuffer();
        sb.append( "<?xml version='1.0' encoding='UTF-8'?>" );
        sb.append( "<JDBCConnection configVersion='3.0.0'  xmlns='http://www.deegree.org/jdbc' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.deegree.org/jdbc http://schemas.deegree.org/jdbc/3.0.0/jdbc.xsd'>" );
        sb.append( "<Url>" + dbConn + "</Url>" );
        sb.append( "<User>" + dbUser + "</User>" );
        sb.append( "<Password>" + dbPwd + "</Password>" );
        sb.append( "<ReadOnly>false</ReadOnly>" );
        sb.append( "</JDBCConnection>" );
        ResourceState rs = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream( sb.toString().getBytes( "UTF-8" ) );
            Map<String, Object> sMap = ctx.getSessionMap();
            String newId = (String) sMap.get( "newConfigId" );
            rs = mgr.createResource( newId, is );
            rs = mgr.activate( rs.getId() );
            ResourceManagerMetadata2 rsMetadata = (ResourceManagerMetadata2) sMap.get( "resourceManagerMetadata" );
            this.config = new Config( rs, (ConfigManager) sMap.get( "configManager" ), mgr, rsMetadata.getStartView(),
                                      true );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }
}