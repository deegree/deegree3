/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import javax.xml.namespace.QName;

import org.deegree.filter.Filter;
import org.deegree.filter.sort.SortProperty;

/**
 * A query to be performed against a {@link MetadataStore}.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class MetadataQuery {

	private final QName[] queryTypeNames;

	private final QName[] returnTypeNames;

	private final Filter filter;

	private final SortProperty[] sortCriteria;

	private final int startPosition;

	private final int maxRecords;

	/**
	 * Creates a new {@link MetadataQuery} instance.
	 * @param queryTypeNames names of record types on which the query will be performed,
	 * can be <code>null</code> (depending on the metadata profile)
	 * @param returnTypeNames names of record types to be returned, can be
	 * <code>null</code> (depending on the metadata profile)
	 * @param filter constraint on the queried records, can be <code>null</code>
	 * @param sortCriteria sort criteria, can be <code>null</code>
	 * @param startPosition number of the first hit to be included in the result, starting
	 * at one
	 * @param maxRecords maximum number of hits to include in the results or -1
	 * (unrestricted)
	 */
	public MetadataQuery(QName[] queryTypeNames, QName[] returnTypeNames, Filter filter, SortProperty[] sortCriteria,
			int startPosition, int maxRecords) {
		this.queryTypeNames = queryTypeNames == null ? new QName[0] : queryTypeNames;
		this.returnTypeNames = returnTypeNames == null ? new QName[0] : returnTypeNames;
		this.filter = filter;
		this.sortCriteria = sortCriteria;
		this.startPosition = startPosition;
		this.maxRecords = maxRecords;
	}

	/**
	 * Returns the queried record types.
	 * <p>
	 * Depending on the concrete metadata profile, multiple type names are allowed and may
	 * define aliases (e.g eBRIM).
	 * </p>
	 * @return queried record types, never <code>null</code>
	 */
	public QName[] getQueryTypeNames() {
		return queryTypeNames;
	}

	/**
	 * Returns the record types that should be returned.
	 * <p>
	 * This only makes sense for metadata profiles that support join queries on multiple
	 * record types.
	 * </p>
	 * @return record types to be returned, never <code>null</code>
	 */
	public QName[] getReturnTypeNames() {
		return returnTypeNames;
	}

	/**
	 * Returns the constraints to be applied on the queried records.
	 * @return filter constraints, can be <code>null</code> (no constraints)
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Returns the constraints to be applied on the queried records.
	 * @return filter constraints, can be <code>null</code> (no constraints)
	 */
	public SortProperty[] getSorting() {
		return sortCriteria;
	}

	/**
	 * Returns the number of the first hit to be included in the result.
	 * @return number of the first hit to be included, starting at one
	 */
	public int getStartPosition() {
		return startPosition;
	}

	/**
	 * Returns the maximum number of records to include in the result.
	 * @return maximum number of records or -1 (unrestricted)
	 */
	public int getMaxRecords() {
		return maxRecords;
	}

}