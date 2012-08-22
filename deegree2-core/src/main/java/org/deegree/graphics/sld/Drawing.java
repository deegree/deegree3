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

import java.util.HashMap;
import java.util.Map;

/**
 * This is the top level interface of <tt>Fill</tt> and <tt>Stroke</tt> defining the methods
 * <tt>getGraphicFill()</tt> and <tt>getCssParameters()</tt> that are common to both.
 * <p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */

public class Drawing {

    /**
     * the filler of a graphic
     */
    protected GraphicFill graphicFill = null;

    /**
     * The css parameters.
     */
    protected Map<String, Object> cssParams = null;

    /**
     * Constructs a new instance of <tt>Drawing</tt>.
     *
     * @param cssParams
     * @param graphicFill
     */
    Drawing( Map<String, Object> cssParams, GraphicFill graphicFill ) {
        this.cssParams = cssParams;
        this.graphicFill = graphicFill;
    }

    /**
     * The GraphicFill element both indicates that a stipple-fill repeated graphic will be used and
     * specifies the fill graphic.
     *
     * @return the GraphicFill-Element
     *
     */
    public GraphicFill getGraphicFill() {
        return graphicFill;
    }

    /**
     * The GraphicFill element both indicates that a stipple-fill repeated graphic will be used and
     * specifies the fill graphic.
     *
     * @param graphicFill
     *            the GraphicFill-Element
     *
     */
    public void setGraphicFill( GraphicFill graphicFill ) {
        this.graphicFill = graphicFill;
    }

    /**
     * A simple SVG/CSS2 styling parameters are given with the CssParameter element. <br>
     * This method is for technical use. The user should access the specialized methods of the
     * derived classes.
     *
     * @return the CssParameters
     */
    public Map<String, Object> getCssParameters() {
        return cssParams;
    }

    /**
     * A simple SVG/CSS2 styling parameters are given with the CssParameter element. <br>
     * This method sets CssParameters.
     *
     * @param cssParameters
     *            the CssParameters
     */
    void setCssParameters( HashMap<String, Object> cssParameters ) {
        this.cssParams = cssParameters;
    }

    /**
     * Simple SVG/CSS2 styling parameters are given with the CssParameter element. This method adds
     * a CssParameter to a given set of CssParameters.
     * <p>
     *
     * @param key
     *            the key of the object to insert
     * @param value
     *            the value of the object to insert
     */
    void addCssParameter( String key, Object value ) {
        cssParams.put( key, value );
    }

    /**
     * Simple SVG/CSS2 styling parameters are given with the CssParameter element.
     * <p>
     * This method adds a CssParameter to a given set of CssParameters.
     *
     * @param key
     *            the key of the object to remove
     */
    void removeCssParameter( Object key ) {
        cssParams.remove( key );
    }

}
