package org.deegree.layer.persistence.remotewms;

import static java.util.Collections.singletonList;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.deegree.layer.AbstractLayer;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.protocol.wms.metadata.LayerMetadata;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.remoteows.wms.WMSClient;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.style.se.unevaluated.Style;

public class RemoteWMSLayer extends AbstractLayer {

    private final WMSClient client;

    protected RemoteWMSLayer( LayerMetadata md, WMSClient client ) {
        super( md );
        md.setCascaded( md.getCascaded() + 1 );
        this.client = client;
    }

    @Override
    public LinkedList<String> paintMap( RenderContext context, RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        try {
            GetMap gm = new GetMap( singletonList( getMetadata().getName() ), info.getWidth(), info.getHeight(),
                                    info.getEnvelope(), info.getEnvelope().getCoordinateSystem(),
                                    client.getFormats( WMSRequestType.GetMap ).getFirst() );
            Pair<BufferedImage, String> map = client.getMap( gm, null, 60 );
            if ( map.first != null ) {
                context.paintImage( map.first );
            }
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new LinkedList<String>();
    }
}
