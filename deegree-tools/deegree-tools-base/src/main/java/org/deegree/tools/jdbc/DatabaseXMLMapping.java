//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.tools.jdbc;

import static org.slf4j.LoggerFactory.getLogger;


import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.jaxp.OMResult;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML3GeometryWriter;
import org.slf4j.Logger;

import com.vividsolutions.jts.io.ParseException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: buesching $
 * 
 * @version $Revision: 1.2 $, $Date: 2011-03-02 13:37:30 $
 */
public class DatabaseXMLMapping {

    private static final Logger LOG = getLogger( DatabaseXMLMapping.class );

    private static String namespace = "http://www.deegree.org/xmlmapping";

    private static NamespaceBindings nsc = CommonNamespaces.getNamespaceContext();

    private static final TransformerFactory factory = TransformerFactory.newInstance();

    private final OMFactory omFactory = OMAbstractFactory.getOMFactory();

    protected Table mainTable;

    private String jdbcId;

    protected URL xslt;

    static {
        nsc.addNamespace( "dxm", namespace );
    }

    /**
     * 
     * @param fileName
     * @throws Exception
     */
    public DatabaseXMLMapping( File file, String workspaceName ) throws Exception {
        readConfig( file );
    }

    private void readConfig( File file )
                            throws Exception {
        XMLAdapter adapter = new XMLAdapter( file );

        // parse database connection info
        OMElement rootElement = adapter.getRootElement();
        jdbcId = adapter.getRequiredNodeAsString( rootElement, new XPath( "/dxm:XMLMapping/dxm:JDBCID", nsc ) );

        // xslt file to transform result of xml mapping

        String xsltFileName = adapter.getNodeAsString( rootElement, new XPath( "/dxm:XMLMapping/dxm:XSLT", nsc ), null );
        if ( xsltFileName != null ) {
            xslt = adapter.resolve( xsltFileName );
        }

        // parse table relations
        String elementName = adapter.getRequiredNodeAsString( rootElement,
                                                              new XPath( "/dxm:XMLMapping/dxm:Table/dxm:ElementName",
                                                                         nsc ) );
        String select = adapter.getRequiredNodeAsString( rootElement,
                                                         new XPath( "/dxm:XMLMapping/dxm:Table/dxm:Select", nsc ) );

        List<Pair<String, String>> geomFieldList = new ArrayList<Pair<String, String>>();
        List<OMElement> elements = adapter.getElements( rootElement,
                                                        new XPath( "/dxm:XMLMapping/dxm:Table/dxm:GeometryColumn", nsc ) );
        for ( OMElement element : elements ) {
            String field = adapter.getNodeAsString( element, new XPath( ".", nsc ), null );
            String crs = adapter.getNodeAsString( element, new XPath( "@crs", nsc ), null );
            Pair<String, String> p = new Pair<String, String>( field, crs );
            geomFieldList.add( p );
        }
        mainTable = new Table( elementName, select, geomFieldList );
        List<OMElement> tables = adapter.getElements( rootElement, new XPath( "/dxm:XMLMapping/dxm:Table/dxm:Table",
                                                                              nsc ) );
        for ( OMElement element : tables ) {
            parseTable( mainTable, element );
        }
    }

    /**
     * @param table
     * @param element
     */
    private void parseTable( Table table, OMElement element )
                            throws Exception {
        XMLAdapter adapter = new XMLAdapter();
        String elementName = adapter.getRequiredNodeAsString( element, new XPath( "dxm:ElementName", nsc ) );
        String select = adapter.getRequiredNodeAsString( element, new XPath( "dxm:Select", nsc ) );
        List<Pair<String, String>> geomFieldList = new ArrayList<Pair<String, String>>();
        List<OMElement> elements = adapter.getElements( element, new XPath( "dxm:GeometryColumn", nsc ) );
        for ( OMElement elem : elements ) {
            String field = adapter.getNodeAsString( elem, new XPath( ".", nsc ), null );
            String crs = adapter.getNodeAsString( elem, new XPath( "@crs", nsc ), null );
            Pair<String, String> p = new Pair<String, String>( field, crs );
            geomFieldList.add( p );
        }
        Table subTable = new Table( elementName, select, geomFieldList );
        table.getTables().add( subTable );
        List<OMElement> tables = adapter.getElements( element, new XPath( "dxm:Table", nsc ) );
        for ( OMElement subEelement : tables ) {
            parseTable( subTable, subEelement );
        }
    }

