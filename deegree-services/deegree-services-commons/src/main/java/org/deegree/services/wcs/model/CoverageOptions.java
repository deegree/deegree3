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
package org.deegree.services.wcs.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.deegree.coverage.raster.interpolation.InterpolationType;

/**
 * This class stores options for a coverage like the supported output format, interpolation, etc.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CoverageOptions {

    private final Set<InterpolationType> interpolations;

    private final Set<String> outputFormats;

    private final Set<String> supportedFormats;

    private final String nativeFormat;

    /**
     * @param nativeFormat
     *            of this coverage
     * @param formats
     *            the supported output formats
     * @param crss
     *            the supported request and response crs
     * @param interpolations
     *            the supported interpolations
     */
    public CoverageOptions( String nativeFormat, List<String> formats, List<String> crss,
                            List<InterpolationType> interpolations ) {
        this.nativeFormat = nativeFormat;
        this.outputFormats = new LinkedHashSet<String>( formats );
        if ( nativeFormat != null && !"".equals( nativeFormat ) ) {
            outputFormats.remove( nativeFormat );
        }
        this.supportedFormats = new LinkedHashSet<String>( crss );
        this.interpolations = new LinkedHashSet<InterpolationType>( interpolations );
    }

    /**
     * @return all supported interpolations
     */
    public Set<InterpolationType> getInterpolations() {
        return interpolations;
    }

    /**
     * @return all supported output formats
     */
    public Set<String> getOutputFormats() {
        return outputFormats;
    }

    /**
     * @return all supported request/response CRSs
     */
    public Set<String> getCRSs() {
        return supportedFormats;
    }

    /**
     * @return the nativeFormat
     */
    public final String getNativeFormat() {
        return nativeFormat;
    }

}
