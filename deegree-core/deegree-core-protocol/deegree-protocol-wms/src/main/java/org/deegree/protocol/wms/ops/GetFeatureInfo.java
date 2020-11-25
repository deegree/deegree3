//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/deegree3/trunk/deegree-services/deegree-services-wms/src/main/java/org/deegree/services/wms/controller/ops/GetFeatureInfo.java $
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

package org.deegree.protocol.wms.ops;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.INVALID_POINT;
import static org.deegree.commons.ows.exception.OWSException.MISSING_PARAMETER_VALUE;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.layer.LayerRef.FROM_NAMES;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.LayerRef;
import org.deegree.rendering.r2d.RenderHelper;
import org.deegree.style.StyleRef;
import org.slf4j.Logger;

/**
 * <code>GetFeatureInfo</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 31400 $, $Date: 2011-08-02 10:11:48 +0200 (Tue, 02 Aug 2011) $
 */
public class GetFeatureInfo extends RequestBase {

    private static final Logger LOG = getLogger( GetFeatureInfo.class );

    private static final GeometryFactory fac = new GeometryFactory();

    private ICRS crs;

    private Envelope bbox;

    private int width;

    private int height;

    private int x;

    private int y;

    private Envelope clickBox;

    private String infoFormat;

    private int featureCount = 1;

    private boolean returnGeometries;

    private HashMap<String, String> parameterMap = new HashMap<String, String>();

    private double scale;

