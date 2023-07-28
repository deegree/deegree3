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
package org.deegree.client.core.utils;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLExecution implements Serializable {

	private static final long serialVersionUID = -5784976166723417648L;

	private static Logger LOG = LoggerFactory.getLogger(SQLExecution.class);

	private String connId;

	private String[] sqlStatements;

	private String message = "Click Execute to create tables.";

	private String backOutcome;

	private Workspace workspace;

	public SQLExecution(String connId, String[] sqlStatements, String backOutcome, Workspace workspace) {
		this.connId = connId;
		this.sqlStatements = sqlStatements;
		this.backOutcome = backOutcome;
		this.workspace = workspace;
	}

	public String getMessage() {
		return message;
	}

	public String getStatements() {
		StringBuffer sql = new StringBuffer();
		for (int i = 0; i < sqlStatements.length; i++) {
			sql.append(sqlStatements[i]);
			if (!sqlStatements[i].trim().isEmpty()) {
				sql.append(";");
			}
			sql.append("\n");
		}
		return sql.toString();
	}

	public void setStatements(String sql) {
		sqlStatements = sql.split(";\\s*\\n");
		for (int i = 0; i < sqlStatements.length; ++i) {
			if (sqlStatements[i].endsWith("end")) {
				sqlStatements[i] = sqlStatements[i] + ";";
			}
		}
	}

	public String execute() {
		Connection conn = null;
		Statement stmt = null;
		try {
			ConnectionProvider prov = workspace.getResource(ConnectionProviderProvider.class, connId);
			conn = prov.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			for (String sql : sqlStatements) {
				LOG.debug("Executing: {}", sql);
				stmt.execute(sql);
			}
			conn.commit();
			FacesMessage fm = new FacesMessage(SEVERITY_INFO,
					"Executed " + sqlStatements.length + " statements successfully.", null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		catch (Throwable t) {
			if (conn != null) {
				try {
					conn.rollback();
				}
				catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			JDBCUtils.close(null, stmt, conn, LOG);
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "Error: " + t.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
			return null;
		}
		return backOutcome;
	}

	public String getBackOutcome() {
		return backOutcome;
	}

}