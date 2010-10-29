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

package org.deegree.services.wpvs.controller.getview;

import static org.deegree.commons.utils.kvp.KVPUtils.getBoolean;
import static org.deegree.commons.utils.kvp.KVPUtils.getRequired;
import static org.deegree.commons.utils.kvp.KVPUtils.getRequiredDouble;
import static org.deegree.commons.utils.kvp.KVPUtils.getRequiredInt;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.SunInfo;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.services.controller.ows.OWSException;

/**
 * The <code>GetViewKVPAdapter</code> class provides a GetView request chopper.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetViewKVPAdapter {

    private static final double RAD_90 = Math.toRadians( 90 );

    private static final double RAD_360 = Math.toRadians( 360 );

    private static final GregorianCalendar DEFAULT_CAL = new GregorianCalendar( 2009, 2, 21, 12, 0 );

    /**
     * Factory method to create an instance of GetView from teh parameters in <code>model</code>
     *
     * @param requestParams
     *            a map containing request parameters and values
     * @param encoding
     *            of the request
     * @param translationVector
     * @param configuredNearClippingPlane
     * @param configuredFarClippingPlane
     * @return a new instance of GetView
     * @throws OWSException
     *             if a mandatory parameter is missing or if a parameter has an illegal value
     */
    public static GetView create( Map<String, String> requestParams, String encoding, double[] translationVector,
                                  double configuredNearClippingPlane, double configuredFarClippingPlane )
                            throws OWSException {

        String id = requestParams.get( "ID" );
        try {
            String version = KVPUtils.getRequired( requestParams, "VERSION" );

            CRS coordinateSystem = new CRS( KVPUtils.getRequired( requestParams, "CRS" ) );

            Envelope requestedBBox = getBoundingBox( requestParams, coordinateSystem, encoding, translationVector );
            ViewParams viewParams = getViewParams( requestParams, translationVector, configuredNearClippingPlane,
                                                   configuredFarClippingPlane );
            GetViewResponseParameters responseParams = getResponseParams( requestParams );
            GetViewSceneParameters sceneParameters = getSceneParameters( requestParams );

            return new GetView( id, version, coordinateSystem, requestedBBox, viewParams, responseParams,
                                sceneParameters );
        } catch ( InvalidParameterValueException e ) {
            throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE );
        } catch ( MissingParameterException e ) {
            throw new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE );
        }
    }

    private static Envelope getBoundingBox( Map<String, String> requestParams, CRS coordinateSystem, String encoding,
                                            double[] translationVector )
                            throws OWSException {
        String boxstring = getRequired( requestParams, "BOUNDINGBOX" );
        try {
            boxstring = URLDecoder.decode( boxstring, encoding );
        } catch ( UnsupportedEncodingException e ) {
            throw new OWSException( "Cannot decode BOUNDINGBOX: ' " + boxstring + " using " + encoding,
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        String[] tokens = boxstring.split( "," );
        if ( tokens.length != 4 ) {
            throw new OWSException( "BOUNDINGBOX value must have a value such as xmin,ymin,xmax,ymax",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        double minx;
        double maxx;
        double miny;
        double maxy;
        try {
            minx = Double.parseDouble( tokens[0] ) + translationVector[0];
            miny = Double.parseDouble( tokens[1] ) + translationVector[1];
            maxx = Double.parseDouble( tokens[2] ) + translationVector[0];
            maxy = Double.parseDouble( tokens[3] ) + translationVector[1];
        } catch ( NumberFormatException e ) {
            throw new OWSException( "BOUNDINGBOX has an illegal value: " + e.getMessage(),
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( minx >= maxx ) {
            throw new OWSException( "minx must be less than maxx", OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( miny >= maxy ) {
            throw new OWSException( "miny must be less than maxy", OWSException.INVALID_PARAMETER_VALUE );
        }

        return new GeometryFactory().createEnvelope( minx, miny, maxx, maxy, coordinateSystem );

    }

    private static ViewParams getViewParams( Map<String, String> requestParams, double[] translationVector,
                                             double configuredNearClippingPlane, double configuredFarClippingPlane )
                            throws OWSException {
        // width
        int width = getRequiredInt( requestParams, "WIDTH" );
        int height = getRequiredInt( requestParams, "HEIGHT" );

        if ( width < 0 || height < 0 ) {
            throw new OWSException( "WIDTH and HEIGHT must be >= 0", OWSException.INVALID_PARAMETER_VALUE );
        }

        double angleOfView = getRequiredDouble( requestParams, "AOV" );
        /**
         * checking for > 0 || < 180
         */
        if ( ( angleOfView <= 0 ) || ( angleOfView >= 180 ) ) {
            throw new OWSException( "AOV value must be a number between 0° and 180°",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        /**
         * checking for > 360 && < 360
         */
        double roll = Math.toRadians( getRequiredDouble( requestParams, "ROLL" ) ) % RAD_360;
        if ( roll < 0 ) {
            roll += RAD_360;
        }

        double distance = getRequiredDouble( requestParams, "DISTANCE" );
        if ( distance < 0 ) {
            throw new OWSException( "DISTANCE must be a number >= 0.", OWSException.INVALID_PARAMETER_VALUE );
        }

        double pitch = Math.toRadians( getRequiredDouble( requestParams, "PITCH" ) );
        if ( ( pitch < -RAD_90 ) || ( pitch > RAD_90 ) ) {
            throw new OWSException( "PITCH value must be a number between -90° and 90°",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        double yaw = Math.toRadians( getRequiredDouble( requestParams, "YAW" ) ) % RAD_360;
        if ( yaw < 0 ) {
            yaw += RAD_360;
        }

        String poi = KVPUtils.getRequired( requestParams, "POI" );
        double[] pointOfInterest = ArrayUtils.splitAsDoubles( poi, "," );

        if ( pointOfInterest.length != 3 ) {
            throw new OWSException(
                                    "POI value must denote a number tuple with valid x,y,z values, for example '123.45,678.90,456.123'",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        pointOfInterest[0] += translationVector[0];
        pointOfInterest[1] += translationVector[1];

        double farClippingPlane = KVPUtils.getDefaultDouble( requestParams, "FARCLIPPINGPLANE",
                                                             configuredFarClippingPlane );

        ViewFrustum vf = new ViewFrustum( pitch, yaw, roll, distance, new Point3d( pointOfInterest[0],
                                                                                   pointOfInterest[1],
                                                                                   pointOfInterest[2] ), angleOfView,
                                          width / (double) height, configuredNearClippingPlane, farClippingPlane );

        return new ViewParams( vf, width, height );
    }

    private static GetViewResponseParameters getResponseParams( Map<String, String> requestParams )
                            throws OWSException {
        // TRANSPARENCY
        boolean transparency = getBoolean( requestParams, "TRANSPARENT", false );

        // FORMAT
        String format = getRequired( requestParams, "OUTPUTFORMAT" );

        if ( transparency
             && ( "image/jpg".equals( format ) || "image/jpeg".equals( format ) || "image/bmp".equals( format )
                  || "image/tif".equals( format ) || "image/tiff".equals( format ) ) ) {
            throw new OWSException( "TRANSPARENCY=true is inconsistent with OUTPUTFORMAT=" + format
                                    + ".Valid transparent formats are 'image/gif' " + "and 'image/png'.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        double quality = KVPUtils.getDefaultDouble( requestParams, "QUALITY", 1 );

        String exceptionFormat = requestParams.get( "EXCEPTIONFORMAT" );
        if ( exceptionFormat == null ) {
            exceptionFormat = "INIMAGE";
        }

        return new GetViewResponseParameters( transparency, format, quality, exceptionFormat );
    }

    private static GetViewSceneParameters getSceneParameters( Map<String, String> requestParams )
                            throws OWSException {
        String elevationModel = requestParams.get( "ELEVATIONMODEL" );
        if ( elevationModel != null ) {
            elevationModel = elevationModel.trim();
            if ( elevationModel.split( "," ).length > 1 ) {
                throw new OWSException( "Only one ELEVATIONMODEL may be requested.",
                                        OWSException.INVALID_PARAMETER_VALUE );
            }
        }

        float scale = (float) KVPUtils.getDefaultDouble( requestParams, "SCALE", 1 );

        Color bgColor = new Color( 0.1f, 0.2f, 0.8f );
        String tmp = KVPUtils.getDefault( requestParams, "BACKGROUNDCOLOR", "" + Color.white.getRGB() );
        try {
            bgColor = Color.decode( tmp );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The BACKGROUNDCOLOR '" + tmp + "' does not denote a valid hexadecimal color.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        String backgroundImage = requestParams.get( "BACKGROUND" );

        String datasetsString = requestParams.get( "DATASETS" );
        List<String> datasets = new LinkedList<String>();
        if ( datasetsString != null ) {
            String[] ds = datasetsString.split( "," );
            for ( String dataset : ds ) {
                datasets.add( dataset.trim() );
            }
        }

        String date = requestParams.remove( "DATETIME" );

        GregorianCalendar cal = DEFAULT_CAL;
        if ( date != null ) {
            try {
                Date requestedDate = DateUtils.parseISO8601Date( date );
                cal = new GregorianCalendar();
                cal.setTime( requestedDate );
            } catch ( ParseException e ) {
                throw new OWSException(
                                        "Requested DATETIME: "
                                                                + date
                                                                + ", could not be parsed please specify it in ISO8601 (YYYY-MM-DDTHH:MM:SS), or leave blank to use the servers default ('2009-03-21T12:00:00')",
                                        OWSException.INVALID_PARAMETER_VALUE );
            }
        }

        SunInfo pos = new SunInfo( cal );

        return new GetViewSceneParameters( scale, elevationModel, datasets, bgColor, backgroundImage, date, pos );
    }
}
