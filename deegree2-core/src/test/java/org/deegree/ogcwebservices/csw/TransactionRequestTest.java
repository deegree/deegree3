//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/csw/TransactionRequestTest.java $
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

package org.deegree.ogcwebservices.csw;

import java.io.StringReader;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.csw.manager.Transaction;
import org.deegree.ogcwebservices.csw.manager.TransactionDocument;
import org.deegree.ogcwebservices.csw.manager.TransactionResult;
import org.deegree.ogcwebservices.csw.manager.TransactionResultDocument;
import org.deegree.ogcwebservices.csw.manager.XMLFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import alltests.AllTests;

/**
 * TODO describe function and usage of the class here.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 */
public class TransactionRequestTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( TransactionRequestTest.class );

    /**
     * @return Test
     */
    public static Test suite() {
        return new TestSuite( TransactionRequestTest.class );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /**
     *
     */
    public TransactionRequestTest() {
        super();
    }

    /**
     * @param arg0
     */
    public TransactionRequestTest( String arg0 ) {
        super( arg0 );
    }

    /**
     *
     */
    public void testParseTransaction() {
        try {
            String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><csw:Transaction "
                       + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                       + "xmlns:csw=\"http://www.opengis.net/cat/csw\" version=\"2.0.0\" "
                       + "service=\"CSW\" verboseResponse=\"true\"><csw:Insert handle=\"D1\">"
                       + "<csw:Record>Just for testing</csw:Record></csw:Insert><csw:Update "
                       + "handle=\"D2\" version='1.1.0'><csw:Record>Just for testing</csw:Record><csw:Constraint version='1.1.0'>"
                       + "<ogc:Filter><ogc:PropertyIsLike wildCard=\"%\" singleChar=\"_\" "
                       + "escape=\"\\\"><ogc:PropertyName>subject</ogc:PropertyName>"
                       + "<ogc:Literal>%structure%</ogc:Literal></ogc:PropertyIsLike></ogc:Filter>"
                       + "</csw:Constraint></csw:Update><csw:Delete handle=\"D3\"><csw:Constraint version='1.1.0'>"
                       + "<ogc:Filter><ogc:PropertyIsLike wildCard=\"%\" singleChar=\"_\" "
                       + "escape=\"\\\"><ogc:PropertyName>subject</ogc:PropertyName>"
                       + "<ogc:Literal>%structure%</ogc:Literal></ogc:PropertyIsLike>"
                       + "</ogc:Filter></csw:Constraint></csw:Delete></csw:Transaction>";
            StringReader sr = new StringReader( s );
            XMLFragment xml = new XMLFragment();
            xml.load( sr, XMLFragment.DEFAULT_URL );
            Transaction trans = Transaction.create( "", xml.getRootElement() );
            assertEquals( trans.getVersion(), "2.0.0" );
            assertEquals( trans.verboseResponse(), true );
            assertEquals( trans.getOperations().size(), 3 );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( e.getMessage() );
        }
    }

    /**
     *
     */
    public void testExportTransaction() {
        try {
            String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><csw:Transaction "
                       + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                       + "xmlns:csw=\"http://www.opengis.net/cat/csw\" version=\"2.0.0\" "
                       + "service=\"CSW\" verboseResponse=\"true\"><csw:Insert handle=\"D1\">"
                       + "<csw:Record>Just for testing</csw:Record></csw:Insert><csw:Update "
                       + "handle=\"D2\" version='1.1.0'><csw:Record>Just for testing</csw:Record><csw:Constraint version='1.1.0'>"
                       + "<ogc:Filter><ogc:PropertyIsLike wildCard=\"%\" singleChar=\"_\" "
                       + "escape=\"\\\"><ogc:PropertyName>subject</ogc:PropertyName>"
                       + "<ogc:Literal>%structure%</ogc:Literal></ogc:PropertyIsLike></ogc:Filter>"
                       + "</csw:Constraint></csw:Update><csw:Delete handle=\"D3\"><csw:Constraint version='1.1.0'>"
                       + "<ogc:Filter><ogc:PropertyIsLike wildCard=\"%\" singleChar=\"_\" "
                       + "escape=\"\\\"><ogc:PropertyName>subject</ogc:PropertyName>"
                       + "<ogc:Literal>%structure%</ogc:Literal></ogc:PropertyIsLike>"
                       + "</ogc:Filter></csw:Constraint></csw:Delete></csw:Transaction>";
            StringReader sr = new StringReader( s );
            XMLFragment xml = new XMLFragment();
            xml.load( sr, XMLFragment.DEFAULT_URL );
            Transaction trans = Transaction.create( "", xml.getRootElement() );
            TransactionDocument td = XMLFactory.export( trans );
            // td.write( System.out );
            trans = Transaction.create( "", td.getRootElement() );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( e.getMessage() );
        }
    }

    /**
     *
     */
    public void testParseTransactionResponse() {
        try {
            String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><csw:TransactionResponse "
                       + "xmlns:csw=\"http://www.opengis.net/cat/csw\" version=\"2.0.0\" "
                       + "xmlns:iso19115=\"http://schemas.opengis.net/iso19115brief\">"
                       + "<csw:TransactionSummary><csw:totalInserted>2</csw:totalInserted>"
                       + "<csw:totalUpdated>4</csw:totalUpdated><csw:totalDeleted>5"
                       + "</csw:totalDeleted></csw:TransactionSummary><csw:InsertResult>"
                       + "<iso19115:MD_Metadata><iso19115:fileIdentifier>5q32kwc3"
                       + "</iso19115:fileIdentifier></iso19115:MD_Metadata><iso19115:MD_Metadata>"
                       + "<iso19115:fileIdentifier>akhscdoew08ew</iso19115:fileIdentifier>"
                       + "</iso19115:MD_Metadata></csw:InsertResult></csw:TransactionResponse>";
            StringReader sr = new StringReader( s );
            TransactionResultDocument trd = new TransactionResultDocument();
            trd.load( sr, XMLFragment.DEFAULT_URL );
            TransactionResult tr = trd.parseTransactionResponse( null );
            assertEquals( tr.getTotalInserted(), 2 );
            assertEquals( tr.getTotalUpdated(), 4 );
            assertEquals( tr.getTotalDeleted(), 5 );
            List<Node> list = tr.getResults().getRecords();
            for ( int i = 0; i < list.size(); i++ ) {
                XMLFragment xml = new XMLFragment();
                xml.setRootElement( (Element) list.get( i ) );
                xml.write( System.out );
                LOG.logInfo( "\n" );
            }
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( e.getMessage() );
        }
    }

}
