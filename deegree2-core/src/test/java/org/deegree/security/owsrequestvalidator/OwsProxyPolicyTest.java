//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/security/owsrequestvalidator/OwsProxyPolicyTest.java $
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
package org.deegree.security.owsrequestvalidator;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;

import alltests.Configuration;

/**
 *
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
public class OwsProxyPolicyTest extends TestCase {

    public OwsProxyPolicyTest() {
        super();
    }

    public OwsProxyPolicyTest( String arg0 ) {
        super( arg0 );
    }

    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    public static Test suite() {
        return new TestSuite( OwsProxyPolicyTest.class );
    }

    public void testWFSPolicy()
                            throws Exception {
        URL url = new URL( Configuration.getOwsProxyBaseDir(), Configuration.OWSPROXY_WFSCONFIGURATION_EXAMPLE );
        PolicyDocument doc = new PolicyDocument( url );
        Policy policy = doc.getPolicy();
        Condition con = policy.getRequest( "WFS", "GetFeature" ).getPreConditions();
        OperationParameter[] ops = con.getOperationParameters();
        for ( int i = 0; i < ops.length; i++ ) {
            assertNotNull( ops[i].getName() );
        }
        con = policy.getRequest( "WFS", "GetFeature" ).getPostConditions();
        ops = con.getOperationParameters();
        for ( int i = 0; i < ops.length; i++ ) {
            assertNotNull( ops[i].getName() );
        }
    }

    public void testWMSPolicy()
                            throws Exception {
        URL url = new URL( Configuration.getOwsProxyBaseDir(), Configuration.OWSPROXY_WMSCONFIGURATION_EXAMPLE );
        PolicyDocument doc = new PolicyDocument( url );
        doc.getPolicy();
        Policy policy = doc.getPolicy();
        Condition con = policy.getRequest( "WMS", "GetMap" ).getPreConditions();
        OperationParameter[] ops = con.getOperationParameters();
        for ( int i = 0; i < ops.length; i++ ) {
            assertNotNull( ops[i].getName() );
        }
        con = policy.getRequest( "WMS", "GetMap" ).getPostConditions();
        ops = con.getOperationParameters();
        for ( int i = 0; i < ops.length; i++ ) {
            assertNotNull( ops[i].getName() );
        }
    }

    public void testCSWPolicy()
                            throws Exception {
        URL url = new URL( Configuration.getOwsProxyBaseDir(), Configuration.OWSPROXY_CSWCONFIGURATION_EXAMPLE );
        PolicyDocument doc = new PolicyDocument( url );
        doc.getPolicy();
        Policy policy = doc.getPolicy();
        Condition con = policy.getRequest( "CSW", "GetRecords" ).getPreConditions();
        OperationParameter[] ops = con.getOperationParameters();
        for ( int i = 0; i < ops.length; i++ ) {
            assertNotNull( ops[i].getName() );
        }
        con = policy.getRequest( "CSW", "GetRecords" ).getPostConditions();
        ops = con.getOperationParameters();
        for ( int i = 0; i < ops.length; i++ ) {
            assertNotNull( ops[i].getName() );
        }
    }

}