    /**
     * @param map
     * @param version
     * @throws OWSException
     */
    public GetFeatureInfo( Map<String, String> map, Version version ) throws OWSException {
        if ( version.equals( VERSION_111 ) ) {
            parse111( map );
        }
        if ( version.equals( VERSION_130 ) ) {
            parse130( map );
        }
        parameterMap.putAll( map );
        scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, DEFAULT_PIXEL_SIZE );
    }

    public GetFeatureInfo( List<String> layers, int width, int height, int x, int y, Envelope envelope, ICRS crs,
                           int featureCount ) {
        this.layers.addAll( map( layers, FROM_NAMES ) );
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.bbox = envelope;
        this.crs = crs;
        this.featureCount = featureCount;
        scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, DEFAULT_PIXEL_SIZE );
    }

    public GetFeatureInfo( List<LayerRef> layers, List<StyleRef> styles, List<String> queryLayers, int width,
                           int height, int x, int y, Envelope envelope, ICRS crs, int featureCount, String infoFormat,
                           HashMap<String, String> parameterMap, Map<String, List<?>> dimensions ) throws OWSException {
        this.layers.addAll( layers );
        this.styles.addAll( styles );
        cleanUpLayers( queryLayers );
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.bbox = envelope;
        this.crs = crs;
        this.featureCount = featureCount;
        this.infoFormat = infoFormat;
        this.dimensions.putAll( dimensions );
        this.parameterMap.putAll( parameterMap );
        this.scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, DEFAULT_PIXEL_SIZE );
    }

    private void parse111( Map<String, String> map )
                            throws OWSException {
        double[] vals = handleCommon( map );

        String c = map.get( "SRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( "The SRS parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        crs = GetMap.getCRS111( c );

        bbox = fac.createEnvelope( new double[] { vals[0], vals[1] }, new double[] { vals[2], vals[3] }, crs );

        String xs = map.get( "X" );
        if ( xs == null ) {
            throw new OWSException( "The X parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        try {
            x = parseInt( xs );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The value " + xs + " is not valid for X.", INVALID_PARAMETER_VALUE );
        }

        String ys = map.get( "Y" );
        if ( ys == null ) {
            throw new OWSException( "The Y parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        try {
            y = parseInt( ys );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The value " + ys + " is not valid for Y.", INVALID_PARAMETER_VALUE );
        }
    }

    private void parse130( Map<String, String> map )
                            throws OWSException {
        double[] vals = handleCommon( map );

        String requestedCrs = map.get( "CRS" );
        if ( requestedCrs == null || requestedCrs.trim().isEmpty() ) {
            throw new OWSException( "The CRS parameter is missing.", MISSING_PARAMETER_VALUE );
        }

        bbox = GetMap.getCRSAndEnvelope130( requestedCrs, vals );
        crs = bbox.getCoordinateSystem();

        String i = map.get( "I" );
        if ( i == null ) {
            throw new OWSException( "The I parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        try {
            x = parseInt( i );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The value " + i + " is not valid for I.", INVALID_PARAMETER_VALUE );
        }

        String j = map.get( "J" );
        if ( j == null ) {
            throw new OWSException( "The J parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        try {
            y = parseInt( j );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The value " + j + " is not valid for J.", INVALID_PARAMETER_VALUE );
        }

        if ( hasAxisOrderChanged( requestedCrs ) ) {
            int tmpX = x;
            this.x = this.height - this.y;
            this.y = this.width - tmpX;

            int tmpWidth = width;
            this.width = height;
            this.height = tmpWidth;
        }

        if ( x >= width || y >= height || x < 0 || y < 0 ) {
            throw new OWSException( "The I/J parameters do not fit in the image dimensions.", INVALID_POINT );
        }
    }

    // returns the bbox
    private double[] handleCommon( Map<String, String> map )
                            throws OWSException {
        String sld = map.get( "SLD" );
        String sldBody = map.get( "SLD_BODY" );

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( "The BBOX parameter is missing.", MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        if ( vals.length != 4 ) {
            throw new OWSException( box + " is not a valid BBOX.", INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( "MAXX cannot be smaller than MINX.", INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( "MAXY cannot be smaller than MINY.", INVALID_PARAMETER_VALUE );
        }

        String qls = map.get( "QUERY_LAYERS" );
        if ( qls == null || qls.trim().isEmpty() ) {
            throw new OWSException( "The QUERY_LAYERS parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        String ls = map.get( "LAYERS" );
        if ( ( ls == null || ls.trim().isEmpty() ) && sld == null && sldBody == null ) {
            throw new OWSException( "The LAYERS parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        String ss = map.get( "STYLES" );
        if ( ss == null ) {
            LOG.warn( "Mandatory STYLES parameter is missing for GFI request, silently ignoring the protocol breach." );
            ss = "";
        }

        this.layers = ls == null ? new LinkedList<LayerRef>() : new LinkedList<LayerRef>( map( ls.split( "," ),
                                                                                               FROM_NAMES ) );
        if ( layers.size() == 1 && layers.get( 0 ).getName().isEmpty() ) {
            layers.clear();
        }
        LinkedList<String> qlayers = new LinkedList<String>( asList( qls.split( "," ) ) );

        if ( sld == null && sldBody == null ) {
            this.styles = GetMap.handleKVPStyles( ss, layers.size() );
        } else {
            // TODO think about whether STYLES has to be handled here as well
            handleSLD( sld, sldBody );
        }

        cleanUpLayers( qlayers );

        String format = map.get( "INFO_FORMAT" );
        if ( format != null ) {
            infoFormat = format;
        }

        String w = map.get( "WIDTH" );
        if ( w == null ) {
            throw new OWSException( "The WIDTH parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        try {
            width = parseInt( w );
        } catch ( NumberFormatException e ) {
            throw new OWSException( w + " is not a valid WIDTH value.", INVALID_PARAMETER_VALUE );
        }

        String h = map.get( "HEIGHT" );
        if ( h == null ) {
            throw new OWSException( "The HEIGHT parameter is missing.", MISSING_PARAMETER_VALUE );
        }
        try {
            height = parseInt( h );
        } catch ( NumberFormatException e ) {
            throw new OWSException( h + "is not a valid HEIGHT value.", INVALID_PARAMETER_VALUE );
        }

        String fc = map.get( "FEATURE_COUNT" );
        if ( fc != null ) {
            try {
                featureCount = parseInt( fc );
            } catch ( NumberFormatException e ) {
                throw new OWSException( fc + " is not a valid FEATURE_COUNT value.", INVALID_PARAMETER_VALUE );
            }
        }

        returnGeometries = map.get( "GEOMETRIES" ) != null && map.get( "GEOMETRIES" ).equalsIgnoreCase( "true" );

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
    public LinkedList<LayerRef> getQueryLayers() {
        return layers;
    }

    /**
     * @return the live list of styles (this uses the query_layers parameter as source for layer names)
     */
    public LinkedList<StyleRef> getStyles() {
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

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public List<LayerRef> getLayers() {
        return layers;
    }

    /*
     * Styles and Layers must be set before!
     */
    private void cleanUpLayers( List<String> qlayers )
                            throws OWSException {
        ListIterator<LayerRef> lays = this.layers.listIterator();
        ListIterator<StyleRef> stys = this.styles.listIterator();

        while ( lays.hasNext() ) {
            String name = lays.next().getName();
            stys.next();
            if ( !qlayers.contains( name ) ) {
                lays.remove();
                stys.remove();
            }
        }

        if ( layers.isEmpty() ) {
            throw new OWSException( "An invalid combination of LAYERS and QUERY_LAYERS was specified.",
                                    "LayerNotDefined" );
        }
    }

    private boolean hasAxisOrderChanged( String requestedCrs ) {
        if ( !requestedCrs.startsWith( "AUTO2:" ) ) {
            ICRS crsRef = CRSManager.getCRSRef( requestedCrs );
            try {
                return !CRSUtils.isAxisAware( crsRef );
            } catch ( UnknownCRSException e ) {
                // already checked
            }
        }
        return false;
    }

}
