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

package org.deegree.services.wms.model.layers;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.RemoteOWSStore;
import org.deegree.remoteows.wms.RemoteWMSStore;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.slf4j.Logger;

/**
 * <code>RemoteWMSLayer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(info = "logs in case of an IO error when sending response", warn = "logs problems with CRS", debug = "logs information about interaction with the remote WMS, errors from remote WMS etc.", trace = "logs details of interaction with remote WMS, stack traces")
public class RemoteWMSLayer extends Layer {

    private static final Logger LOG = getLogger( RemoteWMSLayer.class );

    private boolean available = true;

    private RemoteWMSStore wmsStore;

    /**
     * Construct one with RemoteWMSStoreId in jaxb bean.
     * 
     * @param service
     * @param layer
     *            must have a remotewmsstoreid set and resolving to a remote wms store.
     * @param parent
     */
    public RemoteWMSLayer( MapService service, AbstractLayerType layer, Layer parent ) {
        super( service, layer, parent );

        RemoteOWSManager manager = OGCFrontController.getServiceWorkspace().getSubsystemManager( RemoteOWSManager.class );
        RemoteOWSStore store = manager.get( layer.getRemoteWMSStoreId() );

        if ( !( store instanceof RemoteWMSStore ) ) {
            available = false;
            LOG.warn( "Layer configuration for {} did not resolve to a remote WMS store.",
                      layer.getName() == null ? layer.getTitle() : layer.getName() );
            return;
        }

        wmsStore = (RemoteWMSStore) store;
        setBbox( wmsStore.getEnvelope() );
    }

    /**
     * @param service
     * @param store
     * @param name
     * @param title
     * @param parent
     */
    public RemoteWMSLayer( MapService service, RemoteWMSStore store, String name, String title, Layer parent ) {
        super( service, name, title, parent );
        wmsStore = store;
        setBbox( wmsStore.getEnvelope() );
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public LinkedList<String> paintMap( Graphics2D g, GetMap gm, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        List<BufferedImage> images = wmsStore.getMap( gm.getBoundingBox(), gm.getWidth(), gm.getHeight(),
                                                      gm.getParameterMap() );
        if ( images == null ) {
            return new LinkedList<String>();
        }
        for ( BufferedImage img : images ) {
            g.drawImage( img, 0, 0, null );
        }
        return new LinkedList<String>();
    }

    @Override
    public Pair<BufferedImage, LinkedList<String>> paintMap( GetMap gm, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        if ( wmsStore.isSimple() ) {
            List<BufferedImage> images = wmsStore.getMap( gm.getBoundingBox(), gm.getWidth(), gm.getHeight(),
                                                          gm.getParameterMap() );
            return new Pair<BufferedImage, LinkedList<String>>( images.get( 0 ), new LinkedList<String>() );
        }

        // this assumes that super.paintMap will call paintMap(Graphics2D...)
        // while not beautiful, this avoids copying the image once more in case the wmsStore is 'simple'
        return super.paintMap( gm, style );
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi, Style style ) {
        FeatureCollection col = wmsStore.getFeatureInfo( fi.getEnvelope(), fi.getWidth(), fi.getHeight(), fi.getX(),
                                                         fi.getY(), fi.getFeatureCount(), fi.getParameterMap() );
        return new Pair<FeatureCollection, LinkedList<String>>( col, new LinkedList<String>() );
    }

    @Override
    public FeatureType getFeatureType() {
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

}
