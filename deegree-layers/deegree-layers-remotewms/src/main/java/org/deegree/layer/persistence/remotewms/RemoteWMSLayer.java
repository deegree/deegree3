package org.deegree.layer.persistence.remotewms;

import static java.util.Collections.singletonList;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType.DefaultCRS;
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

    private ICRS crs;

    private boolean alwaysUseDefaultCrs;

    private String format;

    private boolean transparent = true;

    protected RemoteWMSLayer( LayerMetadata md, WMSClient client, RequestOptionsType opts ) {
        super( md );
        md.setCascaded( md.getCascaded() + 1 );
        this.client = client;
        if ( opts != null ) {
            if ( opts.getDefaultCRS() != null ) {
                DefaultCRS crs = opts.getDefaultCRS();
                this.crs = CRSManager.getCRSRef( crs.getValue(), true );
                alwaysUseDefaultCrs = crs.isUseAlways();
            }
            if ( opts.getImageFormat() != null ) {
                this.format = opts.getImageFormat().getValue();
                this.transparent = opts.getImageFormat().isTransparent();
            }
        }
        // set default values if not configured
        if ( this.crs == null ) {
            this.crs = CRSManager.getCRSRef( client.getCoordinateSystems( md.getName() ).getFirst() );
        }
        if ( this.format == null ) {
            format = client.getFormats( GetMap ).getFirst();
        }
    }

    @Override
    public LinkedList<String> paintMap( RenderContext context, RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        try {
            ICRS crs = this.crs;
            if ( !alwaysUseDefaultCrs ) {
                ICRS envCrs = info.getEnvelope().getCoordinateSystem();
                if ( client.getCoordinateSystems( getMetadata().getName() ).contains( envCrs.getAlias() ) ) {
                    crs = envCrs;
                }
            }

            GetMap gm = new GetMap( singletonList( getMetadata().getName() ), info.getWidth(), info.getHeight(),
                                    info.getEnvelope(), crs, format, transparent );
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
            FeatureCollection col = client.getFeatureInfo( gfi, null );
            return new Pair<FeatureCollection, LinkedList<String>>( col, new LinkedList<String>() );
        } catch ( IOException e ) {
            LOG.warn( "Error when retrieving remote feature info: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return new Pair<FeatureCollection, LinkedList<String>>( null, new LinkedList<String>() );
    }

}
