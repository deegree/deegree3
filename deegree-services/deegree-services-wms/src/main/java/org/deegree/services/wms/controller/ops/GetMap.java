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

import static java.awt.Color.decode;
import static java.awt.Color.white;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.CollectionUtils.unzipPair;
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
import static org.deegree.services.i18n.Messages.get;
import static org.deegree.services.wms.controller.sld.SLDParser.parse;
import static org.deegree.style.utils.Styles.getStyleFilters;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.logical.And;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.dims.DimensionsLexer;
import org.deegree.layer.dims.DimensionsParser;
import org.deegree.rendering.r2d.RenderHelper;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.MapOptionsGetter;
import org.deegree.rendering.r2d.context.MapOptions.MapOptionsSetter;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.rendering.r2d.context.MapOptionsMaps;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.StyleRegistry;
import org.deegree.services.wms.controller.WMSController111;
import org.deegree.services.wms.controller.WMSController130;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * Constructors must make sure there is an equal number of layers and styles, the VSP maps are filled, and the
 * scale/resolution are calculated properly. Also, width/height and envelope must obviously be set to reasonable values.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(trace = "logs stack traces", debug = "logs request scale", warn = "logs if scale could not be calculated, also logs values of invalid VSPs, like ANTIALIAS, PIXELSIZE etc.", error = "logs errors when handling dimension values")
public class GetMap {

    private static final Logger LOG = getLogger( GetMap.class );

    private static GeometryFactory fac = new GeometryFactory();

    private ICRS crs;

    private Envelope bbox;

    private LinkedList<Layer> layers = new LinkedList<Layer>();

    private LinkedList<Style> styles = new LinkedList<Style>();

    private MapOptionsMaps options = new MapOptionsMaps();

    private HashMap<String, Filter> filters = new HashMap<String, Filter>();

    private String format;

    private int width, height;

    private boolean transparent;

    private Color bgcolor = white;

    private HashMap<String, List<?>> dimensions = new HashMap<String, List<?>>();

    private double scale;

    private double pixelSize = 0.28;

    private double resolution;

    private Map<String, String> parameterMap = new HashMap<String, String>();

    /**
     * @param map
     * @param version
     * @param service
     * @throws OWSException
     */
    public GetMap( Map<String, String> map, Version version, MapService service ) throws OWSException {
        if ( version.equals( VERSION_111 ) ) {
            parse111( map, service );
        }
        if ( version.equals( VERSION_130 ) ) {
            parse130( map, service );
        }
        parameterMap.putAll( map );
        try {
            scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, pixelSize / 1000 );
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
     * @param service
     * @param layers
     * @param styles
     * @param width
     * @param height
     * @param boundingBox
     */
    public GetMap( MapService service, Collection<Layer> layers, Collection<Style> styles, int width, int height,
                   Envelope boundingBox ) {
        this.layers.addAll( layers );
        this.styles.addAll( styles );
        this.width = width;
        this.height = height;
        this.bbox = boundingBox;
        this.crs = boundingBox.getCoordinateSystem();
        this.bgcolor = white;
        format = "image/png";
        transparent = false;
        handleVSPs( service, new HashMap<String, String>() );
        try {
            scale = RenderHelper.calcScaleWMS130( width, height, bbox, crs, pixelSize / 1000 );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}' (adapted to pixel size of {}).", scale,
                       pixelSize );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( ReferenceResolvingException e ) {
            LOG.trace( "Stack trace:", e );
            LOG.warn( "The scale of a GetMap request could not be calculated: '{}'.", e.getLocalizedMessage() );
        }
    }

