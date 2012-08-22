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
package org.deegree.portal.portlet.modules.wfs.actions.portlets;

import org.apache.jetspeed.portal.Portlet;
import org.apache.turbine.util.RunData;

/**
 * Portlet for managing localized annotation. The assigned
 *
 * @see org.deegree.portal.portlet.modules.wfs.actions.portlets.AnnotationPortletPerform extends
 * @see org.deegree.portal.portlet.modules.wfs.actions.portlets.WFSClientPortletPerform by adding capabilities to
 *      perform a transaction directly against a database
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class AnnotationPortletAction extends WFSClientPortletAction {

    /**
     * @param portlet
     * @param data
     * @throws Exception
     */
    @Override
    protected void buildNormalContext( Portlet portlet, RunData data )
                            throws Exception {
        AnnotationPortletPerform wfspp = new AnnotationPortletPerform( data.getRequest(), portlet,
                                                                       data.getServletContext() );
        wfspp.buildNormalContext();
    }

    /**
     *
     * @param data
     * @param portlet
     * @throws Exception
     */
    @Override
    public void doTransaction( RunData data, Portlet portlet )
                            throws Exception {

        try {
            AnnotationPortletPerform wfspp = new AnnotationPortletPerform( data.getRequest(), portlet,
                                                                           data.getServletContext() );
            wfspp.doTransaction( data.getUser().getUserName() );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw e;
        }
    }
}
