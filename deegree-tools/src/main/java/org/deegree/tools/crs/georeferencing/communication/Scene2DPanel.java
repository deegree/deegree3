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
package org.deegree.tools.crs.georeferencing.communication;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;

/**
 * The JPanel that should display a BufferedImage.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DPanel extends AbstractPanel2D {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public final static String SCENE2D_PANEL_NAME = "Scene2DPanel";

    private BufferedImage imageToDraw;

    private List<Polygon> polygonList;

    private Point2d beginDrawImageAtPosition;

    private Point2d translationPoint;

    private Rectangle imageDimension;

    private float resolution;

    private float resizing;

    private float initialResolution;

    public Scene2DPanel() {
        this.setName( SCENE2D_PANEL_NAME );
        this.selectedPoints = new ArrayList<Point4Values>();

    }

    @Override
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        Graphics2D g2 = (Graphics2D) g;

        if ( translationPoint == null ) {
            translationPoint = new Point2d( 0.0, 0.0 );
        }

        if ( imageToDraw != null ) {

            g2.drawImage( imageToDraw, (int) beginDrawImageAtPosition.getX(), (int) beginDrawImageAtPosition.getY(),
                          (int) imageDimension.width, (int) imageDimension.height, this );

        }
        g2.translate( -translationPoint.x, -translationPoint.y );
        if ( lastAbstractPoint != null ) {
            // if ( isTranslated == false ) {
            g2.fillOval( (int) lastAbstractPoint.getNewValue().getX() - 5,
                         (int) lastAbstractPoint.getNewValue().getY() - 5, 10, 10 );
            // }
        }

        if ( polygonList != null ) {
            // g2.translate( 29, 0 );
            // g2.rotate( 0.2 );
            for ( Polygon polygon : polygonList ) {
                g2.drawPolygon( polygon );
            }
            // g2.rotate( -0.2 );
            // g2.translate( -29, 0 );
        }

        if ( points != null ) {
            for ( Pair<Point4Values, Point4Values> point : points ) {
                g2.fillOval( (int) point.second.getNewValue().getX() - 5, (int) point.second.getNewValue().getY() - 5,
                             10, 10 );
            }
        }

        g2.translate( translationPoint.x, translationPoint.y );

        // if ( tempPoint != null ) {
        // // if ( isTranslated == true ) {
        // g2.fillOval( (int) ( tempPoint.x - 5 ), (int) ( tempPoint.y - 5 ), 10, 10 );
        //
        // // }
        // }

    }

    public Point2d getBeginDrawImageAtPosition() {
        return beginDrawImageAtPosition;
    }

    public void setBeginDrawImageAtPosition( Point2d beginDrawImageAtPosition ) {
        this.beginDrawImageAtPosition = beginDrawImageAtPosition;
    }

    public Rectangle getImageDimension() {
        return imageDimension;
    }

    public void setImageDimension( Rectangle imageDimension ) {
        this.imageDimension = imageDimension;
    }

    public void setImageToDraw( BufferedImage imageToDraw ) {
        this.imageToDraw = imageToDraw;

    }

    public void setinitialResolution( float initialResolution ) {
        this.initialResolution = initialResolution;
    }

    public Point2d getTranslationPoint() {
        return translationPoint;
    }

    public void setTranslationPoint( Point2d translationPoint ) {
        this.translationPoint = translationPoint;
    }

    @Override
    protected void updateSelectedPoints() {
        GeoReferencedPoint point = null;
        List<Point4Values> selectedPointsTemp = new ArrayList<Point4Values>();
        for ( Point4Values p : selectedPoints ) {
            point = new GeoReferencedPoint( ( p.getInitialValue().getX() / initialResolution ) * resolution,
                                            ( p.getInitialValue().getY() / initialResolution ) * resolution );
            selectedPointsTemp.add( new Point4Values( p.getNewValue(), p.getInitialValue(), point, p.getWorldCoords() ) );
        }
        selectedPoints = selectedPointsTemp;
        if ( lastAbstractPoint != null ) {
            double x = lastAbstractPoint.getInitialValue().getX() / initialResolution;
            double y = lastAbstractPoint.getInitialValue().getY() / initialResolution;
            double x1 = x - roundDouble( x * resizing );
            double y1 = y - roundDouble( y * resizing );

            GeoReferencedPoint pi = new GeoReferencedPoint( lastAbstractPoint.getNewValue().getX() + x1,
                                                            lastAbstractPoint.getNewValue().getY() + y1 );
            lastAbstractPoint.setNewValue( new GeoReferencedPoint( pi.getX(), pi.getY() ) );
        }

    }

    @Override
    public void updatePoints( float newSize ) {
        this.resizing = Math.abs( newSize - this.resolution );
        BigDecimal b = new BigDecimal( newSize );
        b = b.round( new MathContext( 2 ) );
        this.resolution = b.floatValue();

        updateSelectedPoints();
    }

    public void setPolygonList( List<Polygon> polygonList, List<Pair<Point4Values, Point4Values>> mappedPoints ) {

        // int counterSrc = 0;
        // int counterDst = 0;
        // AbstractGRPoint[] pointsSrc = new AbstractGRPoint[2];
        // AbstractGRPoint[] pointsDst = new AbstractGRPoint[2];
        // int count = 0;
        // for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {
        // // double x = p.first.getWorldCoords().getX();
        // // double y = p.first.getWorldCoords().getY();
        // // ordinatesSrc[counterSrc] = x;
        // // ordinatesSrc[++counterSrc] = y;
        // // counterSrc++;
        // pointsSrc[counterSrc] = p.first.getWorldCoords();
        // counterSrc++;
        //
        // // Point4Values pValue = p.second;
        // // x = pValue.getNewValue().getX();
        // // y = pValue.getNewValue().getY();
        // // ordinatesDst[counterDst] = x;
        // // ordinatesDst[++counterDst] = y;
        // // counterDst++;
        // pointsDst[counterDst] = p.second.getWorldCoords();
        // counterDst++;
        //
        // }
        //
        // double distanceSrc = 0.0;
        // distanceSrc = pointsSrc[0].distance( pointsSrc[1] );
        // System.out.println( distanceSrc );
        //
        // double distanceDst = 0.0;
        // distanceDst = pointsDst[0].distance( pointsDst[1] );
        // System.out.println( distanceDst );
        //
        // double scale = distanceDst / distanceSrc;
        // // for ( FootprintPoint p : footPanel.getPixelCoordinates() ) {
        // // double x = p.getX() * scale;
        // // double y = p.getY() * scale;
        // // System.out.println( x + " " + y );
        // // }

        this.polygonList = polygonList;

        // int counter = 0;
        // for ( Polygon po : polygonList ) {
        // int[] x = new int[po.npoints];
        // int[] y = new int[po.npoints];
        // for ( int i = 0; i < po.npoints; i++ ) {
        // x[i] = (int) ( po.xpoints[i] );
        // y[i] = (int) ( po.ypoints[i] );
        // pixelCoordinates[counter++] = new FootprintPoint( ( po.xpoints[i] - x ) * resolution,
        // ( po.ypoints[i] - y ) * resolution );
        // pointsPixelToWorld.put( new FootprintPoint( x2[i], y2[i] ), new FootprintPoint( po.xpoints[i],
        // po.ypoints[i] ) );
        // // System.out.println( "[Footprint] Polygon: " + x2[i] );
        // }
        // Polygon p = new Polygon( x2, y2, po.npoints );
        // pixelCoordinatePolygonList.add( p );

    }

}
