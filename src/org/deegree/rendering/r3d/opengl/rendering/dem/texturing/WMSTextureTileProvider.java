//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wms.client.WMSClient111;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TextureTileProvider} that delegates tile requests to a WMS.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class WMSTextureTileProvider implements TextureTileProvider {

    private static final Logger LOG = LoggerFactory.getLogger( WMSTextureTileProvider.class );

    private static GeometryFactory fac = new GeometryFactory();

    private final WMSClient111 client;

    private final List<String> layers;

    private final String requestedFormat;

    private final CRS requestedCRS;

    private final double res;

    private final int requestTimeout;

    /**
     * Creates a new {@link WMSTextureTileProvider} instance.
     *
     * @param capabilitiesURL
     *            URL of the capabilities document (usually a GetCapabilities request)
     * @param requestedLayers
     *            name of the requested layers
     * @param requestCRS
     *            crs for the GetMap requests
     * @param requestFormat
     *            image format, e.g. 'image/png'
     * @param transparent
     *            true, if the image should be requested using "transparent=true", false otherwise
     * @param res
     *            resolution (world units per pixel)
     * @param maxWidth
     *            maximum map width (in pixels) that the WMS allows or -1 if unconstrained
     * @param maxHeight
     *            maximum map height (in pixels) that the WMS allows or -1 if unconstrained
     * @param requestTimeout
     *            maximum number of seconds to wait for a WMS response or -1 if unconstrained
     */
    public WMSTextureTileProvider( URL capabilitiesURL, String[] requestedLayers, CRS requestCRS, String requestFormat,
                                   boolean transparent, double res, int maxWidth, int maxHeight, int requestTimeout ) {
        this.client = new WMSClient111( capabilitiesURL );
        this.client.setMaxMapDimensions( maxWidth, maxHeight );
        this.layers = Arrays.asList( requestedLayers );
        this.requestedFormat = requestFormat;
        this.requestedCRS = requestCRS;
        this.res = res;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public TextureTile getTextureTile( float minX, float minY, float maxX, float maxY ) {

        int width = (int) ( ( maxX - minX ) / res );
        int height = (int) ( ( maxY - minY ) / res );

        LOG.debug ("Fetching texture tile (" + width + "x" + height + ") via WMSClient.");

        Envelope bbox = fac.createEnvelope( minX, minY, maxX, maxY, requestedCRS );
        SimpleRaster raster = null;
        try {

            raster = client.getMapAsSimpleRaster( layers, width, height, bbox, requestedCRS, requestedFormat, true,
                                                  true, requestTimeout, false, new ArrayList<String>() ).first;
            LOG.debug ("Success");
        } catch ( IOException e ) {
            LOG.debug ("Failed: " + e.getMessage(), e);
            // this must never happen, cause the above request uses errorsInImage=true
            throw new RuntimeException( e.getMessage() );
        }
        PixelInterleavedRasterData rasterData = (PixelInterleavedRasterData) raster.getRasterData();
        return new TextureTile( minX, minY, maxX, maxY, rasterData.getWidth(), rasterData.getHeight(),
                                rasterData.getByteBuffer(), true );
    }

    @Override
    public double getNativeResolution() {
        return res;
    }
}
