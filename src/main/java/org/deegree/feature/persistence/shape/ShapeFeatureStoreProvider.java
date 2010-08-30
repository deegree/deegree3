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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.shape.jaxb.ShapeFeatureStoreConfig;
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
public class ShapeFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( ShapeFeatureStoreProvider.class );

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/feature/shape";
    }

    @Override
    public URL getConfigSchema() {
        return ShapeFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/shape/0.6.0/shape.xsd" );
    }

    @Override
    public URL getConfigTemplate() {
        return ShapeFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/shape/0.6.0/example.xml" );
    }

    @Override
    public FeatureStore getFeatureStore( URL configURL )
                            throws FeatureStoreException {

        ShapeFeatureStore fs = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.shape.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            ShapeFeatureStoreConfig config = (ShapeFeatureStoreConfig) u.unmarshal( configURL );
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );

            String srs = config.getStorageCRS();
            CRS crs = null;
            if ( srs != null ) {
                // rb: if it is null, the shape feature store will try to read the prj files.
                // srs = "EPSG:4326";
                // } else {
                srs = srs.trim();
                crs = new CRS( srs );
            }

            String shapeFileName = null;
            try {
                shapeFileName = new File( resolver.resolve( config.getFile().trim() ).toURI() ).toString();
            } catch ( MalformedURLException e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg, e );
                throw new FeatureStoreException( msg, e );
            } catch ( URISyntaxException e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg );
                LOG.trace( "Stack trace:", e );
                throw new FeatureStoreException( msg, e );
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

            // TODO make cache configurable
            Boolean genIdx = config.isGenerateAlphanumericIndexes();
            fs = new ShapeFeatureStore( shapeFileName, crs, cs, config.getNamespace(), config.getFeatureTypeName(),
                                        genIdx == null || genIdx, null );

        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new FeatureStoreException( msg, e );
        }
        return fs;
    }
}
