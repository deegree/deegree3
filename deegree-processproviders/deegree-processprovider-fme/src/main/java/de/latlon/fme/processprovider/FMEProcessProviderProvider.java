/*
 * Copyright lat/lon GmbH 2011
 * All rights reserved.
 */
package de.latlon.fme.processprovider;

import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.services.wps.provider.ProcessProviderProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

import java.net.URL;

/**
 * {@link ProcessProviderProvider} for the {@link FMEProcessProvider}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: goltz $
 * @version $Revision: 90 $, $Date: 2011-05-30 10:31:58 +0200 (Mo, 30 Mai 2011)
 */
public class FMEProcessProviderProvider extends ProcessProviderProvider {

    @Override
    public String getNamespace() {
        return "http://www.deegree.org/processes/fme";
    }

    /*
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends ResourceManager>[] getDependencies() {
            return new Class[] { ProxyUtils.class, ConnectionManager.class };
        }
    */
    @Override
    public URL getSchema() {
        return FMEProcessProviderProvider.class.getResource( "/META-INF/schemas/fme/3.4.0/fme.xsd" );
    }

    @Override
    public ResourceMetadata<ProcessProvider> createFromLocation( Workspace workspace,
                                                                 ResourceLocation<ProcessProvider> resourceLocation ) {
        return new FMEProcessMetadata( workspace, resourceLocation, this );
    }

}
