//$Header: $
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

package org.deegree.portal.portlet.modules.wfs.actions.portlets;

import org.apache.jetspeed.portal.Portlet;
import org.apache.turbine.util.RunData;

/**
 * Removes a number of Features (listed by their ID). The actual implementation is done by the Perform class.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author: mays$
 *
 * @version $Revision$, $Date: 03.06.2008 20:18:12$
 */
public class RemoveAnnotationAction extends WFSClientPortletAction {

    /**
     * Builds the portlet up.
     *
     * @param portlet
     * @param data
     * @throws Exception
     */
    @Override
    protected void buildNormalContext( Portlet portlet, RunData data )
                            throws Exception {
        RemoveAnnotationPerform arp = new RemoveAnnotationPerform( data.getRequest(), portlet, data.getServletContext() );
        arp.buildNormalContext( data );
    }

    /**
     *
     * Delegates to RemoveAnnotationPerform, which does the deleting.
     *
     * @param data
     * @param portlet
     * @throws Exception
     */
    public void doDeletetransaction( RunData data, Portlet portlet )
                            throws Exception {
        try {
            RemoveAnnotationPerform arp = new RemoveAnnotationPerform( data.getRequest(), portlet,
                                                                       data.getServletContext() );
            arp.doDeletetransaction( data );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw e;
        }
    }

}
