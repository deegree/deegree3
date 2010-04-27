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
package org.deegree.services.sos.storage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.services.sos.model.Procedure;
import org.deegree.services.sos.model.Property;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class DatastoreConfiguration {
    private final String jdbcDriver;

    private final String jdbcConnection;

    private final String tableName;

    private final String username;

    private final String password;

    private final Map<Property, String> propertyColumnMap = new HashMap<Property, String>();

    private final Map<String, Property> propertyNameMap = new HashMap<String, Property>();

    private final Map<String, String> columnMap = new HashMap<String, String>();

    private final Map<String, String> options = new HashMap<String, String>();

    private final Map<String, Procedure> proceduresID = new HashMap<String, Procedure>();

    private final Map<String, String> proceduresName = new HashMap<String, String>();

    /**
     * @param jdbcDriver
     * @param jdbcConnection
     * @param username
     * @param password
     * @param tableName
     */
    public DatastoreConfiguration( String jdbcDriver, String jdbcConnection, String username, String password,
                                   String tableName ) {
        this.jdbcDriver = jdbcDriver;
        this.jdbcConnection = jdbcConnection;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
    }

    /**
     * Add a mapping between properties and column names.
     *
     * @param property
     * @param columnName
     */
    public void addPropertyColumnMapping( Property property, String columnName ) {
        propertyColumnMap.put( property, columnName );
        propertyNameMap.put( property.getName(), property );
    }

    /**
     * Add a ObservationDatastore specific mapping. (eg. id, time, geom,...)
     *
     * @param name
     * @param columnName
     */
    public void addDSColumnMapping( String name, String columnName ) {
        columnMap.put( name, columnName );
    }

    /**
     * @param name
     * @param value
     */
    public void addOption( String name, String value ) {
        options.put( name, value );
    }

    /**
     * @param id
     * @param proc
     */
    public void addProcedure( String id, Procedure proc ) {
        proceduresID.put( id, proc );
        proceduresName.put( proc.getName(), id );
    }

    /**
     * @param property
     * @return the column name
     */
    public String getColumnName( Property property ) {
        return propertyColumnMap.get( property );
    }

    /**
     * @param name
     * @return the column name
     */
    public String getDSColumnName( String name ) {
        return columnMap.get( name );
    }

    /**
     * @param name
     * @return the option value
     */
    public String getOption( String name ) {
        return options.get( name );
    }

    /**
     * @return the jdbcConnection
     */
    public String getJdbcConnection() {
        return jdbcConnection;
    }

    /**
     * @return the jdbcDriver
     */
    public String getJdbcDriver() {
        return jdbcDriver;
    }

    /**
     * @return the propertyColumnMap
     */
    public Map<Property, String> getPropertyColumnMap() {
        return propertyColumnMap;
    }

    /**
     * Returns the procedure for the given ID. If the ID is null or empty, it will return the first procedure. That way
     * you can use datastores with one procedure without additional configuration.
     *
     * @param id
     * @return the procedure with id or <code>null</code>
     */
    public Procedure getProcedure( String id ) {
        if ( ( id == null || id.equals( "" ) ) && ( proceduresID.size() > 0 ) ) {
            return proceduresID.values().iterator().next();
        }
        return proceduresID.get( id );
    }

    /**
     * @param procedureName
     * @return the procedure id for the procedure name
     */
    public String getProcedureIDFromName( String procedureName ) {
        return proceduresName.get( procedureName );
    }

    /**
     * @return all procedures of this datastore
     */
    public List<Procedure> getProcedures() {
        return new LinkedList<Procedure>( proceduresID.values() );
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return jdbcDriver + ": " + tableName + "@" + jdbcConnection;
    }

    /**
     * @param name
     *            the property name
     * @return the property object
     */
    public Property getPropertyFromName( String name ) {
        return propertyNameMap.get( name );
    }

}
