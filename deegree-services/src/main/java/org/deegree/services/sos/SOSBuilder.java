//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.services.sos;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.CRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.services.jaxb.sos.Column;
import org.deegree.services.jaxb.sos.Option;
import org.deegree.services.jaxb.sos.ServiceConfiguration;
import org.deegree.services.jaxb.sos.ServiceConfiguration.Offering;
import org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Datastore;
import org.deegree.services.sos.model.Procedure;
import org.deegree.services.sos.model.Property;
import org.deegree.services.sos.offering.ObservationOffering;
import org.deegree.services.sos.storage.DatastoreConfiguration;
import org.deegree.services.sos.storage.ObservationDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SOSBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( SOSBuilder.class );

    private final ServiceConfigurationXMLAdapter adapter;

    /**
     * @param config
     */
    public SOSBuilder( ServiceConfigurationXMLAdapter config ) {
        this.adapter = config;
    }

    /**
     * Create a SOS configuration from a config file.
     * 
     * @param config
     * @return a new SOSConfiguration
     * @throws SOSConfigurationException
     */
    public static SOService createService( URL config )
                            throws SOSConfigurationException {
        SOService result = null;
        try {
            XMLAdapter adapter = new XMLAdapter( config );
            NamespaceContext ctxt = new NamespaceContext();
            ctxt.addNamespace( "sos", "http://www.deegree.org/services/sos" );

            OMElement serviceConf = adapter.getElement( adapter.getRootElement(),
                                                        new XPath( "/sos:ServiceConfiguration", ctxt ) );
            if ( serviceConf == null ) { // maybe its a web service configuration
                serviceConf = adapter.getElement( adapter.getRootElement(),
                                                  new XPath( "/sos:deegreeSOS/sos:ServiceConfiguration", ctxt ) );
            }

            ServiceConfigurationXMLAdapter serviceAdapter = new ServiceConfigurationXMLAdapter();
            serviceAdapter.setRootElement( serviceConf );
            serviceAdapter.setSystemId( adapter.getSystemId() );

            result = createService( serviceAdapter );
        } catch ( XMLProcessingException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Create a SOS configuration from a XMLStreamReader.
     * 
     * @param config
     * @return a new SOSConfiguration
     * @throws SOSConfigurationException
     */
    public static SOService createService( ServiceConfigurationXMLAdapter config )
                            throws SOSConfigurationException {
        SOSBuilder builder = new SOSBuilder( config );
        return builder.buildService();
    }

    private SOService buildService()
                            throws SOSConfigurationException {
        SOService result = new SOService();
        ServiceConfiguration serviceConf = adapter.parse();
        for ( Offering conf : serviceConf.getOffering() ) {
            result.addOffering( createObservationOffering( conf ) );
        }
        return result;
    }

    private ObservationOffering createObservationOffering( Offering conf )
                            throws SOSConfigurationException {
        Map<Property, String> properties = getProperties( conf );
        Map<Procedure, String> procedures = getProcedures( conf );
        ObservationDatastore ds = createObservationDatastore( conf, properties, procedures );
        String name = conf.getName();
        String id = generateID( name );
        ObservationOffering result = new ObservationOffering( id, name, ds, procedures.keySet(), properties.keySet() );
        return result;
    }

    private Map<Procedure, String> getProcedures( Offering conf ) {
        Map<Procedure, String> procedures = new HashMap<Procedure, String>();
        for ( org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Procedure p : conf.getProcedure() ) {
            try {
                Point pos = getPosition( getOptionValue( p.getOption(), "long_lat_position" ) );
                URL resolvedURL = adapter.resolve( p.getSensor().getHref() );
                Procedure procedure = new Procedure( p.getHref(), resolvedURL.toExternalForm(),
                                                     p.getFeatureOfInterest().getHref(), pos );
                String id = getOptionValue( p.getOption(), "value" );
                procedures.put( procedure, id );
            } catch ( MalformedURLException e ) {
                LOG.error( "Couldn't parse sensor location for {}. Ignoring this procedure!", p.getHref() );
                continue;
            }
        }
        return procedures;
    }

    private Point getPosition( String pos ) {
        if ( pos != null ) {
            try {
                GeometryFactory geomFactory = new GeometryFactory();
                String[] coords = pos.split( " " );
                double x = Double.parseDouble( coords[0] );
                double y = Double.parseDouble( coords[1] );

                CRS crs = new CRS( "EPSG:4326" );

                // TODO precision
                return geomFactory.createPoint( null, new double[] { x, y }, crs );
            } catch ( Exception e ) {
                LOG.warn( "unable to parse long_lat_position (" + pos + "): " + e.getMessage() );
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getOptionValue( List<Option> options, String name ) {
        for ( Option option : options ) {
            if ( option.getName().equals( name ) ) {
                return option.getValue();
            }
        }
        return null;
    }

    private String generateID( String name ) {
        int i = name.lastIndexOf( ":" );
        if ( i != 0 ) {
            return "ID_" + name.substring( i + 1 );
        }
        return "ID_" + name;
    }

    private ObservationDatastore createObservationDatastore( Offering conf, Map<Property, String> properties,
                                                             Map<Procedure, String> procedures )
                            throws SOSConfigurationException {
        Datastore odsConf = conf.getDatastore();
        String jdbcId = odsConf.getJDBCConnId();
        DatastoreConfiguration dsConf = new DatastoreConfiguration( jdbcId, odsConf.getTable() );
        for ( Column column : odsConf.getColumn() ) {
            dsConf.addDSColumnMapping( column.getType(), column.getName() );
        }
        for ( Option option : odsConf.getOption() ) {
            dsConf.addOption( option.getName(), option.getValue() );
        }
        for ( Entry<Property, String> prop : properties.entrySet() ) {
            dsConf.addPropertyColumnMapping( prop.getKey(), prop.getValue() );
        }
        for ( Entry<Procedure, String> proc : procedures.entrySet() ) {
            dsConf.addProcedure( proc.getValue(), proc.getKey() );
        }
        try {
            Constructor<?> constr = Class.forName( odsConf.getClazz() ).getConstructor( dsConf.getClass() );
            ObservationDatastore ods = (ObservationDatastore) constr.newInstance( dsConf );

            return ods;
        } catch ( SecurityException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( NoSuchMethodException e ) {
            throw new SOSConfigurationException( "missing constructor in " + odsConf.getClazz() );
        } catch ( ClassNotFoundException e ) {
            throw new SOSConfigurationException( "configured ObservationDatastore " + odsConf.getClazz() + " not found" );
        } catch ( IllegalArgumentException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InvocationTargetException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    private Map<Property, String> getProperties( Offering conf ) {
        Map<Property, String> result = new HashMap<Property, String>();
        for ( org.deegree.services.jaxb.sos.ServiceConfiguration.Offering.Property prop : conf.getProperty() ) {
            String name = prop.getHref();
            Column propColumn = prop.getColumn().get( 0 );
            Option propOption = prop.getOption().get( 0 );
            Property p = new Property( name, propColumn.getName(), propOption.getValue() );
            result.put( p, propColumn.getName() );
        }
        return result;
    }

}
