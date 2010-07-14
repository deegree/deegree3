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
package org.deegree.observation.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.observation.model.Property;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class DatastoreConfiguration {

    private final String jdbcConnId;

    private final String tableName;

    private final List<Property> properties = new ArrayList<Property>();

    private final Map<String, String> columnMap = new HashMap<String, String>();

    /**
     * map between option name and option value
     */
    private final Map<String, String> generalOptionsMap = new HashMap<String, String>();

    // private final Map<Property, String> propertyColumnMap = new HashMap<Property, String>();
    //
    // private final Map<String, Property> propertyNameMap = new HashMap<String, Property>();
    //
    // private final Map<String, String> options = new HashMap<String, String>();
    //
    // private final Map<String, Procedure> proceduresID = new HashMap<String, Procedure>();
    //
    // private final Map<String, String> proceduresName = new HashMap<String, String>();

    /**
     * @param jdbcConnId
     * @param tableName
     */
    public DatastoreConfiguration( String jdbcConnId, String tableName ) {
        this.jdbcConnId = jdbcConnId;
        this.tableName = tableName;
    }

    public void addToProperties( Property property ) {
        properties.add( property );
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addToColumnMap( String type, String name ) {
        columnMap.put( type, name );
    }

    /**
     * Add an entry in the general options map.
     * 
     * @param name
     * @param type
     */
    public void addToGenOptionsMap( String name, String type ) {
        generalOptionsMap.put( name, type );
    }

    // /**
    // * Add a mapping between properties and column names.
    // *
    // * @param property
    // * @param columnName
    // */
    // public void addPropertyColumnMapping( Property property, String columnName ) {
    // propertyColumnMap.put( property, columnName );
    // // propertyNameMap.put( property.getName(), property );
    // }
    //
    // /**
    // * Add a ObservationDatastore specific mapping. (eg. id, time, geom,...)
    // *
    // * @param name
    // * @param columnName
    // */
    // public void addDSColumnMapping( String name, String columnName ) {
    // columnMap.put( name, columnName );
    // }
    //
    // /**
    // * @param name
    // * @param value
    // */
    // public void addOption( String name, String value ) {
    // options.put( name, value );
    // }
    //
    // /**
    // * @param id
    // * @param proc
    // */
    // public void addProcedure( String id, Procedure proc ) {
    // proceduresID.put( id, proc );
    // proceduresName.put( proc.getName(), id );
    // }
    //
    // /**
    // * @param property
    // * @return the column name
    // */
    // public String getColumnName( Property property ) {
    // return propertyColumnMap.get( property );
    // }

    //
    // /**
    // * @param name
    // * @return the column name
    // */
    // public String getDSColumnName( String name ) {
    // return columnMap.get( name );
    // }

    // /**
    // * @return a map between property name (its column name) and a map of option name -> option value
    // *
    // */
    // public Map<String, Map<String, String>> getColumnOptionsMap() {
    // return columnOptionsMap;
    // }
    //
    /**
     * @param name
     *            option name
     * @return the option value
     */
    public String getOptionValue( String name ) {
        return generalOptionsMap.get( name );
    }

    /**
     * @return the jdbcConnection
     */
    public String getJdbcConnId() {
        return jdbcConnId;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return tableName + "@" + jdbcConnId;
    }

    public String getColumnName( String columnType ) {
        return columnMap.get( columnType );
    }
}
