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
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.deegree.geometry.primitive.Ring;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
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

    private Rectangle imageDimension;

    private List<Ring> worldPolygonList;

    private ArrayList<Polygon> polygonListTranslated;

    public Scene2DPanel() {
        this.setName( SCENE2D_PANEL_NAME );
        this.selectedPoints = new ArrayList<Point4Values>();
        this.beginDrawImageAtPosition = new Point2d( 0, 0 );
        this.translationPoint = new Point2d( 0.0, 0.0 );
    }

    @Override
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        Graphics2D g2 = (Graphics2D) g;

        if ( translationPoint == null ) {
            translationPoint = new Point2d( 0.0, 0.0 );
        }
        g2.translate( -translationPoint.x, -translationPoint.y );
        if ( imageToDraw != null ) {

            g2.drawImage( imageToDraw, (int) translationPoint.x, (int) translationPoint.y, (int) imageDimension.width,
                          (int) imageDimension.height, this );

        }

        if ( lastAbstractPoint != null ) {
            g2.fillOval( (int) lastAbstractPoint.getNewValue().getX() - 5,
                         (int) lastAbstractPoint.getNewValue().getY() - 5, 10, 10 );
        }

        if ( polygonList != null ) {

            for ( Polygon polygon : polygonList ) {
                g2.drawPolygon( polygon );
            }

        }

        if ( selectedPoints != null ) {
            for ( Point4Values point : selectedPoints ) {
                g2.fillOval( (int) point.getNewValue().getX() - 5, (int) point.getNewValue().getY() - 5, 10, 10 );
            }
        }
        g2.translate( translationPoint.x, translationPoint.y );

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

    private void updateSelectedPoints( Scene2DValues sceneValues ) {
        List<Point4Values> selectedPointsTemp = new ArrayList<Point4Values>();
        for ( Point4Values p : selectedPoints ) {
            int[] pValues = sceneValues.getPixelCoord( p.getWorldCoords() );
            double x = pValues[0] + translationPoint.getX();
            double y = pValues[1] + translationPoint.getY();
            GeoReferencedPoint pi = new GeoReferencedPoint( x, y );
            selectedPointsTemp.add( new Point4Values( p.getNewValue(), p.getInitialValue(), pi, p.getWorldCoords() ) );
        }
        selectedPoints = selectedPointsTemp;
        if ( lastAbstractPoint != null ) {

            int[] p = sceneValues.getPixelCoord( lastAbstractPoint.getWorldCoords() );
            double x = p[0] + translationPoint.getX();
            double y = p[1] + translationPoint.getY();

            GeoReferencedPoint pi = new GeoReferencedPoint( x, y );
            lastAbstractPoint.setNewValue( new GeoReferencedPoint( pi.getX(), pi.getY() ) );
        }

    }

    @Override
    public void updatePoints( Scene2DValues sceneValues ) {
        if ( worldPolygonList != null ) {

            setPolygonList( worldPolygonList, sceneValues );
        }
        updateSelectedPoints( sceneValues );

    }

    public void setPolygonList( List<Ring> polygonRing, Scene2DValues sceneValues ) {
        if ( polygonRing != null ) {
            this.worldPolygonList = polygonRing;
            polygonListTranslated = new ArrayList<Polygon>();

            int sizeOfPoints = 0;
            for ( Ring p : polygonRing ) {
                sizeOfPoints += p.getControlPoints().size();

            }
            for ( Ring ring : polygonRing ) {
                int[] x2 = new int[ring.getControlPoints().size()];
                int[] y2 = new int[ring.getControlPoints().size()];
                for ( int i = 0; i < ring.getControlPoints().size(); i++ ) {
                    double x = ring.getControlPoints().getX( i );
                    double y = ring.getControlPoints().getY( i );
                    int[] p = sceneValues.getPixelCoord( new GeoReferencedPoint( x, y ) );
                    x2[i] = new Double( p[0] + translationPoint.getX() ).intValue();
                    y2[i] = new Double( p[1] + translationPoint.getY() ).intValue();

                }
                Polygon p = new Polygon( x2, y2, ring.getControlPoints().size() );
                polygonListTranslated.add( p );

            }

            this.polygonList = polygonListTranslated;
        } else {
            this.polygonList = null;
        }

    }

    public List<Ring> getWorldPolygonList() {
        return worldPolygonList;
    }

}
