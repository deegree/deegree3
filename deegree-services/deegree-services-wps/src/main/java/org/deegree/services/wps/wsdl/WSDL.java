//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.services.wps.wsdl;

import java.io.File;

import javax.ws.rs.core.UriBuilder;

import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WSDL {

    private final File wsdlFile;

    public WSDL( String wsPath ) {
        DefaultWorkspace ws = (DefaultWorkspace) OGCFrontController.getServiceWorkspace().getNewWorkspace();
        wsdlFile = new File( ws.getLocation(), wsPath );
    }

    public File getFile() {
        return wsdlFile;
    }

    public boolean exists() {
        return wsdlFile.exists();
    }

    public String getRestURL() {
        return OGCFrontController.getContext().getResourcesUrl()
               + "../rest"
               + UriBuilder.fromResource( WSDLResource.class ).path( WSDLResource.class, "get" ).build( "ALL" ).toString();
    }
}
