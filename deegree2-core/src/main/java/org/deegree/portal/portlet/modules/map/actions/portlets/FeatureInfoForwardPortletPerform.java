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
package org.deegree.portal.portlet.modules.map.actions.portlets;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jetspeed.portal.Portlet;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.portal.PortalException;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.ViewContext;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public abstract class FeatureInfoForwardPortletPerform extends FeatureInfoPortletPerform {

    private static final ILogger LOG = LoggerFactory.getLogger( FeatureInfoForwardPortletPerform.class );

    /**
     * @param request
     * @param portlet
     * @param servletContext containing servlet information
     */
    public FeatureInfoForwardPortletPerform( HttpServletRequest request, Portlet portlet, ServletContext servletContext ) {
        super( request, portlet, servletContext );

    }

    @Override
    protected void doGetFeatureInfo()
                            throws PortalException, OGCWebServiceException {

        // a get feature info request only can be performed if a viewcontext
        // has been initialized before by the MapWindowPortletAction
        ViewContext vc = getCurrentViewContext( portlet.getID() );
        if ( vc == null ) {
            throw new PortalException( "no valid view context available through users session" );
        }
        String reqlayer = parameter.get( PARAM_FILAYERS );
        if ( reqlayer == null || reqlayer.length() == 0 ) {
            throw new PortalException( "at least one layer/featuretype/coverage must be "
                                       + "targeted by a feature info request" );
        }
        // update the view contexts bounding box with the current one. This information
        // will be evaluated when creating a GetFeatureInfo request later
        String tmp = parameter.get( PARAM_BBOX );
        if ( tmp == null || tmp.length() == 0 ) {
            throw new PortalException( "required parameter " + PARAM_BBOX + " is missing!" );
        }
        double[] coords = StringTools.toArrayDouble( tmp, "," );
        CoordinateSystem crs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem();
        Point[] pt = new Point[2];
        pt[0] = GeometryFactory.createPoint( coords[0], coords[1], crs );
        pt[1] = GeometryFactory.createPoint( coords[2], coords[3], crs );
        try {
            vc.getGeneral().setBoundingBox( pt );
        } catch ( ContextException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( "could not update viewcontexts bbox", e );
        }

        List<Layer> layerList = new ArrayList<Layer>( 50 );
        layerList.add( vc.getLayerList().getLayer( reqlayer, null ) );
        tmp = perform( layerList, vc ).trim();
        String[] ids = StringTools.toArray( tmp, ";", true );

        List<?> list = performQuery( ids );

        request.setAttribute( "RESULT", list );

    }

    /**
     * the IDs of the features targeted by a GetFeatureInfo request will be passed. A concrete
     * implementation of this method may use them to perform a database query, a GetFeature request
     * or something else.
     *
     * @param ids
     * @return the response of the query
     */
    protected abstract List<?> performQuery( String[] ids );

}
