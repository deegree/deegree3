//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 Additional copyright notes:
 The basic version of this class was taken from the Geotools2 project
 (StyleBuilder.java): Geotools2 - OpenSource mapping toolkit
 http://geotools.org
 (C) 2002, Geotools Project Managment Committee (PMC)      

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

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.deegree.framework.util.ColorUtils;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;

/**
 * An utility class designed to easy creation of style by convinience methods.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 */
public class StyleFactory {

    /**
     * creates a <tt>ParameterValueType</tt> instance with a <tt>String</tt> as value
     * 
     * @param value
     *            value of the <tt>ParameterValueType</tt>
     * 
     * @return the ParameterValueType created
     */
    public static ParameterValueType createParameterValueType( String value ) {
        return new ParameterValueType( new Object[] { value } );
    }

    /**
     * creates a <tt>ParameterValueType</tt> instance with a <tt>int</tt> as value
     * 
     * @param value
     *            value of the <tt>ParameterValueType</tt>
     * 
     * @return the ParameterValueType created
     */
    public static ParameterValueType createParameterValueType( int value ) {
        return new ParameterValueType( new Object[] { "" + value } );
    }

    /**
     * creates a <tt>ParameterValueType</tt> instance with a <tt>String</tt> as value
     * 
     * @param value
     *            value of the <tt>ParameterValueType</tt>
     * 
     * @return the ParameterValueType created
     */
    public static ParameterValueType createParameterValueType( double value ) {
        return new ParameterValueType( new Object[] { "" + value } );
    }

    /**
     * creates a <tt>ParameterValueType</tt> instance with an array of <tt>Expression</tt> s as value
     * 
     * @param expressions
     * 
     * @return the the ParameterValueType created
     */
    public static ParameterValueType createParameterValueType( Expression[] expressions ) {
        return new ParameterValueType( expressions );
    }

    /**
     * creates a CssParameter with a name and a value
     * 
     * @param name
     *            name of the css parameter
     * @param value
     *            value of the css parameter
     * 
     * @return the CssParameter created
     */
    public static CssParameter createCssParameter( String name, String value ) {
        ParameterValueType pvt = createParameterValueType( value );
        return new CssParameter( name, pvt );
    }

    /**
     * creates a CssParameter with a name and a value
     * 
     * @param name
     *            name of the css parameter
     * @param value
     *            value of the css parameter
     * 
     * @return the CssParameter created
     */
    public static CssParameter createCssParameter( String name, int value ) {
        ParameterValueType pvt = createParameterValueType( value );
        return new CssParameter( name, pvt );
    }

    /**
     * creates a CssParameter with a name and a value
     * 
     * @param name
     *            name of the css parameter
     * @param value
     *            value of the css parameter
     * 
     * @return the CssParameter created
     */
    public static CssParameter createCssParameter( String name, double value ) {
        ParameterValueType pvt = createParameterValueType( value );
        return new CssParameter( name, pvt );
    }

    /**
     * creates a <tt>GraphicStroke</tt> from a <tt>Graphic</tt> object
     * 
     * @param graphic
     *            <tt>Graphic</tt object
     *
     * @return the GraphicStroke created
     */
    public static GraphicStroke createGraphicStroke( Graphic graphic ) {
        return new GraphicStroke( graphic );
    }

    /**
     * creates a <tt>GraphicFill</tt> from a <tt>Graphic</tt> object
     * 
     * @param graphic
     *            <tt>Graphic</tt object
     *
     * @return the GraphicFill created
     */
    public static GraphicFill createGraphicFill( Graphic graphic ) {
        return new GraphicFill( graphic );
    }

    /**
     * create a default Stroke that black, 1 pixel width, complete opaque, with round linejoin and square line cap
     * 
     * @return the Stroke created
     */
    public static Stroke createStroke() {
        return createStroke( Color.BLACK, 1, "round", "square" );
    }

    /**
     * create a default stroke with the supplied width
     * 
     * @param width
     *            the width of the line
     * 
     * @return the stroke created
     */
    public static Stroke createStroke( double width ) {
        return createStroke( Color.BLACK, width );
    }

    /**
     * Create a default stroke with the supplied color
     * 
     * @param color
     *            the color of the line
     * 
     * @return the created stroke
     */
    public static Stroke createStroke( Color color ) {
        return createStroke( color, 1 );
    }

    /**
     * create a stroke with the passed width and color
     * 
     * @param color
     *            the color of the line
     * @param width
     *            the width of the line
     * 
     * @return the created stroke
     */
    public static Stroke createStroke( Color color, double width ) {
        return createStroke( color, width, "round", "square" );
    }

    /**
     * create a stroke with color, width, linejoin type and lineCap type.
     * 
     * @param color
     *            the color of the line
     * @param width
     *            the width of the line
     * @param lineJoin
     *            the type of join to be used at points along the line
     * @param lineCap
     *            the type of cap to be used at the end of the line
     * 
     * @return the stroke created
     */
    public static Stroke createStroke( Color color, double width, String lineJoin, String lineCap ) {
        return createStroke( color, width, 1, null, lineJoin, lineCap );
    }

    /**
     * create a stroke with color, width, linejoin type and lineCap type.
     * 
     * @param color
     *            the color of the line
     * @param width
     *            the width of the line
     * @param opacity
     *            the opacity or <I>see throughness </I> of the line, 0 - is transparent, 1 is completely drawn
     * @param dashArray
     * @param lineJoin
     *            the type of join to be used at points along the line
     * @param lineCap
     *            the type of cap to be used at the end of the line
     * 
     * @return the stroke created
     */
    public static Stroke createStroke( Color color, double width, double opacity, float[] dashArray, String lineJoin,
                                       String lineCap ) {
        HashMap<String, Object> cssParams = new HashMap<String, Object>();

        CssParameter stroke = createCssParameter( "stroke", ColorUtils.toHexCode( "#", color ) );
        cssParams.put( "stroke", stroke );
        CssParameter strokeOp = createCssParameter( "stroke-opacity", opacity );
        cssParams.put( "stroke-opacity", strokeOp );
        CssParameter strokeWi = createCssParameter( "stroke-width", width );
        cssParams.put( "stroke-width", strokeWi );
        CssParameter strokeLJ = createCssParameter( "stroke-linejoin", lineJoin );
        cssParams.put( "stroke-linejoin", strokeLJ );
        CssParameter strokeCap = createCssParameter( "stroke-linecap", lineCap );
        cssParams.put( "stroke-linecap", strokeCap );

        if ( dashArray != null ) {
            String s = "";
            for ( int i = 0; i < dashArray.length - 1; i++ ) {
                s = s + dashArray[i] + ",";
            }
            s = s + dashArray[dashArray.length - 1];
            CssParameter strokeDash = createCssParameter( "stroke-dasharray", s );
            cssParams.put( "stroke-dasharray", strokeDash );
        }

        return new Stroke( cssParams, null, null );
    }

