package org.deegree.layer.persistence.remotewms;

import static java.util.Collections.singletonList;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.persistence.remotewms.jaxb.ParameterScopeType;
import org.deegree.layer.persistence.remotewms.jaxb.ParameterUseType;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType.DefaultCRS;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType.Parameter;
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

    private HashMap<String, String> defaultParametersGetMap = new HashMap<String, String>();

    private HashMap<String, String> defaultParametersGetFeatureInfo = new HashMap<String, String>();

    private HashMap<String, String> hardParametersGetMap = new HashMap<String, String>();

    private HashMap<String, String> hardParametersGetFeatureInfo = new HashMap<String, String>();

    private final String originalName;

    protected RemoteWMSLayer( String originalName, LayerMetadata md, WMSClient client, RequestOptionsType opts ) {
        super( md );
        this.originalName = originalName;
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
            if ( opts.getParameter() != null && !opts.getParameter().isEmpty() ) {
                for ( Parameter p : opts.getParameter() ) {
                    String name = p.getName();
                    String value = p.getValue();
                    ParameterUseType use = p.getUse();
                    ParameterScopeType scope = p.getScope();
                    switch ( use ) {
                    case ALLOW_OVERRIDE:
                        switch ( scope ) {
                        case GET_MAP:
                            defaultParametersGetMap.put( name, value );
                            break;
                        case GET_FEATURE_INFO:
                            defaultParametersGetFeatureInfo.put( name, value );
                            break;
                        default:
                            defaultParametersGetMap.put( name, value );
                            defaultParametersGetFeatureInfo.put( name, value );
                            break;
                        }
                        break;
                    case FIXED:
                        switch ( scope ) {
                        case GET_MAP:
                            hardParametersGetMap.put( name, value );
                            break;
                        case GET_FEATURE_INFO:
                            hardParametersGetFeatureInfo.put( name, value );
                            break;
                        default:
                            hardParametersGetMap.put( name, value );
                            hardParametersGetFeatureInfo.put( name, value );
                            break;
                        }
                        break;
                    }
                }
            }
        }
        // set default values if not configured
        if ( this.crs == null ) {
            this.crs = CRSManager.getCRSRef( client.getCoordinateSystems( originalName ).getFirst() );
        }
        if ( this.format == null ) {
            LinkedList<String> fs = client.getFormats( GetMap );
            if ( fs.contains( "image/png" ) ) {
                format = "image/png";
            } else {
                format = fs.getFirst();
            }
        }
    }

    private static void handleParameters( Map<String, String> map, Map<String, String> originals,
                                          Map<String, String> defaults, Map<String, String> hards ) {
        // handle default params
        for ( String def : defaults.keySet() ) {
            String key = def.toUpperCase();
            if ( originals.containsKey( key ) ) {
                map.put( key, originals.get( key ) );
            } else {
                map.put( def, defaults.get( def ) );
            }
        }
        // handle preset params
        for ( Entry<String, String> e : hards.entrySet() ) {
            if ( map.containsKey( e.getKey().toLowerCase() ) ) {
                map.put( e.getKey().toLowerCase(), e.getValue() );
            } else
                map.put( e.getKey(), e.getValue() );
        }
    }

    @Override
    public LinkedList<String> paintMap( RenderContext context, RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        try {
            Map<String, String> extraParams = new HashMap<String, String>();
            handleParameters( extraParams, info.getParameterMap(), defaultParametersGetMap, hardParametersGetMap );
            ICRS crs = this.crs;
            if ( !alwaysUseDefaultCrs ) {
                ICRS envCrs = info.getEnvelope().getCoordinateSystem();
                if ( client.getCoordinateSystems( originalName ).contains( envCrs.getAlias() ) ) {
                    crs = envCrs;
                }
            }

            GetMap gm = new GetMap( singletonList( originalName ), info.getWidth(), info.getHeight(),
                                    info.getEnvelope(), crs, format, transparent );
            Pair<BufferedImage, String> map = client.getMap( gm, extraParams, 60 );
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
        Map<String, String> extraParams = new HashMap<String, String>();
        handleParameters( extraParams, info.getParameterMap(), defaultParametersGetFeatureInfo,
                          hardParametersGetFeatureInfo );

        GetFeatureInfo gfi = new GetFeatureInfo( Collections.singletonList( originalName ), info.getWidth(),
                                                 info.getHeight(), info.getX(), info.getY(), info.getEnvelope(),
                                                 info.getEnvelope().getCoordinateSystem(), info.getFeatureCount() );
        try {
            FeatureCollection col = client.getFeatureInfo( gfi, extraParams );
            return new Pair<FeatureCollection, LinkedList<String>>( col, new LinkedList<String>() );
        } catch ( IOException e ) {
            LOG.warn( "Error when retrieving remote feature info: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return new Pair<FeatureCollection, LinkedList<String>>( null, new LinkedList<String>() );
    }

}
