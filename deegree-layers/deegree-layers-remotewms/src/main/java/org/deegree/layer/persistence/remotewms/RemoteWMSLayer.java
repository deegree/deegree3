package org.deegree.layer.persistence.remotewms;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.FeatureCollection;
import org.deegree.layer.AbstractLayer;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.protocol.wms.metadata.LayerMetadata;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.remoteows.wms.WMSClient;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

public class RemoteWMSLayer extends AbstractLayer {

    private static final Logger LOG = getLogger( RemoteWMSLayer.class );

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
            LOG.warn( "Error when retrieving remote map: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return new LinkedList<String>();
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        GetFeatureInfo gfi = new GetFeatureInfo( Collections.singletonList( getMetadata().getName() ), info.getWidth(),
                                                 info.getHeight(), info.getX(), info.getY(), info.getEnvelope(),
                                                 info.getEnvelope().getCoordinateSystem(), info.getFeatureCount() );
        try {
            return new Pair<FeatureCollection, LinkedList<String>>( client.getFeatureInfo( gfi, null ),
                                                                    new LinkedList<String>() );
        } catch ( IOException e ) {
            LOG.warn( "Error when retrieving remote feature info: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return new Pair<FeatureCollection, LinkedList<String>>( null, new LinkedList<String>() );
    }

}
