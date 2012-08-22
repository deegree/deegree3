//$HeadURL$
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
package org.deegree.portal.standard.gazetteer;

import java.io.IOException;
import java.util.Map;

import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.util.Parameter;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.Constants;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LoadBBOXListener extends AbstractGazetteerListener {

    private static final ILogger LOG = LoggerFactory.getLogger( LoadBBOXListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        Map<String, Object> param = event.getParameter();
        int index = ( (Number) param.get( "level" ) ).intValue();
        int hierarchyIndex = Integer.parseInt( (String) param.get( "hierarchyIndex" ) );
        String geographicIdentifier = (String) param.get( "geographicIdentifier" );

        ViewContext vc = (ViewContext) event.getSession().getAttribute( Constants.CURRENTMAPCONTEXT );
        Module[] modules = vc.getGeneral().getExtension().getFrontend().getModulesByName( "Gazetteer" );
        Parameter[] parameters = modules[0].getParameter().getParameters();
        // just one gazetteer module is allowed to be contained in an instance of iGeoPortal
        Hierarchy hierarchy;
        try {
            hierarchy = loadHierarchy( (String) parameters[hierarchyIndex].getValue() );
        } catch ( Exception e ) {
            LOG.logError( e );
            responseHandler.writeAndClose( "ERROR: can not load hierarchy list" );
            return;
        }
        
        HierarchyNode node = hierarchy.getRoot();
        int k = 0;
        while ( node != null && k < index ) {
            node = node.getChildNode();
            k++;
        }
        QualifiedName ft = node.getFeatureType();
        LoadBBOXCommand cmd = new LoadBBOXCommand( hierarchy.getGazetteerAddress(), ft, node.getProperties(),
                                                   geographicIdentifier );
        Envelope env = null;
        Pair<Geometry, Geometry> geometries = null;
        try {
            // first contains highlight geometry; second geographic extent
            geometries = cmd.execute();
            if ( geometries.first instanceof Point ) {
                // heuristic to ensure a useful bounding box. This may fails with compound CRS
                if ( geometries.second.getCoordinateSystem().getCRS() instanceof ProjectedCRS ) {
                    env = geometries.second.getBuffer( 50 ).getEnvelope();
                } else {
                    env = geometries.second.getBuffer( 0.001 ).getEnvelope();
                }
            } else {
                env = geometries.second.getEnvelope();
            }
        } catch ( Exception e ) {
            LOG.logError( e );
            responseHandler.writeAndClose( "ERROR: could not find/load envelope for geographicIdentifier: "
                                           + geographicIdentifier );
            return;
        }
        
        CoordinateSystem crs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem();
        if ( !crs.equals( env.getCoordinateSystem() ) ) {
            GeoTransformer gt = new GeoTransformer( crs );
            try {
                env = gt.transform( env, env.getCoordinateSystem() );
                event.getSession().setAttribute( "TEMP_WMS_GEOMETRY", gt.transform( geometries.first ) );
            } catch ( Exception e ) {
                LOG.logError( e );
                responseHandler.writeAndClose( "ERROR: can not transform geographic extent into map CRS" );
                return;
            } 
        } else {
            event.getSession().setAttribute( "TEMP_WMS_GEOMETRY", geometries.first );
        }
        responseHandler.writeAndClose( env.getMin().getX() + "," + env.getMin().getY() + "," + env.getMax().getX()+ "," + env.getMax().getY() );
    }

}
