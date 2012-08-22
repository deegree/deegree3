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
 * The GraphicFill element both indicates that a stipple-fill repeated graphic will be used and
 * specifies the fill graphic.
 * <p>
 * </p>
 * A graphic can be defined very informally as a little picture. The appearance of the graphic is
 * defined with the embedded Graphic element. Additional parameters for the GraphicFill may be
 * provided in the future to provide more control the exact style of filling.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GraphicFill implements Marshallable {

    /**
     *
     */
    private Graphic graphic = null;

    /**
     * default constructor
     */
    GraphicFill() {
        //nottin
    }

    /**
     * constructor initializing the class with the GraphicFill
     * @param graphic
     */
    GraphicFill( Graphic graphic ) {
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
     * sets <Graphic>
     *
     * @param graphic
     *
     */
    public void setGraphic( Graphic graphic ) {
        this.graphic = graphic;
    }

    /**
     * exports the content of the GraphicFill as XML formated String
     *
     * @return xml representation of the GraphicFill
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<GraphicFill>" );
        sb.append( ( (Marshallable) graphic ).exportAsXML() );
        sb.append( "</GraphicFill>" );

        return sb.toString();
    }

}
