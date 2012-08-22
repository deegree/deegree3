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
package org.deegree.graphics.sld;

import static org.deegree.framework.xml.XMLTools.escape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.Marshallable;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.jfree.util.Log;

/**
 * A Mark takes a "shape" and applies coloring to it. The shape can be derived either from a well-known name (such as
 * "square"), an external URL in various formats (such as, perhaps GIF), or from a glyph of a font. Multiple external
 * formats may be used with the semantic that they all contain the equivalent shape in different formats. If an image
 * format is used that has inherent coloring, the coloring is discarded and only the opacity channel (or equivalent) is
 * used. A Halo, Fill, and/or Stroke is applied as appropriate for the shape's source format.
 * <p>
 * 
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */

public class Mark implements Marshallable {

    private BufferedImage image = null;

    private Fill fill = null;

    private String wellKnownName = null;

    private Stroke stroke = null;

    /**
     * Constructor for the default <tt>Mark</tt>.
     */
    Mark() {
        // nothing to do
    }

    /**
     * constructor initializing the class with the <Mark>
     * 
     * @param wellKnownName
     * @param stroke
     * @param fill
     */
    Mark( String wellKnownName, Stroke stroke, Fill fill ) {
        setWellKnownName( wellKnownName );
        setStroke( stroke );
        setFill( fill );
    }

    /**
     * Gives the well known name of a Mark's shape. Allowed values include at least "square", "circle", "triangle",
     * "star", "cross", and "x", though map servers may draw a different symbol instead if they don't have a shape for
     * all of these. Renderings of these marks may be made solid or hollow depending on Fill and Stroke parameters. The
     * default value is "square".
     * 
     * @return the WK-Name of the mark
     * 
     */
    public String getWellKnownName() {
        return wellKnownName;
    }

    /**
     * Sets the well known name of a Mark's shape. Allowed values include at least "square", "circle", "triangle",
     * "star", "cross", and "x", though map servers may draw a different symbol instead if they don't have a shape for
     * all of these. Renderings of these marks may be made solid or hollow depending on Fill and Stroke parameters. The
     * default value is "square".
     * 
     * @param wellKnownName
     *            the WK-Name of the mark
     * 
     */
    public void setWellKnownName( String wellKnownName ) {
        this.wellKnownName = wellKnownName;
    }

    /**
     * A Fill allows area geometries to be filled. There are two types of fills: solid-color and repeated GraphicFill.
     * In general, if a Fill element is omitted in its containing element, no fill will be rendered. The default is a
     * solid 50%-gray (color "#808080") opaque fill.
     * 
     * @return the fill of the mark
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * sets the <Fill>
     * 
     * @param fill
     *            the fill of the mark
     */
    public void setFill( Fill fill ) {
        this.fill = fill;
    }

    /**
     * A Stroke allows a string of line segments (or any linear geometry) to be rendered. There are three basic types of
     * strokes: solid Color, GraphicFill (stipple), and repeated GraphicStroke. A repeated graphic is plotted linearly
     * and has its graphic symbol bended around the curves of the line string. The default is a solid black line (Color
     * "#000000").
     * 
     * @return the stroke of the mark
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * sets <Stroke>
     * 
     * @param stroke
     *            the stroke of the mark
     */
    public void setStroke( Stroke stroke ) {
        this.stroke = stroke;
    }

