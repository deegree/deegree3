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
package org.deegree.graphics;

import org.deegree.model.feature.Feature;

/**
 *
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public interface Highlighter {

    /**
     * adds a Theme to the Highlighter that shall be notified if something happens.
     * @param theme to be notified
     */
    void addTheme( Theme theme );

    /**
     * @param theme to be removed of notification
     * @see Highlighter#addTheme(Theme)
     */
    void removeTheme( Theme theme );

    /**
     * highlight the feature with the submitted id. If the feature is already highlighted it will be
     * marked as not highlighted.
     * @param id of the feature to be highlighted
     */
    void highlight( int id );

    /**
     * highlight the submitted feature. If the feature is already highlighted it will be marked as
     * not highlighted.
     * @param feature the given feature
     */
    void highlight( Feature feature );

    /**
     * highlight the features specified by the current selector of the Theme. If a feature is
     * already highlighted it will be marked as not highlighted.
     */
    void highlight();

    /**
     * removes all highlight-flags from the features within the MapView
     */
    void resetHighlight();

}
