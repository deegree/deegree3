///$HeadURL$
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
package org.deegree.ogcwebservices.wfs.operation.transaction;

import java.util.List;

import org.deegree.io.datastore.FeatureId;

/**
 * Represents the results of the {@link Insert} operations of a {@link Transaction} request.
 * <p>
 * Encapsulates an <code>InsertResults</code> element as specified in the WFS Specification
 * OGC 04-094 (#12.3 Pg.72).
 * <p>
 * It contains all feature ids of the features that have been inserted for one insert operation and
 * an optional handle which helps to identify the corresponding insert operation element in the
 * transaction.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InsertResults {

    private String handle;

    private List<FeatureId> featureIDs;

    /**
     * Creates a new <code>InsertResults</code> instance.
     *
     * @param handle
     *            identifier for the corresponding insert operation element
     * @param featureIDs
     *            List of Features IDs
     */
    public InsertResults( String handle, List<FeatureId> featureIDs) {
        this.handle = handle;
        this.featureIDs = featureIDs;
    }

    /**
     * Returns the optional identifier for the insert element that corresponds to these results.
     *
     * @return the optional identifier for the insert element.
     */
    public String getHandle () {
        return this.handle;
    }

    /**
     * Returns the feature ids of the features that have been inserted for the corresponding insert
     * operation.
     *
     * @return the feature ids.
     */
    public List<FeatureId> getFeatureIDs () {
        return this.featureIDs;
    }
}
