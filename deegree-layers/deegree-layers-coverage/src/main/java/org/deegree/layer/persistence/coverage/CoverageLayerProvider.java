//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.coverage;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.coverage.persistence.CoverageBuilderManager;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.OldLayerStoreProvider;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayers;
import org.deegree.style.persistence.StyleStoreManager;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class CoverageLayerProvider implements OldLayerStoreProvider {

    private static final Logger LOG = getLogger( CoverageLayerProvider.class );

    private static final URL CONFIG_SCHEMA = CoverageLayerProvider.class.getResource( "/META-INF/schemas/layers/coverage/3.2.0/coverage.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public LayerStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            CoverageLayers cfg;
            cfg = (CoverageLayers) JAXBUtils.unmarshall( "org.deegree.layer.persistence.coverage.jaxb", CONFIG_SCHEMA,
                                                         configUrl, workspace );
            if ( cfg.getAutoLayers() != null ) {
                LOG.debug( "Using auto configuration for coverage layers." );
                AutoCoverageLayerBuilder builder = new AutoCoverageLayerBuilder( workspace );
                return builder.createFromAutoLayers( cfg.getAutoLayers() );
            }

            LOG.debug( "Using manual configuration for coverage layers." );

            ManualCoverageLayerBuilder builder = new ManualCoverageLayerBuilder( workspace );
            return builder.buildManual( cfg );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Error while creating coverage layers: " + e.getLocalizedMessage(), e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { CoverageBuilderManager.class, StyleStoreManager.class };
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/layers/coverage";
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

}
