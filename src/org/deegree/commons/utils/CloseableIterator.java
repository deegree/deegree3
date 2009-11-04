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

package org.deegree.commons.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An <code>Iterator</code> that can (and must) be closed after it's not needed anymore.
 * <p>
 * This interface is used to implement <code>Iterator</code>s that generate their elements lazily, e.g. from SQL
 * <code>ResultSet</code>s. In order to be able to release the underlying resources (such as JDBC
 * <code>Connection</code>s), an explicit notification is necessary which is provided by the {@link #close()} method.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @param <T>
 *            type of the iterated objects
 */
public interface CloseableIterator<T> extends Iterator<T> {

    /**
     * Frees all underlying resources, no other method calls are permitted after calling this.
     */
    public void close();

    /**
     * Returns the elements as an easy accessible <code>List</code> and closes the iterator.
     * <p>
     * NOTE: This should only be used for small numbers of elements.
     * </p>
     * 
     * @return list that contains all elements, never <code>null</code>
     */
    public List<T> getAsListAndClose();

    /**
     * Copies the elements into the given <code>Collection</code> and closes the iterator.
     * 
     * @param collection
     *            collection where the elements are copied, must not be <code>null</code>
     * @return collection that contains all elements, same instance as the parameter
     */
    public Collection<T> getAsCollectionAndClose( Collection<T> collection );
}
