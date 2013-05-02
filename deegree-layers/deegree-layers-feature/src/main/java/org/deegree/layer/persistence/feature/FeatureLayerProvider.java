//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.NewFeatureStoreProvider;
import org.deegree.layer.persistence.OldLayerStoreProvider;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayers;
import org.deegree.style.persistence.StyleStoreManager;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureLayerProvider implements OldLayerStoreProvider {

    private static final Logger LOG = getLogger( FeatureLayerProvider.class );

    private static final URL SCHEMA_URL = FeatureLayerProvider.class.getResource( "/META-INF/schemas/layers/feature/3.2.0/feature.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public MultipleLayerStore create( URL configUrl )
                            throws ResourceInitException {
        String pkg = "org.deegree.layer.persistence.feature.jaxb";
        try {
            FeatureLayers lays = (FeatureLayers) unmarshall( pkg, SCHEMA_URL, configUrl, workspace );

            if ( lays.getAutoLayers() != null ) {
                AutoFeatureLayerBuilder builder = new AutoFeatureLayerBuilder( workspace );
                return builder.createInAutoMode( lays.getAutoLayers() );
            }

            LOG.debug( "Creating configured feature layers only." );

            String id = lays.getFeatureStoreId();
            FeatureStore store = workspace.getNewWorkspace().getResource( NewFeatureStoreProvider.class, id );
            if ( store == null ) {
                throw new ResourceInitException( "Feature layer config was invalid, feature store with id " + id
                                                 + " is not available." );
            }

            ManualFeatureLayerBuilder builder = new ManualFeatureLayerBuilder( lays, configUrl, store, workspace );
            return builder.buildFeatureLayers();
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not parse layer configuration file.", e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { StyleStoreManager.class };
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/layers/feature";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA_URL;
    }

}