    /**
     * create a dashed line of color and width
     * 
     * @param color
     *            the color of the line
     * @param width
     *            the width of the line
     * @param dashArray
     *            an array of floats describing the length of line and spaces
     * 
     * @return the stroke created
     */
    public static Stroke createStroke( Color color, double width, float[] dashArray ) {
        HashMap<String, Object> cssParams = new HashMap<String, Object>();

        CssParameter stroke = createCssParameter( "stroke", ColorUtils.toHexCode( "#", color ) );
        cssParams.put( "stroke", stroke );
        CssParameter strokeOp = createCssParameter( "stroke-opacity", "1" );
        cssParams.put( "stroke-opacity", strokeOp );
        CssParameter strokeWi = createCssParameter( "stroke-width", width );
        cssParams.put( "stroke-width", strokeWi );
        CssParameter strokeLJ = createCssParameter( "stroke-linejoin", "mitre" );
        cssParams.put( "stroke-linejoin", strokeLJ );
        CssParameter strokeCap = createCssParameter( "stroke-linecap", "butt" );
        cssParams.put( "stroke-linecap", strokeCap );

        if ( dashArray != null ) {
            String s = "";
            for ( int i = 0; i < dashArray.length - 1; i++ ) {
                s = s + dashArray[i] + ",";
            }
            s = s + dashArray[dashArray.length - 1];
            CssParameter strokeDash = createCssParameter( "stroke-dasharray", s );
            cssParams.put( "stroke-dasharray", strokeDash );
        }

        return new Stroke( cssParams, null, null );
    }

    /**
     * create a stroke with color, width and opacity supplied
     * 
     * @param color
     *            the color of the line
     * @param width
     *            the width of the line
     * @param opacity
     *            the opacity or <I>see throughness </I> of the line, 0 - is transparent, 1 is completely drawn
     * 
     * @return the stroke created
     */
    public static Stroke createStroke( Color color, double width, double opacity ) {
        return createStroke( color, width, opacity, null, "mitre", "butt" );
    }

    /**
     * create a default fill 50% gray
     * 
     * @return the fill created
     */
    public static Fill createFill() {
        return createFill( Color.GRAY, 1d, null );
    }

    /**
     * create a fill of color
     * 
     * @param color
     *            the color of the fill
     * 
     * @return the fill created
     */
    public static Fill createFill( Color color ) {
        return createFill( color, 1d, null );
    }

    /**
     * create a fill with the supplied color and opacity
     * 
     * @param color
     *            the color to fill with
     * @param opacity
     *            the opacity of the fill 0 - transparent, 1 - completly filled
     * 
     * @return the fill created
     */
    public static Fill createFill( Color color, double opacity ) {
        return createFill( color, opacity, null );
    }

    /**
     * create a fill with color and opacity supplied and uses the graphic fill supplied for the fill
     * 
     * @param color
     *            the foreground color
     * @param opacity
     *            the opacity of the fill
     * @param fill
     *            the graphic object to use to fill the fill
     * 
     * @return the fill created
     */
    public static Fill createFill( Color color, double opacity, GraphicFill fill ) {
        HashMap<String, Object> cssParams = new HashMap<String, Object>();
        CssParameter fillCo = createCssParameter( "fill", ColorUtils.toHexCode( "#", color ) );
        cssParams.put( "fill", fillCo );
        CssParameter fillOp = createCssParameter( "fill-opacity", opacity );
        cssParams.put( "fill-opacity", fillOp );
        return new Fill( cssParams, fill );
    }

    /**
     * create the named mark
     * 
     * @param wellKnownName
     *            the wellknown name of the mark
     * 
     * @return the mark created
     */
    public static Mark createMark( String wellKnownName ) {
        return new Mark( wellKnownName, createStroke(), createFill() );
    }

    /**
     * create the named mark with the colors etc supplied
     * 
     * @param wellKnownName
     *            the well known name of the mark
     * @param fillColor
     *            the color of the mark
     * @param borderColor
     *            the outline color of the mark
     * @param borderWidth
     *            the width of the outline
     * 
     * @return the mark created
     */
    public static Mark createMark( String wellKnownName, Color fillColor, Color borderColor, double borderWidth ) {
        Stroke stroke = createStroke( borderColor, borderWidth );
        Fill fill = createFill( fillColor );
        return new Mark( wellKnownName, stroke, fill );
    }

    /**
     * create a mark with default fill (50% gray) and the supplied outline
     * 
     * @param wellKnownName
     *            the well known name of the mark
     * @param borderColor
     *            the outline color
     * @param borderWidth
     *            the outline width
     * 
     * @return the mark created
     */
    public static Mark createMark( String wellKnownName, Color borderColor, double borderWidth ) {
        Stroke stroke = createStroke( borderColor, borderWidth );
        Fill fill = createFill();
        return new Mark( wellKnownName, stroke, fill );
    }

    /**
     * create a mark of the supplied color and a default outline (black)
     * 
     * @param wellKnownName
     *            the well known name of the mark
     * @param fillColor
     *            the color of the mark
     * 
     * @return the created mark
     */
    public static Mark createMark( String wellKnownName, Color fillColor ) {
        Stroke stroke = createStroke();
        Fill fill = createFill( fillColor );
        return new Mark( wellKnownName, stroke, fill );
    }

    /**
     * create a mark with the supplied fill and stroke
     * 
     * @param wellKnownName
     *            the well known name of the mark
     * @param fill
     *            the fill to use
     * @param stroke
     *            the stroke to use
     * 
     * @return the mark created
     */
    public static Mark createMark( String wellKnownName, Fill fill, Stroke stroke ) {
        return new Mark( wellKnownName, stroke, fill );
    }

    /**
     * wrapper for stylefactory method
     * 
     * @param uri
     *            the uri of the image
     * @param format
     *            mime type of the image
     * 
     * @return the external graphic
     * @throws MalformedURLException
     */
    public static ExternalGraphic createExternalGraphic( String uri, String format )
                            throws MalformedURLException {
        return createExternalGraphic( new URL( uri ), format );
    }

    /**
     * wrapper for stylefactory method
     * 
     * @param url
     *            the url of the image
     * @param format
     *            mime type of the image
     * 
     * @return the external graphic
     */
    public static ExternalGraphic createExternalGraphic( java.net.URL url, String format ) {
        return createExternalGraphic( url, format, null );
    }

    /**
     * wrapper for stylefactory method
     * 
     * @param url
     *            the url of the image
     * @param format
     *            mime type of the image
     * @param title
     *            the title of the externalGraphic
     * @return the external graphic
     */
    public static ExternalGraphic createExternalGraphic( java.net.URL url, String format, String title ) {
        return new ExternalGraphic( format, url, title );
    }

