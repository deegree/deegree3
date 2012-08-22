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

package org.deegree.ogcwebservices.wpvs.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.wpvs.j3d.ViewPoint;

/**
 * This class divides a visible area into stripes by calculating a resolution for the near clipping plane and the far
 * clipping plane. The near resolution (the one closest to the viewer) is doubled until it is <= far clippingplane
 * resolution. If the Viewer has a pitch which is greater than the angle of view (in other words: looking steeply down)
 * the resolutionstripes are concentric quads, which are adapted to the form of the footprint (aka. the perspective
 * ViewFrustum's intersection with the ground).
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */

public class StripeFactory {
    private final ILogger LOG = LoggerFactory.getLogger( StripeFactory.class );

    private final ViewPoint viewPoint;

    private double minScaleResolution;

    /**
     * @param vp
     *            the capsulation of the location and viewing direction of the viewer.
     * @param minScaleDenominator
     *            the best possible resolution the datasets can show.
     */
    public StripeFactory( ViewPoint vp, double minScaleDenominator ) {
        this.viewPoint = vp;
        if ( minScaleDenominator < 0.0001 )
            minScaleDenominator = 0.0001;
        this.minScaleResolution = minScaleDenominator /** MapUtils.DEFAULT_PIXEL_SIZE*/;
        LOG.logDebug( "The minimal scale Resolution: " + minScaleResolution);
    }

    /**
     * Calculates the number of resolutionstripes, their size and their position in worldcoordinates.
     *
     * @param imageWidth
     *            of the request
     * @param minimalHeight
     *            the minimalHeight of the terrain
     * @param g2d
     *            if !null the stripes are drawn on the graphics object (handy for debugging purposes
     * @param scale
     *            of the heightmap.
     * @return the resolutionsstripes for the footprint
     */
    public ArrayList<ResolutionStripe> createResolutionStripes( int imageWidth, double minimalHeight, Graphics2D g2d,
                                                                double scale ) {

        // Transformations used to calculate the footprint
        Transform3D invertTransform = viewPoint.getSimpleTransform();
        invertTransform.invert();
        Transform3D transform = viewPoint.getSimpleTransform();

        Point3d[] footprint = this.viewPoint.getFootprint();
        Vector3d farLeft = new Vector3d( footprint[0] );
        Vector3d farRight = new Vector3d( footprint[1] );
        Vector3d nearLeft = new Vector3d( footprint[2] );
        Vector3d nearRight = new Vector3d( footprint[3] );

        if ( g2d != null ) {
            g2d.drawString( "nearLeft", (int) nearLeft.x, (int) nearLeft.y );
            g2d.drawString( "farLeft", (int) farLeft.x, (int) farLeft.y );
            g2d.drawString( "nearRight", (int) nearRight.x, (int) nearRight.y );
            g2d.drawString( "farRight", (int) farRight.x, (int) farRight.y );

        }

        invertTransform.transform( nearRight );
        invertTransform.transform( farRight );
        Vector3d length = new Vector3d( farRight );
        length.sub( nearRight );

        // double halfAngleOfView = viewPoint.getAngleOfView() * 0.5;
        double footprintSideGradient = Math.acos( Math.abs( farRight.y - nearRight.y ) / length.length() );
        LOG.logDebug( "halfAngleOfView : " + Math.toDegrees( footprintSideGradient ) );
        transform.transform( nearRight );
        transform.transform( farRight );

        // We use a tangens therefore checking if we are close to 0 - 180
        if ( Math.abs( ( footprintSideGradient + Math.toRadians( 90 ) ) % Math.toRadians( 180 ) ) <= 0.00001 )
            return null;

        // !!!!For now the footprint dimensions are in meters
        double footprintNearResolution = StripeFactory.calcScaleOfVector( nearRight, nearLeft, imageWidth );

        LOG.logDebug( "nearRight: " + nearRight );
        LOG.logDebug( "nearRight: " + nearLeft );
        length = new Vector3d( nearLeft );
        length.sub( nearRight );

        LOG.logDebug( "length: " + length.length() );
        LOG.logDebug( "nearResolution: " + footprintNearResolution );
        LOG.logDebug( "(nearResolution*imageWidth)/sqrt2): " + ( ( footprintNearResolution * imageWidth ) / MapUtils.SQRT2 ) );
        double footprintFarResolution = StripeFactory.calcScaleOfVector( farRight, farLeft, imageWidth );

        // if the near clipping plane is behind the viewer, which means pitch > angleOfView the
        // resolutionStripes should be centered around the viewpoint
        if ( viewPoint.isNearClippingplaneBehindViewPoint() ) {
            return createStripesForHighPitch( invertTransform,
                                              transform,
                                              farRight,
                                              farLeft,
                                              nearRight,
                                              nearLeft,
                                              imageWidth,
                                              minimalHeight,
                                              footprintSideGradient,
                                              footprintNearResolution,
                                              footprintFarResolution,
                                              g2d,
                                              scale );
        }
        return createStripesForFrontalPerspective( invertTransform,
                                                   transform,
                                                   farRight,
                                                   farLeft,
                                                   nearRight,
                                                   nearLeft,
                                                   imageWidth,
                                                   minimalHeight,
                                                   footprintSideGradient,
                                                   footprintNearResolution,
                                                   footprintFarResolution,
                                                   g2d,
                                                   scale );
    }

