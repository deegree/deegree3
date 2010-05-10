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

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class BuildingFootprintPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int xValue = 0;

    private int yValue = 0;

    private final Insets insets = new Insets( 10, 10, 0, 0 );

    private BufferedImage image;

    /**
     * 
     */
    public BuildingFootprintPanel() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void paintComponent( Graphics g ) {

        super.paintComponent( g );
        g.drawOval( xValue, yValue, 30, 60 );

    }

    public int getXValue() {
        return xValue;
    }

    public int getYValue() {
        return yValue;
    }

    public void setXValue( int x ) {
        xValue = x;
    }

    public void setYValue( int y ) {
        yValue = y;
    }

    @Override
    public Insets getInsets() {
        return insets;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage( BufferedImage image ) {
        this.image = image;
    }

}
