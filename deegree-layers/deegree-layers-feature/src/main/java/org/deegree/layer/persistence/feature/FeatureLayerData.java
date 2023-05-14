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
package org.deegree.layer.persistence.feature;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.ThreadedFeatureInputStream;
import org.deegree.feature.types.AppSchemas;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class FeatureLayerData implements LayerData {

	private static final Logger LOG = getLogger(FeatureLayerData.class);

	private int maxFeatures;

	private final Style style;

	private XPathEvaluator<?> evaluator;

	private final List<Query> queries;

	private final FeatureStore featureStore;

	public FeatureLayerData(List<Query> queries, FeatureStore featureStore, int maxFeatures, Style style,
			QName ftName) {
		this.queries = queries;
		this.featureStore = featureStore;
		this.maxFeatures = maxFeatures;
		this.style = style;
		Map<String, QName> bindings = new HashMap<String, QName>();
		Set<QName> validNames = AppSchemas.collectProperyNames(featureStore.getSchema(), ftName);
		for (QName name : validNames) {
			bindings.put(name.getLocalPart(), name);
		}
		evaluator = new TypedObjectNodeXPathEvaluator(bindings);
	}

	@Override
	public void render(RenderContext context) throws InterruptedException {
		FeatureInputStream features = null;
		try {
			// TODO Should this always be done on this level? What about queueSize value?
			features = featureStore.query(queries.toArray(new Query[queries.size()]));
			features = new ThreadedFeatureInputStream(features, 100);

			FeatureStreamRenderer renderer = new FeatureStreamRenderer(context, maxFeatures, evaluator);
			renderer.renderFeatureStream(features, style);
		}
		catch (InterruptedException e) {
			throw e;
		}
		catch (FilterEvaluationException e) {
			LOG.warn("A filter could not be evaluated. The error was '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		catch (Throwable e) {
			LOG.warn("Data could not be fetched from the feature store. The error was '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		finally {
			if (features != null) {
				features.close();
			}
		}
	}

	private static FeatureCollection clearDuplicates(FeatureInputStream rs) {
		FeatureCollection col = null;
		try {
			col = new GenericFeatureCollection();
			for (Feature f : rs) {
				if (!col.contains(f)) {
					col.add(f);
				}
			}
		}
		finally {
			rs.close();
		}
		return col;
	}

	@Override
	public FeatureCollection info() {
		FeatureCollection col = null;
		try {
			col = clearDuplicates(featureStore.query(queries.toArray(new Query[queries.size()])));
		}
		catch (Throwable e) {
			LOG.warn("Data could not be fetched from the feature store. The error was '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		return col;
	}

}
