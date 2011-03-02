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
package org.deegree.feature.persistence.oracle;

import static java.util.Collections.singletonMap;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.WorkspaceInitializationException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.oracle.jaxb.OracleFeatureStoreConfig;
import org.deegree.feature.persistence.oracle.jaxb.OracleFeatureStoreConfig.NamespaceHint;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link OracleFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OracleFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( OracleFeatureStoreProvider.class );

    private DeegreeWorkspace workspace;

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/feature/oracle";
    }

    @Override
    public URL getConfigSchema() {
        return OracleFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/0.5.0/feature/oracle.xsd" );
    }

    @Override
    public Map<String, URL> getConfigTemplates() {
        String loc = "/META-INF/schemas/datasource/0.5.0/feature/example.xml";
        return singletonMap( "example", OracleFeatureStoreProvider.class.getResource( loc ) );
    }

    @Override
    public OracleFeatureStore create( URL configURL )
                            throws WorkspaceInitializationException {

        OracleFeatureStore fs = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.oracle.jaxb",
                                                      workspace.getModuleClassLoader() );
            Unmarshaller u = jc.createUnmarshaller();
            OracleFeatureStoreConfig config = (OracleFeatureStoreConfig) u.unmarshal( configURL );
            ICRS storageSRS = CRSManager.getCRSRef( config.getStorageSRS() );
            MappedApplicationSchema schema = OracleApplicationSchemaBuilder.build( null, config.getFeatureType(),
                                                                                   config.getJDBCConnId(),
                                                                                   config.getDBSchemaQualifier(),
                                                                                   storageSRS );
            fs = new OracleFeatureStore( schema, config.getJDBCConnId() );
        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new WorkspaceInitializationException( msg, e );
        } catch ( SQLException e ) {
            String msg = "Error creating mapped application schema: " + e.getMessage();
            LOG.error( msg );
            throw new WorkspaceInitializationException( msg, e );
        } catch ( FeatureStoreException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new WorkspaceInitializationException( msg, e );
        }
        return fs;
    }

    private static Map<String, String> getHintMap( List<NamespaceHint> hints ) {
        Map<String, String> prefixToNs = new HashMap<String, String>();
        for ( NamespaceHint namespaceHint : hints ) {
            prefixToNs.put( namespaceHint.getPrefix(), namespaceHint.getNamespaceURI() );
        }
        return prefixToNs;
    }

    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class, ConnectionManager.class };
    }
}
