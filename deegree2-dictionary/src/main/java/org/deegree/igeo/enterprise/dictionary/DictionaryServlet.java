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
package org.deegree.igeo.enterprise.dictionary;

import static org.deegree.ogcbase.CommonNamespaces.GMLNS;
import static org.deegree.ogcbase.CommonNamespaces.GML_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.XSINS;
import static org.deegree.ogcbase.CommonNamespaces.XSI_PREFIX;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.TimeTools;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class DictionaryServlet extends HttpServlet {

    private static final long serialVersionUID = -6878316731046973766L;

    private static final ILogger LOG = LoggerFactory.getLogger( DictionaryServlet.class );

    private DictionaryResourceType dictRes;

    @Override
    public void init()
                            throws ServletException {
        super.init();

        String s = getInitParameter( "ConfigFile" );
        File file = new File( s );
        if ( !file.isAbsolute() ) {
            s = getServletContext().getRealPath( s );
            file = new File( s );
        }

        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.igeo.enterprise.dictionary" );
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<?> o = (JAXBElement<?>) u.unmarshal( file.toURI().toURL() );
            this.dictRes = (DictionaryResourceType) o.getValue();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ServletException( e );
        }
    }

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        Map<String, String> param = KVP2Map.toMap( req );
        if ( param.get( "NAME" ) == null ) {
            readAll( resp );
        } else {
            read( param.get( "NAME" ), param.get( "CODESPACE" ), resp );
        }
    }

    /**
     * @param name
     * @param codeSpace
     * @param resp
     */
    private void read( String name, String codeSpace, HttpServletResponse resp ) {
        String table = null;
        List<DefinitionType> defs = dictRes.getDefinition();
        for ( DefinitionType definitionType : defs ) {
            if ( definitionType.getName().equals( name ) && definitionType.getCodeSpace().equals( codeSpace ) ) {
                table = definitionType.getTable();
                break;
            }
        }

        DBConnectionPool pool = DBConnectionPool.getInstance();
        JDBCConnectionType jdbc = dictRes.getConnection();
        Connection conn = null;

        XMLStreamWriter writer = null;
        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            writer = XMLOutputFactory.newFactory().createXMLStreamWriter( outputStream );

            String cs = Charset.defaultCharset().displayName();
            resp.setCharacterEncoding( cs );
            resp.setContentType( "text/xml" );
            LOG.logDebug( "using charset: ", cs );

            writeDictionaryStart( writer, cs );
            writeName( name, codeSpace, writer );
            conn = pool.acquireConnection( jdbc.driver, jdbc.getUrl(), jdbc.getUser(), jdbc.getPassword() );
            if ( table != null ) {
                handleTable( name, codeSpace, table, conn, writer, 0 );
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
        } catch ( Exception e ) {
            e.printStackTrace();
            try {
                resp.sendError( 500, e.getMessage() );
            } catch ( IOException e1 ) {
                e1.printStackTrace();
            }
        } finally {
            try {
                pool.releaseConnection( conn, jdbc.driver, jdbc.getUrl(), jdbc.getUser(), jdbc.getPassword() );
                if ( writer != null )
                    writer.close();
            } catch ( DBPoolException e ) {
                e.printStackTrace();
            } catch ( XMLStreamException e ) {
                e.printStackTrace();
            }
        }
    }

    private void handleTable( String name, String codeSpace, String table, Connection conn, XMLStreamWriter writer,
                              int no )
                            throws SQLException, XMLStreamException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery( "select * from " + table );
        ResultSetMetaData rsmd = rs.getMetaData();
        int cnt = rsmd.getColumnCount();

        String gmlId = "id_" + no + "_" + table;

        writer.writeStartElement( GML_PREFIX, "dictionaryEntry", GMLNS.toString() );
        writer.writeStartElement( GML_PREFIX, "DefinitionCollection", GMLNS.toString() );
        writer.writeAttribute( GML_PREFIX, GMLNS.toString(), "id", gmlId );

        writeName( name, codeSpace, writer );

        int k = 0;
        while ( rs.next() ) {

            writer.writeStartElement( GML_PREFIX, "dictionaryEntry", GMLNS.toString() );
            writer.writeStartElement( GML_PREFIX, "Definition", GMLNS.toString() );
            writer.writeAttribute( GML_PREFIX, GMLNS.toString(), "id", gmlId + "_" + k++ );
            for ( int i = 0; i < cnt; i++ ) {
                String codeSpace2 = "urn:org:deegree:igeodesktop:" + rsmd.getColumnName( i + 1 ).toLowerCase();

                Object val = rs.getObject( i + 1 );
                if ( val instanceof Date ) {
                    writeName( TimeTools.getISOFormattedTime( (Date) val ), codeSpace2, writer );
                } else {
                    writeName( val.toString(), codeSpace2, writer );
                }
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }
        rs.close();
        stmt.close();

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeDictionaryStart( XMLStreamWriter writer, String cs )
                            throws XMLStreamException {
        writer.writeStartDocument( cs, "1.0" );
        writer.writeStartElement( GML_PREFIX, "Dictionary", GMLNS.toString() );
        writer.writeNamespace( GML_PREFIX, GMLNS.toString() );
        writer.writeAttribute( GML_PREFIX, GMLNS.toString(), "id", "ExternalCodeLists" );
        writer.writeNamespace( XSI_PREFIX, XSINS.toString() );
        writer.writeAttribute( XSI_PREFIX, XSINS.toString(), "schemaLocation",
                               GMLNS.toString() + " http://schemas.opengis.net/gml/3.1.1/base/gml.xsd" );
    }

    private void writeName( String name, String codeSpace, XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( GML_PREFIX, "name", GMLNS.toString() );
        if ( codeSpace != null && codeSpace.length() > 0 ) {
            writer.writeAttribute( "codeSpace", codeSpace );
        }
        writer.writeCharacters( name );
        writer.writeEndElement();
    }

    /**
     * @param resp
     */
    private void readAll( HttpServletResponse resp ) {

        DBConnectionPool pool = DBConnectionPool.getInstance();
        JDBCConnectionType jdbc = dictRes.getConnection();
        Connection conn = null;

        // PrintWriter pw = null;
        XMLStreamWriter writer = null;
        try {
            ServletOutputStream outputStream = resp.getOutputStream();
            writer = XMLOutputFactory.newFactory().createXMLStreamWriter( outputStream );
            writeDictionaryStart( writer, Charset.defaultCharset().displayName() );
            writeName( "deegree dictionary", null, writer );

            List<DefinitionType> defs = dictRes.getDefinition();
            conn = pool.acquireConnection( jdbc.getDriver().trim(), jdbc.getUrl().trim(), jdbc.getUser().trim(),
                                           jdbc.getPassword().trim() );
            int i = 0;
            for ( DefinitionType definitionType : defs ) {
                String table = definitionType.getTable();
                String name = definitionType.getName();
                String codeSpace = definitionType.getCodeSpace();
                handleTable( name, codeSpace, table, conn, writer, i++ );
            }
            // pw.write( "</gml:Dictionary>" );
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            // pw.flush();
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            try {
                pool.releaseConnection( conn, jdbc.getDriver(), jdbc.getUrl(), jdbc.getUser(), jdbc.getPassword() );
                if ( writer != null ) {
                    writer.close();
                }
            } catch ( DBPoolException e ) {
                e.printStackTrace();
            } catch ( XMLStreamException e ) {
                e.printStackTrace();
            }
        }
        // pw.close();

    }

    @Override
    public void doPost( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        super.doGet( req, resp );
    }

}
