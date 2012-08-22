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
package org.deegree.graphics.optimizers;

import java.awt.Graphics2D;

import org.deegree.graphics.MapView;
import org.deegree.graphics.Theme;
import org.deegree.graphics.displayelements.LabelDisplayElement;

/**
 * This is the interface for (graphical) {@link Optimizer}s that need to alter the contents of {@link Theme}s (e.g.
 * positions of display elements} before the parent {@link MapView} object is painted.
 * <p>
 * For example, the placements of {@link LabelDisplayElement}s in a {@link Theme} may be optimized to minimize
 * overlapping using the {@link LabelOptimizer}.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface Optimizer {

    /**
     * Sets the associated {@link MapView} instance. This is needed to provide scale and projection information. Called
     * by the {@link MapView}.
     *
     * @param mapView
     *            {@link MapView} instance to associate with this {@link Optimizer}
     */
    public void setMapView( MapView mapView );

    /**
     * Adds a {@link Theme} to be considered by this {@link Optimizer}.
     *
     * @param theme
     *            {@link Theme} to be considered
     */
    public void addTheme( Theme theme );

    /**
     * Invokes the optimization process. This {@link Optimizer} will now process and modify the contents of the attached
     * {@link Theme}s.
     *
     * @param g
     *            graphis context that will be used to draw the optimized display elements
     * @throws Exception
     */
    public abstract void optimize( Graphics2D g )
                            throws Exception;
}
