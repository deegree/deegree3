//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.metadata;

import static org.deegree.commons.config.ResourceState.StateType.created;
import static org.deegree.commons.config.ResourceState.StateType.deactivated;
import static org.deegree.commons.config.ResourceState.StateType.init_error;
import static org.deegree.commons.config.ResourceState.StateType.init_ok;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ExtendedResourceProvider;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.slf4j.Logger;

/**
 * {@link ResourceManager} for {@link OWSMetadataProvider}s.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
@SuppressWarnings("unchecked")
public class OWSMetadataProviderManager extends AbstractResourceManager<OWSMetadataProvider> {

    private static final Logger LOG = getLogger( OWSMetadataProviderManager.class );

    private ServiceMetadataManagerMetadata metadata;

    private static final Pattern filenamePattern = Pattern.compile( "(.*)_metadata\\.(xml|ignored)" );

    @Override
    protected ExtendedResourceProvider<OWSMetadataProvider> getProvider( URL file ) {
        Matcher m = filenamePattern.matcher( file.toExternalForm() );
        if ( m.find() ) {
            return super.getProvider( file );
        }
        return null;
    }

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        metadata = new ServiceMetadataManagerMetadata( workspace );
    }

    @Override
    public ResourceManagerMetadata<OWSMetadataProvider> getMetadata() {
        return metadata;
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    protected ResourceState<OWSMetadataProvider> processResourceConfig( File configFile )
                            throws IOException {

        LOG.debug( "Processing file '{}'", configFile );

        ResourceState<OWSMetadataProvider> state = null;

        String dirName = dir.getCanonicalPath();
        String fileName = configFile.getCanonicalPath().substring( dirName.length() );

        ResourceProvider provider = getProvider( configFile.toURI().toURL() );

        if ( fileName.startsWith( File.separator ) ) {
            fileName = fileName.substring( 1 );
        }
        Matcher m = filenamePattern.matcher( fileName );
        boolean matched = m.find();
        if ( matched && m.group( 2 ).equals( "xml" ) ) {
            String id = m.group( 1 );
            LOG.info( "Setting up {} '{}' from file '{}'...", new Object[] { name, id, fileName } );
            if ( provider != null ) {
                try {
                    OWSMetadataProvider resource = create( id, configFile.toURI().toURL() );
                    state = new ResourceState<OWSMetadataProvider>( id, configFile, provider, created, resource, null );
                    resource.init( workspace );
                    state = new ResourceState<OWSMetadataProvider>( id, configFile, provider, init_ok, resource, null );
                    add( resource );
                } catch ( ResourceInitException e ) {
                    LOG.error( "Error creating {}: {}", new Object[] { name, e.getMessage(), e } );
                    LOG.error( "Stack trace: ", e );
                    state = new ResourceState<OWSMetadataProvider>( id, configFile, provider, init_error, null, e );
                } catch ( Throwable t ) {
                    LOG.error( "Error creating {}: {}", new Object[] { name, t.getMessage(), t } );
                    LOG.error( "Stack trace: ", t );
                    state = new ResourceState<OWSMetadataProvider>( id, configFile, provider, init_error, null,
                                                                    new ResourceInitException( t.getMessage(), t ) );
                }
            } else {
                String msg = "No suitable resource provider available.";
                ResourceInitException e = new ResourceInitException( msg );
                state = new ResourceState<OWSMetadataProvider>( id, configFile, provider, init_error, null, e );
            }
        } else if ( matched ) {
            // 7 is the length of ".ignore"
            String id = fileName.substring( 0, fileName.length() - 7 );
            System.out.println( "failed " + id + "from /" + fileName + "/" );
            state = new ResourceState<OWSMetadataProvider>( id, configFile, provider, deactivated, null, null );
        }
        return state;
    }

    static class ServiceMetadataManagerMetadata extends DefaultResourceManagerMetadata<OWSMetadataProvider> {
        ServiceMetadataManagerMetadata( DeegreeWorkspace workspace ) {
            super( "service metadata", "services/", OWSMetadataProviderProvider.class, workspace );
        }
    }
}
