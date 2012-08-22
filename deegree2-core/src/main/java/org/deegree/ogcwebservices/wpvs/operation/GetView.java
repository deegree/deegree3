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

package org.deegree.ogcwebservices.wpvs.operation;

import java.awt.Color;
import java.awt.Dimension;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;

/**
 * This Class handles a kvp request from a client and stores it's values.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class GetView extends WPVSRequestBase {

    /**
     *
     */
    private static final long serialVersionUID = 3147456903146907261L;

    private static final ILogger LOG = LoggerFactory.getLogger( GetView.class );

    private final List<String> datasets;

    private double quality;

    /**
     * using deegree's Position
     */
    private final Point3d pointOfInterest;

    private double pitch;

    private double yaw;

    private double roll;

    private double distance;

    private double angleOfView;

    private final boolean transparent;

    private final Dimension imageDimension;

    private final String outputFormat;

    private final Color backgroundColor;

    private final String exceptionFormat;

    private final String elevationModel;

    private final Envelope boundingBox;

    private final CoordinateSystem crs;

    private double farClippingPlane;

    private GetView( String version, String id, List<String> datasets, String elevationModel,
                     double quality, Position pointOfInterest, Envelope bbox, CoordinateSystem crs,
                     double pitch, double yaw, double roll, double distance, double angleOfView,
                     String outputFormat, Color backgroundColor, boolean transparent,
                     Dimension imageDimension, String exceptionFormat, double farClippingPlane,
                     Map<String, String> vendorSpecificParameter ) {
        this(
              version,
              id,
              datasets,
              elevationModel,
              quality,
              new Point3d( pointOfInterest.getX(), pointOfInterest.getY(), pointOfInterest.getZ() ),
              bbox, crs, pitch, yaw, roll, distance, angleOfView, outputFormat, backgroundColor,
              transparent, imageDimension, exceptionFormat, farClippingPlane,
              vendorSpecificParameter );
    }

    /**
     * Trusted constructor. No parameter validity is performed. This is delegated to the factory
     * method createGeMap.
     *
     * TODO the list of pars is too long, should break up into smaller classes, e.g. pars for
     * perspective output, etc.
     *
     * @param version
     * @param id
     * @param datasets
     * @param elevationModel
     * @param quality
     * @param pointOfInterest
     * @param bbox
     * @param crs
     * @param pitch
     * @param yaw
     * @param roll
     * @param distance
     * @param angleOfView
     * @param outputFormat
     * @param backgroundColor
     * @param transparent
     * @param imageDimension
     * @param exceptionFormat
     * @param farClippingPlane
     * @param vendorSpecificParameter
     */
    private GetView( String version, String id, List<String> datasets, String elevationModel,
                     double quality, Point3d pointOfInterest, Envelope bbox, CoordinateSystem crs,
                     double pitch, double yaw, double roll, double distance, double angleOfView,
                     String outputFormat, Color backgroundColor, boolean transparent,
                     Dimension imageDimension, String exceptionFormat, double farClippingPlane,
                     Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
        this.datasets = datasets;
        this.elevationModel = elevationModel;

        this.quality = quality;
        this.pointOfInterest = pointOfInterest;
        this.boundingBox = bbox;
        this.crs = crs;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.distance = distance;
        this.angleOfView = angleOfView;
        this.outputFormat = outputFormat;
        this.backgroundColor = backgroundColor;
        this.transparent = transparent;
        this.imageDimension = imageDimension;
        this.exceptionFormat = exceptionFormat;
        this.farClippingPlane = farClippingPlane;

    }

    /**
     * Factory method to create an instance of GetView from teh parameters in <code>model</code>
     *
     * @param requestParams
     *            a map containing request parameters and values
     * @return a new instance of GetView
     * @throws InconsistentRequestException
     *             if a mandatory parameter is missing
     * @throws InvalidParameterValueException
     *             if a parameter has an illegal value
     */
    public static GetView create( Map<String, String> requestParams )
                            throws InconsistentRequestException, InvalidParameterValueException {

        // TODO throw a proper exception, the InconsistentRequestException doesn't cover all cases

        // not needed anymore
        requestParams.remove( "REQUEST" );

        String id = requestParams.remove( "ID" );

        /*
         * TODO check if this is right WPVSConfiguration configuration = (WPVSConfiguration)
         * model.remove( "CAPABILITIES" ); if ( configuration == null ){ throw new RuntimeException (
         * "Working site: you forgot to add config to model -> " + "see how this is done in wms" ); }
         */

        String version = requestParams.remove( "VERSION" );
        if ( version == null ) {
            throw new InconsistentRequestException( "'VERSION' value must be set" );
        }

        // FORMAT
        String format = requestParams.remove( "OUTPUTFORMAT" );
        if ( format == null ) {
            throw new InconsistentRequestException( "OUTPUTFORMAT value must be set" );
        }
        try {
            format = URLDecoder.decode( format, CharsetUtils.getSystemCharset() );
        } catch ( UnsupportedEncodingException e1 ) {
            LOG.logError( e1.getLocalizedMessage(), e1 );
        }
        if ( !MimeTypeMapper.isKnownImageType( format ) ) {
            throw new InvalidParameterValueException(
                                                      StringTools.concat( 50, format,
                                                                          " is not a valid image/result format" ) );
        }

        // TRANSPARENCY
        boolean transparency = false;
        String tp = requestParams.remove( "TRANSPARENT" );
        if ( tp != null ) {
            transparency = tp.toUpperCase().trim().equals( "TRUE" );
        }

        if ( transparency
             && ( format.equals( "image/jpg" ) || format.equals( "image/jpeg" )
                  || format.equals( "image/bmp" ) || format.equals( "image/tif" ) || format.equals( "image/tiff" ) ) ) {

            throw new InconsistentRequestException(
                                                    StringTools.concat(
                                                                        100,
                                                                        "TRANSPARENCY=true is inconsistent with OUTPUTFORMAT=",
                                                                        format,
                                                                        ".Valid transparent formats are 'image/gif' ",
                                                                        "and 'image/png'." ) );
        }

        // width
        String tmp = requestParams.remove( "WIDTH" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "'WIDTH' value must be set" );
        }
        int width = 0;
        try {
            width = Integer.parseInt( tmp );
        } catch ( NumberFormatException e ) {
            throw new InconsistentRequestException( "WIDTH must be a valid integer number" );
        }

        tmp = requestParams.remove( "HEIGHT" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "'HEIGHT' value must be set" );
        }
        int height = 0;
        try {
            height = Integer.parseInt( tmp );
        } catch ( NumberFormatException e ) {
            throw new InconsistentRequestException( "HEIGHT must be a valid integer number" );
        }

        if ( width < 0 || height < 0 ) {
            throw new InconsistentRequestException( "WIDTH and HEIGHT must be >= 0" );
        }
        Dimension imgDimension = new Dimension( width, height );

        Color bgColor = Color.white;

        tmp = requestParams.remove( "BACKGROUNDCOLOR" );
        if ( tmp != null ) {
            try {
                bgColor = Color.decode( tmp );
            } catch ( NumberFormatException e ) {
                throw new InconsistentRequestException(
                                                        StringTools.concat(
                                                                            100,
                                                                            "The BACKGROUNDCOLOR '",
                                                                            tmp,
                                                                            "' does not denote a valid hexadecimal color." ) );
            }
        }

        String elevModel = requestParams.remove( "ELEVATIONMODEL" );
        /*
         * if ( elevModel == null ) { throw new InconsistentRequestException( "'ELEVATIONMODEL'
         * value must be set" ); }
         */
        if ( elevModel != null ) {
            elevModel = elevModel.trim();
            if ( elevModel.length() == 0 ) {
                throw new InconsistentRequestException(
                                                        "ELEVATIONMODEL cannot contain space characters only or be empty" );
            }
        }

        tmp = requestParams.remove( "AOV" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "'AOV' value must be set" );
        }

        double aov = 0;
        try {
            aov = Math.toRadians( Double.parseDouble( tmp ) );
            /**
             * checking for > 0 || < 90
             */
            if ( ( aov <= 0 ) || ( aov >= 1.5707963265 ) ) {
                throw new InvalidParameterValueException(
                                                          "AOV value must be a number between 0° and 180°" );
            }
        } catch ( NumberFormatException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidParameterValueException( "AOV couldn't parse the aov value" );
        }

        tmp = requestParams.remove( "ROLL" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "'ROLL' value must be set" );
        }
        double roll;
        try {
            /**
             * checking for > 360 && < 360
             */
            roll = Double.parseDouble( tmp ) % 360;
            if ( roll < 0 )
                roll += 360;
            roll = Math.toRadians( roll );
        } catch ( NumberFormatException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidParameterValueException( "ROLL value must be a number" );
        }

        tmp = requestParams.remove( "DISTANCE" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "'DISTANCE' value must be set." );
        }

        double distance;
        String mesg = "DISTANCE must be a number >= 0.";
        try {
            distance = Double.parseDouble( tmp );
            if ( distance < 0 ) {
                throw new InvalidParameterValueException( mesg );
            }
        } catch ( NumberFormatException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidParameterValueException( mesg );
        }

        tmp = requestParams.remove( "PITCH" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "'PITCH' value must be set." );
        }
        double pitch = 0;
        try {
            pitch = Math.toRadians( Double.parseDouble( tmp ) );
            if ( ( pitch < -1.570796327 ) || ( pitch > 1.570796327 ) ) {
                throw new InvalidParameterValueException(
                                                          "PITCH value must be a number between -90° and 90°" );
            }
        } catch ( NumberFormatException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidParameterValueException(
                                                      "PITCH value must be a number between -90° and 90°" );
        }

        tmp = requestParams.remove( "YAW" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "'YAW' value must be set." );
        }
        double yaw;
        try {
            double tmpYaw = Double.parseDouble( tmp ) % 360;
            if ( tmpYaw < 0 )
                tmpYaw += 360;
            // YAW == 270 -> OutOfMem Error
            // if ( tmpYaw > 89.5 && tmpYaw < 90.5 ) {
            // tmpYaw = 91;
            // } else if ( tmpYaw > 269.5 && tmpYaw < 270.5 ) {
            // tmpYaw = 271;
            // }
            // [UT] 06.06.2005 splitter doesn't work fine for 0 (or 360) and 180
            // if ( tmpYaw % 180 == 0 ) {
            // tmpYaw += 0.5;
            // }
            yaw = Math.toRadians( tmpYaw );

        } catch ( NumberFormatException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidParameterValueException( "YAW value must be a number" );
        }

        tmp = requestParams.remove( "POI" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "POI value is missing." );
        }
        mesg = "POI value must denote a number tuple with valid x,y,z values, for example '123.45,678.90,456.123'";

        try {
            tmp = URLDecoder.decode( tmp, CharsetUtils.getSystemCharset() );
        } catch ( UnsupportedEncodingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InconsistentRequestException( e.getLocalizedMessage() );
        }
        String[] xyz = tmp.split( "," );
        if ( xyz.length != 3 ) {
            throw new InvalidParameterValueException( mesg );
        }
        Position poi;
        double[] p = new double[3];
        try {
            p[0] = Double.parseDouble( xyz[0] );
            p[1] = Double.parseDouble( xyz[1] );
            p[2] = Double.parseDouble( xyz[2] );

        } catch ( NumberFormatException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidParameterValueException( mesg );
        }
        poi = GeometryFactory.createPosition( p );

        String crsString = requestParams.remove( "CRS" );
        CoordinateSystem crs = null;
        if ( crsString == null ) {
            throw new InconsistentRequestException( "CRS parameter is missing." );
        }
        try {
            crsString = URLDecoder.decode( crsString, CharsetUtils.getSystemCharset() );
            crs = CRSFactory.create( crsString );
        } catch ( UnsupportedEncodingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        } catch ( UnknownCRSException ucrse ) {
            LOG.logError( ucrse.getLocalizedMessage(), ucrse );
            throw new InvalidParameterValueException( ucrse.getMessage() );
        }

        String datasetsString = requestParams.remove( "DATASETS" );
        if ( datasetsString == null ) {
            throw new InconsistentRequestException( "'DATASETS' value must be set" );
        }

        datasetsString = datasetsString.trim();

        String[] datasets = datasetsString.split( "," );
        if ( "".equals( datasetsString )  || datasets.length == 0 ) {
            throw new InconsistentRequestException(
                                                    "'DATASETS' must contain at least one dataset name, and cannot be empty (i.e. only have spaces)" );
        }
        List<String> datasetList = new ArrayList<String>( datasets.length );
        for ( String dataset : datasets ) {
            datasetList.add( dataset.trim() );
        }

        String boxstring = requestParams.remove( "BOUNDINGBOX" );
        Envelope boundingBox = null;
        if ( boxstring == null ) {
            throw new InconsistentRequestException( "BOUNDINGBOX value must be set" );
        }

        try {
            boxstring = URLDecoder.decode( boxstring, CharsetUtils.getSystemCharset() );
        } catch ( UnsupportedEncodingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InconsistentRequestException(
                                                    StringTools.concat(
                                                                        100,
                                                                        "Cannot decode BOUNDINGBOX: '",
                                                                        boxstring,
                                                                        " using ",
                                                                        CharsetUtils.getSystemCharset() ) );
        }

        String[] tokens = boxstring.split( "," );
        if ( tokens.length != 4 ) {
            throw new InconsistentRequestException(
                                                    "BOUNDINGBOX value must have a value such as xmin,ymin,xmax,ymax" );
        }

        double minx;
        double maxx;
        double miny;
        double maxy;
        try {
            minx = Double.parseDouble( tokens[0] );
            miny = Double.parseDouble( tokens[1] );
            maxx = Double.parseDouble( tokens[2] );
            maxy = Double.parseDouble( tokens[3] );
        } catch ( NumberFormatException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InconsistentRequestException( "BOUNDINGBOX has an illegal value: "
                                                    + e.getMessage() );
        }

        if ( minx >= maxx ) {
            throw new InvalidParameterValueException( "minx must be less than maxx" );
        }

        if ( miny >= maxy ) {
            throw new InvalidParameterValueException( "miny must be less than maxy" );
        }

        boundingBox = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, crs );

        /**
         * Doing some checking of the given request parameters.
         */

        if ( !boundingBox.contains( poi ) ) {
            throw new InconsistentRequestException( "POI (" + poi
                                                    + " )must be inside the Bounding box ("
                                                    + boundingBox + ")" );
        }

        tmp = requestParams.remove( "FARCLIPPINGPLANE" );
        double farClippingPlane = 150000;
        if ( tmp != null ) {
            try {
                farClippingPlane = Double.parseDouble( tmp );
            } catch ( NumberFormatException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
                throw new InvalidParameterValueException( "FarClippingPlane must be a number" );
            }
        }

        tmp = requestParams.remove( "QUALITY" );
        double quality = 1f;
        if ( tmp != null ) {
            try {
                quality = Double.parseDouble( tmp );
            } catch ( NumberFormatException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
                throw new InvalidParameterValueException( "QUALITY must have a value between [0,1]" );
            }
        }

        String exceptions = requestParams.remove( "EXCEPTIONFORMAT" );
        if ( exceptions == null ) {
            exceptions = "XML";
        }

        // Shouldn't this be checked for the right value ???
        tmp = requestParams.remove( "DATETIME" );
        if ( tmp == null ) {
            // when the moon is in the second house,
            // and jupiter aligns with mars.
            // -> dawning of the age of the wpvs
            tmp = "2007-03-21T12:00:00";
        } else {
            try {
                TimeTools.createCalendar( tmp );
            } catch ( NumberFormatException nfe ) {
                throw new InvalidParameterValueException(
                                                          Messages.getMessage( "WPVS_GETVIEW_ILLEGAL_DATETIME", tmp)  );
            }
        }

        // org.deegree.framework.util.TimeTools.createCalendar( tmp );

        requestParams.put( "DATETIME", tmp );

        tmp = requestParams.remove( "SCALE" );
        if ( tmp != null ) {
            try {
                Double.parseDouble( tmp );
                requestParams.put( "SCALE", tmp );
            } catch ( NumberFormatException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
                throw new InvalidParameterValueException( e );
            }
        }

        return new GetView( version, id, datasetList, elevModel, quality, poi, boundingBox, crs,
                            pitch, yaw, roll, distance, aov, format, bgColor, transparency,
                            imgDimension, exceptions, farClippingPlane, requestParams );
    }

    /**
     * @return the requested angleOfView
     */
    public double getAngleOfView() {
        return angleOfView;
    }

    /**
     * @return the requested distance to the poi
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @return the requested dimension of the resultimage
     */
    public Dimension getImageDimension() {
        return imageDimension;
    }

    /**
     * @return the requested pitch (rotation around the x-axis)
     */
    public double getPitch() {
        return pitch;
    }

    /**
     *
     * @return the point of interest as Point3d
     */
    public Point3d getPointOfInterest() {
        return pointOfInterest;
    }

    /**
     * @return the quality of the textures
     */
    public double getQuality() {
        return quality;
    }

    /**
     * @return the requested roll (rotation around the y-axis)
     */
    public double getRoll() {
        return roll;
    }

    /**
     * @return if the resultimage should be transparent
     */
    public boolean isTransparent() {
        return transparent;
    }

    /**
     * @return the requested yaw (rotation around the z-axis)
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * @return the requested datasets (e.g. layers or features etc.)
     */
    public List<String> getDatasets() {
        return datasets;
    }

    /**
     * @return the requested color of the background
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * @return the requested format of thrown exceptions
     */
    public String getExceptionFormat() {
        return exceptionFormat;
    }

    /**
     * @return the mimetype of the resultimage
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @return the boundingbox of the request
     */
    public Envelope getBoundingBox() {
        return boundingBox;
    }

    /**
     * @return the Coordinate System of the request
     */
    public CoordinateSystem getCrs() {
        return crs;
    }

    /**
     * @return the elevationmodel to be used.
     */
    public String getElevationModel() {
        return elevationModel;
    }

    /**
     * @return Returns the farClippingPlane.
     */
    public double getFarClippingPlane() {
        return farClippingPlane;
    }

    /**
     * @param farClippingPlane
     *            another clippingPlane distance.
     */
    public void setFarClippingPlane( double farClippingPlane ) {
        this.farClippingPlane = farClippingPlane;
    }

    /**
     * @param pitch
     *            a new pitch value
     */
    public void setPitch( double pitch ) {
        this.pitch = pitch;
    }

    /**
     * @param distance
     *            An other distance value.
     */
    public void setDistance( double distance ) {
        this.distance = distance;
    }

    /**
     * @param yaw
     *            An other yaw value.
     */
    public void setYaw( double yaw ) {
        this.yaw = yaw;
    }

    /**
     * @param angleOfView
     *            An other angleOfView value.
     */
    public void setAngleOfView( double angleOfView ) {
        this.angleOfView = angleOfView;
    }
}
