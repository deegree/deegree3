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

package org.deegree.services.wms.controller.ops;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.services.i18n.Messages.get;
import static org.deegree.services.wms.controller.ops.GetMap.parseDimensionValues;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController111;
import org.deegree.services.wms.controller.WMSController130;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * <code>GetFeatureInfo</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetFeatureInfo {

    private static final Logger LOG = getLogger( GetFeatureInfo.class );

    private static final GeometryFactory fac = new GeometryFactory();

    private ICRS crs;

    private Envelope bbox;

    private LinkedList<Layer> layers = new LinkedList<Layer>();

    private LinkedList<Style> styles = new LinkedList<Style>();

    private int width;

    private int height;

    private int x;

    private int y;

    private Envelope clickBox;

    private String infoFormat;

    private int featureCount = 1;

    private boolean returnGeometries;

    private HashMap<String, List<?>> dimensions;

    private MapService service;

    private HashMap<String, String> parameterMap = new HashMap<String, String>();

    /**
     * @param map
     * @param version
     * @param service
     * @throws OWSException
     */
    public GetFeatureInfo( Map<String, String> map, Version version, MapService service ) throws OWSException {
        this.service = service;
        if ( version.equals( VERSION_111 ) ) {
            parse111( map );
        }
        if ( version.equals( VERSION_130 ) ) {
            parse130( map );
        }
        parameterMap.putAll( map );
    }

    /**
     * @param layers
     * @param styles
     * @param radius
     * @param envelope
     * @param x
     * @param y
     * @param width
     * @param height
     * @param maxFeatures
     */
    public GetFeatureInfo( Collection<Layer> layers, Collection<Style> styles, int radius, Envelope envelope, int x,
                           int y, int width, int height, int maxFeatures ) {
        this.layers.addAll( layers );
        this.styles.addAll( styles );
        this.bbox = envelope;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.featureCount = maxFeatures;
        crs = bbox.getCoordinateSystem();
        calcClickBox( radius );
    }

    private void parse111( Map<String, String> map )
                            throws OWSException {
        double[] vals = handleCommon( map );

        String c = map.get( "SRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "SRS" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        crs = WMSController111.getCRS( c );

        bbox = fac.createEnvelope( new double[] { vals[0], vals[1] }, new double[] { vals[2], vals[3] }, crs );

        String xs = map.get( "X" );
        if ( xs == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "X" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            x = parseInt( xs );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "X", xs ), OWSException.INVALID_PARAMETER_VALUE );
        }

        String ys = map.get( "Y" );
        if ( ys == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "Y" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            y = parseInt( ys );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "Y", ys ), OWSException.INVALID_PARAMETER_VALUE );
        }

        calcClickBox( map );
    }

    private void parse130( Map<String, String> map )
                            throws OWSException {
        double[] vals = handleCommon( map );

        String c = map.get( "CRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "CRS" ), OWSException.MISSING_PARAMETER_VALUE );
        }

        bbox = WMSController130.getCRSAndEnvelope( c, vals );
        crs = bbox.getCoordinateSystem();

        parseIj( map );

        if ( x >= width || y >= height || x < 0 || y < 0 ) {
            throw new OWSException( get( "WMS.INVALID_POINT" ), OWSException.INVALID_POINT );
        }

        calcClickBox( map );
    }

    private void parseIj( Map<String, String> map )
                            throws OWSException {
        String xs = map.get( "I" );
        if ( xs == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "I" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            x = parseInt( xs );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "I", xs ), OWSException.INVALID_PARAMETER_VALUE );
        }

        String ys = map.get( "J" );
        if ( ys == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "J" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            y = parseInt( ys );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "J", ys ), OWSException.INVALID_PARAMETER_VALUE );
        }
    }

    private void calcClickBox( int radius ) {
        double dw = bbox.getSpan0() / width;
        double dh = bbox.getSpan1() / height;
        int r2 = radius / 2;
        r2 = r2 == 0 ? 1 : r2;
        clickBox = fac.createEnvelope( new double[] { bbox.getMin().get0() + ( x - r2 ) * dw,
                                                     bbox.getMax().get1() - ( y + r2 ) * dh },
                                       new double[] { bbox.getMin().get0() + ( x + r2 ) * dw,
                                                     bbox.getMax().get1() - ( y - r2 ) * dh }, crs );
    }

    private void calcClickBox( Map<String, String> map ) {
        int radius = service.getGlobalFeatureInfoRadius();
        // TODO implement all this per layer also if more than one layer is requested
        if ( layers.size() == 1 && service.getExtensions().getFeatureInfoRadius( layers.getFirst().getName() ) != -1 ) {
            radius = service.getExtensions().getFeatureInfoRadius( layers.getFirst().getName() );
        }
        radius = map.get( "RADIUS" ) == null ? radius : parseInt( map.get( "RADIUS" ) );
        calcClickBox( radius );
    }

    // returns the bbox
    private double[] handleCommon( Map<String, String> map )
                            throws OWSException {
        double[] vals = parseBoundingBox( map );

        parseLayersAndStyles( map );

        String format = map.get( "INFO_FORMAT" );
        if ( format != null ) {
            infoFormat = format;
        }

        String w = map.get( "WIDTH" );
        if ( w == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "WIDTH" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            width = parseInt( w );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "WIDTH", w ), OWSException.INVALID_PARAMETER_VALUE );
        }

        String h = map.get( "HEIGHT" );
        if ( h == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "HEIGHT" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            height = parseInt( h );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "HEIGHT", h ), OWSException.INVALID_PARAMETER_VALUE );
        }

        String fc = map.get( "FEATURE_COUNT" );
        if ( fc != null ) {
            try {
                featureCount = parseInt( fc );
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.NOT_A_NUMBER", "FEATURE_COUNT", fc ),
                                        OWSException.INVALID_PARAMETER_VALUE );
            }
        }

        dimensions = parseDimensionValues( map );

        returnGeometries = map.get( "GEOMETRIES" ) != null && map.get( "GEOMETRIES" ).equalsIgnoreCase( "true" );

        return vals;
    }

    private void parseLayersAndStyles( Map<String, String> map )
                            throws OWSException {
        String qls = map.get( "QUERY_LAYERS" );
        if ( qls == null || qls.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "QUERY_LAYERS" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        String ls = map.get( "LAYERS" );
        if ( ls == null || ls.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "LAYERS" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        String ss = map.get( "STYLES" );
        if ( ss == null ) {
            LOG.warn( "Mandatory STYLES parameter is missing for GFI request, silently ignoring the protocol breach." );
            ss = "";
        }

        LinkedList<String> layers = new LinkedList<String>( asList( ls.split( "," ) ) );
        this.layers = GetMap.handleKVPLayers( layers, service );
        this.styles = GetMap.handleKVPStyles( ss, service, this.layers );

        LinkedList<String> qlayers = new LinkedList<String>( asList( qls.split( "," ) ) );
        for ( String l : qlayers ) {
            if ( service.getLayer( l ) == null ) {
                throw new OWSException( get( "WMS.LAYER_NOT_KNOWN", l ), OWSException.LAYER_NOT_DEFINED );
            }
        }
        ListIterator<Layer> lays = this.layers.listIterator();
        ListIterator<Style> stys = this.styles.listIterator();

        while ( lays.hasNext() ) {
            String name = lays.next().getName();
            stys.next();
            if ( !qlayers.contains( name ) ) {
                lays.remove();
                stys.remove();
            }
        }
    }

    private double[] parseBoundingBox( Map<String, String> map )
                            throws OWSException {
        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "BBOX" ), OWSException.MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        if ( vals.length != 4 ) {
            throw new OWSException( get( "WMS.BBOX_WRONG_FORMAT", box ), OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( get( "WMS.MAXX_MINX" ), OWSException.INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( get( "WMS.MAXY_MINY" ), OWSException.INVALID_PARAMETER_VALUE );
        }
        return vals;
    }

    /**
     * @return the envelope where the user clicked
     */
    public Envelope getClickBox() {
        return clickBox;
    }

    /**
     * @return the live list of query layers
     */
    public LinkedList<Layer> getQueryLayers() {
        return layers;
    }

    /**
     * @return the live list of styles (this uses the query_layers parameter as source for layer names)
     */
    public LinkedList<Style> getStyles() {
        return styles;
    }

    /**
     * @return the info format
     */
    public String getInfoFormat() {
        return infoFormat;
    }

    /**
     * @return the max feature count
     */
    public int getFeatureCount() {
        return featureCount;
    }

    /**
     * @return whether geometries shall be returned or not
     */
    public boolean returnGeometries() {
        return returnGeometries;
    }

    /**
     * @return the crs
     */
    public ICRS getCoordinateSystem() {
        return crs;
    }

    /**
     * @param crs
     */
    public void setCoordinateSystem( ICRS crs ) {
        this.crs = crs;
    }

    /**
     * @return the dimensions
     */
    public HashMap<String, List<?>> getDimensions() {
        return dimensions;
    }

    /**
     * @return the original GetMap envelope
     */
    public Envelope getEnvelope() {
        return bbox;
    }

    /**
     * @return the original GetMap width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the original GetMap height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the x click point
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y click point
     */
    public int getY() {
        return y;
    }

    /**
     * @return the original parameter map (might be empty if not constructed via request)
     */
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

}
