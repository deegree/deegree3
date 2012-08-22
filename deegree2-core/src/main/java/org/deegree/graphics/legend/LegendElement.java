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
package org.deegree.graphics.legend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.displayelements.DisplayElementFactory;
import org.deegree.graphics.displayelements.IncompatibleGeometryTypeException;
import org.deegree.graphics.displayelements.PolygonDisplayElement;
import org.deegree.graphics.sld.LineSymbolizer;
import org.deegree.graphics.sld.PointSymbolizer;
import org.deegree.graphics.sld.PolygonSymbolizer;
import org.deegree.graphics.sld.RasterSymbolizer;
import org.deegree.graphics.sld.Rule;
import org.deegree.graphics.sld.Symbolizer;
import org.deegree.graphics.sld.TextSymbolizer;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.wms.GraphicContextFactory;

/**
 * The implements the basic legend element. a legend element may has a label that can be set to eight positions relative
 * to the legend graphic. A <tt>LegendElement</tt> can be activated or deactivated. It depends on the using
 * application what effect this behavior will have.
 * <p>
 * <tt>LegendElement</tt>s can be collected in a <tt>LegendElementCollection</tt> which also is a
 * <tt>LegendElement</tt> to group elements or to create more complex elements.
 * <p>
 * Each <tt>LegendElement</tt> is able to paint itself as <tt>BufferedImage</tt>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public class LegendElement {

    private static final ILogger LOG = LoggerFactory.getLogger( LegendElement.class );

    /**
     * the list of rules
     */
    protected ArrayList<Rule> ruleslist = null;

    /**
     * The initial empty label.
     */
    protected String label = "";

    /**
     * The orientation initialized with 0.
     */
    protected double orientation = 0;

    /**
     * The label position initialized with -1.
     */
    protected int labelPosition = -1;

    /**
     * A flag signaling if the legend is active initialized with false.
     */
    protected boolean active = false;

    /**
     * The width of the legend element initialized with 0;
     */
    protected int width = 0;

    /**
     * The height of the legend element initialized with 0;
     */
    protected int height = 0;

    /**
     * The width in pixels between the legend and it's label initialized with 10;
     */
    protected int bufferBetweenLegendAndLabel = 10;

    /**
     * The icon of the legend
     */
    protected BufferedImage bi;

    /**
     * empty constructor
     *
     */
    protected LegendElement() {
        this.ruleslist = new ArrayList<Rule>();
    }

    /**
     * @param legendImage
     *            the icon of the legend.
     *
     *
     */
    LegendElement( BufferedImage legendImage ) {
        this();
        bi = legendImage;
    }

    /**
     * constructor
     *
     * @param rules
     *            the different rules from the SLD
     * @param label
     *            the label beneath the legend symbol
     * @param orientation
     *            the rotation of the text in the legend
     * @param labelPosition
     *            the position of the text according to the symbol
     * @param active
     *            whether the legendsymbol is active or not
     * @param width
     *            the requested width of the legend symbol
     * @param height
     *            the requested height of the legend symbol
     */
    LegendElement( Rule[] rules, String label, double orientation, int labelPosition, boolean active, int width,
                   int height ) {
        this();
        setRules( rules );
        setLabel( label );
        setLabelOrientation( orientation );
        setLabelPlacement( labelPosition );
        setActive( active );
        setWidth( width );
        setHeight( height );
    }

    /**
     * gets the Rules as an array
     *
     * @return array of sld rules
     */
    public Rule[] getRules() {
        if ( ruleslist != null && ruleslist.size() > 0 ) {
            return ruleslist.toArray( new Rule[ruleslist.size()] );
        }
        return null;
    }

    /**
     * adds a rule to the ArrayList ruleslist
     *
     * @param rule
     *            a sld rule
     */
    public void addRule( Rule rule ) {
        this.ruleslist.add( rule );
    }

    /**
     * sets the rules
     *
     * @param rules
     *            an array of sld rules
     */
    public void setRules( Rule[] rules ) {
        this.ruleslist.clear();

        if ( rules != null ) {
            for ( int i = 0; i < rules.length; i++ ) {
                this.ruleslist.add( rules[i] );
            }
        }
    }

    /**
     * sets the label of the <tt>LegendElement</tt>
     *
     * @param label
     *            label of the <tt>LegendElement</tt>
     *
     */
    public void setLabel( String label ) {
        this.label = label;
    }

    /**
     * returns the label set to <tt>LegendElement</tt>. If no label is set, the method returns <tt>null</tt>
     *
     * @return label of the <tt>LegendElement</tt> or <tt>null</tt>
     *
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * sets the orientation of the label of the <tt>LegendElement</tt>. A label can have an orientation from -90� to
     * 90� expressed in radians, where 0� is horizontal
     *
     * @param orientation
     */
    public void setLabelOrientation( double orientation ) {
        this.orientation = orientation;
    }

    /**
     * returns the current orientation of the label of the <tt>LegendElement</tt> in radians. If the element hasn't a
     * label <tt>Double.NEGATIVE_INFINITY</tt> will be returned.
     *
     * @return orientation of the label of the <tt>LegendElement</tt> in radians
     */
    public double getLabelOrientation() {
        return this.orientation;
    }

    /**
     * sets the placement of the label relative to the legend symbol. Possible values are:
     * <ul>
     * <li>LP_TOPCENTER
     * <li>LP_TOPLEFT
     * <li>LP_TOPRIGHT
     * <li>LP_RIGHT
     * <li>LP_LEFT
     * <li>LP_BOTTOMCENTER
     * <li>LP_BOTTOMRIGHT
     * <li>LP_BOTTOMLEFT
     * </ul>
     *
     * <pre>
     *   +---+---+---+
     *   | 1 | 0 | 2 |
     *   +---+---+---+
     *   | 4 |LEG| 3 |
     *   +---+---+---+
     *   | 7 | 5 | 6 |
     *   +---+---+---+
     * </pre>
     *
     * An implementation of the interface may not supoort all positions.
     *
     * @param labelPosition
     */
    public void setLabelPlacement( int labelPosition ) {
        this.labelPosition = labelPosition;
    }

    /**
     * returns the placement of the label relative to the legend symbol. If the element hasn't a label
     * <tt>LegendElement.LP_NOLABEL</tt> will be returned. Otherwise possible values are:
     * <ul>
     * <li>LP_TOPCENTER
     * <li>LP_TOPLEFT
     * <li>LP_TOPRIGHT
     * <li>LP_RIGHT
     * <li>LP_LEFT
     * <li>LP_BOTTOMCENTER
     * <li>LP_BOTTOMRIGHT
     * <li>LP_BOTTOMLEFT
     * </ul>
     *
     * @return coded placement of the label relative to the legend symbol
     */
    public int getLabelPlacement() {
        return this.labelPosition;
    }

    /**
     * activates or deactivates the label
     *
     * @param active
     */
    public void setActive( boolean active ) {
        this.active = active;
    }

    /**
     * @return the activtion-status of the label
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * sets the width of the LegendSymbol (in pixels)
     *
     * @param width
     */
    public void setWidth( int width ) {
        this.width = width;
    }

    /**
     * @return the width of the LegendSymbol (in pixels)
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * sets the height of the LegendSymbol (in pixels)
     *
     * @param height
     */
    public void setHeight( int height ) {
        this.height = height;
    }

    /**
     * @return the height of the LegendSymbol (in pixels)
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * returns the buffer place between the legend symbol and the legend label in pixels
     *
     * @return the buffer as integer in pixels
     */
    public int getBufferBetweenLegendAndLabel() {
        return this.bufferBetweenLegendAndLabel;
    }

    /**
     * @see org.deegree.graphics.legend.LegendElement#getBufferBetweenLegendAndLabel()
     * @param i
     *            the buffer as integer in pixels
     */
    public void setBufferBetweenLegendAndLabel( int i ) {
        this.bufferBetweenLegendAndLabel = i;
    }

    /**
     * draws a legendsymbol, if the SLD defines a point
     *
     * @param g
     *            the graphics context
     * @param c
     *            the PointSymbolizer representing the drawable point
     * @param width
     *            the requested width of the symbol
     * @param height
     *            the requested height of the symbol
     * @throws LegendException
     *             is thrown, if the parsing of the sld failes.
     */
    protected void drawPointLegend( Graphics g, PointSymbolizer c, int width, int height )
                            throws LegendException {
        org.deegree.graphics.sld.Graphic deegreegraphic = c.getGraphic();
        try {
            BufferedImage buffi = deegreegraphic.getAsImage( null );
            int w = buffi.getWidth();
            int h = buffi.getHeight();
            g.drawImage( buffi, width / 2 - w / 2, height / 2 - h / 2, null );
        } catch ( FilterEvaluationException feex ) {
            throw new LegendException( "FilterEvaluationException occured during " + "the creation of the legend:\n"
                                       + "The legend for the PointSymbol can't be processed.\n" + feex.getMessage() );
        }

    }

    /**
     * draws a legendsymbol, if the SLD defines a line
     *
     * @param g
     *            the graphics context
     * @param ls
     *            the LineSymbolizer representing the drawable line
     * @param width
     *            the requested width of the symbol
     * @param height
     *            the requested height of the symbol
     * @throws LegendException
     *             is thrown, if the parsing of the sld failes.
     */
    protected void drawLineStringLegend( Graphics2D g, LineSymbolizer ls, int width, int height )
                            throws LegendException {

        org.deegree.graphics.sld.Stroke sldstroke = ls.getStroke();
        try {
            // color, opacity
            setColor( g, sldstroke.getStroke( null ), sldstroke.getOpacity( null ) );
            g.setStroke( getBasicStroke( sldstroke ) );
        } catch ( FilterEvaluationException feex ) {
            throw new LegendException( "FilterEvaluationException occured during the creation "
                                       + "of the legend:\n The legend for the LineSymbol can't be " + "processed.\n"
                                       + feex.getMessage() );
        }

        // p1 = [0 | height]
        // p2 = [width / 3 | height / 3]
        // p3 = [width - width / 3 | height - height / 3]
        // p4 = [width | 0]
        int[] xPoints = { 0, width / 3, width - width / 3, width };
        int[] yPoints = { height, height / 3, height - height / 3, 0 };
        int nPoints = 4;

        g.drawPolyline( xPoints, yPoints, nPoints );

    }

    /**
     * draws a legendsymbol, if the SLD defines a polygon
     *
     * @param g
     *            the graphics context
     * @param ps
     *            the PolygonSymbolizer representing the drawable polygon
     * @param width
     *            the requested width of the symbol
     * @param height
     *            the requested height of the symbol
     * @throws LegendException
     *             if the parsing of the sld failes.
     */
    protected void drawPolygonLegend( Graphics2D g, PolygonSymbolizer ps, int width, int height )
                            throws LegendException {

        Position p1 = GeometryFactory.createPosition( 0, 0 );
        Position p2 = GeometryFactory.createPosition( 0, height - 1 );
        Position p3 = GeometryFactory.createPosition( width - 1, height - 1 );
        Position p4 = GeometryFactory.createPosition( width - 1, 0 );

        Position[] pos = { p1, p2, p3, p4, p1 };
        Surface surface = null;
        try {
            surface = GeometryFactory.createSurface( pos, null, null, null );
        } catch ( Exception ex ) {
            throw new LegendException( "Exception occured during the creation of the legend:\n"
                                       + "The legendsymbol for the Polygon can't be processed.\n"
                                       + "Error in creating the Surface from the Positions.\n" + ex.getMessage() );
        }

        PolygonDisplayElement pde = null;
        try {
            // Feature, Geometry, PolygonSymbolizer
            pde = DisplayElementFactory.buildPolygonDisplayElement( null, surface, ps );
        } catch ( IncompatibleGeometryTypeException igtex ) {
            throw new LegendException( "IncompatibleGeometryTypeException occured during "
                                       + "the creation of the legend:\n The legendsymbol for "
                                       + "the Polygon can't be processed.\nError in creating "
                                       + "the PolygonDisplayElement.\n" + igtex.getMessage() );
        } catch ( Exception e ) {
            throw new LegendException( "Could not create symbolizer:\n" + e.getMessage() );
        }

        Envelope envelope = GeometryFactory.createEnvelope( p1, p3, null );

        WorldToScreenTransform wtst = new WorldToScreenTransform( envelope, envelope );
        pde.paint( g, wtst, -1 );

    }

    /**
     * sets the color including an opacity
     *
     * @param g2
     *            the graphics contect as Graphics2D
     * @param color
     *            the requested color of the legend symbol
     * @param opacity
     *            the requested opacity of the legend symbol
     * @return the Graphics2D object containing color and opacity
     */
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

    /**
     * constructs a java.awt.BasicStroke for painting a LineString legend symbol.
     *
     * @param sldstroke
     *            the deegree sld stroke
     * @return a java.awt.BasicStroke
     * @throws LegendException
     *             if the sld cannot be processed
     */
    private BasicStroke getBasicStroke( org.deegree.graphics.sld.Stroke sldstroke )
                            throws LegendException {
        BasicStroke bs = null;
        try {
            float width = (float) sldstroke.getWidth( null );
            int cap = sldstroke.getLineCap( null );
            int join = sldstroke.getLineJoin( null );
            float miterlimit = 1f;
            float[] dash = sldstroke.getDashArray( null );
            float dash_phase = sldstroke.getDashOffset( null );

            bs = new BasicStroke( width, cap, join, miterlimit, dash, dash_phase );
            // return new BasicStroke((float)sldstroke.getWidth(null), sldstroke.getLineCap(null),
            // sldstroke.getLineJoin(null), 1f, sldstroke.getDashArray(null),
            // sldstroke.getDashOffset(null));

        } catch ( FilterEvaluationException ex ) {
            throw new LegendException( "FilterEvaluationException occured during the creation of the legend:\n"
                                       + "The Stroke of the element can't be processed.\n" + ex.getMessage() );
        }
        return bs;
    }

    /**
     * calculates the FontMetrics of the LegendLabel in pixels. It returns an 3-dimensional array containing [0] the
     * width, [1] the ascent and [2] the descent.
     *
     * @param label
     *            the label of the LegendElement
     * @return the 3-dimensional INT-Array contains [0] the width of the string, [1] the ascent and [2] the descent.
     */
    protected int[] calculateFontMetrics( String label ) {
        int[] fontmetrics = new int[3];

        BufferedImage bi = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
        Graphics g = bi.getGraphics();

        FontMetrics fm = g.getFontMetrics();

        if ( label != null && label.length() > 0 ) {
            fontmetrics[0] = fm.stringWidth( label );
            fontmetrics[1] = fm.getAscent();
            fontmetrics[2] = fm.getDescent();
        } else {
            fontmetrics[0] = 0;
            fontmetrics[1] = 0;
            fontmetrics[2] = 0;
            // value = 1, because of a bug, which doesn't paint the right border of the
            // polygon-symbol.
            setBufferBetweenLegendAndLabel( 1 );
        }
        g.dispose();

        return fontmetrics;
    }

    /**
     * calculates the width and height of the resulting LegendSymbol depending on the LabelPlacement
     */
    private BufferedImage calculateImage( int labelposition, int labelwidth, int ascent, int descent, int legendwidth,
                                          int legendheight, int buffer, String mime ) {
        // eliminate buffer if label width is zero, so pixel counting works for the reference
        // implementation
        if ( labelwidth == 0 ) {
            buffer = 0;
        }

        BufferedImage bi = (BufferedImage) GraphicContextFactory.createGraphicTarget(
                                                                                      mime,
                                                                                      ( legendwidth + buffer + labelwidth ),
                                                                                      legendheight );

        Graphics g = bi.getGraphics();
        if ( !( mime.equalsIgnoreCase( "image/png" ) || mime.equalsIgnoreCase( "image/gif" ) ) ) {
            g.setColor( Color.WHITE );
            g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
        }
        g.setColor( Color.BLACK );
        // TODO labelposition

        switch ( labelposition ) {
        // LP_TOPCENTER
        case 0: {
            LOG.logInfo( "The text-position LP_TOPCENTER in the legend is not "
                         + "implemented yet.\n We put the text on the right side (EAST) of " + "the legendsymbol." );
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;
        }
            // LP_TOPLEFT
        case 1: {
            LOG.logInfo( "The text-position LP_TOPLEFT in the legend is not implemented "
                         + "yet.\n We put the text on the right side (EAST) of the legendsymbol." );
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;
        }
            // LP_TOPRIGHT
        case 2: {
            LOG.logInfo( "The text-position LP_TOPRIGHT in the legend is not implemented "
                         + "yet.\n We put the text on the right side (EAST) of the legendsymbol." );
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;
        }
            // LP_RIGHT
        case 3: {
            LOG.logInfo( "The text-position LP_RIGHT in the legend is not implemented "
                         + "yet.\n We put the text on the right side (EAST) of the legendsymbol." );
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;

        }
            // LP_LEFT
        case 4: {
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;
        }
            // LP_BOTTOMCENTER
        case 5: {
            LOG.logInfo( "The text-position LP_BOTTOMCENTER in the legend is not "
                         + "implemented yet.\n We put the text on the right side (EAST) of " + "the legendsymbol." );
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;
        }
            // LP_BOTTOMRIGHT
        case 6: {
            LOG.logInfo( "The text-position LP_BOTTOMRIGHT in the legend is not "
                         + "implemented yet.\n We put the text on the right side (EAST) of " + "the legendsymbol." );
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;
        }
            // LP_BOTTOMLEFT
        case 7: {
            LOG.logInfo( "The text-position LP_BOTTOMLEFT in the legend is not implemented "
                         + "yet.\n We put the text on the right side (EAST) of the legendsymbol." );
            g.drawString( getLabel(), width + 10, height / 2 + ( ( ascent - descent ) / 2 ) );
            break;
        }
        }
        g.dispose();
        return bi;
    }

    /**
     * exports the <tt>LegendElement</tt> as </tt>BufferedImage</tt>
     *
     * @param mime
     *
     * @return the image
     * @throws LegendException
     */
    public BufferedImage exportAsImage( String mime )
                            throws LegendException {

        if ( bi == null ) {
            // calculates the fontmetrics and creates the bufferedimage
            // if getLabel() is null is checked in calculateFontMetrics!
            int[] fontmetrics = calculateFontMetrics( getLabel() );
            bi = calculateImage( getLabelPlacement(), fontmetrics[0], fontmetrics[1], fontmetrics[2], getWidth(),
                                 getHeight(), getBufferBetweenLegendAndLabel(), mime );
            Graphics g = bi.getGraphics();
            ( (Graphics2D) g ).setClip( 0, 0, getWidth(), getHeight() );
            g.setColor( Color.WHITE );
            Rule[] myrules = getRules();
            Symbolizer[] symbolizer = null;
            // determines the legendsymbol and paints it
            for ( int a = 0; a < myrules.length; a++ ) {
                symbolizer = myrules[a].getSymbolizers();

                for ( int b = 0; b < symbolizer.length; b++ ) {
                    if ( symbolizer[b] instanceof PointSymbolizer ) {
                        drawPointLegend( g, (PointSymbolizer) symbolizer[b], getWidth(), getHeight() );
                    }
                    if ( symbolizer[b] instanceof LineSymbolizer ) {
                        drawLineStringLegend( (Graphics2D) g, (LineSymbolizer) symbolizer[b], width, height );
                    }
                    if ( symbolizer[b] instanceof PolygonSymbolizer ) {
                        drawPolygonLegend( (Graphics2D) g, (PolygonSymbolizer) symbolizer[b], width, height );
                    }
                    if ( symbolizer[b] instanceof RasterSymbolizer ) {
                        // throw new LegendException("RasterSymbolizer is not implemented yet!");
                    }
                    if ( symbolizer[b] instanceof TextSymbolizer ) {
                        // throw new LegendException("TextSymbolizer is not implemented yet!");
                    }
                }

                // g.setColor(Color.black);
                // g.drawString(getLabel(), width + 10, height / 2 + ((fontmetrics[1] -
                // fontmetrics[2]) / 2));

            }
        } else {
            if ( mime.equalsIgnoreCase( "image/gif" ) || mime.equalsIgnoreCase( "image/png" ) ) {
                BufferedImage bii = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB );
                Graphics g = bii.getGraphics();
                g.drawImage( bi, 0, 0, null );
                g.dispose();
                bi = bii;
            } else if ( mime.equalsIgnoreCase( "image/jpg" ) || mime.equalsIgnoreCase( "image/jpeg" )
                        || mime.equalsIgnoreCase( "image/tif" ) || mime.equalsIgnoreCase( "image/tiff" )
                        || mime.equalsIgnoreCase( "image/bmp" ) ) {
                BufferedImage bii = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB );
                Graphics g = bii.getGraphics();
                g.drawImage( bi, 0, 0, null );
                g.dispose();
                bi = bii;
            } else if ( mime.equalsIgnoreCase( "image/svg+xml" ) ) {
                // not implemented yet
            }
        }
        return bi;
    }
}
