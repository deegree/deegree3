//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.remotewfs;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.net.URL;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.remotewfs.jaxb.RemoteWFSFeatureStoreConfig;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link RemoteWFSFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWFSFeatureStoreProvider extends FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( RemoteWFSFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/remotewfs";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.remotewfs.jaxb";

    private static final URL CONFIG_SCHEMA = RemoteWFSFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/remotewfs/3.1.0/remotewfs.xsd" );

    private DeegreeWorkspace workspace;

    // @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    // @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    // @Override
    public RemoteWFSFeatureStore create( URL configURL )
                            throws ResourceInitException {

        RemoteWFSFeatureStore fs = null;
        try {
            RemoteWFSFeatureStoreConfig config = (RemoteWFSFeatureStoreConfig) unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                           CONFIG_SCHEMA, configURL,
                                                                                           workspace );

            fs = new RemoteWFSFeatureStore( config );
        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        }
        return fs;
    }

    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.workspace.ResourceProvider#getNamespace()
     */
    @Override
    public String getNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.workspace.standard.AbstractResourceProvider#createFromLocation(org.deegree.workspace.Workspace,
     * org.deegree.workspace.ResourceLocation)
     */
    @Override
    public ResourceMetadata<FeatureStore> createFromLocation( Workspace workspace,
                                                              ResourceLocation<FeatureStore> location ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.workspace.standard.AbstractResourceProvider#getSchema()
     */
    @Override
    public URL getSchema() {
        // TODO Auto-generated method stub
        return null;
    }
}