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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * This Class handles a kvp encoded Get3DFeatureInfo-request and stores it's values.
 *
 * @version $Revision$
 * @author <a href="mailto:cordes@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 */
public class Get3DFeatureInfo extends AbstractOGCWebServiceRequest {

    /**
     * the created serial id.
     */
    private static final long serialVersionUID = 4584807898848764192L;

    private static final ILogger LOGGER = LoggerFactory.getLogger( Get3DFeatureInfo.class );

    private GetView getViewRequestCopy = null;

    private Point clickPoint = null;

    private Rectangle queryBox = null;

    private float apexAngle;

    private int radius;

    private float depth;

    private String exceptions = null;

    private String infoFormat;

    private List<String> queryDatasets = null;

    private int featureCount = 0;

    private boolean parent;

    /**
     * creates a <tt>WPVSFeatureInfoRequest</tt> from a <tt>HashMap</tt> that contains the
     * request parameters as key-value-pairs.
     *
     * @param param
     *            <tt>HashMap</tt> containing the request parameters
     * @return an instance of <tt>WPVSFeatureInfoRequest</tt>
     * @throws OGCWebServiceException
     */
    public static Get3DFeatureInfo create( Map<String, String> param )
                            throws OGCWebServiceException {

        // VERSION
        String version = param.get( "VERSION" ); //$NON-NLS-1$
        if ( version == null ) {
            throw new InconsistentRequestException( Messages.getMessage( "WPVS_INVALID_VERSION" ) ); //$NON-NLS-1$
        }

        // ID
        String id = param.get( "ID" ); //$NON-NLS-1$
        if ( id == null ) {
            throw new InconsistentRequestException( Messages.getMessage( "WPVS_MISSING_ID" ) ); //$NON-NLS-1$
        }

        // <view_request_copy>
        GetView getViewRequestCopy = null;
        try {
            getViewRequestCopy = GetView.create( param );
        } catch ( Exception ex ) {
            throw new InconsistentRequestException( Messages.getMessage( "WPVS_EXCEPTION_GETVIEWREQUESTCOPY" ) //$NON-NLS-1$
                                                    + ex.getMessage() );
        }

        // APEXANGLE
        float apexAngle;
        if ( param.get( "AA" ) != null ) {
            try {
                apexAngle = (float) Math.toRadians( Float.parseFloat( param.remove( "AA" ).trim() ) );
            } catch ( NumberFormatException nfe ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_APEXANGLE" ) );
            }
            if ( apexAngle < 0 ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_APEXANGLE_LESS_0" ) );
            }
        } else {
            apexAngle = 0;
        }

        // I, J
        Point clickPoint = null;
        if ( ( param.get( "I" ) != null ) && ( param.get( "J" ) != null ) ) { //$NON-NLS-1$ //$NON-NLS-2$
            try {
                int i = Integer.parseInt( param.remove( "I" ) ); //$NON-NLS-1$
                int j = Integer.parseInt( param.remove( "J" ) ); //$NON-NLS-1$
                clickPoint = new java.awt.Point( i, j );
            } catch ( NumberFormatException nfe ) {
                LOGGER.logError( nfe.getLocalizedMessage(), nfe );
                throw new OGCWebServiceException( "Get3DFeatureInfo", Messages.getMessage( "WPVS_INVALID_POINT" ), //$NON-NLS-1$ //$NON-NLS-2$
                                                  ExceptionCode.INVALID_POINT );
            }
        }

        // QUERYBOX
        Rectangle queryBox = null;
        if ( param.get( "QUERYBOX" ) != null ) {
            String[] tokens = param.remove( "QUERYBOX" ).split( "," ); //$NON-NLS-1$
            if ( tokens.length != 4 ) {
                throw new InconsistentRequestException( Messages.getMessage( "WPVS_INVALID_QUERYBOX" ) ); //$NON-NLS-1$
            }
            int minx;
            int maxx;
            int miny;
            int maxy;
            try {
                minx = Integer.parseInt( tokens[0] );
                maxx = Integer.parseInt( tokens[1] );
                miny = Integer.parseInt( tokens[2] );
                maxy = Integer.parseInt( tokens[3] );
            } catch ( NumberFormatException e ) {
                throw new InconsistentRequestException( Messages.getMessage( "WPVS_ILLEGAL_QUERYBOX" ) //$NON-NLS-1$
                                                        + e.getMessage() );
            }
            if ( minx >= maxx ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_MINX_MAXX" ) ); //$NON-NLS-1$
            }
            if ( miny >= maxy ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_MINY_MAXY" ) ); //$NON-NLS-1$
            }
            queryBox = new Rectangle( minx, maxx, miny, maxy );
        }

        // RADIUS
        int radius = 0;
        if ( param.get( "RADIUS" ) != null ) {
            try {
                radius = Integer.parseInt( param.remove( "RADIUS" ).trim() );
            } catch ( NumberFormatException nfe ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_RADIUS" ) );
            }
            if ( radius < 0 ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_RADIUS_LESS_0" ) );
            }
        }

        // I,J or QUERYBOX?
        if ( clickPoint == null && queryBox == null ) {
            throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_QUERYTYPE" ) ); //$NON-NLS-1$
        }

        // DEPTH
        float depth = 0;
        if ( param.get( "DEPTH" ) != null ) {
            try {
                depth = Float.parseFloat( param.remove( "DEPTH" ).trim() );
            } catch ( NumberFormatException nfe ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_DEPTH" ) );
            }
            if ( depth < 0 ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_DEPTH_LESS_0" ) );
            }
        }

        // EXCEPTIONS (default=application/vnd.ogc.se_xml)
        String exceptions = param.remove( "EXCEPTIONS" ); //$NON-NLS-1$
        if ( exceptions == null ) {
            exceptions = "application/vnd.ogc.se_xml"; //$NON-NLS-1$
        }

        // INFO_FORMAT (mime-type)
        String infoFormat = param.remove( "INFO_FORMAT" ); //$NON-NLS-1$
        if ( infoFormat == null ) {
            infoFormat = "application/vnd.ogc.gml"; //$NON-NLS-1$
        }

        // QUERY_LAYERS
        List<String> queryDatasets = null;
        if ( param.get( "QUERY_DATASETS" ) != null ) {
            StringTokenizer st = new StringTokenizer( param.remove( "QUERY_DATASETS" ), "," ); //$NON-NLS-1$
            queryDatasets = new ArrayList<String>( st.countTokens() );
            while ( st.hasMoreTokens() ) {
                queryDatasets.add( st.nextToken() );
            }
        } else {
            throw new InconsistentRequestException( Messages.getMessage( "WPVS_INVALID_QUERYLAYERS" ) ); //$NON-NLS-1$
        }

        // FEATURE_COUNT
        int featureCount = 1;
        if ( param.get( "FEATURE_COUNT" ) != null ) {
            try {
                featureCount = Integer.parseInt( param.remove( "FEATURE_COUNT" ) );
            } catch ( NumberFormatException nfe ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_FEATURE_COUNT" ) );
            }
            if ( featureCount < 0 ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_FEATURE_COUNT_LESS_0" ) );
            }
        }

        // PARENT
        boolean parent = true;
        if ( param.get( "PARENT" ) != null ) {
            if ( param.get( "PARENT" ).toUpperCase().trim().equals( "FALSE" ) ) {
                parent = false;
            } else if ( !param.remove( "PARENT" ).toUpperCase().trim().equals( "TRUE" ) ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_INVALID_PARENT_NOT_BOOLEAN" ) );
            }
        }

        // VendorSpecificParameter; because all defined parameters has been
        // removed from the model the vendorSpecificParameters are what left
        Map<String, String> vendorSpecificParameter = param;

        return new Get3DFeatureInfo( version, id, vendorSpecificParameter, getViewRequestCopy, clickPoint, queryBox,
                                     apexAngle, radius, depth, infoFormat, exceptions, queryDatasets, parent,
                                     featureCount );

    }

