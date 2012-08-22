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
package org.deegree.graphics.displayelements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * This is a rotated label with style information and screen coordinates, ready to be rendered to the view.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */

class RotatedLabel implements Label {

    private static final ILogger LOG = LoggerFactory.getLogger( RotatedLabel.class );

    private String caption;

    private int[] xpoints;

    private int[] ypoints;

    private double rotation;

    private Color color;

    private Font font;

    private double opacity;

    private int drawPointX, drawPointY, rotationX, rotationY;

    /**
     * 
     * @param caption
     * @param font
     * @param color
     * @param metrics
     * @param feature
     * @param halo
     * @param x
     * @param y
     * @param w
     * @param h
     * @param rotation
     * @param anchorPoint
     * @param displacement
     * @param opacity
     */
    RotatedLabel( String caption, Font font, Color color, int x, int y, int w, int h, double rotation,
                  double anchorPoint[], double[] displacement, double opacity ) {
        this.caption = caption;
        this.font = font;
        this.color = color;
        this.rotation = rotation;

        this.opacity = opacity;

        // vertices of label boundary
        int[] xpoints = new int[5];
        int[] ypoints = new int[5];
        xpoints[0] = x - w / 2;
        ypoints[0] = y - h / 2;
        xpoints[1] = x + w / 2;
        ypoints[1] = y - h / 2;
        xpoints[2] = x + w / 2;
        ypoints[2] = y + h / 2;
        xpoints[3] = x - w / 2;
        ypoints[3] = y + h / 2;
        xpoints[4] = x;
        ypoints[4] = y;

        this.drawPointX = x - w / 2;
        this.drawPointY = y + h / 2;
        this.rotationX = (int) Math.round( x + displacement[0] + ( anchorPoint[0] - 0.5 ) * w );
        this.rotationY = (int) Math.round( y - displacement[1] + ( anchorPoint[1] - 0.5 ) * h );

        // get rotated + translated points
        this.xpoints = new int[5];
        this.ypoints = new int[5];

        // transform all vertices of the boundary
        for ( int i = 0; i < 5; i++ ) {
            int[] point = transformPoint( xpoints[i], ypoints[i], rotationX, rotationY, rotation );
            this.xpoints[i] = point[0];
            this.ypoints[i] = point[1];
        }

        this.opacity = opacity;
    }

    /**
     * 
     * @return caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * 
     * @return rotation
     */
    public double getRotation() {
        return rotation;
    }

    /**
     *
     */
    public void paintBoundaries( Graphics2D g ) {
    }

    /**
     * Renders the label (including halo) to the submitted <tt>Graphics2D</tt> context.
     * <p>
     * 
     * @param g
     *            <tt>Graphics2D</tt> context to be used
     */
    public void paint( Graphics2D g ) {

        // get the current transform
        AffineTransform saveAT = g.getTransform();

        // perform transformation
        AffineTransform transform = (AffineTransform) saveAT.clone();

        transform.rotate( rotation / 180d * Math.PI, rotationX, rotationY );
        g.setTransform( transform );

        // render the text
        setColor( g, color, opacity );
        g.setFont( font );
        g.drawString( caption, drawPointX, drawPointY );

        // restore original transform
        g.setTransform( saveAT );
    }

    public int getX() {
        return xpoints[0];
    }

    public int getY() {
        return ypoints[0];
    }

    public int getMaxX() {
        return xpoints[1];
    }

    public int getMaxY() {
        return ypoints[1];
    }

    public int getMinX() {
        return xpoints[3];
    }

    public int getMinY() {
        return ypoints[3];
    }

    /**
     * Determines if the label intersects with another label.
     * <p>
     * 
     * @param that
     *            label to test
     * @return true if the labels intersect
     */
    public boolean intersects( Label that ) {
        LOG.logInfo( "Intersection test for rotated labels is not implemented yet!" );
        return false;
    }

    private int[] transformPoint( int x, int y, int tx, int ty, double rotation ) {

        double cos = Math.cos( rotation );
        double sin = Math.sin( rotation );

        double m00 = cos;
        double m01 = -sin;
        // double m02 = cos * dx - sin * dy + tx - tx * cos + ty * sin;
        double m02 = tx - tx * cos + ty * sin;
        double m10 = sin;
        double m11 = cos;
        // double m12 = sin * dx + cos * dy + ty - tx * sin - ty * cos;
        double m12 = ty - tx * sin - ty * cos;

        int[] point2 = new int[2];

        point2[0] = (int) ( m00 * x + m01 * y + m02 + 0.5 );
        point2[1] = (int) ( m10 * x + m11 * y + m12 + 0.5 );

        return point2;
    }

    private Graphics2D setColor( Graphics2D g2, Color color, double opacity ) {
        if ( opacity < 0.999 ) {
            final int alpha = (int) Math.round( opacity * 255 );
            final int red = color.getRed();
            final int green = color.getGreen();
            final int blue = color.getBlue();
            color = new Color( red, green, blue, alpha );
        }

        g2.setColor( color );
        return g2;
    }

    @Override
    public String toString() {
        return caption;
    }
}
