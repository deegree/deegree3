//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.cs.persistence.gml;

import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.persistence.CRSStoreProvider;
import org.deegree.cs.persistence.gml.jaxb.GMLCRSStoreConfig;
import org.deegree.cs.persistence.gml.jaxb.Param;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.slf4j.Logger;

/**
 * {@link GMLCRSStoreProvider} for {@link GMLCRSStore}
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class GMLCRSStoreProvider implements CRSStoreProvider {

    private static final Logger LOG = getLogger( GMLCRSStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/crs/stores/gml";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.cs.persistence.gml.jaxb";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/crs/stores/gml/3.1.0/gml.xsd";

    private static final String CONFIG_TEMPLATE = "/META-INF/schemas/crs/stores/gml/3.1.0/example.xml";

    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    public URL getConfigSchema() {
        return GMLCRSStoreProvider.class.getResource( CONFIG_SCHEMA );
    }

    public Map<String, URL> getConfigTemplates() {
        return singletonMap( "example", GMLCRSStoreProvider.class.getResource( CONFIG_TEMPLATE ) );
    }

    public CRSStore getCRSStore( URL configURL )
                            throws CRSStoreException {
        try {
            GMLCRSStoreConfig config = (GMLCRSStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                                 configURL );

            GMLCRSStore crsStore = new GMLCRSStore( DSTransform.fromSchema( config ) );
            GMLResource resource = null;

            String resourceClassName = config.getGMLResourceClass();
            if ( resourceClassName != null && resourceClassName.trim().length() > 0 ) {
                try {
                    List<Param> configParams = config.getParam();
                    Map<String, String> params = new HashMap<String, String>();
                    for ( Param param : configParams ) {
                        params.put( param.getName(), param.getValue() );
                    }
                    // use reflection to instantiate the configured resource.
                    Class<?> t = Class.forName( resourceClassName );
                    LOG.debug( "Trying to load configured CRS provider from classname: " + resourceClassName );
                    Constructor<?> constructor = t.getConstructor( Map.class );
                    if ( constructor == null ) {
                        LOG.error( "No constructor ( " + this.getClass() + ", Properties.class) found in class:"
                                   + resourceClassName );
                    } else {
                        resource = (GMLResource) constructor.newInstance( params );
                    }
                } catch ( InstantiationException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, e.getMessage() ) );
                } catch ( IllegalAccessException e ) {
                    LOG.error(
                               Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, e.getMessage() ),
                               e );
                } catch ( ClassNotFoundException e ) {
                    LOG.error(
                               Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, e.getMessage() ),
                               e );
                } catch ( SecurityException e ) {
                    LOG.error(
                               Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, e.getMessage() ),
                               e );
                } catch ( NoSuchMethodException e ) {
                    LOG.error(
                               Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, e.getMessage() ),
                               e );
                } catch ( IllegalArgumentException e ) {
                    LOG.error(
                               Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, e.getMessage() ),
                               e );
                } catch ( InvocationTargetException e ) {
                    LOG.error(
                               Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, e.getMessage() ),
                               e );
                } catch ( Throwable t ) {
                    LOG.error(
                               Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", resourceClassName, t.getMessage() ),
                               t );
                } finally {
                    LOG.info( "The configured class: " + resourceClassName + " was not instantiated." );
                }
            }
            if ( resource == null ) {
                LOG.info( "Trying to instantiate the default GMLFileResource" );
                XMLAdapter adapter = new XMLAdapter( configURL );
                URL resolvedGMLFile = adapter.resolve( config.getGMLFile() );

                resource = new GMLFileResource( crsStore, resolvedGMLFile );
            }

            crsStore.setResolver( resource );
            return crsStore;
        } catch ( JAXBException e ) {
            String msg = "Error in gml crs store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new CRSStoreException( msg, e );
        } catch ( MalformedURLException e ) {
            String msg = "Error in GMLFile declaration in gml crs store configuration file '" + configURL + "': "
                         + e.getMessage();
            LOG.error( msg );
            throw new CRSStoreException( msg, e );
        }
    }

}
