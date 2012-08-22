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

package org.deegree.enterprise.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.graphics.charts.ChartConfig;
import org.deegree.graphics.charts.ChartsBuilder;
import org.deegree.graphics.charts.IncorrectFormatException;
import org.deegree.graphics.charts.QueuedMap;
import org.deegree.i18n.Messages;

/**
 * <code>
 * A Servlet that gets an http GET request, with the de.latlon.charts parameters and it generates
 * and returns a BufferedImage based on these parameters. The request should look as follows:
 * http://www.someserver.com/servletname?
 * chart=ChartType&                 ( Mandatory )
 * title=string&
 * legend=boolean&                  ( default = true )
 * width=int&                       ( Mandatory )
 * height=int&                      ( Mandatory )
 * xAxis=string&
 * yAxis=string&
 * tooltips=boolean&                ( default = false )
 * lblType                          Determines in PieChart what the legend and the tool tip to be. You can
 *                                  either show the data key, value or both. Possible values for this parameter are:
 *                                  <i>Key</i>, <i>Value</i> and <i>KeyValue</i> ( default = Key )
 * orientation=direction&           ( default = vertical )
 * format=image_format&             ( Mandatory )
 * value=value                      ( Mandatory )
 * 
 * The keys are the following
 * I) Chart Type: Available chart Types are:
 * 1- Pie
 * 2- Pie3D
 * 3- Bar
 * 4- Bar3D
 * 5- Line
 * 6- Line3D
 * 7- XYBar
 * 8- XYLine
 * 
 * II)  title: Title of the generated image
 * II)  legend: true/false to show/hide the legend of the categories/rows
 * III) width: Width of the generated image
 * IV) height: Height of the generated image
 * V) xAxis Title of the
 * X Axis Title of xAxis
 * VI) yAxis Title of the Y Axis
 * VII) tooltips Show/hide tool tips
 * VIII)orientation vertical/horizontal. the orientation of the chart
 * IX) format format of the output image, ex: image/png and image/jpeg
 * X) type There is actually no parameter called type.
 *      It only indicates the more parameters that carry the actual values for the chart.
 *      They can have different three different formats. but only one format type shall be used
 *      per request. The values can be given as normal key value pairs as follows:
 * 
 *          Format 1: &CDU=34&SPD=36&Gruene=11 - In this format the key has only one value
 *              - This type can only be used together with the chart type: Pie, Pie3D, Bar, Line and Line3D
 * 
 *           Format 2: &CDU=23,25,21,26&SPD=42,23,33,36
 *              - In this format multiple values per keys can be given - Either comma or white spaces can be
 *                  used as separators(white spaces are of course encoded in case of a Http Get request)
 *              - This type can only be used together with the chart type: Bar, Line and ine3D
 * 
 *          Format 3: &CDU=1970,23;1974,44;1978,43&SPD=1970,23;1974,44;1978,43
 *              - In this format the key has multiple value, in which each value is a tuple that contains in
 *                  turn two tokens representing x and y of a cartesian point. This format is
 *                  used to create cartesian de.latlon.charts
 *              - comma and white spacescan be used as separators between tokens, i.e. between x and y,
 *                  while semi colon shall be used as a separator between tupels.
 *              - This type can only be used together with XYLine (and later with XYBar)
 * </code>
 * 
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ChartServlet extends HttpServlet {

    private static final ILogger LOG = LoggerFactory.getLogger( ChartServlet.class );

    private String errorMsgPath = null;

    private ChartConfig configs = null;

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init()
                            throws ServletException {
        errorMsgPath = this.getServletContext().getRealPath( this.getInitParameter( "errorMsg" ) );
        try {
            String filePath = this.getServletContext().getRealPath( this.getInitParameter( "ConfigFile" ) );
            if ( !filePath.startsWith( "file:///" ) ) {
                filePath = "file:///" + filePath;
            }
            configs = new ChartConfig( new URL( filePath ) );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            LOG.logError( Messages.getMessage( "GRA_CHART_SERVLET_FAIL", new Object[] {} ) );
        }
    }

    private static final long serialVersionUID = -374323170888642652L;

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        performAction( request, response );
    }

    /**
     * @param request
     * @param response
     */
    protected void performAction( HttpServletRequest request, HttpServletResponse response ) {

        /*
         * chart=ChartType& title=string& legend=boolean& width=int& height=int& xAxis=string& yAxis=string&
         * tooltips=boolean& orientation=direction& format=image_format& value=value
         */

        Map<String, String> keyedValues = null;
        try {
            keyedValues = initAndValidateParams( request );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            goToErrorPage( response );
            return;
        }

        /**
         * TODO introduce key "SOSURL" to point towards a SOS. In case a SOSURL is provided, the URL will be queried and
         * it will be assumed that a GetObservation request is returned.
         * 
         */
        // probably not required any more

        // for ( String key : keyedValues.keySet() ) {
        // String value = keyedValues.get( key );
        // try {
        // String encoding = Charset.defaultCharset().name();
        // if ( request.getCharacterEncoding() != null ) {
        // encoding = request.getCharacterEncoding();
        // }
        // String dec = URLDecoder.decode( value, encoding );
        // keyedValues.put( key, dec );
        // } catch ( Exception e ) {
        // LOG.logError( e.getLocalizedMessage() );
        // }
        // }

        String format = request.getParameter( "format" );
        String style = null;
        ChartConfig chartConfigs = null;
        if ( ( style = request.getParameter( "style" ) ) != null ) {
            try {
                chartConfigs = new ChartConfig( new URL( style ) );
            } catch ( Exception e ) {
                // Its not a big problem that the chartConfig is null. In this case the default chart config will be
                // used
                LOG.logError( Messages.getMessage( "GRA_CHART_INVALID_URL", style ) );
            }
        }
        try {
            BufferedImage img = buildChart( request.getParameter( "chart" ), request.getParameter( "title" ),
                                            request.getParameter( "legend" ), request.getParameter( "width" ),
                                            request.getParameter( "height" ), request.getParameter( "xAxis" ),
                                            request.getParameter( "yAxis" ), request.getParameter( "tooltips" ),
                                            request.getParameter( "orientation" ), request.getParameter( "lblType" ),
                                            request.getParameter( "format" ), keyedValues, chartConfigs );
            String imgType = null;
            if ( MimeTypeMapper.isKnownImageType( format ) ) {
                int index = format.indexOf( "/" );
                imgType = format.substring( index + 1, format.length() );
            } else {
                throw new IncorrectFormatException( "The given image type is not a supported type" );
            }
            try {
                ImageUtils.saveImage( img, response.getOutputStream(), imgType, 1.0f );
            } catch ( Exception e ) {
                // Nothing. An exception happened here, because the GET request is called multiple times.
                // This exception happens on the second and third time.
            }
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
            goToErrorPage( response );
        }
    }

    /**
     * Init and validate params does what its name say. It extracts the mandatory parameters from the HttpServletRequest
     * class and checks their validity. Then it fills a queued map with all parameters, that are not defined, because
     * these are the candidates to be the parameters for the chart
     * 
     * @param request
     * @return instance of the queued map with
     * @throws IncorrectFormatException
     * 
     */
    @SuppressWarnings("unchecked")
    protected QueuedMap<String, String> initAndValidateParams( HttpServletRequest request )
                            throws IncorrectFormatException {

        QueuedMap<String, String> keyedValues = new QueuedMap<String, String>();

        Map<String, String> param = KVP2Map.toMap( request );

        String chart = param.get( "CHART" );
        String width = param.get( "WIDTH" );
        String height = param.get( "HEIGHT" );
        String format = param.get( "FORMAT" );

        if ( chart == null ) {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_ERROR_NULL_PARAM", "chart" ) );
        }

        if ( width == null ) {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_ERROR_NULL_PARAM", "width" ) );
        }

        if ( height == null ) {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_ERROR_NULL_PARAM", "height" ) );
        }

        if ( format == null ) {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_ERROR_NULL_PARAM", "format" ) );
        } else if ( !MimeTypeMapper.isKnownImageType( format ) ) {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_ERROR_UNSUPPORTED_TYPE" ) );
        }

        Iterator<String> it = request.getParameterMap().keySet().iterator();
        while ( it.hasNext() ) {
            String key = it.next();
            if ( !key.equals( "chart" ) && !key.equals( "title" ) && !key.equals( "legend" ) && !key.equals( "width" )
                 && !key.equals( "height" ) && !key.equals( "xAxis" ) && !key.equals( "yAxis" )
                 && !key.equals( "tooltips" ) && !key.equals( "orientation" ) && !key.equals( "format" )
                 && !key.equals( "style" ) ) {
                keyedValues.put( key, request.getParameter( key ) );
            }
        }
        return keyedValues;
    }

    /**
     * Creates a buffered image that contains an error label. It is used to display an error instead of a chart in case
     * an error happened
     * 
     * @param response
     */
    protected void goToErrorPage( HttpServletResponse response ) {
        if ( errorMsgPath == null ) {
            LOG.logError( Messages.getMessage( "GRA_CHART_MISSING_ERROR_IMAGE", new Object[] {} ) );
            return;
        }
        try {
            BufferedImage img = ImageUtils.loadImage( errorMsgPath );
            ImageUtils.saveImage( img, response.getOutputStream(), "gif", 1.0f );

        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
        }
    }

    /**
     * Takes in all the needed parameters and a queued map containing the values of the chart and returns a
     * BufferedImage with the chart
     * 
     * @param chart
     * @param title
     * @param legend
     * @param width
     * @param height
     * @param xAxis
     * @param yAxis
     * @param tooltips
     * @param orientation
     * @param lblType
     * @param imagetype
     * @param params
     * @param chartConfigs
     *            to configure the output chart, or null to use the default ChartConfig
     * @return BufferedImage containing the chart
     * @throws IncorrectFormatException
     */
    protected BufferedImage buildChart( String chart, String title, String legend, String width, String height,
                                        String xAxis, String yAxis, String tooltips, String orientation,
                                        String lblType, String imagetype, Map<String, String> params,
                                        ChartConfig chartConfigs )
                            throws IncorrectFormatException {
        if ( configs == null ) {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_ERROR_SERVLET_BROKEN", new Object[] {} ) );
        }

        ChartsBuilder builder = null;
        try {
            builder = new ChartsBuilder( configs );
        } catch ( Exception e ) {
            throw new IncorrectFormatException( e.getLocalizedMessage() );
        }
        boolean legend2 = "true".equals( legend );
        int width2 = Integer.parseInt( width );
        int height2 = Integer.parseInt( height );
        boolean tooltips2 = "true".equals( tooltips );
        int orientation2 = "horizontal".equals( orientation ) ? ChartsBuilder.ORIENTATION_HORIZONTAL
                                                             : ChartsBuilder.ORIENTATION_VERTICAL;
        QueuedMap<String, String> map = new QueuedMap<String, String>();
        map.putAll( params );

        final String THREE_D = "3D";
        /*
         * * Format 1: &CDU=34&SPD=36&Gruene=11 - In this format the key has only one value - This type can only be used
         * together with the chart type: Pie, Pie3D, Bar, Line and Line3D
         * 
         * Format 2: &CDU=23,25,21,26&SPD=42,23,33,36 - In this format multiple values per keys can be given - Either
         * comma or white spaces can be used as separators - This type can only be used together with the chart type:
         * Bar, Line and Line3D
         * 
         * Format 3: &CDU=1970 23;1974,44;1978,43&SPD=1970,23;1974,44;1978,43 - In this format the key has multiple
         * value, in which each value is a tuple that contains in turn two tokens representing x and y of a cartesian
         * point. This format is used to create cartesian de.latlon.charts - comma and white spacescan be used as
         * separators between tokens, i.e. between x and y, while semi colon shall be used as a separator between
         * tupels. - This type can only be used together with XYBar and XYBar
         */
        if ( chart.equals( "Pie" ) || chart.equals( "Pie3D" ) ) {
            QueuedMap<String, Double> map2 = new QueuedMap<String, Double>();
            Iterator<String> it = map.keySet().iterator();
            while ( it.hasNext() ) {
                String key = it.next();
                String value = map.get( key );
                try {
                    if ( value != null ) {
                        map2.put( key, Double.parseDouble( value ) );
                    }
                } catch ( Exception e ) {
                    // It's not a problem. There might be parameters, that are not values.
                }
            }
            return builder.createPieChart( title, map2, width2, height2, chart.contains( THREE_D ), legend2, tooltips2,
                                           lblType, imagetype, chartConfigs );
        } else if ( chart.equals( "Line" ) || chart.equals( "Line3D" ) ) {
            return builder.createLineChart( title, map, width2, height2, chart.contains( THREE_D ), legend2, tooltips2,
                                            orientation2, imagetype, xAxis, yAxis, chartConfigs );
        } else if ( chart.equals( "Bar" ) || chart.equals( "Bar3D" ) ) {
            return builder.createBarChart( title, map, width2, height2, chart.contains( THREE_D ), legend2, tooltips2,
                                           orientation2, imagetype, xAxis, yAxis, chartConfigs );
        } else if ( chart.equals( "XYLine" ) ) {
            return builder.createXYLineChart( title, map, width2, height2, legend2, tooltips2, orientation2, imagetype,
                                              xAxis, yAxis, chartConfigs );
        } else {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_UNSUPPORTED_TYPE", chart ) );
        }
    }
}
