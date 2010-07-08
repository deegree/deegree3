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
package org.deegree.feature.persistence.postgis;

import java.net.URL;

import junit.framework.Assert;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreTest {

    @Test
    public void testInstantiation()
                            throws FeatureStoreException {

//        ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
//                                         "deegreetest", "deegreetest", 1, 10 );
//
//        URL configURL = this.getClass().getResource( "philosopher.xml" );
//        PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
//        fs.init();
//        
//        ApplicationSchema schema = fs.getSchema();
//        Assert.assertEquals( 1, schema.getFeatureTypes().length );
//
//        FeatureType ft = schema.getFeatureTypes()[0];
//        System.out.println (ft);
    }
}