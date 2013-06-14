/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.console.connection.sql;

import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.console.AbstractCreateResourceBean;
import org.deegree.db.ConnectionProviderManager;

/**
 * JSF backing bean for "Create new database connection" view.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
@ManagedBean
@RequestScoped
public class CreateSqlConnectionBean extends AbstractCreateResourceBean {

    public CreateSqlConnectionBean() {
        super( ConnectionProviderManager.class );
    }

    @Override
    protected String getOutcome() {
        return "/console/connection/sql/index";
    }

    @Override
    public String create() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sMap = ctx.getSessionMap();
        sMap.put( "newConfigId", getId() );
        return "/console/connection/sql/jdbcparams";
    }
}
