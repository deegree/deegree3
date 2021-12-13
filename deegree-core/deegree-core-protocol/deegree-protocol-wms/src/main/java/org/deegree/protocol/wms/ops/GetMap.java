//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import static java.awt.Color.decode;
import static java.awt.Color.white;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.StringUtils.splitEscaped;
import static org.deegree.layer.LayerRef.FROM_NAMES;
import static org.deegree.layer.dims.Dimension.parseTyped;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.rendering.r2d.context.MapOptions.getAntialiasGetter;
import static org.deegree.rendering.r2d.context.MapOptions.getAntialiasSetter;
import static org.deegree.rendering.r2d.context.MapOptions.getInterpolationGetter;
import static org.deegree.rendering.r2d.context.MapOptions.getInterpolationSetter;
import static org.deegree.rendering.r2d.context.MapOptions.getQualityGetter;
import static org.deegree.rendering.r2d.context.MapOptions.getQualitySetter;
import static org.deegree.rendering.r2d.context.MapOptions.Antialias.BOTH;
import static org.deegree.rendering.r2d.context.MapOptions.Interpolation.NEARESTNEIGHBOR;
import static org.deegree.rendering.r2d.context.MapOptions.Quality.NORMAL;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.LayerRef;
import org.deegree.layer.dims.DimensionsLexer;
import org.deegree.layer.dims.DimensionsParser;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.filter.ScaleFunction;
import org.deegree.rendering.r2d.RenderHelper;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.MapOptionsGetter;
import org.deegree.rendering.r2d.context.MapOptions.MapOptionsSetter;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.rendering.r2d.context.MapOptionsMaps;
import org.deegree.style.StyleRef;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetMap extends RequestBase {

    private static final Logger LOG = getLogger( GetMap.class );

    private static final boolean PARSE_LAX = false;

    private static GeometryFactory fac = new GeometryFactory();

    private ICRS crs;

    private Envelope bbox;

    private String format;

    private int width, height;

    private boolean transparent;

    private Color bgcolor = white;

    private double scale;

    private double pixelSize = 0.00028;

    private double resolution;

    private MapOptionsMaps extensions = new MapOptionsMaps();

    private double queryBoxSize = -1;

    private Map<String, String> parameterMap = new HashMap<String, String>();

    private Map<String, String> overriddenParameters;

    /**
     * @param map
     * @param version
     * @param exts
     * @throws OWSException
     */
    public GetMap( Map<String, String> map, Version version, MapOptionsMaps exts, boolean parseStrict )
                    throws OWSException {
        if ( version.equals( VERSION_111 ) ) {
            parse111( map, exts );
        }
        if ( version.equals( VERSION_130 ) ) {
            parse130( map, exts, parseStrict );
        }
        parameterMap.putAll( map );
        try {
            scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, pixelSize );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}' (adapted to pixel size of {}).", scale,
                       pixelSize );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( ReferenceResolvingException e ) {
            LOG.trace( "Stack trace:", e );
            LOG.warn( "The scale of a GetMap request could not be calculated: '{}'.", e.getLocalizedMessage() );
        }
    }

    /**
     * @param layers
     * @param styles
     * @param width
     * @param height
     * @param boundingBox
     * @throws OWSException
     */
    public GetMap( Collection<LayerRef> layers, Collection<StyleRef> styles, int width, int height,
                   Envelope boundingBox, MapOptionsMaps exts ) throws OWSException {
        this.layers.addAll( layers );
        this.styles.addAll( styles );
        this.width = width;
        this.height = height;
        this.bbox = boundingBox;
        this.crs = boundingBox.getCoordinateSystem();
        this.bgcolor = white;
        format = "image/png";
        transparent = false;
        handleVSPs( new HashMap<String, String>(), exts );
        try {
            scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, pixelSize );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}' (adapted to pixel size of {}).", scale,
                       pixelSize );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( ReferenceResolvingException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( e.getLocalizedMessage(), "InvalidParameterValue" );
        }
    }

    public GetMap( List<String> layers, int width, int height, Envelope envelope, ICRS crs, String format,
                   boolean transparent ) {
        this.layers = map( layers, FROM_NAMES );
        this.width = width;
        this.height = height;
        this.bbox = envelope;
        this.crs = crs;
        this.format = format;
        this.transparent = transparent;
    }

    public GetMap( List<LayerRef> layers, List<StyleRef> styles, int width, int height, Envelope envelope, ICRS crs,
                   String format, boolean transparent, Color bgcolor, Map<String, String> parameterMap,
                   Map<String, List<?>> dimensions ) {
        this.layers.addAll( layers );
        this.styles.addAll( styles );
        this.width = width;
        this.height = height;
        this.bbox = envelope;
        this.crs = crs;
        this.format = format;
        this.transparent = transparent;
        this.bgcolor = bgcolor;
        this.parameterMap.putAll( parameterMap );
        this.dimensions.putAll( dimensions );
    }
    
    public GetMap( List<LayerRef> layers, List<StyleRef> styles, int width, int height, Envelope envelope, ICRS crs,
                   String format, boolean transparent, Color bgcolor, Map<String, String> parameterMap,
                   Map<String, List<?>> dimensions, Map<String, String> kvp ) {
        this( layers, styles, width, height, envelope, crs, format, transparent, bgcolor, parameterMap, dimensions );
        handlePixelSize( parameterMap );
    }

    public GetMap( List<String> layers, List<String> styles, int width, int height, Envelope envelope, ICRS crs,
                   String format, boolean transparent, Map<String, String> overriddenParameters ) {
        this( layers, width, height, envelope, crs, format, transparent );
        this.overriddenParameters = overriddenParameters;
        this.styles = map( styles, StyleRef.FROM_NAMES );
    }

    public GetMap( List<Pair<String, String>> layers, int width, int height, Envelope boundingBox, String format,
                   boolean transparent ) throws OWSException {
        for ( Pair<String, String> layer : layers ) {
            this.layers.add( new LayerRef( layer.first ) );
            this.styles.add( layer.second != null ? new StyleRef( layer.second ) : null );
        }
        this.width = width;
        this.height = height;
        this.bbox = boundingBox;
        this.crs = boundingBox.getCoordinateSystem();
        this.bgcolor = white;
        this.format = format;
        this.transparent = transparent;
        try {
            scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, pixelSize );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}' (adapted to pixel size of {}).", scale,
                       pixelSize );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( ReferenceResolvingException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( e.getLocalizedMessage(), "InvalidParameterValue" );
        }
    }

    private void parse111( Map<String, String> map, MapOptionsMaps exts )
                            throws OWSException {
        String c = map.get( "SRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( "The SRS parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        crs = getCRS111( c );

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( "The BBOX parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        // hack to work around ESRI ArcGIS Explorer localized bboxes...
        if ( vals.length == 8 ) {
            String[] ss = box.split( "," );
            vals = new double[] { parseDouble( ss[0] + "." + ss[1] ), parseDouble( ss[2] + "." + ss[3] ),
                                 parseDouble( ss[4] + "." + ss[5] ), parseDouble( ss[6] + "." + ss[7] ) };
        }
        if ( vals.length != 4 ) {
            throw new OWSException( "The bounding box parameter value " + box
                                    + " is in the wrong format (too many values).",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( "The maxx component of the bounding box is smaller than the minx component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( "The maxy component of the bounding box is smaller than the miny component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        bbox = fac.createEnvelope( new double[] { vals[0], vals[1] }, new double[] { vals[2], vals[3] }, crs );

        handleCommon( map, exts, PARSE_LAX );
    }

    static LinkedList<StyleRef> handleKVPStyles( String ss, int numLayers )
                            throws OWSException {
        LinkedList<StyleRef> styles = new LinkedList<StyleRef>();

        // result is a list with 'default' where default styles were requested
        if ( ss.trim().isEmpty() ) {
            for ( int i = 0; i < numLayers; ++i ) {
                styles.add( new StyleRef( "default" ) );
            }
        } else {
            // to work around #split limitations
            if ( ss.startsWith( "," ) ) {
                ss = "default" + ss;
            }
            if ( ss.endsWith( "," ) ) {
                ss = ss + "default";
            }
            String[] styls = ss.split( "," );
            if ( styls.length != numLayers ) {
                throw new OWSException( styls.length + "styles have been specified, and " + numLayers
                                        + " layers (should be equal).", OWSException.INVALID_PARAMETER_VALUE );
            }

            for ( int i = 0; i < numLayers; ++i ) {
                if ( styls[i].isEmpty() || styls[i].equals( "default" ) ) {
                    styles.add( new StyleRef( "default" ) );
                } else {
                    styles.add( new StyleRef( styls[i] ) );
                }
            }
        }

        return styles;
    }

    private void handlePixelSize( Map<String, String> map ) {
        String psize = map.get( "PIXELSIZE" );
        if ( psize != null ) {
            try {
                pixelSize = Double.parseDouble( psize ) / 1000;
            } catch ( NumberFormatException e ) {
                LOG.warn( "The value of PIXELSIZE could not be parsed as a number." );
                LOG.trace( "Stack trace:", e );
            }
        } else {
            String key = "RES";
            String pdpi = map.get( key );

            if ( pdpi == null ) {
                key = "DPI";
                pdpi = map.get( key );
            }
            if ( pdpi == null ) {
                key = "MAP_RESOLUTION";
                pdpi = map.get( key );
            }
            if ( pdpi == null ) {
                for ( String word : splitEscaped( map.get( "FORMAT_OPTIONS" ), ';', 0 ) ) {
                    List<String> keyValue = StringUtils.splitEscaped( word, ':', 2 );

                    if ( "dpi".equalsIgnoreCase( keyValue.get( 0 ) ) ) {
                        key = "FORMAT_OPTIONS=dpi";
                        pdpi = keyValue.size() == 1 ? null : StringUtils.unescape( keyValue.get( 1 ) );
                        break;
                    }
                }
            }
            if ( pdpi == null ) {
                key = "X-DPI";
                pdpi = map.get( key );
            }
            if ( pdpi != null ) {
                try {
                    pixelSize = 0.0254d / Double.parseDouble( pdpi );
                } catch ( Exception e ) {
                    LOG.warn( "The value of {} could not be parsed as a number.", key );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }
    
    private void handleCommon( Map<String, String> map, MapOptionsMaps exts, boolean parseStrict )
                            throws OWSException {
        String ls = map.get( "LAYERS" );
        String sld = map.get( "SLD" );
        String sldBody = map.get( "SLD_BODY" );

        if ( ( ls == null || ls.trim().isEmpty() ) && sld == null && sldBody == null ) {
            throw new OWSException( "The LAYERS parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        layers = ls == null ? new LinkedList<LayerRef>() : CollectionUtils.map( ls.split( "," ), LayerRef.FROM_NAMES );

        if ( layers.size() == 1 && layers.get( 0 ).getName().isEmpty() ) {
            layers.clear();
        }

        String ss = map.get( "STYLES" );
        if ( ss == null && sld == null && sldBody == null ) {
            throw new OWSException( "The STYLES parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        if ( sld == null && sldBody == null ) {
            this.styles = handleKVPStyles( ss, layers.size() );
        } else {
            // TODO think about whether STYLES has to be handled here as well
            handleSLD( sld, sldBody );
        }

        handlePixelSize( map );

        format = map.get( "FORMAT" );
        if ( format == null ) {
            throw new OWSException( "The FORMAT parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        String w = map.get( "WIDTH" );
        if ( w == null ) {
            throw new OWSException( "The WIDTH parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            width = parseInt( w );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The WIDTH parameter value is not a number (was " + w + ").",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        String h = map.get( "HEIGHT" );
        if ( h == null ) {
            throw new OWSException( "The HEIGHT parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            height = parseInt( h );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The HEIGHT parameter value is not a number (was " + h + ").",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        String t = map.get( "TRANSPARENT" );

        if ( parseStrict && ( t != null && !t.equalsIgnoreCase( "true" ) && !t.equalsIgnoreCase( "false" ) ) ) {
            throw new OWSException(
                            "The TRANSPARENT parameter value is not valid (was " + t
                            + "), expected is TRUE or FALSE.",
                            OWSException.INVALID_PARAMETER_VALUE );
        }
        transparent = t != null && t.equalsIgnoreCase( "true" );
        if ( transparent && ( format.indexOf( "gif" ) != -1 || format.indexOf( "png" ) != -1 ) ) {
            bgcolor = new Color( 255, 255, 255, 0 );
        } else {
            String col = map.get( "BGCOLOR" );
            if ( col != null ) {
                bgcolor = decode( col );
            }
        }

        dimensions = parseDimensionValues( map );

        String q = map.get( "QUERYBOXSIZE" );
        if ( q != null ) {
            try {
                queryBoxSize = Double.parseDouble( q );
            } catch ( NumberFormatException e ) {
                LOG.warn( "The QUERYBOXSIZE parameter could not be parsed." );
            }
        }

        handleVSPs( map, exts );
    }

    private void handleVSPs( Map<String, String> map, MapOptionsMaps defaults ) {
        if ( defaults == null ) {
            defaults = new MapOptionsMaps();
        }
        handleEnumVSP( Quality.class, getQualitySetter( extensions ), NORMAL, map.get( "QUALITY" ),
                       getQualityGetter( defaults ) );
        handleEnumVSP( Interpolation.class, getInterpolationSetter( extensions ), NEARESTNEIGHBOR,
                       map.get( "INTERPOLATION" ), getInterpolationGetter( defaults ) );
        handleEnumVSP( Antialias.class, getAntialiasSetter( extensions ), BOTH, map.get( "ANTIALIAS" ),
                       getAntialiasGetter( defaults ) );
        String maxFeatures = map.get( "MAX_FEATURES" );
        if ( maxFeatures == null ) {
            for ( LayerRef l : this.layers ) {
                Integer max = defaults.getMaxFeatures( l.getName() );
                if ( max == null ) {
                    max = 10000;
                    LOG.debug( "Using global max features setting of {}.", max );
                }
                extensions.setMaxFeatures( l.getName(), max );
            }
        } else {
            String[] mfs = maxFeatures.split( "," );
            if ( mfs.length == this.layers.size() ) {
                for ( int i = 0; i < mfs.length; ++i ) {
                    LayerRef cur = this.layers.get( i );
                    Integer def = defaults.getMaxFeatures( cur.getName() );
                    try {
                        Integer val = Integer.valueOf( mfs[i] );
                        extensions.setMaxFeatures( cur.getName(), def == null ? val : min( def, val ) );
                    } catch ( NumberFormatException e ) {
                        LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                        extensions.setMaxFeatures( cur.getName(), def == null ? 10000 : def );
                    }
                }
            } else {
                for ( int i = 0; i < mfs.length; ++i ) {
                    LayerRef cur = this.layers.get( i );
                    Integer def = defaults.getMaxFeatures( cur.getName() );
                    if ( mfs.length <= i ) {
                        try {
                            Integer val = Integer.valueOf( mfs[i] );
                            extensions.setMaxFeatures( cur.getName(), def == null ? val : min( def, val ) );
                        } catch ( NumberFormatException e ) {
                            LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                            extensions.setMaxFeatures( cur.getName(), def == null ? 10000 : def );
                        }
                    } else {
                        extensions.setMaxFeatures( cur.getName(), def == null ? 10000 : def );
                    }
                }
            }
        }
    }

    private <T extends Enum<T>> void handleEnumVSP( Class<T> enumType, MapOptionsSetter<T> setter, T defaultVal,
                                                    String vals, MapOptionsGetter<T> defaults ) {
        if ( vals == null ) {
            for ( LayerRef l : layers ) {
                T val = defaults.getOption( l.getName() );
                setter.setOption( l.getName(), val == null ? defaultVal : val );
            }
        } else {
            String[] ss = vals.split( "," );
            if ( ss.length == layers.size() ) {
                for ( int i = 0; i < ss.length; ++i ) {
                    T val = defaults.getOption( layers.get( i ).getName() );
                    try {
                        setter.setOption( layers.get( i ).getName(), Enum.valueOf( enumType, ss[i].toUpperCase() ) );
                    } catch ( IllegalArgumentException e ) {
                        setter.setOption( layers.get( i ).getName(), val == null ? defaultVal : val );
                        LOG.warn( "'{}' is not a valid value for '{}'. Using default value '{}' instead.",
                                  new Object[] { ss[i], enumType.getSimpleName(), val == null ? defaultVal : val } );
                    }
                }
            } else {
                for ( int i = 0; i < layers.size(); ++i ) {
                    T val = defaults.getOption( layers.get( i ).getName() );
                    if ( ss.length <= i ) {
                        setter.setOption( layers.get( i ).getName(), val == null ? defaultVal : val );
                    } else {
                        try {
                            setter.setOption( layers.get( i ).getName(), Enum.valueOf( enumType, ss[i].toUpperCase() ) );
                        } catch ( IllegalArgumentException e ) {
                            setter.setOption( layers.get( i ).getName(), val == null ? defaultVal : val );
                            LOG.warn( "'{}' is not a valid value for '{}'. Using default value '{}' instead.",
                                      new Object[] { ss[i], enumType.getSimpleName(), val == null ? defaultVal : val } );
                        }
                    }
                }
            }
        }
    }

    static HashMap<String, List<?>> parseDimensionValues( Map<String, String> map )
                            throws OWSException {
        HashMap<String, List<?>> dims = new HashMap<String, List<?>>();
        try {
            for ( Entry<String, String> entry : map.entrySet() ) {
                String key = entry.getKey();
                String val = entry.getValue();
                if ( key.equals( "TIME" ) ) {
                    dims.put( "time", (List<?>) parseTyped( parseDimensionValues( val, "time" ), true ) );
                }
                if ( key.equals( "ELEVATION" ) || key.startsWith( "DIM_" ) ) {
                    String name = key.equals( "ELEVATION" ) ? "elevation" : key.substring( 4 ).toLowerCase();
                    dims.put( name, (List<?>) parseTyped( parseDimensionValues( val, name ), false ) );
                }
            }
            return dims;
        } catch ( ParseException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( "The TIME parameter value was not in ISO8601 format: " + e.getLocalizedMessage(),
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
    }

    /**
     * @param value
     * @param name
     * @return the parsed list of strings or intervals
     * @throws OWSException
     */
    public static List<?> parseDimensionValues( String value, String name )
                            throws OWSException {
        DimensionsLexer lexer = new DimensionsLexer( new ANTLRStringStream( value ) );
        DimensionsParser parser = new DimensionsParser( new CommonTokenStream( lexer ) );
        try {
            parser.dimensionvalues();
            if ( parser.error != null ) {
                final String msg = "The value for the " + name + " dimension parameter was invalid: " + parser.error;
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE );
            }
            return parser.values;
        } catch ( RecognitionException e ) {
            // ignore exception, error message in the parser
        }
        return null;
    }

    private void parse130( Map<String, String> map, MapOptionsMaps exts, boolean parseStrict )
                            throws OWSException {
        String c = map.get( "CRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( "The CRS parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( "The BBOX parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        double[] vals;
        try {
            vals = splitAsDoubles( box, "," );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The value of the BBOX parameter is invalid: " + box,
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        if ( vals.length != 4 ) {
            throw new OWSException( "The value of the BBOX parameter had too many values: " + box,
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( "The maxx component of the BBOX was smaller that the minx component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( "The maxy component of the BBOX was smaller that the miny component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        bbox = getCRSAndEnvelope130( c, vals );
        crs = bbox.getCoordinateSystem();

        handleCommon( map, exts, parseStrict );
    }

    /**
     * @return the coordinate system of the bbox
     */
    public ICRS getCoordinateSystem() {
        return crs;
    }

    /**
     * @return the bbox
     */
    public Envelope getBoundingBox() {
        return bbox;
    }

    /**
     * @return a copy of the layers list
     */
    @Override
    public LinkedList<LayerRef> getLayers() {
        return new LinkedList<LayerRef>( layers );
    }

    /**
     * @return a copy of the styles list
     */
    public LinkedList<StyleRef> getStyles() {
        return new LinkedList<StyleRef>( styles );
    }

    /**
     * @return the image format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the transparent parameter
     */
    public boolean getTransparent() {
        return transparent;
    }

    /**
     * @return the desired background color
     */
    public Color getBgColor() {
        return bgcolor;
    }

    /**
     * @param crs
     */
    public void setCoordinateSystem( ICRS crs ) {
        this.crs = crs;
        bbox = fac.createEnvelope( bbox.getMin().getAsArray(), bbox.getMax().getAsArray(), crs );
    }

    /**
     * @return the scale as WMS 1.3.0/SLD scale
     */
    @Override
    public double getScale() {
        return scale;
    }

    /**
     * @return the value of the pixel size parameter (default is 0.00028 m).
     */
    public double getPixelSize() {
        return pixelSize;
    }

    /**
     * @return max(horizontal/vertical) resolution
     */
    public double getResolution() {
        return resolution;
    }

    /**
     * @return the get map extensions for the layers
     */
    public MapOptionsMaps getRenderingOptions() {
        return extensions;
    }

    /**
     * @return the envelope that should be used for backend queries
     */
    public Envelope getQueryBox() {
        if ( queryBoxSize < 0 ) {
            return bbox;
        }
        double minx = bbox.getMin().get0();
        double miny = bbox.getMin().get1();
        double maxx = bbox.getMax().get0();
        double maxy = bbox.getMax().get1();

        double w = bbox.getSpan0();
        double h = bbox.getSpan1();
        double dx = ( w * queryBoxSize - w ) / 2;
        double dy = ( h * queryBoxSize - h ) / 2;

        minx -= dx;
        miny -= dy;
        maxx += dx;
        maxy += dy;

        return new GeometryFactory().createEnvelope( minx, miny, maxx, maxy, bbox.getCoordinateSystem() );
    }

    /**
     * @return the KVP map of parameters. May not be accurate/empty, especially if this object has been created by some
     *         other means than a KVP request.
     */
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    /**
     * @param crs
     * @return the auto crs as defined in WMS 1.1.1 spec Annex E
     */
    public static ICRS getCRS111( String crs ) {
        if ( crs.startsWith( "AUTO:" ) ) {
            String[] cs = crs.split( ":" )[1].split( "," );
            int id = Integer.parseInt( cs[0] );
            // this is not supported
            // int units = Integer.parseInt( cs[1] );
            double lon0 = Double.parseDouble( cs[2] );
            double lat0 = Double.parseDouble( cs[3] );

            return Utils.getAutoCRS( id, lon0, lat0 );
        }
        return CRSManager.getCRSRef( crs, true );
    }

    /**
     * @param crs
     * @param bbox
     * @return a new CRS
     */
    public static Envelope getCRSAndEnvelope130( String crs, double[] bbox ) {
        if ( crs.startsWith( "AUTO2:" ) ) {
            String[] cs = crs.split( ":" )[1].split( "," );
            int id = Integer.parseInt( cs[0] );
            // this is not supported
            double factor = Double.parseDouble( cs[1] );
            double lon0 = Double.parseDouble( cs[2] );
            double lat0 = Double.parseDouble( cs[3] );

            return new GeometryFactory().createEnvelope( factor * bbox[0], factor * bbox[1], factor * bbox[2],
                                                         factor * bbox[3], Utils.getAutoCRS( id, lon0, lat0 ) );
        }
        ICRS crsRef = CRSManager.getCRSRef( crs );
        try {
            crsRef = CRSUtils.getAxisAwareCrs( crsRef );
        } catch ( Exception e ) {
            LOG.warn( "Unable to determine axis-aware variant of '" + crs + "'. Continuing." );
        }
        return new GeometryFactory().createEnvelope( bbox[0], bbox[1], bbox[2], bbox[3], crsRef );
    }

    /**
     * @return null, or a map with parameters that should be overridden when used as client parameter object.
     */
    public Map<String, String> getOverriddenParameters() {
        return overriddenParameters;
    }

}
