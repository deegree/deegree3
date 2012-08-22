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

package org.deegree.io.mapinfoapi;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.deegree.model.spatialschema.GeometryFactory.createCurve;
import static org.deegree.model.spatialschema.GeometryFactory.createCurveSegment;
import static org.deegree.model.spatialschema.GeometryFactory.createMultiPoint;
import static org.deegree.model.spatialschema.GeometryFactory.createMultiSurface;
import static org.deegree.model.spatialschema.GeometryFactory.createPoint;
import static org.deegree.model.spatialschema.GeometryFactory.createPosition;
import static org.deegree.model.spatialschema.GeometryFactory.createSurface;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;

/**
 * <code>MIFGeometryParser</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MIFGeometryParser {

    private static final ILogger LOG = LoggerFactory.getLogger( MIFGeometryParser.class );

    private StreamTokenizer mif;

    private CoordinateSystem crs;

    /**
     *
     */
    public LinkedList<String> errors = new LinkedList<String>();

    /**
     * @param mif
     * @param crs
     */
    public MIFGeometryParser( StreamTokenizer mif, CoordinateSystem crs ) {
        this.mif = mif;
        this.crs = crs;
    }

    private Position[] parsePositions( int cnt )
                            throws IOException {
        Position[] ps = new Position[cnt];
        for ( int k = 0; k < cnt; ++k ) {
            double x = parseDouble( mif.sval );
            mif.nextToken();
            double y = parseDouble( mif.sval );
            mif.nextToken();

            ps[k] = createPosition( x, y );
        }

        return ps;
    }

    /**
     * @return a deegree Point
     * @throws IOException
     */
    public Point parsePoint()
                            throws IOException {
        LOG.logDebug( "Parsing point..." );
        mif.nextToken();
        double x = parseDouble( mif.sval );
        mif.nextToken();
        double y = parseDouble( mif.sval );
        mif.nextToken();

        return createPoint( x, y, crs );
    }

    /**
     * @return a deegree multipoint
     * @throws IOException
     */
    public MultiPoint parseMultipoint()
                            throws IOException {
        LOG.logDebug( "Parsing multipoint..." );
        mif.nextToken();

        int cnt = parseInt( mif.sval );
        mif.nextToken();
        Point[] points = new Point[cnt];

        for ( int i = 0; i < cnt; ++i ) {
            double x = parseDouble( mif.sval );
            mif.nextToken();
            double y = parseDouble( mif.sval );
            mif.nextToken();

            points[i] = createPoint( x, y, crs );
        }

        return createMultiPoint( points );
    }

    /**
     * @return a deegree Curve or null, if the line contained errors
     * @throws IOException
     */
    public Curve parseLine()
                            throws IOException {
        LOG.logDebug( "Parsing line..." );
        mif.nextToken();

        Position[] ps = parsePositions( 2 );

        try {
            return createCurve( ps, crs );
        } catch ( GeometryException e ) {
            errors.add( e.getLocalizedMessage() );
        }

        return null;
    }

    /**
     * @return a deegree Curve or null, if errors were found
     * @throws IOException
     */
    public Curve parsePLine()
                            throws IOException {
        LOG.logDebug( "Parsing pline..." );
        mif.nextToken();

        int cnt = 1;

        if ( mif.sval.equals( "multiple" ) ) {
            mif.nextToken();
            cnt = parseInt( mif.sval );
            mif.nextToken();
        }

        CurveSegment[] segs = new CurveSegment[cnt];

        for ( int i = 0; i < cnt; ++i ) {
            int pcnt = Integer.parseInt( mif.sval );
            mif.nextToken();

            Position[] ps = parsePositions( pcnt );

            try {
                segs[i] = createCurveSegment( ps, crs );
            } catch ( GeometryException e ) {
                errors.add( e.getLocalizedMessage() );
            }
        }

        try {
            return cnt > 1 ? createCurve( segs ) : createCurve( segs[0] );
        } catch ( GeometryException e ) {
            errors.add( e.getLocalizedMessage() );
        }

        return null;
    }

    /**
     * Null will be returned if polygons are broken, for example the two-point-polygons (actual
     * example exported from MapInfo!):
     *
     * <pre>
     *             Region 1
     *             2
     *             2505127 5631765
     *             2505127 5631765
     * </pre>
     *
     * @return a deegree multi surface or null in case of errors
     * @throws IOException
     */
    public MultiSurface parseRegion()
                            throws IOException {
        LOG.logDebug( "Parsing region..." );
        mif.nextToken();

        int cnt = parseInt( mif.sval );
        mif.nextToken();
        HashSet<Surface> polygons = new HashSet<Surface>();

        // the mapping is used to extract the relevant rings later on
        HashMap<Surface, Pair<Position[], Position[][]>> arrays = new HashMap<Surface, Pair<Position[], Position[][]>>();

        for ( int i = 0; i < cnt; ++i ) {

            int pcnt = parseInt( mif.sval );
            mif.nextToken();

            Position[] ps = parsePositions( pcnt );

            try {
                Surface s = createSurface( ps, null, null, crs );
                arrays.put( s, new Pair<Position[], Position[][]>( ps, null ) );
                polygons.add( s );
            } catch ( GeometryException e ) {
                errors.add( e.getLocalizedMessage() );
            }
        }

        // fixed point, should sort the polygons by size first
        // the mapping is used here to extract the rings
        fp: while ( true ) {
            for ( Surface s : polygons ) {
                for ( Surface t : polygons ) {
                    if ( s.contains( t ) ) {
                        polygons.remove( t );
                        polygons.remove( s );

                        Pair<Position[], Position[][]> spos = arrays.get( s );
                        Pair<Position[], Position[][]> tpos = arrays.get( t );

                        arrays.remove( s );
                        arrays.remove( t );

                        Position[] outer = spos.first;
                        Position[][] inner = new Position[spos.second == null ? 1 : spos.second.length + 1][];
                        if ( spos.second != null ) {
                            for ( int i = 0; i < spos.second.length; ++i ) {
                                inner[i] = spos.second[i];
                            }
                        }
                        inner[inner.length - 1] = tpos.first;

                        if ( tpos.second != null ) {
                            LOG.logWarning( "Ignoring previously found inner rings. Is your data corrupt?" );
                        }

                        Surface nsurface = null;
                        try {
                            nsurface = createSurface( outer, inner, null, crs );
                        } catch ( GeometryException e ) {
                            errors.add( e.getLocalizedMessage() );
                        }

                        arrays.put( nsurface, new Pair<Position[], Position[][]>( outer, inner ) );

                        polygons.add( nsurface );

                        continue fp;
                    }
                }
            }
            break;
        }

        // can happen if polygons are broken, for example the two-point-polygons (actual example
        // exported from MapInfo!):
        // Region 1
        // 2
        // 2505127 5631765
        // 2505127 5631765
        if ( polygons.size() == 0 ) {
            return null;
        }

        MultiSurface ms = createMultiSurface( polygons.toArray( new Surface[polygons.size()] ) );

        return ms;
    }

    /**
     * Currently just skips over the tokens.
     *
     * @throws IOException
     */
    public void parseArc()
                            throws IOException {
        LOG.logDebug( "Parsing arc..." );
        LOG.logWarning( "Arcs are not understood and will be ignored." );
        mif.nextToken(); // x1, y1, x2, y2 coordinates
        mif.nextToken();
        mif.nextToken();
        mif.nextToken();

        mif.nextToken(); // a, b
        mif.nextToken();
    }

    /**
     * Currently just skips over the tokens.
     *
     * @throws IOException
     */
    public void parseRoundRect()
                            throws IOException {
        LOG.logDebug( "Parsing roundrect..." );

        LOG.logWarning( "Roundrects are not understood and will be ignored." );
        mif.nextToken(); // x1, y1, x2, y2 coordinates
        mif.nextToken();
        mif.nextToken();
        mif.nextToken();

        mif.nextToken(); // a
    }

    /**
     * Currently just skips over the tokens.
     *
     * @throws IOException
     */
    public void parseEllipse()
                            throws IOException {
        LOG.logDebug( "Parsing ellipse..." );
        LOG.logWarning( "Ellipses are not understood and will be ignored." );
        mif.nextToken(); // x1, y1, x2, y2 coordinates
        mif.nextToken();
        mif.nextToken();
        mif.nextToken();
        mif.nextToken();
    }

    /**
     * @return a deegree surface with 5 points
     * @throws IOException
     */
    public Surface parseRect()
                            throws IOException {
        LOG.logDebug( "Parsing rect..." );

        mif.nextToken();
        double x1 = Double.parseDouble( mif.sval );
        mif.nextToken();
        double y1 = Double.parseDouble( mif.sval );
        mif.nextToken();
        double x2 = Double.parseDouble( mif.sval );
        mif.nextToken();
        double y2 = Double.parseDouble( mif.sval );
        mif.nextToken();

        Position[] ps = new Position[5];
        ps[0] = createPosition( x1, y1 );
        ps[1] = createPosition( x2, y1 );
        ps[2] = createPosition( x2, y2 );
        ps[3] = createPosition( x1, y2 );
        ps[4] = createPosition( x1, y1 );
        try {
            return createSurface( ps, null, null, crs );
        } catch ( GeometryException e ) {
            errors.add( e.getLocalizedMessage() );
        }

        return null;
    }

}
