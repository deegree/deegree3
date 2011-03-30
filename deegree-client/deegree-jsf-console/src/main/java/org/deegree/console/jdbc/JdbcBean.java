package org.deegree.console.jdbc;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import lombok.Getter;
import lombok.Setter;

@ManagedBean
@RequestScoped
public class JdbcBean {

    @Getter
    private String dbType;

    @Getter
    private String dbPort;

    @Getter
    private String dbHost;

    @Getter
    private String dbName;

    @Getter
    @Setter
    private String dbConn;

    @Getter
    @Setter
    private String dbUser;

    @Getter
    @Setter
    private String dbPwd;

    public void update() {
        if ( dbType == null ) {
            dbConn = "";
            return;
        }
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
            dbConn = "jdbc:oracle:thin:@" + dbHost + ":"+ dbPort +":" + dbName;
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

    public void setDbType( String dbType ) {
        this.dbType = dbType;
        update();
    }

}
