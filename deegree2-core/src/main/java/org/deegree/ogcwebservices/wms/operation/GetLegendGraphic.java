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
package org.deegree.ogcwebservices.wms.operation;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.InvalidFormatException;

/**
 *
 * @author Katharina Lupp <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */
public class GetLegendGraphic extends WMSRequestBase {

    private static final long serialVersionUID = -3632596487434212256L;

    private String rule = null;

    private String sLD_Body = null;

    private String featureType = null;

    private String format = null;

    private String layer = null;

    private URL sLD = null;

    private String style = null;

    private double scale = 0;

    private int width = 0;

    private int height = 0;

    private String exceptions = null;

    /**
     * @param model
     *            key-value-pair representation of the request
     * @return an instance
     * @throws InconsistentRequestException
     */
    public static GetLegendGraphic create( Map<String, String> model )
                            throws InconsistentRequestException {
        // version
        String version = model.remove( "VERSION" );

        if ( version == null ) {
            throw new InconsistentRequestException( "Parameter VERSION must be set." );
        }
        if ( version.compareTo( "1.1.1" ) < 0 ) {
            throw new InconsistentRequestException( "Version must be >= 1.1.1." );
        }

        // format
        String format = model.remove( "FORMAT" );
        if ( format == null ) {
            throw new InconsistentRequestException( "Parameter FORMAT must be set." );
        }
        if ( !MimeTypeMapper.isKnownImageType( format ) ) {
            throw new InvalidFormatException( format + " is not a valid image/result format" );
        }

        // layer
        String layer = model.remove( "LAYER" );
        if ( layer == null ) {
            throw new InconsistentRequestException( "Parameter LAYER must be set." );
        }

        // style
        String style = model.remove( "STYLE" );
        if ( style == null || style.equals( "" ) || "DEFAULT".equalsIgnoreCase( style ) ) {
            style = "default:" + layer;
        }

        // featureType
        String featureType = model.remove( "FEATURETYPE" );

        // rule
        String rule = model.remove( "RULE" );

        // scale
        String tmp = model.remove( "SCALE" );
        if ( tmp != null && rule != null ) {
            throw new InconsistentRequestException(
                                                    "SCALE or RULE can be set in a request but not both" );
        }
        double scale = -1;
        if ( tmp != null ) {
            try {
                scale = Double.parseDouble( tmp );
            } catch ( Exception e ) {
                throw new InconsistentRequestException( "Scale, if set, must be a valid number" );
            }
        }

        // SLD
        tmp = model.remove( "SLD" );
        URL sld = null;
        if ( tmp != null ) {
            try {
                sld = new URL( tmp );
            } catch ( Exception e ) {
                throw new InconsistentRequestException(
                                                        "If SLD parameter is set it must be a valid URL" );
            }
        }

        // SLD_BODY
        String sld_body = model.remove( "SLD_BODY" );
        if ( sld_body != null && sld != null ) {
            throw new InconsistentRequestException(
                                                    "SLD or SLD_BODY can be set in a request but not both" );
        }

        // width
        tmp = model.remove( "WIDTH" );
        if ( tmp == null ) {
            tmp = "20";
        }

        int width = 0;
        try {
            width = Integer.parseInt( tmp );
        } catch ( Exception e ) {
            throw new InconsistentRequestException( "WIDTH must be a valid integer number" );
        }

        // height
        tmp = model.remove( "HEIGHT" );
        if ( tmp == null ) {
            tmp = "20";
        }

        int height = 0;
        try {
            height = Integer.parseInt( tmp );
        } catch ( Exception e ) {
            throw new InconsistentRequestException( "HEIGHT must be a valid integer number" );
        }

        // exceptions
        String exceptions = model.remove( "EXCEPTIONS" );
        if ( exceptions == null ) {
            exceptions = "application/vnd.ogc.se_xml";
        }

        String id = model.remove( "ID" );
        Map<String, String> vendorSpecificParameter = model;

        return create( id, version, layer, style, featureType, rule, scale, sld, sld_body, format,
                       width, height, exceptions, vendorSpecificParameter );
    }

