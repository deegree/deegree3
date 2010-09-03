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
package org.deegree.services.wcs.coverages;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.geometry.Envelope;
import org.deegree.services.wcs.WCServiceException;
import org.deegree.services.wcs.model.CoverageOptions;
import org.deegree.services.wcs.model.CoverageResult;
import org.deegree.services.wcs.model.Grid;

/**
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public abstract class WCSCoverage {

    /**
     * The output envelopes in different coordinate systems, is synchronized
     */
    public final Set<Envelope> responseEnvelopes = Collections.synchronizedSet( new HashSet<Envelope>() );

    /**
     * The real coverage
     */
    protected final org.deegree.coverage.AbstractCoverage coverage;

    private final CoverageOptions coverageOptions;

    private RangeSet rangeSet;

    private final String name;

    private String label;

    /**
     * @param name
     * @param label
     * @param coverage
     * @param options
     *            of this coverage
     * @param rangeSet
     */
    public WCSCoverage( String name, String label, org.deegree.coverage.AbstractCoverage coverage,
                        CoverageOptions options, RangeSet rangeSet ) {
        this.name = name;
        this.label = label;
        this.coverage = coverage;
        this.coverageOptions = options;
        this.rangeSet = rangeSet;
    }

    /**
     * @return short unique name
     */
    public String getName() {
        return name;
    }

    /**
     * @return human readable label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the supported options for this coverage (like interpolation, etc)
     */
    public CoverageOptions getCoverageOptions() {
        return coverageOptions;
    }

    /**
     * @return the range set of this coverage.
     */
    public RangeSet getRangeSet() {
        return rangeSet;
    }

    /**
     * @return the envelope
     */
    public Envelope getEnvelope() {
        return coverage.getEnvelope();
    }

    /**
     * Query the Coverage for a subset.
     * 
     * @param env
     *            the requested envelope
     * @param grid
     *            grid format of the output
     * @param format
     *            the output format
     * @param interpolation
     *            the interpolation method
     * @param rangeset
     *            the requested range set.
     * @return the subset
     * @throws WCServiceException
     */
    public abstract CoverageResult getCoverageResult( Envelope env, Grid grid, String format, String interpolation,
                                                      RangeSet rangeset )
                            throws WCServiceException;

    /**
     * @param configuredRS
     */
    public void setRangeSet( RangeSet configuredRS ) {
        this.rangeSet = configuredRS;
    }
}
