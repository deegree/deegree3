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
package org.deegree.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.NumberFormat;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface ScaleBar {

    /**
     * A constant signaling no scale
     */
    public static final int L_NONE = -1;

    /**
     * the scale
     */
    public static final int L_SCALE = 0;

    /**
     * the default scale denominator
     */
    public static final int L_SCALEDENOMINATOR = 1;

    /**
     * will paint the scale bar to the passed graphic context
     *
     * @param g
     *            graphic context
     */
    void paint( Graphics g );

    /**
     * sets the type of the label above the scale bar
     *
     * @param labelType
     *            lable type
     */
    void setTopLabel( int labelType );

    /**
     * sets the type of the label below the scale bar
     *
     * @param labelType
     *            lable type
     */
    void setBottomLabel( int labelType );

    /**
     * sets the scale as defined in the OGC WMS 1.1.1 specification. Scale is defined as the
     * diagonal size of a pixel in the center of a map measured in meter. The setting of the scale
     * will affect the value of the scale denominator
     *
     * @param scale
     *            map scale
     */
    void setScale( double scale );

    /**
     * sets the scale denominator for the scale bar. The scale denominator is the scale expression
     * as we know it for printed maps (e.g. 1:10000 1:5000). The passed value is expressed in
     * meters. The setting of the scale denominator will affect the value of the scale
     *
     * @param scaleDen
     *            scale denominator value
     */
    void setScaleDenominator( double scaleDen );

    /**
     * sets the units the scale and the scale denominater will be expressed at. Settings other than
     * meter will cause that the passed values for scale and scale denominater will be recalculated
     * for painting. it depends on the implementation what units are supported.
     *
     * @param units
     *            name units (meter, miles, feet etc.)
     */
    void setUnits( String units );

    /**
     * sets the front color of the scale bar
     *
     * @param color
     */
    void setBarColor( Color color );

    /**
     * sets the label color of the scale bar
     *
     * @param color
     */
    void setLabelColor( Color color );

    /**
     * sets the background color of the scale bar
     *
     * @param color
     */
    void setBackgroundColor( Color color );

    /**
     * sets the style of the scale bar. default style is |--------| the list of known styles depends
     * on the implementation
     *
     * @param style
     *            style name
     */
    void setStyle( String style );

    /**
     * sets the font for label rendering
     *
     * @param font
     *            awt font object
     */
    void setFont( Font font );

    /**
     * sets the format for scale/scaleDen
     *
     * @param numberFormat
     *            a NumberFormat object
     */
    void setNumberFormat( NumberFormat numberFormat );

}