    /**
     * @param id
     *            unique id of the request
     * @param version
     *            version of the target WMS
     * @param layer
     *            name of the layer the style is assigned to
     * @param style
     *            name of the style (optional; if not present -> 'default')
     * @param featureType
     *            name of the feature type a legend element shall be created for --> SLD
     * @param rule
     *            name of the rule a legend element shall be created for --> SLD
     * @param scale
     *            scale for which a rule must be valid --> SLD
     * @param sld
     *            refernce to a SLD document
     * @param sld_body
     *            SLD document
     * @param format
     *            image format of the returned legend element
     * @param width
     * @param height
     * @param exceptions
     *            format of the excpetion if something went wrong
     * @param vendorSpecificParameter
     * @return instance of <tt>GetLegendGraphic</tt>
     */
    public static GetLegendGraphic create( String id, String version, String layer, String style,
                                           String featureType, String rule, double scale, URL sld,
                                           String sld_body, String format, int width, int height,
                                           String exceptions,
                                           Map<String, String> vendorSpecificParameter ) {
        return new GetLegendGraphic( id, version, layer, style, featureType, rule, scale, sld,
                                     sld_body, format, width, height, exceptions,
                                     vendorSpecificParameter );
    }

    /**
     * Creates a new GetLegendGraphic object.
     *
     * @param layer
     * @param style
     * @param featureType
     * @param rule
     * @param scale
     * @param sLD
     * @param sLD_Body
     * @param format
     * @param version
     * @param id
     * @param vendorSpecificParameter
     */
    private GetLegendGraphic( String id, String version, String layer, String style,
                              String featureType, String rule, double scale, URL sLD,
                              String sLD_Body, String format, int width, int height,
                              String exceptions, Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
        setLayer( layer );
        setStyle( style );
        setFeatureType( featureType );
        setRule( rule );
        setScale( scale );
        setSLD( sLD );
        setSLD_Body( sLD_Body );
        setFormat( format );
        this.width = width;
        this.height = height;
        this.exceptions = exceptions;
    }

    /**
     * @return the &lt;Layer&gt;. A Map Server MUST include at least one <Layer> element for each
     *         map layer offered. If desired, data layers MAY be repeated in different categories
     *         when relevant. A Layer element MAY state the Name by which a map of the layer is
     *         requested, MUST give a Title to be used in human-readable menus, and MAY include: a
     *         human-readable Abstract containing further description, available Spatial Reference
     *         Systems (SRS), bounding boxes in Lat/Lon and SRS-specific coordinates indicating the
     *         available geographic coverage, styles in which the layer is available, a URL for more
     *         information about the data, and a hint concerning appropriate map scales for
     *         displaying this layer. Use of the nesting hierarchy is optional.
     */
    public String getLayer() {
        return layer;
    }

    /**
     * sets the
     *
     * @param layer
     *            &lt;Layer&gt;
     */
    public void setLayer( String layer ) {
        this.layer = layer;
    }

    /**
     * @return the &lt;Style&gt;. Named style that can be used for rendering the layer.
     */
    public String getStyle() {
        return style;
    }

    /**
     * sets the
     *
     * @param style
     *            &lt;Style&gt;
     */
    public void setStyle( String style ) {
        this.style = style;
    }

    /**
     * @return the &lt;FeatureType&gt;
     */
    public String getFeatureType() {
        return featureType;
    }

    /**
     * sets the
     *
     * @param featureType
     *            &lt;FeatureType&gt;
     */
    public void setFeatureType( String featureType ) {
        this.featureType = featureType;
    }

    /**
     * @return the &lt;Rule&gt;
     */
    public String getRule() {
        return rule;
    }

    /**
     * sets the
     *
     * @param rule
     *            &lt;Rule&gt;
     */
    public void setRule( String rule ) {
        this.rule = rule;
    }

