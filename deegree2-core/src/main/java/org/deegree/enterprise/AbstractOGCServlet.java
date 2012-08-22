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
package org.deegree.enterprise;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Abstract servlet that serves as an OCC-compliant HTTP-frontend to any OGC-WebService (WFS, WMS,
 * ...).
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$ $Date$
 *
 * @todo refactoring required, move to package servlet
 */
public abstract class AbstractOGCServlet extends HttpServlet {

    private static final long serialVersionUID = 2874150881447533442L;

    /**
     * Called by the servlet container to indicate that the servlet is being placed into service.
     * Sets the debug level according to the debug parameter defined in the ServletEngine's
     * environment.
     * <p>
     * <p>
     *
     * @param servletConfig
     *            servlet configuration
     * @throws ServletException
     *             exception if something occurred that interferes with the servlet's normal
     *             operation
     */
    @Override
    public void init( ServletConfig servletConfig )
                            throws ServletException {
        super.init( servletConfig );
    }

}
