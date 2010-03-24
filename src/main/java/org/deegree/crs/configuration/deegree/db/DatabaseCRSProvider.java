//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.crs.configuration.deegree.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Unit;
import org.deegree.crs.configuration.CRSProvider;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.exceptions.CRSException;
import org.deegree.crs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>DatabaseCRSProvider</code> class is the intermediate class for accessing the Deegree CRS via a database
 * backend. It also initializes the CRSQuerier (for retrieving CRSs from database), CRSExported (for inserting CRSs to
 * database) and CRSRemover (for removing CRSs from database).
 * 
 * In the constructor the database connection is realized in read-only mode (by default, using the classpath
 * subprotocol), but can be set so that database changes are possible (by setting the environment variable CRS_DB_URL).
 * The JDBC driver is also set (CRS_DB_DRIVER variable), as well as the username and password.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class DatabaseCRSProvider implements CRSProvider {

    private static Logger LOG = LoggerFactory.getLogger( DatabaseCRSProvider.class );

    private CRSQuerier querier;

    private CRSDBExporter exporter;

    private CRSRemover remover;

    private static Connection conn = null;

    private static String dbConnectionURL = null;

    private static String dbUser = "";

    private static String dbPass = "";

    private static String dbDriver = null;

    /**
     * Retrieve the internal database ID for the object supplied
     * 
     * @param identifiable
     *            the CRSIdentifiable object
     * @return the internal database ID
     */
    public int getInternalID( CRSIdentifiable identifiable ) {
        return querier.getInternalID( identifiable );
    }

    /**
     * Request an Update into the database for a new Code
     * 
     * @param internalID
     *            the internal database ID of the object
     * @param code
     *            the Code that will be set
     * @throws SQLException
     */
    public void setCode( int internalID, String code )
                            throws SQLException {
        querier.setCode( internalID, code );
    }

    /**
     * @param sourceCRS
     * @param targetCRS
     * @return <code>null</code>
     * @throws CRSConfigurationException
     */
    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSConfigurationException {
        return null;
    }

    /**
     * Retrieves the CRS via its code
     * 
     * @param code
     * @return the CoordinateSystem identified
     * @throws CRSConfigurationException
     */
    public CoordinateSystem getCRSByCode( CRSCodeType code )
                            throws CRSConfigurationException {
        CoordinateSystem result = null;
        try {
            result = querier.getCRSByCode( code );

        } catch ( IllegalArgumentException e ) {
            LOG.warn( "Instantiation for code: " + code.getOriginal() + " could not be forefullfilled, because: "
                      + e.getLocalizedMessage() );
        } catch ( SQLException e ) {
            throw new CRSConfigurationException( "Could not get an CoordinateSystem from code: " + code.getOriginal()
                                                 + " because: " + e.getLocalizedMessage(), e );
        }
        return result;
    }

    /**
     * @throws CRSConfigurationException
     */
    public DatabaseCRSProvider() throws CRSConfigurationException {
        dbConnectionURL = System.getenv( "CRS_DB_URL" );
        if ( dbConnectionURL == null ) {
            dbConnectionURL = "jdbc:derby:classpath:META-INF/deegreeCRS";
        }
        LOG.debug( "using the connection protocol: " + dbConnectionURL );

        dbUser = System.getenv( "CRS_DB_USER" );
        dbPass = System.getenv( "CRS_DB_PASS" );
        dbDriver = System.getenv( "CRS_DB_DRIVER" );
        if ( dbDriver == null )
            dbDriver = "org.apache.derby.jdbc.EmbeddedDriver";

        // connect to the database
        try {
            Class.forName( dbDriver );
            conn = DriverManager.getConnection( dbConnectionURL, dbUser, dbPass );
        } catch ( ClassNotFoundException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( SQLException e ) {
            LOG.error( e.getMessage(), e );
        }

        querier = new CRSQuerier();
        querier.setConnection( conn );
        exporter = new CRSDBExporter();
        exporter.setConnection( conn );
        remover = new CRSRemover();
        remover.setConnection( conn );
    }

    /**
     * 
     * @param properties
     * @throws CRSConfigurationException
     */
    public DatabaseCRSProvider( Properties properties ) throws CRSConfigurationException {
        this(); // currently properties are not needed but the CRSConfiguration instantiation mechanism forces this
        // parameter
    }

    /**
     * @return whether there is an exported or not
     */
    public boolean canExport() {
        return exporter != null;
    }

    /**
     * @return a list of {@link CRSCodeType}s from all the available CRSs
     */
    public List<CRSCodeType> getAvailableCRSCodes() {
        return querier.getAvailableCRSCodes();
    }

    /**
     * @return a list of {@link CoordinateSystem}s of all the available CRSs.
     */
    public List<CoordinateSystem> getAvailableCRSs() {
        try {
            return querier.getAvailableCRSs();
        } catch ( SQLException e ) {
            LOG.error( e.getMessage(), e );
        }
        return null;
    }

    /**
     * @param sb
     * @param crsToExport
     */
    public void export( StringBuilder sb, List<CoordinateSystem> crsToExport ) {
        // TODO modify the super-class signature since here we don't write into a StringBuilder
    }

    /**
     * 
     * @param crsList
     *            a list of {@link CoordinateSystem}s to be removed
     * @throws SQLException
     */
    public void remove( List<CoordinateSystem> crsList )
                            throws SQLException {
        remover.removeCRSList( crsList );
    }

    /**
     * Method used by the synchronization-with-EPSG-database class to update the Axis with the EPSG code that it was
     * lacking.
     * 
     * @param axisName
     * @param axisOrientation
     * @param uom
     * @param code
     *            the EPSG code that will be assigned to the Axis
     * @throws SQLException
     */
    public void changeAxisCode( String axisName, String axisOrientation, Unit uom, CRSCodeType code )
                            throws SQLException {
        querier.changeAxisCode( axisName, axisOrientation, uom, code );
    }

    /**
     * Export a list of CoordianteSystems to the database
     * 
     * @param crsList
     * @throws SQLException
     * @throws CRSException
     */
    public void export( List<CoordinateSystem> crsList )
                            throws SQLException, CRSException {
        String url = System.getenv( "CRS_DB_URL" );
        if ( url == null ) {
            throw new SQLException(
                                    "Please specify the database connection by setting the CRS_DB_URL property (environment setting, for example derby: -DCRS_DB_URL=jdbc:derby:META-INF/deegreeCRS)" );
        }
        exporter.export( crsList );
    }

    /**
     * 
     * @return database connection
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * 
     * @return the remover object that was initialized once the db crs provider was instantiated.
     */
    public CRSRemover getRemover() {
        return remover;
    }

    /**
     * @param id
     *            the {@link CRSCodeType} of the wanted identifiable
     * @return the {@link CRSIdentifiable} object
     */
    @Override
    public CRSIdentifiable getIdentifiable( CRSCodeType id )
                            throws CRSConfigurationException {
        try {
            return querier.getIdentifiable( id );
        } catch ( SQLException e ) {
            LOG.error( e.getMessage(), e );
        }
        return null;
    }
}
