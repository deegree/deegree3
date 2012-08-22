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
package org.deegree.portal.portlet.jsp.taglib;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.deegree.framework.util.StringTools;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class IGeoPortalPanButtonTag extends TagSupport {

    /**
     *
     */
    private static final long serialVersionUID = -8722131286601433700L;

    private String direction = null;

    private String imageBase = "./igeoportal/images/";

    private int width;

    private int height;

    /**
     * @return direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     *
     * @param direction
     */
    public void setDirection( String direction ) {
        this.direction = direction;
    }

    /**
     *
     * @return imageBase
     */
    public String getImageBase() {
        return imageBase;
    }

    /**
     *
     * @param imageBase
     */
    public void setImageBase( String imageBase ) {
        this.imageBase = imageBase;
    }

    @Override
    public int doStartTag()
                            throws JspException {

        ArrayList list = (ArrayList) pageContext.getRequest().getAttribute( "PANBUTTONS" );
        String portletID = (String) pageContext.getRequest().getAttribute( "PORTLETID" );
        portletID = StringTools.replace( portletID, "-", "", true );
        if ( list.contains( direction ) ) {
            try {
                pageContext.getOut().flush();

                String img = StringTools.concat( 300, "<a href=\"javascript:mapWindowPortlet", portletID, '.',
                                                 "pan( '$2', 25 );\" ><img src='./igeoportal/images/$1Arrow.gif' ",
                                                 "border='0'" );
                StringBuffer sb = new StringBuffer( img );
                if ( width > 0 ) {
                    sb.append( " width='" ).append( width ).append( "' " );
                }
                if ( height > 0 ) {
                    sb.append( " height='" ).append( height ).append( "' " );
                }

                // sb.append( " title='pan $1'/></a>" );

                String pan = IGeoPortalL10nTag.getMessage( "MapWindow.pan" + direction );

                sb.append( " title='" ).append( pan ).append( "'/></a>" );

                img = sb.toString();
                if ( direction.equals( "NORTH" ) ) {
                    img = StringTools.replace( img, "$1", "north", true );
                    img = StringTools.replace( img, "$2", "N", true );
                } else if ( direction.equals( "NORTHEAST" ) ) {
                    img = StringTools.replace( img, "$1", "northEast", true );
                    img = StringTools.replace( img, "$2", "NE", true );
                } else if ( direction.equals( "NORTHWEST" ) ) {
                    img = StringTools.replace( img, "$1", "northWest", true );
                    img = StringTools.replace( img, "$2", "NW", true );
                } else if ( direction.equals( "WEST" ) ) {
                    img = StringTools.replace( img, "$1", "west", true );
                    img = StringTools.replace( img, "$2", "W", true );
                } else if ( direction.equals( "EAST" ) ) {
                    img = StringTools.replace( img, "$1", "east", true );
                    img = StringTools.replace( img, "$2", "E", true );
                } else if ( direction.equals( "SOUTH" ) ) {
                    img = StringTools.replace( img, "$1", "south", true );
                    img = StringTools.replace( img, "$2", "S", true );
                } else if ( direction.equals( "SOUTHEAST" ) ) {
                    img = StringTools.replace( img, "$1", "southEast", true );
                    img = StringTools.replace( img, "$2", "SE", true );
                } else if ( direction.equals( "SOUTHWEST" ) ) {
                    img = StringTools.replace( img, "$1", "southWest", true );
                    img = StringTools.replace( img, "$2", "SW", true );
                }

                JspWriter pw = pageContext.getOut();
                pw.print( img.toString() );
            } catch ( IOException e ) {
                e.printStackTrace();
                String message = "Error processing name '" + direction + "'.";
                try {
                    pageContext.getOut().print( message );
                } catch ( java.io.IOException ioe ) {
                    //nottin
                }
            }
        }

        return SKIP_BODY;
    }

    /**
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width
     */
    public void setWidth( int width ) {
        this.width = width;
    }

    /**
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height
     */
    public void setHeight( int height ) {
        this.height = height;
    }
}
