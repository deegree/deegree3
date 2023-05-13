package org.deegree.layer.persistence.inline;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.DynamicAppSchema;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerData;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.style.se.unevaluated.Style;

public class InlineFeatureLayer extends AbstractLayer {

	private final InlineFeatureLayerData data;

	public InlineFeatureLayer(FeatureCollection col, DynamicAppSchema schema, int maxFeatures, Style style) {
		super(createEmptyLayerMetadata());
		data = new InlineFeatureLayerData(col, schema, maxFeatures, style);
	}

	@Override
	public LayerData mapQuery(LayerQuery query, List<String> headers) throws OWSException {

		return data;
	}

	@Override
	public LayerData infoQuery(LayerQuery query, List<String> headers) throws OWSException {

		return data;
	}

	private static LayerMetadata createEmptyLayerMetadata() {
		final List<LanguageString> titles = emptyList();
		final List<LanguageString> abstracts = emptyList();
		final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		final Description description = new Description(null, titles, abstracts, keywords);
		final SpatialMetadata spatialMetadata = null;
		return new LayerMetadata(null, description, spatialMetadata);
	}

}
