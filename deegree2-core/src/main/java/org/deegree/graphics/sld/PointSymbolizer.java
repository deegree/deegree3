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
 * Used to render a "graphic" at a point. If a line-string or polygon geometry is used with this
 * symbol, then the semantic is to use the centroid of the geometry, or any similar representative
 * point. The meaning of the contained elements are discussed with the element definitions below. If
 * the Geometry element is omitted, then the "default" geometry for the feature type is used. (Many
 * feature types will have only one geometry attribute.) If the Graphic element is omitted, then
 * nothing will be plotted.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */

public class PointSymbolizer extends AbstractSymbolizer implements Marshallable {

    private Graphic graphic = null;

    /**
     * Creates a new PointSymbolizer object.
     */
    public PointSymbolizer() {
        super( null, "org.deegree.graphics.displayelements.PointDisplayElement" );
        Stroke stroke = new Stroke();
        Fill fill = new Fill();
        Mark mark = new Mark( "square", stroke, fill );
        graphic = StyleFactory.createGraphic( null, mark, 1, 5, 0 );
    }

    /**
     * constructor initializing the class with the <PointSymbolizer>
     * @param graphic
     * @param geometry
     * @param min
     * @param max
     */
    PointSymbolizer( Graphic graphic, Geometry geometry, double min, double max ) {
        super( geometry, "org.deegree.graphics.displayelements.PointDisplayElement" );

        if ( graphic == null ) {
            graphic = new Graphic();
        }

        setGraphic( graphic );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * constructor initializing the class with the <PointSymbolizer>
     * @param graphic
     * @param geometry
     * @param responsibleClass
     * @param min
     * @param max
     */
    PointSymbolizer( Graphic graphic, Geometry geometry, String responsibleClass, double min, double max ) {
        super( geometry, responsibleClass );

        if ( graphic == null ) {
            graphic = new Graphic();
        }

        setGraphic( graphic );
        setMinScaleDenominator( min );
        setMaxScaleDenominator( max );
    }

    /**
     * A Graphic is a "graphic symbol" with an inherent shape, color, and size. Graphics can either
     * be referenced from an external URL in a common format (such as GIF or SVG) or may be derived
     * from a Mark. Multiple external URLs may be referenced with the semantic that they all provide
     * the same graphic in different formats. The "hot spot" to use for rendering at a point or the
     * start and finish handle points to use for rendering a graphic along a line must either be
     * inherent in the external format or are system- dependent. The default size of an image format
     * (such as GIF) is the inherent size of the image. The default size of a format without an
     * inherent size is 16 pixels in height and the corresponding aspect in width. If a size is
     * specified, the height of the graphic will be scaled to that size and the corresponding aspect
     * will be used for the width. The default if neither an ExternalURL nor a Mark is specified is
     * to use the default Mark with a size of 6 pixels. The size is in pixels and the rotation is in
     * degrees clockwise, with 0 (default) meaning no rotation. In the case that a Graphic is
     * derived from a font-glyph Mark, the Size specified here will be used for the final rendering.
     * Allowed CssParameters are "opacity", "size", and "rotation".
     *
     * @return the graphic of the point
     *
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * sets the <Graphic>
     *
     * @param graphic
     *            the graphic of the point
     *
     */
    public void setGraphic( Graphic graphic ) {
        this.graphic = graphic;
    }

    /**
     * exports the content of the PointSymbolizer as XML formated String
     *
     * @return xml representation of the PointSymbolizer
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<PointSymbolizer>" );
        if ( geometry != null ) {
            sb.append( ( (Marshallable) geometry ).exportAsXML() );
        }
        if ( graphic != null ) {
            sb.append( ( (Marshallable) graphic ).exportAsXML() );
        }
        sb.append( "</PointSymbolizer>" );

        return sb.toString();
    }
}
