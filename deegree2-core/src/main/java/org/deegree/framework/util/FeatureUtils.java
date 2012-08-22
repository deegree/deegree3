//$HeadURL$
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
package org.deegree.framework.util;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Surface;

/**
 * The <code>FeatureUtils</code> class offeres several static methods for handling features.
 * 
 * TODO add class documentation here.
 * 
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureUtils {

    private static final ILogger LOG = LoggerFactory.getLogger( FeatureUtils.class );

    /**
     * This method separates the features in the given FeatureCollection according to their geometry type into Points,
     * MulitPoints, (Multi-)Curves, (Multi-)Surfaces. features with geometries that do not match these geometry types
     * are collected in a five group (others).
     * 
     * The separation according to geometryType is needed to prepare the feature collections for creating shape files.
     * 
     * @param fc
     * @return an array of five feature collections, containing the features of the original fc separated according to
     *         their geometry type. The first entry contains Points, the second contains MultiPoints, the third contains
     *         Curves and MultiCurves, the fourth contains Surfaces and MultiSurfaces. If the original fc contains
     *         features with MultiGeoemetries, these features are added to the fourth fc.
     */
    public static FeatureCollection[] separateFeaturesForShapes( FeatureCollection fc ) {

        FeatureCollection fcPoint = FeatureFactory.createFeatureCollection( fc.getId() + "_Points", null );
        FeatureCollection fcMultiPoint = FeatureFactory.createFeatureCollection( fc.getId() + "_MultiPoints", null );
        FeatureCollection fcCurve = FeatureFactory.createFeatureCollection( fc.getId() + "_Curves", null );
        FeatureCollection fcSurface = FeatureFactory.createFeatureCollection( fc.getId() + "_Surfaces", null );
        FeatureCollection fcOther = FeatureFactory.createFeatureCollection( fc.getId() + "_Other", null );

        for ( int i = 0; i < fc.size(); i++ ) {
            Feature f = fc.getFeature( i );
            Geometry geom = f.getDefaultGeometryPropertyValue();

            if ( geom instanceof Point ) {
                fcPoint.add( f );
                LOG.logDebug( "Feature contains a POINT" );
            } else if ( geom instanceof MultiPoint ) {
                fcMultiPoint.add( f );
                LOG.logDebug( "Feature contains a MULTIPOINT" );
            } else if ( geom instanceof Curve || geom instanceof MultiCurve ) {
                fcCurve.add( f );
                LOG.logDebug( "Feature contains a LINESTRING" );
            } else if ( geom instanceof Surface || geom instanceof MultiSurface ) {
                fcSurface.add( f );
                LOG.logDebug( "Feature contains a POLYGON" );
            } else {
                LOG.logDebug(
                              "Feature geometry is neither POINT, MULTIPOINT, CURVE, MULTICURVE, SURFACE, MULTISURFACE. ",
                              f.getName(), " contains a geometry of type: " + geom.getClass() );
                fcOther.add( f );
            }
        }
        return new FeatureCollection[] { fcPoint, fcMultiPoint, fcCurve, fcSurface, fcOther };
    }

}