    /**
     * creates a new WPVSFeatureInfoRequest_Impl object
     *
     * @param version
     * @param id
     * @param vendorSpecificParameter
     * @param getViewRequestCopy
     * @param clickPoint
     * @param queryBox
     * @param radius
     * @param depth
     * @param infoFormat
     * @param exceptions
     * @param queryDatasets
     * @param parent
     * @param featureCount
     */
    private Get3DFeatureInfo( String version, String id, Map<String, String> vendorSpecificParameter,
                              GetView getViewRequestCopy, Point clickPoint, Rectangle queryBox, float apexAngle,
                              int radius, float depth, String infoFormat, String exceptions,
                              List<String> queryDatasets, boolean parent, int featureCount ) {

        super( version, id, vendorSpecificParameter );
        this.getViewRequestCopy = getViewRequestCopy;
        this.clickPoint = clickPoint;
        this.queryBox = queryBox;
        this.apexAngle = apexAngle;
        this.radius = radius;
        this.depth = depth;
        this.infoFormat = infoFormat;
        this.exceptions = exceptions;
        this.queryDatasets = queryDatasets;
        this.parent = parent;
        this.featureCount = featureCount;
    }

    public String getServiceName() {
        return "WPVS"; //$NON-NLS-1$
    }

    /**
     * The AA Parameter indicates the apex angle of a request with a cone
     *
     * @return the apex angle
     */
    public float getApexAngle() {
        return apexAngle;
    }

