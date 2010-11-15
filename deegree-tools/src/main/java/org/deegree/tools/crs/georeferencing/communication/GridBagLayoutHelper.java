//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * 
 * This <Code>DeegreeGridBagLayout</Code> encapsulates helpful constructors to specify a GridBagLayout.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class GridBagLayoutHelper {

    /**
     * Here are all the attributes encapsulated that are needed to specify a GridBagLayout.
     * 
     * @param cont
     *            the Container which should be affected
     * @param gbl
     *            the GridBagLayout
     * @param c
     *            the Component that should be inserted into the container
     * @param x
     *            specifies the row at the upper left of the component
     * @param y
     *            specifies the column at the upper left of the component
     * @param width
     *            specifies the number of columns in the component's display area
     * @param height
     *            specifies the number of rows in the component's display area
     * @param fill
     *            when the component's display area is larger than the component's requested size <li>NONE</li><li>
     *            HORIZONTIAL</li><li>VERTICAL</li><li>BOTH</li>
     * @param iPadx
     *            specifies the internal padding: how much to add to the size of the component in horizontal direction
     * @param iPady
     *            specifies the internal padding: how much to add to the size of the component in vertical direction
     * @param insets
     *            specifies the external padding of the component -- the minimum amount of space between the component
     *            and the edges of its display area
     * @param anchor
     *            used when the component is smaller than its display area to determine where (within the area) to place
     *            the component
     * @param weightx
     *            weights are used to determine how to distribute space among columns
     * @param weighty
     *            weights are used to determine how to distribute space among rows
     */
    public static void addComponent( Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
                                     int height, int fill, int iPadx, int iPady, Insets insets, int anchor,
                                     double weightx, double weighty ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.ipadx = iPadx;
        gbc.ipady = iPady;
        gbc.insets = insets;
        gbc.anchor = anchor;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints( c, gbc );
        cont.add( c );

    }

    /**
     * Here are the attributes encapsulated that are needed to specify a GridBagLayout.
     * 
     * @param cont
     *            the Container which should be affected
     * @param gbl
     *            the GridBagLayout
     * @param c
     *            the Component that should be inserted into the container
     * @param x
     *            specifies the row at the upper left of the component
     * @param y
     *            specifies the column at the upper left of the component
     * @param width
     *            specifies the number of columns in the component's display area
     * @param height
     *            specifies the number of rows in the component's display area
     * @param fill
     *            when the component's display area is larger than the component's requested size <li>NONE</li><li>
     *            HORIZONTIAL</li><li>VERTICAL</li><li>BOTH</li>
     * @param iPadx
     *            specifies the internal padding: how much to add to the size of the component in horizontal direction
     * @param iPady
     *            specifies the internal padding: how much to add to the size of the component in vertical direction
     * @param insets
     *            specifies the external padding of the component -- the minimum amount of space between the component
     *            and the edges of its display area
     * @param weightx
     *            weights are used to determine how to distribute space among columns
     * @param weighty
     *            weights are used to determine how to distribute space among rows
     */
    public static void addComponent( Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
                                     int height, int fill, int iPadx, int iPady, Insets insets, double weightx,
                                     double weighty ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.ipadx = iPadx;
        gbc.ipady = iPady;
        gbc.insets = insets;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints( c, gbc );
        cont.add( c );

    }

    /**
     * Here are the attributes encapsulated that are needed to specify a GridBagLayout.
     * 
     * @param cont
     *            the Container which should be affected
     * @param gbl
     *            the GridBagLayout
     * @param c
     *            the Component that should be inserted into the container
     * @param x
     *            specifies the row at the upper left of the component
     * @param y
     *            specifies the column at the upper left of the component
     * @param width
     *            specifies the number of columns in the component's display area
     * @param height
     *            specifies the number of rows in the component's display area
     * @param fill
     *            when the component's display area is larger than the component's requested size <li>NONE</li><li>
     *            HORIZONTIAL</li><li>VERTICAL</li><li>BOTH</li>
     * @param iPadx
     *            specifies the internal padding: how much to add to the size of the component in horizontal direction
     * @param iPady
     *            specifies the internal padding: how much to add to the size of the component in vertical direction
     * @param weightx
     *            weights are used to determine how to distribute space among columns
     * @param weighty
     *            weights are used to determine how to distribute space among rows
     */
    public static void addComponent( Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
                                     int height, int fill, int iPadx, int iPady, double weightx, double weighty ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.ipadx = iPadx;
        gbc.ipady = iPady;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints( c, gbc );
        cont.add( c );

    }

    /**
     * Here are the attributes encapsulated that are needed to specify a GridBagLayout.
     * 
     * @param cont
     *            the Container which should be affected
     * @param gbl
     *            the GridBagLayout
     * @param c
     *            the Component that should be inserted into the container
     * @param x
     *            specifies the row at the upper left of the component
     * @param y
     *            specifies the column at the upper left of the component
     * @param width
     *            specifies the number of columns in the component's display area
     * @param height
     *            specifies the number of rows in the component's display area
     * @param fill
     *            when the component's display area is larger than the component's requested size <li>NONE</li><li>
     *            HORIZONTIAL</li><li>VERTICAL</li><li>BOTH</li>
     * @param weightx
     *            weights are used to determine how to distribute space among columns
     * @param weighty
     *            weights are used to determine how to distribute space among rows
     */
    public static void addComponent( Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
                                     int height, int fill, double weightx, double weighty ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints( c, gbc );
        cont.add( c );

    }

    /**
     * Here are the attributes encapsulated that are needed to specify a GridBagLayout. The fill-attribute here is set
     * to BOTH by default.
     * 
     * @param cont
     *            the Container which should be affected
     * @param gbl
     *            the GridBagLayout
     * @param c
     *            the Component that should be inserted into the container
     * @param x
     *            specifies the row at the upper left of the component
     * @param y
     *            specifies the column at the upper left of the component
     * @param width
     *            specifies the number of columns in the component's display area
     * @param height
     *            specifies the number of rows in the component's display area
     * @param weightx
     *            weights are used to determine how to distribute space among columns
     * @param weighty
     *            weights are used to determine how to distribute space among rows
     */
    public static void addComponent( Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
                                     int height, double weightx, double weighty ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints( c, gbc );
        cont.add( c );

    }

    /**
     * Here are the attributes encapsulated that are needed to specify a GridBagLayout. The fill-attribute here is set
     * to BOTH by default.
     * 
     * @param cont
     *            the Container which should be affected
     * @param gbl
     *            the GridBagLayout
     * @param c
     *            the Component that should be inserted into the container
     * @param x
     *            specifies the row at the upper left of the component
     * @param y
     *            specifies the column at the upper left of the component
     * @param width
     *            specifies the number of columns in the component's display area
     * @param height
     *            specifies the number of rows in the component's display area
     */
    public static void addComponent( Container cont, GridBagLayout gbl, Component c, int x, int y, int width, int height ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbl.setConstraints( c, gbc );
        cont.add( c );

    }

    /**
     * Here are all the attributes encapsulated that are needed to specify a GridBagLayout. The fill-attribute here is
     * set to BOTH by default.
     * 
     * @param cont
     *            the Container which should be affected
     * @param gbl
     *            the GridBagLayout
     * @param c
     *            the Component that should be inserted into the container
     * @param x
     *            specifies the row at the upper left of the component
     * @param y
     *            specifies the column at the upper left of the component
     * @param width
     *            specifies the number of columns in the component's display area
     * @param height
     *            specifies the number of rows in the component's display area
     * @param insets
     *            specifies the external padding of the component -- the minimum amount of space between the component
     *            and the edges of its display area
     * @param anchor
     *            used when the component is smaller than its display area to determine where (within the area) to place
     *            the component
     * @param weightx
     *            weights are used to determine how to distribute space among columns
     * @param weighty
     *            weights are used to determine how to distribute space among rows
     */
    public static void addComponent( Container cont, GridBagLayout gbl, Component c, int x, int y, int width,
                                     int height, Insets insets, int anchor, double weightx, double weighty ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.insets = insets;
        gbc.anchor = anchor;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints( c, gbc );
        cont.add( c );

    }

}
