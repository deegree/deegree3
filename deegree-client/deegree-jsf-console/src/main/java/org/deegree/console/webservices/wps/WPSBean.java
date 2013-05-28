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
package org.deegree.console.webservices.wps;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.WPService;

/**
 * JSF-Bean for the WPS main info page.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 29926 $, $Date: 2011-03-08 11:47:59 +0100 (Di, 08. Mär 2011) $
 */
@ManagedBean
@RequestScoped
public class WPSBean {

    private final String version;

    private final List<String> processIds = new ArrayList<String>();

    public String getVersion() {
        return version;
    }

    public List<String> getProcessIds() {
        return processIds;
    }

    /**
     * Creates a new {@link WPSBean} instance (only used by JSF).
     */
    public WPSBean() {
        WPService service = (WPService) ( OGCFrontController.getServiceConfiguration().getByOWSClass( WPService.class ).get( 0 ) );
        this.version = service.getOfferedVersionsString();
        for ( WPSProcess process : service.getProcessManager().getProcesses().values() ) {
            processIds.add( process.getDescription().getIdentifier().getValue().toString() );
        }
    }
}