    /**
     * @return the &lt;Scale&gt;. Comma-seperated min and max scale values of a layer.
     */
    public double getScale() {
        return scale;
    }

    /**
     * Comma-seperated min and max scale values of a layer. sets the
     *
     * @param scale
     *            &lt;Scale&gt;.
     */
    public void setScale( double scale ) {
        this.scale = scale;
    }

    /**
     * @return a reference (URL) to a SLD document
     */
    public URL getSLD() {
        return sLD;
    }

    /**
     * sets a reference (URL) to a SLD document
     *
     * @param sLD
     *            the URL
     */
    public void setSLD( URL sLD ) {
        this.sLD = sLD;
    }

    /**
     * @return the body of a SLD document. If SLD_BODY parameter is set, the SLD parameter isn't set
     *         and vice versa
     */
    public String getSLD_Body() {
        return sLD_Body;
    }

    /**
     * sets the body of a SLD document. If SLD_BODY parameter is set, the SLD parameter isn't set
     * and vice versa
     *
     * @param sLD_Body
     *            the body
     */
    public void setSLD_Body( String sLD_Body ) {
        this.sLD_Body = sLD_Body;
    }

    /**
     * @return the name of the image format the legend graphics shall have
     */
    public String getFormat() {
        return format;
    }

    /**
     * sets the name of the image format the legend graphics shall have
     *
     * @param format
     *            the format string
     */
    public void setFormat( String format ) {
        this.format = format;
    }

    /**
     * This gives a hint for the height of the returned graphic in pixels. Vector-graphics can use
     * this value as a hint for the level of detail to include.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @see GetLegendGraphic#getHeight()
     * @param height
     */
    public void setHeight( int height ) {
        this.height = height;
    }

    /**
     * This gives a hint for the width of the returned graphic in pixels. Vector-graphics can use
     * this value as a hint for the level of detail to include.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @see GetLegendGraphic#getWidth()
     * @param width
     */
    public void setWidth( int width ) {
        this.width = width;
    }

    /**
     * This gives the MIME type of the format in which to return exceptions. Allowed values are the
     * same as for the EXCEPTIONS= parameter of the WMS GetMap request.
     *
     * @return the exception format
     */
    public String getExceptions() {
        return exceptions;
    }

    /**
     * @see GetLegendGraphic#getExceptions()
     * @param exceptions
     */
    public void setExceptions( String exceptions ) {
        this.exceptions = exceptions;
    }

    /**
     * method for creating a OGC WMS 1.1.1 conform legend graphic request
     */
    @Override
    public String getRequestParameter()
                            throws OGCWebServiceException {

        StringBuffer url = new StringBuffer( "SERVICE=WMS&VERSION=" + getVersion() );
        url.append( "&REQUEST=GetLegendGraphic");
        url.append( "&LAYER=" ).append( getLayer() );

        if ( getStyle() != null && getStyle().length() > 0 ) {
            url.append( "&STYLE=" + getStyle() );
        }

        if ( getFeatureType() != null && getFeatureType().length() > 0 ) {
            url.append( "&FEATURETYPE=" + getFeatureType() );
        }

        if ( getSLD() != null ) {
            url.append( "&SLD=" + getSLD().toExternalForm() );
        } else if ( getSLD_Body() != null ) {
            String tmp = null;
            try {
                tmp = URLEncoder.encode( getSLD_Body(), CharsetUtils.getSystemCharset() );
            } catch ( Exception e ) {
                throw new OGCWebServiceException( e.toString() );
            }
            url.append( "&SLD_BODY=" + tmp );
        }

        url.append( "&FORMAT=" + getFormat() );
        url.append( "&WIDTH=" + getWidth() );
        url.append( "&HEIGHT=" + getHeight() );

        if ( ( getExceptions() != null ) && ( getExceptions().length() > 0 ) ) {
            url.append( "&EXCEPTIONS=" + getExceptions() );
        }

        return url.toString();
    }

}