    private ArrayList<ResolutionStripe> createStripesForFrontalPerspective( Transform3D invertTransform,
                                                                            Transform3D transform, Vector3d farRight,
                                                                            Vector3d farLeft, Vector3d nearestRight,
                                                                            Vector3d nearestLeft, double imageWidth,
                                                                            double minimalHeight,
                                                                            double footprintSideGradientAngle,
                                                                            double footprintNearResolution,
                                                                            double footprintFarResolution,
                                                                            Graphics2D g2d, double scale ) {
        ArrayList<ResolutionStripe> resultStripes = new ArrayList<ResolutionStripe>( 10 );

        if ( minScaleResolution > footprintNearResolution ) {
            LOG.logWarning( "the footprintnearResolution " + footprintNearResolution + " is smaller than the defined minScaleResolution(" + minScaleResolution + "), replacing footprints resolution accordingly" );
            footprintNearResolution = minScaleResolution;
        }
        double resolutionQuotient = footprintFarResolution / footprintNearResolution;
        int numberOfStripes = nearestPowerOfTwo( resolutionQuotient );

        if ( numberOfStripes == 0 ) {// can happen if the aov is close to the pitch
            LOG.logError( "No resolution stripes in frontal view!!!" );
            resultStripes.add( createResolutionStripe( nearestRight,
                                                       nearestLeft,
                                                       farLeft,
                                                       farRight,
                                                       imageWidth,
                                                       minimalHeight,
                                                       scale ) );
            return resultStripes;
        }

        // Find the distances to switch levelofdetail
        for ( int stripesCounter = 0; stripesCounter < numberOfStripes; ++stripesCounter ) {
            // go back to null point
            invertTransform.transform( nearestRight );
            invertTransform.transform( nearestLeft );

            double xLength = ( nearestRight.x - nearestLeft.x ) * 0.5;
            double yLength = xLength / Math.tan( footprintSideGradientAngle );

            // create the new slice by adding the lengt in x and y direction
            Vector3d tmpFarRight = new Vector3d( nearestRight.x + xLength, nearestRight.y - yLength, nearestRight.z );
            Vector3d tmpFarLeft = new Vector3d( nearestLeft.x - xLength, nearestLeft.y - yLength, nearestLeft.z );

            // transform the resulting shape back to the original position.
            transform.transform( tmpFarRight );
            transform.transform( tmpFarLeft );
            transform.transform( nearestRight );
            transform.transform( nearestLeft );

            ResolutionStripe rs = createResolutionStripe( nearestLeft,
                                                          nearestRight,
                                                          tmpFarRight,
                                                          tmpFarLeft,
                                                          imageWidth,
                                                          minimalHeight,
                                                          scale );
            if ( rs != null )
                resultStripes.add( rs );

            /**
             * For debugging purposes
             */
            if ( g2d != null ) {
                g2d.setColor( Color.ORANGE );
                g2d.drawLine( (int) nearestRight.x, (int) nearestRight.y, (int) nearestLeft.x, (int) nearestLeft.y );
                g2d.drawString( "nearestRight", (int) nearestRight.x, (int) nearestRight.y );
                g2d.drawString( "nearestLeft", (int) nearestLeft.x, (int) nearestLeft.y );
            }

            nearestRight = new Vector3d( tmpFarRight );
            nearestLeft = new Vector3d( tmpFarLeft );
        }

        ResolutionStripe rs = createResolutionStripe( nearestLeft,
                                                      nearestRight,
                                                      farRight,
                                                      farLeft,
                                                      imageWidth,
                                                      minimalHeight,
                                                      scale );
        if ( rs != null )
            resultStripes.add( rs );

        if ( g2d != null ) {
            g2d.setColor( Color.ORANGE );
            g2d.drawLine( (int) nearestRight.x, (int) nearestRight.y, (int) nearestLeft.x, (int) nearestLeft.y );
            g2d.drawLine( (int) farRight.x, (int) farRight.y, (int) farLeft.x, (int) farLeft.y );
            g2d.drawString( "nearestRight", (int) nearestRight.x, (int) nearestRight.y );
            g2d.drawString( "nearestLeft", (int) nearestLeft.x, (int) nearestLeft.y );

        }

        return resultStripes;
    }

