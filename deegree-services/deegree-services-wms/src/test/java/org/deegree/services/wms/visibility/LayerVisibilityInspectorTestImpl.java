package org.deegree.services.wms.visibility;

import org.deegree.layer.metadata.LayerMetadata;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LayerVisibilityInspectorTestImpl implements LayerVisibilityInspector {

    @Override
    public boolean isVisible( LayerMetadata layerMetadata ) {
        return !"NotVisible".equals( layerMetadata.getName() );
    }

}