    /**
     * Draws the given feature on the buffered image. The drawing values are used from the 'Fill' object (if set). or
     * white as a default fill color and black as a stroke color. If the stroke is null the BasicStroke values
     * (CAP_ROUND, JOIN_ROUND) will be used.
     * <p>
     * Be careful to set the buffered image first!.
     * 
     * @param feature
     * 
     * @param size
     *            DOCUMENT ME!
     * 
     * @return The feature as a buffered image with a the 'Fill' values. or white as a default fill color and black as a
     *         stroke color.
     * @throws FilterEvaluationException
     */
    public BufferedImage getAsImage( Feature feature, int size )
                            throws FilterEvaluationException {
        double fillOpacity = 1.0;
        double strokeOpacity = 1.0;
        float[] dash = null;
        float dashOffset = 0;
        int cap = BasicStroke.CAP_ROUND;
        int join = BasicStroke.JOIN_ROUND;
        float width = 1;
        Color fillColor = new Color( 128, 128, 128 );
        Color strokeColor = new Color( 0, 0, 0 );
        String symbol = null;

        if ( fill != null ) {
            fillOpacity = fill.getOpacity( feature );
            fillColor = fill.getFill( feature );
            symbol = fill.getSymbol( feature );
        }

        if ( stroke != null ) {
            strokeOpacity = stroke.getOpacity( feature );
            strokeColor = stroke.getStroke( feature );
            dash = stroke.getDashArray( feature );
            cap = stroke.getLineCap( feature );
            join = stroke.getLineJoin( feature );
            width = (float) stroke.getWidth( feature );
            dashOffset = stroke.getDashOffset( feature );
        }
        if ( wellKnownName == null ) {
            wellKnownName = "square";
        }

        if ( symbol != null ) {
            try {
                ExternalGraphic eg = new ExternalGraphic( null, new URL( symbol ) );
                return eg.getAsImage( size, size, feature );
            } catch ( MalformedURLException e ) {
                Log.debug( "Could not create an image out of the given symbol with url " + symbol );
            }
        } else if ( wellKnownName.equalsIgnoreCase( "circle" ) ) {
            image = drawCircle( size, fillOpacity, fillColor, strokeOpacity, strokeColor, dash, dashOffset, width, cap,
                                join );
        } else if ( wellKnownName.equalsIgnoreCase( "triangle" ) ) {
            image = drawTriangle( size, fillOpacity, fillColor, strokeOpacity, strokeColor, dash, dashOffset, width,
                                  cap, join );
        } else if ( wellKnownName.equalsIgnoreCase( "cross" ) ) {
            image = drawCross1( size, strokeOpacity, strokeColor, dash, dashOffset, width, cap, join );
        } else if ( wellKnownName.equalsIgnoreCase( "x" ) ) {
            image = drawCross2( size, strokeOpacity, strokeColor, dash, dashOffset, width, cap, join );
        } else if ( wellKnownName.startsWith( "CHAR" ) ) {
            image = drawCharacter( size, fillOpacity, fillColor, strokeOpacity, strokeColor, wellKnownName );
        } else if ( wellKnownName.equalsIgnoreCase( "star" ) ) {
            image = drawStar( size, fillOpacity, fillColor, strokeOpacity, strokeColor, dash, dashOffset, width, cap,
                              join );
        } else {
            image = drawSquare( size, fillOpacity, fillColor, strokeOpacity, strokeColor, dash, dashOffset, width, cap,
                                join );
        }

        return image;
    }

    /**
     * Sets the mark as an image. RThis method is not part of the sld specifications but it is added to speed up
     * applications.
     * 
     * @param bufferedImage
     *            the bufferedImage to be set for the mark
     */
    public void setAsImage( BufferedImage bufferedImage ) {
        this.image = bufferedImage;
    }

    /**
     * 
     * @param dash
     * @param dashOffset
     * @param width
     * @param cap
     * @param join
     * @return the basic stroke
     */
    private BasicStroke createBasicStroke( float[] dash, float dashOffset, float width, int cap, int join ) {
        BasicStroke bs2 = null;
        if ( ( dash == null ) || ( dash.length < 2 ) ) {
            bs2 = new BasicStroke( width, cap, join );
        } else {
            bs2 = new BasicStroke( width, cap, join, 10.0f, dash, dashOffset );
        }
        return bs2;
    }

