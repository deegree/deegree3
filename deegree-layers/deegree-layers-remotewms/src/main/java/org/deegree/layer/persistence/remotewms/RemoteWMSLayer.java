package org.deegree.layer.persistence.remotewms;

import static java.util.Collections.singletonList;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerMetadata;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.remoteows.wms.WMSClient;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.style.se.unevaluated.Style;

public class RemoteWMSLayer extends AbstractLayer {

    private final WMSClient client;

    private final String name;

    protected RemoteWMSLayer( LayerMetadata md, WMSClient client, String name ) {
        super( md );
        this.client = client;
        this.name = name;
        setIdentifier( name );
    }

    @Override
    public LinkedList<String> paintMap( RenderContext context, RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        try {
            Pair<BufferedImage, String> map = client.getMap( singletonList( name ), info.getWidth(), info.getHeight(),
                                                             info.getEnvelope(),
                                                             info.getEnvelope().getCoordinateSystem(),
                                                             client.getFormats( WMSRequestType.GetMap ).getFirst(),
                                                             true, true, 60, false, null, null );
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
