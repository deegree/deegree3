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
package org.deegree.protocol.wfs.query;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.sort.SortProperty;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * A self-contained {@link Query}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class AdHocQuery extends Query {

	private final TypeName[] typeNames;

	private final String featureVersion;

	private final ICRS srsName;

	private final ProjectionClause[] projectionClauses;

	private final SortProperty[] sortBy;

	/**
	 * Creates a new {@link AdHocQuery} instance.
	 * @param handle client-generated query identifier, may be <code>null</code>
	 * @param typeNames requested feature types (with optional aliases), may be
	 * <code>null</code>
	 * @param featureVersion version of the feature instances to be retrieved, may be
	 * <code>null</code>
	 * @param srsName WFS-supported SRS that should be used for returned feature
	 * geometries, may be <code>null</code>
	 * @param projectionClauses limits the properties of the features that should be
	 * retrieved, may be <code>null</code>
	 * @param sortBy properties whose values should be used to order the result set may be
	 * <code>null</code>
	 */
	public AdHocQuery(String handle, TypeName[] typeNames, String featureVersion, ICRS srsName,
			ProjectionClause[] projectionClauses, SortProperty[] sortBy) {
		super(handle);
		if (typeNames == null) {
			this.typeNames = new TypeName[0];
		}
		else {
			this.typeNames = typeNames;
		}
		this.featureVersion = featureVersion;
		this.srsName = srsName;
		if (projectionClauses != null) {
			this.projectionClauses = projectionClauses;
		}
		else {
			this.projectionClauses = new ProjectionClause[0];
		}
		if (sortBy != null) {
			this.sortBy = sortBy;
		}
		else {
			this.sortBy = new SortProperty[0];
		}
	}

	/**
	 * Returns the requested feature types (with optional aliases).
	 * @return the requested feature types, never null and contains always one entry
	 */
	public TypeName[] getTypeNames() {
		return typeNames;
	}

	/**
	 * Returns the version of the feature instances to be retrieved.
	 * @return the version of the feature instances to be retrieved, may be null
	 */
	public String getFeatureVersion() {
		return featureVersion;
	}

	/**
	 * Returns the SRS that should be used for returned feature geometries.
	 * @return the SRS that should be used for returned feature geometries, may be null
	 */
	public ICRS getSrsName() {
		return srsName;
	}

	/**
	 * Returns the projections for the features that should be retrieved.
	 * @return the projections for features that should be retrieved, may be empty, but
	 * never <code>null</code>
	 */
	public ProjectionClause[] getProjectionClauses() {
		return projectionClauses;
	}

	/**
	 * Returns the properties whose values should be used to order the result set.
	 * @return sort criteria, may be empty, but never <code>null</code>
	 */
	public SortProperty[] getSortBy() {
		return sortBy;
	}

}
