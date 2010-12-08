//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wfs.getfeature;

/**
 * Used for discriminating between the two <code>GetFeature</code> modes: <code>results</code> and <code>hits</code>.
 * <p>
 * Since WFS spec 1.1.0, a WFS can respond to a query operation in one of two ways (excluding an exception response). It
 * may either generate a complete response document containing resources that satisfy the operation or it may simply
 * generate an empty response container that indicates the count of the total number of resources that the operation
 * would return. Which of these two responses a WFS generates is determined by the value of the optional resultType
 * parameter.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public enum ResultType {

    /**
     * The full response set (i.e. all the feature instances) should be returned.
     */
    RESULTS,

    /**
     * An empty response set should be returned (i.e. no feature instances should be returned), but the
     * "numberOfFeatures" attribute should be set to the number of feature instances that would be returned.
     */
    HITS
}
