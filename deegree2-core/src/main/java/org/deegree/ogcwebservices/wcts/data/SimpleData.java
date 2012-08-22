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

package org.deegree.ogcwebservices.wcts.data;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.crs.transformations.Transformation;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * <code>SimpleData</code> takes a list of points which will be transformed.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class SimpleData extends TransformableData<Point3d> {
    private static ILogger LOG = LoggerFactory.getLogger( SimpleData.class );

    /**
     * The coordinate separator, default to ','
     */
    private final String cs;

    /**
     * The tuple separator, default to ' '
     */
    private final String ts;

    private List<Point3d> sourcePoints;

    /**
     * Creates a simple data instance.
     *
     * @param transformableData
     * @param cs
     *            the coordinate separator
     * @param ts
     *            the tuple separator
     * @throws IllegalArgumentException
     *             if either one of the crs's are <code>null</code>.
     */
    public SimpleData( List<Point3d> transformableData, String cs, String ts ) throws IllegalArgumentException {
        if ( transformableData == null ) {
            transformableData = new ArrayList<Point3d>();
        }
        this.sourcePoints = transformableData;
        if ( cs == null || "".equals( cs ) ) {
            cs = ",";
        }
        this.cs = cs;

        if ( ts == null || "".equals( ts ) ) {
            ts = " ";
        }
        this.ts = ts;
    }

    /**
     * using default values, cs="," and ts='space'
     */
    public SimpleData() {
        this( new ArrayList<Point3d>(), null, null );
    }

    @Override
    public void doTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, boolean enableLogging ) {
        GeoTransformer transformer = getGeotransformer( targetCRS );
        try {
            sourcePoints = transformer.transform( sourceCRS, sourcePoints );
        } catch ( IllegalArgumentException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( CRSTransformationException e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    @Override
    public void doTransform( Transformation transformation, boolean enableLogging )
                            throws OGCWebServiceException {
        GeoTransformer transformer = getGeotransformer( transformation );
        try {
            sourcePoints = transformer.transform( CRSFactory.create( transformation.getSourceCRS() ), sourcePoints );
        } catch ( IllegalArgumentException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( CRSTransformationException e ) {
            LOG.logError( e.getMessage(), e );
        }

    }

    /**
     * @return true if this simpleData has sourcePoints.
     */
    public boolean hasData() {
        return ( sourcePoints != null && sourcePoints.size() > 0 );
    }

    @Override
    public List<Point3d> getTransformedData() {
        return sourcePoints;
    }

    /**
     * @return the value for the ts attribute, defaults to a single space character.
     */
    public final String getTupleSeparator() {
        return ts;
    }

    /**
     * @return the value for the cs attribute, defaults to a single comma character.
     */
    public final String getCoordinateSeparator() {
        return cs;
    }

    /**
     * Parses a String of data into the appropriate Point3d, according to the given cs, ts and coordinate dimension.
     *
     * @param simpleData
     *            a String containing the data
     * @param sourceDim
     *            the dimension of the source coordinate system, in which the points are defined.
     * @param cs
     *            the coordinate separator.
     * @param ts
     *            the tuple separator.
     * @param ds
     *            the decimal separator currently not evaluated.
     * @return a list containing the parsed points or an empty list if the points could not be retrieved, never
     *         <code>null</code>
     */
    public static List<Point3d> parseData( String simpleData, int sourceDim, String cs, String ts, String ds ) {
        if ( simpleData == null || "".equals( simpleData.trim() ) ) {
            return new ArrayList<Point3d>();
        }
        List<Point3d> result = new ArrayList<Point3d>();
        simpleData = simpleData.trim();
        String[] tuples = simpleData.split( ts );

        int count = 0;
        for ( String tuple : tuples ) {
            count++;
            if ( tuple != null ) {
                tuple = tuple.trim();
                String[] coords = tuple.split( cs );
                if ( coords.length != sourceDim ) {
                    LOG.logError( Messages.getMessage( "WCTS_DIM_COORDS_NOT_CONGRUENT", sourceDim, cs ) );
                } else {
                    String first = coords[0];
                    String second = coords[1];
                    String third = null;
                    if ( sourceDim == 3 ) {
                        third = coords[2];
                    }
                    double x = 0;
                    double y = 0;
                    double z = 0;
                    try {
                        x = Double.parseDouble( first );
                    } catch ( NumberFormatException e ) {
                        LOG.logError( "Unparsable x value: " + x + " at coord " + count );
                        x = Double.NaN;
                    }
                    try {
                        y = Double.parseDouble( second );
                    } catch ( NumberFormatException e ) {
                        LOG.logError( "Unparsable y value: " + y + " at coord " + count );
                        y = Double.NaN;
                    }
                    if ( sourceDim == 3 ) {
                        try {
                            z = Double.parseDouble( third );
                        } catch ( NumberFormatException e ) {
                            LOG.logError( "Unparsable z value: " + z + " at coord " + count );
                            z = Double.NaN;
                        }
                    }
                    if ( !( Double.isNaN( x ) || Double.isNaN( y ) || Double.isNaN( z ) ) ) {
                        result.add( new Point3d( x, y, z ) );
                    }
                }
            }
        }
        return result;
    }

}
