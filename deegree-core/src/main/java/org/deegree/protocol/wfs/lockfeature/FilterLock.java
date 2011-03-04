//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.wfs.lockfeature;

import org.deegree.filter.Filter;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * {@link LockOperation} that specifies the features instances to be locked using a {@link Filter} expression.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FilterLock implements LockOperation {

    private String handle;

    private TypeName typeName;

    private Filter filter;

    /**
     * Creates a new {@link FilterLock} instance.
     * 
     * @param handle
     *            client-generated identifier, may be null
     * @param typeName
     *            name of the feature type to be locked, must not be null
     * @param filter
     *            constraint on the feature instances to be locked, may be null
     */
    public FilterLock( String handle, TypeName typeName, Filter filter ) {
        this.handle = handle;
        this.typeName = typeName;
        this.filter = filter;
    }

    /**
     * Returns the client-generated identifier for the lock operation.
     * 
     * @return the client-generated identifier, can be null
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Returns the name of the feature type to be locked.
     * 
     * @return the name of the feature type to be locked, never null
     */
    public TypeName getTypeName() {
        return typeName;
    }

    /**
     * Returns the constraint on the feature instances to be locked.
     * 
     * @return the constraint on the feature instances to be locked, can be null
     */
    public Filter getFilter() {
        return filter;
    }
}
