package org.deegree.layer.persistence.inline;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.MemoryFeatureInputStream;
import org.deegree.feature.stream.ThreadedFeatureInputStream;
import org.deegree.feature.types.AppSchemas;
import org.deegree.feature.types.DynamicAppSchema;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.XPathEvaluator;
import org.deegree.layer.persistence.FeatureStreamRenderer;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineFeatureLayerData implements LayerData {

	private static final Logger LOG = LoggerFactory.getLogger(InlineFeatureLayerData.class);

	private final FeatureCollection col;

	private XPathEvaluator<?> evaluator;

	private int maxFeatures;

	private final Style style;

	InlineFeatureLayerData(FeatureCollection col, DynamicAppSchema schema, int maxFeatures, Style style) {
		this.col = col;
		this.maxFeatures = maxFeatures;
		this.style = style;

		Map<String, QName> bindings = new HashMap<String, QName>();
		// Set<QName> validNames = AppSchemas.collectProperyNames(
		// featureStore.getSchema(), ftName );
		if (schema.getFeatureTypes().length > 0) {
			Set<QName> validNames = AppSchemas.collectProperyNames(schema, schema.getFeatureTypes()[0].getName());
			for (QName name : validNames) {
				bindings.put(name.getLocalPart(), name);
			}
		}
		evaluator = new TypedObjectNodeXPathEvaluator(bindings);
	}

	@Override
	public void render(RenderContext context) throws InterruptedException {
		FeatureInputStream features = null;
		try {
			features = new MemoryFeatureInputStream(col);
			features = new ThreadedFeatureInputStream(features, 100);

			FeatureStreamRenderer renderer = new FeatureStreamRenderer(context, maxFeatures, evaluator);

			renderer.renderFeatureStream(features, style);
		}
		catch (InterruptedException e) {
			throw e;
			// } catch ( FilterEvaluationException e ) {
			// LOG.warn( "A filter could not be evaluated. The error was '{}'.",
			// e.getLocalizedMessage() );
			// LOG.trace( "Stack trace:", e );
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

	@Override
	public FeatureCollection info() {
		return col;
	}

}
