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

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class MapOperationFactor {
    private boolean free = false;

    private boolean selected = false;

    private double factor = 10;

    /**
     * Creates a new MapOperationFactor object.
     *
     * @param factor
     * @param selected
     *            true if the zoom factor is selected.
     * @param free
     *            true if the <tt>MapOperationFactor</tt> represents a field that allows the user to enter his own map
     *            size
     */
    public MapOperationFactor( double factor, boolean selected, boolean free ) {
        this.factor = factor;
        this.selected = selected;
        this.free = free;
    }

    /**
     * returns the numeric factor to be used in a map operation like zoom or pan
     *
     * @return the numeric factor to be used in a map operation like zoom or pan
     */
    public double getFactor() {
        return factor;
    }

    /**
     * returns true if the zoom factor is selected.
     *
     * @return true if the zoom factor is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     *
     *
     * @param selected
     */
    public void setSelected( boolean selected ) {
        this.selected = selected;
    }

    /**
     * @return true if the <tt>MapOperationFactor</tt> represents a field that allows the user to enter his own map
     *         size
     */
    public boolean isFree() {
        return free;
    }
}