    /**
     * creates a graphic object
     * 
     * @param externalGraphic
     *            an external graphic to use if displayable
     * @param mark
     *            a mark to use
     * @param opacity -
     *            the opacity of the graphic
     * @param size -
     *            the size of the graphic
     * @param rotation -
     *            the rotation from the top of the page of the graphic
     * 
     * @return the graphic created
     */
    public static Graphic createGraphic( ExternalGraphic externalGraphic, Mark mark, double opacity, double size,
                                         double rotation ) {

        Object[] mae = null;
        if ( externalGraphic != null && mark != null ) {
            mae = new Object[] { externalGraphic, mark };
        } else if ( externalGraphic != null ) {
            mae = new Object[] { externalGraphic };
        } else if ( mark != null ) {
            mae = new Object[] { mark };
        }
        ParameterValueType op_pvt = createParameterValueType( opacity );
        ParameterValueType sz_pvt = createParameterValueType( size );
        ParameterValueType ro_pvt = createParameterValueType( rotation );
        return new Graphic( mae, op_pvt, sz_pvt, ro_pvt );
    }

    /**
     * wrapper round Stylefactory Method
     * 
     * @return the default pointplacement
     */
    public static PointPlacement createPointPlacement() {
        return new PointPlacement();
    }

    /**
     * wrapper round Stylefactory Method
     * 
     * @param anchorX -
     *            the X coordinate
     * @param anchorY -
     *            the Y coordinate
     * @param rotation -
     *            the rotaion of the label
     * 
     * @return the pointplacement created
     */
    public static PointPlacement createPointPlacement( double anchorX, double anchorY, double rotation ) {
        ParameterValueType pvt1 = createParameterValueType( anchorX );
        ParameterValueType pvt2 = createParameterValueType( anchorY );
        ParameterValueType[] anchorPoint = new ParameterValueType[] { pvt1, pvt2 };
        ParameterValueType rot = createParameterValueType( rotation );
        return new PointPlacement( anchorPoint, null, rot, false );
    }

    /**
     * wrapper round Stylefactory Method
     * 
     * @param anchorX -
     *            the X coordinate
     * @param anchorY -
     *            the Y coordinate
     * @param displacementX -
     *            the X distance from the anchor
     * @param displacementY -
     *            the Y distance from the anchor
     * @param rotation -
     *            the rotaion of the label
     * 
     * @return the pointplacement created
     */
    public static PointPlacement createPointPlacement( double anchorX, double anchorY, double displacementX,
                                                       double displacementY, double rotation ) {
        ParameterValueType pvt1 = createParameterValueType( anchorX );
        ParameterValueType pvt2 = createParameterValueType( anchorY );
        ParameterValueType[] anchorPoint = new ParameterValueType[] { pvt1, pvt2 };

        ParameterValueType pvt3 = createParameterValueType( displacementX );
        ParameterValueType pvt4 = createParameterValueType( displacementY );
        ParameterValueType[] displacement = new ParameterValueType[] { pvt3, pvt4 };

        ParameterValueType rot = createParameterValueType( rotation );
        return new PointPlacement( anchorPoint, displacement, rot, false );
    }

    /**
     * @param anchorX -
     *            the X coordinate
     * @param anchorY -
     *            the Y coordinate
     * @param displacementX -
     *            the X distance from the anchor
     * @param displacementY -
     *            the Y distance from the anchor
     * @param rotation -
     *            the rotaion of the label
     * @param auto -
     *            auto positioning of the label
     * 
     * @return the pointplacement created
     */
    public static PointPlacement createPointPlacement( double anchorX, double anchorY, double displacementX,
                                                       double displacementY, double rotation, boolean auto ) {
        ParameterValueType pvt1 = createParameterValueType( anchorX );
        ParameterValueType pvt2 = createParameterValueType( anchorY );
        ParameterValueType[] anchorPoint = new ParameterValueType[] { pvt1, pvt2 };

        ParameterValueType pvt3 = createParameterValueType( displacementX );
        ParameterValueType pvt4 = createParameterValueType( displacementY );
        ParameterValueType[] displacement = new ParameterValueType[] { pvt3, pvt4 };

        ParameterValueType rot = createParameterValueType( rotation );
        return new PointPlacement( anchorPoint, displacement, rot, auto );
    }

    /**
     * creates a <tt>LinePlacement</tt> with a user defined distance between the labels and the lines. A positive
     * value indicates a position above the line, a negative value indicates a position below. The line width is asumed
     * to be 2 pixel and the gap between the labels is set to factor 10 of the label width.
     * 
     * @param offset -
     *            the distance between the line and the label
     * 
     * @return the LinePlacement created
     */
    public static LinePlacement createLinePlacement( double offset ) {

        ParameterValueType perpendicularOffset = createParameterValueType( offset );
        ParameterValueType lineWidth = createParameterValueType( 2 );
        ParameterValueType gap = createParameterValueType( 10 );

        return new LinePlacement( perpendicularOffset, lineWidth, gap );
    }

    /**
     * creates a <tt>LinePlacement</tt> with a relative position of the label according to the line the lines. The
     * line width is asumed to be 2 pixel and the gap between the labels is set to factor 10 of the label width.
     * 
     * @param position
     *            of the label relative to the line
     * 
     * @return the LinePlacement created
     */
    public static LinePlacement createLinePlacement( String position ) {

        ParameterValueType perpendicularOffset = createParameterValueType( position );
        ParameterValueType lineWidth = createParameterValueType( 2 );
        ParameterValueType gap = createParameterValueType( 10 );

        return new LinePlacement( perpendicularOffset, lineWidth, gap );
    }

    /**
     * creates a <tt>LinePlacement</tt> with a user defined distance between the labels and the lines. A positive
     * value indicates a position above the line, a negative value indicates a position below.
     * 
     * @param offset -
     *            the distance between the line and the label
     * @param lineWidth -
     *            assumed lineWidth
     * @param gap -
     *            gap between the labels measured in label width
     * 
     * @return the LinePlacement created
     */
    public static LinePlacement createLinePlacement( double offset, double lineWidth, int gap ) {

        ParameterValueType perpendicularOffset = createParameterValueType( offset );
        ParameterValueType lineWidth_ = createParameterValueType( lineWidth );
        ParameterValueType gap_ = createParameterValueType( gap );

        return new LinePlacement( perpendicularOffset, lineWidth_, gap_ );
    }

    /**
     * creates a <tt>LinePlacement</tt> with a user defined distance between the labels and the lines. A positive
     * value indicates a position above the line, a negative value indicates a position below.
     * 
     * @param position -
     *            relative position of the label to the line
     * @param lineWidth -
     *            assumed lineWidth
     * @param gap -
     *            gap between the labels measured in label width
     * 
     * @return the LinePlacement created
     */
    public static LinePlacement createLinePlacement( String position, double lineWidth, int gap ) {

        ParameterValueType perpendicularOffset = createParameterValueType( position );
        ParameterValueType lineWidth_ = createParameterValueType( lineWidth );
        ParameterValueType gap_ = createParameterValueType( gap );

        return new LinePlacement( perpendicularOffset, lineWidth_, gap_ );
    }

