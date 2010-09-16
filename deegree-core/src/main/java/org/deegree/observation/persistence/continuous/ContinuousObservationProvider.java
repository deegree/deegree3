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
package org.deegree.observation.persistence.continuous;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.observation.model.Property;
import org.deegree.observation.persistence.ObservationDatastore;
import org.deegree.observation.persistence.ObservationDatastoreException;
import org.deegree.observation.persistence.ObservationStoreProvider;
import org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ContinuousObservationProvider implements ObservationStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( ContinuousObservationProvider.class );

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/observation/contsql";
    }

    @Override
    public URL getConfigSchema() {
        return ContinuousObservationProvider.class.getResource( "/META-INF/schemas/datasource/observation/0.6.0/contsql/contsql.xsd" );
    }

    @Override
    public URL getConfigTemplate() {
        return ContinuousObservationProvider.class.getResource( "/META-INF/schemas/datasource/observation/0.6.0/contsql/example.xml" );
    }

    @Override
    public ObservationDatastore getObservationStore( URL configURL )
                            throws ObservationDatastoreException {
        ContinuousObservationDatastore store = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.observation.persistence.contsql.jaxb" );
            Unmarshaller u = jc.createUnmarshaller();
            ContinuousObservationStore config = (ContinuousObservationStore) u.unmarshal( configURL );

            String jdbcId = config.getJDBCConnId();
            String tableName = config.getTable();

            Map<String, String> columnMap = new HashMap<String, String>();
            List<org.deegree.observation.persistence.contsql.jaxb.ColumnType> columns = config.getColumn();
            for ( org.deegree.observation.persistence.contsql.jaxb.ColumnType col : columns ) {
                columnMap.put( col.getType(), col.getName() );
            }

            Map<String, String> optionsMap = new HashMap<String, String>();
            List<org.deegree.observation.persistence.contsql.jaxb.OptionType> optionTypes = config.getOption();
            for ( org.deegree.observation.persistence.contsql.jaxb.OptionType opt : optionTypes ) {
                optionsMap.put( opt.getName(), opt.getValue() );
            }

            List<org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore.Property> jaxbProps = config.getProperty();
            List<Property> properties = new ArrayList<Property>();
            for ( org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore.Property propType : jaxbProps ) {
                org.deegree.observation.model.Property prop = new org.deegree.observation.model.Property(
                                                                                                          propType.getHref(),
                                                                                                          propType.getColumn().getName() );
                for ( org.deegree.observation.persistence.contsql.jaxb.OptionType propOpType : propType.getOption() ) {
                    prop.addToOption( propOpType.getName(), propOpType.getValue() );
                }
                properties.add( prop );
                columnMap.put( propType.getHref(), propType.getColumn().getName() );
            }

            store = new ContinuousObservationDatastore( jdbcId, tableName, columnMap, optionsMap, properties );

        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new ObservationDatastoreException( msg, e );
        }
        return store;
    }

}
