//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wcs/getcoverage/GetCoverageTest.java $
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
package org.deegree.ogcwebservices.wcs.getcoverage;

import java.io.StringReader;
import java.net.URI;

import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 *
 * @version $Revision: 18195 $
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 1.0. $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class GetCoverageTest extends TestCase {

    public static Test suite() {
        return new TestSuite(GetCoverageTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for GetCoverageTest
     *
     * @param arg0
     */
    public GetCoverageTest(String arg0) {
        super(arg0);
    }

    /**
     * Create a GetCoverage request object from KVP <BR>
     * Steps:
     * <ul>
     * <li>read a request
     * <li>validate the created request
     * </ul>
     *
     */
    public void testCreateGetCoverageByKVP() {
        try {
            StringBuffer kvp = new StringBuffer("http://127.0.0.1/deegree/wcs?");
            kvp.append("service=WCS&version=1.0.0&coverage=MapNeatline&CRS=EPSG:4326&");
            kvp.append("RESPONSE_CRS=EPSG:4326&BBOX=-1.5,-1.5,1.5,1.5&width=600&");
            kvp.append("height=500&format=image/tiff&Exceptions=application/vnd.ogc.se_xml");
            GetCoverage req = GetCoverage.create( "ID", kvp.toString() );
            if ( !req.getOutput().getFormat().getCode().equals("image/tiff") ) {
                fail("format hasn't been read correctly");
            }
//            if ( !req.getOutput().getCrs().getCode().equals("EPSG:4326") ) {
//                fail("output crs  hasn't been read correctly: " +
//                        req.getOutput().getCrs().getCode() );
//            }
//            if ( !req.getOutput().getCrs().getCodeSpace().equals(new URI("EPSG")) ) {
//                fail("output crs  hasn't been read correctly: " +
//                        req.getOutput().getCrs().getCodeSpace() );
//            }
            if ( !req.getDomainSubset().getRequestSRS().getCode().equals("EPSG:4326") ) {
                fail("request crs  hasn't been read correctly: " +
                        req.getDomainSubset().getRequestSRS() );
            }
            Envelope env = GeometryFactory.createEnvelope(-1.5,-1.5,1.5,1.5, CRSFactory.create( "EPSG:4326" ));
            if ( !req.getDomainSubset().getSpatialSubset().getEnvelope().equals(env) ) {
                fail("request envelope hasn't been read correctly: " +
                      req.getDomainSubset().getSpatialSubset().getEnvelope() );
            }
            env = GeometryFactory.createEnvelope(0,0,600-1,500-1, null);
            if ( !req.getDomainSubset().getSpatialSubset().getGrid().equals(env) ) {
                fail("request grid hasn't been read correctly: " +
                      req.getDomainSubset().getSpatialSubset().getGrid() );
            }
            if ( !req.getSourceCoverage().equals("MapNeatline") ) {
                fail("request coverage hasn't been read correctly: " +
                        req.getSourceCoverage() );
            }
        } catch (Exception e) {
            fail( e.getMessage() );
        }
    }

    public void testGetCoverageByXML() {
        String req = "<?xml version='1.0' encoding='ISO-8859-1'?><GetCoverage " +
                "xmlns='http://www.opengis.net/wcs' xmlns:gml='http://www.opengis.net/gml'" +
                " service='WCS' version='1.0.0'><sourceCoverage>Europe</sourceCoverage>" +
                "<domainSubset><spatialSubset><gml:Envelope srsName='EPSG:4326'>" +
                "<gml:pos dimension='2'>-1.5 -1.5</gml:pos><gml:pos dimension='2'>" +
                "1.5 1.5</gml:pos></gml:Envelope><gml:Grid dimension='2'><gml:limits>" +
                "<gml:GridEnvelope><gml:low>0 0</gml:low><gml:high>100 100</gml:high>" +
                "</gml:GridEnvelope></gml:limits><gml:axisName>String</gml:axisName>" +
                "</gml:Grid></spatialSubset></domainSubset>" +
                "<interpolationMethod>nearest neighbor</interpolationMethod><output>" +
                "<crs>EPSG:4326</crs><format>jpeg</format></output></GetCoverage>";
        try {
            StringReader sr = new StringReader( req );
            Document doc = XMLTools.parse(sr);
            GetCoverage.create("ww", doc);
        } catch (Exception e) {
            fail( e.getMessage() );
        }
    }

}