    private void parse111( Map<String, String> map, MapService service )
                            throws OWSException {
        String c = map.get( "SRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "SRS" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        crs = WMSController111.getCRS( c );

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "BBOX" ), OWSException.MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        // hack to work around ESRI ArcGIS Explorer localized bboxes...
        if ( vals.length == 8 ) {
            String[] ss = box.split( "," );
            vals = new double[] { parseDouble( ss[0] + "." + ss[1] ), parseDouble( ss[2] + "." + ss[3] ),
                                 parseDouble( ss[4] + "." + ss[5] ), parseDouble( ss[6] + "." + ss[7] ) };
        }
        if ( vals.length != 4 ) {
            throw new OWSException( get( "WMS.BBOX_WRONG_FORMAT", box ), OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( get( "WMS.MAXX_MINX" ), OWSException.INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( get( "WMS.MAXY_MINY" ), OWSException.INVALID_PARAMETER_VALUE );
        }

        bbox = fac.createEnvelope( new double[] { vals[0], vals[1] }, new double[] { vals[2], vals[3] }, crs );

        handleCommon( map, service );
    }

    static LinkedList<Layer> handleKVPLayers( List<String> lays, MapService service )
                            throws OWSException {
        LinkedList<Layer> layers = new LinkedList<Layer>();
        for ( String lay : lays ) {
            Layer l = service.getLayer( lay );
            if ( l == null ) {
                throw new OWSException( get( "WMS.LAYER_NOT_KNOWN", lay ), OWSException.LAYER_NOT_DEFINED );
            }
            layers.add( l );
        }
        return layers;
    }

    static LinkedList<Style> handleKVPStyles( String ss, MapService service, LinkedList<Layer> layers )
                            throws OWSException {
        LinkedList<Style> styles = new LinkedList<Style>();

        // result is a list with 'default' where default styles were requested
        StyleRegistry registry = service.getStyles();
        if ( ss.trim().isEmpty() ) {
            for ( Layer layer : layers ) {
                styles.add( registry.get( layer.getName(), null ) );
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
            if ( styls.length != layers.size() ) {
                throw new OWSException( get( "WMS.INVALID_NUMBER_OF_STYLES", layers.size(), styls.length ),
                                        OWSException.INVALID_PARAMETER_VALUE );
            }

            int i = -1;
            for ( Layer l : layers ) {
                if ( styls[++i].isEmpty() || styls[i].equals( "default" ) ) {
                    styles.add( registry.get( l.getName(), null ) );
                } else {
                    if ( !registry.hasStyle( l.getName(), styls[i] ) ) {
                        throw new OWSException( get( "WMS.UNDEFINED_STYLE", styls[i], l.getName() ),
                                                OWSException.STYLE_NOT_DEFINED );
                    }
                    styles.add( registry.get( l.getName(), styls[i] ) );
                }
            }
        }

        return styles;
    }

    private void handleCommon( Map<String, String> map, MapService service )
                            throws OWSException {
        String ls = map.get( "LAYERS" );
        String sld = map.get( "SLD" );
        String sldBody = map.get( "SLD_BODY" );

        LinkedList<String> layers = parseLayers( ls, sld, sldBody );

        String ss = map.get( "STYLES" );
        if ( ss == null && sld == null && sldBody == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "STYLES" ), OWSException.MISSING_PARAMETER_VALUE );
        }

        if ( sld == null && sldBody == null ) {
            this.layers = handleKVPLayers( layers, service );
            this.styles = handleKVPStyles( ss, service, this.layers );
        } else {
            // TODO think about whether STYLES has to be handled here as well
            handleSLD( sld, sldBody, layers, service );
        }

        parsePixelsize( map );

        format = map.get( "FORMAT" );
        if ( format == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "FORMAT" ), OWSException.MISSING_PARAMETER_VALUE );
        }

        parseSize( map );
        parseTransparency( map );

        dimensions = parseDimensionValues( map );

