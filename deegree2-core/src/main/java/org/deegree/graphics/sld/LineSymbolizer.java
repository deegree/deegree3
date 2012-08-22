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
 * Used to render a "stroke" along a linear geometry. If a point geometry is used, it should be
 * interpreted as a line of zero length and two end caps. If a polygon is used, then its closed
 * outline is used as the line string (with no end caps). A missing Geometry element selects the
 * default geometry. A missing Stroke element means that nothing will be plotted.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */

public class LineSymbolizer extends AbstractSymbolizer implements Marshallable {

    private Stroke stroke = null;

    /**
     * Creates a new LineSymbolizer object.
     */
    public LineSymbolizer() {
        super( null, "org.deegree.graphics.displayelements.LineStringDisplayElement" );

        Stroke stroke = new Stroke();
        setStroke( stroke );
    }

    /**
     * constructor initializing the class with the LineSymbolizer
     * @param stroke
     * @param geometry
     * @param min
     * @param max
     */
    LineSymbolizer( Stroke stroke, Geometry geometry, double min, double max ) {
        super( geometry, "org.deegree.graphics.displayelements.LineStringDisplayElement" );
        setStroke( stroke );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * constructor initializing the class with the LineSymbolizer
     * @param stroke
     * @param geometry
     * @param responsibleClass
     * @param min
     * @param max
     */
    LineSymbolizer( Stroke stroke, Geometry geometry, String responsibleClass, double min, double max ) {
        super( geometry, responsibleClass );
        // super( geometry, "org.deegree.graphics.displayelements.LineStringDisplayElement" );
        setStroke( stroke );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * A Stroke allows a string of line segments (or any linear geometry) to be rendered. There are
     * three basic types of strokes: solid Color, GraphicFill (stipple), and repeated GraphicStroke.
     * A repeated graphic is plotted linearly and has its graphic symbol bended around the curves of
     * the line string. The default is a solid black line (Color "#000000").
     *
     * @return the Stroke
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * sets the <Stroke>
     *
     * @param stroke
     *            the Stroke
     *
     */
    public void setStroke( Stroke stroke ) {
        this.stroke = stroke;
    }

    /**
     * exports the content of the LineSymbolizer as XML formated String
     *
     * @return xml representation of the LineSymbolizer
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<LineSymbolizer>" );
        if ( geometry != null ) {
            sb.append( ( (Marshallable) geometry ).exportAsXML() );
        }
        if ( stroke != null ) {
            sb.append( ( (Marshallable) stroke ).exportAsXML() );
        }
        sb.append( "</LineSymbolizer>" );

        return sb.toString();
    }
}
