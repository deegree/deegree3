package org.deegree.console.connection.sql;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.deegree.db.ConnectionProviderUtils.getSyntheticProvider;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.console.Config;
import org.deegree.console.workspace.WorkspaceBean;
import org.deegree.db.ConnectionProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;

@ManagedBean
@ViewScoped
public class JdbcBean implements Serializable {

	private static final long serialVersionUID = -425251614342669735L;

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

	public void setDbType(String dbType) {
		this.dbType = dbType;
		if (dbType.equals("mssql")) {
			dbPort = "1433";
			dbConn = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";databaseName=" + dbName;
			return;
		}
		if (dbType.equals("oracle")) {
			dbPort = "1521";
			dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
			return;
		}
		if (dbType.equals("postgis")) {
			dbPort = "5432";
			dbConn = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
			return;
		}
	}

	public void setDbPort(String dbPort) {
		this.dbPort = dbPort;
		update();
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
		update();
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
		update();
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
		update();
	}

	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
		update();
	}

	public void setDbConn(String dbConn) {
		this.dbConn = dbConn;
		update();
	}

	public void update() {
		if (dbType.equals("mssql")) {
			if (dbPort == null || dbPort.isEmpty()) {
				dbPort = "1433";
			}
			dbConn = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";databaseName=" + dbName;
			return;
		}
		if (dbType.equals("oracle")) {
			if (dbPort == null || dbPort.isEmpty()) {
				dbPort = "1521";
			}
			dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
			return;
		}
		if (dbType.equals("postgis")) {
			if (dbPort == null || dbPort.isEmpty()) {
				dbPort = "5432";
			}
			dbConn = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
			return;
		}
	}

	public String editAsXml() throws IOException {
		create();
		if (config != null) {
			return config.edit();
		}
		return null;
	}

	public void testConnection() {

		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sMap = ctx.getSessionMap();
		String newId = (String) sMap.get("newConfigId");

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dbConn, dbUser, dbPwd);
			FacesMessage fm = new FacesMessage(SEVERITY_INFO, "Connection '" + newId + "' ok", null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		catch (SQLException e) {
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR,
					"Connection '" + newId + "' unavailable: " + e.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (SQLException e) {
					// nothing to do
				}
			}
		}
	}

	public void cancel() {
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
		return "/console/connection/sql/index";
	}

	private void create() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		Workspace ws = ((WorkspaceBean) ctx.getApplicationMap().get("workspace")).getActiveWorkspace()
			.getNewWorkspace();
		try {
			Map<String, Object> sMap = ctx.getSessionMap();
			String newId = (String) sMap.get("newConfigId");

			ResourceLocation<ConnectionProvider> loc = getSyntheticProvider(newId, dbConn, dbUser, dbPwd);
			ws.add(loc);
			ws.init(loc.getIdentifier(), null);

			ws.getLocationHandler().persist(loc);
		}
		catch (Throwable t) {
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
	}

}
