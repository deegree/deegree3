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
package org.deegree.protocol.wfs.lockfeature;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.junit.Test;

/**
 * The <code>LockFeatureKVPAdapterTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class LockFeatureKVPAdapterTest extends TestCase {

    private final String EXAMPLE1 = "example_kvp/v110/example1.kvp";

    private final String EXAMPLE2 = "example_kvp/v110/example2.kvp";

    private final String EXAMPLE3 = "example_kvp/v110/example3.kvp";

    private final String EXAMPLE4 = "example_kvp/v110/example4.kvp";

    /**
     * @throws Exception
     */
    @Test
    public void testEXAMPLE1()
                            throws Exception {
        URL example = this.getClass().getResource( EXAMPLE1 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( example );

        LockFeature lockFeature = LockFeatureKVPAdapter.parse( kvpMap );
        FilterLock filterLock = (FilterLock) lockFeature.getLocks()[0];
        assertEquals( new QName( "InWaterA_1M" ), filterLock.getTypeName().getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testEXAMPLE2()
                            throws Exception {
        URL example = this.getClass().getResource( EXAMPLE2 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( example );

        LockFeature lockFeature = LockFeatureKVPAdapter.parse( kvpMap );
        FeatureIdLock featureLock = (FeatureIdLock) lockFeature.getLocks()[0];
        assertEquals( "RoadL_1M.1013", featureLock.getFeatureIds()[0] );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testEXAMPLE3()
                            throws Exception {
        URL example = this.getClass().getResource( EXAMPLE3 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( example );

        LockFeature lockFeature = LockFeatureKVPAdapter.parse( kvpMap );
        FilterLock featureLock0 = (FilterLock) lockFeature.getLocks()[0];
        assertEquals( new QName( "InWaterA_1M" ), featureLock0.getTypeName().getFeatureTypeName() );

        FilterLock featureLock1 = (FilterLock) lockFeature.getLocks()[1];
        assertEquals( new QName( "BuiltUpA_1M" ), featureLock1.getTypeName().getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    @SuppressWarnings("boxing")
    @Test
    public void testEXAMPLE4()
                            throws Exception {
        URL example = this.getClass().getResource( EXAMPLE4 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( example );

        LockFeature lockFeature = LockFeatureKVPAdapter.parse( kvpMap );
        assertTrue( lockFeature.getLockAll() );
        assertEquals( new Integer( 5 ), lockFeature.getExpiry() );

        LockOperation[] locks = lockFeature.getLocks();
        assertEquals( 2, locks.length );

        FilterLock filterLock1 = (FilterLock) locks[0];

        OperatorFilter filter1 = (OperatorFilter) filterLock1.getFilter();
        assertTrue( filter1.getOperator() instanceof Within );

        Within within = (Within) filter1.getOperator();
        assertEquals( "wkbGeom", within.getPropName().getPropertyName() );
        Envelope env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10, 10, 20, 20 );

    }

    @SuppressWarnings("boxing")
    private void verifyEnvelope( Envelope env, double d, double e, double f, double g ) {
        Point p1 = env.getMin();
        assertEquals( p1.get0(), d );
        assertEquals( p1.get1(), e );
        Point p2 = env.getMax();
        assertEquals( p2.get0(), f );
        assertEquals( p2.get1(), g );
    }
}