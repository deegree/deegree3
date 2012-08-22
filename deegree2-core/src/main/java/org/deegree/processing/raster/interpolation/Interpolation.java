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
package org.deegree.processing.raster.interpolation;

import java.net.URI;
import java.net.URISyntaxException;

import org.deegree.datatypes.values.Interval;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.Values;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.io.quadtree.IndexException;
import org.deegree.io.quadtree.Quadtree;
import org.deegree.model.coverage.grid.FloatGridCoverage;
import org.deegree.model.spatialschema.Envelope;

/**
 * <code>Interpolation</code> is the abstract base class for all interpolation algorithms. Data
 * representation is done via the Quadtree interface.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class Interpolation {

    /**
     *
     */
    protected Quadtree<?> data;

    private static URI type = null;
    static {
        try {
            type = new URI( "xsd:integer" );
        } catch ( URISyntaxException never_happens ) {
            // nottin
        }
    }

    protected Values ignoreValues = new Values( new Interval[0], new TypedLiteral[0], new TypedLiteral( "-9999", type ) );

    protected double searchRadius1 = 0;

    protected double searchRadius2 = 0;

    protected double searchRadiusAngle = 0;

    protected int minData = 3;

    protected int maxData = Integer.MAX_VALUE;

    protected double noValue = -9999;

    protected double autoincreaseSearchRadius1 = 0;

    protected double autoincreaseSearchRadius2 = 0;

    private static final ILogger LOG = LoggerFactory.getLogger( Interpolation.class );

    /**
     *
     * @param data
     */
    protected Interpolation( Quadtree<?> data ) {
        this.data = data;
        searchRadius1 = calcSearchRadius();
        searchRadius2 = searchRadius1;
    }

    /**
     *
     * @param data
     * @param ignoreValues
     */
    protected Interpolation( Quadtree<?> data, Values ignoreValues ) {
        this.data = data;
        this.ignoreValues = ignoreValues;
        searchRadius1 = calcSearchRadius();
        searchRadius2 = searchRadius1;
    }

    /**
     *
     * @param data
     * @param ignoreValues
     * @param searchRadius1
     * @param searchRadius2
     * @param searchRadiusAngle
     * @param minData
     * @param maxData
     * @param noValue
     * @param autoincreaseSearchRadius1
     * @param autoincreaseSearchRadius2
     */
    protected Interpolation( Quadtree<?> data, Values ignoreValues, double searchRadius1, double searchRadius2,
                             double searchRadiusAngle, int minData, int maxData, double noValue,
                             double autoincreaseSearchRadius1, double autoincreaseSearchRadius2 ) {
        this.data = data;
        this.ignoreValues = ignoreValues;
        // this.envelope = envelope;
        this.searchRadius1 = searchRadius1;
        this.searchRadius2 = searchRadius2;
        this.searchRadiusAngle = searchRadiusAngle;
        this.minData = minData;
        this.maxData = maxData;
        this.noValue = noValue;
        this.autoincreaseSearchRadius1 = autoincreaseSearchRadius1;
        this.autoincreaseSearchRadius2 = autoincreaseSearchRadius2;
    }

    private double calcSearchRadius() {
        try {
            double w = data.getRootBoundingBox().getWidth();
            double h = data.getRootBoundingBox().getHeight();
            // default search radius is 20% of the target envelope
            return Math.sqrt( w * w + h * h ) / 5d;
        } catch ( IndexException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        }
        return 0;
    }

    /**
     * performs the interpolation
     *
     * @param width
     *            width of the result grid in number of cells
     * @param height
     *            height of the result grid in number of cells
     * @return result grid as an instance of
     * @see org.deegree.model.coverage.grid.GridCoverage
     * @throws InterpolationException
     */
    public FloatGridCoverage interpolate( int width, int height )
                            throws InterpolationException {

        Envelope envelope = null;

        try {
            envelope = data.getRootBoundingBox();
        } catch ( IndexException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        }

        WorldToScreenTransform trans = new WorldToScreenTransform( envelope.getMin().getX(), envelope.getMin().getY(),
                                                                   envelope.getMax().getX(), envelope.getMax().getY(),
                                                                   0, 0, width - 1, height - 1 );

        float[][][] data = new float[1][height][width];
        for ( int i = 0; i < data[0][0].length; i++ ) {
            for ( int j = 0; j < data[0].length; j++ ) {
                data[0][j][i] = (float) calcInterpolatedValue( trans.getSourceX( i ), trans.getSourceY( j ),
                                                               searchRadius1, searchRadius2 );
            }
        }

        // the CoverageOffering is passed as null here, desired? TODO
        FloatGridCoverage result = new FloatGridCoverage( null, envelope, data );

        return result;
    }

    /**
     * calculates the interpolated value for a position defined by x and y
     *
     * @param x
     * @param y
     * @param searchRadius1
     * @param searchRadius2
     * @return the interpolated value
     * @throws InterpolationException
     */
    public abstract double calcInterpolatedValue( double x, double y, double searchRadius1, double searchRadius2 )
                            throws InterpolationException;

}
