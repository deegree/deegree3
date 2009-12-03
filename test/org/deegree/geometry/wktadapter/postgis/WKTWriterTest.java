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
package org.deegree.geometry.wktadapter.postgis;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManagerTest;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.WKTWriterNG;
import org.deegree.geometry.WKTWriterNG.WKTFlag;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML311GeometryDecoder;
import org.deegree.gml.geometry.GML311GeometryDecoderTest;
import org.junit.Test;

/**
 * Tests the correct syntax of the sql statement that should be dispatched against the postgis database
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class WKTWriterTest extends TestCase {

    private final String BASE_DIR = "testdata/geometries/";

    private static GeometryFactory geomFac = new GeometryFactory();

    @Test
    public void test_EXAMPLE1_Point()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        // point.gml
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Point_coord.gml" );
        WKTwriter.writeGeometry( geom, writer );

        ConnectionManagerTest con = new ConnectionManagerTest();
        con.testConnectionAllocation();
        Connection conn = ConnectionManager.getConnection( "conn1" );

        String s = "Select (GeomFromText('Point(2 2)'))";
        ResultSet rs = conn.createStatement().executeQuery( s );
        int countRows = 0;
        while ( rs.next() ) {
            countRows = rs.getInt( 1 );
            System.out.println( rs.getInt( 1 ) );
        }

        conn.close();

    }

    private Geometry parseGeometry( String fileName ) throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException {

        URL gmlDocURL = GML311GeometryDecoderTest.class.getResource( BASE_DIR + fileName );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, gmlDocURL );
        return gmlReader.readGeometry();
    }
}
