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

import org.deegree.framework.xml.Marshallable;
import org.deegree.model.filterencoding.Expression;

/**
 * Used to render a text label, according to the parameters. A missing Geometry, Label, Font, or
 * LabelPlacement element selects the default value or behavior for the element. The default Label,
 * Font, and LabelPlacement are system- dependent. Multiple Font elements may be used to specify
 * alternate fonts in order of preference in case a map server does not support the first
 * preference. A missing Halo or Fill element means that no halo or fill will be plotted,
 * respectively. The Fill is rendered over top of the Halo, and the Halo includes the interiors of
 * the font glyphs.
 * <p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */

public class TextSymbolizer extends AbstractSymbolizer implements Marshallable {

    private Fill fill = null;

    private Font font = null;

    private Halo halo = null;

    private LabelPlacement labelPlacement = null;

    private ParameterValueType label = null;

    private ParameterValueType[] bbox;

    /**
     * constructor initializing the class with the <code>TextSymbolizer</code>
     * @param geometry
     * @param label
     * @param font
     * @param labelPlacement
     * @param halo
     * @param fill
     * @param min
     * @param max
     */
    TextSymbolizer( Geometry geometry, ParameterValueType label, Font font, LabelPlacement labelPlacement, Halo halo,
                    Fill fill, double min, double max ) {
        super( geometry, "org.deegree.graphics.displayelements.LabelDisplayElement" );
        setLabel( label );
        setFont( font );
        setLabelPlacement( labelPlacement );
        setHalo( halo );
        setFill( fill );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * constructor initializing the class with the <code>TextSymbolizer</code>
     * @param geometry
     * @param responsibleClass
     * @param label
     * @param font
     * @param labelPlacement
     * @param halo
     * @param fill
     * @param min
     * @param max
     */
    TextSymbolizer( Geometry geometry, String responsibleClass, ParameterValueType label, Font font,
                    LabelPlacement labelPlacement, Halo halo, Fill fill, double min, double max ) {
        super( geometry, responsibleClass );
        setLabel( label );
        setFont( font );
        setLabelPlacement( labelPlacement );
        setHalo( halo );
        setFill( fill );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * Initializing with bounding box.
     *
     * @param geometry
     * @param text
     * @param font
     * @param halo
     * @param minx
     * @param miny
     * @param maxx
     * @param maxy
     */
    public TextSymbolizer( Geometry geometry, ParameterValueType text, Font font, Halo halo, ParameterValueType minx,
                           ParameterValueType miny, ParameterValueType maxx, ParameterValueType maxy ) {
        super( geometry, "org.deegree.graphics.displayelements.LabelDisplayElement" );
        setLabel( text );
        setFont( font );
        setHalo( halo );
        bbox = new ParameterValueType[] { minx, miny, maxx, maxy };
        setLabelPlacement( new LabelPlacement( (LinePlacement) null ) );
    }

    /**
     * @return the bounding box, or null, if not set (minx, miny, maxx, maxy)
     */
    public ParameterValueType[] getBoundingBox() {
        return bbox;
    }

    /**
     * returns the Label as a <tt>ParameterValueType</tt> to be renderd
     *
     * @return the label
     *
     */
    public ParameterValueType getLabel() {
        return label;
    }

    /**
     * returns the label as a String to be renderd
     *
     * @return the label or null, if the label is not set or not an expression
     *
     */
    public String getLabelAsString() {
        Object[] o = label.getComponents();
        for ( int i = 0; i < o.length; i++ ) {
            if ( o[i] instanceof Expression ) {
                return o[i].toString().trim();
            }
        }
        return null;
    }

    /**
     * sets the <Label>
     *
     * @param label
     *            the label
     *
     */
    public void setLabel( ParameterValueType label ) {
        this.label = label;
    }

    /**
     * Identifies a Font of a certain family, style, and size.
     *
     * @return the font
     *
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets a Font of a certain family, style, and size.
     *
     * @param font
     *            the font
     *
     */
    public void setFont( Font font ) {
        this.font = font;
    }

    /**
     * Used to position a label relative to a point or a line string. For a point, you can specify
     * the anchor point of the label and a linear displacement from the point (so that you can also
     * plot a graphic symbol at the point). For a line-string placement, you can specify a
     * perpendicular offset (so you can draw a stroke on the line).
     * <p>
     * </p>
     * MORE PARAMETERS ARE PROBABLY NEEDED HERE.
     *
     * @return the labelPlacement
     *
     */
    public LabelPlacement getLabelPlacement() {
        return labelPlacement;
    }

    /**
     * sets the <LabelPlacement>
     *
     * @param labelPlacement
     *            the labelPlacement
     *
     */
    public void setLabelPlacement( LabelPlacement labelPlacement ) {
        this.labelPlacement = labelPlacement;
    }

    /**
     * A Halo is an extension (sub-type) of a Fill and is applied to the backgrounds of font glyphs.
     * Either a Radius or a Block halo type can be used. The radius is computed from the outside
     * edge of the font glyph (or inside of "holes"). The default is a Radius of 1.0 (pixels) but if
     * no Halo is selected in a containing structure, no halo will be rendered. The default is a
     * solid white (Color "#FFFFFF") opaque halo.
     *
     * @return the halo
     *
     */
    public Halo getHalo() {
        return halo;
    }

    /**
     * sets <Halo>
     *
     * @param halo
     *            the halo
     *
     */
    public void setHalo( Halo halo ) {
        this.halo = halo;
    }

    /**
     * A Fill allows area geometries to be filled. There are two types of fills: solid-color and
     * repeated GraphicFill. In general, if a Fill element is omitted in its containing element, no
     * fill will be rendered. The default is a solid 50%-gray (color "#808080") opaque fill.
     *
     * @return the fill
     *
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * sets the <Fill>
     *
     * @param fill
     *            the fill
     *
     */
    public void setFill( Fill fill ) {
        this.fill = fill;
    }

    /**
     * exports the content of the TextSymbolizer as XML formated String
     *
     * @return xml representation of the TextSymbolizer
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<TextSymbolizer>" );
        if ( geometry != null ) {
            sb.append( ( (Marshallable) geometry ).exportAsXML() );
        }
        if ( label != null ) {
            sb.append( "<Label>" );
            sb.append( ( (Marshallable) label ).exportAsXML() );
            sb.append( "</Label>" );
        }
        if ( font != null ) {
            sb.append( ( (Marshallable) font ).exportAsXML() );
        }
        if ( labelPlacement != null ) {
            sb.append( ( (Marshallable) labelPlacement ).exportAsXML() );
        }
        if ( halo != null ) {
            sb.append( ( (Marshallable) halo ).exportAsXML() );
        }
        if ( fill != null ) {
            sb.append( ( (Marshallable) fill ).exportAsXML() );
        }
        sb.append( "</TextSymbolizer>" );

        return sb.toString();
    }
}