    public void run()
                            throws Exception {
        Connection conn = ConnectionManager.getConnection( jdbcId );
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
                OMDocument result = omFactory.createOMDocument();
                OMElement dbTable = omFactory.createOMElement( new QName( "DatabaseTable" ) );
                result.addChild( dbTable );

                // result.load( new StringReader( "<DatabaseTable/>" ), XMLFragment.DEFAULT_URL );
                // append root table element
                // Element tableElement = XMLTools.appendElement( result.getRootElement(), null, mainTable.getName() );
                OMElement tableElement = omFactory.createOMElement( new QName( mainTable.getName() ) );
                dbTable.addChild( tableElement );
                for ( int i = 0; i < colCount; i++ ) {
                    String cName = rsmd.getColumnName( i + 1 ).toLowerCase();
                    Object value = rs.getObject( i + 1 );
                    row.put( cName, value );

                    OMElement cNameElement = omFactory.createOMElement( new QName( cName ) );
                    tableElement.addChild( cNameElement );
                    if ( value != null ) {
                        Pair<String, ICRS> p = mainTable.getGeometryColumn( cName );
                        if ( p != null ) {
                            handleGeometry( tableElement, p, (byte[]) value );
                        } else {
                            cNameElement.addChild( omFactory.createOMText( value.toString() ) );
                        }
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
            LOG.error( "Could not create a XMLDocument", e );
            throw e;
        } finally {
            // rs.close();
            stmt.close();
            // CSW?
            // ConnectionManager.destroy();
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
    private void appendTable( OMElement tableElement, Connection conn, Map<String, Object> targetRow, Table subTable )
                            throws Exception {
        Statement stmt = null;

        try {
            String sql = subTable.getSelect();
            List<String> variables = subTable.getVariables();
            // replace variables with real values
            for ( String variable : variables ) {
                Object value = targetRow.get( variable.substring( 1, variable.length() ).toLowerCase() );                
                if ( value instanceof String ) {
                    sql = StringUtils.replaceAll( sql, variable, "'" + value.toString() + "'" );
                } else if ( value != null ) {
                    sql = StringUtils.replaceAll( sql, variable, value.toString() );
                } else {
                    sql = StringUtils.replaceAll( sql, variable,  "'XXXXXXXdummyXXXXXXX'");
                }
            }

            LOG.debug( sql );
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( sql );
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            Map<String, Object> row = new LinkedHashMap<String, Object>();
            while ( rs.next() ) {
                // append sub table element
                // Element subTableElement = XMLTools.appendElement( tableElement, null, subTable.getName() );
                OMElement subTableElement = omFactory.createOMElement( new QName( subTable.getName() ) );
                tableElement.addChild( subTableElement );
                for ( int i = 0; i < colCount; i++ ) {
                    String cName = rsmd.getColumnName( i + 1 ).toLowerCase();
                    Object value = rs.getObject( i + 1 );
                    row.put( cName, value );
                    if ( value != null ) {
                        Pair<String, ICRS> p = subTable.getGeometryColumn( cName );
                        OMElement cNameElement = omFactory.createOMElement( new QName( cName ) );
                        subTableElement.addChild( cNameElement );
                        if ( p != null ) {
                            handleGeometry( subTableElement, p, (byte[]) value );
                        } else {
                            cNameElement.addChild( omFactory.createOMText( value.toString() ) );
                        }
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
            LOG.error( "", e );
            throw e;
        } finally {
            stmt.close();
        }
    }

    /**
     * 
     * @param tableElement
     * @param p
     * @param wkb
     *            wkb representation of the geometry
     * @throws Exception
     */
    private void handleGeometry( OMElement tableElement, Pair<String, ICRS> p, byte[] wkb )
                            throws Exception {
        try {
            Geometry geom = WKBReader.read( wkb, p.second );
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( sw );
            GML3GeometryWriter geomWriter = new GML3GeometryWriter( GMLVersion.GML_32, writer );
            geomWriter.export( geom );
            writer.close();

            OMElement geomElement = omFactory.createOMElement( new QName( p.first ) );
            geomElement.addChild( omFactory.createOMText( sw.toString() ) );
            tableElement.addChild( geomElement );
        } catch ( ParseException e ) {
            LOG.info( "WKB from the DB could not be parsed: '{}'.", e.getLocalizedMessage() );
            LOG.info( "For PostGIS users: you have to select the geometry field 'asbinary(geometry)'." );
            LOG.trace( "Stack trace:", e );
        }
    }

    /**
     * @param xml
     * @return
     * @throws Exception
     */
    protected OMDocument transform( OMDocument xml )
                            throws Exception {
        return transform( xml, null, null );
    }

    protected OMDocument transform( OMDocument xml, Properties outputProperties, Map<String, String> params )
                            throws Exception {
        System.out.println( xml.getOMDocumentElement() );
        Source xmlSource = new OMSource( xml.getOMDocumentElement() );
        XMLAdapter xsltAdapter = new XMLAdapter( xslt );
        Source xslSource = new OMSource( xsltAdapter.getRootElement() );
        xslSource.setSystemId( xsltAdapter.getSystemId() );

        OMResult sr = new OMResult();
        try {
            Transformer transformer = factory.newTransformer( xslSource );
            if ( params != null ) {
                for ( String key : params.keySet() ) {
                    transformer.setParameter( key, params.get( key ) );
                }
            }
            if ( outputProperties != null ) {
                transformer.setOutputProperties( outputProperties );
            }
            transformer.transform( xmlSource, sr );
        } catch ( TransformerException e ) {
            String transformerClassName = null;
            String transformerFactoryClassName = factory.getClass().getName();
            try {
                transformerClassName = factory.newTransformer().getClass().getName();
            } catch ( Exception e2 ) {
                LOG.error( "Error creating Transformer instance." );
            }
            String errorMsg = "XSL transformation using stylesheet with systemId '" + xslSource.getSystemId()
                              + "' and xml source with systemId '" + xmlSource.getSystemId()
                              + "' failed. TransformerFactory class: " + transformerFactoryClassName
                              + "', Transformer class: " + transformerClassName;
            LOG.error( errorMsg, e );
            throw new TransformerException( errorMsg, e );
        }
        return sr.getDocument();
    }

    protected void performAction( OMDocument xml ) {
    }

}
