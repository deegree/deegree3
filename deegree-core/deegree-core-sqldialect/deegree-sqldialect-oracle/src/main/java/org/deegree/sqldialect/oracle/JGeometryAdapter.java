//$HeadURL: svn+ssh://criador.lat-lon.de/srv/svn/deegree-intern/trunk/latlon-sqldialect-oracle/src/main/java/de/latlon/deegree/sqldialect/oracle/JGeometryAdapter.java $
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
package org.deegree.sqldialect.oracle;

import static oracle.spatial.geometry.JGeometry.GTYPE_COLLECTION;
import static oracle.spatial.geometry.JGeometry.GTYPE_CURVE;
import static oracle.spatial.geometry.JGeometry.GTYPE_MULTICURVE;
import static oracle.spatial.geometry.JGeometry.GTYPE_MULTIPOINT;
import static oracle.spatial.geometry.JGeometry.GTYPE_MULTIPOLYGON;
import static oracle.spatial.geometry.JGeometry.GTYPE_POINT;
import static oracle.spatial.geometry.JGeometry.GTYPE_POLYGON;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKB;
import oracle.spatial.util.WKT;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.io.WKBReader;
import org.deegree.geometry.io.WKBWriter;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.standard.points.PackedPoints;

/**
 * Converts between Oracle {@link JGeometry} and deegree {@link Geometry} objects.
 * <p>
 * <h4>Topological constraints</h4>
 * Oracle expects a certain orientation for surface boundaries: The exterior ring must be oriented counter-clockwise and
 * the interior rings must be oriented clockwise, see <a
 * href="http://www.error-code.org.uk/view.asp?e=ORACLE-ORA-13367">here</a>.
 * <h4>SRID for an unknown CRS</h4>
 * In SQL, Oracle uses a value of <code>null</code> as SRID for undefined CRS. No official documentation on the integer
 * value corresponding to the <code>null</code> value for {@link JGeometry#setSRID(int)} could be found, but it appears
 * to be <code>0</code>.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schmitz $
 * 
 * @version $Revision: 310 $, $Date: 2011-06-17 14:03:30 +0200 (Fr, 17. Jun 2011) $
 */
public class JGeometryAdapter {

    private static GeometryFactory fac = new GeometryFactory();

    private final ICRS deegreeCRS;

    private final int oracleSRID;

    private final WKT oracleWktAdapter = new WKT();

    private final WKB oracleWkbAdapter = new WKB();

    private final WKTReader deegreeWktAdapter;

    /**
     * @param deegreeCRS
     * @param oracleSRID
     */
    public JGeometryAdapter( ICRS deegreeCRS, int oracleSRID ) {
        this.deegreeCRS = deegreeCRS;
        this.oracleSRID = oracleSRID;
        this.deegreeWktAdapter = new WKTReader( deegreeCRS );
    }

    public JGeometry toJGeometry( Geometry g ) {
        JGeometry jg = null;
        switch ( g.getGeometryType() ) {
        case COMPOSITE_GEOMETRY:
        case ENVELOPE:
        case MULTI_GEOMETRY:
        case PRIMITIVE_GEOMETRY: {
            // TODO implement native conversions
            try {
                byte[] wkb = WKBWriter.write( g );
                jg = oracleWkbAdapter.toJGeometry( wkb );
                jg.setSRID( oracleSRID );
            } catch ( Exception e ) {
                e.printStackTrace();
                String msg = "Internal error: Unable to convert from deegree geometry to JGeometry: " + e.getMessage();
                throw new RuntimeException( msg, e );
            }
        }
        }
        return jg;
    }

    public Geometry toGeometry( JGeometry jg ) {
        Geometry g = null;

        int type = jg.getType();
        switch ( type ) {
        case GTYPE_POINT: {
            g = fac.createPoint( null, jg.getPoint(), deegreeCRS );
            break;
        }
        case GTYPE_CURVE: {
            // TODO implement support for non-linear curves
            PackedPoints points = new PackedPoints( deegreeCRS, jg.getOrdinatesArray(), jg.getDimensions() );
            g = fac.createLineString( null, deegreeCRS, points );
            break;
        }
        case GTYPE_POLYGON: {
            // manually handle 3d bboxes
            if ( jg.getDimensions() == 3 && jg.getNumPoints() == 2 ) {
                double[] ords = jg.getOrdinatesArray();
                double[] min = new double[] { ords[0], ords[1], ords[2] };
                double[] max = new double[] { ords[3], ords[4], ords[5] };
                g = fac.createEnvelope( min, max, deegreeCRS );
                break;
            }
        }
        case GTYPE_MULTIPOINT:
        case GTYPE_MULTICURVE:
        case GTYPE_MULTIPOLYGON:
        case GTYPE_COLLECTION: {
            // TODO implement native conversions
            try {
                byte[] wkt = oracleWkbAdapter.fromJGeometry( jg );
                g = WKBReader.read( wkt, deegreeCRS );
            } catch ( Throwable e ) {
                // sometimes the WKB has unclosed rings, so let's try with WKT
                try {
                    byte[] wkt = oracleWktAdapter.fromJGeometry( jg );
                    g = deegreeWktAdapter.read( new String( wkt ) );
                } catch ( Throwable t ) {
                    e.printStackTrace();
                    String msg = "Internal error: Unable to convert JGeometry to deegree geometry: " + e.getMessage();
                    throw new RuntimeException( msg, e );
                }
            }
            break;
        }
        default: {
            throw new RuntimeException( "Internal error. Unhandled GTYPE (" + type + ")." );
        }
        }
        return g;
    }

}
