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
package org.deegree.observation.persistence.simple;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.observation.persistence.ObservationDatastore;
import org.deegree.observation.persistence.ObservationDatastoreException;
import org.deegree.observation.persistence.ObservationStoreProvider;
import org.deegree.observation.persistence.simplesql.jaxb.ColumnType;
import org.deegree.observation.persistence.simplesql.jaxb.OptionType;
import org.deegree.observation.persistence.simplesql.jaxb.SimpleObservationStore;
import org.deegree.observation.persistence.simplesql.jaxb.SimpleObservationStore.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>SimpleObservationProvider</code> makes available the SimpleObservationStore model. This includes a schema,
 * te
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SimpleObservationProvider implements ObservationStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( SimpleObservationProvider.class );

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/observation/simplesql";
    }

    @Override
    public URL getConfigSchema() {
        return SimpleObservationProvider.class.getResource( "/META-INF/schemas/datasource/observation/0.6.0/simplesql/simplesql.xsd" );
    }

    @Override
    public URL getConfigTemplate() {
        return SimpleObservationProvider.class.getResource( "/META-INF/schemas/datasource/observation/0.6.0/simplesql/example.xml" );
    }

    @Override
    public ObservationDatastore getObservationStore( URL configURL )
                            throws ObservationDatastoreException {
        SimpleObservationDatastore store = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.observation.persistence.simplesql.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            SimpleObservationStore config = (SimpleObservationStore) u.unmarshal( configURL );

            String jdbcId = config.getJDBCConnId();
            String tableName = config.getTable();

            List<ColumnType> columns = config.getColumn();
            Map<String, String> columnMap = new HashMap<String, String>();
            for ( ColumnType col : columns ) {
                columnMap.put( col.getType(), col.getName() );
            }

            List<OptionType> optionTypes = config.getOption();
            Map<String, String> optionMap = new HashMap<String, String>();
            for ( OptionType opt : optionTypes ) {
                optionMap.put( opt.getName(), opt.getValue() );
            }

            List<Property> jaxbProperties = config.getProperty();
            List<org.deegree.observation.model.Property> properties = new ArrayList<org.deegree.observation.model.Property>();
            for ( Property propType : jaxbProperties ) {
                org.deegree.observation.model.Property prop = new org.deegree.observation.model.Property(
                                                                                                          propType.getHref(),
                                                                                                          propType.getColumn().getName() );
                for ( OptionType propOpType : propType.getOption() ) {
                    prop.addToOption( propOpType.getName(), propOpType.getValue() );
                }
                columnMap.put( propType.getHref(), propType.getColumn().getName() );
                properties.add( prop );
            }

            store = new SimpleObservationDatastore( jdbcId, tableName, columnMap, optionMap, properties );

        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new ObservationDatastoreException( msg, e );
        }
        return store;
    }
}
