package org.deegree.layer.persistence.remotewms;

import java.util.LinkedList;

import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerMetadata;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.style.se.unevaluated.Style;

public class RemoteWMSLayer extends AbstractLayer {

    protected RemoteWMSLayer( LayerMetadata md ) {
        super( md );
    }

    @Override
    public LinkedList<String> paintMap( RenderContext context, RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        return null;
    }

}