        handleVSPs( service, map );
    }

    private LinkedList<String> parseLayers( String ls, String sld, String sldBody )
                            throws OWSException {
        if ( ( ls == null || ls.trim().isEmpty() ) && sld == null && sldBody == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "LAYERS" ), OWSException.MISSING_PARAMETER_VALUE );
        }
        LinkedList<String> layers = ls == null ? new LinkedList<String>()
                                              : new LinkedList<String>( asList( ls.split( "," ) ) );

        if ( layers.size() == 1 && layers.get( 0 ).isEmpty() ) {
            layers.clear();
        }
        return layers;
    }

    private void parsePixelsize( Map<String, String> map ) {
        String psize = map.get( "PIXELSIZE" );
        if ( psize != null ) {
            try {
                pixelSize = Double.parseDouble( psize );
            } catch ( NumberFormatException e ) {
                LOG.warn( "The value of PIXELSIZE could not be parsed as a number." );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    private void parseTransparency( Map<String, String> map ) {
        String t = map.get( "TRANSPARENT" );
        transparent = t != null && t.equalsIgnoreCase( "true" );
        if ( transparent && ( format.indexOf( "gif" ) != -1 || format.indexOf( "png" ) != -1 ) ) {
            bgcolor = new Color( 255, 255, 255, 0 );
        } else {
            String col = map.get( "BGCOLOR" );
            if ( col != null ) {
                bgcolor = decode( col );
            }
        }
    }

    private void parseSize( Map<String, String> map )
                            throws OWSException {
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
    }

    private void handleVSPs( MapService service, Map<String, String> map ) {
        handleEnumVSP( Quality.class, getQualitySetter( options ), NORMAL, map.get( "QUALITY" ),
                       getQualityGetter( service.getExtensions() ) );
        handleEnumVSP( Interpolation.class, getInterpolationSetter( options ), NEARESTNEIGHBOR,
                       map.get( "INTERPOLATION" ), getInterpolationGetter( service.getExtensions() ) );
        handleEnumVSP( Antialias.class, getAntialiasSetter( options ), BOTH, map.get( "ANTIALIAS" ),
                       getAntialiasGetter( service.getExtensions() ) );
        String maxFeatures = map.get( "MAX_FEATURES" );
        if ( maxFeatures == null ) {
            for ( Layer l : this.layers ) {
                Integer max = service.getExtensions().getMaxFeatures( l.getName() );
                if ( max == null ) {
                    max = service.getGlobalMaxFeatures();
                    LOG.debug( "Using global max features setting of {}.", max );
                }
                options.setMaxFeatures( l.getName(), max );
            }
        } else {
            parseMaxFeatures( maxFeatures, service );
        }
    }

    private void parseMaxFeatures( String maxFeatures, MapService service ) {
        String[] mfs = maxFeatures.split( "," );
        if ( mfs.length == this.layers.size() ) {
            handleMultipleMaxFeatures( mfs, service );
        } else {
            for ( int i = 0; i < mfs.length; ++i ) {
                Layer cur = this.layers.get( i );
                Integer def = service.getExtensions().getMaxFeatures( cur.getName() );
                if ( mfs.length <= i ) {
                    try {
                        Integer val = Integer.valueOf( mfs[i] );
                        options.setMaxFeatures( cur.getName(), def == null ? val : min( def, val ) );
                    } catch ( NumberFormatException e ) {
                        LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                        options.setMaxFeatures( cur.getName(), def == null ? 10000 : def );
                    }
                } else {
                    options.setMaxFeatures( cur.getName(), def == null ? 10000 : def );
                }
            }
        }
    }

    private void handleMultipleMaxFeatures( String[] mfs, MapService service ) {
        for ( int i = 0; i < mfs.length; ++i ) {
            Layer cur = this.layers.get( i );
            Integer def = service.getExtensions().getMaxFeatures( cur.getName() );
            try {
                Integer val = Integer.valueOf( mfs[i] );
                options.setMaxFeatures( cur.getName(), def == null ? val : min( def, val ) );
            } catch ( NumberFormatException e ) {
                LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                options.setMaxFeatures( cur.getName(), def == null ? 10000 : def );
            }
        }
    }

    private <T extends Enum<T>> void handleEnumVSP( Class<T> enumType, MapOptionsSetter<T> setter, T defaultVal,
                                                    String vals, MapOptionsGetter<T> defaults ) {
        if ( vals == null ) {
            for ( Layer l : layers ) {
                T val = defaults.getOption( l.getName() );
                setter.setOption( l.getName(), val == null ? defaultVal : val );
            }
        } else {
            String[] ss = vals.split( "," );
            if ( ss.length == layers.size() ) {
                handleEnumVSPAllValues( ss, defaults, setter, enumType, defaultVal );
            } else {
                handleEnumVSPSomeValues( ss, defaults, setter, enumType, defaultVal );
            }
        }
    }

    private <T extends Enum<T>> void handleEnumVSPSomeValues( String[] ss, MapOptionsGetter<T> defaults,
                                                              MapOptionsSetter<T> setter, Class<T> enumType,
                                                              T defaultVal ) {
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

    private <T extends Enum<T>> void handleEnumVSPAllValues( String[] ss, MapOptionsGetter<T> defaults,
                                                             MapOptionsSetter<T> setter, Class<T> enumType, T defaultVal ) {
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
    }

    private void handleSLD( String sld, String sldBody, LinkedList<String> layers, MapService service )
                            throws OWSException {
        XMLInputFactory xmlfac = XMLInputFactory.newInstance();
        Pair<LinkedList<Layer>, LinkedList<Style>> pair = null;
        if ( sld != null ) {
            pair = parseSld( xmlfac, sld, service );
        }
        if ( sldBody != null ) {
            pair = parseSldBody( xmlfac, sldBody, service );
        }

        // if layers are referenced, clear the other layers out, else leave all in
        if ( pair != null && !layers.isEmpty() ) {
            // it might be in SLD that a layer has multiple styles, so we need to map to a list here
            HashMap<String, LinkedList<Pair<Layer, Style>>> lays = new HashMap<String, LinkedList<Pair<Layer, Style>>>();

            ListIterator<Layer> it = pair.first.listIterator();
            ListIterator<Style> st = pair.second.listIterator();
            while ( it.hasNext() ) {
                Layer l = it.next();
                Style s = st.next();
                String name = l.getName();
                if ( !layers.contains( name ) ) {
                    it.remove();
                    st.remove();
                } else {
                    LinkedList<Pair<Layer, Style>> list = lays.get( name );
                    if ( list == null ) {
                        list = new LinkedList<Pair<Layer, Style>>();
                        lays.put( name, list );
                    }
                    list.add( new Pair<Layer, Style>( l, s ) );
                }
            }

            // to get the order right, in case it's different from the SLD order
            orderLayers( layers, lays );
        } else {
            if ( pair != null ) {
                this.layers = pair.first;
                styles = pair.second;
            }
        }
    }

    private void orderLayers( LinkedList<String> layers, HashMap<String, LinkedList<Pair<Layer, Style>>> lays )
                            throws OWSException {
        for ( String name : layers ) {
            LinkedList<Pair<Layer, Style>> l = lays.get( name );
            if ( l == null ) {
                throw new OWSException( get( "WMS.SLD_LAYER_INVALID", name ), "InvalidParameterValue", "layers" );
            }
            Pair<ArrayList<Layer>, ArrayList<Style>> p = unzipPair( l );
            this.layers.addAll( p.first );
            styles.addAll( p.second );
        }
    }

    private Pair<LinkedList<Layer>, LinkedList<Style>> parseSldBody( XMLInputFactory xmlfac, String sldBody,
                                                                     MapService service )
                            throws OWSException {
        try {
            return parse( xmlfac.createXMLStreamReader( new StringReader( sldBody ) ), service, this );
        } catch ( XMLParsingException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD_BODY", e.getMessage() ), "InvalidParameterValue",
                                    "sld_body" );
        } catch ( XMLStreamException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD_BODY", e.getMessage() ), "InvalidParameterValue",
                                    "sld_body" );
        } catch ( ParseException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.DIMENSION_PARAMETER_INVALID", "embeddded in SLD", e.getMessage() ),
                                    "InvalidDimensionValue", "sld_body" );
        }
    }

    private Pair<LinkedList<Layer>, LinkedList<Style>> parseSld( XMLInputFactory xmlfac, String sld, MapService service )
                            throws OWSException {
        try {
            return parse( xmlfac.createXMLStreamReader( sld, new URL( sld ).openStream() ), service, this );
        } catch ( MalformedURLException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD", e.getMessage() ), "InvalidParameterValue", "sld" );
        } catch ( XMLStreamException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD", e.getMessage() ), "InvalidParameterValue", "sld" );
        } catch ( XMLParsingException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD", e.getMessage() ), "InvalidParameterValue", "sld" );
        } catch ( ParseException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.DIMENSION_PARAMETER_INVALID", "embeddded in SLD", e.getMessage() ),
                                    "InvalidDimensionValue", "sld" );
        } catch ( IOException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD", e.getMessage() ), "InvalidParameterValue", "sld" );
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
            throw new OWSException( get( "WMS.TIME_PARAMETER_NOT_ISO_FORMAT", e.getLocalizedMessage() ),
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
        } catch ( RecognitionException e ) {
            // ignore exception, error message in the parser
        }

        if ( parser.error != null ) {
            final String msg = get( "WMS.DIMENSION_PARAMETER_INVALID", name, parser.error );
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE );
        }

        return parser.values;
    }

    private void parse130( Map<String, String> map, MapService service )
                            throws OWSException {
        String c = map.get( "CRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "CRS" ), OWSException.MISSING_PARAMETER_VALUE );
        }

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

        bbox = WMSController130.getCRSAndEnvelope( c, vals );
        crs = bbox.getCoordinateSystem();

        handleCommon( map, service );
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
    public LinkedList<Layer> getLayers() {
        return new LinkedList<Layer>( layers );
    }

    /**
     * @return a copy of the styles list
     */
    public LinkedList<Style> getStyles() {
        return new LinkedList<Style>( styles );
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
     * @return returns a map with the requested dimension values
     */
    public HashMap<String, List<?>> getDimensions() {
        return dimensions;
    }

    /**
     * @param layer
     * @param filter
     */
    public void addFilter( String layer, Filter filter ) {
        if ( filters.get( layer ) != null ) {
            Operator oldop = ( (OperatorFilter) filters.get( layer ) ).getOperator();
            Operator snd = ( (OperatorFilter) filter ).getOperator();
            filters.put( layer, new OperatorFilter( new And( oldop, snd ) ) );
        } else {
            filters.put( layer, filter );
        }
    }

    /**
     * @param name
     * @param values
     */
    public void addDimensionValue( String name, List<?> values ) {
        dimensions.put( name, values );
    }

    /**
     * @param name
     * @param filter
     * @param style
     * @return a new filter for the layer, fulfilling the filter parameter as well
     */
    public Filter getFilterForLayer( String name, Filter filter, Style style ) {
        Filter sldFilter = getStyleFilters( style, getScale() );

        Filter extra = filters.get( name );
        if ( extra == null ) {
            extra = sldFilter;
        } else {
            if ( sldFilter != null ) {
                Operator op1 = ( (OperatorFilter) sldFilter ).getOperator();
                Operator op2 = ( (OperatorFilter) extra ).getOperator();
                extra = new OperatorFilter( new And( op1, op2 ) );
            }
        }
        if ( filter != null ) {
            if ( extra != null ) {
                Operator op = ( (OperatorFilter) extra ).getOperator();
                Operator op2 = ( (OperatorFilter) filter ).getOperator();
                return new OperatorFilter( new And( op, op2 ) );
            }
            return filter;
        }
        return extra;
    }

    /**
     * @return the scale as WMS 1.3.0/SLD scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return the value of the pixel size parameter (default is 0.28 mm).
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
     * @return the extension parameter values for this request
     */
    public MapOptionsMaps getRenderingOptions() {
        return options;
    }

    /**
     * @return the KVP map of parameters. May not be accurate/empty, especially if this object has been created by some
     *         other means than a KVP request.
     */
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

}
