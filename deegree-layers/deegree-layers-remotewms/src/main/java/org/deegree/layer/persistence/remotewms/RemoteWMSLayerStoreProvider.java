package org.deegree.layer.persistence.remotewms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.StringPair;
import org.deegree.layer.Layer;
import org.deegree.layer.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.remotewms.jaxb.RemoteWMSLayers;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.wms.RemoteWMS;
import org.deegree.remoteows.wms.WMSClient;

public class RemoteWMSLayerStoreProvider implements LayerStoreProvider {

    private static final URL SCHEMA_URL = RemoteWMSLayerStoreProvider.class.getResource( "/META-INF/schemas/layers/remotewms/3.1.0/remotewms.xsd" );

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
            String id = cfg.getRemoteWMSStoreId();
            RemoteOWSManager mgr = workspace.getSubsystemManager( RemoteOWSManager.class );
            RemoteOWS store = mgr.get( id );
            if ( !( store instanceof RemoteWMS ) ) {
                throw new ResourceInitException( "The remote WMS store with id " + id
                                                 + " is not available or not of type WMS." );
            }

            Map<String, Layer> map = new LinkedHashMap<String, Layer>();

            WMSClient client = ( (RemoteWMS) store ).getClient();
            List<StringPair> layers = client.getLayerTree().flattenDepthFirst();
            for ( StringPair p : layers ) {
                if ( p.first != null ) {
                    map.put( p.first, new RemoteWMSLayer( new LayerMetadata(), client, p.first ) );
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
