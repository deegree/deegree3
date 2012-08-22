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
package org.deegree.graphics.displayelements;

import java.awt.Graphics;

import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.model.feature.Feature;

/**
 * Basic interface for all display elements. A <code>DisplayElement</code> is associated to one
 * {@link Feature} instance that may have a geometry property or not (which is the common case).
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface DisplayElement {

    /**
     * Returns the associated {@link Feature}.
     *
     * @return the associated feature
     */
    Feature getFeature();

    /**
     * Sets the associated {@link Feature}.
     *
     * @param feature
     */
    void setFeature( Feature feature );

    /**
     * Returns the id of the associated feature.
     *
     * @return the associated feature's id
     */
    String getAssociateFeatureId();

    /**
     * Renders the <code>DisplayElement</code> to the submitted graphic context.
     *
     * @param g
     *            graphics context to render to
     * @param projection
     * @param scale
     */
    void paint( Graphics g, GeoTransform projection, double scale );

    /**
     * Sets the selection state of the <code>DisplayElement</code>.
     *
     * @param selected
     *            true, if the <code>DisplayElement</code> is selected, false otherwise
     */
    void setSelected( boolean selected );

    /**
     * Returns whether the <code>DisplayElement</code> is selected.
     *
     * @return true, if the <code>DisplayElement</code> is selected, false otherwise
     */
    boolean isSelected();

    /**
     * Sets the highlighting state of the <code>DisplayElement</code>.
     *
     * @param highlighted
     *            true, if the <code>DisplayElement</code> is highlighted, false otherwise
     */
    void setHighlighted( boolean highlighted );

    /**
     * Returns whether the <code>DisplayElement</code> is highlighted.
     *
     * @return true, if the <code>DisplayElement</code> is highlighted, false otherwise
     */
    boolean isHighlighted();

    /**
     * Returns whether the <code>DisplayElement</code> should be painted at the given scale.
     *
     * @param scale
     *            scale to check
     * @return true, if the <code>DisplayElement</code> has to be painted, false otherwise
     */
    boolean doesScaleConstraintApply( double scale );
}
