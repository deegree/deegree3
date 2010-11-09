//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.coverage;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.deegree.coverage.raster.SampleResolution;
import org.slf4j.Logger;

/**
 * Information about the (native) resolutions of a coverage.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ResolutionInfo implements Serializable {

    private static final long serialVersionUID = 3626345247575846857L;

    private static final Logger LOG = getLogger( ResolutionInfo.class );

    /**
     * Different modes to retrieve the next / previous native resolution. They coarsely resemble the same mathematical
     * functions for floating point numbers.
     */
    public enum RoundingMode {
        /** Retrieve the native resolution which is 'larger' then the given one */
        CEIL,
        /** Retrieve the native resolution which is 'smaller' then the given one */
        FLOOR,
        /**
         * Retrieve the native resolution which is 'nearest' to the given one, if perfectly in between two resolutions
         * the upper one will be used.
         */
        ROUND;
    }

    private ArrayList<SampleResolution> nativeResolutions;

    /**
     * A resolution info for continuous coverages.
     */
    public ResolutionInfo() {
        this.nativeResolutions = new ArrayList<SampleResolution>();
    }

    /**
     * The info holds only one native resolution (typically from a single/tiled raster).
     * 
     * @param resolution
     *            the native resolution of the coverage.
     */
    public ResolutionInfo( SampleResolution resolution ) {
        this();
        this.nativeResolutions.add( resolution );
    }

    /**
     * The info holds only one native resolution (typically a multi resolution raster).
     * 
     * @param resolutions
     *            the native resolutions of the coverage.
     */
    public ResolutionInfo( List<SampleResolution> resolutions ) {
        this();
        this.nativeResolutions.addAll( resolutions );
    }

    /**
     * Returns the native resolutions of the coverage. A 'continuous' coverage has no sample resolutions, it therefore
     * will return the empty list.
     * 
     * @return the native resolutions of the given coverage or the empty list if the coverage has no 'native' resolution
     *         (for example a continuous function). Never returns <code>null</code>
     */
    public List<SampleResolution> getNativeResolutions() {
        return nativeResolutions;
    }

    /**
     * Returns the best fitting (in terms of the given rounding mode) native resolution. If the given coverage does not
     * have a native resolution the given resolution will be returned.
     * 
     * @param targetResolution
     *            the required resolution.
     * @param mode
     *            the mode of the to be used for finding the best fit.
     * @return the SampleResolution best fitting the given target resolution and mode, if no 'native' resolution is
     *         available, the given targetResolution will be returned, never <code>null</code>.
     */
    public SampleResolution getBestFit( SampleResolution targetResolution, RoundingMode mode ) {
        SampleResolution result = targetResolution;
        LOG.warn( "Retrieval of best fitting sample resolution needs to be implemented yet." );
        if ( !nativeResolutions.isEmpty() ) {
            switch ( mode ) {
            case CEIL:
                break;
            case FLOOR:
                break;
            case ROUND:
                break;
            }
        }
        return result;
    }

}
