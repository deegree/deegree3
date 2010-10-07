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
package org.deegree.metadata.persistence;

import java.util.List;

/**
 * Base interface of the {@link MetadataStore} persistence layer, provides access to stored {@link MetadataStore}
 * instances and their schemas.
 * <p>
 * NOTE: One {@link MetadataStore} instance corresponds to one metadata format (e.g. DublinCore, MD_Metadata (ISO
 * TC211), SV_Service (ISO TC211)).
 * </p>
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface MetadataStore {

    /**
     * Called by the container to indicate that this {@link MetadataStore} instance is being placed into service.
     * 
     * @throws MetadataStoreException
     *             if the initialization fails
     */
    public void init()
                            throws MetadataStoreException;

    /**
     * Called by the container to indicate that this {@link MetadataStore} instance is being taken out of service.
     */
    public void destroy();

    /**
     * 
     * Exports the XML for the requested records.
     * 
     * @param query
     *            {@link MetadataQuery}
     * @throws MetadataStoreException
     */
    public MetadataResultSet getRecords( MetadataQuery query )
                            throws MetadataStoreException;

    /**
     * Exports the records by the requested identifier.
     * 
     * @param idList
     *            list of the requested identifiers
     * @throws MetadataStoreException
     */
    public MetadataResultSet getRecordsById( List<String> idList )
                            throws MetadataStoreException;

    /**
     * Acquires transactional access to the metadata store.
     * 
     * @return transaction object that allows to perform transactions operations on the metadata store, never
     *         <code>null</code>
     * @throws MetadataStoreException
     *             if the transactional access could not be acquired or is not implemented for this
     *             {@link MetadataStore}
     */
    public MetadataStoreTransaction acquireTransaction()
                            throws MetadataStoreException;

}
