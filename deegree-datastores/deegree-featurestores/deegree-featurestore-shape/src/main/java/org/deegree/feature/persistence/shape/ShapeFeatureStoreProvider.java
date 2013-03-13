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
package org.deegree.feature.persistence.shape;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.NewFeatureStoreProvider;
import org.deegree.feature.persistence.shape.jaxb.ShapeFeatureStoreConfig;
import org.deegree.feature.persistence.shape.jaxb.ShapeFeatureStoreConfig.Mapping.GeometryProperty;
import org.deegree.feature.persistence.shape.jaxb.ShapeFeatureStoreConfig.Mapping.SimpleProperty;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link ShapeFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ShapeFeatureStoreProvider extends NewFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( ShapeFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/shape";

    static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.shape.jaxb";

    private static final URL CONFIG_SCHEMA = ShapeFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/shape/3.1.0/shape.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public ShapeFeatureStore create( URL configURL )
                            throws ResourceInitException {

        ShapeFeatureStore fs = null;
        try {
            ShapeFeatureStoreConfig config = (ShapeFeatureStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                             CONFIG_SCHEMA, configURL,
                                                                                             workspace );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );

            String srs = config.getStorageCRS();
            ICRS crs = null;
            if ( srs != null ) {
                // rb: if it is null, the shape feature store will try to read
                // the prj files.
                // srs = "EPSG:4326";
                // } else {
                srs = srs.trim();
                crs = CRSManager.getCRSRef( srs );
            }

            String shapeFileName = null;
            try {
                shapeFileName = new File( resolver.resolve( config.getFile().trim() ).toURI() ).toString();
            } catch ( MalformedURLException e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg, e );
                throw new ResourceInitException( msg, e );
            } catch ( URISyntaxException e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg );
                LOG.trace( "Stack trace:", e );
                throw new ResourceInitException( msg, e );
            }

            Charset cs = null;
            String encoding = config.getEncoding();
            if ( encoding != null ) {
                try {
                    cs = Charset.forName( encoding );
                } catch ( Exception e ) {
                    String msg = "Unsupported encoding '" + encoding + "'. Continuing with encoding guessing mode.";
                    LOG.error( msg );
                }
            }

            List<Mapping> mappings = null;
            if ( config.getMapping() != null ) {
                mappings = new ArrayList<Mapping>();
                for ( Object o : config.getMapping().getSimplePropertyOrGeometryProperty() ) {
                    if ( o instanceof GeometryProperty ) {
                        GeometryProperty g = (GeometryProperty) o;
                        mappings.add( new Mapping( null, g.getName(), false ) );
                    }
                    if ( o instanceof SimpleProperty ) {
                        SimpleProperty f = (SimpleProperty) o;
                        String name = f.getName();
                        if ( name == null ) {
                            name = f.getMapping();
                        }
                        mappings.add( new Mapping( f.getMapping(), name, f.isGenerateIndex() ) );
                    }
                }
            }

            Boolean genIdx = config.isGenerateAlphanumericIndexes();
            fs = new ShapeFeatureStore( shapeFileName, crs, cs, config.getFeatureTypeNamespace(),
                                        config.getFeatureTypeName(), config.getFeatureTypePrefix(), genIdx == null
                                                                                                    || genIdx, null,
                                        mappings, null );

        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        }
        return fs;
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    static class Mapping {
        String fieldname;

        String propname;

        boolean index;

        Mapping( String fieldname, String propname, boolean index ) {
            this.fieldname = fieldname;
            this.propname = propname;
            this.index = index;
        }
    }

    @Override
    public String getNamespace() {
        return CONFIG_NS;
    }

    @Override
    public ResourceMetadata<FeatureStore> createFromLocation( Workspace workspace,
                                                              ResourceLocation<FeatureStore> location ) {
        return new ShapeFeatureStoreMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }

}