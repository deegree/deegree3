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
 * The LegendGraphic element gives an optional explicit Graphic symbol to be displayed in a legend
 * for this rule.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LegendGraphic implements Marshallable {

    /**
     *
     */
    private Graphic graphic = null;

    /**
     * default constructor
     */
    LegendGraphic() {
        //nothing
    }

    /**
     * constructor initializing the class with the <LegendGraphic>
     * @param graphic
     */
    LegendGraphic( Graphic graphic ) {
        setGraphic( graphic );
    }

    /**
     * A Graphic is a graphic symbol with an inherent shape, color(s), and possibly size. A graphic
     * can be very informally defined as a little picture and can be of either a raster or
     * vector-graphic source type. The term graphic is used since the term symbol is similar to
     * symbolizer which is used in a different context in SLD.
     *
     * @return graphic
     *
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * sets the <Graphic>
     *
     * @param graphic
     *
     */
    public void setGraphic( Graphic graphic ) {
        this.graphic = graphic;
    }

    /**
     * exports the content of the Font as XML formated String
     *
     * @return xml representation of the Font
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<LegendGraphic>" );
        sb.append( ( (Marshallable) graphic ).exportAsXML() );
        sb.append( "</LegendGraphic>" );

        return sb.toString();
    }
}
