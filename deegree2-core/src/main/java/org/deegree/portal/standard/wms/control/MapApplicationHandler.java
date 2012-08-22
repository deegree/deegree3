//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.portal.standard.wms.control;

import org.deegree.enterprise.control.ApplicationHandler;
import org.deegree.portal.context.ViewContext;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mays$
 *
 * @version $Revision$, $Date: 27.05.2008 17:15:08$
 */
public class MapApplicationHandler extends ApplicationHandler {
    private ViewContext defaultContext = null;

    /**
     * Creates a new MapApplicationHandler object.
     *
     * @param controllerFile
     * @param vc
     *
     * @throws Exception
     */
    public MapApplicationHandler( String controllerFile, ViewContext vc ) throws Exception {
        super( controllerFile );
        defaultContext = vc;
    }

    /**
     *
     * @return returns the default configuration of the WMS client
     */
    public ViewContext getDefaultWebMapContext() {
        return defaultContext;
    }

    /**
     * sets the default configuration for the map cliet
     *
     * @param defaultContext
     */
    public void setDefaultWebMapContext( ViewContext defaultContext ) {
        this.defaultContext = defaultContext;
    }
}