    /**
     * The I and J parameters indicate a point of interest on the map. Used by the request with a
     * line or cone. The origin is set to (0,0) centered in the pixel at the upper left corner; I
     * increases to the right and J increases downward. I and J are returned as java.awt.Point
     * class/datastructure.
     *
     * @return the point of interest
     */
    public Point getClickPoint() {
        return clickPoint;
    }

    /**
     * This optional parameter indicates the depth of a query
     *
     * @return the depth
     */
    public float getDepth() {
        return depth;
    }

    /**
     * The optional EXCEPTIONS parameter states the manner in which errors are to be reported to the
     * client. The default value is application/vnd.ogc.se_xml if this parameter is absent from the
     * request.
     *
     * @return the exception format
     */
    public String getExceptions() {
        return exceptions;
    }

    /**
     * <view request copy> is not a name/value pair like the other parameters. Instead, most of the
     * GetView request parameters that generated the original map are repeated. Two are omitted
     * because Get3DFeatureInfo provides its own values: VERSION and REQUEST. The remainder of the
     * GetView request shall be embedded contiguously in the Get3DFeatureInfo request.
     *
     * @return a copy of the original request
     */
    public GetView getGetViewRequestCopy() {
        return getViewRequestCopy;
    }

    /**
     * The optional INFO_FORMAT indicates what format to use when returning the feature information.
     * Supported values for a Get3DFeatureInfo request on a WPVS instance are listed as MIME types
     * in one or more <Format>elements inside the <Request><FeatureInfo>element of its Capabilities
     * XML. The entire MIME type string in <Format>is used as the value of the INFO_FORMAT
     * parameter. In an HTTP environment, the MIME type shall be set on the returned object using
     * the Content-type entity header.
     * <p>
     * </p>
     * <b>EXAMPLE: </b> <tt> The parameter INFO_FORMAT=application/vnd.ogc.gml
     * requests that the feature information be formatted in Geography Markup
     * Language (GML).</tt>
     *
     * @return the format
     */
    public String getInfoFormat() {
        return infoFormat;
    }

    /**
     * The required QUERY_LAYERS parameter states the map layer(s) from which feature information is
     * desired to be retrieved. Its value is a comma- separated list of one or more map layers that
     * are returned as an array. This parameter shall contain at least one layer name, but may
     * contain fewer layers than the original GetView request.
     * <p>
     * </p>
     * If any layer in this list is not contained in the Capabilities XML of the WPVS, the results
     * are undefined and the WPVS shall produce an exception response.
     *
     * @return the layer names
     */
    public List<String> getQueryDatasets() {
        return queryDatasets;
    }

    /**
     * @return true if a parent is available.
     */
    public boolean getParent() {
        return parent;
    }

    /**
     * @return the number of features.
     */
    public int getFeatureCount() {
        return featureCount;
    }

    /**
     * The parameter QueryBox indicates the rectangle for the Request with a pyramid
     *
     * @return the queryBox
     */
    public Rectangle getQueryBox() {
        return queryBox;
    }

    /**
     * @return the radius of a feature info request.
     */
    public int getRadius() {
        return radius;
    }

}
