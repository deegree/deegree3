package org.deegree.services.wms.controller.capabilities.theme;

import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.theme.Theme;

import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface MetadataMerger {

	SpatialMetadata mergeSpatialMetadata(List<Theme> themes);

	LayerMetadata mergeLayerMetadata(Theme theme);

}
