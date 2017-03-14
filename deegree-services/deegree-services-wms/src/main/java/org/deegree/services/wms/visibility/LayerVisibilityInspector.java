package org.deegree.services.wms.visibility;

import org.deegree.layer.metadata.LayerMetadata;

/**
 * Configurable interface in deegreeWMS configuration to check the visibility of layers.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface LayerVisibilityInspector {

    /**
     * Checks if the layer with the passed {@link LayerMetadata} should be rendered or not.
     *
     * @param layerMetadata
     *                         the metadata of the layer to inspect, never <code>null</code>
     *
     * @return <code>true</code> if the layer should be rendered, <code>false</code> otherwise
     */
    boolean isVisible( LayerMetadata layerMetadata );

}