    /**
     * creates a label placement that is orientated on a line
     * 
     * @param linePlacement
     *            description of the line where the lable will be orientated on
     * @return created LabelPlacement
     */
    public static LabelPlacement createLabelPlacement( LinePlacement linePlacement ) {
        return new LabelPlacement( linePlacement );
    }

    /**
     * creates a label placement that is orientated on a point
     * 
     * @param pointPlacement
     *            description of the point where the lable will be orientated on
     * @return created LabelPlacement
     */
    public static LabelPlacement createLabelPlacement( PointPlacement pointPlacement ) {
        return new LabelPlacement( pointPlacement );
    }

    /**
     * create a geotools font object from a java font
     * 
     * @param font -
     *            the font to be converted
     * 
     * @return - the deegree sld font
     */
    public static Font createFont( java.awt.Font font ) {
        return createFont( font.getFamily(), font.isItalic(), font.isBold(), font.getSize() );
    }

    /**
     * create font of supplied family and size
     * 
     * @param fontFamily -
     *            the font family
     * @param fontSize -
     *            the size of the font in points
     * 
     * @return the font object created
     */
    public static Font createFont( String fontFamily, double fontSize ) {
        return createFont( fontFamily, false, false, fontSize );
    }

    /**
     * create font of supplied family, size and weight/style
     * 
     * @param fontFamily -
     *            the font family
     * @param italic -
     *            should the font be italic?
     * @param bold -
     *            should the font be bold?
     * @param fontSize -
     *            the size of the font in points
     * 
     * @return the new font object
     */
    public static Font createFont( String fontFamily, boolean italic, boolean bold, double fontSize ) {
        HashMap<String, CssParameter> cssParams = new HashMap<String, CssParameter>();

        cssParams.put( "font-family", createCssParameter( "font-family", fontFamily ) );
        cssParams.put( "font-size", createCssParameter( "font-size", "" + fontSize ) );
        if ( bold ) {
            cssParams.put( "font-weight", createCssParameter( "font-weight", "bold" ) );
        } else {
            cssParams.put( "font-weight", createCssParameter( "font-weight", "normal" ) );
        }
        if ( italic ) {
            cssParams.put( "font-style", createCssParameter( "font-style", "italic" ) );
        } else {
            cssParams.put( "font-style", createCssParameter( "font-style", "normal" ) );
        }

        return new Font( cssParams );
    }

    /**
     * wrapper round StyleFactory method to create default halo
     * 
     * @return the new halo
     */
    public static Halo createHalo() {
        return createHalo( createFill(), createStroke(), -1 );
    }

    /**
     * wrapper round StyleFactory method to create halo
     * 
     * @param color -
     *            the color of the halo
     * @param radius -
     *            the radius of the halo use a value <= 0 for rectangle
     * 
     * @return the new halo
     */
    public static Halo createHalo( Color color, double radius ) {
        return createHalo( createFill( color ), createStroke(), radius );
    }

    /**
     * wrapper round StyleFactory method to create halo
     * 
     * @param fillColor -
     *            the fill color of the halo
     * @param opacity -
     *            the opacity of the halo fill 0 - transparent 1 - solid
     * @param strokeColor -
     *            the stroke color of the halo
     * @param radius -
     *            the radius of the halo use a value <= 0 for rectangle
     * 
     * @return the new halo
     */
    public static Halo createHalo( Color fillColor, double opacity, Color strokeColor, double radius ) {
        Fill fill = createFill( fillColor, opacity );
        Stroke stroke = createStroke( strokeColor );
        return createHalo( fill, stroke, radius );
    }

    /**
     * wrapper round StyleFactory method to create halo
     * 
     * @param fill -
     *            the fill of the halo
     * @param stroke -
     *            the stroke of the halo
     * @param radius -
     *            the radius of the halo use a value <= 0 for rectangle
     * 
     * @return the new halo
     */
    public static Halo createHalo( Fill fill, Stroke stroke, double radius ) {
        ParameterValueType pvt = null;
        if ( radius > 0 ) {
            pvt = createParameterValueType( radius );
        }
        return new Halo( pvt, fill, stroke );
    }

    /**
     * create a default line symboliser
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer() {
        return createLineSymbolizer( createStroke( 1 ), null );
    }

    /**
     * create a new line symbolizer
     * 
     * @param width
     *            the width of the line
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer( double width ) {
        return createLineSymbolizer( createStroke( width ), null );
    }

    /**
     * create a LineSymbolizer
     * 
     * @param color -
     *            the color of the line
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer( Color color ) {
        return createLineSymbolizer( createStroke( color ), null );
    }

    /**
     * create a LineSymbolizer
     * 
     * @param color -
     *            the color of the line
     * @param width -
     *            the width of the line
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer( Color color, double width ) {
        return createLineSymbolizer( createStroke( color, width ), null );
    }

    /**
     * create a LineSymbolizer
     * 
     * @param color -
     *            the color of the line
     * @param width -
     *            the width of the line
     * @param geometryPropertyName -
     *            the name of the geometry to be drawn
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer( Color color, double width, PropertyPath geometryPropertyName ) {
        return createLineSymbolizer( createStroke( color, width ), geometryPropertyName );
    }

    /**
     * create a LineSymbolizer
     * 
     * @param stroke -
     *            the stroke to be used to draw the line
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer( Stroke stroke ) {
        return createLineSymbolizer( stroke, null );
    }

    /**
     * create a LineSymbolizer
     * 
     * @param stroke -
     *            the stroke to be used to draw the line
     * @param geometryPropertyName -
     *            the name of the geometry to be drawn
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer( Stroke stroke, PropertyPath geometryPropertyName ) {
        return createLineSymbolizer( stroke, geometryPropertyName, 0, Double.MAX_VALUE );
    }

    /**
     * create a LineSymbolizer
     * 
     * @param stroke -
     *            the stroke to be used to draw the line
     * @param geometryPropertyName -
     *            the name of the geometry to be drawn
     * @param min
     *            min scale denominator
     * @param max
     *            max scale denominator
     * 
     * @return the new line symbolizer
     */
    public static LineSymbolizer createLineSymbolizer( Stroke stroke, PropertyPath geometryPropertyName, double min,
                                                       double max ) {
        Geometry geom = null;
        if ( geometryPropertyName != null ) {
            geom = new Geometry( geometryPropertyName, null );
        }
        return new LineSymbolizer( stroke, geom, min, max );
    }

    /**
     * create a default polygon symbolizer
     * 
     * @return the new polygon symbolizer
     */
    public static PolygonSymbolizer createPolygonSymbolizer() {
        return createPolygonSymbolizer( createStroke(), createFill() );
    }

