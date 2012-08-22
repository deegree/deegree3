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

import java.io.Serializable;

import org.deegree.model.feature.Feature;

/**
 * Base class for all display elements.
 * <p>
 * A <code>DisplayElement</code> is associated to one feature that may have a geometry property or not (this is the
 * common case).
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract class AbstractDisplayElement implements DisplayElement, Serializable {

    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 1226236249388451855L;

    /**
     * The feature to display
     */
    protected Feature feature;

    private boolean highlighted;

    private boolean selected;

    /**
     * An empty constructor doing nothing
     */
    AbstractDisplayElement() {
        // nothing
    }

    /**
     *
     *
     * @param feature
     */
    AbstractDisplayElement( Feature feature ) {
        if ( feature != null ) {
            setFeature( feature );
        }
    }

    /**
     * Returns the associated <tt>Feature</tt>.
     *
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * sets the feature encapsulated by a DisplayElement
     *
     * @param feature
     */
    public void setFeature( Feature feature ) {
        this.feature = new ScaledFeature( feature, -1 );
    }

    /**
     * returns the id of the feature that's associated with the DisplayElement
     */
    public String getAssociateFeatureId() {
        return feature.getId();
    }

    /**
     * marks a <tt>DisplayElement</tt> as selected or not
     *
     */
    public void setSelected( boolean selected ) {
        this.selected = selected;
    }

    /**
     * returns if the <tt>DisplayElement</tt> is selected or not
     *
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * marks the <tt>DisplayElement</tt> as highlighted or not
     *
     */
    public void setHighlighted( boolean highlighted ) {
        this.highlighted = highlighted;
    }

    /**
     * returns if the <tt>DisplayElement</tt> is highlighted or not.
     *
     */
    public boolean isHighlighted() {
        return highlighted;
    }

    /**
     * Returns whether the <code>DisplayElement</code> should be painted at the current scale or not.
     */
    public abstract boolean doesScaleConstraintApply( double scale );
}
