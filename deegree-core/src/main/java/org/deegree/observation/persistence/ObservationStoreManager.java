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
package org.deegree.observation.persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.feature.i18n.Messages;
import org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore;
import org.deegree.observation.persistence.simplesql.jaxb.ColumnType;
import org.deegree.observation.persistence.simplesql.jaxb.OptionType;
import org.deegree.observation.persistence.simplesql.jaxb.SimpleObservationStore;
import org.deegree.observation.persistence.simplesql.jaxb.SimpleObservationStore.Property;
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
public class ObservationStoreManager {

    private static final Logger LOG = LoggerFactory.getLogger( ObservationStoreManager.class );

    private static Map<String, ObservationDatastore> idToOds = Collections.synchronizedMap( new HashMap<String, ObservationDatastore>() );

    /**
     * @param osDir
     */
    public static void init( File osDir ) {
        if ( !osDir.exists() ) {
            LOG.info( "No 'datasources/observation' directory -- skipping initialization of observation stores." );
            return;
        }
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up observation stores." );
        LOG.info( "--------------------------------------------------------------------------------" );

        File[] osConfigFiles = osDir.listFiles( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.toLowerCase().endsWith( ".xml" );
            }
        } );
        for ( File osConfigFile : osConfigFiles ) {
            String fileName = osConfigFile.getName();
            // 4 is the length of ".xml"
            String odsId = fileName.substring( 0, fileName.length() - 4 );
            LOG.info( "Setting up observation store '" + odsId + "' from file '" + fileName + "'..." + "" );
            try {
                ObservationDatastore ods = create( osConfigFile.toURI().toURL() );
                register( odsId, ods );
            } catch ( Exception e ) {
                LOG.error( "Error creating feature store: " + e.getMessage(), e );
            }
        }
        LOG.info( "" );
    }

    /**
     * @param odsId
     * @param ods
     * @throws ObservationDatastoreException
     */
    private static void register( String odsId, ObservationDatastore ods )
                            throws ObservationDatastoreException {
        if ( odsId != null ) {
            if ( idToOds.containsKey( odsId ) ) {
                String msg = Messages.getMessage( "STORE_MANAGER_DUPLICATE_ID", odsId );
                throw new ObservationDatastoreException( msg );
            }
            LOG.info( "Registering global observation store with id '" + odsId + "', type: '"
                      + ods.getClass().getName() + "'" );
            idToOds.put( odsId, ods );
        }
    }

    /**
     * @param url
     * @throws ObservationDatastoreException
     */
    private static ObservationDatastore create( URL url )
                            throws ObservationDatastoreException {
        XMLAdapter adapter = new XMLAdapter( url );
        ObservationStoreXMLAdapter storeAdapter = new ObservationStoreXMLAdapter();
        storeAdapter.setRootElement( adapter.getRootElement() );
        storeAdapter.setSystemId( adapter.getSystemId() );

        ObservationDatastore datastore = null;
        if ( "SimpleObservationStore".equals( adapter.getRootElement().getQName().getLocalPart() ) ) {
            SimpleObservationStore simpleStore = storeAdapter.parseSimple();
            DatastoreConfiguration dsConf = getSimpleStoreConfig( simpleStore );
            datastore = new SimpleObservationDatastore( dsConf );

        } else if ( "ContinuousObservationStore".equals( adapter.getRootElement().getQName().getLocalPart() ) ) {
            ContinuousObservationStore contStore = storeAdapter.parseContinuous();
            DatastoreConfiguration dsConf = getContStoreConfig( contStore );
            datastore = new ContinuousObservationDatastore( dsConf );

        } else if ( "BinarySQLObservationStore".equals( adapter.getRootElement().getQName().getLocalPart() ) ) {
            // TODO
            throw new UnsupportedOperationException(
                                                     "Unfortunately, Binary Observation stores are not supported at the moment." );
        }
        return datastore;
    }

    /**
     * @param contStore
     */
    private static DatastoreConfiguration getContStoreConfig( ContinuousObservationStore contStore ) {
        String jdbcId = contStore.getJDBCConnId();
        String tableName = contStore.getTable();

        DatastoreConfiguration dsConf = new DatastoreConfiguration( jdbcId, tableName );
        List<org.deegree.observation.persistence.contsql.jaxb.ColumnType> columns = contStore.getColumn();
        for ( org.deegree.observation.persistence.contsql.jaxb.ColumnType col : columns ) {
            dsConf.addToColumnMap( col.getType(), col.getName() );
        }

        List<org.deegree.observation.persistence.contsql.jaxb.OptionType> optionTypes = contStore.getOption();
        for ( org.deegree.observation.persistence.contsql.jaxb.OptionType opt : optionTypes ) {
            dsConf.addToGenOptionsMap( opt.getName(), opt.getValue() );
        }

        List<org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore.Property> properties = contStore.getProperty();
        for ( org.deegree.observation.persistence.contsql.jaxb.ContinuousObservationStore.Property propType : properties ) {
            org.deegree.observation.model.Property prop = new org.deegree.observation.model.Property(
                                                                                                      propType.getHref(),
                                                                                                      propType.getColumn().getName() );
            for ( org.deegree.observation.persistence.contsql.jaxb.OptionType propOpType : propType.getOption() ) {
                prop.addToOption( propOpType.getName(), propOpType.getValue() );
            }
            dsConf.addToColumnMap( propType.getHref(), propType.getColumn().getName() );
            dsConf.addToProperties( prop );
        }
        return dsConf;
    }

    /**
     * @param simpleStore
     */
    private static DatastoreConfiguration getSimpleStoreConfig( SimpleObservationStore simpleStore ) {
        String jdbcId = simpleStore.getJDBCConnId();
        String tableName = simpleStore.getTable();

        DatastoreConfiguration dsConf = new DatastoreConfiguration( jdbcId, tableName );
        List<ColumnType> columns = simpleStore.getColumn();
        for ( ColumnType col : columns ) {
            dsConf.addToColumnMap( col.getType(), col.getName() );
        }

        List<OptionType> optionTypes = simpleStore.getOption();
        for ( OptionType opt : optionTypes ) {
            dsConf.addToGenOptionsMap( opt.getName(), opt.getValue() );
        }

        List<Property> properties = simpleStore.getProperty();
        for ( Property propType : properties ) {
            org.deegree.observation.model.Property prop = new org.deegree.observation.model.Property(
                                                                                                      propType.getHref(),
                                                                                                      propType.getColumn().getName() );
            for ( OptionType propOpType : propType.getOption() ) {
                prop.addToOption( propOpType.getName(), propOpType.getValue() );
            }
            dsConf.addToColumnMap( propType.getHref(), propType.getColumn().getName() );
            dsConf.addToProperties( prop );
        }
        return dsConf;
    }

    /**
     * @param datastoreId
     * @return a new store
     * @throws ObservationDatastoreException
     */
    public static ObservationDatastore getDatastoreById( String datastoreId )
                            throws ObservationDatastoreException {
        if ( idToOds.containsKey( datastoreId ) ) {
            return idToOds.get( datastoreId );
        }
        throw new ObservationDatastoreException( "The requested datastore id " + datastoreId
                                                 + " is not registered in the ObservationStoreManager" );
    }

    /**
     * @param datastoreId
     * @return true, if it does
     */
    public static boolean containsDatastore( String datastoreId ) {
        return idToOds.containsKey( datastoreId );
    }

    // private ObservationOffering createObservationOffering( Offering conf )
    // throws SOSConfigurationException {
    // Map<Property, String> properties = getProperties( conf );
    // ObservationDatastore ds = createObservationDatastore( conf, properties, procedures );
    // String name = conf.getName();
    // String id = generateID( name );
    // ObservationOffering result = new ObservationOffering( id, name, ds, procedures.keySet(), properties.keySet() );
    // return result;
    // }

    // private Map<Procedure, String> getProcedures( Offering conf ) {
    // Map<Procedure, String> procedures = new HashMap<Procedure, String>();
    // for ( org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Procedure p : conf.getProcedure() ) {
    // try {
    // Point pos = getPosition( getOptionValue( p.getOption(), "long_lat_position" ) );
    // URL resolvedURL = adapter.resolve( p.getSensor().getHref() );
    // Procedure procedure = new Procedure( p.getHref(), resolvedURL.toExternalForm(),
    // p.getFeatureOfInterest().getHref(), pos );
    // String id = getOptionValue( p.getOption(), "value" );
    // procedures.put( procedure, id );
    // } catch ( MalformedURLException e ) {
    // LOG.error( "Couldn't parse sensor location for {}. Ignoring this procedure!", p.getHref() );
    // continue;
    // }
    // }
    // return procedures;
    // }

    // private Point getPosition( String pos ) {
    // if ( pos != null ) {
    // try {
    // GeometryFactory geomFactory = new GeometryFactory();
    // String[] coords = pos.split( " " );
    // double x = Double.parseDouble( coords[0] );
    // double y = Double.parseDouble( coords[1] );
    //
    // CRS crs = new CRS( "EPSG:4326" );
    //
    // // TODO precision
    // return geomFactory.createPoint( null, new double[] { x, y }, crs );
    // } catch ( Exception e ) {
    // LOG.warn( "unable to parse long_lat_position (" + pos + "): " + e.getMessage() );
    // e.printStackTrace();
    // }
    // }
    // return null;
    // }

    // private String getOptionValue( List<Option> options, String name ) {
    // for ( Option option : options ) {
    // if ( option.getName().equals( name ) ) {
    // return option.getValue();
    // }
    // }
    // return null;
    // }

    // private String generateID( String name ) {
    // int i = name.lastIndexOf( ":" );
    // if ( i != 0 ) {
    // return "ID_" + name.substring( i + 1 );
    // }
    // return "ID_" + name;
    // }

    // private ObservationDatastore createObservationDatastore( Offering conf, Map<Property, String> properties,
    // Map<Procedure, String> procedures )
    // throws SOSConfigurationException {
    // Datastore odsConf = conf.getDatastore();
    // String jdbcId = odsConf.getJDBCConnId();
    // DatastoreConfiguration dsConf = new DatastoreConfiguration( jdbcId, odsConf.getTable() );
    // for ( Column column : odsConf.getColumn() ) {
    // dsConf.addDSColumnMapping( column.getType(), column.getName() );
    // }
    // for ( Option option : odsConf.getOption() ) {
    // dsConf.addOption( option.getName(), option.getValue() );
    // }
    // for ( Entry<Property, String> prop : properties.entrySet() ) {
    // dsConf.addPropertyColumnMapping( prop.getKey(), prop.getValue() );
    // }
    // for ( Entry<Procedure, String> proc : procedures.entrySet() ) {
    // dsConf.addProcedure( proc.getValue(), proc.getKey() );
    // }
    // try {
    // Constructor<?> constr = Class.forName( odsConf.getClazz() ).getConstructor( dsConf.getClass() );
    // ObservationDatastore ods = (ObservationDatastore) constr.newInstance( dsConf );
    //
    // return ods;
    // } catch ( SecurityException e ) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch ( NoSuchMethodException e ) {
    // throw new SOSConfigurationException( "missing constructor in " + odsConf.getClazz() );
    // } catch ( ClassNotFoundException e ) {
    // throw new SOSConfigurationException( "configured ObservationDatastore " + odsConf.getClazz() + " not found" );
    // } catch ( IllegalArgumentException e ) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch ( InstantiationException e ) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch ( IllegalAccessException e ) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch ( InvocationTargetException e ) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // return null;
    //
    // }
    //
    // private Map<Property, String> getProperties( Offering conf ) {
    // Map<Property, String> result = new HashMap<Property, String>();
    // for ( org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Property prop : conf.getProperty() ) {
    // String name = prop.getHref();
    // Column propColumn = prop.getColumn().get( 0 );
    // Option propOption = prop.getOption().get( 0 );
    // Property p = new Property( name, propColumn.getName(), propOption.getValue() );
    // result.put( p, propColumn.getName() );
    // }
    // return result;
    // }

}
