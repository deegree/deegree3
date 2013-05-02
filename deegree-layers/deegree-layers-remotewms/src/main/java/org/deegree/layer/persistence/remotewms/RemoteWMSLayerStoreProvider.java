package org.deegree.layer.persistence.remotewms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.net.URL;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.layer.Layer;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.OldLayerStoreProvider;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.remotewms.jaxb.RemoteWMSLayers;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.wms.RemoteWMS;

public class RemoteWMSLayerStoreProvider implements OldLayerStoreProvider {

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

            RemoteOWSManager mgr = workspace.getSubsystemManager( RemoteOWSManager.class );
            RemoteOWS store = mgr.get( id );
            if ( !( store instanceof RemoteWMS ) ) {
                throw new ResourceInitException( "The remote WMS store with id " + id
                                                 + " is not available or not of type WMS." );
            }

            RemoteWmsLayerBuilder builder = new RemoteWmsLayerBuilder( ( (RemoteWMS) store ).getClient(), cfg );

            Map<String, Layer> map = builder.buildLayerMap();

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
