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
package org.deegree.portal.standard.wms.control.layertree;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.portal.Constants;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.MMLayer;
import org.deegree.portal.context.MapModel;
import org.deegree.portal.context.ViewContext;
import org.stringtree.json.JSONReader;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetLayersInformationListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GetLayersInformationListener.class );

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        Map<Object, Object> parameter = event.getParameter();
        JSONReader reader = new JSONReader();        
        List<String> layerIDs = (List) reader.read( (String)parameter.get( "layers" ) );
        ViewContext vc = (ViewContext) event.getSession().getAttribute( Constants.CURRENTMAPCONTEXT );
        MapModel mapModel = vc.getGeneral().getExtension().getMapModel();
        LayerBean[] layerBeans = new LayerBean[layerIDs.size()];
        int i = 0;
        try {
            for ( String id : layerIDs ) {
                Layer layer = ( (MMLayer) mapModel.getMapModelEntryByIdentifier( id ) ).getLayer();
                layerBeans[i++] = new LayerBean( layer.getServer().getTitle(), layer.getName(),
                                                 layer.getServer().getService() + " " + layer.getServer().getVersion(),
                                                 layer.getServer().getOnlineResource().toURI().toASCIIString(),
                                                 layer.getFormatList().getCurrentFormat().getName() );

            }
        } catch ( Exception e ) {
            LOG.logError( e );
            responseHandler.writeAndClose( "ERROR: " + e.getMessage() );
            return;
        }
        responseHandler.writeAndClose( false, layerBeans );

    }

}
