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
package org.deegree.ogcwebservices.wcs.getcoverage;

import org.deegree.datatypes.Code;

/**
 * Encapsulates the result of a GetCoverage request. In addition to the data/coverage itself
 * informations about the desired output format and the type of the coverage are included.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class ResultCoverage {

    private Object coverage = null;

    private Class coverageType = null;

    private Code desiredOutputFormat = null;

    private GetCoverage request = null;

    /**
     * @param coverage
     * @param coverageType
     * @param desiredOutputFormat
     */
    public ResultCoverage( Object coverage, Class coverageType, Code desiredOutputFormat, GetCoverage request ) {
        super();
        this.coverage = coverage;
        this.coverageType = coverageType;
        this.desiredOutputFormat = desiredOutputFormat;
        this.request = request;
    }

    public GetCoverage getRequest() {
        return request;
    }

    /**
     * @return Returns the coverage.
     *
     */
    public Object getCoverage() {
        return coverage;
    }

    /**
     * @return Returns the coverageType.
     *
     */
    public Class getCoverageType() {
        return coverageType;
    }

    /**
     * @return Returns the desiredOutputFormat.
     *
     */
    public Code getDesiredOutputFormat() {
        return desiredOutputFormat;
    }

}