    /**
     * Draws a scaled instance of a triangle mark according to the given parameters.
     * 
     * @param size
     *            resulting image's height and width
     * @param fillOpacity
     *            opacity value for the filled parts of the image
     * @param fillColor
     *            <tt>Color</tt> to be used for the fill
     * @param strokeOpacity
     *            opacity value for the stroked parts of the image
     * @param strokeColor
     *            <tt>Color</tt> to be used for the strokes
     * @param dash
     *            dash array for rendering boundary line
     * @param width
     *            of the boundary line
     * @param cap
     *            of the boundary line
     * @param join
     *            of the boundary line
     * @param dashOffset
     * 
     * @return image displaying a triangle
     */
    public BufferedImage drawTriangle( int size, double fillOpacity, Color fillColor, double strokeOpacity,
                                       Color strokeColor, float[] dash, float dashOffset, float width, int cap, int join ) {

        int offset = (int) ( width * 2 + 1 ) / 2;
        BufferedImage image = new BufferedImage( size + offset * 2, size + offset * 2, BufferedImage.TYPE_INT_ARGB );

        int[] x_ = new int[3];
        int[] y_ = new int[3];
        x_[0] = offset;
        y_[0] = offset;
        x_[1] = size / 2 + offset;
        y_[1] = size - 1 + offset;
        x_[2] = size - 1 + offset;
        y_[2] = offset;

        Graphics2D g2D = (Graphics2D) image.getGraphics();
        BasicStroke bs = createBasicStroke( dash, dashOffset, width, cap, join );
        g2D.setStroke( bs );
        setColor( g2D, fillColor, fillOpacity );
        g2D.fillPolygon( x_, y_, 3 );
        setColor( g2D, strokeColor, strokeOpacity );
        g2D.drawPolygon( x_, y_, 3 );
        g2D.dispose();

        return image;
    }

