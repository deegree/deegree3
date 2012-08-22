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

/**
 * Used to render an interior "fill" and an outlining "stroke" for a polygon or other 2D-area geometry. If a point or
 * line are used, the fill is ignored and the stroke is used as described in the LineSymbol. A missing Geometry element
 * selects the default geometry. A missing Fill or Stroke element means that there will be no fill or stroke plotted,
 * respectively. The contained elements are in the conceptual order of their being used and plotted using the
 * "painters model", where the Fill will be rendered first, and then the Stroke will be rendered on top of the Fill.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */

public class PolygonSymbolizer extends AbstractSymbolizer implements Marshallable {

    private Fill fill = null;

    private Stroke stroke = null;

    /**
     * Creates a new PolygonSymbolizer object.
     */
    public PolygonSymbolizer() {
        super( null, "org.deegree.graphics.displayelements.PolygonDisplayElement" );
        setFill( new Fill() );

        Stroke stroke = new Stroke();
        setStroke( stroke );
    }

    /**
     * constructor initializing the class with the <PolygonSymbolizer>
     *
     * @param fill
     * @param stroke
     * @param geometry
     * @param min
     * @param max
     */
    public PolygonSymbolizer( Fill fill, Stroke stroke, Geometry geometry, double min, double max ) {
        super( geometry, "org.deegree.graphics.displayelements.PolygonDisplayElement" );
        setFill( fill );
        setStroke( stroke );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * constructor initializing the class with the <PolygonSymbolizer>
     *
     * @param fill
     * @param stroke
     * @param geometry
     * @param responsibleClass
     * @param min
     * @param max
     */
    PolygonSymbolizer( Fill fill, Stroke stroke, Geometry geometry, String responsibleClass, double min, double max ) {
        super( geometry, responsibleClass );
        setFill( fill );
        setStroke( stroke );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * A Fill allows area geometries to be filled. There are two types of fills: solid-color and repeated GraphicFill.
     * In general, if a Fill element is omitted in its containing element, no fill will be rendered. The default is a
     * solid 50%-gray (color "#808080") opaque fill.
     *
     * @return the fill of the polygon
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * sets the <Fill>
     *
     * @param fill
     *            the fill of the polygon
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
     * @return the stroke of the polygon
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * sets the <Stroke>
     *
     * @param stroke
     *            the stroke of the polygon
     */
    public void setStroke( Stroke stroke ) {
        this.stroke = stroke;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "scale constraint:  >=" + minDenominator + " AND <" + maxDenominator + "\n" );
        sb.append( "<PolygonSymbolizer>\n" );

        if ( getGeometry() != null ) {
            sb.append( getGeometry() ).append( "\n" );
        }

        if ( getFill() != null ) {
            sb.append( getFill() ).append( "\n" );
        }

        if ( getStroke() != null ) {
            sb.append( getStroke() ).append( "\n" );
        }

        sb.append( "</PolygonSymbolizer>\n" );

        return sb.toString();
    }

    /**
     * exports the content of the PolygonSymbolizer as XML formated String
     *
     * @return xml representation of the PolygonSymbolizer
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<PolygonSymbolizer>" );
        if ( geometry != null ) {
            sb.append( ( (Marshallable) geometry ).exportAsXML() );
        }
        if ( fill != null ) {
            sb.append( ( (Marshallable) fill ).exportAsXML() );
        }
        if ( stroke != null ) {
            sb.append( ( (Marshallable) stroke ).exportAsXML() );
        }
        sb.append( "</PolygonSymbolizer>" );

        return sb.toString();
    }
}