    /**
     * create a polygon symbolizer
     * 
     * @param fillColor -
     *            the color to fill the polygon
     * 
     * @return the new polygon symbolizer
     */
    public static PolygonSymbolizer createPolygonSymbolizer( Color fillColor ) {
        return createPolygonSymbolizer( createStroke(), createFill( fillColor ) );
    }

    /**
     * create a polygon symbolizer
     * 
     * @param fillColor -
     *            the color to fill the polygon
     * @param borderColor -
     *            the outline color of the polygon
     * @param borderWidth -
     *            the width of the outline
     * 
     * @return the new polygon symbolizer
     */
    public static PolygonSymbolizer createPolygonSymbolizer( Color fillColor, Color borderColor, double borderWidth ) {
        return createPolygonSymbolizer( createStroke( borderColor, borderWidth ), createFill( fillColor ) );
    }

    /**
     * create a polygon symbolizer
     * 
     * @param borderColor -
     *            the outline color of the polygon
     * @param borderWidth -
     *            the width of the outline
     * 
     * @return the new polygon symbolizer
     */
    public static PolygonSymbolizer createPolygonSymbolizer( Color borderColor, double borderWidth ) {
        Stroke stroke = createStroke( borderColor, borderWidth );
        return createPolygonSymbolizer( stroke, createFill() );
    }

    /**
     * create a polygon symbolizer
     * 
     * @param stroke -
     *            the stroke to use to outline the polygon
     * @param fill -
     *            the fill to use to color the polygon
     * 
     * @return the new polygon symbolizer
     */
    public static PolygonSymbolizer createPolygonSymbolizer( Stroke stroke, Fill fill ) {
        return createPolygonSymbolizer( stroke, fill, null );
    }

    /**
     * create a polygon symbolizer
     * 
     * @param stroke -
     *            the stroke to use to outline the polygon
     * @param fill -
     *            the fill to use to color the polygon
     * @param geometryPropertyName -
     *            the name of the geometry to be drawn
     * 
     * @return the new polygon symbolizer
     */
    public static PolygonSymbolizer createPolygonSymbolizer( Stroke stroke, Fill fill, PropertyPath geometryPropertyName ) {
        return createPolygonSymbolizer( stroke, fill, geometryPropertyName, 0, Double.MAX_VALUE );
    }

    /**
     * create a polygon symbolizer
     * 
     * @param stroke -
     *            the stroke to use to outline the polygon
     * @param fill -
     *            the fill to use to color the polygon
     * @param geometryPropertyName -
     *            the name of the geometry to be drawn
     * @param min
     *            min scale denominator
     * @param max
     *            max scale denominator
     * 
     * @return the new polygon symbolizer
     */
    public static PolygonSymbolizer createPolygonSymbolizer( Stroke stroke, Fill fill,
                                                             PropertyPath geometryPropertyName, double min, double max ) {
        Geometry geom = null;
        if ( geometryPropertyName != null ) {
            geom = new Geometry( geometryPropertyName, null );
        }
        return new PolygonSymbolizer( fill, stroke, geom, min, max );
    }

    /**
     * create a default point symbolizer
     * 
     * @return the new point symbolizer
     */
    public static PointSymbolizer createPointSymbolizer() {
        Graphic graphic = createGraphic( null, null, 1, 5, 0 );
        return createPointSymbolizer( graphic );
    }

    /**
     * create a point symbolizer
     * 
     * @param graphic -
     *            the graphic object to draw at the point
     * 
     * @return the new point symbolizer
     */
    public static PointSymbolizer createPointSymbolizer( Graphic graphic ) {
        return createPointSymbolizer( graphic, null );
    }

    /**
     * create a point symbolizer
     * 
     * @param graphic -
     *            the graphic object to draw at the point
     * @param geometryPropertyName -
     *            the name of the geometry to be drawn
     * 
     * @return the new point symbolizer
     */
    public static PointSymbolizer createPointSymbolizer( Graphic graphic, PropertyPath geometryPropertyName ) {
        return createPointSymbolizer( graphic, geometryPropertyName, 0, Double.MAX_VALUE );
    }

    /**
     * create a point symbolizer
     * 
     * @param graphic -
     *            the graphic object to draw at the point
     * @param geometryPropertyName -
     *            the name of the geometry to be drawn
     * @param min
     *            min scale denominator
     * @param max
     *            max scale denominator
     * 
     * @return the new point symbolizer
     */
    public static PointSymbolizer createPointSymbolizer( Graphic graphic, PropertyPath geometryPropertyName,
                                                         double min, double max ) {
        Geometry geom = null;
        if ( geometryPropertyName != null ) {
            geom = new Geometry( geometryPropertyName, null );
        }
        return new PointSymbolizer( graphic, geom, min, max );
    }

    /**
     * create a textsymbolizer
     * 
     * @param color
     *            the color of the text
     * @param font
     *            the font to use
     * @param attributeName
     *            the attribute to use for the label
     * @param labelPlacement
     * 
     * @return the new textsymbolizer
     * 
     */
    public static TextSymbolizer createTextSymbolizer( Color color, Font font, String attributeName,
                                                       LabelPlacement labelPlacement ) {
        ParameterValueType label = createParameterValueType( attributeName );
        Fill fill = createFill( color );
        Halo halo = createHalo();
        return createTextSymbolizer( null, label, font, labelPlacement, halo, fill, 0, Double.MAX_VALUE );
    }

    /**
     * create a textsymbolizer
     * 
     * @param geometryPropertyName
     *            geometry assigned to the TextSymbolizer
     * @param attribute
     *            attribute to draw/print
     * @param labelPlacement
     *            defines the placement of the text
     * 
     * @return the new textsymbolizer
     * 
     */
    public static TextSymbolizer createTextSymbolizer( PropertyPath geometryPropertyName, String attribute,
                                                       LabelPlacement labelPlacement ) {
        Font font = createFont( java.awt.Font.decode( "Sans Serif" ) );
        return createTextSymbolizer( geometryPropertyName, attribute, font, labelPlacement, createHalo(), createFill(),
                                     0, Double.MAX_VALUE );
    }

    /**
     * create a textsymbolizer
     * 
     * @param geometryPropertyName
     *            geometry assigned to the TextSymbolizer
     * @param attribute
     *            attribute to draw/print
     * @param font
     *            font to use for the text
     * @param labelPlacement
     *            defines the placement of the text
     * @param halo
     *            halo/backgroud of the text
     * @param fill
     *            color, opacity of the text
     * @param min
     *            min scale denominator
     * @param max
     *            max scale denominator
     * 
     * @return the new textsymbolizer
     * 
     */
    public static TextSymbolizer createTextSymbolizer( PropertyPath geometryPropertyName, String attribute, Font font,
                                                       LabelPlacement labelPlacement, Halo halo, Fill fill, double min,
                                                       double max ) {
        Geometry geom = null;
        if ( geometryPropertyName != null ) {
            geom = new Geometry( geometryPropertyName, null );
        }
        ParameterValueType label = createParameterValueType( attribute );
        return createTextSymbolizer( geom, label, font, labelPlacement, halo, fill, min, max );
    }