    /**
     * This method finds the resolutionstripes around the Viewpoint if the nearClippingplane is behind the viewer (which
     * happens if the pitch is higher as the angleOfView).
     *
     * @param invertTransform
     * @param transform
     * @param farRight
     * @param farLeft
     * @param nearRight
     * @param nearLeft
     * @param imageWidth
     * @param minimalHeight
     * @param halfAngleOfView
     * @param footprintNearResolution
     * @param footprintFarResolution
     * @param g2d
     * @return the ResolutionStripes centered around the viewpoint.
     */
    private ArrayList<ResolutionStripe> createStripesForHighPitch( Transform3D invertTransform, Transform3D transform,
                                                                   Vector3d farRight, Vector3d farLeft,
                                                                   Vector3d nearRight, Vector3d nearLeft,
                                                                   double imageWidth, double minimalHeight,
                                                                   double halfAngleOfView,
                                                                   double footprintNearResolution,
                                                                   double footprintFarResolution, Graphics2D g2d,
                                                                   double scale ) {
        ArrayList<ResolutionStripe> resultStripes = new ArrayList<ResolutionStripe>( 40 );

        // double halfAngleOfView = viewPoint.getAngleOfView() * 0.5;

        Vector3d origin = new Vector3d( viewPoint.getObserverPosition() );
        invertTransform.transform( origin );

        invertTransform.transform( nearRight );
        Vector3d length = new Vector3d( nearRight );
        length.sub( origin );

        halfAngleOfView = Math.acos( Math.abs( origin.y - nearRight.y ) / length.length() );
        LOG.logDebug( "halfAngleOfView: " + Math.toDegrees( halfAngleOfView ) );

        transform.transform( nearRight );

        if ( minScaleResolution > footprintNearResolution )
            footprintNearResolution = minScaleResolution;

        double resolutionStripeLength = imageWidth * ( ((minScaleResolution)) / MapUtils.SQRT2 );

        int numberOfStripesNear = nearestPowerOfTwo( Math.floor( footprintNearResolution / (minScaleResolution) ) );

        double tangensOfHalfAngleOfView = Math.tan( halfAngleOfView );

        double yDistance = ( resolutionStripeLength * 0.5 ) / tangensOfHalfAngleOfView;

        LOG.logDebug( "yDistance: " + yDistance );

        Vector3d backNearestLeft = new Vector3d( origin.x + resolutionStripeLength * 0.5,
                                                 origin.y - yDistance,
                                                 nearLeft.z );
        Vector3d backNearestRight = new Vector3d( origin.x - resolutionStripeLength * 0.5,
                                                  origin.y - yDistance,
                                                  nearLeft.z );

        Vector3d frontalNearestLeft = new Vector3d( origin.x - resolutionStripeLength * 0.5,
                                                    origin.y + yDistance,
                                                    nearLeft.z );
        Vector3d frontalNearestRight = new Vector3d( origin.x + resolutionStripeLength * 0.5,
                                                     origin.y + yDistance,
                                                     nearLeft.z );

        transform.transform( backNearestLeft );
        transform.transform( backNearestRight );
        transform.transform( frontalNearestLeft );
        transform.transform( frontalNearestRight );
        ResolutionStripe middleStripe = createResolutionStripe( backNearestLeft,
                                                                backNearestRight,
                                                                frontalNearestRight,
                                                                frontalNearestLeft,
                                                                minimalHeight,
                                                                minScaleResolution,
                                                                minScaleResolution,
                                                                scale );
        if ( middleStripe != null ) {
            resultStripes.add( middleStripe );
        }
        /**
         * For debugging purpaces
         */
        // if ( g2d != null ) {
        // g2d.setColor( Color.GREEN );
        // g2d.drawLine( (int) backNearestRight.x, (int) backNearestRight.y,
        // (int) backNearestLeft.x, (int) backNearestLeft.y );
        // g2d.setColor( Color.RED );
        // g2d.drawLine( (int) frontalNearestRight.x, (int) frontalNearestRight.y,
        // (int) frontalNearestLeft.x, (int) frontalNearestLeft.y );
        // }
        // Find the distances to switch levelofdetail
        /**
         * If the near clipping plane is behind the viewer the resolutionstripes are centered around the viewpoint.
         * <code>
         * FL = farLeft  = UpperLeft
         * FR = FarRight = UpperRight
         * NL = nearLeft = LowerLeft
         * NR = nearRight = LowerRight
         * VP = viewPoint
         * R = Right
         * L = Left
         * F = front
         * B = Back
         * FL__________________FR    .
         *   \                /     /|\
         *    \     _F_      /       |
         *     \  L |VP| R  /     viewDir
         *      \   -B-    /         |
         *       \        /          |
         *       NL------NR
         * </code>
         */
        for ( int stripesCounter = 0; stripesCounter < numberOfStripesNear; ++stripesCounter ) {

            // go back to null point
            invertTransform.transform( backNearestRight );
            invertTransform.transform( backNearestLeft );
            // find the distance from the center of the far from the center of the near
            // clipping plane
            double xLength = Math.abs( ( backNearestRight.x - backNearestLeft.x ) * 0.5 );
            double yLength = xLength / tangensOfHalfAngleOfView;

            // For the frontal perspective ResolutionStripes we need to know the distance from the
            // origin
            double nearFrontalYDistance = origin.y + Math.abs( origin.y - backNearestLeft.y );
            double farFrontalYDistance = nearFrontalYDistance + yLength;

            // create the new Back-ResolutionStripe by adding the length in x and y direction
            // according to the angleOfView. From this resolutionStripe the left, front and right
            // stripes are calculated
            Vector3d tmpBackLowerLeft = new Vector3d( backNearestLeft.x + xLength,
                                                      backNearestLeft.y - yLength,
                                                      backNearestLeft.z );
            Vector3d tmpBackLowerRight = new Vector3d( backNearestRight.x - xLength,
                                                       backNearestRight.y - yLength,
                                                       backNearestRight.z );
            Vector3d tmpBackUpperRight = new Vector3d( backNearestRight.x - xLength,
                                                       backNearestRight.y,
                                                       backNearestRight.z );
            Vector3d tmpBackUpperLeft = new Vector3d( backNearestLeft.x + xLength, backNearestLeft.y, backNearestLeft.z );

            // left
            Vector3d tmpLeftLowerLeft = new Vector3d( tmpBackUpperLeft );
            Vector3d tmpLeftLowerRight = new Vector3d( backNearestLeft );
            Vector3d tmpLeftUpperRight = new Vector3d( backNearestLeft.x, nearFrontalYDistance, backNearestLeft.z );
            Vector3d tmpLeftUpperLeft = new Vector3d( tmpBackUpperLeft.x, nearFrontalYDistance, backNearestLeft.z );

            // front
            Vector3d tmpFrontLowerLeft = new Vector3d( tmpLeftUpperLeft );
            Vector3d tmpFrontLowerRight = new Vector3d( tmpBackLowerRight.x, nearFrontalYDistance, backNearestLeft.z );
            Vector3d tmpFrontUpperRight = new Vector3d( tmpBackLowerRight.x, farFrontalYDistance, backNearestLeft.z );
            Vector3d tmpFrontUpperLeft = new Vector3d( tmpLeftUpperLeft.x, farFrontalYDistance, backNearestLeft.z );

            // right
            Vector3d tmpRightLowerLeft = new Vector3d( backNearestRight );
            Vector3d tmpRightLowerRight = new Vector3d( tmpBackUpperRight );
            Vector3d tmpRightUpperRight = new Vector3d( tmpFrontLowerRight );
            Vector3d tmpRightUpperLeft = new Vector3d( backNearestRight.x, nearFrontalYDistance, backNearestLeft.z );

            // transform the resulting shape back to scene space
            transform.transform( tmpBackLowerLeft );
            transform.transform( tmpBackLowerRight );
            transform.transform( tmpBackUpperRight );
            transform.transform( tmpBackUpperLeft );

            transform.transform( tmpLeftLowerLeft );
            transform.transform( tmpLeftLowerRight );
            transform.transform( tmpLeftUpperRight );
            transform.transform( tmpLeftUpperLeft );

            transform.transform( tmpFrontLowerLeft );
            transform.transform( tmpFrontLowerRight );
            transform.transform( tmpFrontUpperRight );
            transform.transform( tmpFrontUpperLeft );

            transform.transform( tmpRightLowerLeft );
            transform.transform( tmpRightLowerRight );
            transform.transform( tmpRightUpperRight );
            transform.transform( tmpRightUpperLeft );

            double resolution = StripeFactory.calcScaleOfVector( tmpBackLowerLeft, tmpBackLowerRight, imageWidth );

            ResolutionStripe rs = createResolutionStripe( tmpBackLowerLeft,
                                                          tmpBackLowerRight,
                                                          tmpBackUpperRight,
                                                          tmpBackUpperLeft,
                                                          minimalHeight,
                                                          resolution,
                                                          resolution,
                                                          scale );
            if ( rs != null ) {
                resultStripes.add( rs );
            }

            rs = createResolutionStripe( tmpLeftLowerLeft,
                                         tmpLeftLowerRight,
                                         tmpLeftUpperRight,
                                         tmpLeftUpperLeft,
                                         minimalHeight,
                                         resolution,
                                         resolution,
                                         scale );
            if ( rs != null ) {
                resultStripes.add( rs );
            }

            rs = createResolutionStripe( tmpFrontLowerLeft,
                                         tmpFrontLowerRight,
                                         tmpFrontUpperRight,
                                         tmpFrontUpperLeft,
                                         minimalHeight,
                                         resolution,
                                         resolution,
                                         scale );
            if ( rs != null ) {
                resultStripes.add( rs );
            }

            rs = createResolutionStripe( tmpRightLowerLeft,
                                         tmpRightLowerRight,
                                         tmpRightUpperRight,
                                         tmpRightUpperLeft,
                                         minimalHeight,
                                         resolution,
                                         resolution,
                                         scale );
            if ( rs != null ) {
                resultStripes.add( rs );
            }
            /**
             * For debugging purpaces
             */
            if ( g2d != null ) {
                g2d.setColor( Color.GREEN );
                g2d.drawLine( (int) tmpBackLowerLeft.x,
                              (int) tmpBackLowerLeft.y,
                              (int) tmpBackLowerRight.x,
                              (int) tmpBackLowerRight.y );
                g2d.drawLine( (int) tmpBackLowerRight.x,
                              (int) tmpBackLowerRight.y,
                              (int) tmpBackUpperRight.x,
                              (int) tmpBackUpperRight.y );
                g2d.drawLine( (int) tmpBackUpperRight.x,
                              (int) tmpBackUpperRight.y,
                              (int) tmpBackUpperLeft.x,
                              (int) tmpBackUpperLeft.y );
                g2d.drawLine( (int) tmpBackUpperLeft.x,
                              (int) tmpBackUpperLeft.y,
                              (int) tmpBackLowerLeft.x,
                              (int) tmpBackLowerLeft.y );

                g2d.setColor( Color.RED );
                g2d.drawLine( (int) tmpLeftLowerLeft.x,
                              (int) tmpLeftLowerLeft.y,
                              (int) tmpLeftLowerRight.x,
                              (int) tmpLeftLowerRight.y );
                g2d.drawLine( (int) tmpLeftLowerRight.x,
                              (int) tmpLeftLowerRight.y,
                              (int) tmpLeftUpperRight.x,
                              (int) tmpLeftUpperRight.y );
                g2d.drawLine( (int) tmpLeftUpperRight.x,
                              (int) tmpLeftUpperRight.y,
                              (int) tmpLeftUpperLeft.x,
                              (int) tmpLeftUpperLeft.y );
                g2d.drawLine( (int) tmpLeftUpperLeft.x,
                              (int) tmpLeftUpperLeft.y,
                              (int) tmpLeftLowerLeft.x,
                              (int) tmpLeftLowerLeft.y );

                g2d.setColor( Color.BLUE );
                g2d.drawLine( (int) tmpFrontLowerLeft.x,
                              (int) tmpFrontLowerLeft.y,
                              (int) tmpFrontLowerRight.x,
                              (int) tmpFrontLowerRight.y );
                g2d.drawLine( (int) tmpFrontLowerRight.x,
                              (int) tmpFrontLowerRight.y,
                              (int) tmpFrontUpperRight.x,
                              (int) tmpFrontUpperRight.y );
                g2d.drawLine( (int) tmpFrontUpperRight.x,
                              (int) tmpFrontUpperRight.y,
                              (int) tmpFrontUpperLeft.x,
                              (int) tmpFrontUpperLeft.y );
                g2d.drawLine( (int) tmpFrontUpperLeft.x,
                              (int) tmpFrontUpperLeft.y,
                              (int) tmpFrontLowerLeft.x,
                              (int) tmpFrontLowerLeft.y );

                g2d.setColor( Color.YELLOW );
                g2d.drawLine( (int) tmpRightLowerLeft.x,
                              (int) tmpRightLowerLeft.y,
                              (int) tmpRightLowerRight.x,
                              (int) tmpRightLowerRight.y );
                g2d.drawLine( (int) tmpRightLowerRight.x,
                              (int) tmpRightLowerRight.y,
                              (int) tmpRightUpperRight.x,
                              (int) tmpRightUpperRight.y );
                g2d.drawLine( (int) tmpRightUpperRight.x,
                              (int) tmpRightUpperRight.y,
                              (int) tmpRightUpperLeft.x,
                              (int) tmpRightUpperLeft.y );
                g2d.drawLine( (int) tmpRightUpperLeft.x,
                              (int) tmpRightUpperLeft.y,
                              (int) tmpRightLowerLeft.x,
                              (int) tmpRightLowerLeft.y );
            }
            backNearestRight = new Vector3d( tmpBackLowerRight );
            backNearestLeft = new Vector3d( tmpBackLowerLeft );
        }

        // Find intersection of the line connecting lowerLeft and upperleft of the footprint
        invertTransform.transform( nearLeft );
        invertTransform.transform( farLeft );
        invertTransform.transform( nearRight );
        invertTransform.transform( farRight );
        invertTransform.transform( backNearestLeft );
        invertTransform.transform( backNearestRight );

        double nearFrontalYDistance = origin.y + Math.abs( origin.y - backNearestLeft.y );

        /*
         * We have the circular resolutionstripes, now the last back, left and right stripes (which are bounded by the
         * footprint) have to be calculated, which can be done by Finding intersection between a horizontalLine
         * (constant gradient) and the footprint.
         *
         * A line is defined as yValue = gradient*xValue + offset therefor intersection between those two lines as
         * follows: offsetTwo = gradientOne*xValueOne + offsetOne => xValueOne = (offsetOne - offsetTwo) / gradientOne;
         */
        double gradientLeft = ( nearLeft.y - farLeft.y ) / ( nearLeft.x - farLeft.x );
        double offsetLeft = nearLeft.y - ( gradientLeft * nearLeft.x );

        double gradientRight = ( nearRight.y - farRight.y ) / ( nearRight.x - farRight.x );
        double offsetRight = nearRight.y - gradientRight * nearRight.x;

        double xIntersectionLeft = ( backNearestLeft.y - offsetLeft ) / gradientLeft;
        double xIntersectionRight = ( backNearestRight.y - offsetRight ) / gradientRight;

        // Back
        Vector3d tmpBackLowerLeft = new Vector3d( nearLeft );
        Vector3d tmpBackLowerRight = new Vector3d( nearRight );
        Vector3d tmpBackUpperRight = new Vector3d( xIntersectionRight, backNearestRight.y, backNearestRight.z );
        Vector3d tmpBackUpperLeft = new Vector3d( xIntersectionLeft, backNearestLeft.y, backNearestLeft.z );

        // left
        xIntersectionLeft = ( nearFrontalYDistance - offsetLeft ) / gradientLeft;
        Vector3d tmpLeftLowerLeft = new Vector3d( tmpBackUpperLeft );
        Vector3d tmpLeftLowerRight = new Vector3d( backNearestLeft );
        Vector3d tmpLeftUpperRight = new Vector3d( backNearestLeft.x, nearFrontalYDistance, backNearestLeft.z );
        Vector3d tmpLeftUpperLeft = new Vector3d( xIntersectionLeft, nearFrontalYDistance, backNearestLeft.z );

        // right
        xIntersectionRight = ( nearFrontalYDistance - offsetRight ) / gradientRight;
        Vector3d tmpRightLowerLeft = new Vector3d( backNearestRight );
        Vector3d tmpRightLowerRight = new Vector3d( tmpBackUpperRight );
        Vector3d tmpRightUpperRight = new Vector3d( xIntersectionRight, nearFrontalYDistance, backNearestLeft.z );
        Vector3d tmpRightUpperLeft = new Vector3d( backNearestRight.x, nearFrontalYDistance, backNearestLeft.z );

        // back
        transform.transform( tmpBackLowerLeft );
        transform.transform( tmpBackLowerRight );
        transform.transform( tmpBackUpperRight );
        transform.transform( tmpBackUpperLeft );

        // left
        transform.transform( tmpLeftLowerLeft );
        transform.transform( tmpLeftLowerRight );
        transform.transform( tmpLeftUpperRight );
        transform.transform( tmpLeftUpperLeft );

        // right
        transform.transform( tmpRightLowerLeft );
        transform.transform( tmpRightLowerRight );
        transform.transform( tmpRightUpperRight );
        transform.transform( tmpRightUpperLeft );

        double minResolution = StripeFactory.calcScaleOfVector( tmpBackUpperLeft, tmpBackUpperRight, imageWidth );
        double maxResolution = StripeFactory.calcScaleOfVector( tmpBackLowerLeft, tmpBackLowerRight, imageWidth );
        resultStripes.add( createResolutionStripe( tmpBackLowerLeft,
                                                   tmpBackLowerRight,
                                                   tmpBackUpperRight,
                                                   tmpBackUpperLeft,
                                                   minimalHeight,
                                                   maxResolution,
                                                   minResolution,
                                                   scale ) );
        resultStripes.add( createResolutionStripe( tmpLeftLowerLeft,
                                                   tmpLeftLowerRight,
                                                   tmpLeftUpperRight,
                                                   tmpLeftUpperLeft,
                                                   minimalHeight,
                                                   maxResolution,
                                                   minResolution,
                                                   scale ) );
        resultStripes.add( createResolutionStripe( tmpRightLowerLeft,
                                                   tmpRightLowerRight,
                                                   tmpRightUpperRight,
                                                   tmpRightUpperLeft,
                                                   minimalHeight,
                                                   maxResolution,
                                                   minResolution,
                                                   scale ) );

        /**
         * For debugging purpaces
         */
        if ( g2d != null ) {
            g2d.setColor( Color.GREEN );
            g2d.drawLine( (int) tmpBackLowerLeft.x,
                          (int) tmpBackLowerLeft.y,
                          (int) tmpBackLowerRight.x,
                          (int) tmpBackLowerRight.y );
            g2d.drawLine( (int) tmpBackLowerRight.x,
                          (int) tmpBackLowerRight.y,
                          (int) tmpBackUpperRight.x,
                          (int) tmpBackUpperRight.y );
            g2d.drawLine( (int) tmpBackUpperRight.x,
                          (int) tmpBackUpperRight.y,
                          (int) tmpBackUpperLeft.x,
                          (int) tmpBackUpperLeft.y );
            g2d.drawLine( (int) tmpBackUpperLeft.x,
                          (int) tmpBackUpperLeft.y,
                          (int) tmpBackLowerLeft.x,
                          (int) tmpBackLowerLeft.y );

            g2d.setColor( Color.BLUE );
            g2d.drawLine( (int) tmpLeftLowerLeft.x,
                          (int) tmpLeftLowerLeft.y,
                          (int) tmpLeftLowerRight.x,
                          (int) tmpLeftLowerRight.y );
            g2d.drawString( "lll", (int) tmpLeftLowerLeft.x, (int) tmpLeftLowerLeft.y );
            g2d.drawLine( (int) tmpLeftLowerRight.x,
                          (int) tmpLeftLowerRight.y,
                          (int) tmpLeftUpperRight.x,
                          (int) tmpLeftUpperRight.y );
            g2d.drawString( "llr", (int) tmpLeftLowerRight.x, (int) tmpLeftLowerRight.y );
            g2d.drawLine( (int) tmpLeftUpperRight.x,
                          (int) tmpLeftUpperRight.y,
                          (int) tmpLeftUpperLeft.x,
                          (int) tmpLeftUpperLeft.y );
            g2d.drawString( "lur", (int) tmpLeftUpperRight.x, (int) tmpLeftUpperRight.y );
            g2d.drawLine( (int) tmpLeftUpperLeft.x,
                          (int) tmpLeftUpperLeft.y,
                          (int) tmpLeftLowerLeft.x,
                          (int) tmpLeftLowerLeft.y );

            g2d.setColor( Color.YELLOW );
            g2d.drawLine( (int) tmpRightLowerLeft.x,
                          (int) tmpRightLowerLeft.y,
                          (int) tmpRightLowerRight.x,
                          (int) tmpRightLowerRight.y );
            g2d.drawLine( (int) tmpRightLowerRight.x,
                          (int) tmpRightLowerRight.y,
                          (int) tmpRightUpperRight.x,
                          (int) tmpRightUpperRight.y );
            g2d.drawLine( (int) tmpRightUpperRight.x,
                          (int) tmpRightUpperRight.y,
                          (int) tmpRightUpperLeft.x,
                          (int) tmpRightUpperLeft.y );
            g2d.drawLine( (int) tmpRightUpperLeft.x,
                          (int) tmpRightUpperLeft.y,
                          (int) tmpRightLowerLeft.x,
                          (int) tmpRightLowerLeft.y );
        }

        // What is left, are the frontal Stripes which can be more than one (whith steep pitch).
        Vector3d tmpFrontLowerLeft = new Vector3d( tmpLeftUpperLeft );
        Vector3d tmpFrontLowerRight = new Vector3d( tmpRightUpperRight );
        transform.transform( nearLeft );
        transform.transform( farLeft );
        transform.transform( nearRight );
        transform.transform( farRight );
        double frontalScale = StripeFactory.calcScaleOfVector( tmpFrontLowerLeft, tmpFrontLowerRight, imageWidth );
        if ( Math.abs( ( ( Math.abs( 1.0 / gradientLeft ) ) + Math.toRadians( 90 ) ) % Math.toRadians( 180 ) ) > 0.0001 ) {
            double footPrintAngle = Math.atan( 1.0 / gradientLeft );
            resultStripes.addAll( createStripesForFrontalPerspective( invertTransform,
                                                                      transform,
                                                                      farRight,
                                                                      farLeft,
                                                                      tmpFrontLowerRight,
                                                                      tmpFrontLowerLeft,
                                                                      imageWidth,
                                                                      minimalHeight,
                                                                      footPrintAngle,
                                                                      frontalScale,
                                                                      footprintFarResolution,
                                                                      g2d,
                                                                      scale ) );
        } else {
            // Just one remaining ResolutionStripe, because only a square is left over (could be
            // rounding error).
            resultStripes.add( createResolutionStripe( tmpFrontLowerLeft,
                                                       tmpFrontLowerRight,
                                                       farRight,
                                                       farLeft,
                                                       imageWidth,
                                                       minimalHeight,
                                                       scale ) );
        }
        return resultStripes;
    }

