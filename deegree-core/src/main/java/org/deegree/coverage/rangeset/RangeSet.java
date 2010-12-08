//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wcs/model/RangeSet.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.coverage.rangeset;

import java.util.List;

/**
 * The <code>RangeSet</code> models the different range possibilities in a WCS coverage.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: 19041 $, $Date: 2009-08-11 17:04:57 +0200 (Di, 11 Aug 2009) $
 * 
 */
public class RangeSet {

    private final SingleValue<?> nullValue;

    private List<AxisSubset> axisDescriptions;

    private final String name;

    private final String label;

    /**
     * @param name
     * @param label
     * @param axisDescriptions
     * @param nullValue
     */
    public RangeSet( String name, String label, List<AxisSubset> axisDescriptions, SingleValue<?> nullValue ) {
        this.name = name;
        this.label = label;
        this.axisDescriptions = axisDescriptions;
        this.nullValue = nullValue;
    }

    /**
     * Defines the construction of a range subset.
     * 
     * @param axisDescriptions
     */
    public RangeSet( List<AxisSubset> axisDescriptions ) {
        this( null, null, axisDescriptions, null );
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the nullValue
     */
    public final SingleValue<?> getNullValue() {
        return nullValue;
    }

    /**
     * @return the axisDescriptions
     */
    public final List<AxisSubset> getAxisDescriptions() {
        return axisDescriptions;
    }

    /**
     * @return the label or the name if the label was <code>null</code>
     */
    public final String getLabel() {
        return label == null ? name : label;
    }

}
