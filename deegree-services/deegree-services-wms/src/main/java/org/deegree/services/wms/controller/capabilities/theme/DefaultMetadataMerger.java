package org.deegree.services.wms.controller.capabilities.theme;

import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;

import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class DefaultMetadataMerger implements MetadataMerger {

	@Override
	public SpatialMetadata mergeSpatialMetadata(List<Theme> themes) {
		if (themes.isEmpty())
			return null;
		SpatialMetadata smd = new SpatialMetadata();
		for (Theme t : themes) {
			for (org.deegree.layer.Layer l : Themes.getAllLayers(t)) {
				smd.merge(l.getMetadata().getSpatialMetadata());
			}
		}
		return smd;
	}

	@Override
	public LayerMetadata mergeLayerMetadata(Theme theme) {
		return new LayerMetadataMerger().merge(theme);
	}

}