    /**
     * Creates a ResolutionStripe fromt the given Vectors. The variable names are just indicators for the order in which
     * the Position are added to the polygon which is min, (min+width), max, (min+height).
     *
     * @param lowerLeft
     *            the min-value of the surface
     * @param lowerRight
     *            the (min + width)-value of the surface
     * @param upperRight
     *            the max-value of the surface
     * @param upperLeft
     *            the (min + height)-value of the surface
     * @param imageWidth
     *            needed to calculate the resolution
     * @param minimalHeight
     *            to put the z-value to if it does not exist
     * @return a brand new ResolutionStripe or <code>null</code> if an exception occurred.
     */
    private ResolutionStripe createResolutionStripe( Vector3d lowerLeft, Vector3d lowerRight, Vector3d upperRight,
                                                     Vector3d upperLeft, double imageWidth, double minimalHeight,
                                                     double scale ) {
        double maxResolution = StripeFactory.calcScaleOfVector( upperRight, upperLeft, imageWidth );
        double minResolution = StripeFactory.calcScaleOfVector( lowerRight, lowerLeft, imageWidth );

        return createResolutionStripe( lowerLeft,
                                       lowerRight,
                                       upperRight,
                                       upperLeft,
                                       minimalHeight,
                                       maxResolution,
                                       minResolution,
                                       scale );
    }

