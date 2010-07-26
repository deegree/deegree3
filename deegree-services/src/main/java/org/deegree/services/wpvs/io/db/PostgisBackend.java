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

package org.deegree.services.wpvs.io.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.postgis.Geometry;
import org.postgis.LinearRing;
import org.postgis.PGbox3d;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

/**
 * The <code>PostgisBackend</code> class adds postgis specific methods to the model backend.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class PostgisBackend extends DBBackend<PGgeometry> {

    /**
     * @param connectionID
     *            pointing to the configured database connection.
     * @param type 
     */
    public PostgisBackend( String connectionID, Type type ) {
        super( connectionID, type );
    }

    @Override
    public Envelope createEnvelope( PGgeometry geometry ) {
        Envelope bbox = null;
        double[] translationVector = getWPVSTranslationVector();
        if ( geometry != null ) {
            Geometry geom = geometry.getGeometry();
            int pgType = geom.getType();
            if ( pgType != Geometry.POLYGON ) {
                throw new UnsupportedOperationException(
                                                         "Currently only org.postgis.Polygon is supported as PGGeometry." );
            }
            Polygon pgPolygon = (Polygon) geom;
            CRS crs = new CRS( "EPSG:" + pgPolygon.getSrid() );
            org.postgis.LinearRing ring = pgPolygon.getRing( 0 );
            Point min = ring.getPoint( 0 );
            Point max = ring.getPoint( 2 );
            double[] mi = null;
            double[] ma = null;
            // if geometry is 2-dimensional
            if ( pgPolygon.getDimension() == 2 ) {
                mi = new double[] { translationVector[0] + min.getX(), translationVector[1] + min.getY() };
                ma = new double[] { translationVector[0] + max.getX(), translationVector[1] + max.getY() };

            } else {
                // if geometry is 3-dimensional
                mi = new double[] { translationVector[0] + min.getX(), translationVector[1] + min.getY(), min.getZ() };
                ma = new double[] { translationVector[0] + max.getX(), translationVector[1] + max.getY(), max.getZ() };

            }
            bbox = geomFactory.createEnvelope( mi, ma, crs );
        }

        return bbox;
    }

    @Override
    public PGgeometry createBackendEnvelope( Envelope geometry, int dimension ) {
        LinearRing[] linRing = new LinearRing[1];
        Point[] points = new Point[5];
        org.deegree.geometry.primitive.Point min = geometry.getMin();
        org.deegree.geometry.primitive.Point max = geometry.getMax();
        double[] translationVector = getWPVSTranslationVector();
        // min
        double[] mT = min.getAsArray();
        double[] minD = new double[mT.length];
        System.arraycopy( mT, 0, minD, 0, minD.length );
        minD[0] -= translationVector[0];
        minD[1] -= translationVector[1];

        // max
        mT = max.getAsArray();
        double[] maxD = new double[mT.length];
        System.arraycopy( mT, 0, maxD, 0, maxD.length );
        maxD[0] -= translationVector[0];
        maxD[1] -= translationVector[1];
        if ( dimension == 2 ) {
            points[0] = new Point( minD[0], minD[1] );
            points[1] = new Point( maxD[0], minD[1] );
            points[2] = new Point( maxD[0], maxD[1] );
            points[3] = new Point( minD[0], maxD[1] );
            points[4] = new Point( minD[0], minD[1] );

        } else {

            points[0] = new Point( minD[0], minD[1], minD[2] );
            points[1] = new Point( maxD[0], minD[1], minD[2] );
            points[2] = new Point( maxD[0], maxD[1], maxD[2] );
            points[3] = new Point( minD[0], maxD[1], minD[2] );
            points[4] = new Point( minD[0], minD[1], minD[2] );
        }
        linRing[0] = new LinearRing( points );
        org.postgis.Polygon pgPoly = new org.postgis.Polygon( linRing );
        pgPoly.setSrid( parseSRID( geometry.getCoordinateSystem() ) );
        return new PGgeometry( pgPoly );

    }

    private int parseSRID( CRS crs ) {
        int result = 31466;
        if ( crs != null ) {
            int index = crs.getName().lastIndexOf( ":" );
            if ( index != -1 ) {
                try {
                    result = Integer.parseInt( crs.getName().substring( index ) );
                } catch ( NumberFormatException e ) {
                    // nothing
                }
            }
        }
        return result;
    }

    @Override
    protected String getDriverPrefix() {
        return "jdbc:postgresql://";
    }

    @Override
    protected Envelope getDatasetEnvelope( Connection con, String tableName, String geomColumn )
                            throws SQLException {
        Envelope result = null;
        PreparedStatement statement = con.prepareStatement( "SELECT extent3d(" + geomColumn + ") FROM " + tableName );
        ResultSet rs = statement.executeQuery();
        if ( rs.next() ) {
            Object box3d = rs.getObject( 1 );
            if ( box3d != null ) {
                if ( box3d instanceof PGbox3d ) {
                    PGbox3d bbox = (PGbox3d) box3d;
                    double[] min = new double[] { bbox.getLLB().x, bbox.getLLB().y, bbox.getLLB().z };
                    double[] max = new double[] { bbox.getURT().x, bbox.getURT().y, bbox.getURT().z };
                    double[] trans = getWPVSTranslationVector();
                    min[0] += trans[0];
                    min[1] += trans[1];
                    max[0] += trans[0];
                    max[1] += trans[1];
                    result = geomFactory.createEnvelope( min, max, getBaseCRS() );
                } else {
                    throw new SQLException( "Could not retrieve wkt from column: " + geomColumn + " of table "
                                            + tableName + " because the resulting geometry was null." );
                }
            }
        }
        return result;
    }
}
