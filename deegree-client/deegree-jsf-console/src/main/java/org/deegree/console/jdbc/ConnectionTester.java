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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.console.jdbc;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.deegree.client.core.utils.ActionParams.getParam1;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.console.WorkspaceBean;

/**
 * JSF Bean for testing the availability of connections offered by {@link ConnectionManager}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class ConnectionTester {

    private DeegreeWorkspace getWorkspace() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        return ws;
    }

    public void test() {
        String id = (String) getParam1();
        try {
            ConnectionManager mgr = getWorkspace().getSubsystemManager( ConnectionManager.class );
            mgr.get( id ).close();
            FacesMessage fm = new FacesMessage( SEVERITY_INFO, "Connection '" + id + "' ok", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Connection '" + id + "' unavailable: "
                                                                + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }

    public String testAndSave() {
        String id = (String) getParam1();
        try {
            ConnectionManager mgr = getWorkspace().getSubsystemManager( ConnectionManager.class );
            mgr.get( id ).close();
            FacesMessage fm = new FacesMessage( SEVERITY_INFO, "Connection '" + id + "' ok", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } catch ( Throwable t ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Connection '" + id + "' unavailable: "
                                                                + t.getMessage(), null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
        return "/console/jdbc/index.xhtml";
    }
}
