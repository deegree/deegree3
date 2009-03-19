//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs.configuration.deegree.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.deegree.model.crs.CRSCodeType;
import org.deegree.model.crs.CRSIdentifiable;
import org.deegree.model.crs.EPSGCode;
import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.configuration.CRSProvider;
import org.deegree.model.crs.configuration.deegree.xml.CRSParser;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.CRSConfigurationException;
import org.deegree.model.crs.exceptions.CRSExportingException;
import org.deegree.model.crs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>DatabaseCRSProvider</code> class is the intermediate class for accessing 
 * the Deegree CRS via a database backend. The <code>CRSQuerier</code> imports the CRS's 
 * from the database, while the reverse operation is accomplished by the 
 * <code>CRSToDBExporter</code> class. Here also reside the connecting- and disconnecting
 * from the database methods that are necessary at the beginning and end of each 
 * import/export operation. Regarding the exporting to the database, currently there is
 * a need to fill in some gaps in the EPSG Codes, especially for Projections and Axes.
 * The class <code>EPSGDatabaseSynchronizer</code> creates takes care of this operation
 * and here there is a method that instantiates and runs its operations.      
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

    private CRSExporter exporter;

    private Connection conn = null;

    private String dbConnectionURL = null;

    private String dbDriver = null;

    private static String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static String JDBC_PREFIX = "jdbc:derby:";

    private static String DEFAULT_DRIVER = JDBC_DRIVER;

    private static String DEFAULT_DBNAME = "/home/ionita/DerbyDB/CRS";

    private final static String DEFAULT_DB_PROTOCOL = JDBC_PREFIX;       

    /**
     * Connect to the database provided using the driver name provided. 
     * Please notice that this assumes that the database and its tables are already created! 
     * @param driver
     *          the driver Class as String
     * @param dbName
     *          the database name 
     * @throws ClassNotFoundException 
     */
    public void connectToDatabase( String driver, String dbName) {
        try {
            Class.forName( driver );
        } catch ( ClassNotFoundException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String connectionURL = null;
        if ( driver.equalsIgnoreCase( JDBC_DRIVER ) )
            connectionURL = JDBC_PREFIX + dbName + ";";
        // TODO add here more URL constructions using other PREFIXES 

        try {
            conn = DriverManager.getConnection( connectionURL );
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Could not connect to the database when using the connectionURL: " + connectionURL  );
        }
    }

    public void connectToDatabase() {
        connectToDatabase( DEFAULT_DRIVER, DEFAULT_DBNAME );
    }

    public void closeDatabaseConnection() {
        try {
            conn.close();
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the internal database ID for the object supplied 
     * @param identifiable
     *          the CRSIdentifiable object
     * @return
     *          the internal database ID
     * @throws SQLException
     */
    public int getInternalID( CRSIdentifiable identifiable ) throws SQLException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        int result = querier.getInternalID( identifiable );
        return result;
    }

    /**
     * Request an Update into the database for a new Code  
     * @param internalID
     *              the internal database ID of the object 
     * @param codeInt
     *              the Code that will be set
     * @throws SQLException
     */
    public void setCode( int internalID, int codeInt ) throws SQLException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        querier.setCode( internalID, codeInt );
    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) throws CRSConfigurationException { 
        return null;
    }

    /**
     * Get any object that is identified by the supplied String
     */
    public CRSIdentifiable getIdentifiable( String id ) throws CRSConfigurationException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        CRSIdentifiable result = querier.getIdentifiable( id );
        return result;
    }

    /**
     * Get the CRS that is identified by the supplied String
     * 
     */
    public CoordinateSystem getCRSByID( String id ) throws CRSConfigurationException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        try {
            CoordinateSystem result = querier.getCRSByCode( id );
            return result;
        } catch ( IllegalArgumentException e ) {
            // the getCRSByID() method could also have the throws IllegalArgumentException clause but then that does not conform with the CRSParser definition
            e.printStackTrace();
        } catch ( SQLException e ) {
            // the getCRSById() method could also have the throws SQLException clause but then that does not conform with the CRSParser definition            
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the CRS via a EPSG code type
     * @param epsgCode
     * @return
     *          the CoordinateSystem identified
     * @throws CRSConfigurationException
     */
    public CoordinateSystem getCRSByEPSGCode( EPSGCode epsgCode ) throws CRSConfigurationException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        CoordinateSystem result = querier.getCRSByEPSGCode( epsgCode );
        closeDatabaseConnection();
        return result;
    }

    /**
     * @param properties
     *            containing information about the crs resource class and the file location of the crs configuration. If
     *            either is null the default mechanism is using the {@link CRSParser} and the
     *            deegree-crs-configuration.xml
     * @throws CRSConfigurationException
     *             if the give file or the default-crs-configuration.xml file could not be loaded.
     */
    public DatabaseCRSProvider( Properties properties ) throws CRSConfigurationException {
        dbDriver = (String) properties.get( "DB_DRIVER" );
        String dbProtocol = (String) properties.get("DB_PROTOCOL");
        String dbPath = (String) properties.get( "DB_PATH" );
        dbConnectionURL = dbProtocol + dbPath + ";";

        querier = new CRSQuerier();
        exporter = new CRSExporter( new Properties( properties ) );
    }

    public DatabaseCRSProvider() {
        dbDriver = DEFAULT_DRIVER;
        String dbProtocol = DEFAULT_DB_PROTOCOL;
        String dbPath = DEFAULT_DBNAME;
        dbConnectionURL = dbProtocol + dbPath + ";";

        querier = new CRSQuerier();
        exporter = new CRSExporter( null );
    }

    public boolean canExport() {
        return exporter != null;
    }

    // TODO deprecate it
    public List<String> getAvailableCRSIds() {
        List<CRSCodeType> codes;
        codes = querier.getAvailableCRSCodes();
        List<String> ids = new LinkedList<String>();
        for ( CRSCodeType code : codes )
            ids.add( code.getEquivalentString() );
        return ids;
    }

    /**
     * Get the Codes of all the CRS's in the database 
     * @return 
     *          a List of CRSCodeTypes
     * @throws SQLException
     */
    public List<CRSCodeType> getAvailableCRSCodes() throws SQLException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        List<CRSCodeType> result = querier.getAvailableCRSCodes();
        closeDatabaseConnection();
        return result;
    }

    /**
     * Get all the CRS's present in the database
     * @return
     *      a List of CoordinateSystems
     */
    public List<CoordinateSystem> getAvailableCRSs() {
        querier.setConnection( conn );
        try {
            List<CoordinateSystem> result = querier.getAvailableCRSs();
            closeDatabaseConnection();
            return result;
        } catch ( SQLException e ) {   
            LOG.error( e.getMessage() ); 
        }
        return null;
        //        List<CoordinateSystem> allSystems = new LinkedList<CoordinateSystem>();
        //        List<String> allCRSCodes = querier.getAvailableCRSCodes();
        //        final int total = allCRSCodes.size();
        //        int count = 0;
        //        int percentage = (int) Math.round( total / 100.d );
        //        int number = 0;
        //        System.out.println( "Trying to create a total of " + total + " coordinate systems." );
        //        for ( String crsCode : allCRSCodes ) {
        //            if ( crsCode != null ) {
        //                //String id = crsID.getTextContent();
        //                if ( crsCode != null && !"".equals( crsCode.trim() ) ) {
        //                    if ( count++ % percentage == 0 ) {
        //                        System.out.print( "\r" + ( number ) + ( ( number++ < 10 ) ? "  " : " " ) + "% created" );
        //                    }
        //                    allSystems.add( querier.getCRSbyCode(crsCode ) );
        //                }
        //            }
        //        }
        //        querier.closeDatabase();
        //        System.out.println();
        //        return allSystems;
    }

    public void export( StringBuilder sb, List<CoordinateSystem> crsToExport ) {
        // TODO to modify the super-class signature since here we don't write into a StringBuilder
    }

    //TODO Do something about the ID in the name (change it to Code).
    @Override
    public CoordinateSystem getCRSByID( CRSCodeType id ) throws CRSConfigurationException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        try {
            CoordinateSystem result = querier.getCRSByID( id );
            closeDatabaseConnection();
            return result;
        } catch ( SQLException e ) {
            // Would have used a throws clause, however that is not defined in the CRSProvider interface 
            LOG.error( e.getMessage() );
            return null;
        }
    }

    /**
     * Method used by the synchronization-with-EPSG-database class to update the Axis with the EPSG code that it was lacking. 
     * @param axisName         
     * @param axisOrientation
     * @param uom
     * @param code
     *         the EPSG code that will be assigned to the Axis
     * @throws SQLException 
     */
    public void changeAxisCode( String axisName, String axisOrientation, Unit uom, EPSGCode code ) throws SQLException {
        if ( ! querier.connectionAlreadySet() )
            querier.setConnection( conn );
        querier.changeAxisCode( axisName, axisOrientation, uom, code );        
    }

    /**
     * Export a list of CoordianteSystems to the database
     * @param crsList
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void export( List<CoordinateSystem> crsList ) throws ClassNotFoundException, SQLException {
        CRSExporter exporter = new CRSExporter(); 
        connectToDatabase();
        exporter.setConnection( conn );
        exporter.export( crsList );
        closeDatabaseConnection();
    }    
}
