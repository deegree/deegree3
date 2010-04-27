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
package org.deegree.tools.crs.georeferencing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.protocol.wms.client.WMSClient111;

/**
 * 
 * Inner class to generate a 2D scene from a WMS
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class Generate2DScene extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Pair<BufferedImage, String> pair;

    private Point drawingStartPos;

    private Point imageDimension;

    private Graphics2D g2;

    private double measureX;

    private double measureY;

    private Point absoluteImageMaxPos;

    private Point absoluteImageMinPos;

    private Point absolutePosition;

    private CRS srs;

    protected Envelope bbox;

    private Point onePixel;

    private int positionX;

    private int positionY;

    private String wmsFilename;

    private Rectangle bounds;

    private int ratio;

    protected Envelope cachedEnvelope;

    /**
     * responsible to paint the image above the panelsize
     */
    private int margin;

    /**
     * Creates a <Code>Generate2DScene</Code> instance.
     * 
     * @param wmsFilename
     */
    public Generate2DScene() {

        this.drawingStartPos = new GeometryFactory().createPoint( "DrawingStartPosition", 0.0, 0.0, null );
    }

    @Override
    protected void paintComponent( Graphics g ) {

        super.paintComponent( g );

        g2 = (Graphics2D) g;
        if ( imageDimension != null ) {
            positionX = ( (int) drawingStartPos.get0() - ( margin / 2 ) );
            positionY = ( (int) drawingStartPos.get1() - ( margin / 2 ) );

            g2.drawImage( pair.first, positionX, positionY, (int) imageDimension.get0(), (int) imageDimension.get1(),
                          this );
            System.out.println( "startPos: " + positionX + ", " + positionY + "--" + drawingStartPos );
        }

    }

    /**
     * Scales the image.
     * 
     * @param xy
     *            the amount of scaling.
     */
    public void scaleImage( double xy ) {
        g2.scale( xy, xy );
        System.out.println( "scaleX: " + xy );
        this.setImageDimension( new GeometryFactory().createPoint( "NewImageDimension", this.getImageDimension().get0()
                                                                                        * xy,
                                                                   this.getImageDimension().get1() * xy, null ) );

    }

    /**
     * Here an image will be created. The Envelope can be <Code>null</Code> and the boundingbox of the initialized image
     * will be taken, otherwise the global boundingbox will be overriden by this envelope.
     * 
     * @param env
     *            the envelope, maybe <Code>null</Code>
     * @param isMarginOver
     *            declares if the bounds of the component touches the margins of the image.
     * @return a BufferedImage
     */
    public BufferedImage getImage( Envelope env, boolean isMarginOver ) {

        // ratio = determineRatio( bounds );
        WMSClient111 wmsClient;

        Pair<BufferedImage, String> pairLocal = new Pair<BufferedImage, String>();

        try {
            System.out.println( wmsFilename );
            wmsClient = new WMSClient111( new URL( wmsFilename ) );
            List<String> lays = Collections.singletonList( "dem" );
            srs = new CRS( "EPSG:32618" );
            if ( env != null ) {
                bbox = env;
            }

            if ( ratio == 0 ) {
                // do nothing

            } else if ( ratio < 0 ) {
                if ( !isMarginOver ) {
                    double newWidth = ( bounds.getWidth() / bounds.getHeight() )
                                      * ( bbox.getMax().get1() - bbox.getMin().get1() );

                    bbox = new GeometryFactory().createEnvelope( bbox.getMin().get0(), bbox.getMin().get1(),
                                                                 ( bbox.getMin().get0() + newWidth ),
                                                                 bbox.getMax().get1(), srs );
                }

            } else {
                if ( !isMarginOver ) {
                    double newHeight = ( bounds.getHeight() / bounds.getWidth() )
                                       * ( bbox.getMax().get0() - bbox.getMin().get0() );

                    bbox = new GeometryFactory().createEnvelope( bbox.getMin().get0(), bbox.getMin().get1(),
                                                                 bbox.getMax().get0(),
                                                                 ( bbox.getMin().get0() + newHeight ), srs );
                }
            }
            List<String> formatList = Collections.singletonList( "image/jpeg" );
            System.out.println( bounds.getWidth() + " = (" + bbox.getMin().get0() + ", " + bbox.getMax().get0()
                                + ") -> " + ( bbox.getMax().get0() - bbox.getMin().get0() ) );
            System.out.println( bounds.getHeight() + " = (" + bbox.getMin().get1() + ", " + bbox.getMax().get1()
                                + ") -> " + ( bbox.getMax().get1() - bbox.getMin().get1() ) );

            pairLocal = wmsClient.getMap( lays, (int) bounds.getWidth(), (int) bounds.getHeight(), bbox, srs,
                                          formatList.get( 0 ), true, false, 20, false, null );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return pairLocal.first;

    }

    /**
     * The image will be created and paint on the component without calling repaint(). Every needed attributes are
     * generated here.
     * 
     * @param env
     *            the envelope, not <Code>null</Code>
     * @param isMarginOver
     *            declares if the bounds of the component touches the margins of the image.
     */
    public void paintScreen( Envelope env, boolean isMarginOver ) {

        pair.first = getImage( env, isMarginOver );

        this.drawingStartPos = new GeometryFactory().createPoint( "NewDrawingStartPosition", 0, 0, null );

        this.imageDimension = new GeometryFactory().createPoint( "ImageDimension", bounds.getWidth() + margin,
                                                                 bounds.getHeight() + margin, null );

        this.absoluteImageMaxPos = new GeometryFactory().createPoint( "AboluteImagePositionMax", bounds.getWidth()
                                                                                                 + ( margin / 2 ),
                                                                      bounds.getHeight() + ( margin / 2 ), null );
        this.absoluteImageMinPos = new GeometryFactory().createPoint( "AboluteImagePositionMin", 0 - ( margin / 2 ),
                                                                      0 - ( margin / 2 ), null );

        this.absolutePosition = new GeometryFactory().createPoint( "AbolutePosition",
                                                                   bounds.getWidth() + ( margin / 2 ),
                                                                   bounds.getHeight() + ( margin / 2 ), null );

        System.out.println( "margin: " + margin + ", imageDimension: " + imageDimension + ", absImgPosMax: "
                            + absoluteImageMaxPos + ", absPos: " + absolutePosition + ", absImgPosMin: "
                            + absoluteImageMinPos );
    }

    /**
     * Initializes an image regarding to a filename with a specified bounding box and a margin. The difference to the
     * {@link #getImage(Envelope, boolean)} method is the initializing of the params assigned in this header.
     * 
     * @param wmsFilename
     *            the filename where to get the capabilities of data
     * @param bounds
     *            the bounds of the rectangle of the upper component
     * @param margin
     *            the margin is the value that specifies how is the image translated to have an image bigger than in
     *            upper, lower, left and right hand side. If the bounds are 200/200 and the margin is 200, the imagesize
     *            will be 400/400 and go over the bounds.
     */
    public void initImage( String wmsFilename, Rectangle bounds, int margin ) {

        this.wmsFilename = wmsFilename;
        this.bounds = bounds;
        this.margin = margin;
        // int ratio;
        ratio = determineRatio( bounds );
        WMSClient111 wmsClient;

        try {
            System.out.println( this.wmsFilename );
            wmsClient = new WMSClient111( new URL( wmsFilename ) );
            List<String> lays = Collections.singletonList( "dem" );
            srs = new CRS( "EPSG:32618" );

            bbox = wmsClient.getBoundingBox( srs.getName(), lays );

            if ( ratio == 0 ) {
                // do nothing

            } else if ( ratio < 0 ) {

                double newWidth = ( bounds.getWidth() / bounds.getHeight() )
                                  * ( bbox.getMax().get1() - bbox.getMin().get1() );

                bbox = new GeometryFactory().createEnvelope( bbox.getMin().get0(), bbox.getMin().get1(),
                                                             ( bbox.getMin().get0() + newWidth ), bbox.getMax().get1(),
                                                             srs );
                onePixel = transformToOnePixel( bounds, bbox );

            } else {
                double newHeight = ( bounds.getHeight() / bounds.getWidth() )
                                   * ( bbox.getMax().get0() - bbox.getMin().get0() );

                bbox = new GeometryFactory().createEnvelope( bbox.getMin().get0(), bbox.getMin().get1(),
                                                             bbox.getMax().get0(),
                                                             ( bbox.getMin().get0() + newHeight ), srs );

            }
            List<String> formatList = Collections.singletonList( "image/jpeg" );
            System.out.println( bounds.getWidth() + " = (" + bbox.getMin().get0() + ", " + bbox.getMax().get0()
                                + ") -> " + ( bbox.getMax().get0() - bbox.getMin().get0() ) );
            System.out.println( bounds.getHeight() + " = (" + bbox.getMin().get1() + ", " + bbox.getMax().get1()
                                + ") -> " + ( bbox.getMax().get1() - bbox.getMin().get1() ) );

            pair = wmsClient.getMap( lays, (int) bounds.getWidth(), (int) bounds.getHeight(), bbox, srs,
                                     formatList.get( 0 ), true, false, 20, false, null );

            this.drawingStartPos = new GeometryFactory().createPoint( "NewDrawingStartPosition", 0, 0, null );

            this.imageDimension = new GeometryFactory().createPoint( "ImageDimension", bounds.getWidth() + margin,
                                                                     bounds.getHeight() + margin, null );

            this.absoluteImageMaxPos = new GeometryFactory().createPoint( "AboluteImagePosition", bounds.getWidth()
                                                                                                  + ( margin / 2 ),
                                                                          bounds.getHeight() + ( margin / 2 ), null );
            this.absoluteImageMinPos = new GeometryFactory().createPoint( "AboluteImagePositionMin",
                                                                          0 - ( margin / 2 ), 0 - ( margin / 2 ), null );

            this.absolutePosition = new GeometryFactory().createPoint( "AbolutePosition", bounds.getWidth()
                                                                                          + ( margin / 2 ),
                                                                       bounds.getHeight() + ( margin / 2 ), null );
            System.out.println( "just init " );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Determines the ratio the boundingbox has to orient on. If there is a mismatch between the width and height this
     * should influence the display of the image returned by the WMS to prevent any deformation. <li>pos - orientation
     * on width because width is larger</li> <li>neg - orientation on hight because hight is larger</li> <li>other -
     * orientation on width/hight because they are even</li>
     * 
     * @param bounds
     *            the rectangle bounds, not <Code>null</Code>
     * @return an positive, negative or even integer
     */
    private int determineRatio( Rectangle bounds ) {

        System.out.println( "determineRatio: " + bounds );
        double w = bounds.getWidth();
        double h = bounds.getHeight();

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

    /**
     * Based on bounds from an upper component this method normalizes the bounds of the upper component regarding to an
     * envelope to one pixel.
     * 
     * @param bounds
     *            the rectangle bounds, not <Code>null</Code>
     * @param bbox
     *            the boundingbox, not <Code>null</Code>
     * @return a point, not <Code>null</Code>
     */
    private Point transformToOnePixel( Rectangle bounds, Envelope bbox ) {
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        measureX = bbox.getMax().get0() - bbox.getMin().get0();
        measureY = bbox.getMax().get1() - bbox.getMin().get1();
        double oneX = measureX / w;
        double oneY = measureY / h;

        return new GeometryFactory().createPoint( "OnePixelPoint", oneX, oneY, null );
    }

    /**
     * Transforms the new bounds of the rectangle with a normalized point of a boundingbox into a new envelope that can
     * be displayed.
     * 
     * @param bounds
     *            of a rectangle
     * @param point
     *            normalized point of a boundingbox
     * @return Envelope not <Code>null</Code>
     */
    public Envelope reTransformToEnvelope( Rectangle bounds, Point point ) {
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        double envStartPosX = bbox.getMin().get0() + w * point.get0();
        double envStartPosY = bbox.getMin().get1() + h * point.get1();

        double envEndPosX = bbox.getMax().get0() + w * point.get0();
        double envEndPosY = bbox.getMax().get1() + h * point.get1();

        System.out.println( "  start: " + envStartPosX + ", " + envStartPosY + " end: " + envEndPosX + ", "
                            + envEndPosY );

        return new GeometryFactory().createEnvelope( envStartPosX, envStartPosY, envEndPosX, envEndPosY, srs );

    }

    /**
     * Transforms a shifting of pixels into a new envelope.
     * 
     * @param shifting
     *            the pixels that are shifted, not <Code>null</Code>
     * @param onePixel
     *            one pixel, not <Code>null</Code>
     * @return Envelope, not <Code>null</Code>
     */
    public Envelope reTransformSomePointsToEnvelope( Point shifting, Point onePixel ) {

        double envStartPosX = bbox.getMin().get0() + shifting.get0() * onePixel.get0();
        double envStartPosY = bbox.getMin().get1() + shifting.get1() * onePixel.get1();

        double envEndPosX = bbox.getMax().get0() + shifting.get0() * onePixel.get0();
        double envEndPosY = bbox.getMax().get1() + shifting.get1() * onePixel.get1();

        System.out.println( "RetransformEnv  start: " + envStartPosX + ", " + envStartPosY + " end: " + envEndPosX
                            + ", " + envEndPosY );
        cachedEnvelope = new GeometryFactory().createEnvelope( envStartPosX, envStartPosY, envEndPosX, envEndPosY, srs );
        return cachedEnvelope;

    }

    /**
     * @return the drawingStartPos
     */
    public Point getDrawingStartPos() {
        return drawingStartPos;
    }

    /**
     * @param drawingStartPos
     *            the drawingStartPos to set
     */
    public void setDrawingStartPos( Point drawingStartPos ) {
        this.drawingStartPos = drawingStartPos;
    }

    /**
     * @return the imageDimension
     */
    public Point getImageDimension() {
        return imageDimension;
    }

    /**
     * @param imageDimension
     *            the imageDimension to set
     */
    public void setImageDimension( Point imageDimension ) {
        this.imageDimension = imageDimension;
    }

    public Graphics2D getG2() {
        return g2;
    }

    public void setG2( Graphics2D g2 ) {
        this.g2 = g2;
    }

    public Pair<BufferedImage, String> getPair() {
        return pair;
    }

    /**
     * @return the absoluteImagePos
     */
    public Point getAbsoluteImageMaxPos() {
        return absoluteImageMaxPos;
    }

    /**
     * @param absoluteImageMaxPos
     *            the absoluteImagePos to set
     */
    public void setAbsoluteImageMaxPos( Point absoluteImageMaxPos ) {
        this.absoluteImageMaxPos = absoluteImageMaxPos;
    }

    /**
     * @return the absoluteImageMinPos
     */
    public Point getAbsoluteImageMinPos() {
        return absoluteImageMinPos;
    }

    /**
     * @param absoluteImageMinPos
     *            the absoluteImageMinPos to set
     */
    public void setAbsoluteImageMinPos( Point absoluteImageMinPos ) {
        this.absoluteImageMinPos = absoluteImageMinPos;
    }

    /**
     * @return the absolutePosition
     */
    public Point getAbsolutePosition() {
        return absolutePosition;
    }

    /**
     * @param absolutePosition
     *            the absolutePosition to set
     */
    public void setAbsolutePosition( Point absolutePosition ) {
        this.absolutePosition = absolutePosition;
    }

    /**
     * @return the i
     */
    public Point getOnePixel() {
        return onePixel;
    }

    /**
     * @return the positionX
     */
    public int getPositionX() {
        return positionX;
    }

    /**
     * @return the positionY
     */
    public int getPositionY() {
        return positionY;
    }

    public String getWmsFilename() {
        return wmsFilename;
    }

    public void setWmsFilename( String wmsFilename ) {
        this.wmsFilename = wmsFilename;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void setBounds( Rectangle bounds ) {
        this.bounds = bounds;
    }

    public Envelope getCachedEnvelope() {
        return cachedEnvelope;
    }

    public void setCachedEnvelope( Envelope cachedEnvelope ) {
        this.cachedEnvelope = cachedEnvelope;
    }

}
