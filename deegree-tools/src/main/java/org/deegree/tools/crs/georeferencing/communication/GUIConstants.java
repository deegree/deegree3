//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.tools.crs.georeferencing.communication;

import java.awt.Dimension;

/**
 * Constants used to control the gui elements.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GUIConstants {

    private GUIConstants() {

    }

    public static final String WINDOW_TITLE = " deegree3 Georeferencing Client ";

    /*
     * JMenu
     */
    public static final String MENU_EDIT = "Edit";

    public static final String MENU_FILE = "File";

    public static final String MENU_TRANSFORMATION = "Transformation";

    /*
     * JMenuItem
     */
    public static final String MENUITEM_GETMAP = "Import 2D Map";

    public static final String MENUITEM_GET_3DOBJECT = "Import 3D Object";

    public static final String MENUITEM_TRANS_POLYNOM_FIRST = "Polynomial 1";

    public static final String MENUITEM_TRANS_POLYNOM_SECOND = "Polynomial 2";

    public static final String MENUITEM_TRANS_POLYNOM_THIRD = "Polynomial 3";

    public static final String MENUITEM_TRANS_HELMERT = "Helmert";

    public static final String MENUITEM_EDIT_OPTIONS = "Options";

    /*
     * JTextField
     */
    public static final String JTEXTFIELD_COORDINATE_JUMPER = "CoordinateJumper";

    /*
     * JButton
     */
    public static final String JBUTTON_PAN = "Pan";

    public static final String JBUTTON_ZOOM_IN = "Zoom in";

    public static final String JBUTTON_ZOOM_OUT = "Zoom out";

    public static final String JBUTTON_ZOOM_COORD = "Zoom coord";

    public static final String COMPUTE_BUTTON_TEXT = "Compute";

    /*
     * Bounds
     */
    public static final Dimension FRAME_DIMENSION = new Dimension( 900, 600 );

    public static final Dimension DIM_COMPUTATION_PANEL = new Dimension( 160, 100 );

    public static final Dimension DIM_CHECKBOXLIST = new Dimension( 200, 100 );

    public static final Dimension DIM_COORDINATEJUMPER = new Dimension( 200, 100 );

    public static final Dimension DIALOG_DIMENSION = new Dimension( 500, 300 );

    public static final Dimension DIM_NAVIGATION_BUTTONS = new Dimension( 20, 20 );
}
