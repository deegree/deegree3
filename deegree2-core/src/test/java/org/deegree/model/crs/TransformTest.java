//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/model/crs/TransformTest.java $
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
package org.deegree.model.crs;

import java.net.URL;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;

import alltests.Configuration;

/**
 *
 * The <code>TransformTest</code> class can be used to test the transform of a feature collection.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 */
public class TransformTest extends TestCase {
    private static ILogger LOG = LoggerFactory.getLogger( TransformTest.class );
    /**
     * test transforming all geometries contained within a FeatureCollection
     *
     */
    public void testTransformFeatureCollection() {
        try {
            URL url = new URL( Configuration.getGMLBaseDir(), Configuration.GML_COMPLEX_EXAMPLE );
            GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
            doc.load( url );
            FeatureCollection fc = doc.parse();
            CoordinateSystem crs = CRSFactory.create( "epsg:31467" );
            GeoTransformer gt = new GeoTransformer( crs );
            gt.transform( fc );
            for ( int i = 0; i < fc.size(); i++ ) {
                assertEquals( true, isTargetCRS( fc.getFeature( i ), crs ) );
            }

            // GMLFeatureAdapter ada = new GMLFeatureAdapter();
            // ada.export( fc ).prettyPrint( System.out );

        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            assertEquals( true, false );
        }
    }

    private boolean isTargetCRS( Feature feature, CoordinateSystem targetCRS )
                            throws CRSTransformationException, GeometryException {

        FeatureProperty[] fp = feature.getProperties();
        for ( int i = 0; i < fp.length; i++ ) {

            if ( fp[i].getValue() instanceof Geometry ) {
                Geometry geom = (Geometry) fp[i].getValue();
                if ( !targetCRS.equals( geom.getCoordinateSystem() ) ) {
                    LOG.logInfo( geom.getCoordinateSystem().getIdentifier() );
                    LOG.logInfo( " - " + targetCRS.getIdentifier() );
                    return false;
                }
            } else if ( fp[i].getValue() instanceof Feature ) {
                if ( !isTargetCRS( (Feature) fp[i].getValue(), targetCRS ) ) {
                    return false;
                }
            }

        }

        return true;
    }

}
