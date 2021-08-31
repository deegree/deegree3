package de.latlon.fme.processprovider;

import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;

/**
 * {@link org.deegree.workspace.ResourceMetadata} providing the {@link FMEProcessProviderBuilder}.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FMEProcessMetadata extends AbstractResourceMetadata<ProcessProvider> {

    public FMEProcessMetadata( Workspace workspace,
                               ResourceLocation<ProcessProvider> location,
                               AbstractResourceProvider<ProcessProvider> provider ) {
        super( workspace, location, provider );
    }

    @Override
    public ResourceBuilder<ProcessProvider> prepare() {
        return new FMEProcessProviderBuilder( workspace, location, provider );
    }

}