    /**
     * create a textsymbolizer
     * 
     * @param geometry
     *            geometry assigned to the TextSymbolizer
     * @param label
     *            attribute to draw/print
     * @param font
     *            font to use for the text
     * @param labelPlacement
     *            defines the placement of the text
     * @param halo
     *            halo/backgroud of the text
     * @param fill
     *            color, opacity of the text
     * @param min
     *            min scale denominator
     * @param max
     *            max scale denominator
     * 
     * @return the new textsymbolizer
     * 
     */
    public static TextSymbolizer createTextSymbolizer( Geometry geometry, ParameterValueType label, Font font,
                                                       LabelPlacement labelPlacement, Halo halo, Fill fill, double min,
                                                       double max ) {
        return new TextSymbolizer( geometry, label, font, labelPlacement, halo, fill, min, max );
    }

    /**
     * Throws an UnsupportedOperationException, but should create a textsymbolizer which doesn't change
     * 
     * @param color
     *            the color of the text
     * @param font
     *            the font to use
     * @param label
     *            the label to use
     * 
     * @return the new textsymbolizer
     */
    public static TextSymbolizer createStaticTextSymbolizer( Color color, Font font, String label ) {
        throw new UnsupportedOperationException( "method createStaticTextSymbolizer is not implemented yet" );
    }

    /**
     * Throws an UnsupportedOperationException, but should create a textsymbolizer which doesn't change
     * 
     * @param color
     *            the color of the text
     * @param fonts
     *            an array of fonts to use from the first to last
     * @param label
     *            the label to use
     * 
     * @return the new textsymbolizer
     */
    public static TextSymbolizer createStaticTextSymbolizer( Color color, Font[] fonts, String label ) {
        throw new UnsupportedOperationException( "method createStaticTextSymbolizer is not implemented yet" );
    }

    /**
     * create a simple styling rule
     * 
     * @param symbolizer -
     *            the symbolizer to use
     * 
     * @return the new rule
     */
    public static Rule createRule( Symbolizer symbolizer ) {
        return createRule( symbolizer, 0, Double.MAX_VALUE );
    }

    /**
     * reate a simple styling rule
     * 
     * @param symbolizers -
     *            an array of symbolizers to use
     * 
     * @return the new rule
     */
    public static Rule createRule( Symbolizer[] symbolizers ) {
        return createRule( symbolizers, 0, Double.MAX_VALUE );
    }

    /**
     * create a simple styling rule, see the SLD Spec for more details of scaleDenominators
     * 
     * @param symbolizer -
     *            the symbolizer to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new rule
     */
    public static Rule createRule( Symbolizer symbolizer, double minScaleDenominator, double maxScaleDenominator ) {
        return createRule( new Symbolizer[] { symbolizer }, minScaleDenominator, maxScaleDenominator );
    }

    /**
     * create a simple styling rule, see the SLD Spec for more details of scaleDenominators
     * 
     * @param symbolizers -
     *            an array of symbolizers to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new rule
     */
    public static Rule createRule( Symbolizer[] symbolizers, double minScaleDenominator, double maxScaleDenominator ) {
        return createRule( symbolizers, "default", "default", "default", minScaleDenominator, maxScaleDenominator );
    }

    /**
     * create a simple styling rule, see the SLD Spec for more details of scaleDenominators
     * 
     * @param symbolizers -
     *            an array of symbolizers to use
     * @param name -
     *            name of the rule
     * @param title -
     *            title of the rule
     * @param abstract_ -
     *            text describing throws rule
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new rule
     */
    public static Rule createRule( Symbolizer[] symbolizers, String name, String title, String abstract_,
                                   double minScaleDenominator, double maxScaleDenominator ) {
        return createRule( symbolizers, name, title, abstract_, null, null, false, minScaleDenominator,
                           maxScaleDenominator );
    }

    /**
     * create a complex styling rule, see the SLD Spec for more details of scaleDenominators
     * 
     * @param symbolizers -
     *            an array of symbolizers to use
     * @param name -
     *            name of the rule
     * @param title -
     *            title of the rule
     * @param abstract_ -
     *            text describing throws rule
     * @param legendGraphic
     * @param filter -
     *            filter to use with the rule
     * @param elseFilter -
     *            true if the passed is an ElseFilter (see SLD spec)
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new rule
     */
    public static Rule createRule( Symbolizer[] symbolizers, String name, String title, String abstract_,
                                   LegendGraphic legendGraphic, Filter filter, boolean elseFilter,
                                   double minScaleDenominator, double maxScaleDenominator ) {
        return new Rule( symbolizers, name, title, abstract_, legendGraphic, filter, elseFilter, minScaleDenominator,
                         maxScaleDenominator );
    }

