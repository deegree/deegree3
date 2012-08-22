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
package org.deegree.portal.context;

import java.util.ArrayList;

/**
 * this class encapsulates the description of the front end of a GUI setting up on a web map
 * context. this is a deegree specific form of description. beside the general elements inherited
 * from AbstractFrontend three additional elements are offered:
 * <ul>
 * <li>commonJS: a list of javascript files containing objects and methods to be used by more than
 * one module of the GUI
 * <li>buttons: a javascript file containing a associative array (Map) for the used buttons
 * <li>style: css-style file
 * </ul>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class JSPFrontend extends AbstractFrontend {
    private ArrayList<String> commonJS = new ArrayList<String>();

    private String buttons = null;

    private String style = null;

    private String header = null;

    private String footer = null;

    /**
     * Creates a new Frontend object.
     *
     * @param controller
     *            name of the controlle JSP file
     * @param style
     *            name of the css style file
     * @param buttons
     *            name of the js-file containing a llist (Map) of the used buttons (images)
     * @param commonJS
     *            list of js-files with common used object and methods
     * @param west
     *            gui description for west area
     * @param east
     *            gui description for east area
     * @param south
     *            gui description for south area
     * @param north
     *            gui description for north area
     * @param center
     *            gui description for central area
     * @param header
     *            JSP header page/file
     * @param footer
     *            JSP footer page/file
     */
    public JSPFrontend( String controller, GUIArea west, GUIArea east, GUIArea south, GUIArea north, GUIArea center,
                        String style, String buttons, String[] commonJS, String header, String footer ) {
        super( controller, west, east, south, north, center );
        setStyle( style );
        setButtons( buttons );
        setCommonJS( commonJS );
        setHeader( header );
        setFooter( footer );
    }

    /**
     * returns the name of the css style file to be used by the gui
     *
     * @return the name of the css style file to be used by the gui
     */
    public String getStyle() {
        return style;
    }

    /**
     * returns the name of a javascript file containing a associative array (Map) for the used
     * buttons (images). If this is <tt>null</tt> HTML standard buttons shall be used in the gui
     *
     * @return name of a javascript file containing a associative array for the used buttons
     */
    public String getButtons() {
        return buttons;
    }

    /**
     * returns a list of javascript files containing objects and methods to be used by more than one
     * module of the GUI
     *
     * @return list of javascript files common for all modules
     */
    public String[] getCommonJS() {
        return commonJS.toArray( new String[commonJS.size()] );
    }

    /**
     * sets the name of the css style file to be used by the gui
     *
     * @param style
     */
    public void setStyle( String style ) {
        this.style = style;
    }

    /**
     * sets the name of a javascript file containing a associative array (Map) for the used buttons
     * (images). If this is <tt>null</tt> HTML standard buttons shall be used in the gui
     *
     * @param buttons
     */
    public void setButtons( String buttons ) {
        this.buttons = buttons;
    }

    /**
     * sets a list of javascript files containing objects and methods to be used by more than one
     * module of the GUI
     *
     * @param commonJS
     */
    public void setCommonJS( String[] commonJS ) {
        if ( commonJS != null ) {
            for ( int i = 0; i < commonJS.length; i++ ) {
                this.commonJS.add( commonJS[i] );
            }
        }
    }

    /**
     * adds the name of a javascript file containing objects and methods to be used by more than one
     * module of the GUI
     *
     * @param commonJS
     */
    public void addCommonJS( String commonJS ) {
        this.commonJS.add( commonJS );
    }

    /**
     * removes the name of a javascript file containing objects and methods to be used by more than
     * one module of the GUI
     *
     * @param commonJS
     */
    public void removeCommonJS( String commonJS ) {
        this.commonJS.remove( commonJS );
    }

    /**
     * returns the name of the header JSP page/file. If the returned value is null, no header shall
     * be used
     *
     * @return name of the header JSP page/file
     */
    public String getHeader() {
        return header;
    }

    /**
     * sets the name of the header JSP page/file
     *
     * @param header
     */
    public void setHeader( String header ) {
        this.header = header;
    }

    /**
     * returns the name of the footer JSP page/file. If the returned value is null, no footer shall
     * be used
     *
     * @return name of the footer JSP page/file
     */
    public String getFooter() {
        return footer;
    }

    /**
     * sets the name of the footer JSP page/file
     *
     * @param footer
     */
    public void setFooter( String footer ) {
        this.footer = footer;
    }

}
