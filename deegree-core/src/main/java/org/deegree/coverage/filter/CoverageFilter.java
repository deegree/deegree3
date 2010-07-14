//$HeadURL$
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

package org.deegree.coverage.filter;

import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.rangeset.RangeSet;

/**
 * The <code>CoverageFilter</code> applies a rangeset (a coverage range definition) to a coverage.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public abstract class CoverageFilter {

    /** the coverage to operate upon */
    protected AbstractCoverage coverage;

    /**
     * 
     * @param coverage
     *            used for the operations.
     */
    public CoverageFilter( AbstractCoverage coverage ) {
        this.coverage = coverage;
    }

    /**
     * Applies the given {@link RangeSet} to the coverage. If the coverage has no {@link RangeSet} this method will
     * return the coverage.
     * 
     * @param rasterRangeSet
     *            describing the values of the given coverage
     * @param targetRangeset
     *            describing the ranges of the target coverage
     * @return a raster using the given RangeSet for selection
     */
    public abstract AbstractCoverage apply( RangeSet rasterRangeSet, RangeSet targetRangeset );

}
