//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.crs.projections.cylindric;

import static org.deegree.crs.projections.ProjectionUtils.calcPhiFromConformalLatitude;
import static org.deegree.crs.projections.ProjectionUtils.preCalcedThetaSeries;

import javax.vecmath.Point2d;

import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.EPSGCode;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.exceptions.ProjectionException;
import org.deegree.crs.projections.ProjectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>Mercator</code> projection has following properties:
 * <ul>
 * <li>Cylindircal</li>
 * <li>Conformal</li>
 * <li>Meridians are equally spaced straight lines</li>
 * <li>Parallels are unequally spaced straight lines closest near the equator, cutting meridians at right angles.</li>
 * <li>Scale is true along the Equator, or along two parallels equidistant from the Equator</li>
 * <li>Loxodromes (rhumb lines) are straight lines</li>
 * <li>Not perspective</li>
 * <li>Poles are at infinity; great distortion or area in polar regions</li>
 * <li>Used for navigation</li>
 * <li>Presented by Mercator in 1569</li>
 * </ul>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class Mercator extends CylindricalProjection {

    private static Logger LOG = LoggerFactory.getLogger( Mercator.class );

    private double[] preCalcedPhiSeries;

    /**
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     * @param id
     *            an identifiable instance containing information about this projection
     */
    public Mercator( GeographicCRS geographicCRS, double falseNorthing, double falseEasting, Point2d naturalOrigin,
                     Unit units, double scale, CRSIdentifiable id ) {
        super( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale, true, false, id );
        preCalcedPhiSeries = preCalcedThetaSeries( getSquaredEccentricity() );
    }

    /**
     * Sets the id to EPSG:9804
     * 
     * @param geographicCRS
     * @param falseNorthing
     * @param falseEasting
     * @param naturalOrigin
     * @param units
     * @param scale
     */
    public Mercator( GeographicCRS geographicCRS, double falseNorthing, double falseEasting, Point2d naturalOrigin,
                     Unit units, double scale ) {
        this( geographicCRS, falseNorthing, falseEasting, naturalOrigin, units, scale,
              new CRSIdentifiable( new EPSGCode( 9804 ) ) );
    }

    @Override
    public Point2d doInverseProjection( double x, double y )
                            throws ProjectionException {
        Point2d result = new Point2d( 0, 0 );
        LOG.debug( "InverseProjection, incoming points x: " + x + " y: " + y );
        x -= getFalseEasting();
        y -= getFalseNorthing();

        result.x = ( x / getScaleFactor() ) + getProjectionLongitude();
        result.y = ProjectionUtils.HALFPI - 2. * Math.atan( Math.exp( -y / getScaleFactor() ) );
        if ( !isSpherical() ) {
            result.y = calcPhiFromConformalLatitude( result.y, preCalcedPhiSeries );
        }
        return result;
    }

    @Override
    public Point2d doProjection( double lambda, double phi )
                            throws ProjectionException {
        Point2d result = new Point2d( 0, 0 );
        lambda -= getProjectionLongitude();

        result.x = getScaleFactor() * lambda;
        if ( isSpherical() ) {
            result.y = getScaleFactor() * Math.log( Math.tan( ProjectionUtils.QUARTERPI + 0.5 * phi ) );
        } else {
            result.y = -getScaleFactor()
                       * Math.log( ProjectionUtils.tanHalfCoLatitude( phi, Math.sin( phi ), getEccentricity() ) );
        }
        result.x += getFalseEasting();
        result.y += getFalseNorthing();
        return result;
    }

    @Override
    public String getImplementationName() {
        return "mercator";
    }

}