    /**
     * create a Feature type styler
     * 
     * @param symbolizer -
     *            the symbolizer to use
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( Symbolizer symbolizer ) {
        return createFeatureTypeStyle( null, symbolizer, 0, Double.MAX_VALUE );
    }

    /**
     * create a Feature type styler see the SLD Spec for more details of scaleDenominators
     * 
     * @param symbolizer -
     *            the symbolizer to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( Symbolizer symbolizer, double minScaleDenominator,
                                                           double maxScaleDenominator ) {
        return createFeatureTypeStyle( null, symbolizer, minScaleDenominator, maxScaleDenominator );
    }

    /**
     * create a Feature type styler see the SLD Spec for more details of scaleDenominators
     * 
     * @param symbolizers -
     *            an array of symbolizers to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( Symbolizer[] symbolizers, double minScaleDenominator,
                                                           double maxScaleDenominator ) {
        return createFeatureTypeStyle( null, symbolizers, minScaleDenominator, maxScaleDenominator );
    }

    /**
     * create a Feature type styler
     * 
     * @param featureTypeStyleName -
     *            name for the feature type styler
     * @param symbolizer -
     *            the symbolizer to use
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( String featureTypeStyleName, Symbolizer symbolizer ) {
        return createFeatureTypeStyle( featureTypeStyleName, symbolizer, 0, Double.MAX_VALUE );
    }

    /**
     * create a Feature type styler
     * 
     * @param featureTypeStyleName -
     *            name for the feature type styler
     * @param symbolizers -
     *            an array of symbolizers to use
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( String featureTypeStyleName, Symbolizer[] symbolizers ) {
        return createFeatureTypeStyle( featureTypeStyleName, symbolizers, 0, Double.MAX_VALUE );
    }

    /**
     * create a Feature type styler see the SLD Spec for more details of scaleDenominators
     * 
     * @param featureTypeStyleName -
     *            name for the feature type styler
     * @param symbolizer -
     *            the symbolizer to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( String featureTypeStyleName, Symbolizer symbolizer,
                                                           double minScaleDenominator, double maxScaleDenominator ) {
        return createFeatureTypeStyle( featureTypeStyleName, new Symbolizer[] { symbolizer }, minScaleDenominator,
                                       maxScaleDenominator );
    }

    /**
     * create a Feature type styler see the SLD Spec for more details of scaleDenominators
     * 
     * @param featureTypeStyleName -
     *            name for the feature type styler
     * @param symbolizers -
     *            an array of symbolizers to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( String featureTypeStyleName, Symbolizer[] symbolizers,
                                                           double minScaleDenominator, double maxScaleDenominator ) {
        Rule rule = createRule( symbolizers, minScaleDenominator, maxScaleDenominator );

        return createFeatureTypeStyle( featureTypeStyleName, rule );
    }

    /**
     * create a Feature type styler
     * 
     * @param rule -
     *            rule contained in the featureTypeStyle
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( Rule rule ) {

        return createFeatureTypeStyle( new Rule[] { rule } );
    }

    /**
     * create a Feature type styler
     * 
     * @param rules -
     *            rules contained in the featureTypeStyle
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( Rule[] rules ) {

        return createFeatureTypeStyle( null, rules );
    }

    /**
     * create a Feature type styler
     * 
     * @param featureTypeStyleName -
     *            name for the feature type styler
     * @param rule -
     *            rule contained in the featureTypeStyle
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( String featureTypeStyleName, Rule rule ) {

        return createFeatureTypeStyle( featureTypeStyleName, new Rule[] { rule } );
    }

    /**
     * create a Feature type styler
     * 
     * @param featureTypeStyleName -
     *            name for the feature type styler
     * @param rules -
     *            rules contained in the featureTypeStyle
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( String featureTypeStyleName, Rule[] rules ) {

        return createFeatureTypeStyle( featureTypeStyleName, null, null, null, rules );
    }

    /**
     * create a Feature type styler
     * 
     * @param featureTypeStyleName -
     *            name for the feature type styler
     * @param featureTypeName -
     *            name of the feature type the Feature type style shall be assigned to
     * @param title -
     *            title of the FeatureTypeStyle
     * @param abstract_ -
     *            text describing the FeatureTypeStyle
     * @param rules -
     *            rules contained in the featureTypeStyle
     * 
     * @return the new feature type styler
     */
    public static FeatureTypeStyle createFeatureTypeStyle( String featureTypeStyleName, String title, String abstract_,
                                                           String featureTypeName, Rule[] rules ) {

        return new FeatureTypeStyle( featureTypeStyleName, title, abstract_, featureTypeName, null, rules );
    }

    /**
     * create a new style
     * 
     * @param symbolizer -
     *            the symbolizer to use
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle( Symbolizer symbolizer ) {
        return createStyle( null, symbolizer, 0, Double.MAX_VALUE );
    }

    /**
     * create a new style with name 'default'
     * 
     * @param symbolizer -
     *            the symbolizer to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle( Symbolizer symbolizer, double minScaleDenominator,
                                             double maxScaleDenominator ) {
        return createStyle( "default", symbolizer, minScaleDenominator, maxScaleDenominator );
    }

    /**
     * create a new style
     * 
     * @param name -
     *            the name of the style
     * @param symbolizer -
     *            the symbolizer to use
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle( String name, Symbolizer symbolizer ) {
        return createStyle( name, symbolizer, 0, Double.MAX_VALUE );
    }

    /**
     * create a new style
     * 
     * @param name -
     *            the name of the style
     * @param symbolizer -
     *            the symbolizer to use
     * @param minScaleDenominator -
     *            the minimim scale to draw the feature at
     * @param maxScaleDenominator -
     *            the maximum scale to draw the feature at
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle( String name, Symbolizer symbolizer, double minScaleDenominator,
                                             double maxScaleDenominator ) {
        // create the feature type style
        FeatureTypeStyle fts = createFeatureTypeStyle( name, symbolizer, minScaleDenominator, maxScaleDenominator );

        return createStyle( name, null, null, fts );
    }

    /**
     * create a style
     * 
     * @param name -
     *            the name of the style
     * @param featureTypeName -
     *            name of the feature type the Feature type style shall be assigned to
     * @param title -
     *            title of the FeatureTypeStyle
     * @param abstract_ -
     *            text describing the FeatureTypeStyle
     * @param rules -
     *            rules contained in the featureTypeStyle
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle( String name, String title, String abstract_, String featureTypeName,
                                             Rule[] rules ) {

        FeatureTypeStyle fts = createFeatureTypeStyle( name, title, abstract_, featureTypeName, rules );
        return createStyle( name, null, null, fts );
    }

    /**
     * create a new style
     * 
     * @param name -
     *            the name of the style
     * @param title -
     *            title of the style
     * @param abstract_ -
     *            text describing the style
     * @param featureTypeStyle -
     *            featureTypeStyle
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle( String name, String title, String abstract_,
                                             FeatureTypeStyle featureTypeStyle ) {
        return createStyle( name, title, abstract_, new FeatureTypeStyle[] { featureTypeStyle } );
    }

    /**
     * create a new style
     * 
     * @param name -
     *            the name of the style
     * @param title -
     *            title of the style
     * @param abstract_ -
     *            text describing the style
     * @param featureTypeStyles -
     *            featureTypeStyle
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle( String name, String title, String abstract_,
                                             FeatureTypeStyle[] featureTypeStyles ) {
        return new UserStyle( name, title, abstract_, false, featureTypeStyles );
    }

    /**
     * creates a style with name 'defaultPoint' for rendering point geometries
     * 
     * @param wellKnownName
     *            the well known name of the mark
     * @param fillColor
     *            the color of the mark
     * @param borderColor
     *            the outline color of the mark
     * @param borderWidth
     *            the width of the outline
     * @param opacity -
     *            the opacity of the graphic
     * @param size -
     *            the size of the graphic
     * @param rotation -
     *            the rotation from the top of the page of the graphic
     * @param min -
     *            the minimim scale to draw the feature at
     * @param max -
     *            the maximum scale to draw the feature at
     * 
     * @return the style created
     */
    public static AbstractStyle createPointStyle( String wellKnownName, Color fillColor, Color borderColor,
                                                  double borderWidth, double opacity, double size, double rotation,
                                                  double min, double max ) {
        Mark mark = createMark( wellKnownName, fillColor, borderColor, borderWidth );
        Graphic graphic = createGraphic( null, mark, opacity, size, rotation );
        Symbolizer symbolizer = createPointSymbolizer( graphic, null, min, max );
        return createStyle( "defaultPoint", symbolizer );
    }

