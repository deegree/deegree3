package org.deegree.layer;

import org.deegree.feature.FeatureCollection;
import org.deegree.rendering.r2d.context.RenderContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class NoDataLayerData implements LayerData {

	@Override
	public void render(RenderContext context) throws InterruptedException {

	}

	@Override
	public FeatureCollection info() {
		return null;
	}

}