    /**
     * Draws a five-pointed star (pentagram) according to the given parameters.
     * 
     * @param size
     *            resulting image's height and width
     * @param fillOpacity
     *            opacity value for the filled parts of the image
     * @param fillColor
     *            <tt>Color</tt> to be used for the fill
     * @param strokeOpacity
     *            opacity value for the stroked parts of the image
     * @param strokeColor
     *            <tt>Color</tt> to be used for the strokes
     * @param dash
     *            dash array for rendering boundary line
     * @param width
     *            of the boundary line
     * @param cap
     *            of the boundary line
     * @param join
     *            of the boundary line
     * @param dashOffset
     * 
     * @return an image of a pentagram
     */
    public BufferedImage drawStar( int size, double fillOpacity, Color fillColor, double strokeOpacity,
                                   Color strokeColor, float[] dash, float dashOffset, float width, int cap, int join ) {
        int offset = (int) ( width * 2 + 1 ) / 2;
        BufferedImage image = new BufferedImage( size + offset * 2, size + offset * 2, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g2D = image.createGraphics();
        BasicStroke bs = createBasicStroke( dash, dashOffset, width, cap, join );
        g2D.setStroke( bs );
        int s = size / 2;
        int x0 = s;
        int y0 = s;
        double sin36 = Math.sin( Math.toRadians( 36 ) );
        double cos36 = Math.cos( Math.toRadians( 36 ) );
        double sin18 = Math.sin( Math.toRadians( 18 ) );
        double cos18 = Math.cos( Math.toRadians( 18 ) );
        int smallRadius = (int) ( s * sin18 / Math.sin( Math.toRadians( 54 ) ) );

        int p0X = x0;
        int p0Y = y0 - s;
        int p1X = x0 + (int) ( smallRadius * sin36 );
        int p1Y = y0 - (int) ( smallRadius * cos36 );
        int p2X = x0 + (int) ( s * cos18 );
        int p2Y = y0 - (int) ( s * sin18 );
        int p3X = x0 + (int) ( smallRadius * cos18 );
        int p3Y = y0 + (int) ( smallRadius * sin18 );
        int p4X = x0 + (int) ( s * sin36 );
        int p4Y = y0 + (int) ( s * cos36 );
        int p5Y = y0 + smallRadius;
        int p6X = x0 - (int) ( s * sin36 );
        int p7X = x0 - (int) ( smallRadius * cos18 );
        int p8X = x0 - (int) ( s * cos18 );
        int p9X = x0 - (int) ( smallRadius * sin36 );

        int[] x = new int[] { p0X, p1X, p2X, p3X, p4X, p0X, p6X, p7X, p8X, p9X };
        int[] y = new int[] { p0Y, p1Y, p2Y, p3Y, p4Y, p5Y, p4Y, p3Y, p2Y, p1Y };
        for ( int i = 0; i < y.length; i++ ) {
            x[i] = x[i] + offset;
            y[i] = y[i] + offset;
        }
        Polygon shape = new Polygon( x, y, 10 );

        setColor( g2D, fillColor, fillOpacity );
        g2D.fill( shape );
        setColor( g2D, strokeColor, strokeOpacity );
        g2D.draw( shape );

        g2D.dispose();

        return image;
    }

    /**
     * Draws a scaled instance of a circle mark according to the given parameters.
     * 
     * @param size
     *            resulting image's height and widthh
     * @param fillOpacity
     *            opacity value for the filled parts of the image
     * @param fillColor
     *            <tt>Color</tt> to be used for the fill
     * @param strokeOpacity
     *            opacity value for the stroked parts of the image
     * @param strokeColor
     *            <tt>Color</tt> to be used for the strokes
     * @param dash
     *            dash array for rendering boundary line
     * @param width
     *            of the boundary line
     * @param cap
     *            of the boundary line
     * @param join
     *            of the boundary line
     * @param dashOffset
     * 
     * @return image displaying a circle
     */
    public BufferedImage drawCircle( int size, double fillOpacity, Color fillColor, double strokeOpacity,
                                     Color strokeColor, float[] dash, float dashOffset, float width, int cap, int join ) {
        int offset = (int) ( width * 2 + 1 ) / 2;
        BufferedImage image = new BufferedImage( size + offset * 2, size + offset * 2, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g2D = (Graphics2D) image.getGraphics();
        BasicStroke bs = createBasicStroke( dash, dashOffset, width, cap, join );
        g2D.setStroke( bs );
        setColor( g2D, fillColor, fillOpacity );
        g2D.fillOval( offset, offset, size, size );

        setColor( g2D, strokeColor, strokeOpacity );
        g2D.drawOval( offset, offset, size, size );
        g2D.dispose();

        return image;
    }

    /**
     * Draws a scaled instance of a square mark according to the given parameters.
     * 
     * @param size
     *            resulting image's height and widthh
     * @param fillOpacity
     *            opacity value for the filled parts of the image
     * @param fillColor
     *            <tt>Color</tt> to be used for the fill
     * @param strokeOpacity
     *            opacity value for the stroked parts of the image
     * @param strokeColor
     *            <tt>Color</tt> to be used for the strokes
     * @param dash
     *            dash array for rendering boundary line
     * @param width
     *            of the boundary line
     * @param cap
     *            of the boundary line
     * @param join
     *            of the boundary line
     * @param dashOffset
     * 
     * @return image displaying a square
     */
    public BufferedImage drawSquare( int size, double fillOpacity, Color fillColor, double strokeOpacity,
                                     Color strokeColor, float[] dash, float dashOffset, float width, int cap, int join ) {
        int offset = (int) ( width * 2 + 1 ) / 2;
        BufferedImage image = new BufferedImage( size + offset * 2, size + offset * 2, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g2D = (Graphics2D) image.getGraphics();
        BasicStroke bs = createBasicStroke( dash, dashOffset, width, cap, join );
        g2D.setStroke( bs );
        setColor( g2D, fillColor, fillOpacity );
        g2D.fillRect( offset, offset, size, size );

        setColor( g2D, strokeColor, strokeOpacity );
        g2D.drawRect( offset, offset, size - 1, size - 1 );
        g2D.dispose();

        return image;
    }

    /**
     * Draws a scaled instance of a cross mark (a "+") according to the given parameters.
     * 
     * @param size
     *            resulting image's height and widthh
     * @param strokeOpacity
     *            opacity value for the stroked parts of the image
     * @param strokeColor
     *            <tt>Color</tt> to be used for the strokes
     * @param dash
     *            dash array for rendering boundary line
     * @param width
     *            of the boundary line
     * @param cap
     *            of the boundary line
     * @param join
     *            of the boundary line
     * @param dashOffset
     * 
     * @return image displaying a cross (a "+")
     */
    public BufferedImage drawCross1( int size, double strokeOpacity, Color strokeColor, float[] dash, float dashOffset,
                                     float width, int cap, int join ) {

        int offset = (int) ( width * 2 + 1 ) / 2;
        BufferedImage image = new BufferedImage( size + offset * 2, size + offset * 2, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g2D = (Graphics2D) image.getGraphics();

        BasicStroke bs = createBasicStroke( dash, dashOffset, width, cap, join );
        g2D.setStroke( bs );

        setColor( g2D, strokeColor, strokeOpacity );
        g2D.drawLine( offset, size / 2 + offset, size - 1 + offset, size / 2 + offset );
        g2D.drawLine( size / 2 + offset, offset, size / 2 + offset, size - 1 + offset );
        g2D.dispose();
        return image;
    }

    /**
     * Draws a scaled instance of a cross mark (an "X") according to the given parameters.
     * 
     * @param size
     *            resulting image's height and widthh
     * @param strokeOpacity
     *            opacity value for the stroked parts of the image
     * @param strokeColor
     *            <tt>Color</tt> to be used for the strokes
     * @param dash
     *            dash array for rendering boundary line
     * @param width
     *            of the boundary line
     * @param cap
     *            of the boundary line
     * @param join
     *            of the boundary line
     * @param dashOffset
     * 
     * @return image displaying a cross (a "X")
     */
    public BufferedImage drawCross2( int size, double strokeOpacity, Color strokeColor, float[] dash, float dashOffset,
                                     float width, int cap, int join ) {

        int offset = (int) ( width * 2 + 1 ) / 2;
        BufferedImage image = new BufferedImage( size + offset * 2, size + offset * 2, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g2D = (Graphics2D) image.getGraphics();

        BasicStroke bs = createBasicStroke( dash, dashOffset, width, cap, join );
        g2D.setStroke( bs );

        setColor( g2D, strokeColor, strokeOpacity );
        g2D.drawLine( offset, offset, size - 1 + offset, size - 1 + offset );
        g2D.drawLine( offset, size - 1 + offset, size - 1 + offset, offset );
        g2D.dispose();

        return image;
    }

    /**
     * 
     * @param size
     * @param fillOpacity
     * @param fillColor
     * @param strokeOpacity
     * @param strokeColor
     * @param charDesc
     *            e.g. CHAR:Times New Roman:45
     */
    private BufferedImage drawCharacter( int size, double fillOpacity, Color fillColor, double strokeOpacity,
                                         Color strokeColor, String charDesc ) {

        String[] tmp = StringTools.toArray( charDesc, ":", false );

        BufferedImage image = new BufferedImage( size, size, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g2 = (Graphics2D) image.getGraphics();
        setColor( g2, fillColor, fillOpacity );
        g2.fillRect( 0, 0, size, size );

        java.awt.Font font = new java.awt.Font( tmp[1], java.awt.Font.PLAIN, size );
        g2.setFont( font );
        FontMetrics fm = g2.getFontMetrics();

        char c = (char) Integer.parseInt( tmp[2] );
        int w = fm.charWidth( c );
        int h = fm.getHeight();

        String s = "" + c;
        setColor( g2, strokeColor, strokeOpacity );
        g2.drawString( s, size / 2 - w / 2, size / 2 + h / 2 - fm.getDescent() );
        g2.dispose();
        return image;
    }

    /**
     * @param g2D
     * @param color
     * @param opacity
     */
    private void setColor( Graphics2D g2D, Color color, double opacity ) {
        if ( opacity < 0.999 ) {
            final int alpha = (int) Math.round( opacity * 255 );
            final int red = color.getRed();
            final int green = color.getGreen();
            final int blue = color.getBlue();
            color = new Color( red, green, blue, alpha );
        }

        g2D.setColor( color );
    }

    /**
     * exports the content of the Mark as XML formated String
     * 
     * @return xml representation of the Mark
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<Mark>" );
        if ( wellKnownName != null && !wellKnownName.equals( "" ) ) {
            sb.append( "<WellKnownName>" ).append( escape( wellKnownName ) );
            sb.append( "</WellKnownName>" );
        }
        if ( fill != null ) {
            sb.append( ( (Marshallable) fill ).exportAsXML() );
        }
        if ( stroke != null ) {
            sb.append( ( (Marshallable) stroke ).exportAsXML() );
        }

        sb.append( "</Mark>" );

        return sb.toString();
    }

    // private void drawUnicode(Graphics2D g2, int x, int y, double rotation,
    // double size, String m, Mark mark) {
    // int sz = (int)size;
    // double fo = mark.getFill().getOpacity();
    // double so = mark.getStroke().getOpacity();
    //
    // java.awt.Font font = new java.awt.Font("sans serif", java.awt.Font.PLAIN,
    // sz);
    // g2.setFont( font );
    // FontMetrics fm = g2.getFontMetrics();
    //
    // char c = (char)m.charAt(0);
    // int w = fm.charWidth(c);
    // int h = fm.getHeight();
    //
    // g2 = setColor( g2, mark.getFill().getFill(), fo );
    // g2.fillRect( x-w/2, y-h/2, w, h);
    // g2 = setColor( g2, mark.getStroke().getStroke(), so );
    //
    // String s = "" + c;
    // g2.drawString( s, x-w/2, y+h/2-fm.getDescent());
    // }
    // else {
    //
    // Mark[] marks = sym.getGraphic().getMarks();
    // double rotation = sym.getGraphic().getRotation();
    // double size = sym.getGraphic().getSize();
    // if (marks != null) {
    //
    // for (int k = 0; k > marks.length; k++) {
    //
    // float w = (float)marks[k].getStroke().getWidth();
    // g2.setStroke( new BasicStroke( w ) );
    //
    // if (marks[k].getWellKnownName().equalsIgnoreCase("triangle") ) {
    // drawTriangle( g2, x, y, rotation, size, marks[k] );
    // }
    // else
    // if (marks[k].getWellKnownName().equalsIgnoreCase("circle") ) {
    // drawCircle( g2, x, y, rotation, size, marks[k] );
    // }
    // else
    // if (marks[k].getWellKnownName().equalsIgnoreCase("square") ) {
    // drawSquare( g2, x, y, rotation, size, marks[k] );
    // }
    // else
    // if (marks[k].getWellKnownName().equalsIgnoreCase("cross") ) {
    // drawCross1( g2, x, y, rotation, size, marks[k] );
    // }
    // else
    // if (marks[k].getWellKnownName().equalsIgnoreCase("x") ) {
    // drawCross2( g2, x, y, rotation, size, marks[k] );
    // }
    // else
    // if (marks[k].getWellKnownName().length() == 0 ) {
    // drawSquare( g2, x, y, rotation, size, marks[k] );
    // }
    // else {
    // drawUnicode( g2, x, y, rotation, size,
    // marks[k].getWellKnownName(), marks[k] );
    // }
    // }
    // }
    // }
}
