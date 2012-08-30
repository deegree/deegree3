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

package org.deegree.protocol.wfs.transaction;

import javax.xml.namespace.QName;

import org.deegree.filter.Filter;

/**
 * Represents a WFS <code>Delete</code> operation (part of a {@link Transaction} request).
 * 
 * @see Transaction
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Delete extends TransactionOperation {

    private QName ftName;

    private Filter filter;

    /**
     * Creates a new {@link Delete} instance.
     * 
     * @param handle
     *            identifier for the operation, can be null
     * @param typeName
     *            name of the targeted feature type, must not be null
     * @param filter
     *            selects the feature instances to be deleted, must not be null
     */
    public Delete( String handle, QName typeName, Filter filter ) {
        super( handle );
        this.ftName = typeName;
        this.filter = filter;
    }

    /**
     * Always returns {@link TransactionOperation.Type#DELETE}.
     * 
     * @return {@link TransactionOperation.Type#DELETE}
     */
    @Override
    public Type getType() {
        return Type.DELETE;
    }

    /**
     * Returns the name of the targeted feature type.
     * 
     * @return the name of the targeted feature type, never null
     */
    public QName getTypeName() {
        return this.ftName;
    }

    /**
     * Return the filter that selects the feature instances to be deleted.
     * 
     * @return Filter the filter that selects the feature instances to be deleted, never null
     */
    public Filter getFilter() {
        return this.filter;
    }
}
