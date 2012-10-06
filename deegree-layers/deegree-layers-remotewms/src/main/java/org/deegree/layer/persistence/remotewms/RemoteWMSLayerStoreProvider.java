package org.deegree.layer.persistence.remotewms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.DescriptionConverter;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.SpatialMetadataConverter;
import org.deegree.layer.Layer;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.remotewms.jaxb.LayerType;
import org.deegree.layer.persistence.remotewms.jaxb.RemoteWMSLayers;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.wms.RemoteWMS;

public class RemoteWMSLayerStoreProvider implements LayerStoreProvider {

    private static final URL SCHEMA_URL = RemoteWMSLayerStoreProvider.class.getResource( "/META-INF/schemas/layers/remotewms/3.2.0/remotewms.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public LayerStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            RemoteWMSLayers cfg = (RemoteWMSLayers) unmarshall( "org.deegree.layer.persistence.remotewms.jaxb",
                                                                SCHEMA_URL, configUrl, workspace );
            String id = cfg.getRemoteWMSId();
            RequestOptionsType opts = cfg.getRequestOptions();

            RemoteOWSManager mgr = workspace.getSubsystemManager( RemoteOWSManager.class );
            RemoteOWS store = mgr.get( id );
            if ( !( store instanceof RemoteWMS ) ) {
                throw new ResourceInitException( "The remote WMS store with id " + id
                                                 + " is not available or not of type WMS." );
            }

            Map<String, Layer> map = new LinkedHashMap<String, Layer>();

            WMSClient client = ( (RemoteWMS) store ).getClient();

            Map<String, LayerMetadata> configured = new HashMap<String, LayerMetadata>();
            if ( cfg.getLayer() != null ) {
                for ( LayerType l : cfg.getLayer() ) {
                    String name = l.getName();
                    SpatialMetadata smd = SpatialMetadataConverter.fromJaxb( l.getEnvelope(), l.getCRS() );
                    Description desc = null;
                    if ( l.getDescription() != null ) {
                        desc = DescriptionConverter.fromJaxb( l.getDescription().getTitle(),
                                                              l.getDescription().getAbstract(),
                                                              l.getDescription().getKeywords() );
                    }
                    LayerMetadata md = new LayerMetadata( name, desc, smd );
                    md.setMapOptions( ConfigUtils.parseLayerOptions( l.getLayerOptions() ) );
                    configured.put( l.getOriginalName(), md );
                }
            }

            List<LayerMetadata> layers = client.getLayerTree().flattenDepthFirst();
            if ( configured.isEmpty() ) {
                for ( LayerMetadata md : layers ) {
                    if ( md.getName() != null ) {
                        map.put( md.getName(), new RemoteWMSLayer( md.getName(), md, client, opts ) );
                    }
                }
            } else {
                for ( LayerMetadata md : layers ) {
                    String name = md.getName();
                    LayerMetadata confMd = configured.get( name );
                    if ( confMd != null ) {
                        confMd.merge( md );
                        map.put( confMd.getName(), new RemoteWMSLayer( name, confMd, client, opts ) );
                    }
                }
            }
            return new MultipleLayerStore( map );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not parse remote WMS layer store config.", e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { RemoteOWSManager.class };
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/layers/remotewms";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA_URL;
    }

}
