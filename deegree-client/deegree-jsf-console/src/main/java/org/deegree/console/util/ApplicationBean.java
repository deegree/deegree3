/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.console.util;

import static org.deegree.services.controller.OGCFrontController.getModulesInfo;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.deegree.commons.modules.ModuleInfo;
import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.deegree.console.workspace.WorkspaceBean;
import org.slf4j.Logger;

/**
 * Encapsulates informations about the status of the deegree web services.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 29926 $, $Date: 2011-03-08 11:47:59 +0100 (Di, 08. Mär 2011) $
 */
@ManagedBean
@RequestScoped
public class ApplicationBean implements Serializable {

    private static final long serialVersionUID = 147824864885285227L;

    static final Logger LOG = getLogger( ApplicationBean.class );

    private String logo = DeegreeAALogoUtils.getAsString();

    private String baseVersion;

    private List<String> internalModules = new ArrayList<String>();

    public ApplicationBean() {
        for ( ModuleInfo info : getModulesInfo() ) {
            if ( baseVersion == null ) {
                baseVersion = info.getVersion();
            }
            internalModules.add( info.toString() );
        }
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public String getLogo() {
        return logo;
    }

    public List<String> getInternalModules() {
        return internalModules;
    }

    public List<String> getWorkspaceModules() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        WorkspaceBean wsBean = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) );
        if ( wsBean == null ) {
            return Collections.emptyList();
        }

        List<String> wsModules = new ArrayList<String>();
        try {
            for ( ModuleInfo info : wsBean.getActiveWorkspace().getModulesInfo() ) {
                wsModules.add( info.toString() );
            }
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return wsModules;
    }
}
