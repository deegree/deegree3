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
package org.deegree.feature.persistence.query;

import java.sql.ResultSet;
import java.util.Iterator;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;

/**
 * Provides access to the results of a {@link Query} operation.
 * <p>
 * The primary means of accessing the individual result features is to use the {@link #iterator()} method and
 * subsequently call it's {@link Iterator#next()} methods. Depending on the implementation (e.g. when backed by an SQL
 * result set), this enables the processing of arbitrary large numbers of results without causing memory issues. Also,
 * it's essential to ensure that the {@link #close()} method is called afterwards, or resource leaks may occur (e.g.
 * open SQL result sets).
 * </p>
 * <p>
 * A typical use of a {@link FeatureResultSet} looks like this:
 * <pre>
 *   ...
 *   FeatureResultSet rs = null;
 *    try {
 *        // retrieve the FeatureResultSet
 *        rs = ...
 *        for ( Feature f : rs ) {
 *            // do something with the feature
 *            // ...
 *        }
 *    } finally {
 *        // make sure that the FeatureResultSet always gets closed
 *        if ( rs != null ) {
 *            rs.close();
 *        }
 *    }
 *    ...
 * </pre>
 * 
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface FeatureResultSet extends Iterable<Feature> {

    /**
     * Must be invoked after using to close underlying resources, e.g. SQL {@link ResultSet}s.
     */
    public void close();

    /**
     * Returns all members of the {@link FeatureResultSet} as a {@link FeatureCollection}.
     * <p>
     * NOTE: This method should not be called for very large result sets, as it introduces the overhead of keeping all
     * created feature instances in memory. The returned collection will contain all {@link Feature}s instances from the
     * current position in the iteration sequence.
     * </p>
     * 
     * @return members as feature collection, never <code>null</code>
     */
    public FeatureCollection toCollection();
}
