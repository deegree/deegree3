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
package org.deegree.feature.persistence.geocouch;

import java.net.URL;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.geocouch.jaxb.GeoCouchFeatureStoreConfig;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link GeoCouchFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * 
 */
public class GeoCouchFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( GeoCouchFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/geocouch";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.geocouch.jaxb";

    private static final URL CONFIG_SCHEMA = GeoCouchFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/geocouch/3.1.0/geocouch.xsd" );

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
    public GeoCouchFeatureStore create( URL configURL )
                            throws ResourceInitException {

        GeoCouchFeatureStore fs = null;
        try {
            GeoCouchFeatureStoreConfig config = (GeoCouchFeatureStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                                   CONFIG_SCHEMA,
                                                                                                   configURL, workspace );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );

            String srs = config.getStorageCRS();
            srs = srs.trim();
            ICRS crs = CRSManager.getCRSRef( srs );

            String couchUrl = config.getGeoCouchUrl();
            if ( !couchUrl.endsWith( "/" ) ) {
                couchUrl += "/";
            }

            List<String> configSchemas = config.getGMLSchema();
            String[] schemas = new String[configSchemas.size()];
            int i = -1;
            for ( String s : configSchemas ) {
                schemas[++i] = resolver.resolve( s ).toString();
            }

            GMLAppSchemaReader decoder = new GMLAppSchemaReader( null, null, schemas );
            AppSchema schema = decoder.extractAppSchema();

            fs = new GeoCouchFeatureStore( crs, schema, couchUrl );

        } catch ( Throwable e ) {

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
}