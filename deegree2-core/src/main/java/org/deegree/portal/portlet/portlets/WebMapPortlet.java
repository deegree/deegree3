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
package org.deegree.portal.portlet.portlets;

import org.apache.jetspeed.portal.PortletConfig;
import org.apache.jetspeed.portal.PortletException;
import org.apache.jetspeed.portal.portlets.JspPortlet;
import org.deegree.framework.util.StringTools;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WebMapPortlet extends JspPortlet {

    private static final long serialVersionUID = -6786461476321256002L;

    static String INIT_HOMEBBOX = "homeBoundingBox";

    private Envelope home = null;

    /**
     * loads the ViewContext assigend to a portlet instance from the resource defined in the portles configuration
     *
     * @throws PortletException
     */
    @Override
    public void init()
                            throws PortletException {
        super.init();

        PortletConfig pc = getPortletConfig();

        // get HOME boundingbox
        String tmp = pc.getInitParameter( INIT_HOMEBBOX );
        if ( tmp == null ) {

        } else {
            double[] coords = StringTools.toArrayDouble( tmp, "," );
            home = GeometryFactory.createEnvelope( coords[0], coords[1], coords[2], coords[3], null );
        }

    }

    /**
     * returns the home boundingbox of the context assigned to this portlet
     *
     * @return the home boundingbox of the context assigned to this portlet
     */
    public Envelope getHome() {
        return home;
    }

    /**
     * sets the home boundingbox of the context assigned to this portlet
     *
     * @param home
     */
    public void setHome( Envelope home ) {
        this.home = home;
    }

}
