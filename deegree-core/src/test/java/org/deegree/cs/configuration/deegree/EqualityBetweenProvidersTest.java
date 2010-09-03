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

package org.deegree.cs.configuration.deegree;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * <b>Since the retrieval of all CRSs takes too much time this test should be skipped</b>
 * The <code>EqualityBetweenProvidersTest</code> compares every CRS from the XML backend to the
 * CRS from the DB backend that has the same code. Actually the comparison is only in the data i.e.
 * no identifiable attributes are compared. This is because:
 * <ol>
 *    <li> The two CRS that are compared are checked to have the same id code i.e. the same codespace and codenumber </li>
 *    <li> The list of identifier codes is compressed coming from the DB, thus different to the one coming from
 *          the XML backend. </li>
 * </ol>
 * Nevertheless, for an exhaustive comparison, all the identifiable attributes should be inspected.
 *
 * @author <a href="mailto:ionita@deegree.org">Andrei Ionita</a>
 *
 * @author last edited by: $Author: ionita $
 *
 * @version $Revision: $, $Date: $
 *
 */
public class EqualityBetweenProvidersTest extends TestCase {

    /**
     * 
     */
    @Test
    public void testEqualityBetweenCRSs() {
//        // Load the database provider
//        CRSProvider provider1 =  CRSConfiguration.
//        getCRSConfiguration( "org.deegree.cs.configuration.deegree.db.DatabaseCRSProvider" )
//        .getProvider();
//        assertNotNull( provider1 );
//        assertTrue( provider1 instanceof DatabaseCRSProvider );
//        DatabaseCRSProvider dbProvider = (DatabaseCRSProvider) provider1;
//
//        // Load the degree-xml provider
//        CRSProvider provider2 =  CRSConfiguration.
//        getCRSConfiguration( "org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider" )
//        .getProvider();
//        assertNotNull( provider2 );
//        assertTrue( provider2 instanceof DeegreeCRSProvider );
//        DeegreeCRSProvider xmlProvider = (DeegreeCRSProvider) provider2;
//
//        List<CRSCodeType> dbCodes = dbProvider.getAvailableCRSCodes();
//        List<CRSCodeType> xmlCodes = xmlProvider.getAvailableCRSCodes();
//        
//        for ( int i = 0 ; i < dbCodes.size(); i++) {            
//            assertTrue( xmlCodes.contains( dbCodes.get( i ) ) );
//
//            CoordinateSystem dbCRS = dbProvider.getCRSByCode( dbCodes.get( i ) );
//            CoordinateSystem xmlCRS = xmlProvider.getCRSByCode( dbCodes.get( i ) );
//
//            assertTrue( dbCRS.equals( xmlCRS ) );
//        }
//
//        for ( int i = 0 ; i < xmlCodes.size(); i++) {
//            assertTrue( dbCodes.contains( xmlCodes.get( i ) ) );
//
//            CoordinateSystem dbCRS = dbProvider.getCRSByCode( xmlCodes.get( i ) );            
//            CoordinateSystem xmlCRS = xmlProvider.getCRSByCode( xmlCodes.get( i ) );
//            
//            assertTrue( xmlCRS.equals( dbCRS ) );
//        }
//
    }
}
