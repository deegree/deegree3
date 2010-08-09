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
package org.deegree.services.wcs.getcoverage;

import org.deegree.commons.tom.ows.Version;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.geometry.Envelope;
import org.deegree.services.wcs.model.Grid;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetCoverage {

    private final String coverage;

    private final Envelope requestEnvelope;

    private final Grid outputGrid;

    private final String interpolation;

    private final String outputFormat;

    private final String outputCRS;

    private final boolean storeOutput;

    private final String exceptionFormat;

    private final RangeSet rangeSet;

    private final Version version;

    /**
     * @param version
     * @param coverage
     * @param requestEnvelope
     * @param outputCRS
     * @param outputFormat
     * @param outputGrid
     * @param interpolation
     * @param exceptionFormat
     * @param storeOutput
     * @param rangeSet
     */
    public GetCoverage( Version version, String coverage, Envelope requestEnvelope, String outputCRS,
                        String outputFormat, Grid outputGrid, String interpolation, String exceptionFormat,
                        boolean storeOutput, RangeSet rangeSet ) {
        this.version = version;
        this.coverage = coverage;
        this.requestEnvelope = requestEnvelope;
        this.outputCRS = outputCRS;
        this.outputFormat = outputFormat;
        this.outputGrid = outputGrid;
        this.interpolation = interpolation;
        this.exceptionFormat = exceptionFormat;
        this.storeOutput = storeOutput;
        this.rangeSet = rangeSet;
    }

    /**
     * @return the coverage
     */
    public String getCoverage() {
        return coverage;
    }

    /**
     * @return the requestEnvelope
     */
    public Envelope getRequestEnvelope() {
        return requestEnvelope;
    }

    /**
     * @return the outputGrid
     */
    public Grid getOutputGrid() {
        return outputGrid;
    }

    /**
     * @return the interpolation
     */
    public String getInterpolation() {
        return interpolation;
    }

    /**
     * @return the outputFormat
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @return the outputCRS
     */
    public String getOutputCRS() {
        return outputCRS;
    }

    /**
     * @return the storeOutput
     */
    public boolean isStoreOutput() {
        return storeOutput;
    }

    /**
     * @return the exceptionFormat
     */
    public final String getExceptionFormat() {
        return exceptionFormat;
    }

    /**
     * @return the rangeSet
     */
    public final RangeSet getRangeSet() {
        return rangeSet;
    }

    /**
     * @return the version
     */
    public final Version getVersion() {
        return version;
    }

}