    private ResolutionStripe createResolutionStripe( Vector3d lowerLeft, Vector3d lowerRight, Vector3d upperRight,
                                                     Vector3d upperLeft, double minimalHeight, double maxResolution,
                                                     double minResolution, double scale ) {
        Position[] pos = new Position[5];
        pos[0] = GeometryFactory.createPosition( lowerLeft.x, lowerLeft.y, lowerLeft.z );
        pos[1] = GeometryFactory.createPosition( lowerRight.x, lowerRight.y, lowerLeft.z );
        pos[2] = GeometryFactory.createPosition( upperRight.x, upperRight.y, lowerLeft.z );
        pos[3] = GeometryFactory.createPosition( upperLeft.x, upperLeft.y, lowerLeft.z );
        pos[4] = GeometryFactory.createPosition( lowerLeft.x, lowerLeft.y, lowerLeft.z );

        try {
            Surface surf = GeometryFactory.createSurface( pos, null, null, viewPoint.getCrs() );
            if( minResolution < minScaleResolution ){
                LOG.logDebug( "Setting minResolution ("+ minResolution +") to configured minScaleResolution: "+ minScaleResolution );
                minResolution = minScaleResolution;
            }
            if( maxResolution/MapUtils.DEFAULT_PIXEL_SIZE < minScaleResolution ){
                maxResolution = minScaleResolution;
            }
            return new ResolutionStripe( surf, maxResolution, minResolution, minimalHeight, scale );

        } catch ( GeometryException e ) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param bbox
     * @param imageWidth
     * @param minimalHeight
     * @param scale
     *            of the height in the elevationmodel
     * @return a BoundingBox request ResolutionStripe
     */
    public ArrayList<ResolutionStripe> createBBoxResolutionStripe( Envelope bbox, int imageWidth, double minimalHeight,
                                                                   double scale ) {

        Surface resultSurface = null;
        try {
            resultSurface = GeometryFactory.createSurface( bbox, viewPoint.getCrs() );
        } catch ( GeometryException e ) {
            e.printStackTrace();
        }
        Position min = bbox.getMin();
        double zValue = min.getZ();
        if ( min.getCoordinateDimension() == 2 )
            zValue = minimalHeight;

        Vector3d lowerLeftPoint = new Vector3d( min.getX(), min.getY(), zValue );
        Vector3d lowerRightPoint = new Vector3d( min.getX() + ( bbox.getWidth() ), min.getY(), zValue );
        Vector3d upperLeftPoint = new Vector3d( min.getX() , bbox.getMax().getY(), zValue );
        double minResolution = StripeFactory.calcScaleOfVector( lowerLeftPoint, lowerRightPoint, imageWidth );
        double maxResolution = StripeFactory.calcScaleOfVector( lowerLeftPoint, upperLeftPoint, imageWidth );

        if( minResolution > maxResolution ){
            double tmp = minResolution;
            minResolution = maxResolution;
            maxResolution = tmp;
        }


        ArrayList<ResolutionStripe> resultStripe = new ArrayList<ResolutionStripe>();
        resultStripe.add( new ResolutionStripe( resultSurface, maxResolution, minResolution, minimalHeight, scale ) );

        return resultStripe;
    }

    /**
     * Calculates the Scale ( = Resolution* sqrt( 2 ) (== diagonal of pixel)) of a Vector between two points on the
     * Screen given an imagewidth. That is, how much meter is the Scale of one Pixel in an Image given a certain vector.
     *
     * @param a
     *            "from" point
     * @param b
     *            "to" point
     * @param imageWidth
     *            the target imagewidth
     * @return the scale on the screen.
     */
    public static double calcScaleOfVector( Vector3d a, Vector3d b, double imageWidth ) {
        Vector3d line = new Vector3d( a.x - b.x, a.y - b.y, a.z - b.z );
        // how much meter is one pixel
        // scale = the diagonal of one pixel
        return ( line.length() / imageWidth ) * MapUtils.SQRT2;
    }

    /**
     * @param value
     * @return the nearestPowerOfTwo of the given value
     */
    private int nearestPowerOfTwo( double value ) {
        int result = 0;
        int power = 2;
        while ( power <= value ) {
            power = power << 1;
            result++;
        }
        return result;
    }

}
