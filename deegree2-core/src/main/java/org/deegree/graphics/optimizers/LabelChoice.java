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

import org.deegree.graphics.displayelements.Label;
import org.deegree.graphics.displayelements.LabelDisplayElement;

/**
 * Represents different possibilities (candidates) to draw a {@link LabelDisplayElement}.
 * <p>
 * A {@link LabelChoice} has several {@link Label} candidates, one of this candidates is selected and represents the
 * best choice.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LabelChoice {

    // associated LabelDisplayElement
    private LabelDisplayElement element;

    // currently selected Label (in candidates array)
    private int selected;

    // candidate Label objects
    private Label[] candidates;

    // quality of each Label
    private float[] qualities;

    // boundingbox of all contained labels
    private int maxX;

    // boundingbox of all contained labels
    private int maxY;

    // boundingbox of all contained labels
    private int minX;

    // boundingbox of all contained labels
    private int minY;

    /**
     * Creates a new instance of {@link LabelChoice}.
     *
     * @param element
     * @param candidates
     * @param qualities
     * @param selected
     * @param maxX
     * @param maxY
     * @param minX
     * @param minY
     */
    public LabelChoice( LabelDisplayElement element, Label[] candidates, float[] qualities, int selected, int maxX,
                        int maxY, int minX, int minY ) {
        this.element = element;
        this.candidates = candidates;
        this.qualities = qualities;
        this.selected = selected;
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
    }

    /**
     * Selects one of the contained {@link Label} candidates randomly.
     */
    public void selectLabelRandomly() {
        selected = (int) ( Math.random() * ( candidates.length - 1 ) + 0.5 );
    }

    /**
     * Sets the selected {@link Label} candidate.
     *
     * @param selected
     *            the index of the {@link Label} to be selected
     */
    public void setSelected( int selected ) {
        this.selected = selected;
    }

    /**
     * Returns the index of the currently selected {@link Label}.
     *
     * @return the index of the currently selected {@link Label}
     */
    public int getSelected() {
        return selected;
    }

    /**
     * Returns the quality measure of the currently selected {@link Label}
     *
     * @return the quality of the currently selected {@link Label}
     */
    public float getQuality() {
        return qualities[selected];
    }

    /**
     * Returns the currently selected {@link Label} (which is the best choice of the candidates).
     *
     * @return the currently selected {@link Label}
     */
    public Label getSelectedLabel() {
        return candidates[selected];
    }

    /**
     * Returns the associated {@link LabelDisplayElement}.
     *
     * @return the associated {@link LabelDisplayElement}
     */
    public LabelDisplayElement getElement() {
        return element;
    }

    /**
     * Returns the max x value of the bounding box of all {@link Label} candidates.
     *
     * @return max x value of the bounding box
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Returns the max y value of the bounding box of all {@link Label} candidates.
     *
     * @return max y value of the bounding box
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * Returns the min x value of the bounding box of all {@link Label} candidates.
     *
     * @return min x value of the bounding box
     */
    public int getMinX() {
        return minX;
    }

    /**
     * Returns the min y value of the bounding box of all {@link Label} candidates.
     *
     * @return min y value of the bounding box
     */
    public int getMinY() {
        return minY;
    }

    /**
     * Determines if this {@link LabelChoice} can intersect with another {@link LabelChoice} by any chance, i.e. whether
     * there are at least two {@link Label} candidates from each choice that intersect.
     *
     * @param that
     *            {@link LabelChoice} to be tested against this {@link LabelChoice}
     * @return true if the two {@link LabelChoice}s can intersect
     */
    public boolean intersects( LabelChoice that ) {

        int west1 = getMinX();
        int south1 = getMinY();
        int east1 = getMaxX();
        int north1 = getMaxY();

        int west2 = that.getMinX();
        int south2 = that.getMinY();
        int east2 = that.getMaxX();
        int north2 = that.getMaxY();

        // special cases: one box lays completly inside the other one
        if ( ( west1 <= west2 ) && ( south1 <= south2 ) && ( east1 >= east2 ) && ( north1 >= north2 ) ) {
            return true;
        }
        if ( ( west1 >= west2 ) && ( south1 >= south2 ) && ( east1 <= east2 ) && ( north1 <= north2 ) ) {
            return true;
        }
        // in any other case of intersection, at least one line of the BBOX has
        // to cross a line of the other BBOX
        // check western boundary of box 1
        // "touching" boxes must not intersect
        if ( ( west1 >= west2 ) && ( west1 < east2 ) ) {
            if ( ( south1 <= south2 ) && ( north1 > south2 ) ) {
                return true;
            }

            if ( ( south1 < north2 ) && ( north1 >= north2 ) ) {
                return true;
            }
        }
        // check eastern boundary of box 1
        // "touching" boxes must not intersect
        if ( ( east1 > west2 ) && ( east1 <= east2 ) ) {
            if ( ( south1 <= south2 ) && ( north1 > south2 ) ) {
                return true;
            }

            if ( ( south1 < north2 ) && ( north1 >= north2 ) ) {
                return true;
            }
        }
        // check southern boundary of box 1
        // "touching" boxes must not intersect
        if ( ( south1 >= south2 ) && ( south1 < north2 ) ) {
            if ( ( west1 <= west2 ) && ( east1 > west2 ) ) {
                return true;
            }

            if ( ( west1 < east2 ) && ( east1 >= east2 ) ) {
                return true;
            }
        }
        // check northern boundary of box 1
        // "touching" boxes must not intersect
        if ( ( north1 > south2 ) && ( north1 <= north2 ) ) {
            if ( ( west1 <= west2 ) && ( east1 > west2 ) ) {
                return true;
            }

            if ( ( west1 < east2 ) && ( east1 >= east2 ) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if ( candidates.length > 0 ) {
            return candidates[0].toString();
        }
        return "empty";
    }
}
