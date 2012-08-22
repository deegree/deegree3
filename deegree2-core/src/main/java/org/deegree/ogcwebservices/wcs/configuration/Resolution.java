// $HeadURL$
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
package org.deegree.ogcwebservices.wcs.configuration;

/**
 * The interface defines the encpsulating element to coverage data description as used by the
 * deegree WCS CoverageOffering Extension. The interface extends <tt>java.lang.Comparable</tt> to
 * enable a container to sort its <tt>Resolution</tt>s by scale.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface Resolution extends Comparable {

    /**
     * returns the minimum scale (inculding) the <tt>Resolution</tt> is valid for.
     *
     * @return the minimum scale (inculding) the <tt>Resolution</tt> is valid for.
     */
    double getMinScale();

    /**
     * returns the maximum scale (exculding) the <tt>Resolution</tt> is valid for.
     *
     * @return the maximum scale (exculding) the <tt>Resolution</tt> is valid for.
     */
    double getMaxScale();

    /**
     * returns the <tt>Range</tt>s included with in resolution. A range is similar to those
     * defined in OGC WCS 1.0.0 specification for CoverageOffering. But it is reduced to the
     * elements required for identifying the coverages resources assigned to a specific combination
     * of parameter (values).
     * <p>
     * The return value maybe is <tt>null</tt> if the <tt>Resolution</tt> just describes data
     * from one parameter dimension (missing Range in CoverageOffering). In this case there is
     * direct access to the data source describing element(s).
     *
     * @return the <tt>Range</tt>s included with in resolution.
     */
    public Range[] getRanges();

}
