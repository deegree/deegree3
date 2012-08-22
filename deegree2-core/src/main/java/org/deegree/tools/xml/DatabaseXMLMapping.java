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
package org.deegree.tools.xml;

import java.io.File;

import java.io.FileReader;
import java.io.StringReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.sql.postgis.PGgeometryAdapter;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.CommonNamespaces;
import org.postgis.PGgeometry;
import org.postgis.binary.BinaryParser;
import org.w3c.dom.Element;

/**
 * reads a relational database model and transforms it into XML. Via a XSL script it is possible to transform the XML
 * into any other XML format. The program uses following configuration
 * <pre>
 * <?xml version="1.0" encoding="UTF-8"?>
 * <dor:XMLMapping xmlns:dor="http://www.deegree.org/xmlmapping">
 *     <JDBCConnection xmlns="http://www.deegree.org/jdbc">
 *         <Driver>org.postgresql.Driver</Driver>
 *         <Url>jdbc:postgresql://localhost:5432/csw</Url>
 *         <User>aUser</User>
 *         <Password>aPassword</Password>
 *         <SecurityConstraints/>
 *         <Encoding>iso-8859-1</Encoding>
 *     </JDBCConnection>
 *     <dor:XSLT>e:/temp/test.xsl</dor:XSLT>
 *     <dor:Table>
 *         <dor:ElementName>Metadata</dor:ElementName>
 *         <dor:Select>Select * From cqp_main</dor:Select>
 *         <dor:Table>
 *             <dor:ElementName>domainconsistency</dor:ElementName>
 *             <dor:Select>Select * From cqp_domainconsistency where fk_cqp_main = $ID</dor:Select>
 *             <dor:Table>
 *                 <dor:ElementName>specificationdate</dor:ElementName>
 *                 <dor:Select>Select * From cqp_specificationdate where fk_cqp_domainconsistency = $ID</dor:Select>
 *             </dor:Table>
 *         </dor:Table>
 *         <dor:Table>
 *             <dor:ElementName>bbox</dor:ElementName>
 *             <dor:Select>Select * From cqp_bbox where fk_cqp_main = $ID</dor:Select>
 *             <dor:GeometryColumn crs="EPSG:4326">geom</dor:GeometryColumn>
 *         </dor:Table>
 *     </dor:Table>
 * </dor:XMLMapping>
 * </pre>
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DatabaseXMLMapping {

    private static ILogger LOG = LoggerFactory.getLogger( DatabaseXMLMapping.class );

    private static URI namespace = URI.create( "http://www.deegree.org/xmlmapping" );

    private static NamespaceContext nsc;

    protected Table mainTable;

    private JDBCConnection jdbc;

    protected XSLTDocument xslt;

    /**
     * 
     * @param fileName
     * @throws Exception
     */
    public DatabaseXMLMapping( String fileName ) throws Exception {
        nsc = CommonNamespaces.getNamespaceContext();
        nsc.addNamespace( "dxm", namespace );
        readConfig( fileName );
    }

    private void readConfig( String fileName )
                            throws Exception {
        FileReader fr = new FileReader( fileName );
        XMLFragment xml = new XMLFragment( fr, XMLFragment.DEFAULT_URL );
        // parse database connection info
        Element jdbcElement = XMLTools.getRequiredElement( xml.getRootElement(), "dgjdbc:JDBCConnection", nsc );
        IODocument ioDoc = new IODocument( jdbcElement );
        jdbc = ioDoc.parseJDBCConnection();

        // xslt file to transform result of xml mapping
        String xsltFileName = XMLTools.getNodeAsString( xml.getRootElement(), "./dxm:XSLT", nsc, null );
        if ( xsltFileName != null ) {
            xslt = new XSLTDocument( new File( xsltFileName ).toURL() );
        }

        // parse table relations
        String elementName = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "./dxm:Table/dxm:ElementName", nsc );
        String select = XMLTools.getRequiredNodeAsString( xml.getRootElement(), "./dxm:Table/dxm:Select", nsc );

        List<Pair<String, String>> geomFieldList = new ArrayList<Pair<String, String>>();
        List<Element> elements = XMLTools.getElements( xml.getRootElement(), "./dxm:Table/dxm:GeometryColumn", nsc );
        for ( Element element : elements ) {
            String field = XMLTools.getStringValue( element ).toLowerCase();
            String crs = element.getAttribute( "crs" );
            Pair<String, String> p = new Pair<String, String>( field, crs );
            geomFieldList.add( p );
        }
        mainTable = new Table( elementName, select, geomFieldList );
        List<Element> tables = XMLTools.getElements( xml.getRootElement(), "./dxm:Table/dxm:Table", nsc );
        for ( Element element : tables ) {
            parseTable( mainTable, element );
        }
    }

    /**
     * @param table
     * @param element
     */
    private void parseTable( Table table, Element element )
                            throws Exception {
        String elementName = XMLTools.getRequiredNodeAsString( element, "dxm:ElementName", nsc );
        String select = XMLTools.getRequiredNodeAsString( element, "dxm:Select", nsc );
        List<Pair<String, String>> geomFieldList = new ArrayList<Pair<String, String>>();
        List<Element> elements = XMLTools.getElements( element, "dxm:GeometryColumn", nsc );
        for ( Element elem : elements ) {
            String field = XMLTools.getStringValue( elem ).toLowerCase();
            String crs = elem.getAttribute( "crs" );
            Pair<String, String> p = new Pair<String, String>( field, crs );
            geomFieldList.add( p );
        }
        Table subTable = new Table( elementName, select, geomFieldList );
        table.getTables().add( subTable );
        List<Element> tables = XMLTools.getElements( element, "dxm:Table", nsc );
        for ( Element subEelement : tables ) {
            parseTable( subTable, subEelement );
        }
    }

    public void run()
                            throws Exception {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = pool.acquireConnection( jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = mainTable.getSelect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery( sql );
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            Map<String, Object> row = new LinkedHashMap<String, Object>();
            while ( rs.next() ) {
                // create one XML document for each row of the main table
                XMLFragment result = new XMLFragment();
                result.load( new StringReader( "<DatabaseTable/>" ), XMLFragment.DEFAULT_URL );
                // append root table element
                Element tableElement = XMLTools.appendElement( result.getRootElement(), null, mainTable.getName() );
                for ( int i = 0; i < colCount; i++ ) {
                    String cName = rsmd.getColumnName( i + 1 ).toLowerCase();
                    Object value = rs.getObject( i + 1 );
                    row.put( cName, value );
                    if ( value != null ) {
                        Pair<String, CoordinateSystem> p = mainTable.getGeometryColumn( cName );
                        if ( p != null ) {
                            handleGeometry( tableElement, p, value );
                        } else {
                            XMLTools.appendElement( tableElement, null, cName, value.toString() );
                        }
                    } else {
                        XMLTools.appendElement( tableElement, null, cName );
                    }
                }
                // add sub tables if available
                List<Table> tables = mainTable.getTables();
                for ( Table table : tables ) {
                    appendTable( tableElement, conn, row, table );
                }
                row.clear();
                if ( xslt != null ) {
                    result = transform( result );
                }
                performAction( result );
            }
        } catch ( Exception e ) {
            LOG.logError( e );
            throw e;
        } finally {
            //rs.close();
            stmt.close();
            pool.releaseConnection( conn, jdbc.getDriver(), jdbc.getURL(), jdbc.getUser(), jdbc.getPassword() );
        }

    }

    /**
     * 
     * @param ps
     * @param conn
     * @param targetRow
     * @param targetTable
     * @throws Exception
     */
    private void appendTable( Element tableElement, Connection conn, Map<String, Object> targetRow, Table subTable )
                            throws Exception {
        Statement stmt = null;

        try {
            String sql = subTable.getSelect();
            List<String> variables = subTable.getVariables();
            // replace variables with real values
            for ( String variable : variables ) {
                Object value = targetRow.get( variable.substring( 1, variable.length() ).toLowerCase() );
                if ( value instanceof String ) {
                    sql = StringTools.replace( sql, variable, "'" + value.toString() + "'", true );
                } else if ( value != null ) {
                    sql = StringTools.replace( sql, variable, value.toString(), true );
                } else {
                    sql = StringTools.replace( sql, variable, "'" + "XXXXXXXdummyXXXXXXX"+ "'", true );
                }
            }
            LOG.logDebug( sql );
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( sql );
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            Map<String, Object> row = new LinkedHashMap<String, Object>();
            while ( rs.next() ) {
                // append sub table element
                Element subTableElement = XMLTools.appendElement( tableElement, null, subTable.getName() );
                for ( int i = 0; i < colCount; i++ ) {
                    String cName = rsmd.getColumnName( i + 1 ).toLowerCase();
                    Object value = rs.getObject( i + 1 );
                    row.put( cName, value );
                    if ( value != null ) {
                        Pair<String, CoordinateSystem> p = subTable.getGeometryColumn( cName );
                        if ( p != null ) {
                            handleGeometry( subTableElement, p, value );
                        } else {
                            XMLTools.appendElement( subTableElement, null, cName, value.toString() );
                        }
                    } else {
                        XMLTools.appendElement( subTableElement, null, cName );
                    }
                }
                // recursion!
                // append sub tables if available
                List<Table> tables = subTable.getTables();
                for ( Table table : tables ) {
                    appendTable( subTableElement, conn, row, table );
                }
                row.clear();
            }
        } catch ( Exception e ) {
            LOG.logError( e );
            throw e;
        } finally {
            stmt.close();
        }
    }

    /**
     * @param tableElement
     * @param p
     * @param value
     */
    private void handleGeometry( Element tableElement, Pair<String, CoordinateSystem> p, Object value )
                            throws Exception {
        Geometry geom = null;
        if ( jdbc.getDriver().toLowerCase().indexOf( "postgres" ) > -1 ) {
            geom = PGgeometryAdapter.wrap( (PGgeometry) value, p.second );
        } else if ( jdbc.getDriver().toLowerCase().indexOf( "oracle" ) > -1 ) {
//            JGeometry jGeometry = JGeometry.load( (STRUCT) value );
//            geom = JGeometryAdapter.wrap( jGeometry, p.second );
        } else if ( jdbc.getDriver().toLowerCase().indexOf( "sqlserver" ) > -1 ) {
        } else if ( jdbc.getDriver().toLowerCase().indexOf( "mysql" ) > -1 ) {
            byte[] wkb = (byte[]) value;
            org.postgis.Geometry pgGeometry = new BinaryParser().parse( wkb );
            geom = PGgeometryAdapter.wrap( pgGeometry, p.second );
        }
        String s = GMLGeometryAdapter.export( geom ).toString();
        s = StringTools.replace( s, ">", " xmlns:gml='http://www.opengis.net/gml'>", false );
        XMLFragment xml = new XMLFragment( new StringReader( s ), XMLFragment.DEFAULT_URL );
        Element element = XMLTools.appendElement( tableElement, null, p.first );
        XMLTools.copyNode( xml.getRootElement().getOwnerDocument(), element );
    }

    /**
     * @param xml
     * @return
     */
    protected XMLFragment transform( XMLFragment xml )
                            throws Exception {        
        return xslt.transform( xml );
    }

    protected void performAction( XMLFragment xml ) {
        System.out.println( xml.getAsString() );
    }

    
}
