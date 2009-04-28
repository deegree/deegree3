//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering.r3d.opengl.rendering.dem;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.BandType;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.rendering.r3d.opengl.rendering.dem.TextureTile;

import com.sun.opengl.util.texture.Texture;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WMSTextureTileProvider implements TextureTileProvider {

    private static final String SRS_NAME = "EPSG:31466";

    private static final String FORMAT = "image/png";

    private static GeometryFactory fac = GeometryFactoryCreator.getInstance().getGeometryFactory();

    private static Texture tex;

    private WMSClient111 client;

    private List<String> layers;

    private CRS srs;

    private TextureTile tile;

    private double res;    
    
    public WMSTextureTileProvider(String capabilitiesURL, String[] requestedLayers, double res) throws MalformedURLException {
        client = new WMSClient111( new URL( capabilitiesURL ) );
        layers = Arrays.asList( requestedLayers );
        srs = new CRS( SRS_NAME );
        this.res = res;
    }

    @Override
    public TextureTile getTextureTile( float minX, float minY, float maxX, float maxY) {

        int width = (int) ( ( maxX - minX ) / res );
        int height = (int) ( ( maxY - minY ) / res );

        System.out.println( "width: " + width );

        Envelope bbox = fac.createEnvelope( minX, minY, maxX, maxY, srs );
        Pair<SimpleRaster, String> wmsResponse;
        try {
            wmsResponse = client.getMapAsSimpleRaster( layers, width, height, bbox, srs, FORMAT,
                                                                                  true, false, new ArrayList<String>() );
        } catch ( IOException e ) {
            throw new RuntimeException( e.getMessage());
        }
        if ( wmsResponse.second != null ) {
            throw new RuntimeException( wmsResponse.second );
        }
        PixelInterleavedRasterData rasterData = (PixelInterleavedRasterData) wmsResponse.first.getRasterData();
        for (BandType type : rasterData.getBandTypes()) {
            System.out.println (type);
        }
        return new TextureTile( minX, minY, maxX, maxY, rasterData.getWidth(), rasterData.getHeight(), rasterData.getByteBuffer(), true );
    }

    public BufferedImage getTileImage( float minX, float minY, float maxX, float maxY, int width, int height )
                            throws IOException {

        Envelope bbox = fac.createEnvelope( minX, minY, maxX, maxY, srs );

        Pair<BufferedImage, String> mapResponse = client.getMap( layers, width, height, bbox, srs, FORMAT, false,
                                                                 false, new ArrayList<String>() );
        return ( mapResponse.first );
    }

    @Override
    public double getNativeResolution() {
        return res;
    }
}
