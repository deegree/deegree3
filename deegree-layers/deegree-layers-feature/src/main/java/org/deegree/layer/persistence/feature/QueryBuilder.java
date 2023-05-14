/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static org.deegree.commons.utils.CollectionUtils.clearNulls;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.filter.Filters.addBBoxConstraint;
import static org.deegree.layer.persistence.feature.FilterBuilder.buildFilter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerQuery;
import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * Builds feature store queries for feature layers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class QueryBuilder {

	private FeatureStore featureStore;

	private OperatorFilter filter;

	private QName ftName;

	private Envelope bbox;

	private LayerQuery query;

	private ValueReference geomProp;

	private SortProperty[] sortBy;

	private String layerName;

	QueryBuilder(FeatureStore featureStore, OperatorFilter filter, QName ftName, Envelope bbox, LayerQuery query,
			ValueReference geomProp, SortProperty[] sortBy, String layerName) {
		this.featureStore = featureStore;
		this.filter = filter;
		this.ftName = ftName;
		this.bbox = bbox;
		this.query = query;
		this.geomProp = geomProp;
		this.sortBy = sortBy;
		this.layerName = layerName;
	}

	List<Query> buildMapQueries() {
		List<Query> queries = new ArrayList<Query>();
		Integer maxFeats = query.getRenderingOptions().getMaxFeatures(layerName);
		final int maxFeatures = maxFeats == null ? -1 : maxFeats;
		if (ftName == null && featureStore != null) {
			final Filter filter2 = filter;
			queries.addAll(
					map(featureStore.getSchema().getFeatureTypes(null, false, false), new Mapper<Query, FeatureType>() {
						@Override
						public Query apply(FeatureType u) {
							Filter fil = addBBoxConstraint(bbox, filter2, geomProp, true);
							return createQuery(u.getName(), fil, round(query.getScale()), maxFeatures,
									query.getResolution(), sortBy);
						}
					}));
		}
		else {
			Query fquery = createQuery(ftName, addBBoxConstraint(bbox, filter, geomProp, true), round(query.getScale()),
					maxFeatures, query.getResolution(), sortBy);
			queries.add(fquery);
		}

		return queries;
	}

	List<Query> buildInfoQueries() {
		List<Query> queries = new ArrayList<Query>();
		if (ftName == null) {
			queries.addAll(
					map(featureStore.getSchema().getFeatureTypes(null, false, false), new Mapper<Query, FeatureType>() {
						@Override
						public Query apply(FeatureType u) {
							Filter f;
							if (filter == null) {
								f = buildFilter(null, u, bbox);
							}
							else {
								f = buildFilter(((OperatorFilter) filter).getOperator(), u, bbox);
							}
							return createQuery(u.getName(), f, -1, query.getFeatureCount(), -1, sortBy);
						}
					}));
			clearNulls(queries);
		}
		else {
			Filter f;
			if (filter == null) {
				f = buildFilter(null, featureStore.getSchema().getFeatureType(ftName), bbox);
			}
			else {
				f = buildFilter(((OperatorFilter) filter).getOperator(),
						featureStore.getSchema().getFeatureType(ftName), bbox);
			}
			queries.add(createQuery(ftName, f, -1, query.getFeatureCount(), -1, sortBy));
		}
		return queries;
	}

	static Query createQuery(QName ftName, Filter filter, int scale, int maxFeatures, double resolution,
			SortProperty[] sort) {
		TypeName[] typeNames = new TypeName[] { new TypeName(ftName, null) };
		return new Query(typeNames, filter, sort, scale, maxFeatures, resolution);
	}

}
