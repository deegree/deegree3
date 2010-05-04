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
package org.deegree.tools.crs.georeferencing.model;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point2d;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wms.client.WMSClient111;

/**
 * 
 * Generates a 2D BufferedImage from a WMS request.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DImplWMS implements Scene2D {

    private WMSClient111 wmsClient;

    private CRS srs;

    private List<String> lays;

    private List<String> formatList;

    private Envelope imageBoundingbox;

    private Point2d onePixel;

    private GeometryFactory geometryFactory;

    private BufferedImage requestedImage;

    private URL imageRequestUrl;

    /**
     * The GetMap()-request to a WMSClient.
     * 
     * @param panelWidth
     * @param panelHeight
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     * @return
     */
    private BufferedImage generateMap( double panelWidth, double panelHeight, double minX, double maxX, double minY,
                                       double maxY ) {
        BufferedImage image = null;

        try {
            image = wmsClient.getMap( lays, (int) panelWidth, (int) panelHeight, imageBoundingbox, srs,
                                      formatList.get( 0 ), true, false, 20, false, null ).first;
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * Determines the ratio the boundingbox has to orient on. If there is a mismatch between the width and height this
     * should influence the display of the image returned by the WMS to prevent any deformation. <li>pos - orientation
     * on width because width is larger</li> <li>neg - orientation on hight because hight is larger</li> <li>other -
     * orientation on width/hight because they are even</li>
     * 
     * @param panelBounds
     *            the rectangle bounds, not <Code>null</Code>
     * @return an positive, negative or even integer
     */
    private int determineProportion( Rectangle panelBounds ) {
        double w = panelBounds.getWidth();
        double h = panelBounds.getHeight();

        double ratio = w / h;

        if ( ratio < 1 ) {
            // if < 1 then do orientation on h
            return -1;
        } else if ( ratio > 1 ) {
            // if > 1 then do orientation on w
            return 1;
        }
        // if w = h then return 0
        return 0;
    }

    @Override
    public BufferedImage generateImage( Rectangle sceneBounds ) {

        int proportion = determineProportion( sceneBounds );
        geometryFactory = new GeometryFactory();

        double panelWidth = sceneBounds.getWidth();
        double panelHeight = sceneBounds.getHeight();

        wmsClient = new WMSClient111( imageRequestUrl );
        lays = Collections.singletonList( "dem" );
        srs = new CRS( "EPSG:32618" );
        if ( imageBoundingbox == null ) {
            imageBoundingbox = wmsClient.getBoundingBox( srs.getName(), lays );
        }
        double minX = imageBoundingbox.getMin().get0();
        double maxX = imageBoundingbox.getMax().get0();
        double minY = imageBoundingbox.getMin().get1();
        double maxY = imageBoundingbox.getMax().get1();

        if ( proportion == 0 ) {
            // do nothing

        } else if ( proportion < 0 ) {

            double newWidth = ( panelWidth / panelHeight ) * ( maxY - minY );

            imageBoundingbox = geometryFactory.createEnvelope( minX, minY, ( minX + newWidth ), maxY, srs );

        } else {
            double newHeight = ( panelHeight / panelWidth ) * ( maxX - minX );

            imageBoundingbox = geometryFactory.createEnvelope( minX, minY, minX, ( minX + newHeight ), srs );

        }
        onePixel = normalizeImageBoundingbox( sceneBounds, imageBoundingbox );
        formatList = Collections.singletonList( "image/jpeg" );

        requestedImage = generateMap( panelWidth, panelHeight, minX, maxX, minY, maxY );
        return requestedImage;

    }

    /**
     * Based on bounds from an upper component this method normalizes the bounds of the upper component regarding to an
     * envelope to one pixel.
     * 
     * @param panelBounds
     *            the rectangle bounds, not <Code>null</Code>
     * @param bbox
     *            the boundingbox, not <Code>null</Code>
     * @return a point, not <Code>null</Code>
     */
    private Point2d normalizeImageBoundingbox( Rectangle panelBounds, Envelope bbox ) {

        double w = panelBounds.getWidth();
        double h = panelBounds.getHeight();
        double envelopeMeasureX = bbox.getMax().get0() - bbox.getMin().get0();
        double envelopeMeasureY = bbox.getMax().get1() - bbox.getMin().get1();
        double oneX = envelopeMeasureX / w;
        double oneY = envelopeMeasureY / h;

        return new Point2d( oneX, oneY );
    }

    @Override
    public void setImageUrl( URL imageRequestUrl ) {
        this.imageRequestUrl = imageRequestUrl;
    }

    @Override
    public void setImageBoundingbox( Point2d change ) {
        double envStartPosX = imageBoundingbox.getMin().get0() - change.getX() * onePixel.getX();
        double envStartPosY = imageBoundingbox.getMin().get1() + change.getY() * onePixel.getY();

        double envEndPosX = imageBoundingbox.getMax().get0() - change.getX() * onePixel.getX();
        double envEndPosY = imageBoundingbox.getMax().get1() + change.getY() * onePixel.getY();

        System.out.println( "  start: " + imageBoundingbox.getMin().get0() + ", " + imageBoundingbox.getMin().get1()
                            + " end: " + imageBoundingbox.getMax().get0() + ", " + imageBoundingbox.getMax().get1() );
        System.out.println( "  start: " + envStartPosX + ", " + envStartPosY + " end: " + envEndPosX + ", "
                            + envEndPosY );

        imageBoundingbox = geometryFactory.createEnvelope( envStartPosX, envStartPosY, envEndPosX, envEndPosY, srs );

    }

}
