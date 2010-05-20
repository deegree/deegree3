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
package org.deegree.feature.persistence.oracle;

import static oracle.spatial.geometry.JGeometry.GTYPE_COLLECTION;
import static oracle.spatial.geometry.JGeometry.GTYPE_CURVE;
import static oracle.spatial.geometry.JGeometry.GTYPE_MULTICURVE;
import static oracle.spatial.geometry.JGeometry.GTYPE_MULTIPOINT;
import static oracle.spatial.geometry.JGeometry.GTYPE_MULTIPOLYGON;
import static oracle.spatial.geometry.JGeometry.GTYPE_POINT;
import static oracle.spatial.geometry.JGeometry.GTYPE_POLYGON;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;

import org.deegree.cs.CRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.io.WKTWriter;

/**
 * Converts between Oracle {@link JGeometry} and deegree {@link Geometry} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JGeometryAdapter {

    private static GeometryFactory fac = new GeometryFactory();

    private final WKT wktAdapter = new WKT();

    private final CRS deegreeCRS;

    private final int oracleSRID;

    /**
     * @param deegreeCRS
     * @param oracleSRID
     */
    public JGeometryAdapter( CRS deegreeCRS, int oracleSRID ) {
        this.deegreeCRS = deegreeCRS;
        this.oracleSRID = oracleSRID;
    }

    /**
     * @param g
     * @return
     */
    public JGeometry toJGeometry( Geometry g ) {
        JGeometry jg = null;
        switch ( g.getGeometryType() ) {
        case COMPOSITE_GEOMETRY:
        case ENVELOPE:
        case MULTI_GEOMETRY:
        case PRIMITIVE_GEOMETRY: {
            // TODO implement native conversions
            try {
                String wkt = WKTWriter.write( g );
                jg = wktAdapter.toJGeometry( wkt.getBytes() );
                jg.setSRID( oracleSRID );
            } catch ( Exception e ) {
                e.printStackTrace();
                String msg = "Internal error: Unable to deegree geometry to JGeometry: " + e.getMessage();
                throw new RuntimeException( msg, e );
            }
        }
        }
        return jg;
    }

    /**
     * @param jg
     * @return
     */
    public Geometry toGeometry( JGeometry jg ) {
        Geometry g = null;
        switch ( jg.getType() ) {
        case GTYPE_POINT: {
            g = fac.createPoint( null, jg.getPoint(), deegreeCRS );
            break;
        }
        case GTYPE_CURVE:
        case GTYPE_POLYGON:
        case GTYPE_MULTIPOINT:
        case GTYPE_MULTICURVE:
        case GTYPE_MULTIPOLYGON:
        case GTYPE_COLLECTION: {
            // TODO implement native conversions
            try {
                byte[] wkt = wktAdapter.fromJGeometry( jg );
                g = WKTReader.read( new String( wkt ) );
                g.setCoordinateSystem( deegreeCRS );
            } catch ( Exception e ) {
                e.printStackTrace();
                String msg = "Internal error: Unable to convert JGeometry to deegree geometry: " + e.getMessage();
                throw new RuntimeException( msg, e );
            }
            break;
        }
        default: {
            throw new RuntimeException( "Internal error. Unhandled GTYPE (" + jg.getType() + ")." );
        }
        }
        return g;
    }
}