    /**
     * creates a style with name 'defaultLine' for rendering line geometries
     * 
     * @param color
     *            the line color
     * @param width
     *            the width of the line
     * @param opacity -
     *            the opacity of the line
     * @param min -
     *            the minimim scale to draw the feature at
     * @param max -
     *            the maximum scale to draw the feature at
     * 
     * @return the style created
     */
    public static AbstractStyle createLineStyle( Color color, double width, double opacity, double min, double max ) {
        Stroke stroke = createStroke( color, width, opacity );
        Symbolizer symbolizer = createLineSymbolizer( stroke, null, min, max );
        return createStyle( "defaultLine", symbolizer );
    }

    /**
     * creates a style with name 'defaultPolygon' for rendering polygon geometries
     * 
     * @param fillColor -
     *            the fill color of the polygon
     * @param fillOpacity -
     *            the fill opacity of the polygon
     * @param strokeColor -
     *            the line color
     * @param strokeWidth -
     *            the width of the line
     * @param strokeOpacity -
     *            the opacity of the line
     * @param min -
     *            the minimim scale to draw the feature at
     * @param max -
     *            the maximum scale to draw the feature at
     * 
     * @return the style created
     */
    public static AbstractStyle createPolygonStyle( Color fillColor, double fillOpacity, Color strokeColor,
                                                    double strokeWidth, double strokeOpacity, double min, double max ) {
        Stroke stroke = createStroke( strokeColor, strokeWidth, strokeOpacity );
        Fill fill = createFill( fillColor, fillOpacity );
        Symbolizer symbolizer = createPolygonSymbolizer( stroke, fill, null, min, max );
        return createStyle( "defaultPolygon", symbolizer );
    }

    /**
     * Throws an UnsupportedOperationException, but should create a style with name 'defaultPoint' for rendering point
     * geometries. The style contains 1..n rules depending on the value range and the number of steps within it. So it
     * is possible to create a style that creates different rendering depending on the value of one feature attribute.
     * <p>
     * there will be a linear interpolation between colors, size and width of the first and the last rule considering
     * the number of passed steps (rules)
     * 
     * @param wellKnownNames -
     *            list of well known names of the mark. the first field will be assigned to the starting rule the last
     *            to the ending rule.
     * @param startFillColor -
     *            the color of the mark of the first rule
     * @param endFillColor -
     *            the color of the mark of the last rule
     * @param startBorderColor -
     *            the outline color of the mark of the first rule
     * @param endBorderColor -
     *            the outline color of the mark of the last rule
     * @param startBorderWidth -
     *            the width of the outline of the first rule
     * @param endBorderWidth -
     *            the width of the outline of the last rule
     * @param opacity -
     *            the opacity of the graphic
     * @param startSize -
     *            the size of the graphic of the first rule
     * @param endSize -
     *            the size of the graphic of the last rule
     * @param rotation -
     *            the rotation from the top of the page of the graphic
     * @param min -
     *            the minimim scale to draw the feature at
     * @param max -
     *            the maximum scale to draw the feature at
     * @param featurePropertyName -
     *            name of the feature property that determines the selection of the rule for drawing
     * @param numberOfSteps -
     *            number of steps used for the interpolation between first and last value. It is identical with the
     *            number of rules that will be created.
     * 
     * @return the style created
     */
    public static AbstractStyle createPointStyle( String[] wellKnownNames, Color startFillColor, Color endFillColor,
                                                  Color startBorderColor, Color endBorderColor,
                                                  double startBorderWidth, double endBorderWidth, double opacity,
                                                  double startSize, double endSize, double rotation, double min,
                                                  double max, String featurePropertyName, int numberOfSteps ) {
        throw new UnsupportedOperationException( "method createPointStyle is not implemented yet" );
    }

    /**
     * Throws an UnsupportedOperationException, but should create a style with name 'defaultLine' for rendering line geometries. The style contains 1..n rules depending on
     * the value range and the number of steps within it. So it is possible to create a style that creates different
     * rendering depending on the value of one feature attribute.
     * <p>
     * there will be a linear interpolation between colors, size and width of the first and the last rule considering
     * the number of passed steps (rules)
     * 
     * @param startColor -
     *            the color of the first rule
     * @param endColor -
     *            the color of the last rule
     * @param startWidth -
     *            the width of the line of the first rule
     * @param endWidth -
     *            the width of the line of the last rule
     * @param opacity -
     *            the opacity of the graphic
     * @param min -
     *            the minimim scale to draw the feature at
     * @param max -
     *            the maximum scale to draw the feature at
     * @param featurePropertyName -
     *            name of the feature property that determines the selection of the rule for drawing
     * @param numberOfSteps -
     *            number of steps used for the interpolation between first and last value. It is identical with the
     *            number of rules that will be created.
     * 
     * @return the style created
     */
    public static AbstractStyle createLineStyle( Color startColor, Color endColor, double startWidth, double endWidth,
                                                 double opacity, double min, double max, String featurePropertyName,
                                                 int numberOfSteps ) {
        throw new UnsupportedOperationException( "method createLineStyle is not implemented yet" );
    }

    /**
     * Throws an UnsupportedOperationException, but should create a style with name 'defaultPoint' for rendering point geometries. The style contains 1..n rules depending
     * on the value range and the number of steps within it. So it is possible to create a style that creates different
     * rendering depending on the value of one feature attribute.
     * <p>
     * there will be a linear interpolation between colors, size and width of the first and the last rule considering
     * the number of passed steps (rules)
     * 
     * @param startFillColor -
     *            the fill color of the first rule
     * @param endFillColor -
     *            the fill color of the last rule
     * @param fillOpacity -
     *            the opacity of the fill
     * @param startStrokeColor -
     *            the line color of the first rule
     * @param endStrokeColor -
     *            the line color of the last rule
     * @param startStrokeWidth -
     *            the width of the outline of the first rule
     * @param endStrokeWidth -
     *            the width of the outline of the last rule
     * @param strokeOpacity -
     *            the opacity of the outline
     * @param min -
     *            the minimim scale to draw the feature at
     * @param max -
     *            the maximum scale to draw the feature at
     * @param featurePropertyName -
     *            name of the feature property that determines the selection of the rule for drawing
     * @param numberOfSteps -
     *            number of steps used for the interpolation between first and last value. It is identical with the
     *            number of rules that will be created.
     * 
     * @return the style created
     */
    public static AbstractStyle createPolygonStyle( Color startFillColor, Color endFillColor, double fillOpacity,
                                                    Color startStrokeColor, Color endStrokeColor,
                                                    double startStrokeWidth, double endStrokeWidth,
                                                    double strokeOpacity, double min, double max,
                                                    String featurePropertyName, int numberOfSteps ) {
        throw new UnsupportedOperationException( "method createPolygonStyle is not implemented yet" );
    }

    /**
     * create a new default style
     * 
     * @return the new style
     */
    public static AbstractStyle createStyle() {
        return null;
    }

}